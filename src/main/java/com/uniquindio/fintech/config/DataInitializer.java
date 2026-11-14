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

        /*
        * Registro automático de usuarios al iniciar la aplicación
        * Registra usuarios con diferentes puntajes para que se vea el uso del ArbolBST
        * */
        Usuario u1 = new Usuario("1094901001", "Robinson Ronadl", "rr@uniquindio.edu.co", "3104567890");
        u1.acumularPuntos(2500);

        Usuario u2 = new Usuario("1094901002", "Pacho Gonzales", "p.gonzales@uniquindio.edu.co", "3119876543");
        u2.acumularPuntos(850);

        Usuario u3 = new Usuario("1094901003", "Andrés Gómez", "andres.gomez@uniquindio.edu.co", "3123456789");
        u3.acumularPuntos(120);

        u1 = usuarioRepository.save(u1);
        u2 = usuarioRepository.save(u2);
        u3 = usuarioRepository.save(u3);

        /*
        * Registro de billeteras
        * */
        TipoBilletera tipoPrincipal = TipoBilletera.values()[0];
        TipoBilletera tipoSecundaria = TipoBilletera.values().length > 1
                ? TipoBilletera.values()[1]
                : tipoPrincipal;

        Billetera b1 = new Billetera("BIL-1001", "Cuenta Principal RR", tipoPrincipal, u1);
        b1.recargar(1200000);

        Billetera b2 = new Billetera("BIL-1002", "Cuenta Principal Pachito", tipoPrincipal, u2);
        b2.recargar(650000);

        Billetera b3 = new Billetera("BIL-1003", "Cuenta Principal Andrés", tipoPrincipal, u3);
        b3.recargar(300000);

        Billetera b4 = new Billetera("BIL-1004", "Ahorros Mateo", tipoSecundaria, u1);
        b4.recargar(450000);

        b1 = billeteraRepository.save(b1);
        b2 = billeteraRepository.save(b2);
        b3 = billeteraRepository.save(b3);
        b4 = billeteraRepository.save(b4);

        /* Transacciones para evidenciar el uso de grafos (movimientos sospechosos)*/
        Transaccion txRecarga = crearTxCompletada(TipoTransaccion.RECARGA, 500000, null, b1, NivelRiesgo.BAJO);
        /* BIL-1001 transfiere 2 veces a BIL-1002 */
        Transaccion tx1 = crearTxCompletada(TipoTransaccion.TRANSFERENCIA, 150000, b1, b2, NivelRiesgo.BAJO);
        Transaccion tx2 = crearTxCompletada(TipoTransaccion.TRANSFERENCIA, 80000, b1, b2, NivelRiesgo.BAJO);
        /*Cerramos el ciclo transfiriendo de BIL-1002 -> BIL-1003 -> BIL-1001*/
        Transaccion tx3 = crearTxCompletada(TipoTransaccion.TRANSFERENCIA, 120000, b2, b3, NivelRiesgo.MEDIO);
        Transaccion tx4 = crearTxCompletada(TipoTransaccion.TRANSFERENCIA, 95000, b3, b1, NivelRiesgo.ALTO);

        transaccionRepository.save(txRecarga);
        transaccionRepository.save(tx1);
        transaccionRepository.save(tx2);
        transaccionRepository.save(tx3);
        tx4 = transaccionRepository.save(tx4);

        /*Catálogo de Beneficios y un canje inicial*/
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

        /* Operación programada para 6 horas despues de haberse ejecutado el programa por primera vez */
        FrecuenciaOperacion frecuenciaDefault = FrecuenciaOperacion.values()[0];
        OperacionProgramada opProgramada = new OperacionProgramada(
                LocalDateTime.now().plusHours(6),
                TipoTransaccion.TRANSFERENCIA,
                75000,
                frecuenciaDefault,
                b1,
                b2
        );
        operacionProgramadaRepository.save(opProgramada);

        /* Notificaciones pendientes */
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

        /*Registro de Auditoría de Riesgo*/
        auditoriaRepository.save(new RegistroAuditoria(
                "CICLO_TRANSFERENCIA_DETECTADO",
                "Triangulación circular detectada entre BIL-1001 -> BIL-1002 -> BIL-1003 -> BIL-1001.",
                NivelRiesgo.ALTO,
                tx4,
                u3
        ));

        //Sincronizamos los datos insertados con nuestras estructuras propias (RAM)
        context.recargarDesdeBaseDeDatos();
        //Comprobación en consola
        System.out.println("Se cargó con éxito");
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