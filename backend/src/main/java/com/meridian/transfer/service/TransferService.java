package com.meridian.transfer.service;

import com.meridian.transfer.dto.SendMoneyRequest;
import com.meridian.transfer.model.AccountMovement;
import com.meridian.transfer.model.AccountType;
import com.meridian.transfer.model.AppUser;
import com.meridian.transfer.model.CommissionRate;
import com.meridian.transfer.model.Country;
import com.meridian.transfer.model.LedgerEntryType;
import com.meridian.transfer.model.MovementType;
import com.meridian.transfer.model.ReceptionMode;
import com.meridian.transfer.model.ReceptionModeLedgerEntry;
import com.meridian.transfer.model.Recipient;
import com.meridian.transfer.model.RecipientType;
import com.meridian.transfer.model.TransactionType;
import com.meridian.transfer.model.Transfer;
import com.meridian.transfer.model.TransferEvent;
import com.meridian.transfer.model.TransferEventType;
import com.meridian.transfer.model.TransferLimit;
import com.meridian.transfer.model.TransferStatus;
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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;

@Service
public class TransferService {

    private final TransferRepository transferRepository;
    private final RecipientRepository recipientRepository;
    private final ExchangeRateService exchangeRateService;
    private final TransferLimitRepository transferLimitRepository;
    private final ReceptionModeRepository receptionModeRepository;
    private final ReceptionModeLedgerEntryRepository receptionModeLedgerEntryRepository;
    private final CommissionRateRepository commissionRateRepository;
    private final FeeTierService feeTierService;
    private final AppUserRepository appUserRepository;
    private final CountryRepository countryRepository;
    private final UserAccountRepository userAccountRepository;
    private final AccountMovementRepository accountMovementRepository;
    private final BankApiSimulatorService bankApiSimulatorService;
    private final BigDecimal defaultMaxAmount;

    public TransferService(TransferRepository transferRepository,
                            RecipientRepository recipientRepository,
                            ExchangeRateService exchangeRateService,
                            TransferLimitRepository transferLimitRepository,
                            ReceptionModeRepository receptionModeRepository,
                            ReceptionModeLedgerEntryRepository receptionModeLedgerEntryRepository,
                            CommissionRateRepository commissionRateRepository,
                            FeeTierService feeTierService,
                            AppUserRepository appUserRepository,
                            CountryRepository countryRepository,
                            UserAccountRepository userAccountRepository,
                            AccountMovementRepository accountMovementRepository,
                            BankApiSimulatorService bankApiSimulatorService,
                            @Value("${transfer.default-max-amount}") BigDecimal defaultMaxAmount) {
        this.transferRepository = transferRepository;
        this.recipientRepository = recipientRepository;
        this.exchangeRateService = exchangeRateService;
        this.transferLimitRepository = transferLimitRepository;
        this.receptionModeRepository = receptionModeRepository;
        this.receptionModeLedgerEntryRepository = receptionModeLedgerEntryRepository;
        this.commissionRateRepository = commissionRateRepository;
        this.feeTierService = feeTierService;
        this.appUserRepository = appUserRepository;
        this.countryRepository = countryRepository;
        this.userAccountRepository = userAccountRepository;
        this.bankApiSimulatorService = bankApiSimulatorService;
        this.accountMovementRepository = accountMovementRepository;
        this.defaultMaxAmount = defaultMaxAmount;
    }

    /**
     * Current min/max per-transfer bounds for this country, read from the {@code transfer_limits}
     * table so customer service can change them any time via {@code AdminLimitController} — no
     * redeploy needed. If that country has no row yet (fresh install), seeds it from sensible
     * defaults. countryId null (e.g. account whose country couldn't be resolved) falls back to
     * Canada's row.
     */
    @Transactional
    public TransferLimit getLimits(Long countryId) {
        Long effectiveCountryId = countryId != null ? countryId : 1L;
        return transferLimitRepository.findByCountryId(effectiveCountryId)
                .orElseGet(() -> {
                    TransferLimit limit = new TransferLimit();
                    countryRepository.findById(effectiveCountryId).ifPresent(limit::setCountry);
                    limit.setMinAmount(BigDecimal.ONE);
                    limit.setMaxAmount(defaultMaxAmount);
                    limit.setDailyMaxAmount(defaultMaxAmount.multiply(new BigDecimal("3")));
                    limit.setMonthlyMaxAmount(defaultMaxAmount.multiply(new BigDecimal("10")));
                    limit.setUpdatedAt(Instant.now());
                    limit.setUpdatedBy("system-default");
                    return transferLimitRepository.save(limit);
                });
    }

