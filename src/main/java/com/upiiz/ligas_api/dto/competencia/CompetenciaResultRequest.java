package com.upiiz.ligas_api.dto.competencia;

import jakarta.validation.constraints.NotNull;

public record CompetenciaResultRequest(
        @NotNull Integer golesLocal,
        @NotNull Integer golesVisitante
) {}
