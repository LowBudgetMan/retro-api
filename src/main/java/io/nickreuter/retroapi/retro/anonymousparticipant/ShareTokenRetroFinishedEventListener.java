package io.nickreuter.retroapi.retro.anonymousparticipant;

import io.nickreuter.retroapi.notification.event.RetroFinishedEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;

@Service
@Order(200)
public class ShareTokenRetroFinishedEventListener implements ApplicationListener<RetroFinishedEvent> {
    private final ShareTokenService shareTokenService;

    public ShareTokenRetroFinishedEventListener(ShareTokenService shareTokenService) {
        this.shareTokenService = shareTokenService;
    }

    @Override
    public void onApplicationEvent(RetroFinishedEvent event) {
        if (event.isFinished()) shareTokenService.clearShareTokensForRetro(event.getRetroId());
    }
}
