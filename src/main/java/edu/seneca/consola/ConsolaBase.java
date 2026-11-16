package edu.seneca.consola;

import edu.seneca.ClubDeportivoSeneca;
import edu.seneca.modelo.torneos.BonoDescuento;
import edu.seneca.modelo.torneos.Encuentro;
import edu.seneca.modelo.torneos.EstadoTorneo;
import edu.seneca.modelo.torneos.InscripcionTorneo;
import edu.seneca.modelo.torneos.Modalidad;
import edu.seneca.modelo.torneos.TipoCupo;
import edu.seneca.modelo.torneos.TipoTorneo;
import edu.seneca.modelo.torneos.Torneo;
import edu.seneca.modelo.usuarios.Empleado;
import edu.seneca.modelo.usuarios.Socio;
import edu.seneca.modelo.usuarios.Usuario;
import edu.seneca.modelo.ventas.Producto;
import edu.seneca.modelo.ventas.TipoUbicacion;
import edu.seneca.modelo.ventas.Venta;
import edu.seneca.persistencia.PersistenciaClub;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Scanner;

public abstract class ConsolaBase {

    private static final int MAX_INTENTOS = 3;

    protected ClubDeportivoSeneca club;
    protected Entrada entrada;
    protected Usuario usuario;

    protected abstract String titulo();

    protected abstract boolean puedeEntrar(Usuario usuario);

    protected abstract void mostrarMenu();

    protected abstract void ejecutarOpcion(String opcion) throws Exception;

    public void ejecutar() {
        entrada = new Entrada(new Scanner(System.in));

        try {
            club = PersistenciaClub.cargarOCrear();
        } catch (Exception e) {
            System.out.println("No se pudieron cargar los datos de " + PersistenciaClub.ARCHIVO + ": " + e.getMessage());
            return;
        }

        System.out.println(club.getNombre() + " - " + titulo());

        try {
            if (!autenticar()) {
                System.out.println("Demasiados intentos fallidos");
                return;
            }

            atenderMenu();
        } catch (NoSuchElementException e) {
            System.out.println();
        }

        guardar();
    }

    private boolean autenticar() {
        for (int intento = 1; intento <= MAX_INTENTOS; intento++) {
            String login = entrada.leerTexto("Login");
            String password = entrada.leerTexto("Password");

            try {
                Usuario candidato = club.autenticar(login, password);

                if (puedeEntrar(candidato)) {
                    usuario = candidato;
                    System.out.println("Bienvenido(a), " + usuario.getNombre());
                    return true;
                }

                System.out.println("\tSu usuario no tiene acceso a esta consola");
            } catch (Exception e) {
                System.out.println("\t" + e.getMessage());
            }
        }
        return false;
    }

    private void atenderMenu() {
        boolean salir = false;

        while (!salir) {
            System.out.println();
            System.out.println(titulo());
            mostrarMenu();
            System.out.println("\t0. Guardar y salir");
            String opcion = entrada.leerTexto("Opcion");

            if (opcion.equals("0")) {
                salir = true;
            } else {
                try {
                    ejecutarOpcion(opcion);
                } catch (NoSuchElementException e) {
                    throw e;
                } catch (Exception e) {
                    System.out.println("\tRechazado: " + e.getMessage());
                }
            }
        }
    }

    private void guardar() {
        try {
            PersistenciaClub.guardar(club);
            System.out.println("Cambios guardados en " + PersistenciaClub.ARCHIVO);
        } catch (Exception e) {
            System.out.println("No se pudieron guardar los cambios: " + e.getMessage());
        }
    }

    protected void consultarTorneos() throws Exception {
        String estado = entrada.elegirOpcional("Estado", new String[] {EstadoTorneo.EN_CURSO.getTexto(), EstadoTorneo.FINALIZADO.getTexto()});
        TipoTorneo tipo = null;
        String tipoElegido = entrada.elegirOpcional("Tipo", new String[] {"RECREATIVO", "COMPETITIVO"});

        if (tipoElegido != null) {
            tipo = TipoTorneo.valueOf(tipoElegido);
        }

        String deporte = entrada.leerOpcional("Deporte o disciplina");
        listar(club.consultarTorneos(estado, tipo, deporte));
    }

    protected void listar(List<Torneo> torneos) {
        if (torneos.isEmpty()) {
            System.out.println("\tNo hay torneos");
            return;
        }

        for (Torneo torneo : torneos) {
            System.out.println("\t" + resumen(torneo));
        }
    }

