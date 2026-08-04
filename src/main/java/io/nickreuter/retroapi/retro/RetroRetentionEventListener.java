package io.nickreuter.retroapi.retro;

import io.nickreuter.retroapi.notification.event.RetroFinishedEvent;
import io.nickreuter.retroapi.team.subscription.Feature;
import io.nickreuter.retroapi.team.subscription.FeatureGuard;
import org.springframework.context.ApplicationListener;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;

@Service
@Order(1000)
public class RetroRetentionEventListener implements ApplicationListener<RetroFinishedEvent> {
    private final RetroService retroService;
    private final FeatureGuard featureGuard;

    public RetroRetentionEventListener(RetroService retroService, FeatureGuard featureGuard) {
        this.retroService = retroService;
        this.featureGuard = featureGuard;
    }

    @Override
    public void onApplicationEvent(RetroFinishedEvent event) {
        if (!event.isFinished()) return;
        var retro = retroService.getRetro(event.getRetroId()).orElseThrow();
        if (!featureGuard.hasFeature(retro.teamId(), Feature.HISTORICAL_RETROS)) {
            retroService.deleteRetro(event.getRetroId());
        }
    }
}
