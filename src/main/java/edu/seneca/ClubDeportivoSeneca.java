package edu.seneca;

import edu.seneca.modelo.deportes.Competencia;
import edu.seneca.modelo.deportes.Equipo;
import edu.seneca.modelo.deportes.InscripcionEquipo;
import edu.seneca.modelo.deportes.Partido;
import edu.seneca.modelo.deportes.PartidoCompetencia;
import edu.seneca.modelo.deportes.PartidoInterno;
import edu.seneca.modelo.deportes.PartidoPractica;
import edu.seneca.modelo.estadisticas.EstadisticaBaloncesto;
import edu.seneca.modelo.estadisticas.EstadisticaFutbol;
import edu.seneca.modelo.estadisticas.EstadisticaIndividual;
import edu.seneca.modelo.estadisticas.EstadisticaPartido;
import edu.seneca.modelo.estadisticas.EstadisticaVoleibol;
import edu.seneca.modelo.instalaciones.Instalacion;
import edu.seneca.modelo.instalaciones.ReservaInstalacion;
import edu.seneca.modelo.torneos.BonoDescuento;
import edu.seneca.modelo.torneos.EstadoTorneo;
import edu.seneca.modelo.torneos.InscripcionTorneo;
import edu.seneca.modelo.torneos.Modalidad;
import edu.seneca.modelo.torneos.TipoCupo;
import edu.seneca.modelo.torneos.TipoTorneo;
import edu.seneca.modelo.torneos.Torneo;
import edu.seneca.modelo.torneos.TorneoCompetitivo;
import edu.seneca.modelo.torneos.TorneoRecreativo;
import edu.seneca.modelo.usuarios.Administrador;
import edu.seneca.modelo.usuarios.Empleado;
import edu.seneca.modelo.usuarios.NivelDeportivo;
import edu.seneca.modelo.usuarios.ParticipanteExterno;
import edu.seneca.modelo.usuarios.Sancion;
import edu.seneca.modelo.usuarios.Socio;
import edu.seneca.modelo.usuarios.TipoTarjeta;
import edu.seneca.modelo.usuarios.Turno;
import edu.seneca.modelo.usuarios.Usuario;
import edu.seneca.modelo.ventas.Bebida;
import edu.seneca.modelo.ventas.LineaVenta;
import edu.seneca.modelo.ventas.Producto;
import edu.seneca.modelo.ventas.TipoUbicacion;
import edu.seneca.modelo.ventas.Venta;