    protected String resumen(Torneo torneo) {
        return torneo.getId() + " | " + torneo.getNombre() + " | " + torneo.getTipo() + " | " + torneo.getDeporte() + " " + torneo.getCategoria() + " (" + torneo.getModalidad() + ") | " + torneo.getFecha() + " | cupos " + torneo.getInscripciones().size() + "/" + torneo.getCupos() + " | " + torneo.getEstado().getTexto();
    }

    protected Torneo elegirTorneo() throws Exception {
        listar(club.getTorneos());
        return club.buscarTorneo(entrada.leerTexto("Id del torneo"));
    }

    protected void verHistorial() throws Exception {
        Torneo torneo = elegirTorneo();
        System.out.println(resumen(torneo));
        int libresPreferenciales = torneo.getCuposPreferenciales() - torneo.contarOcupados(TipoCupo.PREFERENCIAL);
        int libresRegulares = torneo.getCuposRegulares() - torneo.contarOcupados(TipoCupo.REGULAR);
        System.out.println("Cupos libres: " + libresRegulares + " regulares, " + libresPreferenciales + " preferenciales");

        if (torneo.getTipo() == TipoTorneo.COMPETITIVO) {
            System.out.println("Tarifa: " + torneo.getTarifa() + " | Recaudado: " + torneo.getTotalRecaudado());
        }

        System.out.println("Participantes:");

        if (torneo.getInscripciones().isEmpty()) {
            System.out.println("\t(ninguno)");
        }

        for (InscripcionTorneo inscripcion : torneo.getInscripciones()) {
            String nota = "";

            if (torneo.getTipo() == TipoTorneo.COMPETITIVO && !inscripcion.tieneDerechoAPremioEfectivo()) {
                nota = " - sin derecho a premio en efectivo";
            }

            System.out.println("\t" + inscripcion.getIdPrincipal() + " " + inscripcion.getNombre() + " (cupo " + inscripcion.getTipoCupo() + ")" + nota);
        }

        System.out.println("Encuentros:");

        if (torneo.getEncuentros().isEmpty()) {
            System.out.println("\t(ninguno)");
        }

        int numero = 1;

        for (Encuentro encuentro : torneo.getEncuentros()) {
            String resultado = "sin resultado";

            if (encuentro.tieneResultado()) {
                resultado = encuentro.getMarcador() + ", gana " + encuentro.getGanador().getNombre();
            }

            System.out.println("\t" + numero + ". " + encuentro.getParticipanteA().getNombre() + " vs " + encuentro.getParticipanteB().getNombre() + ": " + resultado);
            numero++;
        }

        System.out.println("Posiciones:");
        int puesto = 1;

        for (InscripcionTorneo inscripcion : torneo.getPosiciones()) {
            System.out.println("\t" + puesto + ". " + inscripcion.getNombre() + " (" + torneo.contarVictorias(inscripcion) + " victorias)");
            puesto++;
        }

        if (torneo.describirPremio() != null) {
            System.out.println("Premio: " + torneo.describirPremio());
        }
    }

    private boolean confirmarPago(Torneo torneo, int jugadoresQuePagan) {
        double tarifa = torneo.getTarifa() * jugadoresQuePagan;

        if (tarifa <= 0) {
            System.out.println("\tEsta inscripcion no tiene costo");
            return true;
        }

        System.out.println("\tTarifa a pagar: " + tarifa + " (el cobro lo hace la pasarela externa)");
        return entrada.leerSiNo("La pasarela confirmo el pago?");
    }

    protected void inscribirmeEnTorneo() throws Exception {
        Torneo torneo = elegirTorneo();
        int pagan = 1;

        if (usuario instanceof Empleado) {
            pagan = 0;
        }

        InscripcionTorneo inscripcion;

        if (usuario instanceof Socio && entrada.leerSiNo("Solicitar cupo preferencial por ranking?")) {
            inscripcion = club.inscribirEnCupoPreferencial(torneo.getId(), usuario.getId(), confirmarPago(torneo, pagan));
        } else {
            inscripcion = club.inscribirEnTorneo(torneo.getId(), usuario.getId(), confirmarPago(torneo, pagan));
        }

        mostrarInscripcion(torneo, inscripcion);
    }

