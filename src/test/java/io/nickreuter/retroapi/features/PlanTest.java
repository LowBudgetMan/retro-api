package io.nickreuter.retroapi.features;

import io.nickreuter.retroapi.team.subscription.Plan;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static io.nickreuter.retroapi.team.subscription.Feature.*;
import static org.assertj.core.api.Assertions.assertThat;

class PlanTest {

    @Test
    void grants_WhenFeatureNotInSet_ReturnsFalse() {
        assertThat(Plan.FREE.grants(HISTORICAL_RETROS)).isFalse();
    }

    @Test
    void grants_WhenFeatureIsInSet_ReturnsTrue() {
        assertThat(Plan.ENTERPRISE.grants(HISTORICAL_RETROS)).isTrue();
    }

    @Test
    void features_ReturnsListOfFeaturesForPlan() {
        assertThat(Plan.ENTERPRISE.features()).isEqualTo(Set.of(values()));
    }

    @Test
    void features_ReturnsImmutableSetOfFeatures() {
        var modifiedSet = Plan.PRO.features();
        modifiedSet.add(CUSTOM_TEMPLATES);
        assertThat(modifiedSet).isNotEqualTo(Plan.PRO.features());
    }
}
