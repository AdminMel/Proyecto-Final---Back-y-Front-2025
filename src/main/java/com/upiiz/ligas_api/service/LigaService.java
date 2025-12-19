package com.upiiz.ligas_api.service;

import com.upiiz.ligas_api.dto.liga.*;
import com.upiiz.ligas_api.entity.Liga;
import com.upiiz.ligas_api.exception.ResourceNotFoundException;
import com.upiiz.ligas_api.repository.LigaRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class LigaService {

    private final LigaRepository repo;

    public LigaService(LigaRepository repo) {
        this.repo = repo;
    }

    public List<LigaResponse> findAll() {
        return repo.findAll().stream().map(this::toResponse).toList();
    }

    public LigaResponse findById(Long id) {
        return toResponse(getLiga(id));
    }

    public LigaResponse create(LigaRequest req) {
        Liga l = Liga.builder()
                .nombre(req.nombre().trim())
                .descripcion(req.descripcion())
                .build();
        return toResponse(repo.save(l));
    }

    public LigaResponse update(Long id, LigaRequest req) {
        Liga l = getLiga(id);
        l.setNombre(req.nombre().trim());
        l.setDescripcion(req.descripcion());
        return toResponse(repo.save(l));
    }

    public void delete(Long id) {
        Liga l = getLiga(id);
        repo.delete(l);
    }

    private Liga getLiga(Long id) {
        return repo.findById(id).orElseThrow(() -> new ResourceNotFoundException("Liga no encontrada: " + id));
    }

    private LigaResponse toResponse(Liga l) {
        return new LigaResponse(l.getId(), l.getNombre(), l.getDescripcion());
    }
}
