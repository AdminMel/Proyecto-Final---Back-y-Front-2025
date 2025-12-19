package com.upiiz.ligas_api.service;

import com.upiiz.ligas_api.dto.stats.*;
import com.upiiz.ligas_api.repository.EquipoRepository;
import com.upiiz.ligas_api.repository.JugadorRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class StatsService {

    private final EquipoRepository equipoRepo;
    private final JugadorRepository jugadorRepo;

    public StatsService(EquipoRepository equipoRepo, JugadorRepository jugadorRepo) {
        this.equipoRepo = equipoRepo;
        this.jugadorRepo = jugadorRepo;
    }

    // 1) Equipos con más partidos ganados
    public List<EquipoGanadosDto> topGanados() {
        return equipoRepo.topGanadosRaw().stream()
                .map(r -> new EquipoGanadosDto(
                        ((Number) r[0]).longValue(),
                        (String) r[1],
                        ((Number) r[2]).intValue()
                ))
                .toList();
    }

    // 2) Número de equipos por ligas
    public List<EquiposPorLigaDto> equiposPorLiga() {
        return equipoRepo.equiposPorLigaRaw().stream()
                .map(r -> new EquiposPorLigaDto(
                        ((Number) r[0]).longValue(),
                        (String) r[1],
                        ((Number) r[2]).longValue()
                ))
                .toList();
    }

    // 3) Promedio de jugadores por equipo
    public PromedioJugadoresEquipoDto promedioJugadoresPorEquipo() {
        List<Object[]> rows = jugadorRepo.conteoJugadoresPorEquipoRaw();
        if (rows.isEmpty()) return new PromedioJugadoresEquipoDto(0.0);

        double sum = 0;
        for (Object[] r : rows) {
            sum += ((Number) r[2]).doubleValue();
        }
        double avg = sum / rows.size();
        return new PromedioJugadoresEquipoDto(avg);
    }
}
