package com.uniquindio.fintech.model;

import com.uniquindio.fintech.enums.EstadoTransaccion;
import com.uniquindio.fintech.enums.NivelRiesgo;
import com.uniquindio.fintech.enums.TipoTransaccion;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

@Entity
@Table(name = "transacciones")
public class Transaccion {

    @Id
    @Column(length = 36)
    private String id;

    @Column(nullable = false)
    private LocalDateTime fecha;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_transaccion", nullable = false, length = 30)
    private TipoTransaccion tipoTransaccion;

    @Column(nullable = false)
    private double valor;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EstadoTransaccion estado;

    @Column(name = "puntos_generados", nullable = false)
    private int puntosGenerados;

    @Enumerated(EnumType.STRING)
    @Column(name = "nivel_riesgo", nullable = false, length = 20)
    private NivelRiesgo nivelRiesgo;

    @Column(nullable = false)
    private boolean reversible;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "billetera_origen_codigo")
    private Billetera billeteraOrigen;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "billetera_destino_codigo")
    private Billetera billeteraDestino;

    public Transaccion() {
    }

    public Transaccion(TipoTransaccion tipoTransaccion, double valor, Billetera billeteraOrigen, Billetera billeteraDestino) {
        this.id = UUID.randomUUID().toString();
        this.fecha = LocalDateTime.now();
        this.tipoTransaccion = tipoTransaccion;
        this.valor = valor;
        this.estado = EstadoTransaccion.PENDIENTE;
        this.nivelRiesgo = NivelRiesgo.BAJO;
        this.billeteraOrigen = billeteraOrigen;
        this.billeteraDestino = billeteraDestino;
        this.reversible = (tipoTransaccion == TipoTransaccion.TRANSFERENCIA || tipoTransaccion == TipoTransaccion.RECARGA);
        this.puntosGenerados = 0;
    }

    @PrePersist
    public void prePersist() {
        if (this.id == null) {
            this.id = UUID.randomUUID().toString();
        }
        if (this.fecha == null) {
            this.fecha = LocalDateTime.now();
        }
        if (this.nivelRiesgo == null) {
            this.nivelRiesgo = NivelRiesgo.BAJO;
        }
    }

    public int calcularPuntos() {
        int bloquesDeCien = (int) (this.valor / 100.0);
        return switch (this.tipoTransaccion) {
            case RECARGA -> bloquesDeCien;
            case RETIRO -> bloquesDeCien * 2;
            case TRANSFERENCIA, PAGO_PROGRAMADO -> bloquesDeCien * 3;
            case REVERSION -> 0;
        };
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Transaccion that)) return false;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    // Getters y Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public LocalDateTime getFecha() { return fecha; }
    public void setFecha(LocalDateTime fecha) { this.fecha = fecha; }

    public TipoTransaccion getTipoTransaccion() { return tipoTransaccion; }
    public void setTipoTransaccion(TipoTransaccion tipoTransaccion) { this.tipoTransaccion = tipoTransaccion; }

    public double getValor() { return valor; }
    public void setValor(double valor) { this.valor = valor; }

    public EstadoTransaccion getEstado() { return estado; }
    public void setEstado(EstadoTransaccion estado) { this.estado = estado; }

    public int getPuntosGenerados() { return puntosGenerados; }
    public void setPuntosGenerados(int puntosGenerados) { this.puntosGenerados = puntosGenerados; }

    public NivelRiesgo getNivelRiesgo() { return nivelRiesgo; }
    public void setNivelRiesgo(NivelRiesgo nivelRiesgo) { this.nivelRiesgo = nivelRiesgo; }

    public boolean isReversible() { return reversible; }
    public void setReversible(boolean reversible) { this.reversible = reversible; }

    public Billetera getBilleteraOrigen() { return billeteraOrigen; }
    public void setBilleteraOrigen(Billetera billeteraOrigen) { this.billeteraOrigen = billeteraOrigen; }

    public Billetera getBilleteraDestino() { return billeteraDestino; }
    public void setBilleteraDestino(Billetera billeteraDestino) { this.billeteraDestino = billeteraDestino; }
}