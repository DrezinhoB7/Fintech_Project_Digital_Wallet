package com.uniquindio.fintech.model;

import com.uniquindio.fintech.enums.EstadoBilletera;
import com.uniquindio.fintech.enums.TipoBilletera;
import jakarta.persistence.*;
import java.util.Objects;

@Entity
@Table(name = "billeteras")
public class Billetera {

    @Id
    @Column(length = 30)
    private String codigo;

    @Column(nullable = false, length = 80)
    private String nombre;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private TipoBilletera tipo;

    @Column(name = "saldo_disponible", nullable = false)
    private double saldoDisponible;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EstadoBilletera estado;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    public Billetera() {
    }

    public Billetera(String codigo, String nombre, TipoBilletera tipo, Usuario usuario) {
        this.codigo = codigo;
        this.nombre = nombre;
        this.tipo = tipo;
        this.saldoDisponible = 0.0;
        this.estado = EstadoBilletera.ACTIVA;
        this.usuario = usuario;
    }

    public void recargar(double monto) {
        if (monto <= 0) {
            throw new IllegalArgumentException("El monto a recargar debe ser mayor a cero.");
        }
        if (this.estado != EstadoBilletera.ACTIVA) {
            throw new IllegalStateException("La billetera no se encuentra activa.");
        }
        this.saldoDisponible += monto;
    }

    public boolean retirar(double monto) {
        if (monto <= 0 || this.estado != EstadoBilletera.ACTIVA || this.saldoDisponible < monto) {
            return false;
        }
        this.saldoDisponible -= monto;
        return true;
    }

    public double consultarSaldo() {
        return this.saldoDisponible;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Billetera billetera)) return false;
        return Objects.equals(codigo, billetera.codigo);
    }

    @Override
    public int hashCode() {
        return Objects.hash(codigo);
    }

    // Getters y Setters
    public String getCodigo() { return codigo; }
    public void setCodigo(String codigo) { this.codigo = codigo; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public TipoBilletera getTipo() { return tipo; }
    public void setTipo(TipoBilletera tipo) { this.tipo = tipo; }

    public double getSaldoDisponible() { return saldoDisponible; }
    public void setSaldoDisponible(double saldoDisponible) { this.saldoDisponible = saldoDisponible; }

    public EstadoBilletera getEstado() { return estado; }
    public void setEstado(EstadoBilletera estado) { this.estado = estado; }

    public Usuario getUsuario() { return usuario; }
    public void setUsuario(Usuario usuario) { this.usuario = usuario; }
}