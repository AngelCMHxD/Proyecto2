package edu.seneca.modelo.usuarios;

import java.time.LocalDate;

public class ParticipanteExterno extends Usuario {

    private static final long serialVersionUID = 1L;

    private String documento;
    private LocalDate fechaNacimiento;
    private NivelDeportivo nivel;
    private boolean estudiante;

    public ParticipanteExterno(String id, String nombre, String documento, LocalDate fechaNacimiento, String login, String password, NivelDeportivo nivel, boolean estudiante) {
        super(id, nombre, login, password);
        this.documento = documento;
        this.fechaNacimiento = fechaNacimiento;
        this.nivel = nivel;
        this.estudiante = estudiante;
    }

    public String getDocumento() {
        return documento;
    }

    public LocalDate getFechaNacimiento() {
        return fechaNacimiento;
    }

    public NivelDeportivo getNivel() {
        return nivel;
    }

    public boolean isEstudiante() {
        return estudiante;
    }
}
