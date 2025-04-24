package com.goldenowl.backend.service.ImplementService;

import com.goldenowl.backend.entity.Score;
import com.goldenowl.backend.repository.ScoreRepository;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class DataInitializerService {

    private static final Logger log = LoggerFactory.getLogger(DataInitializerService.class);
    private static final int BATCH_SIZE = 1000; // Reduced batch size for Railway

    @Autowired
    private ScoreRepository scoreRepository;

    @Value("classpath:dataset/diem_thi_thpt_2024.csv")
    private Resource csvDataFile;

    @Value("${spring.profiles.active:default}")
    private String activeProfile;

    @Value("${app.data.import.enabled:true}")
    private boolean dataImportEnabled;

    @PostConstruct
    public void initializeData() {
        // Debug information about the CSV file
        try {
            log.info("Active profile: {}", activeProfile);
            log.info("Data import enabled: {}", dataImportEnabled);
            log.info("CSV file exists: {}", csvDataFile.exists());
            log.info("CSV file path: {}", csvDataFile.getURI().toString());
            log.info("CSV file readable: {}", csvDataFile.isReadable());
            log.info("CSV file size: {} bytes", csvDataFile.contentLength());
        } catch (IOException e) {
            log.error("Error checking CSV file: {}", e.getMessage());
        }

        // Skip import if disabled or if data already exists
        if (!dataImportEnabled) {
            log.info("CSV data import disabled by configuration. Skipping.");
            return;
        }

        if (scoreRepository.count() > 0) {
            log.info("Data already exists in the database. Skipping CSV import.");
            return;
        }

        log.info("Starting data import from CSV: {}", csvDataFile.getFilename());
        long startTime = System.currentTimeMillis();
        int recordCount = 0;
        int successCount = 0;
        int errorCount = 0;

        List<Score> batch = new ArrayList<>(BATCH_SIZE);

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(csvDataFile.getInputStream(), StandardCharsets.UTF_8))) {

            String headerLine = reader.readLine();
            if (headerLine == null) {
                log.error("CSV file is empty or cannot be read.");
                return;
            }

            // Process header row and create mapping
            String[] headers = headerLine.split(",");
            Map<String, Integer> headerMap = new HashMap<>();
            for (int i = 0; i < headers.length; i++) {
                headerMap.put(headers[i].trim().toLowerCase(), i);
            }

            // Validate essential columns exist
            if (!headerMap.containsKey("sbd")) {
                log.error("Required column 'sbd' not found in CSV headers");
                throw new RuntimeException("CSV format error: missing 'sbd' column");
            }

            log.info("CSV header mapping: {}", headerMap);

            // Process data rows
            String line;
            while ((line = reader.readLine()) != null) {
                recordCount++;
                try {
                    // Split line into values - handle potential quotes or commas in values
                    String[] values = parseCsvLine(line);

                    // Skip if SBD is missing
                    String sbd = getStringValue(values, headerMap.get("sbd"));
                    if (sbd == null || sbd.isEmpty()) {
                        log.warn("Row {} skipped: Missing SBD", recordCount);
                        errorCount++;
                        continue;
                    }

                    // Create Score object
                    Score score = new Score();
                    score.setRegistrationNumber(sbd);

                    // Set scores from CSV
                    setScoreField(score, "math", parseFloatScore(values, headerMap.get("toan")));
                    setScoreField(score, "literature", parseFloatScore(values, headerMap.get("ngu_van")));
                    setScoreField(score, "foreignLanguage", parseFloatScore(values, headerMap.get("ngoai_ngu")));
                    setScoreField(score, "physics", parseFloatScore(values, headerMap.get("vat_li")));
                    setScoreField(score, "chemistry", parseFloatScore(values, headerMap.get("hoa_hoc")));
                    setScoreField(score, "biology", parseFloatScore(values, headerMap.get("sinh_hoc")));
                    setScoreField(score, "history", parseFloatScore(values, headerMap.get("lich_su")));
                    setScoreField(score, "geography", parseFloatScore(values, headerMap.get("dia_li")));
                    setScoreField(score, "civicEducation", parseFloatScore(values, headerMap.get("gdcd")));

                    // Set foreign language code if available
                    Integer flCodeIndex = headerMap.get("ma_ngoai_ngu");
                    if (flCodeIndex != null && flCodeIndex < values.length) {
                        score.setForeignLanguageCode(getStringValue(values, flCodeIndex));
                    }

                    batch.add(score);
                    successCount++;

                    // Save batch when it reaches size limit
                    if (batch.size() >= BATCH_SIZE) {
                        saveAndClearBatch(batch);

                        // Log progress
                        if (successCount % 10000 == 0) {
                            long elapsedTime = System.currentTimeMillis() - startTime;
                            log.info("Progress: {} records processed, {} successful, {} ms elapsed",
                                    recordCount, successCount, elapsedTime);
                        }
                    }
                } catch (Exception e) {
                    log.warn("Error processing row {}: {}", recordCount, e.getMessage());
                    errorCount++;
                }
            }

            // Save any remaining records
            if (!batch.isEmpty()) {
                saveAndClearBatch(batch);
            }

            long elapsedTime = System.currentTimeMillis() - startTime;
            log.info("CSV import completed in {} ms. Total: {}, Success: {}, Errors: {}",
                    elapsedTime, recordCount, successCount, errorCount);

        } catch (IOException e) {
            log.error("Error reading CSV file: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to read CSV data file", e);
        } catch (Exception e) {
            log.error("Unexpected error during data import: {}", e.getMessage(), e);
            throw new RuntimeException("Data import failed", e);
        }
    }

    /**
     * Save the current batch and clear the list
     */
    private void saveAndClearBatch(List<Score> batch) {
        try {
            scoreRepository.saveAll(batch);
            batch.clear();
        } catch (Exception e) {
            log.error("Error saving batch: {}", e.getMessage(), e);
            // Don't clear batch on error - let it be saved on next attempt
            throw e;
        }
    }

    /**
     * Parse a CSV line, handling quoted values that may contain commas
     */
    private String[] parseCsvLine(String line) {
        List<String> values = new ArrayList<>();
        boolean inQuotes = false;
        StringBuilder currentValue = new StringBuilder();

        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);

            if (c == '"') {
                inQuotes = !inQuotes;
            } else if (c == ',' && !inQuotes) {
                values.add(currentValue.toString().trim());
                currentValue = new StringBuilder();
            } else {
                currentValue.append(c);
            }
        }

        // Add the last value
        values.add(currentValue.toString().trim());

        return values.toArray(new String[0]);
    }

    /**
     * Helper method to get string value from array with index checking
     */
    private String getStringValue(String[] values, Integer index) {
        if (index == null || index < 0 || index >= values.length) {
            return null;
        }
        String value = values[index].trim();
        return value.isEmpty() ? null : value;
    }

    /**
     * Helper method to parse float score safely
     */
    private Float parseFloatScore(String[] values, Integer index) {
        String value = getStringValue(values, index);
        if (value == null) {
            return null;
        }

        try {
            // Handle both comma and period as decimal separators
            value = value.replace(',', '.');
            return Float.parseFloat(value);
        } catch (NumberFormatException e) {
            // Return null for invalid values
            return null;
        }
    }

    /**
     * Helper method to set score field with validation
     */
    private void setScoreField(Score score, String fieldName, Float value) {
        try {
            switch (fieldName) {
                case "math":
                    score.setMath(value);
                    break;
                case "literature":
                    score.setLiterature(value);
                    break;
                case "foreignLanguage":
                    score.setForeignLanguage(value);
                    break;
                case "physics":
                    score.setPhysics(value);
                    break;
                case "chemistry":
                    score.setChemistry(value);
                    break;
                case "biology":
                    score.setBiology(value);
                    break;
                case "history":
                    score.setHistory(value);
                    break;
                case "geography":
                    score.setGeography(value);
                    break;
                case "civicEducation":
                    score.setCivicEducation(value);
                    break;
                default:
                    log.warn("Unknown field name: {}", fieldName);
            }
        } catch (Exception e) {
            log.warn("Error setting {}: {}", fieldName, e.getMessage());
        }
    }
}