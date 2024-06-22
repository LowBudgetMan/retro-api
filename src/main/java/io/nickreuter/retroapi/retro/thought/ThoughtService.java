package io.nickreuter.retroapi.retro.thought;

import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class ThoughtService {
    private final ThoughtRepository thoughtRepository;

    public ThoughtService(ThoughtRepository thoughtRepository) {
        this.thoughtRepository = thoughtRepository;
    }

    public ThoughtEntity createThought(UUID retroId, String message, String category) {
        return thoughtRepository.save(ThoughtEntity.from(message, category, retroId));
    }
}
