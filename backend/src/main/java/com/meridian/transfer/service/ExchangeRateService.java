package com.meridian.transfer.service;

import com.meridian.transfer.model.ExchangeRate;
import com.meridian.transfer.model.RateSource;
import com.meridian.transfer.repository.ExchangeRateRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.Map;
import java.util.Set;

/**
 * Exchange rates now live in the {@code exchange_rates} table — that's the single source of
 * truth the app reads from. Two things keep it up to date:
 *  - a scheduled job ({@link #refreshFromProvider()}) that pulls live rates from Frankfurter for
 *    currencies it covers, and from a secondary provider (ExchangeRate-API) for currencies
 *    Frankfurter doesn't — e.g. XOF — as long as {@code exchange.rate.secondary-api-key} is set;
 *  - customer service, who can overwrite any rate manually (single edit or CSV upload) via
 *    {@code AdminRateController} — useful for a currency neither provider covers, or a temporary
 *    correction. The scheduler will still refresh a MANUAL row on its next run if a provider
 *    covers that currency.
 */
@Service
public class ExchangeRateService {

    private static final Logger log = LoggerFactory.getLogger(ExchangeRateService.class);
    private static final String BASE_CURRENCY = "CAD";

    /** Used only to seed a brand-new currency row the very first time it's requested. */
    private static final Map<String, BigDecimal> BOOTSTRAP_RATES_FROM_CAD = Map.of(
            "PHP", new BigDecimal("41.69"),
            "INR", new BigDecimal("61.85"),
            "EUR", new BigDecimal("0.66"),
            "XOF", new BigDecimal("433.50"),
            "USD", new BigDecimal("0.73")
    );

    /**
     * Currencies Frankfurter actually publishes (ECB reference rates). It does NOT cover
     * regional currencies like the West African CFA franc (XOF) — those fall through to the
     * secondary provider below, if configured.
     */
    private static final Set<String> PROVIDER_COVERAGE = Set.of(
            "AUD", "BGN", "BRL", "CAD", "CHF", "CNY", "CZK", "DKK", "EUR", "GBP", "HKD",
            "HUF", "IDR", "ILS", "INR", "ISK", "JPY", "KRW", "MXN", "MYR", "NOK", "NZD",
            "PHP", "PLN", "RON", "SEK", "SGD", "THB", "TRY", "USD", "ZAR"
    );

    /**
     * Currencies the secondary provider (ExchangeRate-API) covers that Frankfurter doesn't —
     * this is what actually keeps XOF refreshed automatically instead of sitting on a manual
     * value indefinitely. Requires exchange.rate.secondary-api-key to be set; silently skipped
     * (falls back to whatever's on file) if no key is configured.
     */
    private static final Set<String> SECONDARY_PROVIDER_COVERAGE = Set.of("XOF", "XAF");

    // Fee is now computed by FeeTierService (tiered by amount) — the flat national/international
    // constants that used to live here are gone.

    private final RestClient restClient;
    private final RestClient secondaryRestClient;
    private final String secondaryApiKey;
    private final ExchangeRateRepository exchangeRateRepository;

    public ExchangeRateService(RestClient.Builder builder,
                                @Value("${exchange.rate.api-base-url}") String apiBaseUrl,
                                @Value("${exchange.rate.secondary-api-base-url}") String secondaryApiBaseUrl,
                                @Value("${exchange.rate.secondary-api-key:}") String secondaryApiKey,
                                ExchangeRateRepository exchangeRateRepository) {
        this.restClient = builder.baseUrl(apiBaseUrl).build();
        this.secondaryRestClient = builder.baseUrl(secondaryApiBaseUrl).build();
        this.secondaryApiKey = secondaryApiKey;
        this.exchangeRateRepository = exchangeRateRepository;
    }

