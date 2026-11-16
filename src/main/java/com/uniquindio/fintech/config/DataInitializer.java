package com.uniquindio.fintech.config;

import com.uniquindio.fintech.enums.*;
import com.uniquindio.fintech.model.*;
import com.uniquindio.fintech.repository.*;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Component
public class DataInitializer implements CommandLineRunner {

    private final UsuarioRepository usuarioRepository;
    private final BilleteraRepository billeteraRepository;
    private final TransaccionRepository transaccionRepository;
    private final BeneficioRepository beneficioRepository;
    private final UsuarioBeneficioRepository usuarioBeneficioRepository;
    private final OperacionProgramadaRepository operacionProgramadaRepository;
    private final NotificacionRepository notificacionRepository;
    private final RegistroAuditoriaRepository auditoriaRepository;
    private final PlataformaFintechContext context;

    public DataInitializer(UsuarioRepository usuarioRepository,
                           BilleteraRepository billeteraRepository,
                           TransaccionRepository transaccionRepository,
                           BeneficioRepository beneficioRepository,
                           UsuarioBeneficioRepository usuarioBeneficioRepository,
                           OperacionProgramadaRepository operacionProgramadaRepository,
                           NotificacionRepository notificacionRepository,
                           RegistroAuditoriaRepository auditoriaRepository,
                           PlataformaFintechContext context) {
        this.usuarioRepository = usuarioRepository;
        this.billeteraRepository = billeteraRepository;
        this.transaccionRepository = transaccionRepository;
        this.beneficioRepository = beneficioRepository;
        this.usuarioBeneficioRepository = usuarioBeneficioRepository;
        this.operacionProgramadaRepository = operacionProgramadaRepository;
        this.notificacionRepository = notificacionRepository;
        this.auditoriaRepository = auditoriaRepository;
        this.context = context;
    }

