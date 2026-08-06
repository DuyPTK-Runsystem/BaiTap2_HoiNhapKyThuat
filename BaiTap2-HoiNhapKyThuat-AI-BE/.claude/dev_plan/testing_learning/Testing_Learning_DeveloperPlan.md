# Developer Plan: Testing & Learning Module

## 1. Trạng thái

- Trạng thái phê duyệt: Đã phê duyệt Phase 1, Phase 2, Phase 3, Phase 4 và rule cập nhật `Vocab.mastered`.
- Trạng thái triển khai: Đã triển khai Phase 1, Phase 2, Phase 3, Phase 4 và rule cập nhật `Vocab.mastered`.
- Ngày tạo plan: 2026-08-05.
- Agent tạo plan: RunSystem Assistant.
- Phạm vi đề xuất: Testing & Learning, gồm Multiple Choice và Flashcard; triển khai theo phase trong mục 5.

## 2. Mục tiêu

Triển khai module Testing & Learning theo đặc tả dự án:

- Tạo bài kiểm tra Multiple Choice tự động từ các `Vocab`.
- Hỗ trợ chọn một hoặc nhiều `Folder`/`VocabSet` làm nguồn.
- Thu thập vocab đệ quy từ Folder và các item con.
- Sinh câu hỏi theo 5 template được đặc tả.
- Sinh đúng 4 option cho mỗi câu, random vị trí đáp án đúng và bảo đảm chỉ có một đáp án đúng.
- Lưu phiên kiểm tra, câu hỏi và option để tracking kết quả.
- Hỗ trợ giới hạn thời gian khi `time_in_minute > 0`.
- Cung cấp Flashcard động, không lưu `Test`, `Question` hoặc `Option` vào database.
- Bảo vệ các endpoint bằng authenticated-only authorization; không thêm Role/Permission.

## 3. Tài liệu đối chiếu

### 3.1. `.claude/docs/modules/Testing_Module.md`

- Mục 1.1, dòng 5-15: Test Session gồm owner, nguồn vocab, số câu, thời gian và thống kê đúng/sai.
- Mục 1.2, dòng 17-24: Question gồm vocab nguồn, nội dung, đáp án đúng và audio.
- Mục 1.3, dòng 26-33: Option gồm thứ tự, nội dung, cờ đúng/sai và audio.
- Mục 2.1, dòng 39-43: mỗi câu đúng 4 option, unique order và duy nhất một đáp án đúng.
- Mục 2.2, dòng 45-57: 5 template sinh câu hỏi và quy tắc audio.
- Mục 2.3, dòng 59-64: nguồn Folder/VocabSet phải truy vấn đệ quy; nguồn null lấy ngẫu nhiên toàn bộ Vocab.
- Mục 2.4, dòng 66-70: Flashcard không lưu dữ liệu phiên vào database; định nghĩa mặt trước/mặt sau.
- Mục 3, dòng 74-79: checklist BE về recursive lookup, QuestionFactory, randomize options và time limit.

### 3.2. `.claude/docs/Data_Architecture.md`

- Mục 2.4, dòng 71-108: logical schema cho `tests`, `test_items`, `questions`, `options`.
- Mục 3.1, dòng 114-119: recursive CTE để lấy toàn bộ vocab từ item.
- Mục 3.2, dòng 120-128: cascade, unique option order và integrity của đáp án.
- Mục 3.3, dòng 130-132: index và tối ưu truy vấn.

### 3.3. `.claude/docs/ApplicationContext.md`

- Mục 2, dòng 17-20: Multiple Choice tự sinh, có time limit tùy chọn, Flashcard không lưu database và source có thể là item hoặc toàn bộ database.

### 3.4. Quy tắc nền tảng

- `.claude/rules/CLAUDE.md`: phải có plan được phê duyệt trước khi code; báo cáo phạm vi trước khi sửa file.
- `.claude/workflows/WORKFLOW.md`: không tự ý thay đổi dependency, build config hoặc kiến trúc ngoài plan.
- Auth/Authz project-specific rules: chỉ authenticated-only authorization; không Role/Permission hoặc claims tương ứng.

