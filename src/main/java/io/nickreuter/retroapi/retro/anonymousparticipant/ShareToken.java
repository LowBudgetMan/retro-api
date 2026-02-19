package io.nickreuter.retroapi.retro.anonymousparticipant;

import java.util.UUID;

public record ShareToken (
        long id,
        String token,
        UUID retroId
){}