    /** Rate to convert 1 unit of {@code from} into {@code to}, read straight from the table. */
    @Transactional
    public BigDecimal rateFor(String from, String to) {
        String fromCode = from.toUpperCase();
        String toCode = to.toUpperCase();
        if (fromCode.equals(toCode)) {
            return BigDecimal.ONE;
        }

        return exchangeRateRepository.findByBaseCurrencyAndTargetCurrency(fromCode, toCode)
                .map(ExchangeRate::getRate)
                .orElseGet(() -> seedRateRow(fromCode, toCode).getRate());
    }

    /** First time a currency pair is requested and there's no row yet — create one from the bootstrap table. */
    private ExchangeRate seedRateRow(String from, String to) {
        BigDecimal rate = BOOTSTRAP_RATES_FROM_CAD.getOrDefault(to, BigDecimal.ONE);
        ExchangeRate row = new ExchangeRate();
        row.setBaseCurrency(from);
        row.setTargetCurrency(to);
        row.setRate(rate);
        row.setSource(RateSource.SEED);
        row.setUpdatedAt(Instant.now());
        log.info("No rate row yet for {} -> {}; seeding with bootstrap rate {}", from, to, rate);
        return exchangeRateRepository.save(row);
    }

    /** Fee is now computed by FeeTierService — see /api/rates and /api/fee-tiers. */

    /**
     * Currencies with no minor unit (no cents/centimes) — must round to whole units, not 2 decimals.
     * Keep this in sync with the frontend's currency-utils.ts.
     */
    private static final Set<String> ZERO_DECIMAL_CURRENCIES = Set.of("XOF", "XAF", "JPY", "KRW", "VND", "CLP");

    /**
     * Currencies whose smallest *actual circulating* coin isn't 1 unit — e.g. the CFA franc has
     * no 1-franc coin, the smallest coins are 5, 10, 25, 50... Round to the nearest one of these
     * instead of the nearest whole unit. Keep in sync with currency-utils.ts.
     */
    private static final Map<String, BigDecimal> ROUNDING_INCREMENT = Map.of(
            "XOF", new BigDecimal("5"),
            "XAF", new BigDecimal("5")
    );

    public BigDecimal convert(BigDecimal amount, BigDecimal rate, String targetCurrency) {
        String code = targetCurrency.toUpperCase();
        BigDecimal raw = amount.multiply(rate);

        BigDecimal increment = ROUNDING_INCREMENT.get(code);
        if (increment != null) {
            return raw.divide(increment, 0, RoundingMode.HALF_UP).multiply(increment);
        }

        int decimals = ZERO_DECIMAL_CURRENCIES.contains(code) ? 0 : 2;
        return raw.setScale(decimals, RoundingMode.HALF_UP);
    }

    /** All rates for the base currency, for the admin rates screen. */
    public java.util.List<ExchangeRate> listRates() {
        return exchangeRateRepository.findByBaseCurrency(BASE_CURRENCY);
    }

    /**
     * Applies a batch of manually supplied rates (single edit or CSV upload from customer
     * service). Always marks the row MANUAL so it's clear it was a human override.
     */
    @Transactional
    public void applyManualRates(Map<String, BigDecimal> ratesByCurrency) {
        ratesByCurrency.forEach((currency, rate) -> {
            String code = currency.trim().toUpperCase();
            ExchangeRate row = exchangeRateRepository
                    .findByBaseCurrencyAndTargetCurrency(BASE_CURRENCY, code)
                    .orElseGet(() -> {
                        ExchangeRate fresh = new ExchangeRate();
                        fresh.setBaseCurrency(BASE_CURRENCY);
                        fresh.setTargetCurrency(code);
                        return fresh;
                    });
            row.setRate(rate);
            row.setSource(RateSource.MANUAL);
            row.setUpdatedAt(Instant.now());
            exchangeRateRepository.save(row);
            log.info("Rate for {} manually set to {}", code, rate);
        });
    }

