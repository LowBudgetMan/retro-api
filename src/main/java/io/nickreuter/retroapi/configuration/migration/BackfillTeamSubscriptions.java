package io.nickreuter.retroapi.configuration.migration;

import liquibase.change.custom.CustomTaskChange;
import liquibase.database.Database;
import liquibase.database.jvm.JdbcConnection;
import liquibase.exception.CustomChangeException;
import liquibase.exception.ValidationErrors;
import liquibase.resource.ResourceAccessor;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Backfills a default subscription (FREE / ACTIVE) for every team that does not already have one.
 * UUIDs are generated in the JVM via {@link UUID#randomUUID()} to mirror the entities'
 * {@code @GeneratedValue} strategy, keeping this migration free of database-specific SQL functions.
 */
public class BackfillTeamSubscriptions implements CustomTaskChange {
    @Override
    public void execute(Database database) throws CustomChangeException {
        var connection = (JdbcConnection) database.getConnection();
        try {
            List<UUID> teamIds = new ArrayList<>();
            try (Statement statement = connection.createStatement();
                 ResultSet rs = statement.executeQuery(
                         "SELECT t.id FROM team t WHERE NOT EXISTS " +
                         "(SELECT 1 FROM team_subscription ts WHERE ts.team_id = t.id)")) {
                while (rs.next()) {
                    teamIds.add(UUID.fromString(rs.getString(1)));
                }
            }
            try (PreparedStatement ps = connection.prepareStatement(
                    "INSERT INTO team_subscription (id, team_id, plan, plan_status, created_at, modified_at) " +
                    "VALUES (?, ?, 'FREE', 'ACTIVE', ?, ?)")) {
                Timestamp now = Timestamp.from(Instant.now());
                for (UUID teamId : teamIds) {
                    ps.setObject(1, UUID.randomUUID());
                    ps.setObject(2, teamId);
                    ps.setTimestamp(3, now);
                    ps.setTimestamp(4, now);
                    ps.addBatch();
                }
                ps.executeBatch();
            }
        } catch (Exception e) {
            throw new CustomChangeException("Failed to backfill team subscriptions", e);
        }
    }

    @Override
    public String getConfirmationMessage() {
        return "Backfilled default subscriptions for existing teams";
    }

    @Override
    public void setUp() {
    }

    @Override
    public void setFileOpener(ResourceAccessor resourceAccessor) {
    }

    @Override
    public ValidationErrors validate(Database database) {
        return new ValidationErrors();
    }
}
