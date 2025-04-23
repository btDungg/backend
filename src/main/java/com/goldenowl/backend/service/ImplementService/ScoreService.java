package com.goldenowl.backend.service.ImplementService;

import com.goldenowl.backend.dto.ScoreDto;
import com.goldenowl.backend.dto.ScoreReportDto;
import com.goldenowl.backend.dto.SubjectStatsDto;
import com.goldenowl.backend.dto.TopStudentDto;
import com.goldenowl.backend.entity.Score;
import com.goldenowl.backend.exception.ResourceNotFoundException;
import com.goldenowl.backend.repository.ScoreRepository;
import com.goldenowl.backend.service.interfaceService.IScoreService;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class ScoreService implements IScoreService {
    private static final Logger log = LoggerFactory.getLogger(ScoreService.class);

    private final ScoreRepository scoreRepository;

    public ScoreService(ScoreRepository scoreRepository) {
        this.scoreRepository = scoreRepository;
    }

    @PersistenceContext
    private EntityManager entityManager;

    private static final float LEVEL1_MIN = 8.0f;
    private static final float LEVEL2_MIN = 6.0f;
    private static final float LEVEL3_MIN = 4.0f;
    private static final float LEVEL4_MAX = 4.0f;

    private static final List<String> SUBJECT_FIELDS = Arrays.asList(
            "math", "literature", "foreignLanguage", "physics", "chemistry",
            "biology", "history", "geography", "civicEducation"
    );

    public ScoreDto mapToScoreDto(Score score) {
        ScoreDto dto = new ScoreDto();
        dto.setRegistrationNumber(score.getRegistrationNumber());
        dto.setMath(score.getMath());
        dto.setLiterature(score.getLiterature());
        dto.setForeignLanguage(score.getForeignLanguage());
        dto.setPhysics(score.getPhysics());
        dto.setChemistry(score.getChemistry());
        dto.setBiology(score.getBiology());
        dto.setHistory(score.getHistory());
        dto.setGeography(score.getGeography());
        dto.setCivicEducation(score.getCivicEducation());
        dto.setForeignLanguageCode(score.getForeignLanguageCode());

        return dto;
    }

    @Override
    public ScoreDto findScoreByRegistrationNumber(String registrationNumber) {
        log.debug("Đang tìm điểm cho SBD: {}", registrationNumber);
        Score score = scoreRepository.findByRegistrationNumber(registrationNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy kết quả cho SBD: " + registrationNumber));
        return mapToScoreDto(score);
    }

    @Override
    public ScoreReportDto generateScoreReport() {
        log.info("Đang tạo báo cáo thống kê điểm...");
        Map<String, SubjectStatsDto> subjectStatsMap = new LinkedHashMap<>();

        for (String subjectField : SUBJECT_FIELDS) {
            try {
                log.debug("Đang tính toán thống kê cho môn: {}", subjectField);
                SubjectStatsDto stats = calculateStatsForSubject(subjectField);
                subjectStatsMap.put(subjectField, stats);
                log.debug("Thống kê cho môn {}: {}", subjectField, stats);
            } catch (Exception e) {
                log.error("Lỗi khi tính toán thống kê cho môn: {}", subjectField, e);
                // Thêm một giá trị mặc định vào map
                subjectStatsMap.put(subjectField, new SubjectStatsDto(0, 0, 0, 0));
            }
        }

        log.info("Tạo báo cáo thống kê điểm thành công.");
        return new ScoreReportDto(subjectStatsMap);
    }

    private SubjectStatsDto calculateStatsForSubject(String subjectField) {
        // Đếm số lượng điểm trong từng mức
        long level1 = countScoresInRange(subjectField, LEVEL1_MIN, Float.MAX_VALUE);      // >= 8
        long level2 = countScoresInRange(subjectField, LEVEL2_MIN, LEVEL1_MIN);          // >= 6 và < 8
        long level3 = countScoresInRange(subjectField, LEVEL3_MIN, LEVEL2_MIN);          // >= 4 và < 6
        long level4 = countScoresInRange(subjectField, 0.0f, LEVEL3_MIN);                // >= 0 và < 4

        return new SubjectStatsDto(level1, level2, level3, level4);
    }

    private long countScoresInRange(String subjectField, float minScoreInclusive, float maxScoreExclusive) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Long> cq = cb.createQuery(Long.class);
        Root<Score> root = cq.from(Score.class);

        cq.select(cb.count(root));

        Predicate predicate;

        if (maxScoreExclusive == Float.MAX_VALUE) {
            // Trường hợp >= minScore
            predicate = cb.greaterThanOrEqualTo(root.get(subjectField), minScoreInclusive);
        } else if (minScoreInclusive == 0.0f) {
            // Trường hợp < maxScore
            predicate = cb.lessThan(root.get(subjectField), maxScoreExclusive);
        } else {
            // Trường hợp between: >= minScore và < maxScore
            predicate = cb.and(
                    cb.greaterThanOrEqualTo(root.get(subjectField), minScoreInclusive),
                    cb.lessThan(root.get(subjectField), maxScoreExclusive)
            );
        }

        cq.where(predicate);

        TypedQuery<Long> query = entityManager.createQuery(cq);

        log.debug("Đang thực thi query đếm điểm môn {} trong khoảng [{}, {})",
                subjectField, minScoreInclusive, maxScoreExclusive);

        try {
            Long result = query.getSingleResult();
            log.debug("Kết quả đếm {}: {}", subjectField, result);
            return result;
        } catch (Exception e) {
            log.error("Lỗi khi đếm điểm cho môn {}: {}", subjectField, e.getMessage());
            return 0;
        }
    }


    @Override
    public List<TopStudentDto> getTopAStudents(int count){
        if (count <= 0) {
            return Collections.emptyList(); // Trả về danh sách rỗng nếu count không hợp lệ
        }
        // Tạo đối tượng Pageable để yêu cầu trang đầu tiên (index 0) với số lượng 'count'
        Pageable topN = PageRequest.of(0, count);
        log.debug("Đang lấy top {} sinh viên khối A", count);

        // Gọi phương thức repository đã định nghĩa với JPQL
        List<Score> topScores = scoreRepository.findTopGroupAStudents(topN);
        log.info("Đã lấy được {} bản ghi top sinh viên khối A", topScores.size());

        // Chuyển đổi danh sách Entity (Score) sang danh sách DTO (TopStudentDto)
        return topScores.stream()
                .map(this::mapToTopStudentDto) // Gọi hàm map cho từng phần tử
                .collect(Collectors.toList()); // Thu thập kết quả thành List
    }
    private TopStudentDto mapToTopStudentDto(Score score) {
        TopStudentDto dto = new TopStudentDto();
        dto.setRegistrationNumber(score.getRegistrationNumber());
        dto.setMath(score.getMath());
        dto.setPhysics(score.getPhysics());
        dto.setChemistry(score.getChemistry());
        // Tính tổng điểm khối A (cẩn thận với giá trị null nếu query chưa loại trừ hoàn toàn)
        float totalScore = (score.getMath() != null ? score.getMath() : 0f) +
                (score.getPhysics() != null ? score.getPhysics() : 0f) +
                (score.getChemistry() != null ? score.getChemistry() : 0f);
        dto.setTotalScoreA(totalScore);
        return dto;
    }
}