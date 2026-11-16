package edu.seneca;

import edu.seneca.modelo.deportes.Equipo;
import edu.seneca.modelo.instalaciones.Instalacion;
import edu.seneca.modelo.torneos.Modalidad;
import edu.seneca.modelo.torneos.TipoTorneo;
import edu.seneca.modelo.torneos.Torneo;
import edu.seneca.modelo.usuarios.Administrador;
import edu.seneca.modelo.usuarios.Entrenador;
import edu.seneca.modelo.usuarios.Fisioterapeuta;
import edu.seneca.modelo.usuarios.NivelDeportivo;
import edu.seneca.modelo.usuarios.Socio;
import edu.seneca.modelo.usuarios.Turno;
import edu.seneca.modelo.ventas.ArticuloTienda;
import edu.seneca.modelo.ventas.Bebida;

import java.time.LocalDate;
import java.time.LocalTime;

public class ClubDePrueba {

    public static final LocalDate LUNES = LocalDate.of(2027, 3, 1);
    public static final LocalDate SABADO = LocalDate.of(2027, 3, 6);

    private static int consecutivoJugadores;

    public static ClubDeportivoSeneca crear() throws Exception {
        ClubDeportivoSeneca club = new ClubDeportivoSeneca("Club de prueba");
        consecutivoJugadores = 0;

        club.agregarUsuario(socio("S1", "Laura", NivelDeportivo.INTERMEDIO, 1500));
        club.agregarUsuario(socio("S2", "Mateo", NivelDeportivo.AVANZADO, 1800));
        club.agregarUsuario(socio("S3", "Sofia", NivelDeportivo.PRINCIPIANTE, 0));
        club.agregarUsuario(socio("S4", "Daniel", NivelDeportivo.INTERMEDIO, 1400));
        club.agregarUsuario(socio("S5", "Valentina", NivelDeportivo.AVANZADO, 1700));
        club.agregarUsuario(socio("S6", "Camila", NivelDeportivo.INTERMEDIO, 1300));
        club.agregarUsuario(socio("S7", "Tomas", NivelDeportivo.INTERMEDIO, 0));

        Entrenador carlos = new Entrenador("E1", "Carlos", "carlos", "carlos123");
        Fisioterapeuta andres = new Fisioterapeuta("E3", "Andres", "andres", "andres123");

        for (String dia : new String[] {"Lunes", "Martes", "Miercoles", "Jueves", "Viernes"}) {
            carlos.agregarTurno(new Turno(dia, LocalTime.of(6, 0), LocalTime.of(14, 0)));
            andres.agregarTurno(new Turno(dia, LocalTime.of(8, 0), LocalTime.of(16, 0)));
        }

        club.agregarUsuario(carlos);
        club.agregarUsuario(new Entrenador("E2", "Paula", "paula", "paula123"));
        club.agregarUsuario(andres);
        club.agregarUsuario(new Administrador("A1", "Marta", "admin", "admin123"));

        club.agregarInstalacion(new Instalacion("TEN-1", "Tenis", 4));
        club.agregarProducto(new ArticuloTienda("P1", "Raqueta", 250000, 8, "Tenis"));
        club.agregarProducto(new ArticuloTienda("P4", "Pelotas de padel", 35000, 0, "Padel"));
        club.agregarProducto(new Bebida("P5", "Cafe", 4000, 50, true));
        club.agregarProducto(new Bebida("P6", "Agua", 3000, 2, false));

        equipo(club, "FUT-A", "Futbol", "Mayores", 7);
        equipo(club, "FUT-B", "Futbol", "Mayores", 7);
        equipo(club, "FUT-C", "Futbol", "Mayores", 3);
        equipo(club, "BAL-A", "Baloncesto", "Mayores", 5);
        equipo(club, "FUT-SUB15", "Futbol", "Sub-15", 0);
        return club;
    }

    private static Socio socio(String id, String nombre, NivelDeportivo nivel, int rankingTenis) {
        Socio socio = new Socio(id, nombre, nombre.toLowerCase(), nombre.toLowerCase() + "123", nivel, LocalDate.of(2000, 1, 15));

        if (rankingTenis > 0) {
            socio.actualizarRanking("Tenis", rankingTenis);
        }
        return socio;
    }

    private static void equipo(ClubDeportivoSeneca club, String id, String deporte, String categoria, int jugadores) throws Exception {
        club.agregarEquipo(new Equipo(id, deporte, categoria, 22));

        for (int i = 1; i <= jugadores; i++) {
            consecutivoJugadores++;
            String idSocio = "J" + consecutivoJugadores;
            club.agregarUsuario(new Socio(idSocio, "Jugador " + consecutivoJugadores, "jug" + consecutivoJugadores, "clave", NivelDeportivo.INTERMEDIO, LocalDate.of(2000, 1, 15)));
            club.procesarInscripcion(idSocio, id, "Campo", i);
        }
    }

    public static Torneo tenisRecreativo(ClubDeportivoSeneca club, int cupos) throws Exception {
        return club.crearTorneo("Recreativo " + (club.getTorneos().size() + 1), "Tenis", "Intermedio", Modalidad.SENCILLOS, TipoTorneo.RECREATIVO, cupos, SABADO, 0, 0, 30000);
    }

    public static Torneo tenisCompetitivo(ClubDeportivoSeneca club, int cupos) throws Exception {
        return club.crearTorneo("Competitivo " + (club.getTorneos().size() + 1), "Tenis", "Intermedio", Modalidad.SENCILLOS, TipoTorneo.COMPETITIVO, cupos, SABADO, 20000, 50, 0);
    }

    public static Torneo futbolRecreativo(ClubDeportivoSeneca club, int cupos) throws Exception {
        return club.crearTorneo("Futbol " + (club.getTorneos().size() + 1), "Futbol", "Mayores", Modalidad.CONJUNTO, TipoTorneo.RECREATIVO, cupos, SABADO, 0, 0, 30000);
    }
}
