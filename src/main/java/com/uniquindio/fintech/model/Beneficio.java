package com.uniquindio.fintech.model;

import com.uniquindio.fintech.enums.NivelUsuario;
import jakarta.persistence.*;
import java.util.Objects;
import java.util.UUID;

@Entity
@Table(name = "beneficios")
public class Beneficio {

    @Id
    @Column(length = 36)
    private String id;

    @Column(nullable = false, length = 100)
    private String nombre;

    @Column(nullable = false, length = 500)
    private String descripcion;

    @Column(name = "costo_puntos", nullable = false)
    private int costoPuntos;

    @Enumerated(EnumType.STRING)
    @Column(name = "nivel_requerido", nullable = false, length = 20)
    private NivelUsuario nivelRequerido;

    public Beneficio() {
    }

    public Beneficio(String nombre, String descripcion, int costoPuntos, NivelUsuario nivelRequerido) {
        this.id = UUID.randomUUID().toString();
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.costoPuntos = costoPuntos;
        this.nivelRequerido = nivelRequerido;
    }

    @PrePersist
    public void prePersist() {
        if (this.id == null) {
            this.id = UUID.randomUUID().toString();
        }
    }

    public boolean esAplicable(Usuario u) {
        if (u == null || !u.isActivo()) {
            return false;
        }
        boolean cumpleNivel = u.getNivel().ordinal() >= this.nivelRequerido.ordinal();
        boolean cumplePuntos = u.getPuntosAcumulados() >= this.costoPuntos;
        return cumpleNivel && cumplePuntos;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Beneficio beneficio)) return false;
        return Objects.equals(id, beneficio.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    // Getters y Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }

    public int getCostoPuntos() { return costoPuntos; }
    public void setCostoPuntos(int costoPuntos) { this.costoPuntos = costoPuntos; }

    public NivelUsuario getNivelRequerido() { return nivelRequerido; }
    public void setNivelRequerido(NivelUsuario nivelRequerido) { this.nivelRequerido = nivelRequerido; }
}