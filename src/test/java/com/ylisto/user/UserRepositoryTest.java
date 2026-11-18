package com.ylisto.user;

import com.ylisto.TestcontainersConfiguration;
import com.ylisto.business.Business;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import org.springframework.context.annotation.Import;


import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Import(TestcontainersConfiguration.class)
class UserRepositoryTest {

    @Autowired
    UserRepository userRepository;
    @Autowired
    TestEntityManager entityManager;

    @Test
    void shouldReturnTrueWhenUsernameExists() {
        // Given: persist a Business, then a User that belongs to it
           Business business = new Business("Panaderia manolo","Panaderia");

            User user=new User("DavidBS","123",business,"David","60000000",Role.EMPLOYEE,"https://xxxx");
        entityManager.persist(business);
        entityManager.persist(user);
            // When: existsByUsername("ana")
        boolean exists = userRepository.existsByUsername("DavidBS");
        // Then: assertThat(...).isTrue()
        assertThat(exists).isTrue();
    }


    @Test
    void shouldReturnFalseWhenUsernameDoesNotExist() {
        Business business = new Business("Panaderia manolo","Panaderia");

        User user=new User("DavidToronto","123",business,"David","60000000",Role.EMPLOYEE,"https://xxxx");
        entityManager.persist(business);
        entityManager.persist(user);
        boolean exists = userRepository.existsByUsername("Anna");
        assertThat(exists).isFalse();
    }
}
