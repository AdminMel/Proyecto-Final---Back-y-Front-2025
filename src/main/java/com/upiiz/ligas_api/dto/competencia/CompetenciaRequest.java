package com.upiiz.ligas_api.dto.competencia;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

public record CompetenciaRequest(
        @NotNull LocalDateTime fecha,
        Long ligaId,
        @NotNull Long equipoLocalId,
        @NotNull Long equipoVisitanteId
) {}
