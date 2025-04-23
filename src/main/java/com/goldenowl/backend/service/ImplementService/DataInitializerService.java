package com.goldenowl.backend.service.ImplementService;


import com.goldenowl.backend.entity.Score;
import com.goldenowl.backend.repository.ScoreRepository;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource; // Để đọc file từ classpath
import org.springframework.stereotype.Service;



import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets; // Chỉ định encoding UTF-8 khi đọc file
import java.util.ArrayList;
import java.util.HashMap; // Dùng HashMap để lưu chỉ mục cột theo tên
import java.util.List;
import java.util.Map;

@Service // Đánh dấu đây là một Spring Service Bean
public class DataInitializerService {

    private static final Logger log = LoggerFactory.getLogger(DataInitializerService.class);
    // Kích thước mỗi lô insert, điều chỉnh nếu cần để cân bằng tốc độ và bộ nhớ
    private static final int BATCH_SIZE = 5000;

    @Autowired // Tiêm ScoreRepository để lưu dữ liệu
    private ScoreRepository scoreRepository;

    // Lấy đường dẫn file CSV từ application.properties hoặc đặt cố định ở đây
    @Value("classpath:dataset/diem_thi_thpt_2024.csv")
    private Resource csvDataFile; // Resource object để đọc file

    @PostConstruct // Hàm này sẽ được gọi tự động sau khi DataInitializerService được tạo
    public void initializeData() {
//        // --- Bước kiểm tra: Chỉ chạy nếu bảng scores chưa có dữ liệu ---
//        if (scoreRepository.count() == 0) {
//            log.info("CSDL đang trống. Bắt đầu khởi tạo dữ liệu từ CSV: {}", csvDataFile.getFilename());
//            long startTime = System.currentTimeMillis(); // Bắt đầu tính giờ
//            List<Score> batch = new ArrayList<>(BATCH_SIZE); // List để lưu các Score object cho mỗi lô
//            int recordCount = 0; // Tổng số dòng đã đọc (bao gồm cả lỗi)
//            int errorCount = 0; // Số dòng bị lỗi khi xử lý
//
//            // --- Bước đọc file CSV ---
//            // Sử dụng try-with-resources để đảm bảo reader được đóng
//            try (CSVReader reader = new CSVReader(new InputStreamReader(csvDataFile.getInputStream(), StandardCharsets.UTF_8))) {
//                // Đọc dòng đầu tiên (dòng tiêu đề)
//                String[] headers = reader.readNext();
//                if (headers == null) {
//                    log.error("File CSV trống hoặc thiếu dòng tiêu đề.");
//                    return;
//                }
//
//                // Tạo Map để lưu tên tiêu đề (chữ thường) và vị trí cột (chỉ mục)
//                Map<String, Integer> headerMap = new HashMap<>();
//                for (int i = 0; i < headers.length; i++) {
//                    headerMap.put(headers[i].trim().toLowerCase(), i);
//                }
//
//                // Kiểm tra sự tồn tại của cột SBD (quan trọng)
//                if (!headerMap.containsKey("sbd")) {
//                    log.error("Không tìm thấy cột bắt buộc 'sbd' trong tiêu đề CSV!");
//                    throw new RuntimeException("Thiếu cột 'sbd' trong file CSV."); // Ném lỗi để dừng và rollback
//                }
//
//                String[] line; // Mảng chứa dữ liệu của một dòng
//                // Đọc từng dòng còn lại của file CSV
//                while ((line = reader.readNext()) != null) {
//                    recordCount++;
//                    try {
//                        // Lấy chỉ mục cột từ headerMap (dùng tên tiếng Việt từ CSV, viết thường)
//                        Integer sbdIndex = headerMap.get("sbd");
//                        Integer mathIndex = headerMap.get("toan");
//                        Integer literatureIndex = headerMap.get("ngu_van");
//                        Integer foreignLangIndex = headerMap.get("ngoai_ngu");
//                        Integer physicsIndex = headerMap.get("vat_li");
//                        Integer chemistryIndex = headerMap.get("hoa_hoc");
//                        Integer biologyIndex = headerMap.get("sinh_hoc");
//                        Integer historyIndex = headerMap.get("lich_su");
//                        Integer geographyIndex = headerMap.get("dia_li");
//                        Integer civicEduIndex = headerMap.get("gdcd");
//                        Integer foreignLangCodeIndex = headerMap.get("ma_ngoai_ngu");
//
//                        // Lấy giá trị SBD, nếu trống thì bỏ qua dòng
//                        String sbd = getStringValue(line, sbdIndex);
//                        if (sbd == null || sbd.isEmpty()) {
//                            log.warn("Bỏ qua dòng {} do thiếu hoặc SBD trống.", recordCount);
//                            errorCount++;
//                            continue; // Sang dòng tiếp theo
//                        }
//
//                        // Parse các giá trị điểm (trả về null nếu lỗi hoặc trống)
//                        Float math = parseFloatScore(line, mathIndex);
//                        Float literature = parseFloatScore(line, literatureIndex);
//                        Float foreignLanguage = parseFloatScore(line, foreignLangIndex);
//                        Float physics = parseFloatScore(line, physicsIndex);
//                        Float chemistry = parseFloatScore(line, chemistryIndex);
//                        Float biology = parseFloatScore(line, biologyIndex);
//                        Float history = parseFloatScore(line, historyIndex);
//                        Float geography = parseFloatScore(line, geographyIndex);
//                        Float civicEducation = parseFloatScore(line, civicEduIndex);
//                        String foreignLanguageCode = getStringValue(line, foreignLangCodeIndex);
//
//                        // Tạo đối tượng Score Entity
//                        // Chú ý thứ tự tham số phải khớp với constructor trong Score.java (nếu dùng)
//                        // Hoặc dùng setter: score.setRegistrationNumber(sbd); score.setMath(math); ...
//                        Score score = new Score();
//                        score.setRegistrationNumber(sbd);
//                        score.setMath(math);
//                        score.setLiterature(literature);
//                        score.setForeignLanguage(foreignLanguage);
//                        score.setPhysics(physics);
//                        score.setChemistry(chemistry);
//                        score.setBiology(biology);
//                        score.setHistory(history);
//                        score.setGeography(geography);
//                        score.setCivicEducation(civicEducation);
//                        score.setForeignLanguageCode(foreignLanguageCode);
//
//                        batch.add(score); // Thêm vào lô hiện tại
//
//                        // --- Bước Lưu dữ liệu theo lô (Batch Insert) ---
//                        if (batch.size() >= BATCH_SIZE) {
//                            scoreRepository.saveAll(batch); // Lưu cả lô vào CSDL
//                            batch.clear(); // Làm sạch lô để chuẩn bị cho lô tiếp theo
//                            log.info("Đã lưu {} bản ghi... (Tổng đã xử lý: {})", BATCH_SIZE, recordCount);
//                        }
//                    } catch (Exception e) { // Bắt lỗi chung khi xử lý một dòng
//                        log.error("Lỗi xử lý dòng {}: {} - Lỗi: {}", recordCount, String.join(",", line), e.getMessage(), e);
//                        errorCount++;
//                        // Bỏ qua dòng lỗi, tiếp tục xử lý các dòng khác
//                    }
//                } // Kết thúc vòng lặp while
//
//                // Lưu lô cuối cùng nếu còn dữ liệu
//                if (!batch.isEmpty()) {
//                    scoreRepository.saveAll(batch);
//                    log.info("Đã lưu lô cuối cùng gồm {} bản ghi.", batch.size());
//                }
//
//                long endTime = System.currentTimeMillis(); // Kết thúc tính giờ
//                // Log tổng kết
//                log.info("Hoàn tất khởi tạo dữ liệu. Tổng số dòng đã xử lý: {}. Lưu thành công: {}. Số dòng lỗi: {}. Thời gian thực hiện: {} ms",
//                        recordCount, recordCount - errorCount, errorCount, (endTime - startTime));
//
//            } catch (IOException | CsvValidationException e) { // Bắt lỗi liên quan đến đọc file CSV
//                log.error("Lỗi nghiêm trọng khi đọc file CSV: {}", csvDataFile.getFilename(), e);
//                throw new RuntimeException("Không thể khởi tạo dữ liệu từ CSV. Kiểm tra file và cấu hình.", e);
//            }
//        } else {
//            log.info("Dữ liệu đã tồn tại trong CSDL. Bỏ qua quá trình import từ CSV.");
//        }

        if (scoreRepository.count() > 0) {
            log.info("Dữ liệu đã tồn tại trong CSDL. Bỏ qua quá trình import từ CSV.");
            return;
        }

        log.info("Bắt đầu import dữ liệu từ file CSV: {}", csvDataFile.getFilename());
        long startTime = System.currentTimeMillis();
        int recordCount = 0;
        int errorCount = 0;

        List<Score> batch = new ArrayList<>(BATCH_SIZE);

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(csvDataFile.getInputStream(), StandardCharsets.UTF_8))) {
            String headerLine = reader.readLine();
            if (headerLine == null) {
                log.error("File CSV trống.");
                return;
            }

            String[] headers = headerLine.split(",");
            Map<String, Integer> headerMap = new HashMap<>();
            for (int i = 0; i < headers.length; i++) {
                headerMap.put(headers[i].trim().toLowerCase(), i);
            }

            if (!headerMap.containsKey("sbd")) {
                throw new RuntimeException("Thiếu cột 'sbd' trong file CSV.");
            }

            String line;
            while ((line = reader.readLine()) != null) {
                recordCount++;
                String[] values = line.split(",", -1); // Giữ cả ô rỗng

                try {
                    String sbd = getStringValue(values, headerMap.get("sbd"));
                    if (sbd == null || sbd.isEmpty()) {
                        errorCount++;
                        continue;
                    }

                    Score score = new Score();
                    score.setRegistrationNumber(sbd);
                    score.setMath(parseFloatScore(values, headerMap.get("toan")));
                    score.setLiterature(parseFloatScore(values, headerMap.get("ngu_van")));
                    score.setForeignLanguage(parseFloatScore(values, headerMap.get("ngoai_ngu")));
                    score.setPhysics(parseFloatScore(values, headerMap.get("vat_li")));
                    score.setChemistry(parseFloatScore(values, headerMap.get("hoa_hoc")));
                    score.setBiology(parseFloatScore(values, headerMap.get("sinh_hoc")));
                    score.setHistory(parseFloatScore(values, headerMap.get("lich_su")));
                    score.setGeography(parseFloatScore(values, headerMap.get("dia_li")));
                    score.setCivicEducation(parseFloatScore(values, headerMap.get("gdcd")));
                    score.setForeignLanguageCode(getStringValue(values, headerMap.get("ma_ngoai_ngu")));

                    batch.add(score);

                    if (batch.size() >= BATCH_SIZE) {
                        scoreRepository.saveAll(batch);
                        scoreRepository.flush(); // Xả bộ nhớ
                        batch.clear();
                    }

                    if (recordCount % 10000 == 0) {
                        log.info("Đã xử lý {} dòng...", recordCount);
                    }

                } catch (Exception e) {
                    errorCount++;
                    log.warn("Dòng lỗi {}: {} - {}", recordCount, e.getMessage(), line);
                }
            }

            if (!batch.isEmpty()) {
                scoreRepository.saveAll(batch);
                scoreRepository.flush();
                log.info("Đã lưu lô cuối cùng gồm {} bản ghi.", batch.size());
            }

            long endTime = System.currentTimeMillis();
            log.info("Hoàn tất. Tổng dòng: {}. Thành công: {}. Lỗi: {}. Thời gian: {} ms",
                    recordCount, recordCount - errorCount, errorCount, (endTime - startTime));

        } catch (IOException e) {
            log.error("Lỗi đọc file CSV: {}", e.getMessage(), e);
            throw new RuntimeException("Không thể đọc file CSV.", e);
        }
    }

    // Hàm trợ giúp lấy giá trị String từ mảng, trả về null nếu chỉ mục không hợp lệ
    private String getStringValue(String[] line, Integer index) {
        if (index == null || index < 0 || index >= line.length || line[index] == null) {
            return null;
        }
        return line[index].trim();
    }

    // Hàm trợ giúp parse String sang Float, trả về null nếu giá trị trống hoặc không hợp lệ
    private Float parseFloatScore(String[] line, Integer index) {
        String value = getStringValue(line, index);
        if (value == null || value.isEmpty()) {
            return null;
        }
        try {
            // Nếu điểm trong file CSV dùng dấu phẩy (,) thay vì chấm (.) thì bật dòng sau:
            // value = value.replace(',', '.');
            return Float.parseFloat(value);
        } catch (NumberFormatException e) {
            // Không ghi log ở đây để tránh quá nhiều log nếu file có nhiều lỗi định dạng
            // log.warn("Could not parse float score from value '{}' at index {}. Returning null.", value, index != null ? index : "N/A");
            return null;
        }
    }
}