package com.upiiz.ligas_api.controller;

import com.upiiz.ligas_api.dto.liga.LigaRequest;
import com.upiiz.ligas_api.dto.liga.LigaResponse;
import com.upiiz.ligas_api.service.LigaService;
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
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controlador de Ligas.
 *
 * <p>Permite administrar ligas mediante operaciones CRUD:
 * <ul>
 *   <li>Listar ligas</li>
 *   <li>Consultar liga por ID</li>
 *   <li>Crear liga</li>
 *   <li>Actualizar liga</li>
 *   <li>Eliminar liga (solo ADMIN)</li>
 * </ul>
 *
 * <p><b>Seguridad:</b> Todos los endpoints requieren JWT (todo excepto /api/auth/** está protegido).
 * <pre>{@code
 * Authorization: Bearer <TOKEN>
 * }</pre>
 *
 * <p><b>Control de permisos:</b>
 * <ul>
 *   <li><b>DELETE</b> requiere rol <code>ROLE_ADMIN</code>.</li>
 *   <li>Si el token es válido pero el usuario no es ADMIN, Spring Security devuelve <b>403 Forbidden</b>.</li>
 * </ul>
 *
 * <p><b>Errores comunes:</b>
 * <ul>
 *   <li><b>401</b>: token faltante/ inválido/ expirado.</li>
 *   <li><b>403</b>: token válido, pero sin permisos (ej. intentar borrar sin ADMIN).</li>
 *   <li><b>404</b>: liga no existe.</li>
 *   <li><b>400</b>: validación fallida (nombre vacío, etc.).</li>
 * </ul>
 */
@Tag(name = "Ligas", description = "CRUD de ligas (requiere JWT; DELETE solo ADMIN)")
@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("/api/ligas")
public class LigaController {

    private final LigaService svc;

    public LigaController(LigaService svc) {
        this.svc = svc;
    }

    /**
     * Lista todas las ligas registradas.
     *
     * <p><b>Ejemplo:</b>
     * <pre>{@code
     * GET /api/ligas
     * Authorization: Bearer <TOKEN>
     * }</pre>
     *
     * <p><b>Respuesta 200:</b> lista de ligas.
     */
    @Operation(
            summary = "Listar ligas",
            description = "Devuelve la lista completa de ligas. Requiere JWT."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "OK - lista de ligas",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            examples = @ExampleObject(value = """
                            [
                              { "id": 1, "nombre": "Liga UPIIZ", "descripcion": "Torneo interno" },
                              { "id": 2, "nombre": "Liga Zacatecas", "descripcion": "Regional" }
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
                              "path": "/api/ligas"
                            }
                            """))),
            @ApiResponse(responseCode = "500", description = "Error interno",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE))
    })
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<LigaResponse>> getAll() {
        return ResponseEntity.ok(svc.findAll());
    }

    /**
     * Consulta una liga por su ID.
     *
     * <p><b>Ejemplo:</b>
     * <pre>{@code
     * GET /api/ligas/1
     * Authorization: Bearer <TOKEN>
     * }</pre>
     */
    @Operation(
            summary = "Buscar liga por ID",
            description = "Devuelve una liga específica por ID. Requiere JWT."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "OK - liga encontrada",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            examples = @ExampleObject(value = """
                            { "id": 1, "nombre": "Liga UPIIZ", "descripcion": "Torneo interno" }
                            """))),
            @ApiResponse(responseCode = "401", description = "No autorizado",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE)),
            @ApiResponse(responseCode = "404", description = "Liga no encontrada",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            examples = @ExampleObject(value = """
                            {
                              "timestamp": "2025-12-18T00:00:00Z",
                              "status": 404,
                              "error": "Not Found",
                              "message": "Liga no encontrada: 99",
                              "path": "/api/ligas/99"
                            }
                            """))),
            @ApiResponse(responseCode = "500", description = "Error interno",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE))
    })
    @GetMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<LigaResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(svc.findById(id));
    }

    /**
     * Crea una liga.
     *
     * <p><b>Ejemplo de request:</b>
     * <pre>{@code
     * POST /api/ligas
     * Content-Type: application/json
     * Authorization: Bearer <TOKEN>
     *
     * {
     *   "nombre": "Liga UPIIZ",
     *   "descripcion": "Torneo interno 2025"
     * }
     * }</pre>
     */
    @Operation(
            summary = "Crear liga",
            description = "Crea una liga. Requiere JWT."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Liga creada",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            examples = @ExampleObject(value = """
                            { "id": 10, "nombre": "Liga UPIIZ", "descripcion": "Torneo interno 2025" }
                            """))),
            @ApiResponse(responseCode = "400", description = "Validación fallida (campos inválidos)",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            examples = @ExampleObject(value = """
                            {
                              "timestamp": "2025-12-18T00:00:00Z",
                              "status": 400,
                              "error": "Bad Request",
                              "message": "must not be blank",
                              "path": "/api/ligas"
                            }
                            """))),
            @ApiResponse(responseCode = "401", description = "No autorizado",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE)),
            @ApiResponse(responseCode = "500", description = "Error interno",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE))
    })
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<LigaResponse> create(
            @Valid
            @RequestBody(
                    description = "Datos para crear liga",
                    required = true,
                    content = @Content(
                            schema = @Schema(implementation = LigaRequest.class),
                            examples = @ExampleObject(name = "Crear liga", value = """
                            {
                              "nombre": "Liga UPIIZ",
                              "descripcion": "Torneo interno 2025"
                            }
                            """)
                    )
            )
            @org.springframework.web.bind.annotation.RequestBody LigaRequest req
    ) {
        return ResponseEntity.ok(svc.create(req));
    }

    /**
     * Actualiza una liga por ID.
     *
     * <p><b>Ejemplo:</b>
     * <pre>{@code
     * PUT /api/ligas/10
     * Content-Type: application/json
     * Authorization: Bearer <TOKEN>
     *
     * {
     *   "nombre": "Liga UPIIZ 2026",
     *   "descripcion": "Temporada 2026"
     * }
     * }</pre>
     */
    @Operation(
            summary = "Actualizar liga",
            description = "Actualiza nombre/descripcion de una liga por ID. Requiere JWT."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Liga actualizada",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            examples = @ExampleObject(value = """
                            { "id": 10, "nombre": "Liga UPIIZ 2026", "descripcion": "Temporada 2026" }
                            """))),
            @ApiResponse(responseCode = "400", description = "Validación fallida",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE)),
            @ApiResponse(responseCode = "401", description = "No autorizado",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE)),
            @ApiResponse(responseCode = "404", description = "Liga no encontrada",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE)),
            @ApiResponse(responseCode = "500", description = "Error interno",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE))
    })
    @PutMapping(value = "/{id}", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<LigaResponse> update(
            @PathVariable Long id,
            @Valid
            @RequestBody(
                    description = "Datos para actualizar liga",
                    required = true,
                    content = @Content(
                            schema = @Schema(implementation = LigaRequest.class),
                            examples = @ExampleObject(name = "Actualizar liga", value = """
                            {
                              "nombre": "Liga UPIIZ 2026",
                              "descripcion": "Temporada 2026"
                            }
                            """)
                    )
            )
            @org.springframework.web.bind.annotation.RequestBody LigaRequest req
    ) {
        return ResponseEntity.ok(svc.update(id, req));
    }

    /**
     * Elimina una liga por ID (solo ADMIN).
     *
     * <p><b>Ejemplo:</b>
     * <pre>{@code
     * DELETE /api/ligas/10
     * Authorization: Bearer <TOKEN_ADMIN>
     * }</pre>
     *
     * <p><b>Respuesta 204:</b> eliminada.
     *
     * <p><b>403 Forbidden:</b> si el usuario autenticado NO tiene ROLE_ADMIN.
     */
    @Operation(
            summary = "Eliminar liga (solo ADMIN)",
            description = "Elimina una liga por ID. Requiere JWT y rol ADMIN."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Liga eliminada"),
            @ApiResponse(responseCode = "401", description = "No autorizado (sin token / token inválido)",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE)),
            @ApiResponse(responseCode = "403", description = "Prohibido (token válido pero sin ROLE_ADMIN)",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            examples = @ExampleObject(value = """
                            {
                              "timestamp": "2025-12-18T00:00:00Z",
                              "status": 403,
                              "error": "Forbidden",
                              "message": "Access Denied",
                              "path": "/api/ligas/10"
                            }
                            """))),
            @ApiResponse(responseCode = "404", description = "Liga no encontrada",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE)),
            @ApiResponse(responseCode = "500", description = "Error interno",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE))
    })
    // SOLO ADMIN puede borrar ligas
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        svc.delete(id);
        return ResponseEntity.noContent().build();
    }
}