    public BigDecimal getMinAmount(Long countryId) {
        return getLimits(countryId).getMinAmount();
    }

    public BigDecimal getMaxAmount(Long countryId) {
        return getLimits(countryId).getMaxAmount();
    }

    /** Customer-service update for one country's bounds — creates the row if it doesn't exist
     *  yet. Any bound can be omitted (null) to leave it unchanged. */
    @Transactional
    public void setLimits(Long countryId, BigDecimal minAmount, BigDecimal maxAmount,
                           BigDecimal dailyMaxAmount, BigDecimal monthlyMaxAmount, String updatedByUsername) {
        TransferLimit limit = transferLimitRepository.findByCountryId(countryId)
                .orElseGet(() -> {
                    TransferLimit fresh = new TransferLimit();
                    countryRepository.findById(countryId).ifPresent(fresh::setCountry);
                    return fresh;
                });
        if (minAmount != null) {
            limit.setMinAmount(minAmount);
        }
        if (maxAmount != null) {
            limit.setMaxAmount(maxAmount);
        }
        if (dailyMaxAmount != null) {
            limit.setDailyMaxAmount(dailyMaxAmount);
        }
        if (monthlyMaxAmount != null) {
            limit.setMonthlyMaxAmount(monthlyMaxAmount);
        }
        limit.setUpdatedAt(Instant.now());
        limit.setUpdatedBy(updatedByUsername);
        transferLimitRepository.save(limit);
    }

