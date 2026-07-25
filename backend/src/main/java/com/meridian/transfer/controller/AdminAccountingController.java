package com.meridian.transfer.controller;

import com.meridian.transfer.dto.AccountingSummaryDto;
import com.meridian.transfer.dto.LedgerEntryDto;
import com.meridian.transfer.dto.ReceptionModeBalanceDto;
import com.meridian.transfer.dto.RecordSettlementRequest;
import com.meridian.transfer.model.LedgerEntryType;
import com.meridian.transfer.model.ReceptionMode;
import com.meridian.transfer.model.ReceptionModeLedgerEntry;
import com.meridian.transfer.model.Transfer;
import com.meridian.transfer.repository.ReceptionModeLedgerEntryRepository;
import com.meridian.transfer.repository.ReceptionModeRepository;
import com.meridian.transfer.repository.TransferRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.security.Principal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** Customer-service only (ROLE_ADMIN) — see SecurityConfig. */
@RestController
@RequestMapping("/api/admin/accounting")
public class AdminAccountingController {

    private final TransferRepository transferRepository;
    private final ReceptionModeRepository receptionModeRepository;
    private final ReceptionModeLedgerEntryRepository ledgerRepository;

    public AdminAccountingController(TransferRepository transferRepository,
                                      ReceptionModeRepository receptionModeRepository,
                                      ReceptionModeLedgerEntryRepository ledgerRepository) {
        this.transferRepository = transferRepository;
        this.receptionModeRepository = receptionModeRepository;
        this.ledgerRepository = ledgerRepository;
    }

    /** All-time summary for now — revenue (CAD) plus receptionMode commission/principal owed, by currency. */
    @GetMapping("/summary")
    public AccountingSummaryDto summary() {
        List<Transfer> transfers = transferRepository.findAll();

        BigDecimal totalRevenue = transfers.stream()
                .map(Transfer::getPlatformCommissionAmount)
                .filter(a -> a != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        Map<String, BigDecimal> commissionByCurrency = new HashMap<>();
        Map<String, BigDecimal> principalByCurrency = new HashMap<>();

        for (ReceptionModeLedgerEntry entry : ledgerRepository.findAllByOrderByCreatedAtDesc()) {
            if (entry.getType() == LedgerEntryType.COMMISSION_OWED) {
                principalByCurrency.merge(entry.getCurrency(), entry.getAmount(), BigDecimal::add);
            }
        }
        for (Transfer t : transfers) {
            if (t.getReceivingReceptionModeCommissionAmount() != null && t.getReceivingReceptionModeCommissionCurrency() != null) {
                commissionByCurrency.merge(t.getReceivingReceptionModeCommissionCurrency(),
                        t.getReceivingReceptionModeCommissionAmount(), BigDecimal::add);
            }
        }

        return new AccountingSummaryDto(
                null, null,
                transfers.size(),
                totalRevenue,
                toCurrencyAmountList(commissionByCurrency),
                toCurrencyAmountList(principalByCurrency)
        );
    }

    private List<AccountingSummaryDto.CurrencyAmount> toCurrencyAmountList(Map<String, BigDecimal> map) {
        List<AccountingSummaryDto.CurrencyAmount> list = new ArrayList<>();
        map.forEach((currency, amount) -> list.add(new AccountingSummaryDto.CurrencyAmount(currency, amount)));
        return list;
    }

    /** Current balance owed to every receptionMode that has at least one ledger entry. */
    @GetMapping("/balances")
    public List<ReceptionModeBalanceDto> balances() {
        List<ReceptionModeLedgerEntry> entries = ledgerRepository.findAllByOrderByCreatedAtDesc();

        Map<Long, BigDecimal> owed = new HashMap<>();
        Map<Long, BigDecimal> settled = new HashMap<>();
        Map<Long, ReceptionMode> receptionModesById = new HashMap<>();
        Map<Long, String> currencyByReceptionMode = new HashMap<>();

        for (ReceptionModeLedgerEntry entry : entries) {
            Long receptionModeId = entry.getReceptionMode().getId();
            receptionModesById.put(receptionModeId, entry.getReceptionMode());
            currencyByReceptionMode.put(receptionModeId, entry.getCurrency());
            if (entry.getType() == LedgerEntryType.COMMISSION_OWED) {
                owed.merge(receptionModeId, entry.getAmount(), BigDecimal::add);
            } else {
                settled.merge(receptionModeId, entry.getAmount(), BigDecimal::add);
            }
        }

        List<ReceptionModeBalanceDto> result = new ArrayList<>();
        for (Long receptionModeId : receptionModesById.keySet()) {
            BigDecimal totalOwed = owed.getOrDefault(receptionModeId, BigDecimal.ZERO);
            BigDecimal totalSettled = settled.getOrDefault(receptionModeId, BigDecimal.ZERO);
            result.add(new ReceptionModeBalanceDto(
                    receptionModeId,
                    receptionModesById.get(receptionModeId).getName(),
                    currencyByReceptionMode.get(receptionModeId),
                    totalOwed,
                    totalSettled,
                    totalOwed.subtract(totalSettled)
            ));
        }
        return result;
    }

    @GetMapping("/ledger")
    public List<LedgerEntryDto> ledger() {
        return ledgerRepository.findAllByOrderByCreatedAtDesc().stream().map(LedgerEntryDto::from).toList();
    }

    /** Customer service records an actual payment made to a reception mode — reduces its balance. */
    @PostMapping("/settlements")
    public LedgerEntryDto recordSettlement(@Valid @RequestBody RecordSettlementRequest request, Principal principal) {
        ReceptionMode receptionMode = receptionModeRepository.findById(request.receptionModeId())
                .orElseThrow(() -> new EntityNotFoundException("ReceptionMode not found: " + request.receptionModeId()));

        ReceptionModeLedgerEntry entry = new ReceptionModeLedgerEntry();
        entry.setReceptionMode(receptionMode);
        entry.setType(LedgerEntryType.SETTLEMENT_PAYMENT);
        entry.setAmount(request.amount());
        entry.setCurrency(request.currency());
        entry.setCreatedAt(Instant.now());
        entry.setNote(request.note());
        entry.setRecordedBy(principal.getName());

        return LedgerEntryDto.from(ledgerRepository.save(entry));
    }
}
