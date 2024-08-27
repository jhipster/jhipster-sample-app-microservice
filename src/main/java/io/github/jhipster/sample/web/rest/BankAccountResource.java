package io.github.jhipster.sample.web.rest;

import io.github.jhipster.sample.domain.BankAccount;
import io.github.jhipster.sample.repository.BankAccountRepository;
import io.github.jhipster.sample.web.rest.errors.BadRequestAlertException;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import tech.jhipster.web.util.HeaderUtil;
import tech.jhipster.web.util.ResponseUtil;

/**
 * REST controller for managing {@link io.github.jhipster.sample.domain.BankAccount}.
 */
@RestController
@RequestMapping("/api/bank-accounts")
@Transactional
public class BankAccountResource {

    private static final Logger LOG = LoggerFactory.getLogger(BankAccountResource.class);

    private static final String ENTITY_NAME = "jhipsterSampleMicroserviceBankAccount";

    @Value("${jhipster.clientApp.name}")
    private String applicationName;

    private final BankAccountRepository bankAccountRepository;

    public BankAccountResource(BankAccountRepository bankAccountRepository) {
        this.bankAccountRepository = bankAccountRepository;
    }

    /**
     * {@code POST  /bank-accounts} : Create a new bankAccount.
     *
     * @param bankAccount the bankAccount to create.
     * @return the {@link ResponseEntity} with status {@code 201 (Created)} and with body the new bankAccount, or with status {@code 400 (Bad Request)} if the bankAccount has already an ID.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PostMapping("")
    public ResponseEntity<BankAccount> createBankAccount(@Valid @RequestBody BankAccount bankAccount) throws URISyntaxException {
        LOG.debug("REST request to save BankAccount : {}", bankAccount);
        if (bankAccount.getId() != null) {
            throw new BadRequestAlertException("A new bankAccount cannot already have an ID", ENTITY_NAME, "idexists");
        }
        bankAccount = bankAccountRepository.save(bankAccount);
        return ResponseEntity.created(new URI("/api/bank-accounts/" + bankAccount.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(applicationName, false, ENTITY_NAME, bankAccount.getId().toString()))
            .body(bankAccount);
    }

    /**
     * {@code PUT  /bank-accounts/:id} : Updates an existing bankAccount.
     *
     * @param id the id of the bankAccount to save.
     * @param bankAccount the bankAccount to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated bankAccount,
     * or with status {@code 400 (Bad Request)} if the bankAccount is not valid,
     * or with status {@code 500 (Internal Server Error)} if the bankAccount couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PutMapping("/{id}")
    public ResponseEntity<BankAccount> updateBankAccount(
        @PathVariable(value = "id", required = false) final Long id,
        @Valid @RequestBody BankAccount bankAccount
    ) throws URISyntaxException {
        LOG.debug("REST request to update BankAccount : {}, {}", id, bankAccount);
        if (bankAccount.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, bankAccount.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!bankAccountRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        bankAccount = bankAccountRepository.save(bankAccount);
        return ResponseEntity.ok()
            .headers(HeaderUtil.createEntityUpdateAlert(applicationName, false, ENTITY_NAME, bankAccount.getId().toString()))
            .body(bankAccount);
    }

    /**
     * {@code PATCH  /bank-accounts/:id} : Partial updates given fields of an existing bankAccount, field will ignore if it is null
     *
     * @param id the id of the bankAccount to save.
     * @param bankAccount the bankAccount to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated bankAccount,
     * or with status {@code 400 (Bad Request)} if the bankAccount is not valid,
     * or with status {@code 404 (Not Found)} if the bankAccount is not found,
     * or with status {@code 500 (Internal Server Error)} if the bankAccount couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PatchMapping(value = "/{id}", consumes = { "application/json", "application/merge-patch+json" })
    public ResponseEntity<BankAccount> partialUpdateBankAccount(
        @PathVariable(value = "id", required = false) final Long id,
        @NotNull @RequestBody BankAccount bankAccount
    ) throws URISyntaxException {
        LOG.debug("REST request to partial update BankAccount partially : {}, {}", id, bankAccount);
        if (bankAccount.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, bankAccount.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!bankAccountRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        Optional<BankAccount> result = bankAccountRepository
            .findById(bankAccount.getId())
            .map(existingBankAccount -> {
                if (bankAccount.getName() != null) {
                    existingBankAccount.setName(bankAccount.getName());
                }
                if (bankAccount.getBalance() != null) {
                    existingBankAccount.setBalance(bankAccount.getBalance());
                }

                return existingBankAccount;
            })
            .map(bankAccountRepository::save);

        return ResponseUtil.wrapOrNotFound(
            result,
            HeaderUtil.createEntityUpdateAlert(applicationName, false, ENTITY_NAME, bankAccount.getId().toString())
        );
    }

    /**
     * {@code GET  /bank-accounts} : get all the bankAccounts.
     *
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of bankAccounts in body.
     */
    @GetMapping("")
    public List<BankAccount> getAllBankAccounts() {
        LOG.debug("REST request to get all BankAccounts");
        return bankAccountRepository.findAll();
    }

    /**
     * {@code GET  /bank-accounts/:id} : get the "id" bankAccount.
     *
     * @param id the id of the bankAccount to retrieve.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the bankAccount, or with status {@code 404 (Not Found)}.
     */
    @GetMapping("/{id}")
    public ResponseEntity<BankAccount> getBankAccount(@PathVariable("id") Long id) {
        LOG.debug("REST request to get BankAccount : {}", id);
        Optional<BankAccount> bankAccount = bankAccountRepository.findById(id);
        return ResponseUtil.wrapOrNotFound(bankAccount);
    }

    /**
     * {@code DELETE  /bank-accounts/:id} : delete the "id" bankAccount.
     *
     * @param id the id of the bankAccount to delete.
     * @return the {@link ResponseEntity} with status {@code 204 (NO_CONTENT)}.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteBankAccount(@PathVariable("id") Long id) {
        LOG.debug("REST request to delete BankAccount : {}", id);
        bankAccountRepository.deleteById(id);
        return ResponseEntity.noContent()
            .headers(HeaderUtil.createEntityDeletionAlert(applicationName, false, ENTITY_NAME, id.toString()))
            .build();
    }
}
