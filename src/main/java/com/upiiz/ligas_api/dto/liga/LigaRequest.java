package com.upiiz.ligas_api.dto.liga;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record LigaRequest(
        @NotBlank @Size(min = 2, max = 140) String nombre,
        @Size(max = 400) String descripcion
) {}
