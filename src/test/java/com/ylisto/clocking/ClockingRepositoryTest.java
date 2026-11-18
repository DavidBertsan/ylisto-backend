package com.ylisto.clocking;


import com.ylisto.TestcontainersConfiguration;

import com.ylisto.business.Business;
import com.ylisto.user.Role;
import com.ylisto.user.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import org.springframework.context.annotation.Import;

import java.time.Instant;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Import(TestcontainersConfiguration.class)
 class ClockingRepositoryTest {

    @Autowired
    ClockingRepository clockingRepository;
    @Autowired
    TestEntityManager entityManager;

    @Test
    void shouldReturnOnlyOpenClocking() {
        Business business= new Business("Panaderia Felipe","Panaderia");
        User user=new User("AnnaReal","1234",business,"Anna","60000000", Role.EMPLOYEE,"https://xxxxx");
        Instant yesterday = Instant.parse("2026-11-17T08:00:00Z");
        Instant today     = Instant.parse("2026-11-18T08:00:00Z");

        Clocking closed = new Clocking(user, yesterday);
        closed.clockOut(yesterday.plusSeconds(8 * 3600));

        Clocking open = new Clocking(user, today);
        entityManager.persist(business);
        entityManager.persist(user);
        entityManager.persist(closed);
        entityManager.persist(open);
        Optional<Clocking> result=clockingRepository.findByUserIdAndClockOutIsNull(user.getId());
        assertThat(result).contains(open);
    }
}
