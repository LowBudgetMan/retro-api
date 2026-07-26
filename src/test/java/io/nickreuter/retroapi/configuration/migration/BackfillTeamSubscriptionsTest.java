package io.nickreuter.retroapi.configuration.migration;

import io.nickreuter.retroapi.features.Plan;
import io.nickreuter.retroapi.features.PlanStatus;
import io.nickreuter.retroapi.features.TeamSubscriptionEntity;
import io.nickreuter.retroapi.features.TeamSubscriptionRepository;
import io.nickreuter.retroapi.team.TeamEntity;
import io.nickreuter.retroapi.team.TeamRepository;
import jakarta.persistence.EntityManager;
import liquibase.database.DatabaseFactory;
import liquibase.database.jvm.JdbcConnection;
import org.hibernate.Session;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class BackfillTeamSubscriptionsTest {
    @Autowired
    private TeamRepository teamRepository;
    @Autowired
    private TeamSubscriptionRepository subscriptionRepository;
    @Autowired
    private EntityManager entityManager;

    @Test
    void execute_CreatesDefaultSubscriptionForEachTeamWithoutOne() {
        var team1 = teamRepository.save(new TeamEntity("A Team"));
        var team2 = teamRepository.save(new TeamEntity("B Team"));
        entityManager.flush();

        runBackfill();

        var subscriptions = subscriptionRepository.findAll();
        assertThat(subscriptions).hasSize(2);
        assertThat(subscriptions).allSatisfy(subscription -> {
            assertThat(subscription.getId()).isNotNull();
            assertThat(subscription.getPlan()).isEqualTo(Plan.FREE);
            assertThat(subscription.getPlanStatus()).isEqualTo(PlanStatus.ACTIVE);
            assertThat(subscription.getCreatedAt()).isNotNull();
        });
        assertThat(subscriptions.stream().map(TeamSubscriptionEntity::getTeamId))
                .containsExactlyInAnyOrder(team1.getId(), team2.getId());
    }

    @Test
    void execute_SkipsTeamsThatAlreadyHaveASubscription() {
        var existingTeam = teamRepository.save(new TeamEntity("Existing"));
        subscriptionRepository.save(new TeamSubscriptionEntity(null, existingTeam.getId(), Plan.PRO, PlanStatus.ACTIVE, null, null));
        var newTeam = teamRepository.save(new TeamEntity("New"));
        entityManager.flush();

        runBackfill();

        var subscriptions = subscriptionRepository.findAll();
        assertThat(subscriptions).hasSize(2);
        assertThat(subscriptionRepository.findByTeamId(existingTeam.getId()).orElseThrow().getPlan())
                .isEqualTo(Plan.PRO);
        assertThat(subscriptionRepository.findByTeamId(newTeam.getId()).orElseThrow().getPlan())
                .isEqualTo(Plan.FREE);
    }

    @Test
    void execute_IsIdempotentWhenRunMultipleTimes() {
        teamRepository.save(new TeamEntity("A Team"));
        entityManager.flush();

        runBackfill();
        runBackfill();

        assertThat(subscriptionRepository.findAll()).hasSize(1);
    }

    private void runBackfill() {
        entityManager.unwrap(Session.class).doWork(connection -> {
            try {
                var database = DatabaseFactory.getInstance()
                        .findCorrectDatabaseImplementation(new JdbcConnection(connection));
                new BackfillTeamSubscriptions().execute(database);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
        entityManager.clear();
    }
}
