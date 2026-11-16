package edu.seneca.consola;

import edu.seneca.modelo.deportes.Equipo;
import edu.seneca.modelo.torneos.Modalidad;
import edu.seneca.modelo.torneos.TipoTorneo;
import edu.seneca.modelo.torneos.Torneo;
import edu.seneca.modelo.usuarios.Administrador;
import edu.seneca.modelo.usuarios.Empleado;
import edu.seneca.modelo.usuarios.NivelDeportivo;
import edu.seneca.modelo.usuarios.ParticipanteExterno;
import edu.seneca.modelo.usuarios.Turno;
import edu.seneca.modelo.usuarios.Usuario;

import java.time.LocalDate;
import java.time.LocalTime;

public class ConsolaAdministrador extends ConsolaBase {

    private static final String[] NIVELES = {"PRINCIPIANTE", "INTERMEDIO", "AVANZADO"};
    private static final String[] DIAS = {"Lunes", "Martes", "Miercoles", "Jueves", "Viernes", "Sabado", "Domingo"};

    public static void main(String[] args) {
        new ConsolaAdministrador().ejecutar();
    }

    protected String titulo() {
        return "Administrador";
    }

    protected boolean puedeEntrar(Usuario usuario) {
        return usuario instanceof Administrador;
    }

    protected void mostrarMenu() {
        System.out.println("\t1. Crear torneo");
        System.out.println("\t2. Consultar torneos y su estado");
        System.out.println("\t3. Ver historial de un torneo");
        System.out.println("\t4. Inscribir equipo en un torneo");
        System.out.println("\t5. Retirar equipo de un torneo");
        System.out.println("\t6. Iniciar torneo");
        System.out.println("\t7. Programar encuentro");
        System.out.println("\t8. Registrar resultado de un encuentro");
        System.out.println("\t9. Finalizar torneo y entregar premios");
        System.out.println("\t10. Registrar participante externo o estudiante");
        System.out.println("\t11. Asignar turno a un empleado");
        System.out.println("\t12. Reporte de ventas");
        System.out.println("\t13. Reabastecer inventario");
        System.out.println("\t14. Listar usuarios y equipos");
    }

    protected void ejecutarOpcion(String opcion) throws Exception {
        switch (opcion) {
            case "1":
                crearTorneo();
                break;
            case "2":
                consultarTorneos();
                break;
            case "3":
                verHistorial();
                break;
            case "4":
                inscribirEquipo();
                break;
            case "5":
                retirarEquipo();
                break;
            case "6":
                iniciarTorneo();
                break;
            case "7":
                programarEncuentro();
                break;
            case "8":
                registrarResultado();
                break;
            case "9":
                finalizarTorneo();
                break;
            case "10":
                registrarExterno();
                break;
            case "11":
                asignarTurno();
                break;
            case "12":
                reporteVentas();
                break;
            case "13":
                reabastecer();
                break;
            case "14":
                listarUsuariosYEquipos();
                break;
            default:
                System.out.println("opcion no valida");
        }
    }

    private void crearTorneo() throws Exception {
        String nombre = entrada.leerTexto("Nombre del torneo");
        Modalidad modalidad = Modalidad.valueOf(entrada.elegir("Modalidad", new String[] {"CONJUNTO", "SENCILLOS", "DOBLES"}));
        String deporte;
        String categoria;

        if (modalidad == Modalidad.CONJUNTO) {
            deporte = entrada.elegir("Deporte", new String[] {"Futbol", "Baloncesto", "Voleibol"});
            categoria = entrada.elegir("Categoria", new String[] {"Sub-13", "Sub-15", "Sub-17", "Mayores"});
        } else {
            deporte = entrada.elegir("Disciplina", new String[] {"Tenis", "Padel", "Tenis de mesa"});
            categoria = entrada.elegir("Nivel", NIVELES);
        }

        TipoTorneo tipo = TipoTorneo.valueOf(entrada.elegir("Tipo", new String[] {"RECREATIVO", "COMPETITIVO"}));
        int cupos = entrada.leerEntero("Numero de cupos", 1, 1000);
        LocalDate fecha = entrada.leerFecha("Fecha del torneo");

        while (fecha.isBefore(LocalDate.now())) {
            System.out.println("\tLa fecha no puede estar en el pasado");
            fecha = entrada.leerFecha("Fecha del torneo");
        }

        double tarifa = 0;
        double porcentaje = 0;
        double bono = 0;

        if (tipo == TipoTorneo.COMPETITIVO) {
            tarifa = entrada.leerDecimal("Tarifa de inscripcion", 1, 100000000);
            porcentaje = entrada.leerDecimal("Porcentaje del recaudado que se entrega como premio (1 a 100)", 1, 100);
        } else {
            bono = entrada.leerDecimal("Valor del bono de descuento para el ganador", 1, 100000000);
        }

        Torneo torneo = club.crearTorneo(nombre, deporte, categoria, modalidad, tipo, cupos, fecha, tarifa, porcentaje, bono);
        System.out.println("\tTorneo " + torneo.getId() + " creado con inscripciones abiertas: " + torneo.getCupos() + " cupos (" + torneo.getCuposPreferenciales() + " preferenciales y " + torneo.getCuposRegulares() + " regulares)");
    }

