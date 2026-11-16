package edu.seneca;

import edu.seneca.modelo.torneos.EstadoTorneo;
import edu.seneca.modelo.torneos.TipoCupo;
import edu.seneca.modelo.torneos.Torneo;
import edu.seneca.modelo.torneos.TorneoCompetitivo;
import edu.seneca.persistencia.GeneradorDatosPrueba;
import edu.seneca.persistencia.PersistenciaClub;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PersistenciaTest {

    @TempDir
    Path carpeta;

    @Test
    @DisplayName("torneos, inscripciones, encuentros, premios y bonos sobreviven a guardar y cargar")
    void guardarYCargar() throws Exception {
        String archivo = carpeta.resolve("datos/club.dat").toString();
        ClubDeportivoSeneca club = ClubDePrueba.crear();

        Torneo recreativo = ClubDePrueba.tenisRecreativo(club, 5);
        club.inscribirEnTorneo(recreativo.getId(), "S1", true);
        club.inscribirEnTorneo(recreativo.getId(), "S4", true);
        club.iniciarTorneo(recreativo.getId());
        club.programarEncuentro(recreativo.getId(), "S1", "S4");
        club.registrarResultadoEncuentro(recreativo.getId(), 1, "6-2", "S1");
        club.finalizarTorneo(recreativo.getId(), "S1");

        Torneo competitivo = ClubDePrueba.tenisCompetitivo(club, 5);
        club.inscribirEnTorneo(competitivo.getId(), "S1", true);
        club.inscribirEnTorneo(competitivo.getId(), "E2", true);

        PersistenciaClub.guardar(club, archivo);
        ClubDeportivoSeneca cargado = PersistenciaClub.cargar(archivo);

        Torneo finalizado = cargado.buscarTorneo(recreativo.getId());
        assertEquals(EstadoTorneo.FINALIZADO, finalizado.getEstado());
        assertEquals("Laura", finalizado.getGanador().getNombre());
        assertEquals("6-2", finalizado.getEncuentros().get(0).getMarcador());
        assertEquals(1, cargado.consultarBonos("S1").size());
        assertFalse(cargado.consultarBonos("S1").get(0).isUsado());

        Torneo abierto = cargado.buscarTorneo(competitivo.getId());
        assertTrue(abierto instanceof TorneoCompetitivo);
        assertEquals(20000, abierto.getTotalRecaudado());
        assertEquals(1, abierto.contarOcupados(TipoCupo.PREFERENCIAL));
        assertNotNull(abierto.buscarInscripcion(cargado.buscarUsuario("E2")));

        Torneo nuevo = ClubDePrueba.tenisRecreativo(cargado, 5);
        assertEquals("T3", nuevo.getId());
        cargado.inscribirEnTorneo(abierto.getId(), "S4", true);
        cargado.iniciarTorneo(abierto.getId());
        assertEquals(EstadoTorneo.EN_CURSO, abierto.getEstado());
    }

    @Test
    @DisplayName("los datos de prueba predeterminados se generan, se guardan y se cargan completos")
    void datosDePrueba() throws Exception {
        String archivo = carpeta.resolve("club.dat").toString();

        PersistenciaClub.guardar(GeneradorDatosPrueba.crearClub(), archivo);
        ClubDeportivoSeneca club = PersistenciaClub.cargar(archivo);

        assertEquals("Marta Leon", club.autenticar("admin", "admin123").getNombre());
        assertFalse(club.getTorneos().isEmpty(), "los datos de prueba incluyen torneos");
        assertFalse(club.consultarTorneos("En curso", null, null).isEmpty());
        assertFalse(club.consultarTorneos("Finalizado", null, null).isEmpty());
    }
}
