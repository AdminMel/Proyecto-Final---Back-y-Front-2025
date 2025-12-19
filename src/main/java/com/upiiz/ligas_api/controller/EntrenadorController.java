package com.upiiz.ligas_api.controller;

import com.upiiz.ligas_api.dto.entrenador.EntrenadorRequest;
import com.upiiz.ligas_api.dto.entrenador.EntrenadorResponse;
import com.upiiz.ligas_api.service.EntrenadorService;
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
 * Controlador de Entrenadores.
 *
 * <p>Responsable de administrar entrenadores mediante operaciones CRUD:
 * <ul>
 *   <li>Listar entrenadores</li>
 *   <li>Consultar entrenador por ID</li>
 *   <li>Crear entrenador</li>
 *   <li>Actualizar entrenador</li>
 *   <li>Eliminar entrenador</li>
 * </ul>
 *
 * <p><b>Seguridad:</b> Todos los endpoints requieren JWT (porque "todo excepto auth" es protegido).
 * <pre>{@code
 * Authorization: Bearer <TOKEN>
 * }</pre>
 *
 * <p><b>Relación con Equipos:</b>
 * Un entrenador puede asociarse a un equipo desde el módulo de Equipos (por ejemplo usando entrenadorId).
 *
 * <p><b>Errores comunes:</b>
 * <ul>
 *   <li><b>401</b>: token faltante/ inválido/ expirado.</li>
 *   <li><b>404</b>: entrenador no existe.</li>
 *   <li><b>400</b>: validación fallida (nombre vacío, email inválido, etc.).</li>
 * </ul>
 */
@Tag(name = "Entrenadores", description = "CRUD de entrenadores (requiere JWT)")
@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("/api/entrenadores")
public class EntrenadorController {

    private final EntrenadorService svc;

    public EntrenadorController(EntrenadorService svc) {
        this.svc = svc;
    }

