package io.nickreuter.retroapi.team.subscription;

import java.util.HashSet;
import java.util.Set;

import static io.nickreuter.retroapi.team.subscription.Feature.*;

public enum Plan {
    FREE(Set.of()),
    PRO(Set.of(HISTORICAL_RETROS, API_ACCESS, WEBHOOKS)),
    ENTERPRISE(Set.of(Feature.values()));

    private final Set<Feature> featureSet;

    Plan(Set<Feature> featureSet) {
        this.featureSet = featureSet;
    }

    public Set<Feature> features() {
        return new HashSet<>(featureSet);
    }

    public boolean grants(Feature feature) {
        return featureSet.contains(feature);
    }
}
