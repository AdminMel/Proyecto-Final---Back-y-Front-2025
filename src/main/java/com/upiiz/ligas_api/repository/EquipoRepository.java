package com.upiiz.ligas_api.repository;

import com.upiiz.ligas_api.entity.Equipo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface EquipoRepository extends JpaRepository<Equipo, Long> {

    // GET /api/equipos
    @Query("""
           SELECT e
           FROM Equipo e
           LEFT JOIN FETCH e.liga l
           LEFT JOIN FETCH e.entrenador t
           """)
    List<Equipo> findAllWithRefs();

    // GET /api/equipos/{id}
    @Query("""
           SELECT e
           FROM Equipo e
           LEFT JOIN FETCH e.liga l
           LEFT JOIN FETCH e.entrenador t
           WHERE e.id = :id
           """)
    Optional<Equipo> findByIdWithRefs(@Param("id") Long id);

    // GET /api/equipos/liga/{ligaId}
    @Query("""
           SELECT e
           FROM Equipo e
           LEFT JOIN FETCH e.liga l
           LEFT JOIN FETCH e.entrenador t
           WHERE l.id = :ligaId
           """)
    List<Equipo> findByLigaIdWithRefs(@Param("ligaId") Long ligaId);
}