    @Transactional
    public Transfer createTransfer(SendMoneyRequest request, String ownerUsername) {
        Recipient recipient = recipientRepository.findById(request.recipientId())
                .filter(r -> ownerUsername.equals(r.getOwnerUsername()))
                .orElseThrow(() -> new EntityNotFoundException("Recipient not found: " + request.recipientId()));

        AppUser sender = appUserRepository.findByUsername(ownerUsername)
                .orElseThrow(() -> new EntityNotFoundException("User not found: " + ownerUsername));
        UserAccount sourceAccount = userAccountRepository.findByIdAndOwnerUser_Id(request.sourceAccountId(), sender.getId())
                .orElseThrow(() -> new EntityNotFoundException("Account not found: " + request.sourceAccountId()));

        Country senderCountry = resolveSenderCountry(ownerUsername);
        String sourceCurrency = senderCountry != null ? senderCountry.getCurrencyCode() : "CAD";
        Long senderCountryId = senderCountry != null ? senderCountry.getId() : null;
        boolean international = !sourceCurrency.equalsIgnoreCase(recipient.getCurrencyCode());

        TransferLimit limits = getLimits(senderCountryId);
        if (request.amount().compareTo(limits.getMinAmount()) < 0) {
            throw new TransferLimitExceededException(
                    "Amount must be at least " + limits.getMinAmount() + " " + sourceCurrency + " per transfer.");
        }
        if (request.amount().compareTo(limits.getMaxAmount()) > 0) {
            throw new TransferLimitExceededException(
                    "Amount cannot exceed " + limits.getMaxAmount() + " " + sourceCurrency + " per transfer.");
        }

        // Cumulative caps — everything the account has already sent in the period, plus this
        // transaction, checked against the daily/monthly ceiling. Per-transaction bounds above
        // don't catch someone sending many small transfers to add up past a sane daily/monthly
        // total. Null cap means nothing to enforce for that period.
        if (limits.getDailyMaxAmount() != null) {
            Instant startOfToday = Instant.now().truncatedTo(java.time.temporal.ChronoUnit.DAYS);
            BigDecimal sentToday = transferRepository.sumAmountSentSince(ownerUsername, startOfToday);
            if (sentToday.add(request.amount()).compareTo(limits.getDailyMaxAmount()) > 0) {
                throw new TransferLimitExceededException(
                        "This transfer would exceed your daily limit of " + limits.getDailyMaxAmount()
                                + " " + sourceCurrency + " (already sent " + sentToday + " " + sourceCurrency + " today).");
            }
        }
        if (limits.getMonthlyMaxAmount() != null) {
            Instant startOfMonth = Instant.now()
                    .atZone(java.time.ZoneOffset.UTC)
                    .toLocalDate()
                    .withDayOfMonth(1)
                    .atStartOfDay(java.time.ZoneOffset.UTC)
                    .toInstant();
            BigDecimal sentThisMonth = transferRepository.sumAmountSentSince(ownerUsername, startOfMonth);
            if (sentThisMonth.add(request.amount()).compareTo(limits.getMonthlyMaxAmount()) > 0) {
                throw new TransferLimitExceededException(
                        "This transfer would exceed your monthly limit of " + limits.getMonthlyMaxAmount()
                                + " " + sourceCurrency + " (already sent " + sentThisMonth + " " + sourceCurrency + " this month).");
            }
        }

        // Freeze whatever the person already saw and confirmed on screen. Only fall back to a
        // fresh lookup if the client didn't send a value (e.g. an older client) — never
        // re-derive these once the person has clicked "Review transfer", so a scheduled rate
        // refresh landing a second later can't create any inconsistency mid-transaction.
        BigDecimal rate = request.rate() != null
                ? request.rate()
                : exchangeRateService.rateFor(sourceCurrency, recipient.getCurrencyCode());
        BigDecimal fee = request.fee() != null
                ? request.fee()
                : feeTierService.feeFor(request.amount(), senderCountryId);
        BigDecimal amountReceived = request.amountReceived() != null
                ? request.amountReceived()
                : exchangeRateService.convert(request.amount(), rate, recipient.getCurrencyCode());
        BigDecimal total = request.amount().add(fee);
        String bankAuthorizationReference = null;

        if (sourceAccount.getType() == AccountType.DEPOT) {
            // Louly Express's own ledger — checked and debited directly, no external call needed.
            BigDecimal balance = sourceAccount.getBalance() != null ? sourceAccount.getBalance() : BigDecimal.ZERO;
            if (balance.compareTo(total) < 0) {
                throw new TransferLimitExceededException(
                        "Insufficient balance: your compte dépôt has " + balance + " " + sourceCurrency
                                + " but this transfer costs " + total + " " + sourceCurrency + ".");
            }
            sourceAccount.setBalance(balance.subtract(total));
            userAccountRepository.save(sourceAccount);
        } else {
            // An external bank owns this money — simulate calling their authorization API rather
            // than trusting a locally-held number. Only proceed if the "bank" approves.
            BankAuthorizationResult authorization = bankApiSimulatorService.authorizeDebit(sourceAccount, total, sourceCurrency);
            if (!authorization.approved()) {
                throw new TransferLimitExceededException(
                        "Bank declined this transfer: " + authorization.declineReason());
            }
            bankAuthorizationReference = authorization.referenceCode();
            sourceAccount.setBalance(authorization.reportedBalance().subtract(total));
            userAccountRepository.save(sourceAccount);
        }

        Transfer transfer = new Transfer();
        transfer.setRecipient(recipient);
        transfer.setMode(international ? RecipientType.INTERNATIONAL : RecipientType.NATIONAL);
        transfer.setAmountSent(request.amount());
        transfer.setAmountReceived(amountReceived);
        transfer.setSourceCurrency(sourceCurrency);
        transfer.setTargetCurrency(recipient.getCurrencyCode());
        transfer.setExchangeRate(rate);
        transfer.setFee(fee);
        transfer.setTotalCharged(total);
        transfer.setStatus(TransferStatus.SENT);
        transfer.setCreatedAt(Instant.now());
        transfer.setOwnerUsername(ownerUsername);
        transfer.setSourceAccountType(sourceAccount.getType());
        String sourceAccountLabel = sourceAccount.getType() == AccountType.DEPOT
                ? sourceAccount.getLabel()
                : com.meridian.transfer.dto.UserAccountDto.cardDisplayLabel(sourceAccount.getCardNetwork(), sourceAccount.getCardLast4());
        transfer.setSourceAccountLabel(sourceAccountLabel);
        transfer.setBankAuthorizationReference(bankAuthorizationReference);

        // Commission model: a total commission rate applies by transaction type (see
        // CommissionRate / /api/admin/commission-rates), split between Louly Express (platform
        // revenue) and the receiving receptionMode (e.g. Wave ou Orange), if there is one. Frozen onto
        // the transfer — a later change to a rate never rewrites what was actually earned on a
        // past transaction.
        applyCommission(transfer, recipient, international);

        transfer.getEvents().add(timelineEvent(transfer, TransferEventType.PAYMENT_CONFIRMED,
                "Payment confirmed", "Just now", false));
        if (international) {
            transfer.getEvents().add(timelineEvent(transfer, TransferEventType.CONVERTED,
                    "Converted to " + recipient.getCurrencyCode(), "Rate locked at " + rate, false));
        }
        String sentToLabel = recipient.getDetail() != null && !recipient.getDetail().isBlank()
                ? recipient.getDetail() : recipient.getName();
        transfer.getEvents().add(timelineEvent(transfer, TransferEventType.SENT,
                "Sent to " + sentToLabel, "In progress", false));
        transfer.getEvents().add(timelineEvent(transfer, TransferEventType.DELIVERED,
                "Delivered to " + recipient.getName(), "Pending", true));

        Transfer saved = transferRepository.save(transfer);
        recordReceivingReceptionModeLedgerEntry(saved, recipient);
        if (sourceAccount.getBalance() != null) {
            recordWithdrawalMovement(saved, sourceAccount, total);
        }
        return saved;
    }

