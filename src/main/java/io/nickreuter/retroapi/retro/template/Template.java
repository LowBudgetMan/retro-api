package io.nickreuter.retroapi.retro.template;

import java.util.List;

public record Template(
        String id,
        String name,
        String description,
        List<Category> categories
) {}
