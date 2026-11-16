package edu.seneca.modelo.torneos;

import edu.seneca.modelo.deportes.Equipo;
import edu.seneca.modelo.usuarios.Usuario;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public abstract class Torneo implements Serializable {

    private static final long serialVersionUID = 1L;

    public static final int MINIMO_PARTICIPANTES = 2;

    protected String id;
    protected String nombre;
    protected String deporte;
    protected String categoria;
    protected Modalidad modalidad;
    protected int cupos;
    protected int cuposPreferenciales;
    protected LocalDate fecha;
    protected EstadoTorneo estado;
    protected List<InscripcionTorneo> inscripciones;
    protected List<Encuentro> encuentros;
    protected List<InscripcionTorneo> posicionesFinales;

    protected Torneo(String id, String nombre, String deporte, String categoria, Modalidad modalidad, int cupos, LocalDate fecha) {
        this.id = id;
        this.nombre = nombre;
        this.deporte = deporte;
        this.categoria = categoria;
        this.modalidad = modalidad;
        this.cupos = cupos;
        this.cuposPreferenciales = calcularCuposPreferenciales(cupos);
        this.fecha = fecha;
        this.estado = EstadoTorneo.INSCRIPCIONES_ABIERTAS;
        this.inscripciones = new ArrayList<>();
        this.encuentros = new ArrayList<>();
        this.posicionesFinales = new ArrayList<>();
    }

    public static int calcularCuposPreferenciales(int cupos) {
        return (cupos + 4) / 5;
    }

    public abstract TipoTorneo getTipo();

    public abstract double getTarifa();

    protected abstract void entregarPremio();

    public abstract String describirPremio();

    public int getCuposRegulares() {
        return cupos - cuposPreferenciales;
    }

    public int contarOcupados(TipoCupo tipo) {
        int ocupados = 0;

        for (InscripcionTorneo inscripcion : inscripciones) {
            if (inscripcion.getTipoCupo() == tipo) {
                ocupados++;
            }
        }
        return ocupados;
    }

    public boolean hayCupo(TipoCupo tipo) {
        if (tipo == TipoCupo.PREFERENCIAL) {
            return contarOcupados(tipo) < cuposPreferenciales;
        }
        return contarOcupados(tipo) < getCuposRegulares();
    }

    public TipoCupo asignarCupo(boolean calificaPreferencial) throws Exception {
        if (calificaPreferencial && hayCupo(TipoCupo.PREFERENCIAL)) {
            return TipoCupo.PREFERENCIAL;
        }
        if (hayCupo(TipoCupo.REGULAR)) {
            return TipoCupo.REGULAR;
        }
        throw new Exception("El torneo " + nombre + " esta lleno: no quedan cupos regulares");
    }

    public void verificarInscripcionesAbiertas() throws Exception {
        if (estado != EstadoTorneo.INSCRIPCIONES_ABIERTAS) {
            throw new Exception("El torneo " + nombre + " ya inicio o finalizo (estado: " + estado.getTexto() + ")");
        }
    }

    public void inscribir(InscripcionTorneo inscripcion) throws Exception {
        verificarInscripcionesAbiertas();

        if (!hayCupo(inscripcion.getTipoCupo())) {
            throw new Exception("El torneo " + nombre + " no tiene cupos de tipo " + inscripcion.getTipoCupo());
        }

        inscripciones.add(inscripcion);
    }

    public void retirar(InscripcionTorneo inscripcion) throws Exception {
        verificarInscripcionesAbiertas();
        inscripciones.remove(inscripcion);
    }

    public InscripcionTorneo buscarInscripcion(Usuario usuario) {
        for (InscripcionTorneo inscripcion : inscripciones) {
            if (inscripcion.incluye(usuario)) {
                return inscripcion;
            }
        }
        return null;
    }

    public InscripcionTorneo buscarInscripcion(Equipo equipo) {
        for (InscripcionTorneo inscripcion : inscripciones) {
            if (inscripcion.esDeEquipo(equipo)) {
                return inscripcion;
            }
        }
        return null;
    }

    public InscripcionTorneo buscarInscripcionPorId(String idParticipante) throws Exception {
        for (InscripcionTorneo inscripcion : inscripciones) {
            if (inscripcion.getEquipo() != null && inscripcion.getEquipo().getId().equals(idParticipante)) {
                return inscripcion;
            }

            for (Usuario usuario : inscripcion.getParticipantes()) {
                if (usuario.getId().equals(idParticipante)) {
                    return inscripcion;
                }
            }
        }
        throw new Exception(idParticipante + " no esta inscrito en el torneo " + nombre);
    }

    public void iniciar() throws Exception {
        verificarInscripcionesAbiertas();

        if (inscripciones.size() < MINIMO_PARTICIPANTES) {
            int faltan = MINIMO_PARTICIPANTES - inscripciones.size();
            throw new Exception("No se puede iniciar " + nombre + ": faltan " + faltan + " participante(s)");
        }

        cuposPreferenciales = contarOcupados(TipoCupo.PREFERENCIAL);
        estado = EstadoTorneo.EN_CURSO;
    }

    public Encuentro programarEncuentro(InscripcionTorneo a, InscripcionTorneo b) throws Exception {
        if (estado != EstadoTorneo.EN_CURSO) {
            throw new Exception("Solo se programan encuentros en torneos en curso");
        }
        if (a == b) {
            throw new Exception("Un encuentro necesita dos participantes distintos");
        }

        Encuentro encuentro = new Encuentro(a, b);
        encuentros.add(encuentro);
        return encuentro;
    }

    public void registrarResultado(int numero, String marcador, InscripcionTorneo ganador) throws Exception {
        if (estado != EstadoTorneo.EN_CURSO) {
            throw new Exception("Solo se registran resultados en torneos en curso");
        }
        if (numero < 1 || numero > encuentros.size()) {
            throw new Exception("No existe el encuentro " + numero + " en el torneo " + nombre);
        }

        encuentros.get(numero - 1).registrarResultado(marcador, ganador);
    }

    public void finalizar(InscripcionTorneo ganador) throws Exception {
        if (estado != EstadoTorneo.EN_CURSO) {
            throw new Exception("Solo se puede finalizar un torneo en curso (estado: " + estado.getTexto() + ")");
        }
        if (encuentros.isEmpty()) {
            throw new Exception("El torneo " + nombre + " no tiene encuentros registrados");
        }

        for (Encuentro encuentro : encuentros) {
            if (!encuentro.tieneResultado()) {
                throw new Exception("El torneo " + nombre + " tiene encuentros sin resultado");
            }
        }

        if (!inscripciones.contains(ganador)) {
            throw new Exception("El ganador no esta inscrito en el torneo " + nombre);
        }

        posicionesFinales = ordenarPosiciones(ganador);
        estado = EstadoTorneo.FINALIZADO;
        entregarPremio();
    }

    public int contarVictorias(InscripcionTorneo inscripcion) {
        int victorias = 0;

        for (Encuentro encuentro : encuentros) {
            if (encuentro.getGanador() == inscripcion) {
                victorias++;
            }
        }
        return victorias;
    }

    public List<InscripcionTorneo> getPosiciones() {
        if (estado == EstadoTorneo.FINALIZADO) {
            return posicionesFinales;
        }
        return ordenarPosiciones(null);
    }

    private List<InscripcionTorneo> ordenarPosiciones(InscripcionTorneo ganador) {
        List<InscripcionTorneo> tabla = new ArrayList<>();

        for (InscripcionTorneo inscripcion : inscripciones) {
            int posicion = 0;

            while (posicion < tabla.size() && contarVictorias(tabla.get(posicion)) >= contarVictorias(inscripcion)) {
                posicion++;
            }

            tabla.add(posicion, inscripcion);
        }

        if (ganador != null) {
            tabla.remove(ganador);
            tabla.add(0, ganador);
        }
        return tabla;
    }

    public double getTotalRecaudado() {
        double total = 0;

        for (InscripcionTorneo inscripcion : inscripciones) {
            total += inscripcion.getTarifaPagada();
        }
        return total;
    }

    public InscripcionTorneo getGanador() {
        if (posicionesFinales.isEmpty()) {
            return null;
        }
        return posicionesFinales.get(0);
    }

    public boolean esIndividual() {
        return modalidad != Modalidad.CONJUNTO;
    }

    public String getId() {
        return id;
    }

    public String getNombre() {
        return nombre;
    }

    public String getDeporte() {
        return deporte;
    }

    public String getCategoria() {
        return categoria;
    }

    public Modalidad getModalidad() {
        return modalidad;
    }

    public int getCupos() {
        return cupos;
    }

    public int getCuposPreferenciales() {
        return cuposPreferenciales;
    }

    public LocalDate getFecha() {
        return fecha;
    }

    public EstadoTorneo getEstado() {
        return estado;
    }

    public List<InscripcionTorneo> getInscripciones() {
        return inscripciones;
    }

    public List<Encuentro> getEncuentros() {
        return encuentros;
    }
}
