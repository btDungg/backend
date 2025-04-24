package com.goldenowl.backend.service.ImplementService;

import com.goldenowl.backend.entity.Score;
import com.goldenowl.backend.repository.ScoreRepository;

import com.opencsv.CSVReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.InputStreamReader;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.util.*;

@Service
public class DataImportService {
    private static final Logger log = LoggerFactory.getLogger(DataImportService.class);
    private static final int BATCH_SIZE = 500;

    private final ScoreRepository scoreRepository;

    public DataImportService(ScoreRepository scoreRepository) {
        this.scoreRepository = scoreRepository;
    }

    public void importDataIfNeeded(Resource csvDataFile) {
        long count = scoreRepository.count();
        if (count < 10) {
            log.info("Starting CSV import...");
            importData(csvDataFile);
        } else {
            log.info("Skip import: DB already has {} records", count);
        }
    }

    @Transactional
    public void importData(Resource csvDataFile) {
        try (CSVReader reader = new CSVReader(new InputStreamReader(csvDataFile.getInputStream(), StandardCharsets.UTF_8))) {
            String[] headers = reader.readNext();
            if (headers == null) throw new IllegalStateException("Empty CSV file");

            Map<String, Integer> headerMap = mapHeaders(headers);
            validateHeaders(headerMap);

            List<Score> batch = new ArrayList<>(BATCH_SIZE);
            String[] line;
            int total = 0;

            while ((line = reader.readNext()) != null) {
                try {
                    Score score = parseScore(line, headerMap);
                    if (score != null) {
                        batch.add(score);
                        total++;
                        if (batch.size() >= BATCH_SIZE) {
                            scoreRepository.saveAll(batch);
                            batch.clear();
                        }
                    }
                } catch (Exception e) {
                    log.warn("Failed to parse line {}: {}", total + 1, e.getMessage());
                }
            }

            if (!batch.isEmpty()) {
                scoreRepository.saveAll(batch);
            }

            log.info("Imported {} scores successfully", total);

        } catch (Exception e) {
            throw new RuntimeException("Error reading CSV", e);
        }
    }

    private Map<String, Integer> mapHeaders(String[] headers) {
        Map<String, Integer> map = new HashMap<>();
        for (int i = 0; i < headers.length; i++) {
            map.put(headers[i].trim().toLowerCase(), i);
        }
        return map;
    }

    private void validateHeaders(Map<String, Integer> headers) {
        if (!headers.containsKey("sbd")) {
            throw new IllegalArgumentException("Missing SBD column");
        }
    }

    private Score parseScore(String[] values, Map<String, Integer> headerMap) {
        String sbd = getValue(values, headerMap, "sbd");
        if (sbd == null || sbd.isEmpty()) return null;

        Score score = new Score();
        score.setRegistrationNumber(sbd);

        processScore(values, headerMap, score, "toan", "math");
        processScore(values, headerMap, score, "ngu_van", "literature");
        processScore(values, headerMap, score, "ngoai_ngu", "foreignLanguage");
        processScore(values, headerMap, score, "vat_li", "physics");
        processScore(values, headerMap, score, "hoa_hoc", "chemistry");
        processScore(values, headerMap, score, "sinh_hoc", "biology");
        processScore(values, headerMap, score, "lich_su", "history");
        processScore(values, headerMap, score, "dia_li", "geography");
        processScore(values, headerMap, score, "gdcd", "civicEducation");

        String code = getValue(values, headerMap, "ma_ngoai_ngu");
        if (code != null && !code.isEmpty()) score.setForeignLanguageCode(code);

        return score;
    }

    private void processScore(String[] values, Map<String, Integer> headerMap, Score score, String csvField, String entityField) {
        Float val = parseFloat(getValue(values, headerMap, csvField));
        if (val != null) {
            try {
                Method setter = Score.class.getMethod("set" + capitalize(entityField), Float.class);
                setter.invoke(score, val);
            } catch (Exception e) {
                log.warn("Setter error on {}: {}", entityField, e.getMessage());
            }
        }
    }

    private String getValue(String[] values, Map<String, Integer> headerMap, String field) {
        Integer index = headerMap.get(field);
        if (index == null || index >= values.length) return null;
        return values[index].trim();
    }

    private Float parseFloat(String s) {
        if (s == null || s.isEmpty()) return null;
        try {
            return Float.parseFloat(s.replace(',', '.'));
        } catch (Exception e) {
            return null;
        }
    }

    private String capitalize(String str) {
        return str.substring(0, 1).toUpperCase() + str.substring(1);
    }
}
