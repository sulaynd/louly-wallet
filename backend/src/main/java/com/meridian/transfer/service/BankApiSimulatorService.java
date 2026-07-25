package com.meridian.transfer.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.meridian.transfer.model.UserAccount;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

/**
 * Stands in for a real external bank/payment-gateway API. Shaped exactly like a real integration
 * would be — build a request, call the endpoint, parse the JSON response — so that swapping
 * {@link #callBankApi} for an actual HTTP call later is a small, contained change; nothing else
 * in this class (or in TransferService, which only sees {@link BankAuthorizationResult}) needs
 * to change.
 * <p>
 * For now, {@link #callBankApi} returns one of two canned JSON fixtures instead of really
 * calling anything: amounts of 500 or less "succeed", anything above "declines" — a simple
 * placeholder rule until a real bank partner integration replaces it.
 */
@Service
public class BankApiSimulatorService {

    private final ObjectMapper objectMapper = new ObjectMapper();

    private static final BigDecimal DUMMY_APPROVAL_THRESHOLD = new BigDecimal("500");

    private static final String SUCCESS_RESPONSE_JSON = """
            {
              "status": "APPROVED",
              "referenceCode": "BNK-DEMO0001",
              "availableBalance": 1000.00,
              "message": "Transaction approved"
            }
            """;

    private static final String DECLINED_RESPONSE_JSON = """
            {
              "status": "DECLINED",
              "referenceCode": null,
              "availableBalance": 0.00,
              "message": "Insufficient funds reported by bank"
            }
            """;

    public BankAuthorizationResult authorizeDebit(UserAccount account, BigDecimal amount, String currency) {
        String responseJson = callBankApi(account, amount, currency);
        return parseResponse(responseJson);
    }

    /**
     * TODO: replace this method's body with a real call once a bank partner integration exists,
     * e.g.:
     * <pre>
     *   return restClient.post()
     *       .uri("https://bank-partner.example.com/v1/authorizations")
     *       .body(buildRequestPayload(account, amount, currency))
     *       .retrieve()
     *       .body(String.class);
     * </pre>
     * Everything downstream (parseResponse, BankAuthorizationResult, TransferService) already
     * expects a JSON string back, so this is the only method that needs to change.
     */
    private String callBankApi(UserAccount account, BigDecimal amount, String currency) {
        return amount.compareTo(DUMMY_APPROVAL_THRESHOLD) <= 0 ? SUCCESS_RESPONSE_JSON : DECLINED_RESPONSE_JSON;
    }

    private BankAuthorizationResult parseResponse(String responseJson) {
        try {
            JsonNode node = objectMapper.readTree(responseJson);
            boolean approved = "APPROVED".equals(node.path("status").asText());
            String referenceCode = node.hasNonNull("referenceCode") ? node.get("referenceCode").asText() : null;
            BigDecimal reportedBalance = new BigDecimal(node.path("availableBalance").asText("0"));
            String message = node.path("message").asText("");

            return approved
                    ? BankAuthorizationResult.approve(referenceCode, reportedBalance)
                    : BankAuthorizationResult.decline(message, reportedBalance);
        } catch (Exception ex) {
            return BankAuthorizationResult.decline("Bank API response could not be parsed: " + ex.getMessage(), BigDecimal.ZERO);
        }
    }
}
