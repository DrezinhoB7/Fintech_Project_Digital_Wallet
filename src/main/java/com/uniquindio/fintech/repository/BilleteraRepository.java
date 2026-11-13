package com.uniquindio.fintech.repository;

import com.uniquindio.fintech.model.Billetera;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BilleteraRepository extends JpaRepository<Billetera, String> {
    List<Billetera> findByUsuarioId(String usuarioId);
}