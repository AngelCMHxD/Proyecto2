package edu.seneca.modelo.torneos;

import java.io.Serializable;

public class BonoDescuento implements Serializable {

    private static final long serialVersionUID = 1L;

    private String id;
    private double valor;
    private String torneoOrigen;
    private boolean usado;

    public BonoDescuento(String id, double valor, String torneoOrigen) {
        this.id = id;
        this.valor = valor;
        this.torneoOrigen = torneoOrigen;
        this.usado = false;
    }

    public void marcarUsado() {
        usado = true;
    }

    public String getId() {
        return id;
    }

    public double getValor() {
        return valor;
    }

    public String getTorneoOrigen() {
        return torneoOrigen;
    }

    public boolean isUsado() {
        return usado;
    }

    public String getEstado() {
        if (usado) {
            return "usado";
        }
        return "disponible";
    }
}
