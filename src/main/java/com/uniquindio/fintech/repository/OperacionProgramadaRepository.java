package com.uniquindio.fintech.repository;

import com.uniquindio.fintech.model.OperacionProgramada;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OperacionProgramadaRepository extends JpaRepository<OperacionProgramada, String> {
    List<OperacionProgramada> findByActivaTrueOrderByFechaEjecucionAsc();
}