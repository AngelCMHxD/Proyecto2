package edu.seneca.modelo.torneos;

import edu.seneca.modelo.deportes.Equipo;
import edu.seneca.modelo.deportes.InscripcionEquipo;
import edu.seneca.modelo.usuarios.Empleado;
import edu.seneca.modelo.usuarios.Socio;
import edu.seneca.modelo.usuarios.Usuario;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class InscripcionTorneo implements Serializable {

    private static final long serialVersionUID = 1L;

    private List<Usuario> participantes;
    private Equipo equipo;
    private TipoCupo tipoCupo;
    private double tarifaPagada;

    public InscripcionTorneo(List<Usuario> participantes, TipoCupo tipoCupo, double tarifaPagada) {
        this.participantes = new ArrayList<>(participantes);
        this.tipoCupo = tipoCupo;
        this.tarifaPagada = tarifaPagada;
    }

    public InscripcionTorneo(Equipo equipo, TipoCupo tipoCupo, double tarifaPagada) {
        this.participantes = new ArrayList<>();
        this.equipo = equipo;
        this.tipoCupo = tipoCupo;
        this.tarifaPagada = tarifaPagada;
    }

    public boolean incluye(Usuario usuario) {
        for (Usuario participante : participantes) {
            if (participante.getId().equals(usuario.getId())) {
                return true;
            }
        }
        return false;
    }

    public boolean esDeEquipo(Equipo otro) {
        return equipo != null && equipo.getId().equals(otro.getId());
    }

    public String getIdPrincipal() {
        if (equipo != null) {
            return equipo.getId();
        }
        return participantes.get(0).getId();
    }

    public String getNombre() {
        if (equipo != null) {
            return "Equipo " + equipo.getId();
        }

        String nombre = "";

        for (Usuario participante : participantes) {
            if (!nombre.isEmpty()) {
                nombre += " / ";
            }

            nombre += participante.getNombre();
        }
        return nombre;
    }

    public boolean tieneDerechoAPremioEfectivo() {
        if (equipo != null) {
            return true;
        }

        for (Usuario participante : participantes) {
            if (!(participante instanceof Empleado)) {
                return true;
            }
        }
        return false;
    }

    public List<Socio> getSocios() {
        List<Socio> socios = new ArrayList<>();

        if (equipo != null) {
            for (InscripcionEquipo inscripcion : equipo.getInscripciones()) {
                socios.add(inscripcion.getSocio());
            }
        }

        for (Usuario participante : participantes) {
            if (participante instanceof Socio) {
                socios.add((Socio) participante);
            }
        }
        return socios;
    }

    public List<Usuario> getParticipantes() {
        return participantes;
    }

    public Equipo getEquipo() {
        return equipo;
    }

    public TipoCupo getTipoCupo() {
        return tipoCupo;
    }

    public double getTarifaPagada() {
        return tarifaPagada;
    }
}
