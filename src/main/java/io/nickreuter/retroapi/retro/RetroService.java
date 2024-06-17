package io.nickreuter.retroapi.retro;

import io.nickreuter.retroapi.retro.template.Template;
import io.nickreuter.retroapi.retro.template.exception.TemplateNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
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

    public RetroEntity createRetro(UUID teamId, String retroTemplateId) throws InvalidTemplateIdException{
        if(!isValidTemplate(retroTemplateId)) throw new InvalidTemplateIdException();
        return retroRepository.save(new RetroEntity(teamId, retroTemplateId));
    }

    private boolean isValidTemplate(String retroTemplateId) {
        if(retroTemplateId == null) return false;
        return templates.stream().anyMatch(template -> Objects.equals(template.id(), retroTemplateId));
    }

    public List<RetroEntity> getRetros(UUID teamId) {
        return retroRepository.findAllByTeamIdOrderByCreatedAtDesc(teamId);
    }

    public Optional<Retro> getRetro(UUID retroId) {
        Optional<Retro> maybeRetro;
        var maybeRetroEntity = retroRepository.findById(retroId);
        if(maybeRetroEntity.isEmpty()) {
            maybeRetro = Optional.empty();
        } else {
            try {
                maybeRetro = Optional.of(Retro.from(maybeRetroEntity.get(), findTemplateBytId(maybeRetroEntity.get().getTemplateId()).orElseThrow(TemplateNotFoundException::new)));
            } catch (TemplateNotFoundException e) {
                maybeRetro = Optional.empty();
            }
        }
        return maybeRetro;
    }

    private Optional<Template> findTemplateBytId(String templateId) {
        return templates.stream().filter(template -> Objects.equals(template.id(), templateId)).findFirst();
    }
}
