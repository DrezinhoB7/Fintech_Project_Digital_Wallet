package com.uniquindio.fintech.model;

import com.uniquindio.fintech.enums.FrecuenciaOperacion;
import com.uniquindio.fintech.enums.TipoTransaccion;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

@Entity
@Table(name = "operaciones_programadas")
public class OperacionProgramada implements Comparable<OperacionProgramada> {

    @Id
    @Column(length = 36)
    private String id;

    @Column(name = "fecha_ejecucion", nullable = false)
    private LocalDateTime fechaEjecucion;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_operacion", nullable = false, length = 30)
    private TipoTransaccion tipoOperacion;

    @Column(nullable = false)
    private double valor;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private FrecuenciaOperacion frecuencia;

    @Column(nullable = false)
    private boolean activa = true;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "billetera_origen_codigo", nullable = false)
    private Billetera billeteraOrigen;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "billetera_destino_codigo")
    private Billetera billeteraDestino;

    public OperacionProgramada() {
    }

    public OperacionProgramada(LocalDateTime fechaEjecucion, TipoTransaccion tipoOperacion, double valor,
                               FrecuenciaOperacion frecuencia, Billetera billeteraOrigen, Billetera billeteraDestino) {
        this.id = UUID.randomUUID().toString();
        this.fechaEjecucion = fechaEjecucion;
        this.tipoOperacion = tipoOperacion;
        this.valor = valor;
        this.frecuencia = frecuencia;
        this.activa = true;
        this.billeteraOrigen = billeteraOrigen;
        this.billeteraDestino = billeteraDestino;
    }

    @PrePersist
    public void prePersist() {
        if (this.id == null) {
            this.id = UUID.randomUUID().toString();
        }
    }

    public int compararPrioridad(OperacionProgramada otra) {
        return this.fechaEjecucion.compareTo(otra.getFechaEjecucion());
    }

    @Override
    public int compareTo(OperacionProgramada otra) {
        return compararPrioridad(otra);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof OperacionProgramada that)) return false;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    // Getters y Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public LocalDateTime getFechaEjecucion() { return fechaEjecucion; }
    public void setFechaEjecucion(LocalDateTime fechaEjecucion) { this.fechaEjecucion = fechaEjecucion; }

    public TipoTransaccion getTipoOperacion() { return tipoOperacion; }
    public void setTipoOperacion(TipoTransaccion tipoOperacion) { this.tipoOperacion = tipoOperacion; }

    public double getValor() { return valor; }
    public void setValor(double valor) { this.valor = valor; }

    public FrecuenciaOperacion getFrecuencia() { return frecuencia; }
    public void setFrecuencia(FrecuenciaOperacion frecuencia) { this.frecuencia = frecuencia; }

    public boolean isActiva() { return activa; }
    public void setActiva(boolean activa) { this.activa = activa; }

    public Billetera getBilleteraOrigen() { return billeteraOrigen; }
    public void setBilleteraOrigen(Billetera billeteraOrigen) { this.billeteraOrigen = billeteraOrigen; }

    public Billetera getBilleteraDestino() { return billeteraDestino; }
    public void setBilleteraDestino(Billetera billeteraDestino) { this.billeteraDestino = billeteraDestino; }
}