    /**
     * Lista todos los entrenadores registrados.
     *
     * <p><b>Ejemplo:</b>
     * <pre>{@code
     * GET /api/entrenadores
     * Authorization: Bearer <TOKEN>
     * }</pre>
     *
     * <p><b>Respuesta 200:</b> lista de entrenadores.
     */
    @Operation(
            summary = "Listar entrenadores",
            description = "Devuelve todos los entrenadores registrados. Requiere JWT."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "OK - lista de entrenadores",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            examples = @ExampleObject(value = """
                            [
                              { "id": 1, "nombre": "Tony", "email": "tony@coach.com", "telefono": "4921234567" },
                              { "id": 2, "nombre": "Luz", "email": "luz@coach.com", "telefono": "4927654321" }
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
                              "path": "/api/entrenadores"
                            }
                            """))),
            @ApiResponse(responseCode = "500", description = "Error interno",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE))
    })
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<EntrenadorResponse>> getAll() {
        return ResponseEntity.ok(svc.findAll());
    }

    /**
     * Consulta un entrenador por su ID.
     *
     * <p><b>Ejemplo:</b>
     * <pre>{@code
     * GET /api/entrenadores/1
     * Authorization: Bearer <TOKEN>
     * }</pre>
     *
     * <p><b>Respuesta 200:</b> entrenador encontrado.
     * <p><b>Respuesta 404:</b> si no existe.
     */
    @Operation(
            summary = "Buscar entrenador por ID",
            description = "Devuelve un entrenador específico por su ID. Requiere JWT."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "OK - entrenador encontrado",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            examples = @ExampleObject(value = """
                            {
                              "id": 1,
                              "nombre": "Tony",
                              "email": "tony@coach.com",
                              "telefono": "4921234567"
                            }
                            """))),
            @ApiResponse(responseCode = "401", description = "No autorizado",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE)),
            @ApiResponse(responseCode = "404", description = "No encontrado - entrenador no existe",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            examples = @ExampleObject(value = """
                            {
                              "timestamp": "2025-12-18T00:00:00Z",
                              "status": 404,
                              "error": "Not Found",
                              "message": "Entrenador no encontrado: 99",
                              "path": "/api/entrenadores/99"
                            }
                            """))),
            @ApiResponse(responseCode = "500", description = "Error interno",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE))
    })
    @GetMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<EntrenadorResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(svc.findById(id));
    }

    /**
     * Crea un nuevo entrenador.
     *
     * <p><b>Ejemplo de request:</b>
     * <pre>{@code
     * POST /api/entrenadores
     * Content-Type: application/json
     * Authorization: Bearer <TOKEN>
     *
     * {
     *   "nombre": "Tony",
     *   "email": "tony@coach.com",
     *   "telefono": "4921234567"
     * }
     * }</pre>
     *
     * <p><b>Respuesta 200:</b> entrenador creado.
     */
    @Operation(
            summary = "Crear entrenador",
            description = "Crea un entrenador. Requiere JWT."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Creado correctamente",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            examples = @ExampleObject(value = """
                            {
                              "id": 10,
                              "nombre": "Tony",
                              "email": "tony@coach.com",
                              "telefono": "4921234567"
                            }
                            """))),
            @ApiResponse(responseCode = "400", description = "Validación fallida (campos inválidos)",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            examples = @ExampleObject(value = """
                            {
                              "timestamp": "2025-12-18T00:00:00Z",
                              "status": 400,
                              "error": "Bad Request",
                              "message": "must not be blank",
                              "path": "/api/entrenadores"
                            }
                            """))),
            @ApiResponse(responseCode = "401", description = "No autorizado",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE)),
            @ApiResponse(responseCode = "500", description = "Error interno",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE))
    })
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<EntrenadorResponse> create(
            @Valid
            @RequestBody(
                    description = "Datos para crear entrenador",
                    required = true,
                    content = @Content(
                            schema = @Schema(implementation = EntrenadorRequest.class),
                            examples = @ExampleObject(name = "Crear entrenador", value = """
                            {
                              "nombre": "Tony",
                              "email": "tony@coach.com",
                              "telefono": "4921234567"
                            }
                            """)
                    )
            )
            @org.springframework.web.bind.annotation.RequestBody EntrenadorRequest req
    ) {
        return ResponseEntity.ok(svc.create(req));
    }

    /**
     * Actualiza un entrenador existente por ID.
     *
     * <p><b>Ejemplo:</b>
     * <pre>{@code
     * PUT /api/entrenadores/10
     * Content-Type: application/json
     * Authorization: Bearer <TOKEN>
     *
     * {
     *   "nombre": "Tony Actualizado",
     *   "email": "tony@coach.com",
     *   "telefono": "4920000000"
     * }
     * }</pre>
     *
     * <p><b>Respuesta 200:</b> entrenador actualizado.
     * <p><b>Respuesta 404:</b> si no existe.
     */
    @Operation(
            summary = "Actualizar entrenador",
            description = "Actualiza un entrenador por ID. Requiere JWT."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Actualizado correctamente",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            examples = @ExampleObject(value = """
                            {
                              "id": 10,
                              "nombre": "Tony Actualizado",
                              "email": "tony@coach.com",
                              "telefono": "4920000000"
                            }
                            """))),
            @ApiResponse(responseCode = "400", description = "Validación fallida",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE)),
            @ApiResponse(responseCode = "401", description = "No autorizado",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE)),
            @ApiResponse(responseCode = "404", description = "Entrenador no encontrado",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            examples = @ExampleObject(value = """
                            {
                              "timestamp": "2025-12-18T00:00:00Z",
                              "status": 404,
                              "error": "Not Found",
                              "message": "Entrenador no encontrado: 10",
                              "path": "/api/entrenadores/10"
                            }
                            """))),
            @ApiResponse(responseCode = "500", description = "Error interno",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE))
    })
    @PutMapping(value = "/{id}", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<EntrenadorResponse> update(
            @PathVariable Long id,
            @Valid
            @RequestBody(
                    description = "Datos para actualizar entrenador",
                    required = true,
                    content = @Content(
                            schema = @Schema(implementation = EntrenadorRequest.class),
                            examples = @ExampleObject(name = "Actualizar entrenador", value = """
                            {
                              "nombre": "Tony Actualizado",
                              "email": "tony@coach.com",
                              "telefono": "4920000000"
                            }
                            """)
                    )
            )
            @org.springframework.web.bind.annotation.RequestBody EntrenadorRequest req
    ) {
        return ResponseEntity.ok(svc.update(id, req));
    }

    /**
     * Elimina un entrenador por ID.
     *
     * <p><b>Ejemplo:</b>
     * <pre>{@code
     * DELETE /api/entrenadores/10
     * Authorization: Bearer <TOKEN>
     * }</pre>
     *
     * <p><b>Respuesta 204:</b> eliminado.
     *
     * <p><b>Nota:</b> Si tienes equipos que referencian al entrenador, puede fallar por integridad referencial
     * dependiendo de tu modelo (OneToOne/ManyToOne) y constraints de BD. En ese caso, primero desvincula
     * el entrenador del equipo o maneja la regla en tu servicio.
     */
    @Operation(
            summary = "Eliminar entrenador",
            description = "Elimina un entrenador por ID. Requiere JWT."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Eliminado correctamente"),
            @ApiResponse(responseCode = "401", description = "No autorizado",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE)),
            @ApiResponse(responseCode = "404", description = "Entrenador no encontrado",
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
