package com.upiiz.ligas_api.dto.equipo;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record EquipoRequest(
        @NotBlank @Size(min = 2, max = 140) String nombre,
        Long ligaId,
        Long entrenadorId
) {}
