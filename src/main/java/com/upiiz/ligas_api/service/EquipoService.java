package com.upiiz.ligas_api.service;

import com.upiiz.ligas_api.dto.equipo.EquipoRequest;
import com.upiiz.ligas_api.dto.equipo.EquipoResponse;
import com.upiiz.ligas_api.entity.Entrenador;
import com.upiiz.ligas_api.entity.Equipo;
import com.upiiz.ligas_api.entity.Liga;
import com.upiiz.ligas_api.exception.ResourceNotFoundException;
import com.upiiz.ligas_api.repository.EntrenadorRepository;
import com.upiiz.ligas_api.repository.EquipoRepository;
import com.upiiz.ligas_api.repository.LigaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

    @Transactional(readOnly = true)
    public List<EquipoResponse> findAll() {
        return equipoRepo.findAllWithRefs().stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public EquipoResponse findById(Long id) {
        Equipo e = equipoRepo.findByIdWithRefs(id)
                .orElseThrow(() -> new ResourceNotFoundException("Equipo no encontrado: " + id));
        return toResponse(e);
    }

    @Transactional(readOnly = true)
    public List<EquipoResponse> findByLiga(Long ligaId) {
        return equipoRepo.findByLigaIdWithRefs(ligaId).stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public EquipoResponse create(EquipoRequest req) {
        Equipo e = new Equipo();
        e.setNombre(req.nombre().trim());
        e.setPartidosGanados(0);

        if (req.ligaId() != null) {
            e.setLiga(getLiga(req.ligaId()));
        } else {
            e.setLiga(null);
        }

        if (req.entrenadorId() != null) {
            e.setEntrenador(getEntrenador(req.entrenadorId()));
        } else {
            e.setEntrenador(null);
        }

        Equipo guardado = equipoRepo.save(e);

        // devolver con refs cargadas para evitar proxies
        Equipo conRefs = equipoRepo.findByIdWithRefs(guardado.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Equipo no encontrado: " + guardado.getId()));

        return toResponse(conRefs);
    }

    @Transactional
    public EquipoResponse update(Long id, EquipoRequest req) {
        // cargar con refs por consistencia (igual no truena si usas findById normal, pero así queda parejo)
        Equipo e = equipoRepo.findByIdWithRefs(id)
                .orElseThrow(() -> new ResourceNotFoundException("Equipo no encontrado: " + id));

        e.setNombre(req.nombre().trim());

        if (req.ligaId() != null) e.setLiga(getLiga(req.ligaId()));
        else e.setLiga(null);

        if (req.entrenadorId() != null) e.setEntrenador(getEntrenador(req.entrenadorId()));
        else e.setEntrenador(null);

        Equipo guardado = equipoRepo.save(e);

        Equipo conRefs = equipoRepo.findByIdWithRefs(guardado.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Equipo no encontrado: " + guardado.getId()));

        return toResponse(conRefs);
    }

    @Transactional
    public void delete(Long id) {
        Equipo e = equipoRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Equipo no encontrado: " + id));
        equipoRepo.delete(e);
    }

    private Liga getLiga(Long id) {
        return ligaRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Liga no encontrada: " + id));
    }

    private Entrenador getEntrenador(Long id) {
        return entrenadorRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Entrenador no encontrado: " + id));
    }

    private EquipoResponse toResponse(Equipo e) {
        Long ligaId = (e.getLiga() != null) ? e.getLiga().getId() : null;
        String ligaNombre = (e.getLiga() != null) ? e.getLiga().getNombre() : null;

        Long entrenadorId = (e.getEntrenador() != null) ? e.getEntrenador().getId() : null;
        String entrenadorNombre = (e.getEntrenador() != null) ? e.getEntrenador().getNombre() : null;

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
