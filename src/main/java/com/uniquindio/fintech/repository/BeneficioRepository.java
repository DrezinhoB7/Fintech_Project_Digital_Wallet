package com.uniquindio.fintech.repository;

import com.uniquindio.fintech.model.Beneficio;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BeneficioRepository extends JpaRepository<Beneficio, String> {
    List<Beneficio> findAllByOrderByCostoPuntosAsc();
}