    /**
     * Scheduled background refresh — pulls fresh rates from Frankfurter for every currency the
     * app currently has a row for, as long as that currency is actually covered by the provider.
     * Runs every hour; also runs a few seconds after startup so rates aren't stale from a
     * previous run.
     */
    @Scheduled(initialDelay = 15_000, fixedRate = 3_600_000)
    @Transactional
    public void refreshFromProvider() {
        java.util.List<ExchangeRate> rows = exchangeRateRepository.findByBaseCurrency(BASE_CURRENCY);
        int refreshed = 0;

        Map<String, BigDecimal> secondaryRates = fetchSecondaryRatesIfConfigured();

        for (ExchangeRate row : rows) {
            String target = row.getTargetCurrency();

            if (PROVIDER_COVERAGE.contains(target) && PROVIDER_COVERAGE.contains(BASE_CURRENCY)) {
                if (refreshFromFrankfurter(row, target)) {
                    refreshed++;
                }
                continue;
            }

            if (secondaryRates.containsKey(target)) {
                row.setRate(secondaryRates.get(target));
                row.setSource(RateSource.LIVE_PROVIDER);
                row.setUpdatedAt(Instant.now());
                exchangeRateRepository.save(row);
                refreshed++;
            }
            // Neither provider covers it and no secondary key configured — leave whatever's on file (MANUAL).
        }

        if (refreshed > 0) {
            log.info("Scheduled rate refresh updated {} currency pair(s) from live providers", refreshed);
        }
    }

    private boolean refreshFromFrankfurter(ExchangeRate row, String target) {
        try {
            FrankfurterResponse response = restClient.get()
                    .uri("/latest?from={from}&to={to}", BASE_CURRENCY, target)
                    .retrieve()
                    .body(FrankfurterResponse.class);

            BigDecimal rate = response != null && response.rates() != null ? response.rates().get(target) : null;
            if (rate != null) {
                row.setRate(rate);
                row.setSource(RateSource.LIVE_PROVIDER);
                row.setUpdatedAt(Instant.now());
                exchangeRateRepository.save(row);
                return true;
            }
        } catch (RestClientException ex) {
            log.warn("Scheduled rate refresh failed for {} -> {}: {}", BASE_CURRENCY, target, ex.getMessage());
        }
        return false;
    }

    /**
     * One call fetches every rate ExchangeRate-API has for CAD at once — cheaper than one call
     * per currency, and this is what actually keeps XOF current instead of stuck on a manual
     * value. Returns an empty map (safe no-op) if no API key is configured, or on any failure.
     */
    private Map<String, BigDecimal> fetchSecondaryRatesIfConfigured() {
        if (secondaryApiKey == null || secondaryApiKey.isBlank()) {
            return Map.of();
        }
        try {
            SecondaryProviderResponse response = secondaryRestClient.get()
                    .uri("/{apiKey}/latest/{base}", secondaryApiKey, BASE_CURRENCY)
                    .retrieve()
                    .body(SecondaryProviderResponse.class);

            if (response == null || !"success".equalsIgnoreCase(response.result()) || response.conversionRates() == null) {
                log.warn("Secondary rate provider returned no usable data");
                return Map.of();
            }
            return response.conversionRates().entrySet().stream()
                    .filter(e -> SECONDARY_PROVIDER_COVERAGE.contains(e.getKey()))
                    .collect(java.util.stream.Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
        } catch (RestClientException ex) {
            log.warn("Secondary rate provider call failed: {}", ex.getMessage());
            return Map.of();
        }
    }

    private record FrankfurterResponse(String base, Map<String, BigDecimal> rates) {
    }

    /** ExchangeRate-API's v6 /latest/{base} response shape. */
    private record SecondaryProviderResponse(
            String result,
            @com.fasterxml.jackson.annotation.JsonProperty("base_code") String baseCode,
            @com.fasterxml.jackson.annotation.JsonProperty("conversion_rates") Map<String, BigDecimal> conversionRates
    ) {
    }
}
