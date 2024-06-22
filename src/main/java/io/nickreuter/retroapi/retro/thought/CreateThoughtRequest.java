package io.nickreuter.retroapi.retro.thought;

public record CreateThoughtRequest (
   String message,
   String category
) {}
