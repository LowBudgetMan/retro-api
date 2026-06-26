package io.nickreuter.retroapi.retro;

import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class RetroActiveGuard {
    private final RetroRepository retroRepository;

    public RetroActiveGuard(RetroRepository retroRepository) {
        this.retroRepository = retroRepository;
    }

    public boolean isValid(UUID retroId) throws RetroNotFoundException {
        return !retroRepository.findById(retroId).orElseThrow(RetroNotFoundException::new).isFinished();
    }
}
