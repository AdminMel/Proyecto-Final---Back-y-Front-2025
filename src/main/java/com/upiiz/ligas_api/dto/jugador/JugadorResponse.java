package com.upiiz.ligas_api.dto.jugador;

public record JugadorResponse(
        Long id,
        String nombre,
        Integer edad,
        String posicion,
        Long equipoId,
        String equipoNombre
) {}
