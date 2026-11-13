package com.uniquindio.fintech.repository;

import com.uniquindio.fintech.model.Transaccion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface TransaccionRepository extends JpaRepository<Transaccion, String> {
    List<Transaccion> findAllByOrderByFechaAsc();
    List<Transaccion> findByFechaBetweenOrderByFechaDesc(LocalDateTime inicio, LocalDateTime fin);
}