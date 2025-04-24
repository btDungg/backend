package com.goldenowl.backend.service.ImplementService;

import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

@Service
public class DataInitializerService {
    private static final Logger log = LoggerFactory.getLogger(DataInitializerService.class);

    @Value("classpath:dataset/diem_thi_thpt_2024.csv")
    private Resource csvDataFile;

    @Value("${app.data.import.enabled:true}")
    private boolean dataImportEnabled;

    private final DataImportService dataImportService;

    public DataInitializerService(DataImportService dataImportService) {
        this.dataImportService = dataImportService;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void initializeData() {
        try {
            log.debug("Checking CSV file availability...");
            log.info("CSV file exists: {}", csvDataFile.exists());
            log.info("CSV file path: {}", csvDataFile.getURI().toString());
        } catch (Exception e) {
            log.error("CSV file check failed: {}", e.getMessage());
            return;
        }

        if (!dataImportEnabled) {
            log.info("Data import disabled by configuration");
            return;
        }

        try {
            dataImportService.importDataIfNeeded(csvDataFile);
        } catch (Exception e) {
            log.error("Data import failed: {}", e.getMessage(), e);
        }
    }
}
