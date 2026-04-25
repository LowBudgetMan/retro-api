package io.nickreuter.retroapi.retro.anonymousparticipant;

import io.nickreuter.retroapi.retro.RetroEntity;
import io.nickreuter.retroapi.retro.RetroRepository;
import io.nickreuter.retroapi.team.TeamEntity;
import io.nickreuter.retroapi.team.TeamRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class ShareTokenRepositoryTest {
    @Autowired
    private ShareTokenRepository subject;
    @Autowired
    private RetroRepository retroRepository;
    @Autowired
    private TeamRepository teamRepository;

    @Test
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    void deleteAllByRetroId_OpensItsOwnTransaction() {
        var team = teamRepository.save(new TeamEntity("test-team"));
        var retro = retroRepository.save(new RetroEntity(team.getId(), "template-1"));
        subject.save(new ShareTokenEntity("token-to-delete", retro.getId()));

        subject.deleteAllByRetroId(retro.getId());

        assertThat(subject.findAllByRetroId(retro.getId())).isEmpty();
    }
}
