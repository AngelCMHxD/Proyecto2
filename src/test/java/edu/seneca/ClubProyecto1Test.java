package edu.seneca;

import edu.seneca.modelo.usuarios.Administrador;
import edu.seneca.modelo.usuarios.Socio;
import edu.seneca.modelo.ventas.TipoUbicacion;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static edu.seneca.ClubDePrueba.SABADO;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ClubProyecto1Test {

    private ClubDeportivoSeneca club;

    @BeforeEach
    void crearClub() throws Exception {
        club = ClubDePrueba.crear();
    }

    @Test
    @DisplayName("autenticacion: entra con login y password correctos y se rechaza cualquier otro caso")
    void autenticacion() throws Exception {
        assertEquals("S1", club.autenticar("laura", "laura123").getId());
        assertTrue(club.autenticar("admin", "admin123") instanceof Administrador);
        assertThrows(Exception.class, () -> club.autenticar("laura", "LAURA123"));
        assertThrows(Exception.class, () -> club.autenticar("nadie", "laura123"));
    }

    @Test
    @DisplayName("reserva: se confirma si la instalacion esta libre y los niveles son compatibles")
    void reserva() throws Exception {
        assertTrue(club.procesarReserva("S1", "TEN-1", SABADO, LocalTime.of(9, 0), "S4"));
        assertEquals(1, club.getReservas().size());

        assertFalse(club.procesarReserva("S6", "TEN-1", SABADO, LocalTime.of(9, 0), null), "mismo horario");
        assertFalse(club.procesarReserva("S3", "TEN-1", SABADO, LocalTime.of(11, 0), "S2"), "principiante contra avanzado");
        assertEquals(1, club.getReservas().size());
        assertThrows(Exception.class, () -> club.procesarReserva("S1", "NO-EXISTE", SABADO, LocalTime.of(9, 0), null));
    }

    @Test
    @DisplayName("venta a socio: descuenta inventario, cobra impuestos y suma el 2 % en puntos")
    void ventaASocio() throws Exception {
        assertTrue(club.procesarVenta("S1", List.of("P1", "P6"), "Tarjeta", TipoUbicacion.CAFETERIA));

        double total = 250000 * 1.19 + 3000 * 1.08;
        assertEquals(total, club.getVentas().get(0).getTotal(), 0.01);
        assertEquals(7, club.buscarProducto("P1").getStock());
        assertEquals(1, club.buscarProducto("P6").getStock());
        assertEquals((int) (total * 0.02), club.buscarSocio("S1").getPuntosFidelidad());
    }

    @Test
    @DisplayName("venta a empleado: aplica el 15 % de descuento")
    void ventaAEmpleado() throws Exception {
        assertTrue(club.procesarVenta("E1", List.of("P1"), "Tarjeta", TipoUbicacion.CAFETERIA));
        assertEquals(250000 * 1.19 * 0.85, club.getVentas().get(0).getTotal(), 0.01);
    }

    @Test
    @DisplayName("venta rechazada: bebida caliente en cancha de padel o mesa techada, o producto agotado")
    void ventaRechazada() throws Exception {
        assertFalse(club.procesarVenta("S1", List.of("P5"), "Efectivo", TipoUbicacion.CANCHA_PADEL));
        assertFalse(club.procesarVenta("S1", List.of("P5"), "Efectivo", TipoUbicacion.MESA_TECHADA));
        assertFalse(club.procesarVenta("S1", List.of("P4"), "Efectivo", TipoUbicacion.CAFETERIA));
        assertTrue(club.procesarVenta("S1", List.of("P5"), "Efectivo", TipoUbicacion.CAFETERIA));
        assertEquals(1, club.getVentas().size());
    }

    @Test
    @DisplayName("inscripcion a equipo: un socio por deporte y numeros de camiseta unicos")
    void inscripcionAEquipo() throws Exception {
        assertTrue(club.procesarInscripcion("S1", "FUT-C", "Delantera", 9));
        assertFalse(club.procesarInscripcion("S1", "FUT-A", "Delantera", 99), "ya juega futbol en otro equipo");
        assertFalse(club.procesarInscripcion("S4", "FUT-C", "Portero", 9), "numero repetido");
        assertTrue(club.procesarInscripcion("S1", "BAL-A", "Base", 9), "otro deporte si se permite");
        assertFalse(club.procesarInscripcion("S4", "FUT-SUB15", "Portero", 1), "un adulto no entra a Sub-15");
        assertEquals(4, club.buscarEquipo("FUT-C").getInscripciones().size());
    }

    @Test
    @DisplayName("sanciones: dos amarillas o una roja bloquean el proximo partido")
    void sanciones() throws Exception {
        Socio laura = club.buscarSocio("S1");
        Socio daniel = club.buscarSocio("S4");

        club.evaluarReglasSancion(laura, 1, 0);
        assertFalse(laura.isBloqueadoProximoPartido());
        club.evaluarReglasSancion(laura, 1, 0);
        assertTrue(laura.isBloqueadoProximoPartido());

        club.evaluarReglasSancion(daniel, 0, 1);
        assertTrue(daniel.isBloqueadoProximoPartido());
    }

    @Test
    @DisplayName("estadisticas: registrar sobre un partido que no existe se rechaza")
    void estadisticaDePartidoInexistente() {
        Map<String, Integer> datos = new HashMap<>();
        datos.put("goles", 1);
        assertThrows(Exception.class, () -> club.registrarEstadistica("NO-EXISTE", "S1", datos));
    }
}
