package com.upiiz.ligas_api.repository;

import com.upiiz.ligas_api.entity.Equipo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface EquipoRepository extends JpaRepository<Equipo, Long> {

    // ====== FIX Lazy / no session (para /api/equipos) ======

    @Query("""
           SELECT e
           FROM Equipo e
           LEFT JOIN FETCH e.liga l
           LEFT JOIN FETCH e.entrenador t
           """)
    List<Equipo> findAllWithRefs();

    @Query("""
           SELECT e
           FROM Equipo e
           LEFT JOIN FETCH e.liga l
           LEFT JOIN FETCH e.entrenador t
           WHERE e.id = :id
           """)
    Optional<Equipo> findByIdWithRefs(@Param("id") Long id);

    @Query("""
           SELECT e
           FROM Equipo e
           LEFT JOIN FETCH e.liga l
           LEFT JOIN FETCH e.entrenador t
           WHERE l.id = :ligaId
           """)
    List<Equipo> findByLigaIdWithRefs(@Param("ligaId") Long ligaId);


    // ====== STATS (para que compile StatsService) ======
    // Estos regresan "raw" (arreglos) para que StatsService los transforme.

    // Top equipos por partidos ganados
    @Query("""
           SELECT e.nombre, e.partidosGanados
           FROM Equipo e
           ORDER BY e.partidosGanados DESC
           """)
    List<Object[]> topGanadosRaw();

    // Conteo de equipos por liga
    @Query("""
           SELECT l.nombre, COUNT(e.id)
           FROM Equipo e
           JOIN e.liga l
           GROUP BY l.nombre
           ORDER BY COUNT(e.id) DESC
           """)
    List<Object[]> equiposPorLigaRaw();
}
