package edu.seneca.consola;

import edu.seneca.modelo.deportes.Equipo;
import edu.seneca.modelo.instalaciones.Instalacion;
import edu.seneca.modelo.torneos.BonoDescuento;
import edu.seneca.modelo.usuarios.ParticipanteExterno;
import edu.seneca.modelo.usuarios.Socio;
import edu.seneca.modelo.usuarios.Usuario;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public class ConsolaSocio extends ConsolaBase {

    public static void main(String[] args) {
        new ConsolaSocio().ejecutar();
    }

    protected String titulo() {
        return "Socios y participantes";
    }

    protected boolean puedeEntrar(Usuario usuario) {
        return usuario instanceof Socio || usuario instanceof ParticipanteExterno;
    }

    protected void mostrarMenu() {
        System.out.println("\t1. Consultar torneos y su estado");
        System.out.println("\t2. Ver historial de un torneo");
        System.out.println("\t3. Inscribirme en un torneo individual");
        System.out.println("\t4. Inscribir una pareja en un torneo de dobles");
        System.out.println("\t5. Salirme de un torneo");

        if (usuario instanceof Socio) {
            System.out.println("\t6. Ver mis bonos de descuento");
            System.out.println("\t7. Comprar en la tienda o la cafeteria");
            System.out.println("\t8. Reservar una instalacion");
            System.out.println("\t9. Inscribirme en un equipo");
            System.out.println("\t10. Ver mi perfil");
        }
    }

    protected void ejecutarOpcion(String opcion) throws Exception {
        boolean soloSocios = opcion.equals("6") || opcion.equals("7") || opcion.equals("8") || opcion.equals("9") || opcion.equals("10");

        if (soloSocios && !(usuario instanceof Socio)) {
            System.out.println("opcion no valida");
            return;
        }

        switch (opcion) {
            case "1":
                consultarTorneos();
                break;
            case "2":
                verHistorial();
                break;
            case "3":
                inscribirmeEnTorneo();
                break;
            case "4":
                inscribirPareja();
                break;
            case "5":
                salirmeDeTorneo();
                break;
            case "6":
                verBonos();
                break;
            case "7":
                comprar();
                break;
            case "8":
                reservar();
                break;
            case "9":
                inscribirmeEnEquipo();
                break;
            case "10":
                verPerfil();
                break;
            default:
                System.out.println("opcion no valida");
        }
    }

    private void verBonos() throws Exception {
        List<BonoDescuento> bonos = club.consultarBonos(usuario.getId());

        if (bonos.isEmpty()) {
            System.out.println("\tNo tiene bonos");
            return;
        }

        for (BonoDescuento bono : bonos) {
            System.out.println("\t" + bono.getId() + " | " + bono.getValor() + " | ganado en " + bono.getTorneoOrigen() + " | " + bono.getEstado());
        }
    }

    private void reservar() throws Exception {
        for (Instalacion instalacion : club.getInstalaciones()) {
            System.out.println("\t" + instalacion.getId());
        }

        String idInstalacion = entrada.leerTexto("ID de la instalacion").toUpperCase();
        LocalDate fecha = entrada.leerFecha("Fecha");
        LocalTime hora = entrada.leerHora("Hora");
        String idOponente = entrada.leerOpcional("ID del socio oponente");

        if (idOponente != null) {
            idOponente = idOponente.toUpperCase();
        }
        if (club.procesarReserva(usuario.getId(), idInstalacion, fecha, hora, idOponente)) {
            System.out.println("\tReserva confirmada");
        } else {
            System.out.println("\tla instalacion esta ocupada o el nivel del oponente no es compatible");
        }
    }

    private void inscribirmeEnEquipo() throws Exception {
        for (Equipo equipo : club.getEquipos()) {
            System.out.println("\t" + equipo.getId() + " | " + equipo.getDeporte() + " " + equipo.getCategoria() + " | " + equipo.getInscripciones().size() + "/" + equipo.getCupoMaximo());
        }

        String idEquipo = entrada.leerTexto("ID del equipo").toUpperCase();
        String posicion = entrada.leerTexto("Posicion");
        int numero = entrada.leerEntero("Numero de camiseta", 1, 99);

        if (club.procesarInscripcion(usuario.getId(), idEquipo, posicion, numero)) {
            System.out.println("\tse incribio en " + idEquipo);
        } else {
            System.out.println("\trevisa la categoria por edad, el cupo, el numero de camiseta o si ya juega ese deporte en otro equipo");
        }
    }

    private void verPerfil() {
        Socio socio = (Socio) usuario;
        System.out.println("\t" + socio.getNombre() + " | nivel " + socio.getNivel() + " | " + socio.getEdad() + " anos");
        System.out.println("\tPuntos de fidelidad: " + socio.getPuntosFidelidad());
        System.out.println("\tRanking de tenis: " + socio.getRanking("Tenis"));
        System.out.println("\tRanking de padel: " + socio.getRanking("Padel"));
        System.out.println("\tRanking de tenis de mesa: " + socio.getRanking("Tenis de mesa"));
        System.out.println("\tSanciones: " + socio.getSanciones().size());

        if (socio.isBloqueadoProximoPartido()) {
            System.out.println("\tBloqueado para el proximo partido");
        }
    }
}
