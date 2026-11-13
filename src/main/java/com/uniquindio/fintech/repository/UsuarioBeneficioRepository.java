package com.uniquindio.fintech.repository;

import com.uniquindio.fintech.model.UsuarioBeneficio;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UsuarioBeneficioRepository extends JpaRepository<UsuarioBeneficio, Long> {
    List<UsuarioBeneficio> findByUsuarioIdOrderByFechaCanjeDesc(String usuarioId);
}