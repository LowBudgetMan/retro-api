package io.nickreuter.retroapi.retro.anonymousparticipant;

import java.util.UUID;

public record ShareToken (
        UUID id,
        String token,
        UUID retroId
){}
