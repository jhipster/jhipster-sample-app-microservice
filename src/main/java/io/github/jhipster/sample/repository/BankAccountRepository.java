package io.github.jhipster.sample.repository;

import io.github.jhipster.sample.domain.BankAccount;

import org.springframework.data.jpa.repository.*;

import java.util.List;

/**
 * Spring Data JPA repository for the BankAccount entity.
 */
@SuppressWarnings("unused")
public interface BankAccountRepository extends JpaRepository<BankAccount,Long> {

}
