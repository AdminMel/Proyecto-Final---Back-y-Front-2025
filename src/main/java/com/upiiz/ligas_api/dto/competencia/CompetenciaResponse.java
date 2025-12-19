package com.upiiz.ligas_api.dto.competencia;

import java.time.LocalDateTime;

public record CompetenciaResponse(
        Long id,
        LocalDateTime fecha,
        Long ligaId,
        String ligaNombre,
        Long equipoLocalId,
        String equipoLocalNombre,
        Long equipoVisitanteId,
        String equipoVisitanteNombre,
        Integer golesLocal,
        Integer golesVisitante,
        Boolean finalizado
) {}
