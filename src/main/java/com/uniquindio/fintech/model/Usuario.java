package com.uniquindio.fintech.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.uniquindio.fintech.enums.NivelUsuario;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Entity
@Table(name = "usuarios")
public class Usuario implements Comparable<Usuario> {

    @Id
    @Column(length = 36)
    private String id;

    @Column(nullable = false, unique = true, length = 20)
    private String identificacion;

    @Column(nullable = false, length = 100)
    private String nombre;

    @Column(nullable = false, unique = true, length = 120)
    private String email;

    @Column(length = 20)
    private String telefono;

    @Column(name = "puntos_acumulados", nullable = false)
    private int puntosAcumulados;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private NivelUsuario nivel;

    @Column(nullable = false)
    private boolean activo = true;

    @Column(name = "fecha_registro", nullable = false)
    private LocalDateTime fechaRegistro;

    @OneToMany(mappedBy = "usuario", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnore
    private List<Billetera> billeteras = new ArrayList<>();

    public Usuario() {
    }

    public Usuario(String identificacion, String nombre, String email, String telefono) {
        this.id = UUID.randomUUID().toString();
        this.identificacion = identificacion;
        this.nombre = nombre;
        this.email = email;
        this.telefono = telefono;
        this.puntosAcumulados = 0;
        this.nivel = NivelUsuario.BRONCE;
        this.activo = true;
        this.fechaRegistro = LocalDateTime.now();
    }

    @PrePersist
    public void prePersist() {
        if (this.id == null) {
            this.id = UUID.randomUUID().toString();
        }
        if (this.nivel == null) {
            this.nivel = NivelUsuario.obtenerPorPuntos(this.puntosAcumulados);
        }
        if (this.fechaRegistro == null) {
            this.fechaRegistro = LocalDateTime.now();
        }
    }

    public boolean acumularPuntos(int pts) {
        if (pts > 0) {
            this.puntosAcumulados += pts;
        }
        return actualizarNivel();
    }

    public void descontarPuntos(int pts) {
        if (pts > 0) {
            this.puntosAcumulados = Math.max(0, this.puntosAcumulados - pts);
            actualizarNivel();
        }
    }

    public boolean actualizarNivel() {
        NivelUsuario nivelAnterior = this.nivel;
        NivelUsuario nuevoNivel = NivelUsuario.obtenerPorPuntos(this.puntosAcumulados);
        this.nivel = nuevoNivel;
        return nivelAnterior != null && nuevoNivel.ordinal() > nivelAnterior.ordinal();
    }

    public boolean canjearBeneficio(Beneficio beneficio) {
        if (beneficio == null || !beneficio.esAplicable(this)) {
            return false;
        }
        descontarPuntos(beneficio.getCostoPuntos());
        return true;
    }

    @Override
    public int compareTo(Usuario otro) {
        int cmp = Integer.compare(this.puntosAcumulados, otro.puntosAcumulados);
        if (cmp != 0) {
            return cmp;
        }
        String idActual = this.identificacion != null ? this.identificacion : "";
        String idOtro = otro.identificacion != null ? otro.identificacion : "";
        return idActual.compareTo(idOtro);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Usuario usuario)) return false;
        return Objects.equals(id, usuario.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    // Getters y Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getIdentificacion() { return identificacion; }
    public void setIdentificacion(String identificacion) { this.identificacion = identificacion; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getTelefono() { return telefono; }
    public void setTelefono(String telefono) { this.telefono = telefono; }

    public int getPuntosAcumulados() { return puntosAcumulados; }
    public void setPuntosAcumulados(int puntosAcumulados) {
        this.puntosAcumulados = Math.max(0, puntosAcumulados);
        actualizarNivel();
    }

    public NivelUsuario getNivel() { return nivel; }
    public void setNivel(NivelUsuario nivel) { this.nivel = nivel; }

    public boolean isActivo() { return activo; }
    public void setActivo(boolean activo) { this.activo = activo; }

    public LocalDateTime getFechaRegistro() { return fechaRegistro; }
    public void setFechaRegistro(LocalDateTime fechaRegistro) { this.fechaRegistro = fechaRegistro; }

    public List<Billetera> getBilleteras() { return billeteras; }
    public void setBilleteras(List<Billetera> billeteras) { this.billeteras = billeteras; }
    public double calcularSaldoTotal(){
        double d = 0.0f;
        for(Billetera b : billeteras){
            d+=b.getSaldoDisponible();
        }
        return d;
    }
}