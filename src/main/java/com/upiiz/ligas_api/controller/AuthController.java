package com.upiiz.ligas_api.controller;

import com.upiiz.ligas_api.dto.auth.AuthResponse;
import com.upiiz.ligas_api.dto.auth.LoginRequest;
import com.upiiz.ligas_api.dto.auth.RegisterRequest;
import com.upiiz.ligas_api.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Controlador de Autenticación.
 *
 * <p>Responsable de:
 * <ul>
 *   <li>Registrar usuarios (crea un usuario con rol por defecto ROLE_USER).</li>
 *   <li>Autenticar usuarios y emitir un JWT (JSON Web Token) válido por un tiempo configurable.</li>
 * </ul>
 *
 * <p><b>Notas de seguridad:</b>
 * <ul>
 *   <li>Estos endpoints son públicos (no requieren token).</li>
 *   <li>Todos los demás endpoints de la API deben requerir header:
 *       <code>Authorization: Bearer &lt;token&gt;</code>.</li>
 * </ul>
 *
 * <p><b>Errores comunes:</b>
 * <ul>
 *   <li><b>400</b> si la validación falla (email inválido, password muy corto, campos vacíos).</li>
 *   <li><b>401</b> si las credenciales son inválidas en login.</li>
 *   <li><b>409/400</b> si el email ya existe (según cómo lo maneje tu servicio/handler).</li>
 * </ul>
 */
@Tag(name = "Auth", description = "Registro e inicio de sesión con JWT")
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService svc;

    public AuthController(AuthService svc) {
        this.svc = svc;
    }

    /**
     * Registra un nuevo usuario.
     *
     * <p>Flujo:
     * <ol>
     *   <li>Recibe email, password y nombre.</li>
     *   <li>Valida que el email no exista.</li>
     *   <li>Guarda password en forma segura (BCrypt) y asigna rol ROLE_USER.</li>
     * </ol>
     *
     * <p><b>Ejemplo de request:</b>
     * <pre>{@code
     * POST /api/auth/register
     * Content-Type: application/json
     *
     * {
     *   "email": "mel@upiiz.com",
     *   "password": "Mel123456",
     *   "nombre": "Melanie"
     * }
     * }</pre>
     *
     * <p><b>Respuesta esperada:</b> 200 OK (sin body).
     */
    @Operation(
            summary = "Registrar usuario",
            description = "Crea un nuevo usuario (por defecto con ROLE_USER). No requiere token."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Usuario registrado correctamente"),
            @ApiResponse(responseCode = "400", description = "Datos inválidos o email ya registrado",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            examples = @ExampleObject(value = """
                            {
                              "timestamp": "2025-12-18T00:00:00Z",
                              "status": 400,
                              "error": "Bad Request",
                              "message": "El email ya está registrado",
                              "path": "/api/auth/register"
                            }
                            """))),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE))
    })
    @PostMapping(value = "/register", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Void> register(
            @Valid
            @RequestBody(
                    description = "Datos de registro",
                    required = true,
                    content = @Content(
                            schema = @Schema(implementation = RegisterRequest.class),
                            examples = @ExampleObject(name = "Registro ejemplo", value = """
                            {
                              "email": "mel@upiiz.com",
                              "password": "Mel123456",
                              "nombre": "Melanie"
                            }
                            """)
                    )
            )
            @org.springframework.web.bind.annotation.RequestBody RegisterRequest req
    ) {
        svc.register(req);
        return ResponseEntity.ok().build();
    }

    /**
     * Inicia sesión y devuelve un token JWT.
     *
     * <p>Flujo:
     * <ol>
     *   <li>Valida credenciales (email + password).</li>
     *   <li>Genera JWT que incluye:
     *     <ul>
     *       <li>sub: email</li>
     *       <li>roles: roles del usuario</li>
     *       <li>exp: expiración (configurable)</li>
     *     </ul>
     *   </li>
     * </ol>
     *
     * <p><b>Ejemplo de request:</b>
     * <pre>{@code
     * POST /api/auth/login
     * Content-Type: application/json
     *
     * {
     *   "email": "admin@upiiz.com",
     *   "password": "Admin12345"
     * }
     * }</pre>
     *
     * <p><b>Ejemplo de response:</b>
     * <pre>{@code
     * 200 OK
     * {
     *   "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
     * }
     * }</pre>
     */
    @Operation(
            summary = "Login",
            description = "Autentica y devuelve un JWT. No requiere token."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Login exitoso (devuelve JWT)",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = AuthResponse.class),
                            examples = @ExampleObject(value = """
                            {
                              "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJhZG1pbkB1cGlpei5jb20iLCJyb2xlcyI6WyJST0xFX0FETUlOIiwiUk9MRV9VU0VSIl0sImV4cCI6MTc2NjA1MDAwMH0.xxxxx"
                            }
                            """))),
            @ApiResponse(responseCode = "401", description = "Credenciales inválidas",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            examples = @ExampleObject(value = """
                            {
                              "timestamp": "2025-12-18T00:00:00Z",
                              "status": 401,
                              "error": "Unauthorized",
                              "message": "Credenciales inválidas",
                              "path": "/api/auth/login"
                            }
                            """))),
            @ApiResponse(responseCode = "400", description = "Validación fallida (campos vacíos / email inválido)",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE)),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE))
    })
    @PostMapping(value = "/login", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<AuthResponse> login(
            @Valid
            @RequestBody(
                    description = "Credenciales para login",
                    required = true,
                    content = @Content(
                            schema = @Schema(implementation = LoginRequest.class),
                            examples = @ExampleObject(name = "Login ejemplo", value = """
                            {
                              "email": "admin@upiiz.com",
                              "password": "Admin12345"
                            }
                            """)
                    )
            )
            @org.springframework.web.bind.annotation.RequestBody LoginRequest req
    ) {
        return ResponseEntity.ok(svc.login(req));
    }
}
