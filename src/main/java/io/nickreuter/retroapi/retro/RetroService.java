package io.nickreuter.retroapi.retro;

import io.nickreuter.retroapi.retro.template.Template;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class RetroService {
    private final RetroRepository retroRepository;
    private final List<Template> templates;

    public RetroService(RetroRepository retroRepository, List<Template> templates) {
        this.retroRepository = retroRepository;
        this.templates = templates;
    }

    public RetroEntity createRetro(UUID teamId, Integer retroTemplateId) throws InvalidTemplateIdException{
        if(!isValidTemplate(retroTemplateId)) throw new InvalidTemplateIdException();
        return retroRepository.save(new RetroEntity(teamId, retroTemplateId));
    }

    private boolean isValidTemplate(Integer retroTemplateId) {
        if(retroTemplateId == null) return false;
        return templates.stream().anyMatch(template -> template.id() == retroTemplateId);
    }

    public List<RetroEntity> getRetros(UUID teamId) {
        return retroRepository.findAllByTeamIdOrderByCreatedAtDesc(teamId);
    }

    public Optional<RetroEntity> getRetro(UUID retroId) {
        return retroRepository.findById(retroId);
    }
}
