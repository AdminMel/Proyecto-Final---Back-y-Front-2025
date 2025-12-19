package com.upiiz.ligas_api.dto.jugador;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record JugadorRequest(
        @NotBlank @Size(min = 2, max = 140) String nombre,
        @NotNull @Min(1) Integer edad,
        @Size(max = 80) String posicion,
        Long equipoId
) {}
