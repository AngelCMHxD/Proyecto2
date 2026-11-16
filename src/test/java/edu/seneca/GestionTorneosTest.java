package edu.seneca;

import edu.seneca.modelo.torneos.EstadoTorneo;
import edu.seneca.modelo.torneos.Modalidad;
import edu.seneca.modelo.torneos.TipoTorneo;
import edu.seneca.modelo.torneos.Torneo;
import edu.seneca.modelo.torneos.TorneoCompetitivo;
import edu.seneca.modelo.usuarios.Socio;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;

import static edu.seneca.ClubDePrueba.SABADO;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GestionTorneosTest {

    private ClubDeportivoSeneca club;

    @BeforeEach
    void crearClub() throws Exception {
        club = ClubDePrueba.crear();
    }

    private Exception rechazo(org.junit.jupiter.api.function.Executable accion) {
        return assertThrows(Exception.class, accion);
    }

    @Nested
    @DisplayName("hu-tn-a01 crear un torneo")
    class CrearTorneo {
        @Test
        @DisplayName("exito: queda registrado con el 20 % de cupos reservados y sin inscritos")
        void datosValidos() throws Exception {
            Torneo torneo = club.crearTorneo("Mini sub-15", "Futbol", "Sub-15", Modalidad.CONJUNTO, TipoTorneo.RECREATIVO, 8, SABADO, 0, 0, 25000);

            assertSame(torneo, club.buscarTorneo(torneo.getId()));
            assertEquals(EstadoTorneo.INSCRIPCIONES_ABIERTAS, torneo.getEstado());
            assertEquals(8, torneo.getCupos());
            assertEquals(2, torneo.getCuposPreferenciales());
            assertEquals(6, torneo.getCuposRegulares());
            assertTrue(torneo.getInscripciones().isEmpty());
        }

        @Test
        @DisplayName("exito: un competitivo guarda tarifa y porcentaje de premio")
        void competitivo() throws Exception {
            Torneo torneo = ClubDePrueba.tenisCompetitivo(club, 8);

            assertEquals(TipoTorneo.COMPETITIVO, torneo.getTipo());
            assertEquals(20000, torneo.getTarifa());
            assertEquals(0.5, ((TorneoCompetitivo) torneo).getPorcentajePremio());
            assertEquals("INTERMEDIO", torneo.getCategoria());
        }

        @Test
        @DisplayName("exito: puede haber varios torneos simultaneos del mismo deporte y categoria")
        void variosSimultaneos() throws Exception {
            Torneo uno = ClubDePrueba.tenisRecreativo(club, 8);
            Torneo dos = ClubDePrueba.tenisRecreativo(club, 8);

            assertNotEquals(uno.getId(), dos.getId());
            assertEquals(2, club.getTorneos().size());
        }

        @Test
        @DisplayName("rechazo: mismo nombre y misma fecha")
        void duplicado() throws Exception {
            club.crearTorneo("Copa", "Tenis", "Intermedio", Modalidad.SENCILLOS, TipoTorneo.RECREATIVO, 8, SABADO, 0, 0, 30000);
            rechazo(() -> club.crearTorneo("copa", "Tenis", "Avanzado", Modalidad.SENCILLOS, TipoTorneo.RECREATIVO, 8, SABADO, 0, 0, 30000));
        }

        @Test
        @DisplayName("rechazo: dato faltante, indicando cual")
        void datoFaltante() {
            Exception error = rechazo(() -> club.crearTorneo(" ", "Tenis", "Intermedio", Modalidad.SENCILLOS, TipoTorneo.RECREATIVO, 8, SABADO, 0, 0, 30000));
            assertTrue(error.getMessage().contains("nombre"));

            error = rechazo(() -> club.crearTorneo("Copa", "Tenis", "Intermedio", null, TipoTorneo.RECREATIVO, 8, SABADO, 0, 0, 30000));
            assertTrue(error.getMessage().contains("modalidad"));

            error = rechazo(() -> club.crearTorneo("Copa", "Tenis", "Intermedio", Modalidad.SENCILLOS, TipoTorneo.RECREATIVO, 8, null, 0, 0, 30000));
            assertTrue(error.getMessage().contains("fecha"));

            assertTrue(club.getTorneos().isEmpty());
        }

        @Test
        @DisplayName("rechazo: cupos menores o iguales a 0")
        void cuposInvalidos() {
            rechazo(() -> club.crearTorneo("Copa", "Tenis", "Intermedio", Modalidad.SENCILLOS, TipoTorneo.RECREATIVO, 0, SABADO, 0, 0, 30000));
            rechazo(() -> club.crearTorneo("Copa", "Tenis", "Intermedio", Modalidad.SENCILLOS, TipoTorneo.RECREATIVO, -3, SABADO, 0, 0, 30000));
        }

        @Test
        @DisplayName("rechazo: disciplina o categoria que no existe")
        void disciplinaOCategoriaInexistente() {
            rechazo(() -> club.crearTorneo("Copa", "Ajedrez", "Intermedio", Modalidad.SENCILLOS, TipoTorneo.RECREATIVO, 8, SABADO, 0, 0, 30000));
            rechazo(() -> club.crearTorneo("Copa", "Tenis", "Experto", Modalidad.SENCILLOS, TipoTorneo.RECREATIVO, 8, SABADO, 0, 0, 30000));
            rechazo(() -> club.crearTorneo("Copa", "Futbol", "Sub-99", Modalidad.CONJUNTO, TipoTorneo.RECREATIVO, 8, SABADO, 0, 0, 30000));
            rechazo(() -> club.crearTorneo("Copa", "Tenis", "Mayores", Modalidad.CONJUNTO, TipoTorneo.RECREATIVO, 8, SABADO, 0, 0, 30000));
            rechazo(() -> club.crearTorneo("Copa", "Futbol", "Intermedio", Modalidad.SENCILLOS, TipoTorneo.RECREATIVO, 8, SABADO, 0, 0, 30000));
        }

        @Test
        @DisplayName("rechazo: competitivo sin tarifa, o recreativo con tarifa")
        void tarifaIncoherente() {
            rechazo(() -> club.crearTorneo("Copa", "Tenis", "Intermedio", Modalidad.SENCILLOS, TipoTorneo.COMPETITIVO, 8, SABADO, 0, 50, 0));
            rechazo(() -> club.crearTorneo("Copa", "Tenis", "Intermedio", Modalidad.SENCILLOS, TipoTorneo.COMPETITIVO, 8, SABADO, 20000, 0, 0));
            rechazo(() -> club.crearTorneo("Copa", "Tenis", "Intermedio", Modalidad.SENCILLOS, TipoTorneo.RECREATIVO, 8, SABADO, 20000, 0, 30000));
            rechazo(() -> club.crearTorneo("Copa", "Tenis", "Intermedio", Modalidad.SENCILLOS, TipoTorneo.RECREATIVO, 8, SABADO, 0, 0, 0));
        }

        @Test
        @DisplayName("rechazo: conjunto con cupos bajo el minimo del deporte (7, 5 o 6) o dobles con menos de 2")
        void cuposBajoElMinimo() throws Exception {
            rechazo(() -> club.crearTorneo("F", "Futbol", "Mayores", Modalidad.CONJUNTO, TipoTorneo.RECREATIVO, 6, SABADO, 0, 0, 30000));
            rechazo(() -> club.crearTorneo("B", "Baloncesto", "Mayores", Modalidad.CONJUNTO, TipoTorneo.RECREATIVO, 4, SABADO, 0, 0, 30000));
            rechazo(() -> club.crearTorneo("V", "Voleibol", "Mayores", Modalidad.CONJUNTO, TipoTorneo.RECREATIVO, 5, SABADO, 0, 0, 30000));
            rechazo(() -> club.crearTorneo("D", "Tenis", "Intermedio", Modalidad.DOBLES, TipoTorneo.RECREATIVO, 1, SABADO, 0, 0, 30000));

            club.crearTorneo("F", "Futbol", "Mayores", Modalidad.CONJUNTO, TipoTorneo.RECREATIVO, 7, SABADO, 0, 0, 30000);
            club.crearTorneo("B", "Baloncesto", "Mayores", Modalidad.CONJUNTO, TipoTorneo.RECREATIVO, 5, SABADO, 0, 0, 30000);
            club.crearTorneo("V", "Voleibol", "Mayores", Modalidad.CONJUNTO, TipoTorneo.RECREATIVO, 6, SABADO, 0, 0, 30000);
            club.crearTorneo("D", "Tenis", "Intermedio", Modalidad.DOBLES, TipoTorneo.RECREATIVO, 2, SABADO, 0, 0, 30000);
            assertEquals(4, club.getTorneos().size());
        }
    }

    @Nested
    @DisplayName("hu-tn-a02 consultar torneos y su estado")
    class ConsultarTorneos {
        private Torneo enCurso;
        private Torneo abierto;
        private Torneo futbol;

        @BeforeEach
        void crearTorneos() throws Exception {
            enCurso = ClubDePrueba.tenisCompetitivo(club, 8);
            club.inscribirEnTorneo(enCurso.getId(), "S1", true);
            club.inscribirEnTorneo(enCurso.getId(), "S4", true);
            club.iniciarTorneo(enCurso.getId());
            abierto = ClubDePrueba.tenisRecreativo(club, 8);
            futbol = ClubDePrueba.futbolRecreativo(club, 8);
        }

        @Test
        @DisplayName("exito: sin filtro salen todos; con filtro, solo los que coinciden")
        void filtros() throws Exception {
            assertEquals(List.of(enCurso, abierto, futbol), club.consultarTorneos(null, null, null));
            assertEquals(List.of(enCurso), club.consultarTorneos("En curso", null, null));
            assertEquals(List.of(abierto, futbol), club.consultarTorneos(null, TipoTorneo.RECREATIVO, null));
            assertEquals(List.of(enCurso, abierto), club.consultarTorneos(null, null, "tenis"));
            assertEquals(List.of(enCurso), club.consultarTorneos("en curso", TipoTorneo.COMPETITIVO, "Tenis"));
        }

        @Test
        @DisplayName("vacio: un filtro sin coincidencias devuelve una lista vacia")
        void sinCoincidencias() throws Exception {
            assertTrue(club.consultarTorneos("Finalizado", null, null).isEmpty());
            assertTrue(club.consultarTorneos(null, null, "Padel").isEmpty());
        }

        @Test
        @DisplayName("rechazo: estado distinto de en curso o finalizado")
        void estadoInvalido() {
            rechazo(() -> club.consultarTorneos("Cancelado", null, null));
            rechazo(() -> club.consultarTorneos("Inscripciones abiertas", null, null));
        }
    }

    @Nested
    @DisplayName("hu-tn-a03 historial detallado de un torneo")
    class Historial {
        @Test
        @DisplayName("exito: un torneo con inscripciones abiertas muestra participantes y cero encuentros")
        void torneoAbierto() throws Exception {
            Torneo torneo = ClubDePrueba.tenisRecreativo(club, 8);
            club.inscribirEnTorneo(torneo.getId(), "S1", true);

            Torneo consultado = club.buscarTorneo(torneo.getId());
            assertEquals(1, consultado.getInscripciones().size());
            assertEquals("Laura", consultado.getInscripciones().get(0).getNombre());
            assertTrue(consultado.getEncuentros().isEmpty());
            assertNull(consultado.describirPremio());
        }

        @Test
        @DisplayName("exito: un torneo finalizado conserva participantes, encuentros, posiciones y premio")
        void torneoFinalizado() throws Exception {
            Torneo torneo = ClubDePrueba.tenisRecreativo(club, 8);
            club.inscribirEnTorneo(torneo.getId(), "S1", true);
            club.inscribirEnTorneo(torneo.getId(), "S4", true);
            club.iniciarTorneo(torneo.getId());
            club.programarEncuentro(torneo.getId(), "S1", "S4");
            club.registrarResultadoEncuentro(torneo.getId(), 1, "6-3 6-4", "S4");
            club.finalizarTorneo(torneo.getId(), "S4");

            Torneo consultado = club.buscarTorneo(torneo.getId());
            assertEquals(EstadoTorneo.FINALIZADO, consultado.getEstado());
            assertEquals(2, consultado.getInscripciones().size());
            assertEquals("6-3 6-4", consultado.getEncuentros().get(0).getMarcador());
            assertEquals("Daniel", consultado.getPosiciones().get(0).getNombre());
            assertEquals("Laura", consultado.getPosiciones().get(1).getNombre());
            assertTrue(consultado.describirPremio().contains("Daniel"));
        }

        @Test
        @DisplayName("rechazo: torneo inexistente")
        void torneoInexistente() {
            Exception error = rechazo(() -> club.buscarTorneo("T99"));
            assertTrue(error.getMessage().contains("No se encontro"));
        }
    }

    @Nested
    @DisplayName("hu-tn-a04 iniciar un torneo")
    class IniciarTorneo {
        @Test
        @DisplayName("exito: pasa a en curso y los preferenciales vacios quedan como regulares")
        void iniciar() throws Exception {
            Torneo torneo = ClubDePrueba.tenisRecreativo(club, 10);
            club.inscribirEnTorneo(torneo.getId(), "S1", true);
            club.inscribirEnTorneo(torneo.getId(), "S7", true);
            assertEquals(2, torneo.getCuposPreferenciales());

            club.iniciarTorneo(torneo.getId());

            assertEquals(EstadoTorneo.EN_CURSO, torneo.getEstado());
            assertEquals(1, torneo.getCuposPreferenciales(), "solo queda el preferencial que ocupo Laura");
            assertEquals(9, torneo.getCuposRegulares());
        }

        @Test
        @DisplayName("rechazo: menos de 2 participantes")
        void pocosParticipantes() throws Exception {
            Torneo torneo = ClubDePrueba.tenisRecreativo(club, 10);
            club.inscribirEnTorneo(torneo.getId(), "S1", true);

            Exception error = rechazo(() -> club.iniciarTorneo(torneo.getId()));
            assertTrue(error.getMessage().contains("faltan 1"));
            assertEquals(EstadoTorneo.INSCRIPCIONES_ABIERTAS, torneo.getEstado());
        }

        @Test
        @DisplayName("rechazo: torneo ya iniciado")
        void yaIniciado() throws Exception {
            Torneo torneo = ClubDePrueba.tenisRecreativo(club, 10);
            club.inscribirEnTorneo(torneo.getId(), "S1", true);
            club.inscribirEnTorneo(torneo.getId(), "S4", true);
            club.iniciarTorneo(torneo.getId());

            rechazo(() -> club.iniciarTorneo(torneo.getId()));
        }
    }

    @Nested
    @DisplayName("hu-tn-a05 finalizar un torneo y entregar premios")
    class FinalizarTorneo {
        private Torneo jugar(Torneo torneo, String... inscritos) throws Exception {
            for (String id : inscritos) {
                club.inscribirEnTorneo(torneo.getId(), id, true);
            }

            club.iniciarTorneo(torneo.getId());
            return torneo;
        }

        @Test
        @DisplayName("exito (recreativo): queda finalizado y el socio ganador recibe el bono")
        void recreativo() throws Exception {
            Torneo torneo = jugar(ClubDePrueba.tenisRecreativo(club, 8), "S1", "S4");
            club.programarEncuentro(torneo.getId(), "S1", "S4");
            club.registrarResultadoEncuentro(torneo.getId(), 1, "6-2", "S1");

            club.finalizarTorneo(torneo.getId(), "S1");

            assertEquals(EstadoTorneo.FINALIZADO, torneo.getEstado());
            Socio laura = club.buscarSocio("S1");
            assertEquals(1, laura.getBonos().size());
            assertEquals(30000, laura.getBonos().get(0).getValor());
            assertEquals(torneo.getNombre(), laura.getBonos().get(0).getTorneoOrigen());
            assertTrue(club.buscarSocio("S4").getBonos().isEmpty());
        }

        @Test
        @DisplayName("exito (recreativo de conjunto): cada integrante del equipo ganador recibe el bono")
        void recreativoDeEquipos() throws Exception {
            Torneo torneo = ClubDePrueba.futbolRecreativo(club, 8);
            club.inscribirEquipoEnTorneo(torneo.getId(), "FUT-A");
            club.inscribirEquipoEnTorneo(torneo.getId(), "FUT-B");
            club.iniciarTorneo(torneo.getId());
            club.programarEncuentro(torneo.getId(), "FUT-A", "FUT-B");
            club.registrarResultadoEncuentro(torneo.getId(), 1, "3-1", "FUT-A");

            club.finalizarTorneo(torneo.getId(), "FUT-A");

            for (int i = 1; i <= 7; i++) {
                assertEquals(1, club.buscarSocio("J" + i).getBonos().size(), "jugador J" + i + " de FUT-A");
            }

            assertTrue(club.buscarSocio("J8").getBonos().isEmpty(), "J8 juega en FUT-B");
        }

        @Test
        @DisplayName("exito (competitivo): el premio es el recaudado por el porcentaje, sin contar empleados")
        void competitivo() throws Exception {
            Torneo torneo = jugar(ClubDePrueba.tenisCompetitivo(club, 8), "S1", "S4", "E2");
            club.programarEncuentro(torneo.getId(), "S1", "S4");
            club.registrarResultadoEncuentro(torneo.getId(), 1, "6-2", "S1");

            club.finalizarTorneo(torneo.getId(), "S1");

            TorneoCompetitivo competitivo = (TorneoCompetitivo) torneo;
            assertEquals(40000, torneo.getTotalRecaudado(), "pagan Laura y Daniel; Paula es empleada");
            assertEquals(20000, competitivo.getPremioEfectivo());
            assertEquals("Laura", competitivo.getBeneficiarioPremio().getNombre());
            assertTrue(club.buscarSocio("S1").getBonos().isEmpty(), "un competitivo no entrega bonos");
        }

        @Test
        @DisplayName("exito (empleado ganador): el premio pasa al mejor clasificado que no sea empleado")
        void empleadoGanador() throws Exception {
            Torneo torneo = jugar(ClubDePrueba.tenisCompetitivo(club, 8), "S1", "S4", "E2");
            club.programarEncuentro(torneo.getId(), "S1", "S4");
            club.programarEncuentro(torneo.getId(), "E2", "S4");
            club.registrarResultadoEncuentro(torneo.getId(), 1, "6-2", "S4");
            club.registrarResultadoEncuentro(torneo.getId(), 2, "6-1", "E2");

            club.finalizarTorneo(torneo.getId(), "E2");

            TorneoCompetitivo competitivo = (TorneoCompetitivo) torneo;
            assertEquals("Paula", torneo.getGanador().getNombre());
            assertEquals("Daniel", competitivo.getBeneficiarioPremio().getNombre());
            assertEquals(20000, competitivo.getPremioEfectivo());
        }

        @Test
        @DisplayName("rechazo: encuentros sin resultado; el sistema queda sin cambios")
        void encuentrosSinResultado() throws Exception {
            Torneo torneo = jugar(ClubDePrueba.tenisRecreativo(club, 8), "S1", "S4");
            rechazo(() -> club.finalizarTorneo(torneo.getId(), "S1"));

            club.programarEncuentro(torneo.getId(), "S1", "S4");
            rechazo(() -> club.finalizarTorneo(torneo.getId(), "S1"));

            assertEquals(EstadoTorneo.EN_CURSO, torneo.getEstado());
            assertTrue(club.buscarSocio("S1").getBonos().isEmpty());
        }

        @Test
        @DisplayName("rechazo: torneo ya finalizado, sin entregar el premio dos veces")
        void yaFinalizado() throws Exception {
            Torneo torneo = jugar(ClubDePrueba.tenisRecreativo(club, 8), "S1", "S4");
            club.programarEncuentro(torneo.getId(), "S1", "S4");
            club.registrarResultadoEncuentro(torneo.getId(), 1, "6-2", "S1");
            club.finalizarTorneo(torneo.getId(), "S1");

            rechazo(() -> club.finalizarTorneo(torneo.getId(), "S1"));
            assertEquals(1, club.buscarSocio("S1").getBonos().size());
        }

        @Test
        @DisplayName("rechazo: torneo sin iniciar, o ganador que no esta inscrito")
        void sinIniciarOGanadorAjeno() throws Exception {
            Torneo abierto = ClubDePrueba.tenisRecreativo(club, 8);
            rechazo(() -> club.finalizarTorneo(abierto.getId(), "S1"));

            Torneo torneo = jugar(ClubDePrueba.tenisRecreativo(club, 8), "S1", "S4");
            club.programarEncuentro(torneo.getId(), "S1", "S4");
            club.registrarResultadoEncuentro(torneo.getId(), 1, "6-2", "S1");
            rechazo(() -> club.finalizarTorneo(torneo.getId(), "S6"));
            assertEquals(EstadoTorneo.EN_CURSO, torneo.getEstado());
        }
    }
}