    private void listarEquipos() {
        for (Equipo equipo : club.getEquipos()) {
            String estado = "inactivo";

            if (equipo.isActivo()) {
                estado = "activo";
            }

            System.out.println("\t" + equipo.getId() + " | " + equipo.getDeporte() + " " + equipo.getCategoria() + " | " + equipo.contarHabilitados() + " habilitados | ranking " + equipo.getRanking() + " | " + estado);
        }
    }

    private void inscribirEquipo() throws Exception {
        Torneo torneo = elegirTorneo();
        listarEquipos();
        String idEquipo = entrada.leerTexto("Id del equipo").toUpperCase();
        System.out.println("\tEquipo inscrito en cupo " + club.inscribirEquipoEnTorneo(torneo.getId(), idEquipo).getTipoCupo());
    }

    private void retirarEquipo() throws Exception {
        Torneo torneo = elegirTorneo();
        String idEquipo = entrada.leerTexto("Id del equipo").toUpperCase();
        club.retirarEquipoDeTorneo(torneo.getId(), idEquipo);
        System.out.println("\tEquipo retirado: se libero su cupo");
    }

    private void iniciarTorneo() throws Exception {
        Torneo torneo = elegirTorneo();
        club.iniciarTorneo(torneo.getId());
        System.out.println("\t" + torneo.getNombre() + " esta en curso. los cupos preferenciales vacios quedaron como regulares");
    }

    private void programarEncuentro() throws Exception {
        Torneo torneo = elegirTorneo();
        System.out.println("\tUse el id del equipo o de un jugador (vea el historial del torneo)");
        String idA = entrada.leerTexto("Id del primer participante").toUpperCase();
        String idB = entrada.leerTexto("Id del segundo participante").toUpperCase();
        club.programarEncuentro(torneo.getId(), idA, idB);
        System.out.println("\tEncuentro " + torneo.getEncuentros().size() + " programado");
    }

    private void registrarResultado() throws Exception {
        Torneo torneo = elegirTorneo();

        if (torneo.getEncuentros().isEmpty()) {
            throw new Exception("El torneo no tiene encuentros programados");
        }

        int numero = entrada.leerEntero("Numero del encuentro", 1, torneo.getEncuentros().size());
        String marcador = entrada.leerTexto("Marcador");
        String idGanador = entrada.leerTexto("Id del ganador").toUpperCase();
        club.registrarResultadoEncuentro(torneo.getId(), numero, marcador, idGanador);
        System.out.println("\tResultado registrado");
    }

    private void finalizarTorneo() throws Exception {
        Torneo torneo = elegirTorneo();
        String idGanador = entrada.leerTexto("Id del ganador del torneo (equipo o jugador)").toUpperCase();
        club.finalizarTorneo(torneo.getId(), idGanador);
        System.out.println("\t" + torneo.getNombre() + " quedo finalizado. " + torneo.describirPremio());
    }

    private void registrarExterno() throws Exception {
        String nombre = entrada.leerTexto("Nombre completo");
        String documento = entrada.leerTexto("Documento");
        LocalDate nacimiento = entrada.leerFecha("Fecha de nacimiento");
        String login = entrada.leerTexto("Login");
        String password = entrada.leerTexto("Password");
        NivelDeportivo nivel = NivelDeportivo.valueOf(entrada.elegir("Nivel deportivo", NIVELES));
        boolean estudiante = entrada.leerSiNo("Es estudiante matriculado en la universidad?");

        ParticipanteExterno externo = club.registrarParticipanteExterno(nombre, documento, nacimiento, login, password, nivel, estudiante);
        System.out.println("\tParticipante " + externo.getId() + " creado. puede entrar por la consola de socios y participantes");
    }

    private void asignarTurno() throws Exception {
        for (Usuario candidato : club.getUsuarios()) {
            if (candidato instanceof Empleado && !(candidato instanceof Administrador)) {
                System.out.println("\t" + candidato.getId() + " | " + candidato.getNombre() + " | " + ((Empleado) candidato).getCargo());
            }
        }

        String idEmpleado = entrada.leerTexto("Id del empleado").toUpperCase();
        String dia = entrada.elegir("Dia", DIAS);
        LocalTime inicio = entrada.leerHora("Hora de inicio");
        LocalTime fin = entrada.leerHora("Hora de fin");
        club.asignarTurno(idEmpleado, new Turno(dia, inicio, fin));
        System.out.println("\tTurno asignado");
    }

    private void reporteVentas() {
        LocalDate desde = entrada.leerFecha("Desde");
        LocalDate hasta = entrada.leerFecha("Hasta");
        ((Administrador) usuario).generarReporteVentas(desde, hasta, club.getVentas());
    }

    private void reabastecer() throws Exception {
        String idProducto = entrada.leerTexto("Id del producto").toUpperCase();
        int cantidad = entrada.leerEntero("Unidades que entran", 1, 100000);
        ((Administrador) usuario).reabastecerInventario(idProducto, cantidad, club.getProductos());
        System.out.println("\tExistencias de " + idProducto + ": " + club.buscarProducto(idProducto).getStock());
    }

    private void listarUsuariosYEquipos() {
        System.out.println("Usuarios:");

        for (Usuario candidato : club.getUsuarios()) {
            System.out.println("\t" + candidato.getId() + " | " + candidato.getNombre() + " | login: " + candidato.getLogin() + " | " + candidato.getClass().getSimpleName());
        }

        System.out.println("Equipos:");
        listarEquipos();
    }
}
