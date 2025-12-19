package com.upiiz.ligas_api.controller;

import com.upiiz.ligas_api.dto.equipo.EquipoRequest;
import com.upiiz.ligas_api.dto.equipo.EquipoResponse;
import com.upiiz.ligas_api.service.EquipoService;
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
 * Controlador de Equipos.
 *
 * <p>Permite administrar equipos mediante CRUD y consultas por liga:
 * <ul>
 *   <li>Listar equipos</li>
 *   <li>Consultar equipo por ID</li>
 *   <li>Listar equipos por liga</li>
 *   <li>Crear equipo (opcionalmente asignando liga y entrenador)</li>
 *   <li>Actualizar equipo (cambiando liga/entrenador)</li>
 *   <li>Eliminar equipo</li>
 * </ul>
 *
 * <p><b>Seguridad:</b> Todos los endpoints requieren JWT:
 * <pre>{@code
 * Authorization: Bearer <TOKEN>
 * }</pre>
 *
 * <p><b>Asignación de relaciones (muy importante):</b>
 * <ul>
 *   <li><code>ligaId</code> en el request asigna el equipo a una liga existente.</li>
 *   <li><code>entrenadorId</code> en el request asigna el equipo a un entrenador existente.</li>
 *   <li>Si envías <code>null</code> en ligaId o entrenadorId en update, se desvincula (según tu service).</li>
 * </ul>
 *
 * <p><b>Errores comunes:</b>
 * <ul>
 *   <li><b>401</b>: token faltante/ inválido/ expirado.</li>
 *   <li><b>404</b>: equipo no existe, o ligaId/entrenadorId no existen al asignar.</li>
 *   <li><b>400</b>: validación fallida (nombre vacío, etc.).</li>
 * </ul>
 */
@Tag(name = "Equipos", description = "CRUD de equipos y consulta por liga (requiere JWT)")
@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("/api/equipos")
public class EquipoController {

    private final EquipoService svc;

    public EquipoController(EquipoService svc) {
        this.svc = svc;
    }

