package com.upiiz.ligas_api.service;

import com.upiiz.ligas_api.dto.auth.*;
import com.upiiz.ligas_api.entity.Role;
import com.upiiz.ligas_api.entity.User;
import com.upiiz.ligas_api.exception.BadRequestException;
import com.upiiz.ligas_api.repository.UserRepository;
import com.upiiz.ligas_api.security.JwtService;
import org.springframework.security.authentication.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;

@Service
public class AuthService {

    private final UserRepository userRepo;
    private final PasswordEncoder encoder;
    private final AuthenticationManager authManager;
    private final JwtService jwtService;

    public AuthService(UserRepository userRepo, PasswordEncoder encoder, AuthenticationManager authManager, JwtService jwtService) {
        this.userRepo = userRepo;
        this.encoder = encoder;
        this.authManager = authManager;
        this.jwtService = jwtService;
    }

    public void register(RegisterRequest req) {
        if (userRepo.existsByEmail(req.email())) {
            throw new BadRequestException("El email ya está registrado");
        }
        User u = User.builder()
                .email(req.email().toLowerCase().trim())
                .nombre(req.nombre().trim())
                .password(encoder.encode(req.password()))
                .roles(Set.of(Role.ROLE_USER))
                .build();
        userRepo.save(u);
    }

    public AuthResponse login(LoginRequest req) {
        var token = new UsernamePasswordAuthenticationToken(req.email().toLowerCase().trim(), req.password());
        authManager.authenticate(token);

        var u = userRepo.findByEmail(req.email().toLowerCase().trim())
                .orElseThrow(() -> new BadCredentialsException("Credenciales inválidas"));

        List<String> roles = u.getRoles().stream().map(Enum::name).toList();
        String jwt = jwtService.generateToken(u.getEmail(), roles);

        return new AuthResponse(jwt);
    }
}
