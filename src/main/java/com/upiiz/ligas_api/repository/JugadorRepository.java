package com.upiiz.ligas_api.repository;

import com.upiiz.ligas_api.entity.Jugador;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface JugadorRepository extends JpaRepository<Jugador, Long> {
    List<Jugador> findByEquipoId(Long equipoId);

    @Query("select e.id, e.nombre, count(j.id) from Equipo e left join Jugador j on j.equipo.id = e.id group by e.id, e.nombre")
    List<Object[]> conteoJugadoresPorEquipoRaw();
}