    protected void inscribirPareja() throws Exception {
        Torneo torneo = elegirTorneo();

        if (torneo.getModalidad() != Modalidad.DOBLES) {
            throw new Exception("El torneo " + torneo.getNombre() + " no es de dobles");
        }

        String companero = entrada.leerTexto("Login o documento del companero");
        System.out.println("\tSi el torneo tiene tarifa, la paga cada integrante que no sea empleado");
        boolean pago = true;

        if (torneo.getTarifa() > 0) {
            pago = entrada.leerSiNo("La pasarela confirmo el pago de la pareja?");
        }

        mostrarInscripcion(torneo, club.inscribirPareja(torneo.getId(), usuario.getId(), companero, pago));
    }

    private void mostrarInscripcion(Torneo torneo, InscripcionTorneo inscripcion) {
        System.out.println("\tInscripcion confirmada en " + torneo.getNombre() + ": " + inscripcion.getNombre() + ", cupo " + inscripcion.getTipoCupo());

        if (torneo.getTipo() == TipoTorneo.COMPETITIVO) {
            System.out.println("\tTarifa pagada: " + inscripcion.getTarifaPagada());

            if (!inscripcion.tieneDerechoAPremioEfectivo()) {
                System.out.println("\tComo empleado participa sin derecho a premio en efectivo");
            }
        }
    }

    protected void salirmeDeTorneo() throws Exception {
        List<Torneo> mios = new ArrayList<>();

        for (Torneo torneo : club.getTorneos()) {
            if (torneo.buscarInscripcion(usuario) != null) {
                mios.add(torneo);
            }
        }

        if (mios.isEmpty()) {
            System.out.println("\tNo esta inscrito en ningun torneo");
            return;
        }

        listar(mios);
        Torneo torneo = club.buscarTorneo(entrada.leerTexto("Id del torneo"));
        club.salirDeTorneo(torneo.getId(), usuario.getId());
        System.out.println("\tInscripcion cancelada: se libero el cupo en " + torneo.getNombre());
    }

    protected void comprar() throws Exception {
        System.out.println("Productos:");

        for (Producto producto : club.getProductos()) {
            double precio = producto.getPrecioBase() + producto.getImpuesto();
            System.out.println("\t" + producto.getId() + " | " + producto.getNombre() + " | " + precio + " | existencias: " + producto.getStock());
        }

        List<String> carrito = new ArrayList<>();
        String[] ids = entrada.leerTexto("Ids de los productos separados por coma (repita el id para llevar varios)").split(",");

        for (String id : ids) {
            String limpio = id.trim().toUpperCase();

            if (!limpio.isEmpty()) {
                club.buscarProducto(limpio);
                carrito.add(limpio);
            }
        }

        if (carrito.isEmpty()) {
            throw new Exception("El carrito esta vacio");
        }

        TipoUbicacion ubicacion = TipoUbicacion.valueOf(entrada.elegir("Donde se entrega", new String[] {"CAFETERIA", "CANCHA_PADEL", "MESA_TECHADA"}));
        String metodoPago = entrada.elegir("Metodo de pago", new String[] {"Tarjeta", "Efectivo"});

        String idBono = null;
        int puntos = 0;

        if (usuario instanceof Socio) {
            Socio socio = (Socio) usuario;
            List<BonoDescuento> disponibles = new ArrayList<>();

            for (BonoDescuento bono : socio.getBonos()) {
                if (!bono.isUsado()) {
                    disponibles.add(bono);
                }
            }

            if (!disponibles.isEmpty() && entrada.leerSiNo("Tiene " + disponibles.size() + " bono(s) de torneo. usar uno?")) {
                for (BonoDescuento bono : disponibles) {
                    System.out.println("\t" + bono.getId() + " | " + bono.getValor() + " | " + bono.getTorneoOrigen());
                }

                idBono = entrada.leerTexto("Id del bono");
            }
            if (idBono == null && socio.getPuntosFidelidad() > 0) {
                puntos = entrada.leerEntero("Puntos de fidelidad a redimir (tiene " + socio.getPuntosFidelidad() + ", 0 para ninguno)", 0, socio.getPuntosFidelidad());
            }
        }

        if (club.procesarVenta(usuario.getId(), carrito, metodoPago, ubicacion, idBono, puntos)) {
            Venta venta = club.getVentas().get(club.getVentas().size() - 1);
            System.out.println("\tCompra registrada. total a pagar: " + venta.getTotal());
        } else {
            System.out.println("\tCompra rechazada: no hay existencias suficientes o pidio una bebida caliente en una zona donde no se permite");
        }
    }
}