    /** Records the debit against the movement history now that the transfer (and its ID) exists —
     *  the balance itself was already debited earlier in createTransfer(). */
    private void recordWithdrawalMovement(Transfer transfer, UserAccount account, BigDecimal amount) {
        AccountMovement movement = new AccountMovement();
        movement.setAccount(account);
        movement.setType(MovementType.WITHDRAWAL);
        movement.setAmount(amount);
        movement.setBalanceAfter(account.getBalance());
        movement.setRelatedTransfer(transfer);
        movement.setCreatedAt(Instant.now());
        movement.setNote("Transfer #" + transfer.getId());
        accountMovementRepository.save(movement);
    }

    /**
     * Louly Express doesn't owe itself, so this only applies to an actual third-party receiving
     * receptionMode (e.g. Wave ou Orange) who fronts the payout — it now owes them the principal they
     * advanced plus their commission, until customer service records a settlement.
     */
    private void recordReceivingReceptionModeLedgerEntry(Transfer transfer, Recipient recipient) {
        java.util.Optional.ofNullable(recipient.getReceivingReceptionMode()).ifPresent(receptionMode -> {
            if ("Louly Express".equals(receptionMode.getName())) {
                return;
            }
            BigDecimal commission = transfer.getReceivingReceptionModeCommissionAmount() != null
                    ? transfer.getReceivingReceptionModeCommissionAmount() : BigDecimal.ZERO;
            BigDecimal owed = transfer.getAmountReceived().add(commission);

            ReceptionModeLedgerEntry entry = new ReceptionModeLedgerEntry();
            entry.setReceptionMode(receptionMode);
            entry.setTransfer(transfer);
            entry.setType(LedgerEntryType.COMMISSION_OWED);
            entry.setAmount(owed);
            entry.setCurrency(transfer.getTargetCurrency());
            entry.setCreatedAt(Instant.now());
            entry.setNote("Transfer #" + transfer.getId() + ": principal " + transfer.getAmountReceived()
                    + " + commission " + commission);
            receptionModeLedgerEntryRepository.save(entry);
        });
    }