## 4. Kiến trúc hiện tại và thành phần tái sử dụng

- Package gốc: `net.runsystem.duyptk.BaiTap2_HoiNhapKyThuat_AI`.
- Layer hiện tại: `controller`, `service`, `repository`, `domain/table`, `domain/requestDTO`, `domain/responseDTO`, `config`, `util`.
- `Vocab` đã có `vocab_id`, `word`, `meaning`, `ipa`, `audio_url`.
- `Item`/`Folder`/`VocabSet` đã có inheritance JOINED, `parent_id`, owner và quan hệ `vocab_vocab_set`.
- Các repository Organization hiện có thể tái sử dụng cho ownership và source lookup.
- `SecurityUtil` và pattern service hiện tại được dùng để lấy authenticated user.
- Hibernate đang quản lý schema qua `spring.jpa.hibernate.ddl-auto`; chưa có Flyway/Liquibase migration.
- JUnit, Mockito/AssertJ, JaCoCo, Checkstyle, PMD và custom HTML test report đã có trong build.

## 5. Phạm vi triển khai đề xuất theo phase

### Phase 1: Source resolution và domain persistence

Bao gồm:

- Entity `Test`, `TestItem`, `Question`, `Option` theo logical schema.
- Repository cho bốn entity và truy vấn cần thiết.
- Kiểm tra ownership của tất cả source item theo authenticated user.
- Resolve nhiều source `Folder`/`VocabSet`.
- Duyệt cây `Folder -> Folder/VocabSet` để thu thập vocab, deduplicate theo `vocab_id`.
- Với source null, lấy ngẫu nhiên từ toàn bộ bảng `Vocab`.
- Validation `number_of_question >= 1` và không vượt số vocab có thể dùng.
- Cascade quan hệ từ Test đến TestItem/Question/Option theo thiết kế.

Không bao gồm trong phase này:

- Flashcard.
- API submit/finish nếu chưa cần để tạo và trả đề cơ bản.
- Recursive tree API cho Organization.
- Thay đổi schema Organization hiện tại.

### Phase 2: QuestionFactory và tạo đề

Bao gồm:

- Enum/strategy nội bộ cho 5 loại câu hỏi.
- Sinh nội dung theo đúng 5 template trong Testing Module Specification.
- Sinh distractor từ các vocab khác, không trùng đáp án đúng.
- Sinh đúng 4 option; random `option_order` từ 1 đến 4.
- Bảo đảm duy nhất một option `is_correct = true` và option đúng khớp `Question.correct_answer`.
- Audio URL chỉ gắn khi loại câu hỏi yêu cầu audio.
- API tạo test và response trả test cùng câu hỏi/options theo contract được chốt trong quá trình phê duyệt.

Contract Phase 2 đề xuất để phê duyệt:

```text
POST /api/v1/tests
```

Request body:

- `sourceItemIds` (`List<Long>`, nullable/rỗng): nguồn Folder/VocabSet; nullable hoặc rỗng để lấy ngẫu nhiên toàn bộ `Vocab`.
- `numberOfQuestion` (`Integer`, bắt buộc, min = 1): số câu hỏi cần sinh.
- `timeInMinute` (`Integer`, nullable): `null` hoặc `0` là không giới hạn; giá trị dương được lưu nhưng xử lý deadline thuộc Phase 3.

Response body:

- `id`: ID bài test đã tạo.
- `numberOfQuestion`: số câu hỏi của bài test.
- `timeInMinute`: giới hạn thời gian đã lưu.
- `correctAnswerCount`, `incorrectAnswerCount`: mặc định `0`.
- `questions`: danh sách câu hỏi đã sinh.
- Mỗi question gồm `id`, `vocabId`, `questionContent`, `correctAnswer`, `audioUrl`, `options`.
- Mỗi option gồm `id`, `optionOrder`, `optionContent`, `correct`, `audioUrl`.

Quy tắc chọn loại câu hỏi Phase 2:

- Không thêm `questionTypes` vào request trong Phase 2 để tránh mở rộng contract ngoài yêu cầu docs.
- Service tự chọn ngẫu nhiên trong các template hợp lệ theo dữ liệu của vocab:
  - Template 1, 3: cần `word` và `meaning`.
  - Template 2, 4, 5: cần `audioUrl` khi template yêu cầu audio.
- Nếu vocab không đủ dữ liệu cho template được chọn, service chọn template hợp lệ khác cho chính vocab đó.
- Nếu không có template hợp lệ cho một vocab, tạo test thất bại với validation error rõ ràng.

Quy tắc distractor Phase 2:

- Distractor lấy từ danh sách vocab khả dụng do `VocabSourceResolver` trả về, không tự lấy ngoài source.
- Mỗi câu cần đủ 3 distractor khác đáp án đúng theo `optionContent`.
- Nếu source không đủ nội dung distractor duy nhất cho loại câu hỏi được chọn, tạo test thất bại với validation error rõ ràng.
- `option_order` được shuffle thành 1-4; chỉ một option có `correct = true`.

Không bao gồm trong Phase 2:

- Submit/finish/result.
- Tính remaining time/deadline hoặc từ chối submit khi hết hạn.
- Flashcard.
- API lấy lại test theo ID nếu không cần cho response tạo test.
- Thêm dependency, migration framework, Role/Permission hoặc authorization model mới.

### Phase 3: Test lifecycle và tracking

Bao gồm:

- API lấy đề của một test thuộc authenticated user.
- API submit câu trả lời hoặc hoàn tất test theo contract được phê duyệt.
- Lưu final answers của user vào bảng `test_answers`.
- Cập nhật `correct_answer_count` và `incorrect_answer_count` sau khi kết thúc.
- Xử lý `time_in_minute`: `null`/`0` là không giới hạn; giá trị dương tạo deadline và từ chối thao tác sau khi hết thời gian.
- API trả kết quả và trạng thái thời gian còn lại khi phù hợp.

Contract Phase 3 đề xuất để phê duyệt:

```text
GET  /api/v1/tests/{testId}
POST /api/v1/tests/{testId}/finish
GET  /api/v1/tests/{testId}/result
```

Không triển khai `POST /api/v1/tests/{testId}/answers` riêng trong Phase 3. Client gửi toàn bộ final answers trong request `finish`, BE lưu một bản ghi `TestAnswer` cho từng question tại thời điểm kết thúc bài test.

Request body cho `POST /api/v1/tests/{testId}/finish`:

- `answers` (`List<ReqTestAnswerDTO>`, bắt buộc): danh sách câu trả lời.
- Mỗi item gồm:
  - `questionId` (`Long`, bắt buộc): ID câu hỏi thuộc test.
  - `optionId` (`Long`, bắt buộc): ID option được chọn.

Response body cho `GET /api/v1/tests/{testId}`:

- Dùng lại cấu trúc `ResTestDTO` của Phase 2.
- Bổ sung `remainingTimeInSeconds` để client hiển thị thời gian còn lại.
- Bổ sung `finished` để client biết test đã kết thúc hay chưa.

Response body cho `POST /api/v1/tests/{testId}/finish` và `GET /api/v1/tests/{testId}/result`:

- `id`, `numberOfQuestion`, `timeInMinute`.
- `correctAnswerCount`, `incorrectAnswerCount`.
- `remainingTimeInSeconds`.
- `finished`.
- `questions` kèm options và final answer của user để client đối chiếu kết quả.

Quy tắc Phase 3:

- Chỉ owner hiện tại được lấy đề, finish hoặc lấy result.
- `finish` chỉ nhận các `questionId` thuộc test hiện tại.
- `optionId` phải thuộc đúng `questionId`.
- Nếu thiếu câu trả lời cho question trong test, lưu `TestAnswer.selectedOption = null` và tính câu đó là incorrect để tổng `correct + incorrect = numberOfQuestion`.
- Nếu có questionId trùng trong request, từ chối với validation error rõ ràng.
- Nếu test đã finish, không cho finish lại.
- Với `timeInMinute = null` hoặc `0`, `remainingTimeInSeconds = null` và không giới hạn thời gian.
- Với `timeInMinute > 0`, server tính remaining time từ thời điểm tạo test; nếu hết giờ thì từ chối `finish` và trả validation error rõ ràng.
- Mỗi cặp `(test, question)` chỉ có một `TestAnswer`.

