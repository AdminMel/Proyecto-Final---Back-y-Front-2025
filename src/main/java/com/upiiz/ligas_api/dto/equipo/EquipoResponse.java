package com.upiiz.ligas_api.dto.equipo;

public record EquipoResponse(
        Long id,
        String nombre,
        Long ligaId,
        String ligaNombre,
        Long entrenadorId,
        String entrenadorNombre,
        Integer partidosGanados
) {}