    /**
     * The total commission for a transaction is now exactly the fee actually charged to the
     * client (see FeeTierService — tiered by amount) — never a separately-computed percentage
     * that could drift from what was really collected. This is then split between platform
     * revenue (CAD) and the receiving reception mode's cut (in their own currency), using the
     * partner-share ratio for the detected transaction type; only if there's an actual
     * third-party receiving reception mode — otherwise Louly Express keeps the whole fee.
     * <p>
     * Type detection is a simplified heuristic for now: a "Cash Pickup" reception mode name maps
     * to CASH_OUT_AGENT, otherwise NATIONAL maps to P2P_LOCAL and INTERNATIONAL to
     * INTERNATIONAL_OUTBOUND_FX. MERCHANT_QR_PAYMENT and INTERNATIONAL_INBOUND aren't reachable
     * through this flow yet — their split ratios exist for when those flows are built.
     */
    private void applyCommission(Transfer transfer, Recipient recipient, boolean international) {
        TransactionType type = detectTransactionType(recipient, international);
        transfer.setTransactionType(type);

        // The fee (and therefore the total commission) is in the sender's own currency —
        // transfer.getSourceCurrency() — not necessarily CAD. Mislabeling it as CAD would corrupt
        // accounting for any non-Canadian sender.
        BigDecimal totalCommission = transfer.getFee() != null ? transfer.getFee() : BigDecimal.ZERO;
        Country senderCountry = resolveSenderCountry(transfer.getOwnerUsername());
        Long senderCountryId = senderCountry != null ? senderCountry.getId() : null;
        BigDecimal feeTierPercent = feeTierService.tierFor(transfer.getAmountSent(), senderCountryId)
                .map(t -> t.getFeePercent()).orElse(null);
        transfer.setCommissionRatePercent(feeTierPercent);

        CommissionRate rate = commissionRateRepository.findByType(type).orElse(null);
        BigDecimal partnerSharePercent = rate != null ? rate.getPartnerSharePercent() : BigDecimal.ZERO;

        boolean hasReceivingReceptionMode = recipient.getReceivingReceptionMode() != null
                && !"Louly Express".equals(recipient.getReceivingReceptionMode().getName());

        BigDecimal receptionModeShareInSenderCurrency = hasReceivingReceptionMode
                ? totalCommission.multiply(partnerSharePercent).divide(new BigDecimal("100"), 4, java.math.RoundingMode.HALF_UP)
                : BigDecimal.ZERO;
        BigDecimal platformShareInSenderCurrency = totalCommission.subtract(receptionModeShareInSenderCurrency);

        transfer.setPlatformCommissionAmount(platformShareInSenderCurrency);
        transfer.setPlatformCommissionCurrency(transfer.getSourceCurrency());

        if (hasReceivingReceptionMode) {
            // transfer.getExchangeRate() converts 1 unit of sourceCurrency into targetCurrency,
            // so this correctly lands the reception mode's share in their own local currency
            // regardless of what currency the fee itself was collected in.
            BigDecimal receptionModeShareLocal = receptionModeShareInSenderCurrency.multiply(transfer.getExchangeRate());
            transfer.setReceivingReceptionModeCommissionAmount(receptionModeShareLocal);
            transfer.setReceivingReceptionModeCommissionCurrency(transfer.getTargetCurrency());
        }
    }

    /**
     * The sender's own account country — resolves both their currency and, separately, which
     * country's fee-tier grid applies. Null if it can't be resolved (shouldn't normally happen,
     * since registration validates the country against the table); callers fall back to CAD.
     */
    private Country resolveSenderCountry(String ownerUsername) {
        return appUserRepository.findByUsername(ownerUsername)
                .map(AppUser::getCountry)
                .flatMap(countryRepository::findByName)
                .orElse(null);
    }

    private TransactionType detectTransactionType(Recipient recipient, boolean international) {
        if (recipient.getReceptionModeName() != null && recipient.getReceptionModeName().toLowerCase().contains("cash pickup")) {
            return TransactionType.CASH_OUT_AGENT;
        }
        return international ? TransactionType.INTERNATIONAL_OUTBOUND_FX : TransactionType.P2P_LOCAL;
    }

    private TransferEvent timelineEvent(Transfer transfer, TransferEventType type, String title, String subtitle, boolean pending) {
        TransferEvent event = new TransferEvent();
        event.setTransfer(transfer);
        event.setType(type);
        event.setTitle(title);
        event.setSubtitle(subtitle);
        event.setOccurredAt(Instant.now());
        event.setPending(pending);
        return event;
    }
}