### Bổ sung đã phê duyệt: cập nhật trạng thái mastered

- Trong `TestResultService.finish()`, khi đáp án được chọn là đúng, cập nhật
  `question.getVocab().setMastered(true)`.
- Khi đáp án sai hoặc không trả lời, không thay đổi giá trị hiện tại của `question.getVocab().mastered`.
- Không reset `mastered` về `false` trong bất kỳ trường hợp nào.
- Không thêm endpoint hoặc rule nào khác.
- Bổ sung unit test cho đáp án đúng, đáp án sai và vocab đã mastered.

Database/entity change cần phê duyệt cho Phase 3:

- Thêm `startedAt` vào entity `Test`, map column `started_at`, để có mốc server-side tính deadline/remaining time.
- Thêm `finishedAt` vào entity `Test`, map column `finished_at`, để biết test đã kết thúc và chặn finish lại.
- Thêm entity `TestAnswer`, map bảng `test_answers`, lưu final answer theo từng question.
- Thêm quan hệ cascade từ `Test` sang `TestAnswer`.
- Thêm unique constraint `(test_id, question_id)` cho `test_answers`.
- Không thêm enum status riêng để tránh tạo domain concept mới khi `finishedAt != null` đã đủ biểu diễn trạng thái kết thúc.

Không bao gồm trong Phase 3:

- Flashcard.
- Lưu nhiều lần chọn đáp án, draft answer, attempt history hoặc answer history.
- Chấm điểm từng phần hoặc scoring ngoài đúng/sai.
- API submit từng câu riêng lẻ.
- Thay đổi cách sinh question/options ở Phase 2.
- Role/Permission hoặc authorization model mới.

### Phase 4: Flashcard/Learning

Bao gồm:

- Service tạo danh sách Flashcard động từ cùng source resolver.
- Hỗ trợ front là meaning hoặc audio.
- Back là word và meaning/audio theo đặc tả.
- Không tạo hoặc lưu `Test`, `Question`, `Option` cho Flashcard.
- API tạo/lấy danh sách flashcard trong một request; contract request/response sẽ được chốt trước khi triển khai phase này.

Contract Phase 4 đề xuất để phê duyệt:

```text
POST /api/v1/flashcards
```

Request body:

- `sourceItemIds` (`List<Long>`, nullable/rỗng): nguồn Folder/VocabSet; nullable hoặc rỗng để lấy ngẫu nhiên toàn bộ `Vocab`.
- `numberOfFlashcards` (`Integer`, bắt buộc, min = 1): số flashcard cần trả.

Response body:

- `sourceItemIds`: source đã request.
- `numberOfFlashcards`: số flashcard trả về.
- `flashcards`: danh sách flashcard động.
- Mỗi flashcard gồm:
  - `vocabId`.
  - `frontType`: `MEANING` hoặc `AUDIO`.
  - `frontText`: có giá trị khi `frontType = MEANING`.
  - `frontAudioUrl`: có giá trị khi `frontType = AUDIO`.
  - `backWord`.
  - `backMeaning`.
  - `backAudioUrl`.

Quy tắc Phase 4:

- Dùng lại `VocabSourceResolver` để kiểm tra ownership, resolve source đệ quy và random fallback.
- Không lưu `Test`, `Question`, `Option`, `TestAnswer` hoặc bất kỳ flashcard session/progress nào vào database.
- BE random `frontType` cho từng flashcard giữa `MEANING` và `AUDIO` dựa trên dữ liệu hợp lệ của vocab.
- Nếu vocab có cả `meaning` và `audioUrl`, chọn ngẫu nhiên một trong hai front type.
- Nếu vocab chỉ có `meaning`, dùng `frontType = MEANING`.
- Nếu vocab chỉ có `audioUrl`, dùng `frontType = AUDIO`.
- Nếu vocab không có cả `meaning` lẫn `audioUrl`, vocab đó không hợp lệ để tạo flashcard.
- Back luôn trả `word`, `meaning`, `audioUrl` theo dữ liệu hiện có của `Vocab`; field nullable được giữ nullable.
- Nếu source không đủ vocab hợp lệ cho `numberOfFlashcards`, trả validation error rõ ràng.

Không bao gồm trong Phase 4:

- Lưu flashcard progress/session/history vào database.
- API update trạng thái học thuộc/chưa thuộc.
- Spaced repetition hoặc thuật toán ôn tập.
- Thay đổi Vocabulary provider IPA/audio.
- Role/Permission hoặc authorization model mới.

## 6. Phạm vi không thực hiện

- Role, Permission hoặc authority claims trong JWT.
- Public endpoint ngoài auth/register/login/refresh và docs/actuator đã được quy hoạch.
- Thay đổi User entity ngoài các field đã được tài liệu cho phép.
- Question bank hoặc câu hỏi do người dùng nhập thủ công.
- Lưu Flashcard session/progress vào database trong phase theo đặc tả hiện tại.
- Recursive tree API Organization đầy đủ.
- Move, rename, delete Folder/VocabSet.
- Thay đổi provider IPA/audio của Vocabulary.
- Thêm dependency hoặc migration framework nếu không có phê duyệt riêng.

## 7. Thiết kế entity và repository dự kiến

### 7.1. `Test`

Map bảng `tests`, gồm owner, `numberOfQuestion`, `timeInMinute`, `correctAnswerCount`, `incorrectAnswerCount` và quan hệ tới source/questions.

### 7.2. `TestItem`

Map bảng `test_items`, liên kết `Test` với `Item` nguồn. Khóa tổng hợp hoặc cách map tương đương phải bảo đảm cặp `(test_id, item_id)` không trùng.

### 7.3. `Question`

Map bảng `questions`, liên kết `Test` và `Vocab`, lưu nội dung, đáp án đúng và audio URL.

### 7.4. `Option`

Map bảng `options`, liên kết `Question`, lưu order, nội dung, `isCorrect` và audio URL. Database unique `(question_id, option_order)`; integrity duy nhất đáp án đúng được bảo đảm ở application logic nếu database không hỗ trợ partial index.

### 7.5. `TestAnswer`

Map bảng `test_answers`, liên kết `Test`, `Question` và `Option` cuối cùng người dùng chọn. `selectedOption` nullable để lưu trường hợp người dùng bỏ trống câu hỏi. Database unique `(test_id, question_id)`.

### 7.6. Component service dự kiến

- `VocabSourceResolver`: resolve source, ownership, recursive traversal và random fallback.
- `QuestionFactory`: dựng câu hỏi theo template.
- `OptionGenerator`: tạo distractor, random order và kiểm tra invariant.
- `TestService`: tạo/lấy test và phối hợp các component.
- `TestResultService`: finish test, lưu final answers, time limit và tracking.
- `FlashcardService`: tạo response động, không persistence.

Tên class/package có thể điều chỉnh trong báo cáo trước khi code nếu không thay đổi phạm vi hoặc kiến trúc.

## 8. API dự kiến cần phê duyệt

Đây là contract đề xuất vì `.claude/docs` chưa đặc tả endpoint cụ thể:

```text
POST /api/v1/tests
GET  /api/v1/tests/{testId}
POST /api/v1/tests/{testId}/answers
POST /api/v1/tests/{testId}/finish
GET  /api/v1/tests/{testId}/result
POST /api/v1/flashcards
```

Đề xuất request tạo test:

- `sourceItemIds`: danh sách `Folder`/`VocabSet` ID, nullable hoặc rỗng để dùng toàn bộ Vocab.
- `numberOfQuestion`: bắt buộc, tối thiểu 1.
- `timeInMinute`: nullable hoặc 0 nếu không giới hạn.
- `questionTypes`: chưa triển khai trong Phase 2 để giữ contract tối giản theo docs; có thể xem xét ở phase sau nếu được yêu cầu.

