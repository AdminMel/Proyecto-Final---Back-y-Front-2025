package com.upiiz.ligas_api.dto.entrenador;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record EntrenadorRequest(
        @NotBlank @Size(min = 2, max = 140) String nombre,
        @Email String email,
        @Size(max = 30) String telefono
) {}
