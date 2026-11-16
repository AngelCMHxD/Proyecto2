package edu.seneca;

import edu.seneca.modelo.torneos.InscripcionTorneo;
import edu.seneca.modelo.torneos.Modalidad;
import edu.seneca.modelo.torneos.TipoCupo;
import edu.seneca.modelo.torneos.TipoTorneo;
import edu.seneca.modelo.torneos.Torneo;
import edu.seneca.modelo.usuarios.Empleado;
import edu.seneca.modelo.usuarios.NivelDeportivo;
import edu.seneca.modelo.usuarios.ParticipanteExterno;
import edu.seneca.modelo.usuarios.Turno;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalTime;

import static edu.seneca.ClubDePrueba.LUNES;
import static edu.seneca.ClubDePrueba.SABADO;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class InscripcionTorneosTest {

    private ClubDeportivoSeneca club;

    @BeforeEach
    void crearClub() throws Exception {
        club = ClubDePrueba.crear();
    }

    private Exception rechazo(org.junit.jupiter.api.function.Executable accion) {
        return assertThrows(Exception.class, accion);
    }

    private Torneo dobles(int cupos) throws Exception {
        return club.crearTorneo("Dobles", "Tenis", "Intermedio", Modalidad.DOBLES, TipoTorneo.COMPETITIVO, cupos, SABADO, 20000, 50, 0);
    }

    private void iniciarConDosInscritos(Torneo torneo) throws Exception {
        club.inscribirEnTorneo(torneo.getId(), "S6", true);
        club.inscribirEnTorneo(torneo.getId(), "S7", true);
        club.iniciarTorneo(torneo.getId());
    }

    @Nested
    @DisplayName("hu-tn-a06 inscribir y retirar un equipo")
    class Equipos {
        @Test
        @DisplayName("exito: un equipo activo, del deporte y categoria, con el minimo de habilitados ocupa un cupo")
        void inscribir() throws Exception {
            Torneo torneo = ClubDePrueba.futbolRecreativo(club, 8);

            InscripcionTorneo inscripcion = club.inscribirEquipoEnTorneo(torneo.getId(), "FUT-A");

            assertEquals(TipoCupo.REGULAR, inscripcion.getTipoCupo());
            assertEquals(1, torneo.contarOcupados(TipoCupo.REGULAR));
            assertEquals("Equipo FUT-A", torneo.getInscripciones().get(0).getNombre());
        }

        @Test
        @DisplayName("exito: el equipo con mejor ranking toma un cupo preferencial")
        void equipoPreferencial() throws Exception {
            Torneo torneo = ClubDePrueba.futbolRecreativo(club, 7);
            club.buscarEquipo("FUT-A").setRanking(900);
            club.buscarEquipo("FUT-B").setRanking(950);
            club.buscarEquipo("FUT-C").setRanking(990);

            assertEquals(TipoCupo.PREFERENCIAL, club.inscribirEquipoEnTorneo(torneo.getId(), "FUT-B").getTipoCupo());
            assertEquals(TipoCupo.REGULAR, club.inscribirEquipoEnTorneo(torneo.getId(), "FUT-A").getTipoCupo());
        }

        @Test
        @DisplayName("exito: retirar un equipo antes del inicio libera su cupo")
        void retirar() throws Exception {
            Torneo torneo = ClubDePrueba.futbolRecreativo(club, 8);
            club.inscribirEquipoEnTorneo(torneo.getId(), "FUT-A");

            club.retirarEquipoDeTorneo(torneo.getId(), "FUT-A");

            assertTrue(torneo.getInscripciones().isEmpty());
            assertEquals(0, torneo.contarOcupados(TipoCupo.REGULAR));
        }

        @Test
        @DisplayName("rechazo: otro deporte, otra categoria, inactivo, pocos habilitados o ya inscrito")
        void equipoNoApto() throws Exception {
            Torneo torneo = ClubDePrueba.futbolRecreativo(club, 8);

            assertTrue(rechazo(() -> club.inscribirEquipoEnTorneo(torneo.getId(), "BAL-A")).getMessage().contains("Baloncesto"));
            assertTrue(rechazo(() -> club.inscribirEquipoEnTorneo(torneo.getId(), "FUT-SUB15")).getMessage().contains("categoria"));
            assertTrue(rechazo(() -> club.inscribirEquipoEnTorneo(torneo.getId(), "FUT-C")).getMessage().contains("habilitados"));

            club.buscarEquipo("FUT-B").setActivo(false);
            assertTrue(rechazo(() -> club.inscribirEquipoEnTorneo(torneo.getId(), "FUT-B")).getMessage().contains("inactivo"));

            club.inscribirEquipoEnTorneo(torneo.getId(), "FUT-A");
            assertTrue(rechazo(() -> club.inscribirEquipoEnTorneo(torneo.getId(), "FUT-A")).getMessage().contains("ya esta inscrito"));
            assertEquals(1, torneo.getInscripciones().size());
        }

        @Test
        @DisplayName("rechazo: un jugador bloqueado por sancion no cuenta como habilitado")
        void jugadorSancionado() throws Exception {
            Torneo torneo = ClubDePrueba.futbolRecreativo(club, 8);
            club.buscarSocio("J1").bloquearInscripcionProximoPartido();

            rechazo(() -> club.inscribirEquipoEnTorneo(torneo.getId(), "FUT-A"));
        }

        @Test
        @DisplayName("rechazo: torneo ya iniciado, tanto para inscribir como para retirar")
        void torneoIniciado() throws Exception {
            Torneo torneo = ClubDePrueba.futbolRecreativo(club, 8);
            club.inscribirEquipoEnTorneo(torneo.getId(), "FUT-A");
            club.inscribirEquipoEnTorneo(torneo.getId(), "FUT-B");
            club.iniciarTorneo(torneo.getId());

            rechazo(() -> club.retirarEquipoDeTorneo(torneo.getId(), "FUT-A"));
            rechazo(() -> club.inscribirEquipoEnTorneo(torneo.getId(), "FUT-C"));
            assertEquals(2, torneo.getInscripciones().size());
        }

        @Test
        @DisplayName("rechazo: equipo en torneo individual, o retirar un equipo que no esta inscrito")
        void casosCruzados() throws Exception {
            Torneo tenis = ClubDePrueba.tenisRecreativo(club, 8);
            Torneo futbol = ClubDePrueba.futbolRecreativo(club, 8);

            rechazo(() -> club.inscribirEquipoEnTorneo(tenis.getId(), "FUT-A"));
            rechazo(() -> club.retirarEquipoDeTorneo(futbol.getId(), "FUT-A"));
            rechazo(() -> club.inscribirEquipoEnTorneo(futbol.getId(), "NO-EXISTE"));
        }
    }

    @Nested
    @DisplayName("hu-tn-a07 registrar a un participante externo")
    class Externos {
        @Test
        @DisplayName("exito: queda creado, puede autenticarse e inscribirse, y no es socio")
        void registrar() throws Exception {
            ParticipanteExterno ana = club.registrarParticipanteExterno("Ana Diaz", "1010", LocalDate.of(1999, 4, 2), "ana", "ana123", NivelDeportivo.INTERMEDIO, false);

            assertEquals(ana, club.autenticar("ana", "ana123"));
            assertFalse(ana.isEstudiante());
            assertThrows(Exception.class, () -> club.buscarSocio(ana.getId()), "no tiene acceso a servicios de socio");

            Torneo torneo = ClubDePrueba.tenisRecreativo(club, 8);
            assertNotNull(club.inscribirEnTorneo(torneo.getId(), ana.getId(), true));
        }

        @Test
        @DisplayName("exito: un estudiante de la universidad se registra igual y queda marcado como tal")
        void estudiante() throws Exception {
            ParticipanteExterno luis = club.registrarParticipanteExterno("Luis", "2020", LocalDate.of(2005, 1, 1), "luis", "luis123", NivelDeportivo.AVANZADO, true);
            assertTrue(luis.isEstudiante());
        }

        @Test
        @DisplayName("rechazo: login o documento repetidos, datos faltantes o fecha de nacimiento futura")
        void datosInvalidos() throws Exception {
            club.registrarParticipanteExterno("Ana", "1010", LocalDate.of(1999, 4, 2), "ana", "ana123", NivelDeportivo.INTERMEDIO, false);
            int usuarios = club.getUsuarios().size();

            assertTrue(rechazo(() -> club.registrarParticipanteExterno("Otra", "3030", LocalDate.of(1999, 4, 2), "ana", "x", NivelDeportivo.INTERMEDIO, false)).getMessage().contains("login"));
            assertTrue(rechazo(() -> club.registrarParticipanteExterno("Otra", "3030", LocalDate.of(1999, 4, 2), "laura", "x", NivelDeportivo.INTERMEDIO, false)).getMessage().contains("login"), "login de un socio");
            assertTrue(rechazo(() -> club.registrarParticipanteExterno("Otra", "1010", LocalDate.of(1999, 4, 2), "otra", "x", NivelDeportivo.INTERMEDIO, false)).getMessage().contains("documento"));
            assertTrue(rechazo(() -> club.registrarParticipanteExterno("", "3030", LocalDate.of(1999, 4, 2), "otra", "x", NivelDeportivo.INTERMEDIO, false)).getMessage().contains("nombre"));
            assertTrue(rechazo(() -> club.registrarParticipanteExterno("Otra", "3030", null, "otra", "x", NivelDeportivo.INTERMEDIO, false)).getMessage().contains("fecha"));
            assertTrue(rechazo(() -> club.registrarParticipanteExterno("Otra", "3030", LocalDate.now().plusDays(1), "otra", "x", NivelDeportivo.INTERMEDIO, false)).getMessage().contains("futura"));

            assertEquals(usuarios, club.getUsuarios().size());
        }
    }

    @Nested
    @DisplayName("hu-tn-s01 inscribirme en un torneo individual")
    class Individual {
        @Test
        @DisplayName("exito: con cupo libre y nivel igual al del torneo, quedo inscrito")
        void inscribirme() throws Exception {
            Torneo torneo = ClubDePrueba.tenisRecreativo(club, 8);

            InscripcionTorneo inscripcion = club.inscribirEnTorneo(torneo.getId(), "S7", true);

            assertEquals(TipoCupo.REGULAR, inscripcion.getTipoCupo());
            assertEquals(1, torneo.contarOcupados(TipoCupo.REGULAR));
            assertNotNull(torneo.buscarInscripcion(club.buscarUsuario("S7")));
        }

        @Test
        @DisplayName("exito (recreativo): no se cobra tarifa, ni siquiera sin pago confirmado")
        void recreativoSinTarifa() throws Exception {
            Torneo torneo = ClubDePrueba.tenisRecreativo(club, 8);

            InscripcionTorneo inscripcion = club.inscribirEnTorneo(torneo.getId(), "S7", false);

            assertEquals(0, inscripcion.getTarifaPagada());
            assertEquals(0, torneo.getTotalRecaudado());
        }

        @Test
        @DisplayName("exito (competitivo): con pago confirmado se registra la tarifa")
        void competitivoConPago() throws Exception {
            Torneo torneo = ClubDePrueba.tenisCompetitivo(club, 8);

            InscripcionTorneo inscripcion = club.inscribirEnTorneo(torneo.getId(), "S7", true);

            assertEquals(20000, inscripcion.getTarifaPagada());
            assertEquals(20000, torneo.getTotalRecaudado());
        }

        @Test
        @DisplayName("rechazo: competitivo con pago no confirmado; no ocupa cupo")
        void pagoNoConfirmado() throws Exception {
            Torneo torneo = ClubDePrueba.tenisCompetitivo(club, 8);

            Exception error = rechazo(() -> club.inscribirEnTorneo(torneo.getId(), "S7", false));

            assertTrue(error.getMessage().contains("no confirmado"));
            assertTrue(torneo.getInscripciones().isEmpty());
        }

        @Test
        @DisplayName("rechazo: sin cupos regulares libres se informa que esta lleno")
        void torneoLleno() throws Exception {
            Torneo torneo = ClubDePrueba.tenisRecreativo(club, 2);
            club.inscribirEnTorneo(torneo.getId(), "S7", true);

            Exception error = rechazo(() -> club.inscribirEnTorneo(torneo.getId(), "S6", true));

            assertTrue(error.getMessage().contains("lleno"));
            assertEquals(1, torneo.getInscripciones().size());
        }

        @Test
        @DisplayName("rechazo: torneo ya iniciado o finalizado")
        void torneoIniciado() throws Exception {
            Torneo torneo = ClubDePrueba.tenisRecreativo(club, 8);
            iniciarConDosInscritos(torneo);

            rechazo(() -> club.inscribirEnTorneo(torneo.getId(), "S1", true));

            club.programarEncuentro(torneo.getId(), "S6", "S7");
            club.registrarResultadoEncuentro(torneo.getId(), 1, "6-0", "S6");
            club.finalizarTorneo(torneo.getId(), "S6");
            rechazo(() -> club.inscribirEnTorneo(torneo.getId(), "S1", true));
        }

        @Test
        @DisplayName("rechazo: ya inscrito, o nivel distinto al del torneo, indicando el motivo")
        void yaInscritoONivelDistinto() throws Exception {
            Torneo torneo = ClubDePrueba.tenisRecreativo(club, 8);
            club.inscribirEnTorneo(torneo.getId(), "S7", true);

            assertTrue(rechazo(() -> club.inscribirEnTorneo(torneo.getId(), "S7", true)).getMessage().contains("ya esta inscrito"));
            assertTrue(rechazo(() -> club.inscribirEnTorneo(torneo.getId(), "S2", true)).getMessage().contains("nivel"));
            assertTrue(rechazo(() -> club.inscribirEnTorneo(torneo.getId(), "S3", true)).getMessage().contains("nivel"));
            assertEquals(1, torneo.getInscripciones().size());
        }

        @Test
        @DisplayName("rechazo: torneo de conjunto, torneo de dobles, torneo o usuario inexistente, o administrador")
        void casosCruzados() throws Exception {
            Torneo futbol = ClubDePrueba.futbolRecreativo(club, 8);
            Torneo tenis = ClubDePrueba.tenisRecreativo(club, 8);
            Torneo parejas = dobles(4);

            rechazo(() -> club.inscribirEnTorneo(futbol.getId(), "S7", true));
            rechazo(() -> club.inscribirEnTorneo(parejas.getId(), "S7", true));
            rechazo(() -> club.inscribirEnTorneo("T99", "S7", true));
            rechazo(() -> club.inscribirEnTorneo(tenis.getId(), "S99", true));
            rechazo(() -> club.inscribirEnTorneo(tenis.getId(), "A1", true));
        }
    }

    @Nested
    @DisplayName("hu-tn-s02 inscribir una pareja en dobles")
    class Parejas {
        @Test
        @DisplayName("exito: dos participantes distintos del nivel del torneo ocupan un solo cupo")
        void inscribirPareja() throws Exception {
            Torneo torneo = dobles(4);

            InscripcionTorneo pareja = club.inscribirPareja(torneo.getId(), "S6", "tomas", true);

            assertEquals(1, torneo.getInscripciones().size());
            assertEquals("Camila / Tomas", pareja.getNombre());
            assertEquals(40000, pareja.getTarifaPagada(), "cada integrante paga su tarifa");
            assertNotNull(torneo.buscarInscripcion(club.buscarUsuario("S7")));
        }

        @Test
        @DisplayName("exito: el companero se puede buscar por documento")
        void companeroPorDocumento() throws Exception {
            Torneo torneo = dobles(4);
            club.registrarParticipanteExterno("Ana", "1010", LocalDate.of(1999, 4, 2), "ana", "ana123", NivelDeportivo.INTERMEDIO, false);

            assertEquals("Camila / Ana", club.inscribirPareja(torneo.getId(), "S6", "1010", true).getNombre());
        }

        @Test
        @DisplayName("rechazo: companero repetido, inexistente, ya inscrito o de otro nivel")
        void companeroInvalido() throws Exception {
            Torneo torneo = dobles(4);
            club.inscribirPareja(torneo.getId(), "S1", "daniel", true);

            rechazo(() -> club.inscribirPareja(torneo.getId(), "S6", "camila", true));
            rechazo(() -> club.inscribirPareja(torneo.getId(), "S6", "nadie", true));
            rechazo(() -> club.inscribirPareja(torneo.getId(), "S6", "daniel", true));
            rechazo(() -> club.inscribirPareja(torneo.getId(), "S6", "mateo", true));
            assertEquals(1, torneo.getInscripciones().size());
        }

        @Test
        @DisplayName("rechazo: un torneo de sencillos no admite parejas")
        void torneoDeSencillos() throws Exception {
            Torneo torneo = ClubDePrueba.tenisRecreativo(club, 8);

            Exception error = rechazo(() -> club.inscribirPareja(torneo.getId(), "S6", "tomas", true));
            assertTrue(error.getMessage().contains("sencillos"));
        }
    }

    @Nested
    @DisplayName("hu-tn-s03 salirme de un torneo")
    class Salirse {
        @Test
        @DisplayName("exito: antes del inicio se libera mi cupo, regular o preferencial")
        void salirme() throws Exception {
            Torneo torneo = ClubDePrueba.tenisRecreativo(club, 5);
            club.inscribirEnTorneo(torneo.getId(), "S1", true);
            club.inscribirEnTorneo(torneo.getId(), "S7", true);

            club.salirDeTorneo(torneo.getId(), "S1");
            club.salirDeTorneo(torneo.getId(), "S7");

            assertEquals(0, torneo.contarOcupados(TipoCupo.PREFERENCIAL));
            assertEquals(0, torneo.contarOcupados(TipoCupo.REGULAR));
            assertTrue(torneo.hayCupo(TipoCupo.PREFERENCIAL));
        }

        @Test
        @DisplayName("exito (pareja): si un integrante se sale, la pareja queda fuera y se libera el cupo")
        void salirDePareja() throws Exception {
            Torneo torneo = dobles(4);
            club.inscribirPareja(torneo.getId(), "S6", "tomas", true);

            club.salirDeTorneo(torneo.getId(), "S7");

            assertTrue(torneo.getInscripciones().isEmpty());
            assertNull(torneo.buscarInscripcion(club.buscarUsuario("S6")));
            assertEquals(0, torneo.getTotalRecaudado());
        }

        @Test
        @DisplayName("rechazo: torneo en curso o finalizado")
        void torneoIniciado() throws Exception {
            Torneo torneo = ClubDePrueba.tenisRecreativo(club, 8);
            iniciarConDosInscritos(torneo);

            rechazo(() -> club.salirDeTorneo(torneo.getId(), "S6"));
            assertEquals(2, torneo.getInscripciones().size());
        }

        @Test
        @DisplayName("rechazo: torneo en el que no estoy inscrito")
        void noInscrito() throws Exception {
            Torneo torneo = ClubDePrueba.tenisRecreativo(club, 8);

            Exception error = rechazo(() -> club.salirDeTorneo(torneo.getId(), "S6"));
            assertTrue(error.getMessage().contains("no esta inscrito"));
        }
    }

    @Nested
    @DisplayName("hu-tn-s04 inscribirme en un cupo preferencial")
    class Preferencial {
        @Test
        @DisplayName("exito: con ranking entre los mejores de mi nivel ocupo un cupo preferencial")
        void rankingEntreLosMejores() throws Exception {
            Torneo torneo = ClubDePrueba.tenisRecreativo(club, 5);

            InscripcionTorneo inscripcion = club.inscribirEnCupoPreferencial(torneo.getId(), "S1", true);

            assertEquals(TipoCupo.PREFERENCIAL, inscripcion.getTipoCupo());
            assertFalse(torneo.hayCupo(TipoCupo.PREFERENCIAL));
            assertEquals(0, torneo.contarOcupados(TipoCupo.REGULAR));
        }

        @Test
        @DisplayName("exito: el ranking se compara solo contra socios del mismo nivel")
        void rankingPorNivel() throws Exception {
            Torneo torneo = ClubDePrueba.tenisRecreativo(club, 5);
            assertTrue(club.socioCalificaPreferencial(club.buscarSocio("S1"), torneo));
            assertFalse(club.socioCalificaPreferencial(club.buscarSocio("S4"), torneo));
            assertFalse(club.socioCalificaPreferencial(club.buscarSocio("S7"), torneo), "sin ranking no califica");
        }

        @Test
        @DisplayName("rechazo: ranking fuera de los mejores; puedo inscribirme en uno regular si hay")
        void rankingInsuficiente() throws Exception {
            Torneo torneo = ClubDePrueba.tenisRecreativo(club, 5);

            Exception error = rechazo(() -> club.inscribirEnCupoPreferencial(torneo.getId(), "S4", true));
            assertTrue(error.getMessage().contains("ranking"));
            assertTrue(torneo.getInscripciones().isEmpty());

            assertEquals(TipoCupo.REGULAR, club.inscribirEnTorneo(torneo.getId(), "S4", true).getTipoCupo());
        }

        @Test
        @DisplayName("rechazo: quien no califica no toma el preferencial aunque sea el unico cupo libre")
        void preferencialReservado() throws Exception {
            Torneo torneo = ClubDePrueba.tenisRecreativo(club, 2);
            club.inscribirEnTorneo(torneo.getId(), "S4", true);

            rechazo(() -> club.inscribirEnTorneo(torneo.getId(), "S6", true));
            assertEquals(TipoCupo.PREFERENCIAL, club.inscribirEnTorneo(torneo.getId(), "S1", true).getTipoCupo());
        }

        @Test
        @DisplayName("exito (liberacion): al iniciar, los preferenciales vacios pasan a regulares")
        void liberacion() throws Exception {
            Torneo torneo = ClubDePrueba.tenisRecreativo(club, 5);
            iniciarConDosInscritos(torneo);

            assertEquals(0, torneo.getCuposPreferenciales());
            assertEquals(5, torneo.getCuposRegulares());
        }
    }

    @Nested
    @DisplayName("hu-tn-e01 y e02 inscribirme y salirme como empleado")
    class Empleados {
        @Test
        @DisplayName("exito: sin turno el dia del torneo, quedo inscrito")
        void sinTurnoEseDia() throws Exception {
            Torneo torneo = ClubDePrueba.tenisRecreativo(club, 8);

            assertNotNull(club.inscribirEnTorneo(torneo.getId(), "E1", true));
            assertNotNull(club.inscribirEnTorneo(torneo.getId(), "E3", true));
        }

        @Test
        @DisplayName("exito (competitivo): no pago tarifa, no sumo al recaudado y no tengo derecho al premio")
        void competitivoGratis() throws Exception {
            Torneo torneo = ClubDePrueba.tenisCompetitivo(club, 8);

            InscripcionTorneo inscripcion = club.inscribirEnTorneo(torneo.getId(), "E1", false);

            assertEquals(0, inscripcion.getTarifaPagada());
            assertEquals(0, torneo.getTotalRecaudado());
            assertFalse(inscripcion.tieneDerechoAPremioEfectivo());
        }

        @Test
        @DisplayName("rechazo: tengo turno el dia del torneo; se indica el conflicto")
        void turnoEseDia() throws Exception {
            Torneo torneo = club.crearTorneo("Lunes", "Tenis", "Intermedio", Modalidad.SENCILLOS, TipoTorneo.RECREATIVO, 8, LUNES, 0, 0, 30000);

            Exception error = rechazo(() -> club.inscribirEnTorneo(torneo.getId(), "E1", true));

            assertTrue(error.getMessage().contains("turno"));
            assertTrue(error.getMessage().contains("Lunes"));
            assertNotNull(club.inscribirEnTorneo(torneo.getId(), "E2", true), "Paula no tiene turnos");
        }

        @Test
        @DisplayName("rechazo: los empleados solo participan en torneos individuales")
        void torneoDeConjunto() throws Exception {
            Torneo torneo = ClubDePrueba.futbolRecreativo(club, 8);

            Exception error = rechazo(() -> club.inscribirEnTorneo(torneo.getId(), "E2", true));
            assertTrue(error.getMessage().contains("individuales"));
        }

        @Test
        @DisplayName("rechazo: torneo sin cupos, ya iniciado, o en el que ya estoy inscrito")
        void otrosRechazos() throws Exception {
            Torneo lleno = ClubDePrueba.tenisRecreativo(club, 2);
            club.inscribirEnTorneo(lleno.getId(), "S7", true);
            rechazo(() -> club.inscribirEnTorneo(lleno.getId(), "E2", true));

            Torneo abierto = ClubDePrueba.tenisRecreativo(club, 8);
            club.inscribirEnTorneo(abierto.getId(), "E2", true);
            rechazo(() -> club.inscribirEnTorneo(abierto.getId(), "E2", true));

            Torneo iniciado = ClubDePrueba.tenisRecreativo(club, 8);
            iniciarConDosInscritos(iniciado);
            rechazo(() -> club.inscribirEnTorneo(iniciado.getId(), "E2", true));
        }

        @Test
        @DisplayName("hu-tn-e02 exito: me salgo antes del inicio y se libera mi cupo")
        void salirme() throws Exception {
            Torneo torneo = ClubDePrueba.tenisRecreativo(club, 8);
            club.inscribirEnTorneo(torneo.getId(), "E2", true);

            club.salirDeTorneo(torneo.getId(), "E2");

            assertTrue(torneo.getInscripciones().isEmpty());
        }

        @Test
        @DisplayName("hu-tn-e02 rechazo: torneo en curso, o en el que no estoy inscrito")
        void salirmeRechazado() throws Exception {
            Torneo abierto = ClubDePrueba.tenisRecreativo(club, 8);
            rechazo(() -> club.salirDeTorneo(abierto.getId(), "E2"));

            Torneo iniciado = ClubDePrueba.tenisRecreativo(club, 8);
            club.inscribirEnTorneo(iniciado.getId(), "E2", true);
            club.inscribirEnTorneo(iniciado.getId(), "S7", true);
            club.iniciarTorneo(iniciado.getId());
            rechazo(() -> club.salirDeTorneo(iniciado.getId(), "E2"));
        }
    }

    @Nested
    @DisplayName("hu-tn-e03 conflicto entre turno y torneo")
    class TurnoYTorneo {
        private final Turno turnoSabado = new Turno("Sabado", LocalTime.of(8, 0), LocalTime.of(12, 0));

        @Test
        @DisplayName("exito: sin torneos ese dia, el turno se asigna")
        void sinTorneos() throws Exception {
            Empleado paula = (Empleado) club.buscarUsuario("E2");

            club.asignarTurno("E2", turnoSabado);

            assertEquals(1, paula.getTurnos().size());
        }

        @Test
        @DisplayName("rechazo: inscrito en un torneo ese dia, el turno se rechaza y los turnos no cambian")
        void conTorneoEseDia() throws Exception {
            Torneo torneo = ClubDePrueba.tenisRecreativo(club, 8);
            club.inscribirEnTorneo(torneo.getId(), "E2", true);
            Empleado paula = (Empleado) club.buscarUsuario("E2");

            Exception error = rechazo(() -> club.asignarTurno("E2", turnoSabado));

            assertTrue(error.getMessage().contains(torneo.getNombre()));
            assertTrue(paula.getTurnos().isEmpty());

            club.asignarTurno("E2", new Turno("Domingo", LocalTime.of(8, 0), LocalTime.of(12, 0)));
            assertEquals(1, paula.getTurnos().size());
        }

        @Test
        @DisplayName("exito: si el empleado se sale del torneo, el turno ya se puede asignar")
        void despuesDeSalirse() throws Exception {
            Torneo torneo = ClubDePrueba.tenisRecreativo(club, 8);
            club.inscribirEnTorneo(torneo.getId(), "E2", true);
            club.salirDeTorneo(torneo.getId(), "E2");

            club.asignarTurno("E2", turnoSabado);
            assertEquals(1, ((Empleado) club.buscarUsuario("E2")).getTurnos().size());
        }

        @Test
        @DisplayName("rechazo: turno con horas invertidas, dia que no existe, o usuario que no es empleado")
        void turnoInvalido() {
            rechazo(() -> club.asignarTurno("E2", new Turno("Sabado", LocalTime.of(12, 0), LocalTime.of(8, 0))));
            rechazo(() -> club.asignarTurno("E2", new Turno("Feriado", LocalTime.of(8, 0), LocalTime.of(12, 0))));
            rechazo(() -> club.asignarTurno("S1", turnoSabado));
        }
    }
}
