package io.nickreuter.retroapi.retro;

import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class RetroService {
    private final RetroRepository retroRepository;

    public RetroService(RetroRepository retroRepository) {
        this.retroRepository = retroRepository;
    }

    public RetroEntity createRetro(UUID teamId) {
        return retroRepository.save(new RetroEntity(teamId));
    }

    public List<RetroEntity> getRetros(UUID teamId) {
        return retroRepository.findAllByTeamIdOrderByCreatedAtDesc(teamId);
    }

    public Optional<RetroEntity> getRetro(UUID retroId) {
        return retroRepository.findById(retroId);
    }
}
