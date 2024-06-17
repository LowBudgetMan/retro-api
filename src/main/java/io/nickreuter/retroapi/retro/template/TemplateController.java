package io.nickreuter.retroapi.retro.template;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/templates")
public class TemplateController {
    private final List<Template> templates;

    public TemplateController(List<Template> templates) {
        this.templates = templates;
    }

    @GetMapping
    public List<Template> getTemplates() {
        return templates;
    }
}
