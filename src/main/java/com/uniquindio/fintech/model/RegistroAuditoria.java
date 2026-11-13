package com.uniquindio.fintech.model;

import com.uniquindio.fintech.enums.NivelRiesgo;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "auditoria_seguridad")
public class RegistroAuditoria {

    @Id
    @Column(length = 36)
    private String id;

    @Column(name = "fecha_deteccion", nullable = false)
    private LocalDateTime fechaDeteccion;

    @Column(name = "regla_activada", nullable = false, length = 80)
    private String reglaActivada;

    @Column(nullable = false, length = 500)
    private String descripcion;

    @Enumerated(EnumType.STRING)
    @Column(name = "nivel_riesgo", nullable = false, length = 20)
    private NivelRiesgo nivelRiesgo;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "transaccion_id", nullable = false)
    private Transaccion transaccion;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    public RegistroAuditoria() {
    }

    public RegistroAuditoria(String reglaActivada, String descripcion, NivelRiesgo nivelRiesgo,
                             Transaccion transaccion, Usuario usuario) {
        this.id = UUID.randomUUID().toString();
        this.fechaDeteccion = LocalDateTime.now();
        this.reglaActivada = reglaActivada;
        this.descripcion = descripcion;
        this.nivelRiesgo = nivelRiesgo;
        this.transaccion = transaccion;
        this.usuario = usuario;
    }

    @PrePersist
    public void prePersist() {
        if (this.id == null) {
            this.id = UUID.randomUUID().toString();
        }
        if (this.fechaDeteccion == null) {
            this.fechaDeteccion = LocalDateTime.now();
        }
    }

    // Getters y Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public LocalDateTime getFechaDeteccion() { return fechaDeteccion; }
    public void setFechaDeteccion(LocalDateTime fechaDeteccion) { this.fechaDeteccion = fechaDeteccion; }

    public String getReglaActivada() { return reglaActivada; }
    public void setReglaActivada(String reglaActivada) { this.reglaActivada = reglaActivada; }

    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }

    public NivelRiesgo getNivelRiesgo() { return nivelRiesgo; }
    public void setNivelRiesgo(NivelRiesgo nivelRiesgo) { this.nivelRiesgo = nivelRiesgo; }

    public Transaccion getTransaccion() { return transaccion; }
    public void setTransaccion(Transaccion transaccion) { this.transaccion = transaccion; }

    public Usuario getUsuario() { return usuario; }
    public void setUsuario(Usuario usuario) { this.usuario = usuario; }
}