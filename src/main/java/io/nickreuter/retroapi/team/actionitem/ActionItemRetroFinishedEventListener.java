package io.nickreuter.retroapi.team.actionitem;

import io.nickreuter.retroapi.notification.event.RetroFinishedEvent;
import io.nickreuter.retroapi.retro.RetroService;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Service;

@Service
public class ActionItemRetroFinishedEventListener implements ApplicationListener<RetroFinishedEvent> {
    private final RetroService retroService;
    private final ActionItemService actionItemService;

    public ActionItemRetroFinishedEventListener(RetroService retroService, ActionItemService actionItemService) {
        this.retroService = retroService;
        this.actionItemService = actionItemService;
    }

    @Override
    public void onApplicationEvent(RetroFinishedEvent event) {
        if (event.isFinished()) {
            var retro = retroService.getRetro(event.getRetroId()).orElseThrow();
            actionItemService.archiveCompletedActionItems(retro.teamId());
        }
    }
}
