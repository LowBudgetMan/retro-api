package io.nickreuter.retroapi.retro.anonymousparticipant;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class ShareTokenRepositoryTest {
        @Autowired
        private ShareTokenRepository subject;

        @Test
        public void findByToken_WhenShareTokenExists_ReturnsShareToken() {
            var expectedShareTokenString = "This is a share token";
            var expected = new ShareTokenEntity(1L, expectedShareTokenString, UUID.randomUUID());
            subject.save(new ShareTokenEntity(expected.getId(), expected.getToken(), expected.getRetroId()));
            var actual = subject.findByToken(expectedShareTokenString).orElseThrow();
            assertThat(actual).isEqualTo(expected);
        }

        @Test
        public void findByToken_WhenTokenDoesNotExist_ReturnsEmptyOptional() {
            subject.save(new ShareTokenEntity(1L, "something", UUID.randomUUID()));
            assertThat(subject.findByToken("something that does not exist")).isEmpty();
        }
}