package edu.seneca;

import edu.seneca.modelo.torneos.BonoDescuento;
import edu.seneca.modelo.torneos.Torneo;
import edu.seneca.modelo.usuarios.Socio;
import edu.seneca.modelo.ventas.TipoUbicacion;
import edu.seneca.modelo.ventas.Venta;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BonosYVentasTest {

    private static final double PRECIO_RAQUETA = 250000 * 1.19;

    private ClubDeportivoSeneca club;
    private Socio laura;
    private BonoDescuento bono;

    @BeforeEach
    void ganarUnBono() throws Exception {
        club = ClubDePrueba.crear();
        Torneo torneo = ClubDePrueba.tenisRecreativo(club, 8);
        club.inscribirEnTorneo(torneo.getId(), "S1", true);
        club.inscribirEnTorneo(torneo.getId(), "S4", true);
        club.iniciarTorneo(torneo.getId());
        club.programarEncuentro(torneo.getId(), "S1", "S4");
        club.registrarResultadoEncuentro(torneo.getId(), 1, "6-2", "S1");
        club.finalizarTorneo(torneo.getId(), "S1");

        laura = club.buscarSocio("S1");
        bono = laura.getBonos().get(0);
    }

    private Venta ultimaVenta() {
        return club.getVentas().get(club.getVentas().size() - 1);
    }

    @Test
    @DisplayName("hu-tn-s06 exito: el bono ganado aparece disponible, con su valor y torneo de origen")
    void consultarBonos() throws Exception {
        List<BonoDescuento> bonos = club.consultarBonos("S1");

        assertEquals(1, bonos.size());
        assertEquals(30000, bonos.get(0).getValor());
        assertEquals("Recreativo 1", bonos.get(0).getTorneoOrigen());
        assertEquals("disponible", bonos.get(0).getEstado());
    }

    @Test
    @DisplayName("hu-tn-s06 vacio: un socio sin bonos recibe una lista vacia")
    void sinBonos() throws Exception {
        assertTrue(club.consultarBonos("S4").isEmpty());
    }

    @Test
    @DisplayName("hu-tn-s07 exito: el total se reduce en el valor del bono y el bono queda usado")
    void usarBono() throws Exception {
        boolean ok = club.procesarVenta("S1", List.of("P1"), "Tarjeta", TipoUbicacion.CAFETERIA, bono.getId(), 0);

        assertTrue(ok);
        assertEquals(PRECIO_RAQUETA - 30000, ultimaVenta().getTotal(), 0.01);
        assertTrue(bono.isUsado());
        assertEquals("usado", club.consultarBonos("S1").get(0).getEstado());
    }

    @Test
    @DisplayName("hu-tn-s07 exito: si el bono vale mas que la compra, el total queda en 0")
    void bonoMayorQueLaCompra() throws Exception {
        club.procesarVenta("S1", List.of("P6"), "Tarjeta", TipoUbicacion.CAFETERIA, bono.getId(), 0);

        assertEquals(0, ultimaVenta().getTotal());
        assertTrue(bono.isUsado());
    }

    @Test
    @DisplayName("hu-tn-s07 rechazo: bono ya usado")
    void bonoYaUsado() throws Exception {
        club.procesarVenta("S1", List.of("P6"), "Tarjeta", TipoUbicacion.CAFETERIA, bono.getId(), 0);
        int ventas = club.getVentas().size();

        Exception error = assertThrows(Exception.class, () -> club.procesarVenta("S1", List.of("P1"), "Tarjeta", TipoUbicacion.CAFETERIA, bono.getId(), 0));

        assertTrue(error.getMessage().contains("ya fue usado"));
        assertEquals(ventas, club.getVentas().size());
        assertEquals(8, club.buscarProducto("P1").getStock());
    }

    @Test
    @DisplayName("hu-tn-s07 rechazo: bono ajeno o inexistente")
    void bonoAjenoOInexistente() {
        assertThrows(Exception.class, () -> club.procesarVenta("S4", List.of("P1"), "Tarjeta", TipoUbicacion.CAFETERIA, bono.getId(), 0));
        assertThrows(Exception.class, () -> club.procesarVenta("S1", List.of("P1"), "Tarjeta", TipoUbicacion.CAFETERIA, "B-NO-EXISTE", 0));
        assertFalse(bono.isUsado());
        assertTrue(club.getVentas().isEmpty());
    }

    @Test
    @DisplayName("hu-tn-s07 rechazo: no se acumula con puntos de fidelidad")
    void noAcumulableConPuntos() {
        laura.sumarPuntosFidelidad(5000);

        Exception error = assertThrows(Exception.class, () -> club.procesarVenta("S1", List.of("P1"), "Tarjeta", TipoUbicacion.CAFETERIA, bono.getId(), 1000));

        assertTrue(error.getMessage().contains("acumulable"));
        assertFalse(bono.isUsado());
        assertEquals(5000, laura.getPuntosFidelidad());
    }

    @Test
    @DisplayName("hu-tn-s07 rechazo: un empleado, que ya tiene su descuento, no puede usar un bono")
    void noAcumulableConDescuentoDeEmpleado() {
        assertThrows(Exception.class, () -> club.procesarVenta("E1", List.of("P1"), "Tarjeta", TipoUbicacion.CAFETERIA, bono.getId(), 0));
        assertFalse(bono.isUsado());
    }

    @Test
    @DisplayName("hu-tn-s07 rechazo: compra sin existencias; el bono sigue disponible")
    void compraSinExistencias() throws Exception {
        boolean ok = club.procesarVenta("S1", List.of("P4"), "Tarjeta", TipoUbicacion.CAFETERIA, bono.getId(), 0);

        assertFalse(ok);
        assertFalse(bono.isUsado());
        assertTrue(club.getVentas().isEmpty());
    }

    @Test
    @DisplayName("una compra rechazada a mitad de carrito no descuenta inventario de los productos anteriores")
    void compraRechazadaNoTocaInventario() throws Exception {
        boolean ok = club.procesarVenta("S1", List.of("P1", "P6", "P6", "P6"), "Tarjeta", TipoUbicacion.CAFETERIA);

        assertFalse(ok);
        assertEquals(8, club.buscarProducto("P1").getStock());
        assertEquals(2, club.buscarProducto("P6").getStock());
    }

    @Test
    @DisplayName("redimir puntos de fidelidad reduce el total y descuenta los puntos")
    void redimirPuntos() throws Exception {
        laura.sumarPuntosFidelidad(5000);

        club.procesarVenta("S1", List.of("P1"), "Tarjeta", TipoUbicacion.CAFETERIA, null, 5000);

        double total = PRECIO_RAQUETA - 5000;
        assertEquals(total, ultimaVenta().getTotal(), 0.01);
        assertEquals((int) (total * 0.02), laura.getPuntosFidelidad(), "se gastan los 5000 y se ganan los de esta compra");
        assertThrows(Exception.class, () -> club.procesarVenta("S4", List.of("P1"), "Tarjeta", TipoUbicacion.CAFETERIA, null, 10));
    }
}
