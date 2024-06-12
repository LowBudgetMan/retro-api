package io.nickreuter.retroapi.retro.template;

public record Category (
    String name,
    int position,
    String lightBackgroundColor,
    String lightTextColor,
    String darkBackgroundColor,
    String darkTextColor
) {}
