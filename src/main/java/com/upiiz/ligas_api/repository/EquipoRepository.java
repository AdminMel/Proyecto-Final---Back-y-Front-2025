package com.upiiz.ligas_api.repository;

import com.upiiz.ligas_api.entity.Equipo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface EquipoRepository extends JpaRepository<Equipo, Long> {

    List<Equipo> findByLigaId(Long ligaId);

    // Top por ganados
    @Query("select e.id, e.nombre, e.partidosGanados from Equipo e order by e.partidosGanados desc")
    List<Object[]> topGanadosRaw();

    // Equipos por liga
    @Query("select l.id, l.nombre, count(e.id) from Liga l left join Equipo e on e.liga.id = l.id group by l.id, l.nombre")
    List<Object[]> equiposPorLigaRaw();
}
