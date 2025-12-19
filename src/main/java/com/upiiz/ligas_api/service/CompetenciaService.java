package com.upiiz.ligas_api.service;

import com.upiiz.ligas_api.dto.competencia.*;
import com.upiiz.ligas_api.entity.*;
import com.upiiz.ligas_api.exception.BadRequestException;
import com.upiiz.ligas_api.exception.ResourceNotFoundException;
import com.upiiz.ligas_api.repository.*;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CompetenciaService {

    private final CompetenciaRepository compRepo;
    private final LigaRepository ligaRepo;
    private final EquipoRepository equipoRepo;

    public CompetenciaService(CompetenciaRepository compRepo, LigaRepository ligaRepo, EquipoRepository equipoRepo) {
        this.compRepo = compRepo;
        this.ligaRepo = ligaRepo;
        this.equipoRepo = equipoRepo;
    }

    public List<CompetenciaResponse> findAll() {
        return compRepo.findAll().stream().map(this::toResponse).toList();
    }

    public CompetenciaResponse findById(Long id) {
        return toResponse(get(id));
    }

    public CompetenciaResponse create(CompetenciaRequest req) {
        if (req.equipoLocalId().equals(req.equipoVisitanteId())) {
            throw new BadRequestException("El equipo local y visitante no pueden ser el mismo");
        }

        Equipo local = getEquipo(req.equipoLocalId());
        Equipo visitante = getEquipo(req.equipoVisitanteId());

        // Si viene ligaId, validamos. Si no, tomamos la liga del local si existe.
        Liga liga = null;
        if (req.ligaId() != null) {
            liga = ligaRepo.findById(req.ligaId())
                    .orElseThrow(() -> new ResourceNotFoundException("Liga no encontrada: " + req.ligaId()));
        } else if (local.getLiga() != null) {
            liga = local.getLiga();
        }

        // Regla útil: si hay liga, local y visitante deben pertenecer a la misma liga (si están asignados).
        if (liga != null) {
            if (local.getLiga() != null && !local.getLiga().getId().equals(liga.getId()))
                throw new BadRequestException("El equipo local no pertenece a la liga indicada");
            if (visitante.getLiga() != null && !visitante.getLiga().getId().equals(liga.getId()))
                throw new BadRequestException("El equipo visitante no pertenece a la liga indicada");
        }

        Competencia c = Competencia.builder()
                .fecha(req.fecha())
                .liga(liga)
                .equipoLocal(local)
                .equipoVisitante(visitante)
                .finalizado(false)
                .build();

        return toResponse(compRepo.save(c));
    }

    @Transactional
    public CompetenciaResponse updateResultado(Long id, CompetenciaResultRequest req) {
        Competencia c = get(id);

        c.setGolesLocal(req.golesLocal());
        c.setGolesVisitante(req.golesVisitante());
        c.setFinalizado(true);

        // Actualiza ganados (si empate, nadie suma)
        if (req.golesLocal() > req.golesVisitante()) {
            Equipo local = c.getEquipoLocal();
            local.setPartidosGanados(local.getPartidosGanados() + 1);
            equipoRepo.save(local);
        } else if (req.golesVisitante() > req.golesLocal()) {
            Equipo vis = c.getEquipoVisitante();
            vis.setPartidosGanados(vis.getPartidosGanados() + 1);
            equipoRepo.save(vis);
        }

        return toResponse(compRepo.save(c));
    }

    public void delete(Long id) {
        compRepo.delete(get(id));
    }

    private Competencia get(Long id) {
        return compRepo.findById(id).orElseThrow(() -> new ResourceNotFoundException("Competencia no encontrada: " + id));
    }

    private Equipo getEquipo(Long id) {
        return equipoRepo.findById(id).orElseThrow(() -> new ResourceNotFoundException("Equipo no encontrado: " + id));
    }

    private CompetenciaResponse toResponse(Competencia c) {
        Long ligaId = c.getLiga() != null ? c.getLiga().getId() : null;
        String ligaNombre = c.getLiga() != null ? c.getLiga().getNombre() : null;

        return new CompetenciaResponse(
                c.getId(),
                c.getFecha(),
                ligaId,
                ligaNombre,
                c.getEquipoLocal().getId(),
                c.getEquipoLocal().getNombre(),
                c.getEquipoVisitante().getId(),
                c.getEquipoVisitante().getNombre(),
                c.getGolesLocal(),
                c.getGolesVisitante(),
                c.getFinalizado()
        );
    }
}
