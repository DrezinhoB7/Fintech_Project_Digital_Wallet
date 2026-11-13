package com.uniquindio.fintech.repository;

import com.uniquindio.fintech.model.RegistroAuditoria;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RegistroAuditoriaRepository extends JpaRepository<RegistroAuditoria, String> {
    List<RegistroAuditoria> findAllByOrderByFechaDeteccionDesc();
}