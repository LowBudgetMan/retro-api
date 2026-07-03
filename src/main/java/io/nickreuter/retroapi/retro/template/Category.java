package io.nickreuter.retroapi.retro.template;

public record Category (
    String name,
    String description,
    int position,
    String lightBackgroundColor,
    String lightTextColor,
    String darkBackgroundColor,
    String darkTextColor
) {}
