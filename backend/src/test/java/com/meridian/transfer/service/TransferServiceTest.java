package com.meridian.transfer.service;

import com.meridian.transfer.dto.SendMoneyRequest;
import com.meridian.transfer.model.AccountType;
import com.meridian.transfer.model.AppUser;
import com.meridian.transfer.model.Country;
import com.meridian.transfer.model.Recipient;
import com.meridian.transfer.model.Transfer;
import com.meridian.transfer.model.TransferLimit;
import com.meridian.transfer.model.UserAccount;
import com.meridian.transfer.repository.AccountMovementRepository;
import com.meridian.transfer.repository.AppUserRepository;
import com.meridian.transfer.repository.CommissionRateRepository;
import com.meridian.transfer.repository.CountryRepository;
import com.meridian.transfer.repository.ReceptionModeLedgerEntryRepository;
import com.meridian.transfer.repository.ReceptionModeRepository;
import com.meridian.transfer.repository.RecipientRepository;
import com.meridian.transfer.repository.TransferLimitRepository;
import com.meridian.transfer.repository.TransferRepository;
import com.meridian.transfer.repository.UserAccountRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TransferServiceTest {

    private static final String OWNER = "demo";
    private static final Long CANADA_ID = 1L;
    private static final Long RECIPIENT_ID = 10L;
    private static final Long ACCOUNT_ID = 20L;

    @Mock private TransferRepository transferRepository;
    @Mock private RecipientRepository recipientRepository;
    @Mock private ExchangeRateService exchangeRateService;
    @Mock private TransferLimitRepository transferLimitRepository;
    @Mock private ReceptionModeRepository receptionModeRepository;
    @Mock private ReceptionModeLedgerEntryRepository receptionModeLedgerEntryRepository;
    @Mock private CommissionRateRepository commissionRateRepository;
    @Mock private FeeTierService feeTierService;
    @Mock private AppUserRepository appUserRepository;
    @Mock private CountryRepository countryRepository;
    @Mock private UserAccountRepository userAccountRepository;
    @Mock private AccountMovementRepository accountMovementRepository;
    @Mock private BankApiSimulatorService bankApiSimulatorService;

    private TransferService transferService;

    private Recipient recipient;
    private AppUser sender;
    private Country canada;
    private UserAccount depotAccount;
    private TransferLimit limits;

    @BeforeEach
    void setUp() {
        transferService = new TransferService(transferRepository, recipientRepository, exchangeRateService,
                transferLimitRepository, receptionModeRepository, receptionModeLedgerEntryRepository,
                commissionRateRepository, feeTierService, appUserRepository, countryRepository,
                userAccountRepository, accountMovementRepository, bankApiSimulatorService, new BigDecimal("5000.00"));

        canada = new Country();
        canada.setId(CANADA_ID);
        canada.setName("Canada");
        canada.setCurrencyCode("CAD");

        sender = new AppUser();
        sender.setId(1L);
        sender.setUsername(OWNER);
        sender.setCountry("Canada");

        recipient = new Recipient();
        recipient.setId(RECIPIENT_ID);
        recipient.setOwnerUsername(OWNER);
        recipient.setCurrencyCode("CAD");
        recipient.setName("Alexandre Roy");
        recipient.setDetail("RBC •••• 4471");

        depotAccount = new UserAccount();
        depotAccount.setId(ACCOUNT_ID);
        depotAccount.setType(AccountType.DEPOT);
        depotAccount.setBalance(new BigDecimal("1000.00"));
        depotAccount.setLabel("Compte dépôt Louly Express");

        limits = new TransferLimit();
        limits.setMinAmount(new BigDecimal("1"));
        limits.setMaxAmount(new BigDecimal("5000"));
        limits.setDailyMaxAmount(new BigDecimal("15000"));
        limits.setMonthlyMaxAmount(new BigDecimal("50000"));

        lenient().when(recipientRepository.findById(RECIPIENT_ID)).thenReturn(Optional.of(recipient));
        lenient().when(appUserRepository.findByUsername(OWNER)).thenReturn(Optional.of(sender));
        lenient().when(countryRepository.findByName("Canada")).thenReturn(Optional.of(canada));
        lenient().when(userAccountRepository.findByIdAndOwnerUser_Id(ACCOUNT_ID, 1L)).thenReturn(Optional.of(depotAccount));
        lenient().when(transferLimitRepository.findByCountryId(CANADA_ID)).thenReturn(Optional.of(limits));
        lenient().when(commissionRateRepository.findByType(any())).thenReturn(Optional.empty());
        lenient().when(transferRepository.sumAmountSentSince(any(), any())).thenReturn(BigDecimal.ZERO);
        lenient().when(transferRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
    }

    private SendMoneyRequest request(BigDecimal amount) {
        return new SendMoneyRequest(RECIPIENT_ID, ACCOUNT_ID, amount, new BigDecimal("100"),
                BigDecimal.ONE, new BigDecimal("5"));
    }

    @Test
    void createTransfer_throws_whenRecipientDoesNotExist() {
        when(recipientRepository.findById(RECIPIENT_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> transferService.createTransfer(request(new BigDecimal("100")), OWNER))
                .isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    void createTransfer_throws_whenRecipientBelongsToSomeoneElse() {
        recipient.setOwnerUsername("someone-else");

        assertThatThrownBy(() -> transferService.createTransfer(request(new BigDecimal("100")), OWNER))
                .isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    void createTransfer_throws_whenAmountIsBelowTheCountryMinimum() {
        limits.setMinAmount(new BigDecimal("50"));

        assertThatThrownBy(() -> transferService.createTransfer(request(new BigDecimal("10")), OWNER))
                .isInstanceOf(TransferLimitExceededException.class)
                .hasMessageContaining("at least");
    }

    @Test
    void createTransfer_throws_whenAmountExceedsTheCountryMaximum() {
        limits.setMaxAmount(new BigDecimal("500"));

        assertThatThrownBy(() -> transferService.createTransfer(request(new BigDecimal("501")), OWNER))
                .isInstanceOf(TransferLimitExceededException.class)
                .hasMessageContaining("cannot exceed");
    }

    @Test
    void createTransfer_throws_whenDailyCumulativeLimitWouldBeExceeded() {
        limits.setDailyMaxAmount(new BigDecimal("200"));
        when(transferRepository.sumAmountSentSince(any(), any())).thenReturn(new BigDecimal("150"));

        assertThatThrownBy(() -> transferService.createTransfer(request(new BigDecimal("100")), OWNER))
                .isInstanceOf(TransferLimitExceededException.class)
                .hasMessageContaining("daily limit");
    }

    @Test
    void createTransfer_throws_whenMonthlyCumulativeLimitWouldBeExceeded() {
        limits.setMonthlyMaxAmount(new BigDecimal("1000"));
        // First call inside createTransfer is the daily check, second is the monthly check —
        // both use sumAmountSentSince, so return a small daily figure but a large monthly one.
        when(transferRepository.sumAmountSentSince(any(), any()))
                .thenReturn(new BigDecimal("10"))
                .thenReturn(new BigDecimal("950"));

        assertThatThrownBy(() -> transferService.createTransfer(request(new BigDecimal("100")), OWNER))
                .isInstanceOf(TransferLimitExceededException.class)
                .hasMessageContaining("monthly limit");
    }

    @Test
    void createTransfer_throws_whenDepotBalanceIsInsufficient() {
        depotAccount.setBalance(new BigDecimal("50.00"));

        assertThatThrownBy(() -> transferService.createTransfer(request(new BigDecimal("100")), OWNER))
                .isInstanceOf(TransferLimitExceededException.class)
                .hasMessageContaining("Insufficient balance");
    }

    @Test
    void createTransfer_debitsTheDepotAccountByAmountPlusFee_onSuccess() {
        depotAccount.setBalance(new BigDecimal("1000.00"));

        Transfer result = transferService.createTransfer(request(new BigDecimal("100")), OWNER);

        // request.fee() = 5, so total charged/debited should be 100 + 5 = 105.
        assertThat(depotAccount.getBalance()).isEqualByComparingTo("895.00");
        assertThat(result.getTotalCharged()).isEqualByComparingTo("105");
        assertThat(result.getFee()).isEqualByComparingTo("5");
    }

    @Test
    void createTransfer_theTotalChargedFeeBecomesExactlyTheRecordedCommission() {
        // The core guarantee of the whole commission model: whatever the client is actually
        // charged as a fee is exactly what gets recorded as platform commission when there's no
        // third-party receiving reception mode involved (recipient.receivingReceptionMode is null here).
        Transfer result = transferService.createTransfer(request(new BigDecimal("100")), OWNER);

        assertThat(result.getPlatformCommissionAmount()).isEqualByComparingTo(result.getFee());
        assertThat(result.getPlatformCommissionCurrency()).isEqualTo("CAD");
    }

    @Test
    void createTransfer_declinesForACard_whenBankAuthorizationIsDeclined() {
        UserAccount card = new UserAccount();
        card.setId(ACCOUNT_ID);
        card.setType(AccountType.BANCAIRE);
        card.setBalance(new BigDecimal("1000.00"));
        when(userAccountRepository.findByIdAndOwnerUser_Id(ACCOUNT_ID, 1L)).thenReturn(Optional.of(card));
        when(bankApiSimulatorService.authorizeDebit(any(), any(), any()))
                .thenReturn(BankAuthorizationResult.decline("Insufficient funds reported by bank", BigDecimal.ZERO));

        assertThatThrownBy(() -> transferService.createTransfer(request(new BigDecimal("600")), OWNER))
                .isInstanceOf(TransferLimitExceededException.class)
                .hasMessageContaining("Bank declined");
    }

    @Test
    void createTransfer_succeedsForACard_whenBankAuthorizationApproves() {
        UserAccount card = new UserAccount();
        card.setId(ACCOUNT_ID);
        card.setType(AccountType.BANCAIRE);
        card.setCardNetwork("VISA");
        card.setCardLast4("1111");
        when(userAccountRepository.findByIdAndOwnerUser_Id(ACCOUNT_ID, 1L)).thenReturn(Optional.of(card));
        when(bankApiSimulatorService.authorizeDebit(any(), any(), any()))
                .thenReturn(BankAuthorizationResult.approve("BNK-DEMO0001", new BigDecimal("1000.00")));

        Transfer result = transferService.createTransfer(request(new BigDecimal("100")), OWNER);

        assertThat(result.getBankAuthorizationReference()).isEqualTo("BNK-DEMO0001");
        // Bank reported 1000.00 available; total charged is 105 (100 + fee of 5).
        assertThat(card.getBalance()).isEqualByComparingTo("895.00");
        assertThat(result.getSourceAccountLabel()).isEqualTo("Visa •••• 1111");
    }
}