import java.io.Serializable;
import java.text.Normalizer;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class ClubDeportivoSeneca implements Serializable {

    private static final long serialVersionUID = 1L;

    private static final double DESCUENTO_EMPLEADO = 0.15;

    private static final String[] DEPORTES_CONJUNTO = {"Futbol", "Baloncesto", "Voleibol"};
    private static final String[] DISCIPLINAS_INDIVIDUALES = {"Tenis", "Padel", "Tenis de mesa"};
    private static final String[] CATEGORIAS = {"Sub-13", "Sub-15", "Sub-17", "Mayores"};
    private static final String[] DIAS_SEMANA = {"Lunes", "Martes", "Miercoles", "Jueves", "Viernes", "Sabado", "Domingo"};

    private String nombre;
    private List<Usuario> usuarios;
    private List<Producto> productos;
    private List<Instalacion> instalaciones;
    private List<ReservaInstalacion> reservas;
    private List<Equipo> equipos;
    private List<Competencia> competencias;
    private List<Partido> partidos;
    private List<Venta> ventas;
    private List<Torneo> torneos;
    private int consecutivoTorneos;
    private int consecutivoExternos;

    public ClubDeportivoSeneca(String nombre) {
        this.nombre = nombre;
        this.usuarios = new ArrayList<>();
        this.productos = new ArrayList<>();
        this.instalaciones = new ArrayList<>();
        this.reservas = new ArrayList<>();
        this.equipos = new ArrayList<>();
        this.competencias = new ArrayList<>();
        this.partidos = new ArrayList<>();
        this.ventas = new ArrayList<>();
        this.torneos = new ArrayList<>();
        this.consecutivoTorneos = 0;
        this.consecutivoExternos = 0;
    }

    public boolean procesarReserva(String idSocio, String idInstalacion, LocalDate fecha, LocalTime hora, String idOponente) throws Exception {
        Socio socio = buscarSocio(idSocio);
        Instalacion instalacion = buscarInstalacion(idInstalacion);

        int jugadores = 1;
        if (idOponente != null) {
            Socio oponente = buscarSocio(idOponente);
            if (!socio.esNivelCompatible(oponente)) {
                return false;
            }
            jugadores = 2;
        }

        if (!instalacion.verificarDisponibilidad(fecha, hora)) {
            return false;
        }

        ReservaInstalacion nuevaReserva = new ReservaInstalacion(fecha, hora, jugadores);
        nuevaReserva.confirmar();
        socio.agregarReserva(nuevaReserva);
        instalacion.agregarReserva(nuevaReserva);
        reservas.add(nuevaReserva);
        return true;
    }

    public boolean procesarVenta(String idUsuario, List<String> carrito, String metodoPago, TipoUbicacion ubicacion) throws Exception {
        return procesarVenta(idUsuario, carrito, metodoPago, ubicacion, null, 0);
    }

    public boolean procesarVenta(String idUsuario, List<String> carrito, String metodoPago, TipoUbicacion ubicacion, String idBono, int puntosARedimir) throws Exception {
        Usuario usuario = buscarUsuario(idUsuario);

        double descuento = 0;

        if (usuario instanceof Empleado) {
            descuento = DESCUENTO_EMPLEADO;
        }

        BonoDescuento bono = null;

        if (idBono != null) {
            bono = validarBono(usuario, idBono, descuento > 0 || puntosARedimir > 0);
        }
        if (puntosARedimir < 0) {
            throw new Exception("Los puntos a redimir no pueden ser negativos");
        }
        if (puntosARedimir > 0) {
            if (!(usuario instanceof Socio) || ((Socio) usuario).getPuntosFidelidad() < puntosARedimir) {
                throw new Exception("No tiene " + puntosARedimir + " puntos de fidelidad para redimir");
            }
        }

        Map<Producto, Integer> cantidades = new LinkedHashMap<>();
        for (String idProducto : carrito) {
            Producto producto = buscarProducto(idProducto);
            if (esBebidaCalienteProhibida(producto, ubicacion)) {
                return false;
            }
            if (cantidades.containsKey(producto)) {
                cantidades.put(producto, cantidades.get(producto) + 1);
            } else {
                cantidades.put(producto, 1);
            }
        }
        for (Map.Entry<Producto, Integer> entrada : cantidades.entrySet()) {
            if (!entrada.getKey().verificarStock(entrada.getValue())) {
                return false;
            }
        }

        Venta nuevaVenta = new Venta(LocalDate.now(), idUsuario);

        for (String idProducto : carrito) {
            Producto producto = buscarProducto(idProducto);
            producto.reducirStock(1);
            double subtotal = producto.getPrecioBase() + producto.getImpuesto();
            nuevaVenta.agregarLineaVenta(new LineaVenta(producto, 1, subtotal));
        }

        nuevaVenta.calcularTotalFinal(descuento);
        if (bono != null) {
            nuevaVenta.aplicarDescuentoFijo(bono.getValor());
            bono.marcarUsado();
        }
        if (puntosARedimir > 0) {
            nuevaVenta.aplicarDescuentoFijo(puntosARedimir);
            ((Socio) usuario).descontarPuntosFidelidad(puntosARedimir);
        }

        if (usuario instanceof Socio) {
            Socio socio = (Socio) usuario;
            socio.sumarPuntosFidelidad(nuevaVenta.calcularPuntosGenerados());
        }
        ventas.add(nuevaVenta);
        return true;
    }

    private BonoDescuento validarBono(Usuario usuario, String idBono, boolean hayOtroDescuento) throws Exception {
        BonoDescuento bono = null;

        if (usuario instanceof Socio) {
            bono = ((Socio) usuario).buscarBono(idBono);
        }

        if (bono == null) {
            throw new Exception("El bono " + idBono + " no existe o no pertenece a " + usuario.getNombre());
        }
        if (bono.isUsado()) {
            throw new Exception("El bono " + idBono + " ya fue usado");
        }
        if (hayOtroDescuento) {
            throw new Exception("El bono no es acumulable con otros descuentos");
        }
        return bono;
    }

    public void registrarEstadistica(String idPartido, String idSocio, Map<String, Integer> estadisticas) throws Exception {
        Partido partido = buscarPartido(idPartido);
        Socio socio = buscarSocio(idSocio);

        if (partido instanceof PartidoPractica) {
            PartidoPractica practica = (PartidoPractica) partido;
            int sets = valor(estadisticas, "setsGanados");
            int games = valor(estadisticas, "gamesGanados");
            int puntos = valor(estadisticas, "puntosTotales");
            practica.agregarEstadistica(new EstadisticaIndividual(idSocio, sets, games, puntos));
            return;
        }

        EstadisticaPartido estadistica;
        String deporte = partido.getDeporte();
        if (deporte.equals("Futbol")) {
            int goles = valor(estadisticas, "goles");
            int amarillas = valor(estadisticas, "amarillas");
            int rojas = valor(estadisticas, "rojas");
            estadistica = new EstadisticaFutbol(idSocio, goles, amarillas, rojas);
            evaluarReglasSancion(socio, amarillas, rojas);
        } else if (deporte.equals("Baloncesto")) {
            int puntos = valor(estadisticas, "puntos");
            int rebotes = valor(estadisticas, "rebotes");
            estadistica = new EstadisticaBaloncesto(idSocio, puntos, rebotes);
        } else if (deporte.equals("Voleibol")) {
            int aces = valor(estadisticas, "aces");
            int bloqueos = valor(estadisticas, "bloqueos");
            estadistica = new EstadisticaVoleibol(idSocio, aces, bloqueos);
        } else {
            throw new Exception("Deporte sin estadisticas: " + deporte);
        }

        if (partido instanceof PartidoInterno) {
            PartidoInterno interno = (PartidoInterno) partido;
            interno.agregarEstadistica(estadistica);
        } else if (partido instanceof PartidoCompetencia) {
            PartidoCompetencia competencia = (PartidoCompetencia) partido;
            competencia.agregarEstadistica(estadistica);
        }
    }

    public void evaluarReglasSancion(Socio socio, int amarillas, int rojas) {
        for (int i = 0; i < amarillas; i++) {
            socio.agregarSancion(new Sancion(TipoTarjeta.AMARILLA));
        }
        for (int i = 0; i < rojas; i++) {
            socio.agregarSancion(new Sancion(TipoTarjeta.ROJA));
        }

        int amarillasActivas = 0;
        boolean tieneRojaActiva = false;
        for (Sancion sancion : socio.getSanciones()) {
            if (sancion.isActiva() && sancion.getTipo() == TipoTarjeta.AMARILLA) {
                amarillasActivas++;
            }
            if (sancion.isActiva() && sancion.getTipo() == TipoTarjeta.ROJA) {
                tieneRojaActiva = true;
            }
        }
        if (amarillasActivas >= 2 || tieneRojaActiva) {
            socio.bloquearInscripcionProximoPartido();
        }
    }

    public boolean procesarInscripcion(String idSocio, String idEquipo, String posicion, int numCamiseta) throws Exception {
        Socio socio = buscarSocio(idSocio);
        Equipo equipo = buscarEquipo(idEquipo);

        if (!equipo.verificarCategoria(socio.getEdad())) {
            return false;
        }
        if (socio.tieneEquipoEnDeporte(equipo.getDeporte())) {
            return false;
        }

        InscripcionEquipo nuevaInscripcion;
        try {
            nuevaInscripcion = equipo.crearInscripcion(socio, posicion, numCamiseta);
        } catch (Exception e) {
            return false;
        }
        socio.vincularInscripcion(nuevaInscripcion);
        return true;
    }

    public Usuario autenticar(String login, String password) throws Exception {
        for (Usuario usuario : usuarios) {
            if (usuario.getLogin().equals(login) && usuario.autenticar(password)) {
                return usuario;
            }
        }
        throw new Exception("Login o password incorrectos");
    }

    public Torneo crearTorneo(String nombre, String deporte, String categoria, Modalidad modalidad, TipoTorneo tipo, int cupos, LocalDate fecha, double tarifa, double porcentajePremio, double valorBono) throws Exception {
        exigirDato(nombre, "nombre");
        exigirDato(deporte, "deporte o disciplina");
        exigirDato(categoria, "categoria o nivel");

        if (modalidad == null) {
            throw new Exception("Falta el dato: modalidad");
        }
        if (tipo == null) {
            throw new Exception("Falta el dato: tipo de torneo");
        }
        if (fecha == null) {
            throw new Exception("Falta el dato: fecha");
        }
        if (cupos <= 0) {
            throw new Exception("El numero de cupos debe ser mayor que 0");
        }

        String deporteOficial;
        String categoriaOficial;

        if (modalidad == Modalidad.CONJUNTO) {
            deporteOficial = buscarEnLista(DEPORTES_CONJUNTO, deporte);

            if (deporteOficial == null) {
                throw new Exception("No existe el deporte de conjunto: " + deporte);
            }

            categoriaOficial = buscarEnLista(CATEGORIAS, categoria);

            if (categoriaOficial == null) {
                throw new Exception("No existe la categoria: " + categoria);
            }

            int minimo = minimoJugadores(deporteOficial);

            if (cupos < minimo) {
                throw new Exception("Un torneo de " + deporteOficial + " necesita al menos " + minimo + " cupos");
            }
        } else {
            deporteOficial = buscarEnLista(DISCIPLINAS_INDIVIDUALES, deporte);

            if (deporteOficial == null) {
                throw new Exception("No existe la disciplina individual: " + deporte);
            }

            categoriaOficial = null;

            for (NivelDeportivo nivel : NivelDeportivo.values()) {
                if (nivel.name().equalsIgnoreCase(categoria.trim())) {
                    categoriaOficial = nivel.name();
                }
            }

            if (categoriaOficial == null) {
                throw new Exception("No existe el nivel: " + categoria);
            }
            if (modalidad == Modalidad.DOBLES && cupos < 2) {
                throw new Exception("Un torneo de dobles necesita al menos 2 cupos");
            }
        }

        for (Torneo existente : torneos) {
            if (existente.getNombre().equalsIgnoreCase(nombre.trim()) && existente.getFecha().equals(fecha)) {
                throw new Exception("Ya existe un torneo llamado " + nombre.trim() + " en la fecha " + fecha);
            }
        }

        Torneo torneo;

        if (tipo == TipoTorneo.COMPETITIVO) {
            if (tarifa <= 0) {
                throw new Exception("Un torneo competitivo exige una tarifa de inscripcion mayor que 0");
            }
            if (porcentajePremio <= 0 || porcentajePremio > 100) {
                throw new Exception("El porcentaje del premio debe estar entre 1 y 100");
            }

            torneo = new TorneoCompetitivo(siguienteIdTorneo(), nombre.trim(), deporteOficial, categoriaOficial, modalidad, cupos, fecha, tarifa, porcentajePremio / 100.0);
        } else {
            if (tarifa != 0) {
                throw new Exception("Un torneo recreativo no tiene costo de inscripcion");
            }
            if (valorBono <= 0) {
                throw new Exception("Un torneo recreativo necesita un bono de valor mayor que 0");
            }

            torneo = new TorneoRecreativo(siguienteIdTorneo(), nombre.trim(), deporteOficial, categoriaOficial, modalidad, cupos, fecha, valorBono);
        }

        torneos.add(torneo);
        return torneo;
    }

    public List<Torneo> consultarTorneos(String estado, TipoTorneo tipo, String deporte) throws Exception {
        EstadoTorneo estadoBuscado = null;

        if (estado != null) {
            estadoBuscado = EstadoTorneo.desdeFiltro(estado);
        }

        List<Torneo> resultado = new ArrayList<>();

        for (Torneo torneo : torneos) {
            boolean coincide = true;

            if (estadoBuscado != null && torneo.getEstado() != estadoBuscado) {
                coincide = false;
            }
            if (tipo != null && torneo.getTipo() != tipo) {
                coincide = false;
            }
            if (deporte != null && !torneo.getDeporte().equalsIgnoreCase(deporte.trim())) {
                coincide = false;
            }
            if (coincide) {
                resultado.add(torneo);
            }
        }
        return resultado;
    }

    public Torneo buscarTorneo(String id) throws Exception {
        for (Torneo torneo : torneos) {
            if (torneo.getId().equalsIgnoreCase(id)) {
                return torneo;
            }
        }
        throw new Exception("No se encontro el torneo " + id);
    }

    public void iniciarTorneo(String idTorneo) throws Exception {
        buscarTorneo(idTorneo).iniciar();
    }

    public void programarEncuentro(String idTorneo, String idA, String idB) throws Exception {
        Torneo torneo = buscarTorneo(idTorneo);
        torneo.programarEncuentro(torneo.buscarInscripcionPorId(idA), torneo.buscarInscripcionPorId(idB));
    }

    public void registrarResultadoEncuentro(String idTorneo, int numeroEncuentro, String marcador, String idGanador) throws Exception {
        Torneo torneo = buscarTorneo(idTorneo);
        torneo.registrarResultado(numeroEncuentro, marcador, torneo.buscarInscripcionPorId(idGanador));
    }

    public void finalizarTorneo(String idTorneo, String idGanador) throws Exception {
        Torneo torneo = buscarTorneo(idTorneo);

        if (torneo.getEstado() != EstadoTorneo.EN_CURSO) {
            throw new Exception("Solo se puede finalizar un torneo en curso (estado: " + torneo.getEstado().getTexto() + ")");
        }

        torneo.finalizar(torneo.buscarInscripcionPorId(idGanador));
    }

    public InscripcionTorneo inscribirEquipoEnTorneo(String idTorneo, String idEquipo) throws Exception {
        Torneo torneo = buscarTorneo(idTorneo);
        Equipo equipo = buscarEquipoDeTorneo(idEquipo);
        torneo.verificarInscripcionesAbiertas();

        if (torneo.esIndividual()) {
            throw new Exception("En el torneo " + torneo.getNombre() + " se inscriben jugadores, no equipos");
        }
        if (!equipo.getDeporte().equals(torneo.getDeporte())) {
            throw new Exception("El equipo " + idEquipo + " es de " + equipo.getDeporte() + " y el torneo de " + torneo.getDeporte());
        }
        if (!equipo.getCategoria().equals(torneo.getCategoria())) {
            throw new Exception("El equipo " + idEquipo + " es de categoria " + equipo.getCategoria() + " y el torneo de " + torneo.getCategoria());
        }
        if (!equipo.isActivo()) {
            throw new Exception("El equipo " + idEquipo + " esta inactivo");
        }

        int minimo = minimoJugadores(torneo.getDeporte());

        if (equipo.contarHabilitados() < minimo) {
            throw new Exception("El equipo " + idEquipo + " tiene " + equipo.contarHabilitados() + " jugadores habilitados y necesita " + minimo);
        }
        if (torneo.buscarInscripcion(equipo) != null) {
            throw new Exception("El equipo " + idEquipo + " ya esta inscrito en " + torneo.getNombre());
        }

        TipoCupo cupo = torneo.asignarCupo(equipoCalificaPreferencial(equipo, torneo));
        InscripcionTorneo inscripcion = new InscripcionTorneo(equipo, cupo, torneo.getTarifa());
        torneo.inscribir(inscripcion);
        return inscripcion;
    }

    public void retirarEquipoDeTorneo(String idTorneo, String idEquipo) throws Exception {
        Torneo torneo = buscarTorneo(idTorneo);
        Equipo equipo = buscarEquipoDeTorneo(idEquipo);
        torneo.verificarInscripcionesAbiertas();
        InscripcionTorneo inscripcion = torneo.buscarInscripcion(equipo);

        if (inscripcion == null) {
            throw new Exception("El equipo " + idEquipo + " no esta inscrito en " + torneo.getNombre());
        }

        torneo.retirar(inscripcion);
    }

    public InscripcionTorneo inscribirEnTorneo(String idTorneo, String idUsuario, boolean pagoConfirmado) throws Exception {
        Torneo torneo = buscarTorneo(idTorneo);
        List<Usuario> jugadores = new ArrayList<>();
        jugadores.add(buscarUsuarioDeTorneo(idUsuario));
        return inscribirJugadores(torneo, jugadores, Modalidad.SENCILLOS, pagoConfirmado, false);
    }

    public InscripcionTorneo inscribirEnCupoPreferencial(String idTorneo, String idUsuario, boolean pagoConfirmado) throws Exception {
        Torneo torneo = buscarTorneo(idTorneo);
        List<Usuario> jugadores = new ArrayList<>();
        jugadores.add(buscarUsuarioDeTorneo(idUsuario));
        return inscribirJugadores(torneo, jugadores, Modalidad.SENCILLOS, pagoConfirmado, true);
    }

    public InscripcionTorneo inscribirPareja(String idTorneo, String idUsuario, String companero, boolean pagoConfirmado) throws Exception {
        Torneo torneo = buscarTorneo(idTorneo);
        Usuario titular = buscarUsuarioDeTorneo(idUsuario);
        Usuario otro = buscarPorLoginODocumento(companero);

        if (otro.getId().equals(titular.getId())) {
            throw new Exception("La pareja necesita dos participantes distintos");
        }

        List<Usuario> jugadores = new ArrayList<>();
        jugadores.add(titular);
        jugadores.add(otro);
        return inscribirJugadores(torneo, jugadores, Modalidad.DOBLES, pagoConfirmado, false);
    }

    public void salirDeTorneo(String idTorneo, String idUsuario) throws Exception {
        Torneo torneo = buscarTorneo(idTorneo);
        Usuario usuario = buscarUsuarioDeTorneo(idUsuario);
        torneo.verificarInscripcionesAbiertas();
        InscripcionTorneo inscripcion = torneo.buscarInscripcion(usuario);

        if (inscripcion == null) {
            throw new Exception(usuario.getNombre() + " no esta inscrito en " + torneo.getNombre());
        }

        torneo.retirar(inscripcion);
    }

    private InscripcionTorneo inscribirJugadores(Torneo torneo, List<Usuario> jugadores, Modalidad modalidad, boolean pagoConfirmado, boolean exigirPreferencial) throws Exception {
        torneo.verificarInscripcionesAbiertas();

        boolean califica = false;
        int pagan = 0;

        for (Usuario jugador : jugadores) {
            if (jugador instanceof Administrador) {
                throw new Exception("El administrador no participa en torneos");
            }
            if (jugador instanceof Empleado) {
                if (!torneo.esIndividual()) {
                    throw new Exception("Los empleados solo participan en torneos individuales");
                }
                if (tieneTurno((Empleado) jugador, torneo.getFecha())) {
                    throw new Exception(jugador.getNombre() + " tiene turno el dia del torneo (" + nombreDia(torneo.getFecha()) + " " + torneo.getFecha() + ")");
                }
            } else {
                pagan++;
            }
        }

        if (!torneo.esIndividual()) {
            throw new Exception("En el torneo " + torneo.getNombre() + " se inscriben equipos completos");
        }
        if (torneo.getModalidad() != modalidad) {
            if (modalidad == Modalidad.DOBLES) {
                throw new Exception("El torneo " + torneo.getNombre() + " es de sencillos: no admite parejas");
            }
            throw new Exception("El torneo " + torneo.getNombre() + " es de dobles: debe inscribir una pareja");
        }

        for (Usuario jugador : jugadores) {
            if (torneo.buscarInscripcion(jugador) != null) {
                throw new Exception(jugador.getNombre() + " ya esta inscrito en " + torneo.getNombre());
            }

            NivelDeportivo nivel = nivelDe(jugador);

            if (nivel != null && !nivel.name().equals(torneo.getCategoria())) {
                throw new Exception("El nivel de " + jugador.getNombre() + " (" + nivel + ") no corresponde al del torneo (" + torneo.getCategoria() + ")");
            }
            if (jugador instanceof Socio && socioCalificaPreferencial((Socio) jugador, torneo)) {
                califica = true;
            }
        }

        TipoCupo cupo;

        if (exigirPreferencial) {
            if (!califica) {
                throw new Exception("Su ranking no esta entre los mejores de la categoria: no puede tomar un cupo preferencial");
            }
            if (!torneo.hayCupo(TipoCupo.PREFERENCIAL)) {
                throw new Exception("El torneo " + torneo.getNombre() + " no tiene cupos preferenciales libres");
            }

            cupo = TipoCupo.PREFERENCIAL;
        } else {
            cupo = torneo.asignarCupo(califica);
        }

        double tarifa = torneo.getTarifa() * pagan;

        if (tarifa > 0 && !pagoConfirmado) {
            throw new Exception("Pago de " + tarifa + " no confirmado: la inscripcion no se completo y no ocupa cupo");
        }

        InscripcionTorneo inscripcion = new InscripcionTorneo(jugadores, cupo, tarifa);
        torneo.inscribir(inscripcion);
        return inscripcion;
    }

    public boolean socioCalificaPreferencial(Socio socio, Torneo torneo) {
        int ranking = socio.getRanking(torneo.getDeporte());

        if (ranking <= 0) {
            return false;
        }

        int mejores = 0;

        for (Usuario usuario : usuarios) {
            if (usuario instanceof Socio) {
                Socio otro = (Socio) usuario;

                if (otro.getNivel() == socio.getNivel() && otro.getRanking(torneo.getDeporte()) > ranking) {
                    mejores++;
                }
            }
        }
        return mejores < torneo.getCuposPreferenciales();
    }

    public boolean equipoCalificaPreferencial(Equipo equipo, Torneo torneo) {
        if (equipo.getRanking() <= 0) {
            return false;
        }

        int mejores = 0;

        for (Equipo otro : equipos) {
            if (otro.isActivo() && otro.getDeporte().equals(equipo.getDeporte()) && otro.getCategoria().equals(equipo.getCategoria()) && otro.getRanking() > equipo.getRanking()) {
                mejores++;
            }
        }
        return mejores < torneo.getCuposPreferenciales();
    }

    public ParticipanteExterno registrarParticipanteExterno(String nombre, String documento, LocalDate fechaNacimiento, String login, String password, NivelDeportivo nivel, boolean estudiante) throws Exception {
        exigirDato(nombre, "nombre");
        exigirDato(documento, "documento");
        exigirDato(login, "login");
        exigirDato(password, "password");

        if (fechaNacimiento == null) {
            throw new Exception("Falta el dato: fecha de nacimiento");
        }
        if (nivel == null) {
            throw new Exception("Falta el dato: nivel");
        }
        if (fechaNacimiento.isAfter(LocalDate.now())) {
            throw new Exception("La fecha de nacimiento no puede ser futura");
        }

        for (Usuario usuario : usuarios) {
            if (usuario.getLogin().equalsIgnoreCase(login.trim())) {
                throw new Exception("El login " + login.trim() + " ya esta en uso");
            }
            if (usuario instanceof ParticipanteExterno && ((ParticipanteExterno) usuario).getDocumento().equals(documento.trim())) {
                throw new Exception("El documento " + documento.trim() + " ya esta registrado");
            }
        }

        consecutivoExternos++;
        ParticipanteExterno externo = new ParticipanteExterno("X" + consecutivoExternos, nombre.trim(), documento.trim(), fechaNacimiento, login.trim(), password, nivel, estudiante);
        usuarios.add(externo);
        return externo;
    }

    public void asignarTurno(String idEmpleado, Turno turno) throws Exception {
        Usuario usuario = buscarUsuarioDeTorneo(idEmpleado);

        if (!(usuario instanceof Empleado)) {
            throw new Exception("El usuario " + idEmpleado + " no es un empleado");
        }
        if (indiceDia(turno.getDiaSemana()) < 0) {
            throw new Exception("Dia de la semana no valido: " + turno.getDiaSemana());
        }
        if (!turno.esTurnoValido()) {
            throw new Exception("La hora de inicio del turno debe ser anterior a la de fin");
        }

        for (Torneo torneo : torneos) {
            boolean pendiente = torneo.getEstado() != EstadoTorneo.FINALIZADO;

            if (pendiente && torneo.buscarInscripcion(usuario) != null && indiceDia(turno.getDiaSemana()) == indiceDia(torneo.getFecha())) {
                throw new Exception(usuario.getNombre() + " esta inscrito en el torneo " + torneo.getNombre() + " del " + nombreDia(torneo.getFecha()) + " " + torneo.getFecha() + ": el turno no se asigna");
            }
        }

        ((Empleado) usuario).agregarTurno(turno);
    }

    public boolean tieneTurno(Empleado empleado, LocalDate fecha) {
        for (Turno turno : empleado.getTurnos()) {
            if (indiceDia(turno.getDiaSemana()) == indiceDia(fecha)) {
                return true;
            }
        }
        return false;
    }

    public List<BonoDescuento> consultarBonos(String idSocio) throws Exception {
        return buscarSocio(idSocio).getBonos();
    }

    private String siguienteIdTorneo() {
        consecutivoTorneos++;
        return "T" + consecutivoTorneos;
    }

    private void exigirDato(String valor, String nombreDato) throws Exception {
        if (valor == null || valor.trim().isEmpty()) {
            throw new Exception("Falta el dato: " + nombreDato);
        }
    }

    private int minimoJugadores(String deporte) {
        if (deporte.equals("Futbol")) {
            return 7;
        } else if (deporte.equals("Baloncesto")) {
            return 5;
        } else {
            return 6;
        }
    }

    private String buscarEnLista(String[] opciones, String texto) {
        for (String opcion : opciones) {
            if (sinTildes(opcion).equalsIgnoreCase(sinTildes(texto.trim()))) {
                return opcion;
            }
        }
        return null;
    }

    private String sinTildes(String texto) {
        return Normalizer.normalize(texto, Normalizer.Form.NFD).replaceAll("\\p{M}", "");
    }

    private int indiceDia(String dia) {
        for (int i = 0; i < DIAS_SEMANA.length; i++) {
            if (DIAS_SEMANA[i].equalsIgnoreCase(sinTildes(dia.trim()))) {
                return i;
            }
        }
        return -1;
    }

    private int indiceDia(LocalDate fecha) {
        return fecha.getDayOfWeek().getValue() - 1;
    }

    private String nombreDia(LocalDate fecha) {
        return DIAS_SEMANA[indiceDia(fecha)];
    }

    private NivelDeportivo nivelDe(Usuario usuario) {
        if (usuario instanceof Socio) {
            return ((Socio) usuario).getNivel();
        }
        if (usuario instanceof ParticipanteExterno) {
            return ((ParticipanteExterno) usuario).getNivel();
        }
        return null;
    }

    private Usuario buscarUsuarioDeTorneo(String id) throws Exception {
        for (Usuario usuario : usuarios) {
            if (usuario.getId().equals(id)) {
                return usuario;
            }
        }
        throw new Exception("No existe el usuario " + id);
    }

    private Usuario buscarPorLoginODocumento(String texto) throws Exception {
        if (texto != null) {
            String buscado = texto.trim();

            for (Usuario usuario : usuarios) {
                boolean porDocumento = usuario instanceof ParticipanteExterno && ((ParticipanteExterno) usuario).getDocumento().equals(buscado);

                if (usuario.getLogin().equals(buscado) || usuario.getId().equals(buscado) || porDocumento) {
                    return usuario;
                }
            }
        }
        throw new Exception("No existe un participante con login o documento " + texto);
    }

    private Equipo buscarEquipoDeTorneo(String id) throws Exception {
        for (Equipo equipo : equipos) {
            if (equipo.getId().equals(id)) {
                return equipo;
            }
        }
        throw new Exception("No existe el equipo " + id);
    }

    public List<Torneo> getTorneos() {
        return torneos;
    }

    public List<Instalacion> getInstalaciones() {
        return instalaciones;
    }

    public List<Partido> getPartidos() {
        return partidos;
    }

    public String getNombre() {
        return nombre;
    }

    public Usuario buscarUsuario(String id) throws Exception {
        for (Usuario usuario : usuarios) {
            if (usuario.getId().equals(id)) {
                return usuario;
            }
        }
        throw new Exception("no existe el usuario " + id);
    }

    public Socio buscarSocio(String id) throws Exception {
        Usuario usuario = buscarUsuario(id);
        if (usuario instanceof Socio) {
            return (Socio) usuario;
        }
        throw new Exception("el usuario " + id + " no es un socio");
    }

    public Instalacion buscarInstalacion(String id) throws Exception {
        for (Instalacion instalacion : instalaciones) {
            if (instalacion.getId().equals(id)) {
                return instalacion;
            }
        }
        throw new Exception("No existe la instalacion " + id);
    }

    public Producto buscarProducto(String id) throws Exception {
        for (Producto producto : productos) {
            if (producto.getId().equals(id)) {
                return producto;
            }
        }
        throw new Exception("No existe el producto " + id);
    }

    public Equipo buscarEquipo(String id) throws Exception {
        for (Equipo equipo : equipos) {
            if (equipo.getId().equals(id)) {
                return equipo;
            }
        }
        throw new Exception("No existe el equipo " + id);
    }

    public Competencia buscarCompetencia(String nombre) throws Exception {
        for (Competencia competencia : competencias) {
            if (competencia.getNombre().equals(nombre)) {
                return competencia;
            }
        }
        throw new Exception("No existe la competencia " + nombre);
    }

    public Partido buscarPartido(String id) throws Exception {
        for (Partido partido : partidos) {
            if (partido.getId().equals(id)) {
                return partido;
            }
        }
        throw new Exception("No existe el partido " + id);
    }

    public void agregarUsuario(Usuario usuario) {
        usuarios.add(usuario);
    }

    public void agregarProducto(Producto producto) {
        productos.add(producto);
    }

    public void agregarInstalacion(Instalacion instalacion) {
        instalaciones.add(instalacion);
    }

    public void agregarEquipo(Equipo equipo) {
        equipos.add(equipo);
    }

    public void agregarCompetencia(Competencia competencia) {
        competencias.add(competencia);
    }

    public void agregarPartido(Partido partido) {
        partidos.add(partido);
    }

    private boolean esBebidaCalienteProhibida(Producto producto, TipoUbicacion ubicacion) {
        if (!(producto instanceof Bebida)) {
            return false;
        }
        Bebida bebida = (Bebida) producto;
        if (!bebida.esBebidaCaliente()) {
            return false;
        }
        return ubicacion == TipoUbicacion.CANCHA_PADEL || ubicacion == TipoUbicacion.MESA_TECHADA;
    }

    private int valor(Map<String, Integer> estadisticas, String clave) {
        if (estadisticas.containsKey(clave)) {
            return estadisticas.get(clave);
        }
        return 0;
    }

    public List<Usuario> getUsuarios() {
        return usuarios;
    }

    public List<Producto> getProductos() {
        return productos;
    }

    public List<ReservaInstalacion> getReservas() {
        return reservas;
    }

    public List<Equipo> getEquipos() {
        return equipos;
    }

    public List<Venta> getVentas() {
        return ventas;
    }
}
