package com.upiiz.ligas_api.controller;

import com.upiiz.ligas_api.dto.competencia.CompetenciaRequest;
import com.upiiz.ligas_api.dto.competencia.CompetenciaResponse;
import com.upiiz.ligas_api.dto.competencia.CompetenciaResultRequest;
import com.upiiz.ligas_api.service.CompetenciaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controlador de Competencias (Partidos/Torneos).
 *
 * <p>Responsable de:
 * <ul>
 *   <li>Crear competencias (partidos) con fecha, liga (opcional) y equipos (local/visitante).</li>
 *   <li>Consultar competencias (lista o por id).</li>
 *   <li>Registrar/actualizar resultados de un partido.</li>
 *   <li>Eliminar competencias.</li>
 * </ul>
 *
 * <p><b>Seguridad:</b> Todos los endpoints requieren JWT:
 * <pre>{@code
 * Authorization: Bearer <TOKEN>
 * }</pre>
 *
 * <p><b>Reglas de negocio típicas (según tu servicio):</b>
 * <ul>
 *   <li>Local y visitante no pueden ser el mismo equipo.</li>
 *   <li>Si se especifica ligaId, los equipos deben pertenecer a esa liga (si están asignados).</li>
 *   <li>Al registrar resultado se marca como finalizado y se actualizan "partidosGanados" del equipo ganador.</li>
 *   <li>Empate: no suma ganados.</li>
 * </ul>
 *
 * <p><b>Errores comunes:</b>
 * <ul>
 *   <li><b>401</b>: token faltante/invalidado/expirado.</li>
 *   <li><b>403</b>: token válido pero sin permisos (si luego aplicas roles por endpoint).</li>
 *   <li><b>404</b>: competencia/equipo/liga no existe.</li>
 *   <li><b>400</b>: validación fallida o regla de negocio (ej. equipos iguales).</li>
 * </ul>
 */
@Tag(name = "Competencias", description = "CRUD de competencias (partidos) y registro de resultados")
@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("/api/competencias")
public class CompetenciaController {

    private final CompetenciaService svc;

    public CompetenciaController(CompetenciaService svc) {
        this.svc = svc;
    }

