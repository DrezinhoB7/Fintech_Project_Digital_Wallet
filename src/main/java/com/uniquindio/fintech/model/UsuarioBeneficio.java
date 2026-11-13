package com.uniquindio.fintech.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "usuario_beneficios")
public class UsuarioBeneficio {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "beneficio_id", nullable = false)
    private Beneficio beneficio;

    @Column(name = "fecha_canje", nullable = false)
    private LocalDateTime fechaCanje;

    @Column(name = "puntos_descontados", nullable = false)
    private int puntosDescontados;

    public UsuarioBeneficio() {
    }

    public UsuarioBeneficio(Usuario usuario, Beneficio beneficio, int puntosDescontados) {
        this.usuario = usuario;
        this.beneficio = beneficio;
        this.fechaCanje = LocalDateTime.now();
        this.puntosDescontados = puntosDescontados;
    }

    // Getters y Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Usuario getUsuario() { return usuario; }
    public void setUsuario(Usuario usuario) { this.usuario = usuario; }

    public Beneficio getBeneficio() { return beneficio; }
    public void setBeneficio(Beneficio beneficio) { this.beneficio = beneficio; }

    public LocalDateTime getFechaCanje() { return fechaCanje; }
    public void setFechaCanje(LocalDateTime fechaCanje) { this.fechaCanje = fechaCanje; }

    public int getPuntosDescontados() { return puntosDescontados; }
    public void setPuntosDescontados(int puntosDescontados) { this.puntosDescontados = puntosDescontados; }
}