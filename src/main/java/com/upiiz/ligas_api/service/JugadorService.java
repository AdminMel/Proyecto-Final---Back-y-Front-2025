package com.upiiz.ligas_api.service;

import com.upiiz.ligas_api.dto.jugador.*;
import com.upiiz.ligas_api.entity.*;
import com.upiiz.ligas_api.exception.ResourceNotFoundException;
import com.upiiz.ligas_api.repository.*;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class JugadorService {

    private final JugadorRepository jugadorRepo;
    private final EquipoRepository equipoRepo;

    public JugadorService(JugadorRepository jugadorRepo, EquipoRepository equipoRepo) {
        this.jugadorRepo = jugadorRepo;
        this.equipoRepo = equipoRepo;
    }

    public List<JugadorResponse> findAll() {
        return jugadorRepo.findAll().stream().map(this::toResponse).toList();
    }

    public JugadorResponse findById(Long id) {
        return toResponse(getJugador(id));
    }

    public List<JugadorResponse> findByEquipo(Long equipoId) {
        return jugadorRepo.findByEquipoId(equipoId).stream().map(this::toResponse).toList();
    }

    public JugadorResponse create(JugadorRequest req) {
        Jugador j = new Jugador();
        j.setNombre(req.nombre().trim());
        j.setEdad(req.edad());
        j.setPosicion(req.posicion());

        if (req.equipoId() != null) {
            j.setEquipo(getEquipo(req.equipoId()));
        }

        return toResponse(jugadorRepo.save(j));
    }

    public JugadorResponse update(Long id, JugadorRequest req) {
        Jugador j = getJugador(id);
        j.setNombre(req.nombre().trim());
        j.setEdad(req.edad());
        j.setPosicion(req.posicion());

        if (req.equipoId() != null) j.setEquipo(getEquipo(req.equipoId()));
        else j.setEquipo(null);

        return toResponse(jugadorRepo.save(j));
    }

    public void delete(Long id) {
        jugadorRepo.delete(getJugador(id));
    }

    private Jugador getJugador(Long id) {
        return jugadorRepo.findById(id).orElseThrow(() -> new ResourceNotFoundException("Jugador no encontrado: " + id));
    }

    private Equipo getEquipo(Long id) {
        return equipoRepo.findById(id).orElseThrow(() -> new ResourceNotFoundException("Equipo no encontrado: " + id));
    }

    private JugadorResponse toResponse(Jugador j) {
        Long equipoId = j.getEquipo() != null ? j.getEquipo().getId() : null;
        String equipoNombre = j.getEquipo() != null ? j.getEquipo().getNombre() : null;

        return new JugadorResponse(j.getId(), j.getNombre(), j.getEdad(), j.getPosicion(), equipoId, equipoNombre);
    }
}
