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

        // Usuarios en los 4 niveles para el ArbolBST
        Usuario u1 = new Usuario("1094901001", "Robinson Ronaldo", "rr@uniquindio.edu.co", "3104567890");
        u1.acumularPuntos(2500); // Nivel ORO

        Usuario u2 = new Usuario("1094901002", "Pacho Gonzales", "p.gonzales@uniquindio.edu.co", "3119876543");
        u2.acumularPuntos(850);  // Nivel PLATA

        Usuario u3 = new Usuario("1094901003", "Andrés Gómez", "andres.gomez@uniquindio.edu.co", "3123456789");
        u3.acumularPuntos(120);  // Nivel BRONCE

        Usuario u4 = new Usuario("1094901004", "Sofía Martínez", "s.martinez@uniquindio.edu.co", "3156789012");
        u4.acumularPuntos(5001); // Nivel PLATINO

        u1 = usuarioRepository.save(u1);
        u2 = usuarioRepository.save(u2);
        u3 = usuarioRepository.save(u3);
        u4 = usuarioRepository.save(u4);

        // Billeteras categorizadas
        Billetera b1 = new Billetera("BIL-1001", "Cuenta Principal RR", TipoBilletera.GASTOS_DIARIOS, u1);
        b1.recargar(2500000);

        Billetera b2 = new Billetera("BIL-1002", "Cuenta Principal Pachito", TipoBilletera.GASTOS_DIARIOS, u2);
        b2.recargar(1400000);

        Billetera b3 = new Billetera("BIL-1003", "Cuenta Principal Andrés", TipoBilletera.AHORRO, u3);
        b3.recargar(650000);

        Billetera b4 = new Billetera("BIL-1004", "Fondo Inversiones RR", TipoBilletera.INVERSION, u1);
        b4.recargar(15000000);

        Billetera b5 = new Billetera("BIL-1005", "Bóveda Platino Sofía", TipoBilletera.INVERSION, u4);
        b5.recargar(25000000);

        b1 = billeteraRepository.save(b1);
        b2 = billeteraRepository.save(b2);
        b3 = billeteraRepository.save(b3);
        b4 = billeteraRepository.save(b4);
        b5 = billeteraRepository.save(b5);

        // Transacciones
        Transaccion txRecarga = crearTxCompletada(TipoTransaccion.RECARGA, 500000, null, b1, NivelRiesgo.BAJO);
        // BIL-1001 transfiere 2 veces a BIL-1002 (ruta frecuente)
        Transaccion tx1 = crearTxCompletada(TipoTransaccion.TRANSFERENCIA, 150000, b1, b2, NivelRiesgo.BAJO);
        Transaccion tx2 = crearTxCompletada(TipoTransaccion.TRANSFERENCIA, 80000, b1, b2, NivelRiesgo.BAJO);
        // Transferencia mayor a $1M -> Riesgo MEDIO
        Transaccion tx3 = crearTxCompletada(TipoTransaccion.TRANSFERENCIA, 1200000, b2, b3, NivelRiesgo.MEDIO);
        // Cierra el ciclo BIL-1001 -> BIL-1002 -> BIL-1003 -> BIL-1001 -> Escala a CRITICO
        Transaccion tx4 = crearTxCompletada(TipoTransaccion.TRANSFERENCIA, 250000, b3, b1, NivelRiesgo.CRITICO);

        // Movimientos de la cuenta Platino (Sofía - BIL-1005)
        Transaccion txRecargaPlatino = crearTxCompletada(TipoTransaccion.RECARGA, 5000000, null, b5, NivelRiesgo.ALTO);
        Transaccion txPlatino1 = crearTxCompletada(TipoTransaccion.TRANSFERENCIA, 3500000, b5, b1, NivelRiesgo.ALTO);
        Transaccion txPlatino2 = crearTxCompletada(TipoTransaccion.TRANSFERENCIA, 1800000, b4, b5, NivelRiesgo.MEDIO);

        transaccionRepository.save(txRecarga);
        transaccionRepository.save(tx1);
        transaccionRepository.save(tx2);
        transaccionRepository.save(tx3);
        tx4 = transaccionRepository.save(tx4);
        transaccionRepository.save(txRecargaPlatino);
        transaccionRepository.save(txPlatino1);
        transaccionRepository.save(txPlatino2);

        // Catálogo de Beneficios y canjes iniciales
        Beneficio ben1 = beneficioRepository.save(
                new Beneficio("Bono Café del Quindío", "50% de descuento en tiendas de café aliadas", 50, NivelUsuario.BRONCE)
        );
        Beneficio ben2 = beneficioRepository.save(
                new Beneficio("Cashback 5% Compras", "Devolución del 5% en pagos con billetera digital", 300, NivelUsuario.PLATA)
        );
        Beneficio ben3 = beneficioRepository.save(
                new Beneficio("Acceso Sala VIP", "Pase de cortesía en salas VIP nacionales", 1500, NivelUsuario.ORO)
        );
        Beneficio ben4 = beneficioRepository.save(
                new Beneficio("Bono Tiquetes Internacionales", "Bono de $500.000 COP en aerolíneas aliadas", 4000, NivelUsuario.PLATINO)
        );

        usuarioBeneficioRepository.save(new UsuarioBeneficio(u1, ben1, ben1.getCostoPuntos()));
        usuarioBeneficioRepository.save(new UsuarioBeneficio(u4, ben3, ben3.getCostoPuntos()));

        // Operaciones programadas con distinta prioridad temporal para la ColaPrioridad
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
        OperacionProgramada opPlatino = new OperacionProgramada(
                LocalDateTime.now().plusHours(6),
                TipoTransaccion.TRANSFERENCIA,
                950000,
                FrecuenciaOperacion.MENSUAL,
                b5,
                b2
        );
        operacionProgramadaRepository.save(opProxima);
        operacionProgramadaRepository.save(opPosterior);
        operacionProgramadaRepository.save(opPlatino);

        // Notificaciones pendientes en Cola FIFO
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
        notificacionRepository.save(new Notificacion(
                TipoAlerta.ASCENSO_NIVEL,
                "¡Felicitaciones Sofía! Alcanzaste el nivel máximo " + u4.getNivel() + " con +5.000 puntos.",
                u4
        ));

        // Registro inicial en Bitácora de Auditoría (CRITICO por triangulación)
        auditoriaRepository.save(new RegistroAuditoria(
                "CICLO_TRANSFERENCIA_DETECTADO",
                "Triangulación circular detectada entre BIL-1001 -> BIL-1002 -> BIL-1003 -> BIL-1001.",
                NivelRiesgo.CRITICO,
                tx4,
                u3
        ));

        // Sincronizamos la base de datos (H2) con nuestras estructuras alojadas en la RAM
        context.recargarDesdeBaseDeDatos();
        System.out.println(">>> FinTech: Datos iniciales cargados y sincronizados");
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