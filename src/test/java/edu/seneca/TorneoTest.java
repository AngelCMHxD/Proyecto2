package edu.seneca;

import edu.seneca.modelo.torneos.Encuentro;
import edu.seneca.modelo.torneos.EstadoTorneo;
import edu.seneca.modelo.torneos.InscripcionTorneo;
import edu.seneca.modelo.torneos.Modalidad;
import edu.seneca.modelo.torneos.TipoCupo;
import edu.seneca.modelo.torneos.Torneo;
import edu.seneca.modelo.torneos.TorneoRecreativo;
import edu.seneca.modelo.usuarios.NivelDeportivo;
import edu.seneca.modelo.usuarios.Socio;
import edu.seneca.modelo.usuarios.Usuario;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TorneoTest {

    private Torneo torneo;

    @BeforeEach
    void crearTorneo() {
        torneo = new TorneoRecreativo("T1", "Abierto", "Tenis", "INTERMEDIO", Modalidad.SENCILLOS, 5, ClubDePrueba.SABADO, 30000);
    }

    private InscripcionTorneo inscribir(String id, TipoCupo cupo) throws Exception {
        List<Usuario> jugadores = new ArrayList<>();
        jugadores.add(new Socio(id, "Socio " + id, id, "clave", NivelDeportivo.INTERMEDIO, LocalDate.of(2000, 1, 1)));
        InscripcionTorneo inscripcion = new InscripcionTorneo(jugadores, cupo, 0);
        torneo.inscribir(inscripcion);
        return inscripcion;
    }

    @ParameterizedTest(name = "{0} cupos reservan {1} preferenciales")
    @CsvSource({"1, 1", "2, 1", "5, 1", "6, 2", "10, 2", "11, 3", "16, 4", "20, 4"})
    @DisplayName("los cupos preferenciales son el 20 % redondeado hacia arriba")
    void cuposPreferencialesRedondeanHaciaArriba(int cupos, int esperados) {
        assertEquals(esperados, Torneo.calcularCuposPreferenciales(cupos));
    }

    @Test
    @DisplayName("las fechas del club de prueba caen en lunes y sabado")
    void fechasDePrueba() {
        assertEquals(DayOfWeek.MONDAY, ClubDePrueba.LUNES.getDayOfWeek());
        assertEquals(DayOfWeek.SATURDAY, ClubDePrueba.SABADO.getDayOfWeek());
    }

    @Test
    @DisplayName("un torneo nuevo tiene inscripciones abiertas y sus cupos repartidos")
    void torneoNuevo() {
        assertEquals(EstadoTorneo.INSCRIPCIONES_ABIERTAS, torneo.getEstado());
        assertEquals(1, torneo.getCuposPreferenciales());
        assertEquals(4, torneo.getCuposRegulares());
        assertTrue(torneo.getInscripciones().isEmpty());
    }

    @Test
    @DisplayName("asignarcupo da preferencial al que califica y regular al resto")
    void asignarCupo() throws Exception {
        assertEquals(TipoCupo.PREFERENCIAL, torneo.asignarCupo(true));
        assertEquals(TipoCupo.REGULAR, torneo.asignarCupo(false));

        inscribir("a", TipoCupo.PREFERENCIAL);
        assertEquals(TipoCupo.REGULAR, torneo.asignarCupo(true), "sin preferenciales libres, el que califica toma uno regular");
    }

    @Test
    @DisplayName("sin cupos regulares, quien no califica es rechazado aunque quede un preferencial")
    void torneoLleno() throws Exception {
        for (int i = 0; i < 4; i++) {
            inscribir("s" + i, TipoCupo.REGULAR);
        }

        assertFalse(torneo.hayCupo(TipoCupo.REGULAR));
        assertTrue(torneo.hayCupo(TipoCupo.PREFERENCIAL));
        assertThrows(Exception.class, () -> torneo.asignarCupo(false));
    }

    @Test
    @DisplayName("iniciar libera los cupos preferenciales vacios como regulares")
    void iniciarLiberaPreferenciales() throws Exception {
        inscribir("a", TipoCupo.REGULAR);
        inscribir("b", TipoCupo.REGULAR);

        torneo.iniciar();

        assertEquals(EstadoTorneo.EN_CURSO, torneo.getEstado());
        assertEquals(0, torneo.getCuposPreferenciales());
        assertEquals(5, torneo.getCuposRegulares());
    }

    @Test
    @DisplayName("no se inicia con menos de 2 participantes y el mensaje dice cuantos faltan")
    void iniciarConPocosParticipantes() throws Exception {
        inscribir("a", TipoCupo.REGULAR);

        Exception error = assertThrows(Exception.class, () -> torneo.iniciar());

        assertTrue(error.getMessage().contains("faltan 1"), error.getMessage());
        assertEquals(EstadoTorneo.INSCRIPCIONES_ABIERTAS, torneo.getEstado());
    }

    @Test
    @DisplayName("con el torneo en curso no se puede inscribir ni retirar")
    void inscripcionesCerradasAlIniciar() throws Exception {
        InscripcionTorneo a = inscribir("a", TipoCupo.REGULAR);
        inscribir("b", TipoCupo.REGULAR);
        torneo.iniciar();

        assertThrows(Exception.class, () -> inscribir("c", TipoCupo.REGULAR));
        assertThrows(Exception.class, () -> torneo.retirar(a));
        assertEquals(2, torneo.getInscripciones().size());
    }

    @Test
    @DisplayName("el resultado de un encuentro exige marcador y un ganador que lo haya jugado")
    void resultadoDeEncuentro() throws Exception {
        InscripcionTorneo a = inscribir("a", TipoCupo.REGULAR);
        InscripcionTorneo b = inscribir("b", TipoCupo.REGULAR);
        InscripcionTorneo c = inscribir("c", TipoCupo.REGULAR);
        torneo.iniciar();
        Encuentro encuentro = torneo.programarEncuentro(a, b);

        assertThrows(Exception.class, () -> torneo.registrarResultado(1, "6-0", c));
        assertThrows(Exception.class, () -> torneo.registrarResultado(1, " ", a));
        assertThrows(Exception.class, () -> torneo.registrarResultado(2, "6-0", a));
        assertThrows(Exception.class, () -> torneo.programarEncuentro(a, a));
        assertFalse(encuentro.tieneResultado());

        torneo.registrarResultado(1, "6-0", a);
        assertTrue(encuentro.tieneResultado());
        assertSame(a, encuentro.getGanador());
    }

    @Test
    @DisplayName("la tabla de posiciones ordena por victorias y deja primero al ganador declarado")
    void tablaDePosiciones() throws Exception {
        InscripcionTorneo a = inscribir("a", TipoCupo.REGULAR);
        InscripcionTorneo b = inscribir("b", TipoCupo.REGULAR);
        InscripcionTorneo c = inscribir("c", TipoCupo.REGULAR);
        torneo.iniciar();
        torneo.programarEncuentro(a, b);
        torneo.programarEncuentro(b, c);
        torneo.programarEncuentro(c, a);
        torneo.registrarResultado(1, "6-4", b);
        torneo.registrarResultado(2, "6-3", b);
        torneo.registrarResultado(3, "7-5", c);

        assertEquals(List.of(b, c, a), torneo.getPosiciones());

        torneo.finalizar(c);
        assertEquals(List.of(c, b, a), torneo.getPosiciones());
        assertSame(c, torneo.getGanador());
    }
}