    @Override
    @Transactional
    public void run(String... args) {
        if (usuarioRepository.count() > 0) {
            return;
        }

        //Usuarios en los 3 niveles para el ArbolBST
        Usuario u1 = new Usuario("1094901001", "Robinson Ronaldo", "rr@uniquindio.edu.co", "3104567890");
        u1.acumularPuntos(2500); // Nivel ORO

        Usuario u2 = new Usuario("1094901002", "Pacho Gonzales", "p.gonzales@uniquindio.edu.co", "3119876543");
        u2.acumularPuntos(850);  // Nivel PLATA

        Usuario u3 = new Usuario("1094901003", "Andrés Gómez", "andres.gomez@uniquindio.edu.co", "3123456789");
        u3.acumularPuntos(120);  // Nivel BRONCE

        u1 = usuarioRepository.save(u1);
        u2 = usuarioRepository.save(u2);
        u3 = usuarioRepository.save(u3);

        //Billeteras categorizadas
        Billetera b1 = new Billetera("BIL-1001", "Cuenta Principal RR", TipoBilletera.GASTOS_DIARIOS, u1);
        b1.recargar(2500000);

        Billetera b2 = new Billetera("BIL-1002", "Cuenta Principal Pachito", TipoBilletera.GASTOS_DIARIOS, u2);
        b2.recargar(1400000);

        Billetera b3 = new Billetera("BIL-1003", "Cuenta Principal Andrés", TipoBilletera.AHORRO, u3);
        b3.recargar(650000);

        Billetera b4 = new Billetera("BIL-1004", "Fondo Inversiones RR", TipoBilletera.INVERSION, u1);
        b4.recargar(15000000);

        b1 = billeteraRepository.save(b1);
        b2 = billeteraRepository.save(b2);
        b3 = billeteraRepository.save(b3);
        b4 = billeteraRepository.save(b4);

        //Transacciones
        Transaccion txRecarga = crearTxCompletada(TipoTransaccion.RECARGA, 500000, null, b1, NivelRiesgo.BAJO);
        // BIL-1001 transfiere 2 veces a BIL-1002 (ruta frecuente)
        Transaccion tx1 = crearTxCompletada(TipoTransaccion.TRANSFERENCIA, 150000, b1, b2, NivelRiesgo.BAJO);
        Transaccion tx2 = crearTxCompletada(TipoTransaccion.TRANSFERENCIA, 80000, b1, b2, NivelRiesgo.BAJO);
        // Transferencia mayor a $1M -> Riesgo MEDIO
        Transaccion tx3 = crearTxCompletada(TipoTransaccion.TRANSFERENCIA, 1200000, b2, b3, NivelRiesgo.MEDIO);
        // Cierra el ciclo BIL-1001 -> BIL-1002 -> BIL-1003 -> BIL-1001 -> Escala a CRITICO
        Transaccion tx4 = crearTxCompletada(TipoTransaccion.TRANSFERENCIA, 250000, b3, b1, NivelRiesgo.CRITICO);

        transaccionRepository.save(txRecarga);
        transaccionRepository.save(tx1);
        transaccionRepository.save(tx2);
        transaccionRepository.save(tx3);
        tx4 = transaccionRepository.save(tx4);

        //Catálogo de Beneficios y un canje inicial
        Beneficio ben1 = beneficioRepository.save(
                new Beneficio("Bono Café del Quindío", "50% de descuento en tiendas de café aliadas", 50, NivelUsuario.BRONCE)
        );
        Beneficio ben2 = beneficioRepository.save(
                new Beneficio("Cashback 5% Compras", "Devolución del 5% en pagos con billetera digital", 300, NivelUsuario.PLATA)
        );
        Beneficio ben3 = beneficioRepository.save(
                new Beneficio("Acceso Sala VIP", "Pase de cortesía en salas VIP nacionales", 1500, NivelUsuario.ORO)
        );

        usuarioBeneficioRepository.save(new UsuarioBeneficio(u1, ben1, ben1.getCostoPuntos()));

        //Dos operaciones programadas con distinta prioridad temporal para la ColaPrioridad
        OperacionProgramada opProxima = new OperacionProgramada(
                LocalDateTime.now().plusHours(2),
                TipoTransaccion.TRANSFERENCIA,
                75000,
                FrecuenciaOperacion.UNICA,
                b1,
                b2
        );
        OperacionProgramada opPosterior = new OperacionProgramada(
                LocalDateTime.now().plusHours(12),
                TipoTransaccion.TRANSFERENCIA,
                200000,
                FrecuenciaOperacion.MENSUAL,
                b4,
                b3
        );
        operacionProgramadaRepository.save(opProxima);
        operacionProgramadaRepository.save(opPosterior);

        //Notificaciones pendientes en Cola FIFO
        notificacionRepository.save(new Notificacion(
                TipoAlerta.ASCENSO_NIVEL,
                "¡Bienvenido al nivel " + u1.getNivel() + "! Tienes beneficios exclusivos disponibles.",
                u1
        ));
        notificacionRepository.save(new Notificacion(
                TipoAlerta.OPERACION_PROGRAMADA,
                "Tienes una transferencia programada de $75.000 desde BIL-1001 hacia BIL-1002.",
                u1
        ));

        //Registro inicial en Bitácora de Auditoría (CRITICO por triangulación)
        auditoriaRepository.save(new RegistroAuditoria(
                "CICLO_TRANSFERENCIA_DETECTADO",
                "Triangulación circular detectada entre BIL-1001 -> BIL-1002 -> BIL-1003 -> BIL-1001.",
                NivelRiesgo.CRITICO,
                tx4,
                u3
        ));

        //Sincronizamos la base de datos (H2) con nuestras listas alojadas en la ram
        context.recargarDesdeBaseDeDatos();
        System.out.println(">>> QuindíoPay: Datos iniciales cargados y sincronizados");
    }

    private Transaccion crearTxCompletada(TipoTransaccion tipo, double valor, Billetera origen,
                                          Billetera destino, NivelRiesgo riesgo) {
        Transaccion tx = new Transaccion(tipo, valor, origen, destino);
        tx.setEstado(EstadoTransaccion.COMPLETADA);
        tx.setNivelRiesgo(riesgo);
        tx.setPuntosGenerados(tx.calcularPuntos());
        return tx;
    }
}