    /**
     * Obtiene todas las competencias registradas.
     *
     * <p><b>Ejemplo:</b>
     * <pre>{@code
     * GET /api/competencias
     * Authorization: Bearer <TOKEN>
     * }</pre>
     *
     * <p><b>Respuesta 200:</b> lista de competencias.
     */
    @Operation(
            summary = "Listar competencias",
            description = "Devuelve la lista completa de competencias. Requiere JWT."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "OK - lista de competencias",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = CompetenciaResponse.class))),
            @ApiResponse(responseCode = "401", description = "No autorizado - token faltante/ inválido/ expirado",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            examples = @ExampleObject(value = """
                            {
                              "timestamp": "2025-12-18T00:00:00Z",
                              "status": 401,
                              "error": "Unauthorized",
                              "message": "Full authentication is required to access this resource",
                              "path": "/api/competencias"
                            }
                            """))),
            @ApiResponse(responseCode = "500", description = "Error interno",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE))
    })
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<CompetenciaResponse>> getAll() {
        return ResponseEntity.ok(svc.findAll());
    }

    /**
     * Obtiene una competencia por su ID.
     *
     * <p><b>Ejemplo:</b>
     * <pre>{@code
     * GET /api/competencias/10
     * Authorization: Bearer <TOKEN>
     * }</pre>
     *
     * <p><b>Respuesta 200:</b> competencia encontrada.
     * <p><b>Respuesta 404:</b> si no existe.
     */
    @Operation(
            summary = "Buscar competencia por ID",
            description = "Devuelve una competencia específica por su ID. Requiere JWT."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "OK - competencia encontrada",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            examples = @ExampleObject(value = """
                            {
                              "id": 10,
                              "fecha": "2025-12-20T18:00:00",
                              "ligaId": 1,
                              "ligaNombre": "Liga UPIIZ",
                              "equipoLocalId": 2,
                              "equipoLocalNombre": "Tigres",
                              "equipoVisitanteId": 3,
                              "equipoVisitanteNombre": "Halcones",
                              "golesLocal": null,
                              "golesVisitante": null,
                              "finalizado": false
                            }
                            """))),
            @ApiResponse(responseCode = "401", description = "No autorizado",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE)),
            @ApiResponse(responseCode = "404", description = "No encontrado - competencia no existe",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            examples = @ExampleObject(value = """
                            {
                              "timestamp": "2025-12-18T00:00:00Z",
                              "status": 404,
                              "error": "Not Found",
                              "message": "Competencia no encontrada: 10",
                              "path": "/api/competencias/10"
                            }
                            """))),
            @ApiResponse(responseCode = "500", description = "Error interno",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE))
    })
    @GetMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<CompetenciaResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(svc.findById(id));
    }

    /**
     * Crea una competencia (partido) en una fecha determinada, con equipo local y visitante.
     *
     * <p><b>Header requerido:</b>
     * <pre>{@code
     * Authorization: Bearer <TOKEN>
     * }</pre>
     *
     * <p><b>Ejemplo de request:</b>
     * <pre>{@code
     * POST /api/competencias
     * Content-Type: application/json
     * Authorization: Bearer <TOKEN>
     *
     * {
     *   "fecha": "2025-12-20T18:00:00",
     *   "ligaId": 1,
     *   "equipoLocalId": 2,
     *   "equipoVisitanteId": 3
     * }
     * }</pre>
     *
     * <p><b>Errores típicos:</b>
     * <ul>
     *   <li>400 si localId == visitanteId</li>
     *   <li>404 si liga/equipo no existe</li>
     * </ul>
     */
    @Operation(
            summary = "Crear competencia",
            description = "Crea una competencia (partido). Requiere JWT. " +
                    "Valida que local y visitante sean diferentes y que existan equipos/liga."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Creada correctamente (se devuelve la competencia)",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            examples = @ExampleObject(value = """
                            {
                              "id": 11,
                              "fecha": "2025-12-20T18:00:00",
                              "ligaId": 1,
                              "ligaNombre": "Liga UPIIZ",
                              "equipoLocalId": 2,
                              "equipoLocalNombre": "Tigres",
                              "equipoVisitanteId": 3,
                              "equipoVisitanteNombre": "Halcones",
                              "golesLocal": null,
                              "golesVisitante": null,
                              "finalizado": false
                            }
                            """))),
            @ApiResponse(responseCode = "400", description = "Datos inválidos o regla de negocio",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            examples = @ExampleObject(value = """
                            {
                              "timestamp": "2025-12-18T00:00:00Z",
                              "status": 400,
                              "error": "Bad Request",
                              "message": "El equipo local y visitante no pueden ser el mismo",
                              "path": "/api/competencias"
                            }
                            """))),
            @ApiResponse(responseCode = "401", description = "No autorizado",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE)),
            @ApiResponse(responseCode = "404", description = "No encontrado (liga/equipo inexistente)",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE)),
            @ApiResponse(responseCode = "500", description = "Error interno",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE))
    })
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<CompetenciaResponse> create(
            @Valid
            @RequestBody(
                    description = "Datos para crear competencia",
                    required = true,
                    content = @Content(
                            schema = @Schema(implementation = CompetenciaRequest.class),
                            examples = @ExampleObject(name = "Crear partido", value = """
                            {
                              "fecha": "2025-12-20T18:00:00",
                              "ligaId": 1,
                              "equipoLocalId": 2,
                              "equipoVisitanteId": 3
                            }
                            """)
                    )
            )
            @org.springframework.web.bind.annotation.RequestBody CompetenciaRequest req
    ) {
        return ResponseEntity.ok(svc.create(req));
    }

    /**
     * Actualiza el resultado de una competencia y la marca como finalizada.
     *
     * <p>Este endpoint típicamente:
     * <ul>
     *   <li>Guarda golesLocal y golesVisitante</li>
     *   <li>Marca finalizado=true</li>
     *   <li>Si hay ganador, incrementa partidosGanados del equipo correspondiente</li>
     * </ul>
     *
     * <p><b>Ejemplo:</b>
     * <pre>{@code
     * PUT /api/competencias/11/resultado
     * Content-Type: application/json
     * Authorization: Bearer <TOKEN>
     *
     * {
     *   "golesLocal": 2,
     *   "golesVisitante": 1
     * }
     * }</pre>
     */
    @Operation(
            summary = "Registrar/actualizar resultado",
            description = "Guarda el marcador del partido y lo marca como finalizado. Requiere JWT."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Resultado actualizado",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            examples = @ExampleObject(value = """
                            {
                              "id": 11,
                              "fecha": "2025-12-20T18:00:00",
                              "ligaId": 1,
                              "ligaNombre": "Liga UPIIZ",
                              "equipoLocalId": 2,
                              "equipoLocalNombre": "Tigres",
                              "equipoVisitanteId": 3,
                              "equipoVisitanteNombre": "Halcones",
                              "golesLocal": 2,
                              "golesVisitante": 1,
                              "finalizado": true
                            }
                            """))),
            @ApiResponse(responseCode = "400", description = "Datos inválidos",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE)),
            @ApiResponse(responseCode = "401", description = "No autorizado",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE)),
            @ApiResponse(responseCode = "404", description = "Competencia no encontrada",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE)),
            @ApiResponse(responseCode = "500", description = "Error interno",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE))
    })
    @PutMapping(value = "/{id}/resultado", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<CompetenciaResponse> updateResultado(
            @PathVariable Long id,
            @Valid
            @RequestBody(
                    description = "Marcador final",
                    required = true,
                    content = @Content(
                            schema = @Schema(implementation = CompetenciaResultRequest.class),
                            examples = @ExampleObject(name = "Resultado ejemplo", value = """
                            {
                              "golesLocal": 2,
                              "golesVisitante": 1
                            }
                            """)
                    )
            )
            @org.springframework.web.bind.annotation.RequestBody CompetenciaResultRequest req
    ) {
        return ResponseEntity.ok(svc.updateResultado(id, req));
    }

    /**
     * Elimina una competencia por ID.
     *
     * <p><b>Ejemplo:</b>
     * <pre>{@code
     * DELETE /api/competencias/11
     * Authorization: Bearer <TOKEN>
     * }</pre>
     *
     * <p><b>Respuesta:</b> 204 No Content.
     */
    @Operation(
            summary = "Eliminar competencia",
            description = "Elimina una competencia por ID. Requiere JWT."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Eliminada correctamente"),
            @ApiResponse(responseCode = "401", description = "No autorizado",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE)),
            @ApiResponse(responseCode = "404", description = "Competencia no encontrada",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE)),
            @ApiResponse(responseCode = "500", description = "Error interno",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE))
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        svc.delete(id);
        return ResponseEntity.noContent().build();
    }
}
