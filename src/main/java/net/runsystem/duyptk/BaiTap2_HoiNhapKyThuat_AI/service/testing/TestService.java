package net.runsystem.duyptk.BaiTap2_HoiNhapKyThuat_AI.service.testing;

import java.time.Instant;
import java.util.List;
import java.util.NoSuchElementException;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import net.runsystem.duyptk.BaiTap2_HoiNhapKyThuat_AI.domain.requestDTO.ReqCreateTestDTO;
import net.runsystem.duyptk.BaiTap2_HoiNhapKyThuat_AI.domain.responseDTO.ResTestDTO;
import net.runsystem.duyptk.BaiTap2_HoiNhapKyThuat_AI.domain.table.Option;
import net.runsystem.duyptk.BaiTap2_HoiNhapKyThuat_AI.domain.table.Question;
import net.runsystem.duyptk.BaiTap2_HoiNhapKyThuat_AI.domain.table.Test;
import net.runsystem.duyptk.BaiTap2_HoiNhapKyThuat_AI.domain.table.User;
import net.runsystem.duyptk.BaiTap2_HoiNhapKyThuat_AI.domain.table.Vocab;
import net.runsystem.duyptk.BaiTap2_HoiNhapKyThuat_AI.repository.TestRepository;
import net.runsystem.duyptk.BaiTap2_HoiNhapKyThuat_AI.repository.UserRepository;
import net.runsystem.duyptk.BaiTap2_HoiNhapKyThuat_AI.util.SecurityUtil;

@Service
@RequiredArgsConstructor
public class TestService {
    private static final int MINIMUM_VOCABS_FOR_OPTIONS = 4;

    private final UserRepository userRepository;
    private final TestRepository testRepository;
    private final VocabSourceResolver vocabSourceResolver;
    private final QuestionFactory questionFactory;
    private final OptionGenerator optionGenerator;
    private final TestResponseMapper testResponseMapper;

    @Transactional
    public ResTestDTO create(ReqCreateTestDTO request) {
        validateRequest(request);
        User user = currentUser();
        int numberOfQuestion = request.getNumberOfQuestion();
        List<Vocab> sourceVocabs = vocabSourceResolver.resolve(
                request.getSourceItemIds(),
                Math.max(numberOfQuestion, MINIMUM_VOCABS_FOR_OPTIONS));
        Test test = Test.builder()
                .user(user)
                .numberOfQuestion(numberOfQuestion)
                .timeInMinute(normalizedTimeInMinute(request.getTimeInMinute()))
                .startedAt(Instant.now())
                .build();

        for (Vocab vocab : sourceVocabs.subList(0, numberOfQuestion)) {
            addQuestion(test, vocab, sourceVocabs);
        }

        return testResponseMapper.convertToDTO(testRepository.save(test));
    }

    @Transactional(readOnly = true)
    public ResTestDTO get(Long testId) {
        return testResponseMapper.convertToDTO(findOwnedTest(testId));
    }

    @Transactional(readOnly = true)
    public ResTestDTO result(Long testId) {
        Test test = findOwnedTest(testId);
        if (test.getFinishedAt() == null) {
            throw new IllegalArgumentException("Bài test chưa kết thúc");
        }
        return testResponseMapper.convertToDTO(test);
    }

    /* default */ Test findOwnedTest(Long testId) {
        if (testId == null || testId < 1) {
            throw new IllegalArgumentException("Test id phải lớn hơn 0");
        }
        User user = currentUser();
        return testRepository.findByIdAndUserId(testId, user.getId())
                .orElseThrow(() -> new NoSuchElementException(
                        "Bài test không tồn tại hoặc không thuộc người dùng hiện tại"));
    }

    private void addQuestion(Test test, Vocab vocab, List<Vocab> sourceVocabs) {
        QuestionFactory.QuestionResult questionResult = questionFactory.create(vocab);
        Question question = questionResult.getQuestion();
        question.setTest(test);
        List<Option> options = optionGenerator.generate(question, questionResult.getQuestionType(), sourceVocabs);
        question.getOptions().addAll(options);
        test.getQuestions().add(question);
    }

    private void validateRequest(ReqCreateTestDTO request) {
        if (request == null) {
            throw new IllegalArgumentException("Request tạo bài test không được để trống");
        }
        if (request.getNumberOfQuestion() == null || request.getNumberOfQuestion() < 1) {
            throw new IllegalArgumentException("Số lượng câu hỏi phải lớn hơn hoặc bằng 1");
        }
        if (request.getTimeInMinute() != null && request.getTimeInMinute() < 0) {
            throw new IllegalArgumentException("Thời gian làm bài không được nhỏ hơn 0");
        }
    }

    private User currentUser() {
        String email = SecurityUtil.getCurrentUserLogin()
                .orElseThrow(() -> new NoSuchElementException("Không tìm thấy người dùng hiện tại"));
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new NoSuchElementException("Không tìm thấy người dùng hiện tại"));
    }

    private Integer normalizedTimeInMinute(Integer timeInMinute) {
        if (timeInMinute == null || timeInMinute == 0) {
            return null;
        }
        return timeInMinute;
    }

}
