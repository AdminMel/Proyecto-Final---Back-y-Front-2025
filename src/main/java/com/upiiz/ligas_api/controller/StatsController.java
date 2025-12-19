package com.upiiz.ligas_api.controller;

import com.upiiz.ligas_api.dto.stats.EquipoGanadosDto;
import com.upiiz.ligas_api.dto.stats.EquiposPorLigaDto;
import com.upiiz.ligas_api.dto.stats.PromedioJugadoresEquipoDto;
import com.upiiz.ligas_api.service.StatsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controlador de Estadísticas (para gráficas en el Frontend).
 *
 * <p>Este controlador expone endpoints agregados para construir visualizaciones en Angular:
 * <ul>
 *   <li><b>Equipos con más partidos ganados</b></li>
 *   <li><b>Número de equipos por liga</b></li>
 *   <li><b>Promedio de jugadores por equipo</b></li>
 * </ul>
 *
 * <p><b>Seguridad:</b> Todos los endpoints requieren JWT:
 * <pre>{@code
 * Authorization: Bearer <TOKEN>
 * }</pre>
 *
 * <p><b>Notas:</b>
 * <ul>
 *   <li>Estas métricas suelen provenir de consultas agregadas (COUNT, GROUP BY, ORDER BY).</li>
 *   <li>Si no hay datos, se devuelven listas vacías o promedio = 0.0 (según tu service).</li>
 * </ul>
 */
@Tag(name = "Stats", description = "Endpoints de estadísticas para gráficas (requiere JWT)")
@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("/api/stats")
public class StatsController {

    private final StatsService svc;

    public StatsController(StatsService svc) {
        this.svc = svc;
    }

    /**
     * Devuelve el ranking de equipos con más partidos ganados.
     *
     * <p><b>Ejemplo:</b>
     * <pre>{@code
     * GET /api/stats/top-ganados
     * Authorization: Bearer <TOKEN>
     * }</pre>
     *
     * <p><b>Respuesta 200 (ejemplo):</b>
     * <pre>{@code
     * [
     *   { "equipoId": 1, "equipoNombre": "Tigres", "partidosGanados": 8 },
     *   { "equipoId": 2, "equipoNombre": "Halcones", "partidosGanados": 5 }
     * ]
     * }</pre>
     */
    @Operation(
            summary = "Top equipos con más partidos ganados",
            description = "Devuelve un ranking de equipos ordenado por partidosGanados (desc). Requiere JWT."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "OK - ranking calculado",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            examples = @ExampleObject(value = """
                            [
                              { "equipoId": 1, "equipoNombre": "Tigres", "partidosGanados": 8 },
                              { "equipoId": 2, "equipoNombre": "Halcones", "partidosGanados": 5 },
                              { "equipoId": 3, "equipoNombre": "Leones", "partidosGanados": 3 }
                            ]
                            """))),
            @ApiResponse(responseCode = "401", description = "No autorizado - token faltante/ inválido/ expirado",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            examples = @ExampleObject(value = """
                            {
                              "timestamp": "2025-12-18T00:00:00Z",
                              "status": 401,
                              "error": "Unauthorized",
                              "message": "Full authentication is required to access this resource",
                              "path": "/api/stats/top-ganados"
                            }
                            """))),
            @ApiResponse(responseCode = "500", description = "Error interno",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE))
    })
    @GetMapping(value = "/top-ganados", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<EquipoGanadosDto>> topGanados() {
        return ResponseEntity.ok(svc.topGanados());
    }

    /**
     * Devuelve cuántos equipos hay por liga.
     *
     * <p><b>Ejemplo:</b>
     * <pre>{@code
     * GET /api/stats/equipos-por-liga
     * Authorization: Bearer <TOKEN>
     * }</pre>
     *
     * <p><b>Respuesta 200 (ejemplo):</b>
     * <pre>{@code
     * [
     *   { "ligaId": 10, "ligaNombre": "Liga UPIIZ", "totalEquipos": 6 },
     *   { "ligaId": 12, "ligaNombre": "Liga Zacatecas", "totalEquipos": 4 }
     * ]
     * }</pre>
     *
     * <p><b>Nota:</b> Si una liga no tiene equipos, puede aparecer con total 0 (si tu query usa LEFT JOIN).
     */
    @Operation(
            summary = "Número de equipos por liga",
            description = "Devuelve una lista con el total de equipos agrupado por liga. Requiere JWT."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "OK - conteo por liga",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            examples = @ExampleObject(value = """
                            [
                              { "ligaId": 10, "ligaNombre": "Liga UPIIZ", "totalEquipos": 6 },
                              { "ligaId": 12, "ligaNombre": "Liga Zacatecas", "totalEquipos": 4 },
                              { "ligaId": 15, "ligaNombre": "Liga Sin Equipos", "totalEquipos": 0 }
                            ]
                            """))),
            @ApiResponse(responseCode = "401", description = "No autorizado",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE)),
            @ApiResponse(responseCode = "500", description = "Error interno",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE))
    })
    @GetMapping(value = "/equipos-por-liga", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<EquiposPorLigaDto>> equiposPorLiga() {
        return ResponseEntity.ok(svc.equiposPorLiga());
    }

    /**
     * Devuelve el promedio de jugadores por equipo.
     *
     * <p>Se calcula a partir del conteo de jugadores por equipo y luego el promedio.
     *
     * <p><b>Ejemplo:</b>
     * <pre>{@code
     * GET /api/stats/promedio-jugadores-por-equipo
     * Authorization: Bearer <TOKEN>
     * }</pre>
     *
     * <p><b>Respuesta 200 (ejemplo):</b>
     * <pre>{@code
     * { "promedio": 12.5 }
     * }</pre>
     *
     * <p><b>Nota:</b> Si no hay equipos o jugadores, puede devolver promedio = 0.0.
     */
    @Operation(
            summary = "Promedio de jugadores por equipo",
            description = "Devuelve el promedio de jugadores por equipo (valor decimal). Requiere JWT."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "OK - promedio calculado",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            examples = @ExampleObject(value = """
                            { "promedio": 12.5 }
                            """))),
            @ApiResponse(responseCode = "401", description = "No autorizado",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE)),
            @ApiResponse(responseCode = "500", description = "Error interno",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE))
    })
    @GetMapping(value = "/promedio-jugadores-por-equipo", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<PromedioJugadoresEquipoDto> promedio() {
        return ResponseEntity.ok(svc.promedioJugadoresPorEquipo());
    }
}
