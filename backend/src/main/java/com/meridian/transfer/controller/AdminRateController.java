package com.meridian.transfer.controller;

import com.meridian.transfer.dto.ExchangeRateDto;
import com.meridian.transfer.dto.UpdateRateRequest;
import com.meridian.transfer.service.ExchangeRateService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;

/** Customer-service only (ROLE_ADMIN) — see SecurityConfig. */
@RestController
@RequestMapping("/api/admin/rates")
public class AdminRateController {

    private final ExchangeRateService exchangeRateService;

    public AdminRateController(ExchangeRateService exchangeRateService) {
        this.exchangeRateService = exchangeRateService;
    }

    @GetMapping
    public java.util.List<ExchangeRateDto> list() {
        return exchangeRateService.listRates().stream().map(ExchangeRateDto::from).toList();
    }

    /** Manually correct a single currency's rate, e.g. PUT /api/admin/rates/XOF { "rate": 435.00 } */
    @PutMapping("/{currency}")
    public ResponseEntity<?> updateOne(@PathVariable String currency, @Valid @RequestBody UpdateRateRequest request) {
        exchangeRateService.applyManualRates(Map.of(currency, request.rate()));
        return ResponseEntity.ok(Map.of("currency", currency.toUpperCase(), "rate", request.rate()));
    }

    /** Forces the scheduled refresh to run immediately instead of waiting for the next hourly tick. */
    @PostMapping("/refresh")
    public ResponseEntity<?> refreshNow() {
        exchangeRateService.refreshFromProvider();
        return ResponseEntity.ok(Map.of("status", "refreshed"));
    }

    /**
     * CSV upload for customer service — one "CURRENCY,RATE" pair per line, e.g.:
     * <pre>
     * PHP,41.75
     * XOF,435.00
     * </pre>
     * Blank lines and lines starting with # are ignored.
     */
    @PostMapping("/upload")
    public ResponseEntity<?> upload(@RequestParam("file") MultipartFile file) throws IOException {
        Map<String, BigDecimal> parsed = new LinkedHashMap<>();

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {
            String line;
            int lineNumber = 0;
            while ((line = reader.readLine()) != null) {
                lineNumber++;
                String trimmed = line.trim();
                if (trimmed.isEmpty() || trimmed.startsWith("#")) {
                    continue;
                }
                String[] parts = trimmed.split(",");
                if (parts.length != 2) {
                    return ResponseEntity.badRequest()
                            .body(Map.of("error", "Line " + lineNumber + " isn't in \"CURRENCY,RATE\" format: " + line));
                }
                try {
                    parsed.put(parts[0].trim().toUpperCase(), new BigDecimal(parts[1].trim()));
                } catch (NumberFormatException ex) {
                    return ResponseEntity.badRequest()
                            .body(Map.of("error", "Line " + lineNumber + " has an invalid rate: " + line));
                }
            }
        }

        if (parsed.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "No valid rate rows found in the file."));
        }

        exchangeRateService.applyManualRates(parsed);
        return ResponseEntity.ok(Map.of("updated", parsed.keySet()));
    }
}