Các contract chi tiết về loại câu hỏi, cách submit nhiều đáp án, trạng thái test và flashcard front mode phải được chốt trước khi triển khai phase tương ứng.

Contract chi tiết cho Phase 2 được cập nhật tại mục 5, phần "Phase 2: QuestionFactory và tạo đề".
Contract chi tiết cho Phase 3 được cập nhật tại mục 5, phần "Phase 3: Test lifecycle và tracking".
Contract chi tiết cho Phase 4 được cập nhật tại mục 5, phần "Phase 4: Flashcard/Learning".

## 9. Unit test và report dự kiến

Chỉ dùng JUnit unit test thuần ở service/component, không tự thêm application context test, repository test hoặc `SecurityUtil` test nếu chưa được người dùng phê duyệt riêng.

Test dự kiến:

- `VocabSourceResolverTests`: ownership, Folder recursion, VocabSet source, null source, deduplication và thiếu số lượng vocab.
- `QuestionFactoryTests`: đủ 5 template, dữ liệu meaning/audio và correct answer.
- `OptionGeneratorTests`: đúng 4 option, một đáp án đúng, random order, không duplicate correct answer.
- `TestServiceTests`: validation, create test, ownership và response.
- `TestResultServiceTests`: submit/finish, thống kê đúng/sai và time limit.
- `FlashcardServiceTests`: front/back mode, audio/meaning và không persistence.

Cập nhật custom HTML report để:

- Ghi nhận module `testing_learning`.
- Liệt kê test case và module tương ứng.
- Tính JaCoCo coverage chỉ cho package Testing/Learning cần kiểm tra, không tính toàn hệ thống.
- Output HTML theo convention report hiện tại, sau khi chốt tên task/output.

## 10. File dự kiến thay đổi khi triển khai

Danh sách dưới đây là phạm vi dự kiến, sẽ được xác nhận lại trong báo cáo trước khi code từng phase:

- Tạo entity trong `src/main/java/.../domain/table/` cho `Test`, `TestItem`, `Question`, `Option`.
- Tạo request/response DTO trong `src/main/java/.../domain/requestDTO/` và `domain/responseDTO/`.
- Tạo repository Testing trong `src/main/java/.../repository/`.
- Tạo service trong `src/main/java/.../service/testing/` và `service/learning/` hoặc package tương đương thống nhất với codebase.
- Tạo controller trong `src/main/java/.../controller/`.
- Tạo unit test tương ứng dưới `src/test/java/.../service/testing/` và `service/learning/`.
- Chỉnh `src/test/.../report/TestHtmlReportGenerator.java` để map module Testing/Learning.
- Không chỉnh `.claude/docs` requirement trừ khi phát hiện mâu thuẫn và được người dùng yêu cầu.

File dự kiến riêng cho Phase 2:

- Tạo `src/main/java/.../domain/requestDTO/ReqCreateTestDTO.java` cho request tạo test.
- Tạo `src/main/java/.../domain/responseDTO/ResTestDTO.java` cho response test.
- Tạo `src/main/java/.../domain/responseDTO/ResQuestionDTO.java` cho response câu hỏi.
- Tạo `src/main/java/.../domain/responseDTO/ResOptionDTO.java` cho response option.
- Tạo `src/main/java/.../repository/TestRepository.java` để persist test cùng cascade questions/options.
- Tạo `src/main/java/.../service/testing/QuestionType.java` làm enum nội bộ cho 5 template.
- Tạo `src/main/java/.../service/testing/QuestionFactory.java` để sinh nội dung câu hỏi và đáp án đúng.
- Tạo `src/main/java/.../service/testing/OptionGenerator.java` để sinh 4 option và shuffle order.
- Tạo `src/main/java/.../service/testing/TestService.java` để orchestrate create test.
- Tạo `src/main/java/.../controller/TestController.java` cho `POST /api/v1/tests`.
- Tạo unit test `QuestionFactoryTests`, `OptionGeneratorTests`, `TestServiceTests`.
- Chỉnh `src/test/.../report/TestHtmlReportGenerator.java` chỉ khi cần bổ sung mapping test Phase 2.