    /**
     * Lista todos los equipos.
     *
     * <p><b>Ejemplo:</b>
     * <pre>{@code
     * GET /api/equipos
     * Authorization: Bearer <TOKEN>
     * }</pre>
     */
    @Operation(
            summary = "Listar equipos",
            description = "Devuelve la lista completa de equipos. Requiere JWT."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "OK - lista de equipos",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            examples = @ExampleObject(value = """
                            [
                              {
                                "id": 1,
                                "nombre": "Tigres",
                                "ligaId": 10,
                                "ligaNombre": "Liga UPIIZ",
                                "entrenadorId": 5,
                                "entrenadorNombre": "Tony",
                                "partidosGanados": 3
                              },
                              {
                                "id": 2,
                                "nombre": "Halcones",
                                "ligaId": 10,
                                "ligaNombre": "Liga UPIIZ",
                                "entrenadorId": null,
                                "entrenadorNombre": null,
                                "partidosGanados": 1
                              }
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
                              "path": "/api/equipos"
                            }
                            """))),
            @ApiResponse(responseCode = "500", description = "Error interno",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE))
    })
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<EquipoResponse>> getAll() {
        return ResponseEntity.ok(svc.findAll());
    }

    /**
     * Consulta un equipo por su ID.
     *
     * <p><b>Ejemplo:</b>
     * <pre>{@code
     * GET /api/equipos/1
     * Authorization: Bearer <TOKEN>
     * }</pre>
     */
    @Operation(
            summary = "Buscar equipo por ID",
            description = "Devuelve un equipo por su ID. Requiere JWT."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "OK - equipo encontrado",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            examples = @ExampleObject(value = """
                            {
                              "id": 1,
                              "nombre": "Tigres",
                              "ligaId": 10,
                              "ligaNombre": "Liga UPIIZ",
                              "entrenadorId": 5,
                              "entrenadorNombre": "Tony",
                              "partidosGanados": 3
                            }
                            """))),
            @ApiResponse(responseCode = "401", description = "No autorizado",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE)),
            @ApiResponse(responseCode = "404", description = "Equipo no encontrado",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            examples = @ExampleObject(value = """
                            {
                              "timestamp": "2025-12-18T00:00:00Z",
                              "status": 404,
                              "error": "Not Found",
                              "message": "Equipo no encontrado: 1",
                              "path": "/api/equipos/1"
                            }
                            """))),
            @ApiResponse(responseCode = "500", description = "Error interno",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE))
    })
    @GetMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<EquipoResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(svc.findById(id));
    }

    /**
     * Lista equipos que pertenecen a una liga específica.
     *
     * <p><b>Ejemplo:</b>
     * <pre>{@code
     * GET /api/equipos/liga/10
     * Authorization: Bearer <TOKEN>
     * }</pre>
     *
     * <p><b>Nota:</b> Si la liga existe pero no tiene equipos, devuelve lista vacía.
     */
    @Operation(
            summary = "Listar equipos por liga",
            description = "Devuelve todos los equipos de una liga (por ligaId). Requiere JWT."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "OK - lista de equipos por liga",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            examples = @ExampleObject(value = """
                            [
                              {
                                "id": 1,
                                "nombre": "Tigres",
                                "ligaId": 10,
                                "ligaNombre": "Liga UPIIZ",
                                "entrenadorId": 5,
                                "entrenadorNombre": "Tony",
                                "partidosGanados": 3
                              }
                            ]
                            """))),
            @ApiResponse(responseCode = "401", description = "No autorizado",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE)),
            @ApiResponse(responseCode = "500", description = "Error interno",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE))
    })
    @GetMapping(value = "/liga/{ligaId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<EquipoResponse>> getByLiga(@PathVariable Long ligaId) {
        return ResponseEntity.ok(svc.findByLiga(ligaId));
    }

    /**
     * Crea un equipo.
     *
     * <p><b>Ejemplo de request (con liga y entrenador):</b>
     * <pre>{@code
     * POST /api/equipos
     * Content-Type: application/json
     * Authorization: Bearer <TOKEN>
     *
     * {
     *   "nombre": "Tigres",
     *   "ligaId": 10,
     *   "entrenadorId": 5
     * }
     * }</pre>
     *
     * <p><b>Ejemplo de request (sin asignar relaciones):</b>
     * <pre>{@code
     * {
     *   "nombre": "Equipo Libre",
     *   "ligaId": null,
     *   "entrenadorId": null
     * }
     * }</pre>
     *
     * <p><b>Errores típicos:</b>
     * <ul>
     *   <li>404 si ligaId o entrenadorId no existen.</li>
     * </ul>
     */
    @Operation(
            summary = "Crear equipo",
            description = "Crea un equipo y opcionalmente lo asigna a una liga y/o entrenador. Requiere JWT."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Equipo creado",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            examples = @ExampleObject(value = """
                            {
                              "id": 100,
                              "nombre": "Tigres",
                              "ligaId": 10,
                              "ligaNombre": "Liga UPIIZ",
                              "entrenadorId": 5,
                              "entrenadorNombre": "Tony",
                              "partidosGanados": 0
                            }
                            """))),
            @ApiResponse(responseCode = "400", description = "Validación fallida",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE)),
            @ApiResponse(responseCode = "401", description = "No autorizado",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE)),
            @ApiResponse(responseCode = "404", description = "Liga/Entrenador no encontrado",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            examples = @ExampleObject(value = """
                            {
                              "timestamp": "2025-12-18T00:00:00Z",
                              "status": 404,
                              "error": "Not Found",
                              "message": "Liga no encontrada: 10",
                              "path": "/api/equipos"
                            }
                            """))),
            @ApiResponse(responseCode = "500", description = "Error interno",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE))
    })
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<EquipoResponse> create(
            @Valid
            @RequestBody(
                    description = "Datos para crear equipo",
                    required = true,
                    content = @Content(
                            schema = @Schema(implementation = EquipoRequest.class),
                            examples = {
                                    @ExampleObject(name = "Crear con liga y entrenador", value = """
                                    {
                                      "nombre": "Tigres",
                                      "ligaId": 10,
                                      "entrenadorId": 5
                                    }
                                    """),
                                    @ExampleObject(name = "Crear sin relaciones", value = """
                                    {
                                      "nombre": "Equipo Libre",
                                      "ligaId": null,
                                      "entrenadorId": null
                                    }
                                    """)
                            }
                    )
            )
            @org.springframework.web.bind.annotation.RequestBody EquipoRequest req
    ) {
        return ResponseEntity.ok(svc.create(req));
    }

    /**
     * Actualiza un equipo por ID.
     *
     * <p><b>Ejemplo (cambiando liga/entrenador):</b>
     * <pre>{@code
     * PUT /api/equipos/100
     * Content-Type: application/json
     * Authorization: Bearer <TOKEN>
     *
     * {
     *   "nombre": "Tigres FC",
     *   "ligaId": 12,
     *   "entrenadorId": 9
     * }
     * }</pre>
     *
     * <p><b>Ejemplo (desvincular entrenador):</b>
     * <pre>{@code
     * {
     *   "nombre": "Tigres FC",
     *   "ligaId": 12,
     *   "entrenadorId": null
     * }
     * }</pre>
     */
    @Operation(
            summary = "Actualizar equipo",
            description = "Actualiza un equipo y sus relaciones (liga/entrenador). Requiere JWT."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Equipo actualizado",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            examples = @ExampleObject(value = """
                            {
                              "id": 100,
                              "nombre": "Tigres FC",
                              "ligaId": 12,
                              "ligaNombre": "Liga 2026",
                              "entrenadorId": null,
                              "entrenadorNombre": null,
                              "partidosGanados": 0
                            }
                            """))),
            @ApiResponse(responseCode = "400", description = "Validación fallida",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE)),
            @ApiResponse(responseCode = "401", description = "No autorizado",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE)),
            @ApiResponse(responseCode = "404", description = "Equipo/Liga/Entrenador no encontrado",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE)),
            @ApiResponse(responseCode = "500", description = "Error interno",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE))
    })
    @PutMapping(value = "/{id}", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<EquipoResponse> update(
            @PathVariable Long id,
            @Valid
            @RequestBody(
                    description = "Datos para actualizar equipo",
                    required = true,
                    content = @Content(
                            schema = @Schema(implementation = EquipoRequest.class),
                            examples = {
                                    @ExampleObject(name = "Actualizar con relaciones", value = """
                                    {
                                      "nombre": "Tigres FC",
                                      "ligaId": 12,
                                      "entrenadorId": 9
                                    }
                                    """),
                                    @ExampleObject(name = "Desvincular entrenador", value = """
                                    {
                                      "nombre": "Tigres FC",
                                      "ligaId": 12,
                                      "entrenadorId": null
                                    }
                                    """)
                            }
                    )
            )
            @org.springframework.web.bind.annotation.RequestBody EquipoRequest req
    ) {
        return ResponseEntity.ok(svc.update(id, req));
    }

    /**
     * Elimina un equipo por ID.
     *
     * <p><b>Ejemplo:</b>
     * <pre>{@code
     * DELETE /api/equipos/100
     * Authorization: Bearer <TOKEN>
     * }</pre>
     *
     * <p><b>Respuesta:</b> 204 No Content.
     *
     * <p><b>Nota de integridad:</b>
     * Si hay jugadores asociados a este equipo, puede fallar si tu BD impide borrar registros referenciados.
     * Soluciones típicas:
     * <ul>
     *   <li>Eliminar primero jugadores</li>
     *   <li>O hacer que Jugador.equipo sea nullable y desvincular</li>
     *   <li>O definir cascade/orphan removal (con cuidado)</li>
     * </ul>
     */
    @Operation(
            summary = "Eliminar equipo",
            description = "Elimina un equipo por ID. Requiere JWT."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Equipo eliminado"),
            @ApiResponse(responseCode = "401", description = "No autorizado",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE)),
            @ApiResponse(responseCode = "404", description = "Equipo no encontrado",
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
