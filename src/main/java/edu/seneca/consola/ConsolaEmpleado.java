package edu.seneca.consola;

import edu.seneca.modelo.deportes.Partido;
import edu.seneca.modelo.entrenamiento.SesionEntrenamiento;
import edu.seneca.modelo.instalaciones.Instalacion;
import edu.seneca.modelo.usuarios.Administrador;
import edu.seneca.modelo.usuarios.Empleado;
import edu.seneca.modelo.usuarios.Entrenador;
import edu.seneca.modelo.usuarios.Fisioterapeuta;
import edu.seneca.modelo.usuarios.Turno;
import edu.seneca.modelo.usuarios.Usuario;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.Map;

public class ConsolaEmpleado extends ConsolaBase {

    public static void main(String[] args) {
        new ConsolaEmpleado().ejecutar();
    }

    protected String titulo() {
        return "Empleados";
    }

    protected boolean puedeEntrar(Usuario usuario) {
        return usuario instanceof Empleado && !(usuario instanceof Administrador);
    }

    protected void mostrarMenu() {
        System.out.println("\t1. Consultar torneos y su estado");
        System.out.println("\t2. Ver historial de un torneo");
        System.out.println("\t3. Inscribirme en un torneo individual");
        System.out.println("\t4. Inscribir una pareja en un torneo de dobles");
        System.out.println("\t5. Salirme de un torneo");
        System.out.println("\t6. Ver mis turnos");
        System.out.println("\t7. Comprar con descuento de empleado");

        if (usuario instanceof Entrenador) {
            System.out.println("\t8. Planear sesion de entrenamiento");
            System.out.println("\t9. Registrar estadisticas de un partido");
        }
        if (usuario instanceof Fisioterapeuta) {
            System.out.println("\t8. Registrar atencion a un socio");
        }
    }

    protected void ejecutarOpcion(String opcion) throws Exception {
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
                verTurnos();
                break;
            case "7":
                comprar();
                break;
            case "8":
                if (usuario instanceof Entrenador) {
                    planearSesion();
                } else if (usuario instanceof Fisioterapeuta) {
                    registrarAtencion();
                } else {
                    System.out.println("opcion no valida");
                }
                break;
            case "9":
                if (usuario instanceof Entrenador) {
                    registrarEstadistica();
                } else {
                    System.out.println("opcion no valida");
                }
                break;
            default:
                System.out.println("opcion no valida");
        }
    }

    private void verTurnos() {
        Empleado empleado = (Empleado) usuario;

        if (empleado.getTurnos().isEmpty()) {
            System.out.println("\tNo tiene turnos asignados");
        }

        for (Turno turno : empleado.getTurnos()) {
            System.out.println("\t" + turno.getDiaSemana() + " de " + turno.getHoraInicio() + " a " + turno.getHoraFin());
        }
    }

    private void planearSesion() throws Exception {
        for (Instalacion instalacion : club.getInstalaciones()) {
            System.out.println("\t" + instalacion.getId());
        }

        Instalacion instalacion = club.buscarInstalacion(entrada.leerTexto("Id de la instalacion").toUpperCase());
        LocalDate fecha = entrada.leerFecha("Fecha");
        LocalTime hora = entrada.leerHora("Hora");
        int duracion = entrada.leerEntero("Duracion en minutos", 15, 480);
        SesionEntrenamiento sesion = ((Entrenador) usuario).planearSesion(fecha, hora, duracion, instalacion);
        System.out.println("\tSesion planeada para el " + sesion.getFecha() + " a las " + sesion.getHora());
    }

    private void registrarEstadistica() throws Exception {
        for (Partido partido : club.getPartidos()) {
            System.out.println("\t" + partido.getId() + " | " + partido.getDeporte());
        }

        String idPartido = entrada.leerTexto("Id del partido").toUpperCase();
        String idSocio = entrada.leerTexto("Id del socio").toUpperCase();
        System.out.println("\tClaves segun el deporte: goles, amarillas, rojas, puntos, rebotes, aces, bloqueos, setsGanados, gamesGanados, puntosTotales");
        Map<String, Integer> datos = new HashMap<>();
        String clave = entrada.leerOpcional("Estadistica");

        while (clave != null) {
            datos.put(clave, entrada.leerEntero("Valor de " + clave, 0, 1000));
            clave = entrada.leerOpcional("Otra estadistica");
        }

        club.registrarEstadistica(idPartido, idSocio, datos);
        System.out.println("\tEstadisticas registradas");
    }

    private void registrarAtencion() throws Exception {
        String idSocio = entrada.leerTexto("Id del socio").toUpperCase();
        String descripcion = entrada.leerTexto("Descripcion de la atencion");
        ((Fisioterapeuta) usuario).registrarAtencion(club.buscarSocio(idSocio), descripcion);
        System.out.println("\tAtencion registrada");
    }
}