File dự kiến riêng cho Phase 3:

- Chỉnh `src/main/java/.../domain/table/Test.java` để thêm `startedAt` và `finishedAt`.
- Tạo `src/main/java/.../domain/table/TestAnswer.java` để lưu final answers.
- Tạo `src/main/java/.../domain/requestDTO/ReqFinishTestDTO.java` cho request finish test.
- Tạo `src/main/java/.../domain/requestDTO/ReqTestAnswerDTO.java` cho từng câu trả lời.
- Tạo `src/main/java/.../domain/responseDTO/ResTestAnswerDTO.java` để trả final answer từng câu.
- Chỉnh `src/main/java/.../domain/responseDTO/ResQuestionDTO.java` để thêm final answer nếu test đã finish.
- Chỉnh `src/main/java/.../domain/responseDTO/ResTestDTO.java` để thêm `remainingTimeInSeconds` và `finished`.
- Tạo `src/main/java/.../repository/TestAnswerRepository.java` nếu cần query riêng final answers.
- Chỉnh `src/main/java/.../repository/TestRepository.java` để thêm query fetch owner/questions/options nếu cần.
- Chỉnh `src/main/java/.../service/testing/TestService.java` để bổ sung get test/result hoặc tách mapper dùng chung.
- Tạo `src/main/java/.../service/testing/TestResultService.java` để validate answer, finish test và cập nhật count.
- Chỉnh `src/main/java/.../controller/TestController.java` để thêm `GET /{testId}`, `POST /{testId}/finish`, `GET /{testId}/result`.
- Tạo unit test `TestResultServiceTests`.
- Chỉnh unit test `TestServiceTests` nếu cần để kiểm tra remaining time trong response.
- Chỉnh `src/test/.../report/TestHtmlReportGenerator.java` nếu cần bổ sung mapping class Phase 3.

File dự kiến riêng cho Phase 4:

- Tạo `src/main/java/.../domain/requestDTO/ReqCreateFlashcardDTO.java` cho request flashcard.
- Tạo `src/main/java/.../domain/responseDTO/ResFlashcardDTO.java` cho từng flashcard.
- Tạo `src/main/java/.../domain/responseDTO/ResFlashcardSessionDTO.java` cho response danh sách flashcard động.
- Tạo `src/main/java/.../service/testing/FlashcardFrontType.java` làm enum loại mặt trước trong response.
- Tạo `src/main/java/.../service/testing/FlashcardService.java` để resolve vocab, validate mode và map response.
- Chỉnh `src/main/java/.../controller/TestController.java` hoặc tạo controller riêng nếu cần để thêm `POST /api/v1/flashcards`.
- Tạo unit test `FlashcardServiceTests`.
- Chỉnh `src/test/.../report/TestHtmlReportGenerator.java` nếu cần bổ sung mapping class Phase 4.

## 11. Rủi ro và quyết định cần xác nhận

- `.claude/docs` mô tả business rules nhưng chưa định nghĩa API request/response và lifecycle status đầy đủ.
- Recursive CTE được nêu trong kiến trúc, trong khi source code hiện có chưa có migration framework; cần quyết định dùng JPQL/service traversal hay native recursive query.
- Nếu một vocab thiếu `meaning` hoặc audio, cần chốt loại template được bỏ qua hay dùng fallback.
- Tạo distractor có thể không đủ 3 vocab khác khi dữ liệu nguồn ít hoặc trùng nội dung.
- Time limit cần chốt semantics giữa server deadline và client remaining time.
- `Test` là tên dễ gây xung đột với class `org.junit.jupiter.api.Test`; cần dùng import đầy đủ/đặt tên entity phù hợp nhưng vẫn map bảng `tests`.
- Số lượng entity/option và lazy loading có thể ảnh hưởng hiệu năng; tránh N+1 và chỉ fetch dữ liệu cần trả.
- Phase 1-4 có thể được phê duyệt toàn bộ hoặc từng phase; không code phase chưa được phê duyệt.

