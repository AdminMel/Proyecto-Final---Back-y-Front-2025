package com.upiiz.ligas_api.service;

import com.upiiz.ligas_api.dto.equipo.*;
import com.upiiz.ligas_api.entity.*;
import com.upiiz.ligas_api.exception.ResourceNotFoundException;
import com.upiiz.ligas_api.repository.*;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class EquipoService {

    private final EquipoRepository equipoRepo;
    private final LigaRepository ligaRepo;
    private final EntrenadorRepository entrenadorRepo;

    public EquipoService(EquipoRepository equipoRepo, LigaRepository ligaRepo, EntrenadorRepository entrenadorRepo) {
        this.equipoRepo = equipoRepo;
        this.ligaRepo = ligaRepo;
        this.entrenadorRepo = entrenadorRepo;
    }

    public List<EquipoResponse> findAll() {
        return equipoRepo.findAll().stream().map(this::toResponse).toList();
    }

    public EquipoResponse findById(Long id) {
        return toResponse(getEquipo(id));
    }

    public List<EquipoResponse> findByLiga(Long ligaId) {
        return equipoRepo.findByLigaId(ligaId).stream().map(this::toResponse).toList();
    }

    public EquipoResponse create(EquipoRequest req) {
        Equipo e = new Equipo();
        e.setNombre(req.nombre().trim());
        e.setPartidosGanados(0);

        if (req.ligaId() != null) {
            e.setLiga(getLiga(req.ligaId()));
        }
        if (req.entrenadorId() != null) {
            e.setEntrenador(getEntrenador(req.entrenadorId()));
        }

        return toResponse(equipoRepo.save(e));
    }

    public EquipoResponse update(Long id, EquipoRequest req) {
        Equipo e = getEquipo(id);
        e.setNombre(req.nombre().trim());

        if (req.ligaId() != null) e.setLiga(getLiga(req.ligaId()));
        else e.setLiga(null);

        if (req.entrenadorId() != null) e.setEntrenador(getEntrenador(req.entrenadorId()));
        else e.setEntrenador(null);

        return toResponse(equipoRepo.save(e));
    }

    public void delete(Long id) {
        equipoRepo.delete(getEquipo(id));
    }

    private Equipo getEquipo(Long id) {
        return equipoRepo.findById(id).orElseThrow(() -> new ResourceNotFoundException("Equipo no encontrado: " + id));
    }

    private Liga getLiga(Long id) {
        return ligaRepo.findById(id).orElseThrow(() -> new ResourceNotFoundException("Liga no encontrada: " + id));
    }

    private Entrenador getEntrenador(Long id) {
        return entrenadorRepo.findById(id).orElseThrow(() -> new ResourceNotFoundException("Entrenador no encontrado: " + id));
    }

    private EquipoResponse toResponse(Equipo e) {
        Long ligaId = e.getLiga() != null ? e.getLiga().getId() : null;
        String ligaNombre = e.getLiga() != null ? e.getLiga().getNombre() : null;

        Long entrenadorId = e.getEntrenador() != null ? e.getEntrenador().getId() : null;
        String entrenadorNombre = e.getEntrenador() != null ? e.getEntrenador().getNombre() : null;

        return new EquipoResponse(
                e.getId(),
                e.getNombre(),
                ligaId,
                ligaNombre,
                entrenadorId,
                entrenadorNombre,
                e.getPartidosGanados()
        );
    }
}
