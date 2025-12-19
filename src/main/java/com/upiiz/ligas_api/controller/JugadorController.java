package com.upiiz.ligas_api.controller;

import com.upiiz.ligas_api.dto.jugador.JugadorRequest;
import com.upiiz.ligas_api.dto.jugador.JugadorResponse;
import com.upiiz.ligas_api.service.JugadorService;
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
 * Controlador de Jugadores.
 *
 * <p>Permite administrar jugadores mediante operaciones CRUD y consultas por equipo:
 * <ul>
 *   <li>Listar jugadores</li>
 *   <li>Consultar jugador por ID</li>
 *   <li>Listar jugadores por equipo</li>
 *   <li>Crear jugador (opcionalmente asignándolo a un equipo)</li>
 *   <li>Actualizar jugador (cambiando su equipo / desvinculando)</li>
 *   <li>Eliminar jugador</li>
 * </ul>
 *
 * <p><b>Seguridad:</b> Todos los endpoints requieren JWT (porque todo excepto /api/auth/** es protegido).
 * <pre>{@code
 * Authorization: Bearer <TOKEN>
 * }</pre>
 *
 * <p><b>Asignación a equipo:</b>
 * <ul>
 *   <li>En create/update puedes enviar <code>equipoId</code> para asociarlo a un equipo existente.</li>
 *   <li>En update, si envías <code>equipoId: null</code> se desvincula del equipo (según tu service).</li>
 * </ul>
 *
 * <p><b>Errores comunes:</b>
 * <ul>
 *   <li><b>401</b>: token faltante/ inválido/ expirado.</li>
 *   <li><b>404</b>: jugador no existe o equipoId no existe al asignar.</li>
 *   <li><b>400</b>: validación fallida (edad < 1, nombre vacío, etc.).</li>
 * </ul>
 */
@Tag(name = "Jugadores", description = "CRUD de jugadores y consulta por equipo (requiere JWT)")
@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("/api/jugadores")
public class JugadorController {

    private final JugadorService svc;

    public JugadorController(JugadorService svc) {
        this.svc = svc;
    }

    /**
     * Lista todos los jugadores.
     *
     * <p><b>Ejemplo:</b>
     * <pre>{@code
     * GET /api/jugadores
     * Authorization: Bearer <TOKEN>
     * }</pre>
     */
    @Operation(
            summary = "Listar jugadores",
            description = "Devuelve la lista completa de jugadores. Requiere JWT."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "OK - lista de jugadores",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            examples = @ExampleObject(value = """
                            [
                              {
                                "id": 1,
                                "nombre": "Ana López",
                                "edad": 19,
                                "posicion": "Delantera",
                                "equipoId": 10,
                                "equipoNombre": "Tigres"
                              },
                              {
                                "id": 2,
                                "nombre": "Luis Pérez",
                                "edad": 21,
                                "posicion": "Portero",
                                "equipoId": null,
                                "equipoNombre": null
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
                              "path": "/api/jugadores"
                            }
                            """))),
            @ApiResponse(responseCode = "500", description = "Error interno",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE))
    })
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<JugadorResponse>> getAll() {
        return ResponseEntity.ok(svc.findAll());
    }

    /**
     * Consulta un jugador por su ID.
     *
     * <p><b>Ejemplo:</b>
     * <pre>{@code
     * GET /api/jugadores/1
     * Authorization: Bearer <TOKEN>
     * }</pre>
     */
    @Operation(
            summary = "Buscar jugador por ID",
            description = "Devuelve un jugador específico por su ID. Requiere JWT."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "OK - jugador encontrado",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            examples = @ExampleObject(value = """
                            {
                              "id": 1,
                              "nombre": "Ana López",
                              "edad": 19,
                              "posicion": "Delantera",
                              "equipoId": 10,
                              "equipoNombre": "Tigres"
                            }
                            """))),
            @ApiResponse(responseCode = "401", description = "No autorizado",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE)),
            @ApiResponse(responseCode = "404", description = "Jugador no encontrado",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            examples = @ExampleObject(value = """
                            {
                              "timestamp": "2025-12-18T00:00:00Z",
                              "status": 404,
                              "error": "Not Found",
                              "message": "Jugador no encontrado: 99",
                              "path": "/api/jugadores/99"
                            }
                            """))),
            @ApiResponse(responseCode = "500", description = "Error interno",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE))
    })
    @GetMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<JugadorResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(svc.findById(id));
    }

    /**
     * Lista jugadores por equipo.
     *
     * <p><b>Ejemplo:</b>
     * <pre>{@code
     * GET /api/jugadores/equipo/10
     * Authorization: Bearer <TOKEN>
     * }</pre>
     *
     * <p><b>Nota:</b> si el equipo existe pero no tiene jugadores, devuelve lista vacía.
     */
    @Operation(
            summary = "Listar jugadores por equipo",
            description = "Devuelve los jugadores que pertenecen a un equipo (equipoId). Requiere JWT."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "OK - lista de jugadores por equipo",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            examples = @ExampleObject(value = """
                            [
                              {
                                "id": 1,
                                "nombre": "Ana López",
                                "edad": 19,
                                "posicion": "Delantera",
                                "equipoId": 10,
                                "equipoNombre": "Tigres"
                              }
                            ]
                            """))),
            @ApiResponse(responseCode = "401", description = "No autorizado",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE)),
            @ApiResponse(responseCode = "500", description = "Error interno",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE))
    })
    @GetMapping(value = "/equipo/{equipoId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<JugadorResponse>> getByEquipo(@PathVariable Long equipoId) {
        return ResponseEntity.ok(svc.findByEquipo(equipoId));
    }

    /**
     * Crea un jugador.
     *
     * <p><b>Ejemplo (asignándolo a un equipo):</b>
     * <pre>{@code
     * POST /api/jugadores
     * Content-Type: application/json
     * Authorization: Bearer <TOKEN>
     *
     * {
     *   "nombre": "Ana López",
     *   "edad": 19,
     *   "posicion": "Delantera",
     *   "equipoId": 10
     * }
     * }</pre>
     *
     * <p><b>Ejemplo (sin equipo):</b>
     * <pre>{@code
     * {
     *   "nombre": "Luis Pérez",
     *   "edad": 21,
     *   "posicion": "Portero",
     *   "equipoId": null
     * }
     * }</pre>
     *
     * <p><b>Errores típicos:</b>
     * <ul>
     *   <li>404 si equipoId no existe.</li>
     * </ul>
     */
    @Operation(
            summary = "Crear jugador",
            description = "Crea un jugador y opcionalmente lo asigna a un equipo. Requiere JWT."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Jugador creado",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            examples = @ExampleObject(value = """
                            {
                              "id": 50,
                              "nombre": "Ana López",
                              "edad": 19,
                              "posicion": "Delantera",
                              "equipoId": 10,
                              "equipoNombre": "Tigres"
                            }
                            """))),
            @ApiResponse(responseCode = "400", description = "Validación fallida (edad mínima, nombre vacío, etc.)",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE)),
            @ApiResponse(responseCode = "401", description = "No autorizado",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE)),
            @ApiResponse(responseCode = "404", description = "Equipo no encontrado",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            examples = @ExampleObject(value = """
                            {
                              "timestamp": "2025-12-18T00:00:00Z",
                              "status": 404,
                              "error": "Not Found",
                              "message": "Equipo no encontrado: 10",
                              "path": "/api/jugadores"
                            }
                            """))),
            @ApiResponse(responseCode = "500", description = "Error interno",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE))
    })
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<JugadorResponse> create(
            @Valid
            @RequestBody(
                    description = "Datos para crear jugador",
                    required = true,
                    content = @Content(
                            schema = @Schema(implementation = JugadorRequest.class),
                            examples = {
                                    @ExampleObject(name = "Crear con equipo", value = """
                                    {
                                      "nombre": "Ana López",
                                      "edad": 19,
                                      "posicion": "Delantera",
                                      "equipoId": 10
                                    }
                                    """),
                                    @ExampleObject(name = "Crear sin equipo", value = """
                                    {
                                      "nombre": "Luis Pérez",
                                      "edad": 21,
                                      "posicion": "Portero",
                                      "equipoId": null
                                    }
                                    """)
                            }
                    )
            )
            @org.springframework.web.bind.annotation.RequestBody JugadorRequest req
    ) {
        return ResponseEntity.ok(svc.create(req));
    }

    /**
     * Actualiza un jugador por ID.
     *
     * <p><b>Ejemplo (cambiar de equipo):</b>
     * <pre>{@code
     * PUT /api/jugadores/50
     * Content-Type: application/json
     * Authorization: Bearer <TOKEN>
     *
     * {
     *   "nombre": "Ana López",
     *   "edad": 20,
     *   "posicion": "Delantera",
     *   "equipoId": 12
     * }
     * }</pre>
     *
     * <p><b>Ejemplo (desvincular equipo):</b>
     * <pre>{@code
     * {
     *   "nombre": "Luis Pérez",
     *   "edad": 21,
     *   "posicion": "Portero",
     *   "equipoId": null
     * }
     * }</pre>
     */
    @Operation(
            summary = "Actualizar jugador",
            description = "Actualiza los datos del jugador y opcionalmente su equipo. Requiere JWT."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Jugador actualizado",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            examples = @ExampleObject(value = """
                            {
                              "id": 50,
                              "nombre": "Ana López",
                              "edad": 20,
                              "posicion": "Delantera",
                              "equipoId": 12,
                              "equipoNombre": "Halcones"
                            }
                            """))),
            @ApiResponse(responseCode = "400", description = "Validación fallida",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE)),
            @ApiResponse(responseCode = "401", description = "No autorizado",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE)),
            @ApiResponse(responseCode = "404", description = "Jugador/Equipo no encontrado",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE)),
            @ApiResponse(responseCode = "500", description = "Error interno",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE))
    })
    @PutMapping(value = "/{id}", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<JugadorResponse> update(
            @PathVariable Long id,
            @Valid
            @RequestBody(
                    description = "Datos para actualizar jugador",
                    required = true,
                    content = @Content(
                            schema = @Schema(implementation = JugadorRequest.class),
                            examples = {
                                    @ExampleObject(name = "Actualizar cambiando equipo", value = """
                                    {
                                      "nombre": "Ana López",
                                      "edad": 20,
                                      "posicion": "Delantera",
                                      "equipoId": 12
                                    }
                                    """),
                                    @ExampleObject(name = "Desvincular equipo", value = """
                                    {
                                      "nombre": "Luis Pérez",
                                      "edad": 21,
                                      "posicion": "Portero",
                                      "equipoId": null
                                    }
                                    """)
                            }
                    )
            )
            @org.springframework.web.bind.annotation.RequestBody JugadorRequest req
    ) {
        return ResponseEntity.ok(svc.update(id, req));
    }

    /**
     * Elimina un jugador por ID.
     *
     * <p><b>Ejemplo:</b>
     * <pre>{@code
     * DELETE /api/jugadores/50
     * Authorization: Bearer <TOKEN>
     * }</pre>
     *
     * <p><b>Respuesta:</b> 204 No Content.
     */
    @Operation(
            summary = "Eliminar jugador",
            description = "Elimina un jugador por ID. Requiere JWT."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Jugador eliminado"),
            @ApiResponse(responseCode = "401", description = "No autorizado",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE)),
            @ApiResponse(responseCode = "404", description = "Jugador no encontrado",
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