## 12. Tiêu chí nghiệm thu dự kiến

- Source thuộc user hiện tại mới được dùng để tạo test.
- Folder source thu thập được toàn bộ vocab từ descendant item và không trùng.
- Test không tạo nếu số vocab khả dụng nhỏ hơn `number_of_question`.
- Mỗi question có đúng 4 option, đúng một option đúng và option đúng khớp correct answer.
- Cả 5 template sinh đúng nội dung và audio theo docs.
- Time limit hoạt động đúng với null/0 và giá trị dương.
- Final answers được lưu vào `test_answers`, kể cả câu bị bỏ trống.
- Flashcard không ghi `Test`, `Question`, `Option` vào database.
- Unit tests, Checkstyle, PMD, build và HTML JaCoCo report đạt theo cấu hình project.
- Không có Role/Permission hoặc thay đổi public endpoint ngoài phạm vi được phê duyệt.

## 13. Trạng thái phê duyệt

- Phase 1 đã được người dùng phê duyệt và triển khai.
- Phase 2 đã được người dùng phê duyệt và triển khai.
- Phase 3 đã được người dùng phê duyệt và triển khai.
- Phase 4 đã được người dùng phê duyệt và triển khai.
- Rule cập nhật `Vocab.mastered` khi trả lời đúng đã được người dùng phê duyệt.

## 14. Lịch sử cập nhật

| Ngày | Nội dung | Người cập nhật |
|---|---|---|
| 2026-08-05 | Tạo Developer Plan Testing & Learning; tách phạm vi Multiple Choice, lifecycle và Flashcard thành các phase; ghi nhận API đề xuất, test strategy và rủi ro cần xác nhận | RunSystem Assistant |
| 2026-08-05 | Người dùng phê duyệt và triển khai Phase 1: entity persistence, recursive source resolution, ownership validation, unit test và report mapping | RunSystem Assistant |
| 2026-08-05 | Cập nhật chi tiết Phase 2: contract `POST /api/v1/tests`, DTO/response, QuestionFactory, OptionGenerator, TestService, test dự kiến và trạng thái chờ phê duyệt | Codex |
| 2026-08-05 | Người dùng phê duyệt và triển khai Phase 2: `POST /api/v1/tests`, QuestionFactory, OptionGenerator, TestService, DTO response và unit test | Codex |
| 2026-08-05 | Cập nhật chi tiết Phase 3 chờ phê duyệt: get test/result, finish test, time remaining, startedAt/finishedAt và TestResultService | Codex |
| 2026-08-05 | Cập nhật Phase 3 theo yêu cầu người dùng: thêm `TestAnswer` để lưu final answer của user khi finish test | Codex |
| 2026-08-05 | Người dùng phê duyệt và triển khai Phase 3: get/result/finish test, time remaining, `TestAnswer` lưu final answers và unit test | Codex |
| 2026-08-06 | Ghi nhận rule chờ phê duyệt: đáp án đúng đánh dấu Vocab tương ứng `mastered = true`, đáp án sai không thay đổi trạng thái | RunSystem Assistant |
| 2026-08-05 | Cập nhật chi tiết Phase 4 chờ phê duyệt: `POST /api/v1/flashcards`, front mode meaning/audio, response động và không persistence | Codex |
| 2026-08-05 | Cập nhật Phase 4 theo yêu cầu người dùng: bỏ `frontMode` request, BE random mặt trước Meaning/Audio theo dữ liệu hợp lệ | Codex |
| 2026-08-05 | Người dùng phê duyệt và triển khai Phase 4: `POST /api/v1/flashcards`, flashcard động random Meaning/Audio và không persistence | Codex |
| 2026-08-06 | Người dùng phê duyệt rule cập nhật `Vocab.mastered`; triển khai trong `TestResultService.finish()` và bổ sung unit test | Codex |
