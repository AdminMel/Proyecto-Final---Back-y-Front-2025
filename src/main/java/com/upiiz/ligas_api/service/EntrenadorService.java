package com.upiiz.ligas_api.service;

import com.upiiz.ligas_api.dto.entrenador.*;
import com.upiiz.ligas_api.entity.Entrenador;
import com.upiiz.ligas_api.exception.ResourceNotFoundException;
import com.upiiz.ligas_api.repository.EntrenadorRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class EntrenadorService {

    private final EntrenadorRepository repo;

    public EntrenadorService(EntrenadorRepository repo) {
        this.repo = repo;
    }

    public List<EntrenadorResponse> findAll() {
        return repo.findAll().stream().map(this::toResponse).toList();
    }

    public EntrenadorResponse findById(Long id) {
        return toResponse(get(id));
    }

    public EntrenadorResponse create(EntrenadorRequest req) {
        Entrenador e = Entrenador.builder()
                .nombre(req.nombre().trim())
                .email(req.email())
                .telefono(req.telefono())
                .build();
        return toResponse(repo.save(e));
    }

    public EntrenadorResponse update(Long id, EntrenadorRequest req) {
        Entrenador e = get(id);
        e.setNombre(req.nombre().trim());
        e.setEmail(req.email());
        e.setTelefono(req.telefono());
        return toResponse(repo.save(e));
    }

    public void delete(Long id) {
        repo.delete(get(id));
    }

    private Entrenador get(Long id) {
        return repo.findById(id).orElseThrow(() -> new ResourceNotFoundException("Entrenador no encontrado: " + id));
    }

    private EntrenadorResponse toResponse(Entrenador e) {
        return new EntrenadorResponse(e.getId(), e.getNombre(), e.getEmail(), e.getTelefono());
    }
}
