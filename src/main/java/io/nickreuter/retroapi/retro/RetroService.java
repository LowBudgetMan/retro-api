package io.nickreuter.retroapi.retro;

import io.nickreuter.retroapi.notification.ActionType;
import io.nickreuter.retroapi.notification.event.RetroFinishedEvent;
import io.nickreuter.retroapi.retro.template.Template;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

@Service
public class RetroService {
    private final RetroRepository retroRepository;
    private final List<Template> templates;
    private final ApplicationEventPublisher applicationEventPublisher;

    public RetroService(RetroRepository retroRepository, List<Template> templates, ApplicationEventPublisher applicationEventPublisher) {
        this.retroRepository = retroRepository;
        this.templates = templates;
        this.applicationEventPublisher = applicationEventPublisher;
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
        var maybeRetroEntity = retroRepository.findById(retroId);
        return maybeRetroEntity.isEmpty() || findTemplateBytId(maybeRetroEntity.get().getTemplateId()).isEmpty()
            ? Optional.empty()
            : Optional.of(Retro.from(maybeRetroEntity.get(), findTemplateBytId(maybeRetroEntity.get().getTemplateId()).get()));
    }

    private Optional<Template> findTemplateBytId(String templateId) {
        return templates.stream().filter(template -> Objects.equals(template.id(), templateId)).findFirst();
    }

    public void setFinished(UUID retroId, boolean finished) throws RetroNotFoundException {
        var retro = retroRepository.findById(retroId).orElseThrow(RetroNotFoundException::new);
        retro.setFinished(finished);
        retroRepository.save(retro);
        applicationEventPublisher.publishEvent(new RetroFinishedEvent(this, ActionType.UPDATE, finished, retroId));
    }
}
