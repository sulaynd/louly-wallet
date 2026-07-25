package com.meridian.transfer.service;

import com.meridian.transfer.model.UserAccount;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

class BankApiSimulatorServiceTest {

    private final BankApiSimulatorService bankApiSimulatorService = new BankApiSimulatorService();

    @Test
    void authorizeDebit_approves_whenAmountIsAtOrBelowTheThreshold() {
        BankAuthorizationResult result = bankApiSimulatorService.authorizeDebit(
                new UserAccount(), new BigDecimal("500"), "CAD");

        assertThat(result.approved()).isTrue();
        assertThat(result.referenceCode()).isEqualTo("BNK-DEMO0001");
        assertThat(result.reportedBalance()).isEqualByComparingTo("1000.00");
        assertThat(result.declineReason()).isNull();
    }

    @Test
    void authorizeDebit_approves_forASmallAmount() {
        BankAuthorizationResult result = bankApiSimulatorService.authorizeDebit(
                new UserAccount(), new BigDecimal("300"), "CAD");

        assertThat(result.approved()).isTrue();
    }

    @Test
    void authorizeDebit_declines_whenAmountExceedsTheThreshold() {
        BankAuthorizationResult result = bankApiSimulatorService.authorizeDebit(
                new UserAccount(), new BigDecimal("600"), "CAD");

        assertThat(result.approved()).isFalse();
        assertThat(result.referenceCode()).isNull();
        assertThat(result.declineReason()).isEqualTo("Insufficient funds reported by bank");
    }

    @Test
    void authorizeDebit_declineHasNoReferenceCode() {
        // A declined authorization must never carry a reference code — nothing was actually
        // authorized, so there's nothing to reference later.
        BankAuthorizationResult result = bankApiSimulatorService.authorizeDebit(
                new UserAccount(), new BigDecimal("2000"), "XOF");

        assertThat(result.referenceCode()).isNull();
    }
}
