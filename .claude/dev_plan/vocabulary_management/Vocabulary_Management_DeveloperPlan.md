# Developer Plan: Vocabulary Management

## 1. Trạng thái

- Trạng thái phê duyệt: Đã phê duyệt phase tạo vocab kèm `vocabSetId`.
- Trạng thái triển khai: Đã triển khai create/get/update/bulk import và tạo/bulk import kèm `vocabSetId`.
- Ngày tạo plan: 2026-08-03.
- Agent tạo plan: Codex.
- Ngày cập nhật gần nhất: 2026-08-05.
- Agent cập nhật gần nhất: Codex.
- Lý do tạo/cập nhật plan: Đã triển khai API tạo/bulk import từ mới kèm query param `vocabSetId`.

## 2. Mục tiêu

Triển khai quick dumb test cho quản lý từ vựng theo tài liệu source-of-truth, gồm entity `Vocab`, API `POST /api/v1/vocabs` nhận request params, và dumb provider để kiểm thử pipeline tự động xử lý IPA/audio trước khi tích hợp thật `hcoles/voices`.

## 3. Tài liệu đối chiếu

- `.claude/docs/modules/Vocabulary_Module.md`
  - Mục 1: Entity `Vocab` gồm `vocab_id`, `word`, `meaning`, `ipa`, `audio_url`.
  - Mục 2, BM1 Manual Import:
    - Cho phép thêm từ lẻ.
    - Nếu không tìm thấy IPA, bắt buộc có đầy đủ `Word`, `Meaning`, `IPA` mới được lưu.
    - Khi update, chỉ cho phép sửa `Meaning`.
  - Mục 2, BM2 Bulk Import:
    - Import `.xlsx` có Partial Failure.
  - Mục 3 Automation:
    - Tự động chuyển hóa từ vựng sang IPA.
    - Tự động xử lý Audio URL.
  - Mục 3.1 IPA/Audio Provider Direction:
    - Provider mục tiêu là `hcoles/voices`.
    - Pipeline IPA mục tiêu: `English word -> phoneme -> IPA text`.
    - Pipeline audio mục tiêu: `English word -> audio file -> audio_url`.
    - Trước khi tích hợp thật, phải có dumb API/provider để kiểm thử pipeline IPA/audio.
- `.claude/docs/Data_Architecture.md`
  - Mục 2.3 Vocabulary Module:
    - Table `vocabs`.
    - Columns `vocab_id`, `word`, `meaning`, `ipa`, `audio_url`.
    - Junction table `vocab_vocab_set` dành cho quan hệ với Organization/VocabSet.
- `.claude/docs/ApplicationContext.md`
  - Mục 2 Core Business Rules:
    - Hỗ trợ import thủ công và qua `.xlsx`.
    - Partial Failure áp dụng cho import hàng loạt.
    - Hệ thống tự tìm IPA và Audio nếu có thể.
  - Mục 4 Module Map:
    - Vocabulary Management Module là module riêng.
- `.claude/rules/CLAUDE.md`
  - Bắt buộc có Developer Plan được phê duyệt trước khi code.
- `.claude/workflows/WORKFLOW.md`
  - Bước 1, 2, 3 quy định đọc docs, kiểm tra plan, báo cáo trước khi code.

## 4. Phạm vi thực hiện

### 4.0. Scope đã được phê duyệt: tạo vocab kèm `vocabSetId`

Triển khai:

- `POST /api/v1/vocabs?vocabSetId={id}`.
- `POST /api/v1/vocabs/bulk?vocabSetId={id}`.

Contract:

- `vocabSetId` là query param optional.
- Nếu không có `vocabSetId`, giữ behavior hiện tại:
  - `POST /api/v1/vocabs` trả `ResVocabDTO`.
  - `POST /api/v1/vocabs/bulk` trả `ResVocabBulkImportDTO`.
- Nếu có `vocabSetId`, backend tạo vocab theo rule hiện có rồi gắn vocab vừa tạo vào vocab set.
- `vocabSetId` phải thuộc authenticated user hiện tại.
- Single create kèm `vocabSetId` trả response giàu thông tin gồm `vocabSet`, `vocab`, `added`.
- Bulk import kèm `vocabSetId` chỉ gắn các dòng tạo vocab thành công; các dòng lỗi vẫn theo Partial Failure hiện có.
- Không tạo `VocabSet` mới trong Vocabulary module.
- Không cho client gửi `audioUrl`; backend vẫn tự sinh audio URL.

### 4.1. Quick Dumb Vocabulary Creation

Triển khai:

- Entity `Vocab` map table `vocabs`.
- Repository `VocabRepository`.
- Response DTO trả thông tin từ vựng.
- Service xử lý nghiệp vụ tạo từ vựng thủ công.
- Controller cho API tạo từ vựng nhanh bằng request params.

API dự kiến:

```text
POST  /api/v1/vocabs
```

### 4.2. Validation và business rules

- `word` bắt buộc khi tạo.
- Khi tạo, service cố gắng tự động bổ sung IPA nếu request không gửi IPA.
- Nếu sau bước tự động vẫn không có IPA thì request phải có đầy đủ `word`, `meaning`, `ipa`; nếu thiếu `meaning` hoặc `ipa` thì trả lỗi.
- `audio_url` có thể được request gửi lên hoặc được automation service tạo nếu có thể.
- Quick dumb test chưa triển khai update, nên rule update chỉ sửa `meaning` sẽ được giữ cho phase CRUD sau.

### 4.3. Automation boundary

Docs đã chỉ định hướng provider mục tiêu là `hcoles/voices`, nhưng người dùng yêu cầu kiểm thử bằng dumb API trước khi tích hợp thật:

- Tạo interface/service nội bộ để resolve IPA/audio.
- Tạo dumb provider implementation trong scope này để kiểm thử pipeline IPA/audio ở mức ứng dụng.
- Dumb provider không gọi network ngoài, không thêm dependency `hcoles/voices`, không tải model và không tạo audio thật.
- Dumb provider trả dữ liệu giả có thể dự đoán để service/controller/test xác nhận luồng:
  - IPA giả theo format cố định, ví dụ `/dummy-ipa/<word>/`.
  - Audio URL giả theo format cố định, ví dụ `/api/v1/vocabs/audio/dummy/<normalized-word>.wav`.
- Thiết kế giữ boundary để sau này thay dumb provider bằng implementation thật dùng `hcoles/voices`.
- Nếu dumb provider hoặc provider thật không resolve được IPA thì vẫn áp dụng rule: người dùng phải nhập đủ dữ liệu.

### 4.4. Provider thật dự kiến sau dumb API

Hướng tích hợp thật sau khi dumb API được kiểm thử:

- Dùng repository `hcoles/voices` cho Java local TTS/phonemizer.
- Pipeline IPA: `English word -> phoneme -> IPA text`.
- Pipeline audio: `English word -> audio file -> audio_url`.
- Khi triển khai thật mới xem xét dependency, model/dictionary, thư mục lưu audio và cơ chế expose static audio URL.
- Việc thêm dependency/model/download provider thật không nằm trong scope implementation đầu tiên này.

## 5. Phạm vi không thực hiện

Không thực hiện trong plan này:

- Bulk import `.xlsx`.
- Partial Failure implementation cho `.xlsx`.
- Entity `Item`, `Folder`, `VocabSet`.
- Junction table/entity `vocab_vocab_set`.
- API gắn `Vocab` vào `VocabSet`.
- API lấy chi tiết `GET /api/v1/vocabs/{id}`.
- API lấy danh sách `GET /api/v1/vocabs`.
- API cập nhật nghĩa `PATCH /api/v1/vocabs/{id}`.
- Request DTO cho create/update nếu API quick test dùng request params trực tiếp.
- Flashcard hoặc Testing/Learning.
- Tích hợp thật `hcoles/voices`, dependency/model/dictionary thật hoặc download model.
- Sinh audio file thật và lưu trữ static file thật.
- Thay đổi docs requirement.
- Thay đổi Auth/Authz ngoài việc các endpoint mới mặc định yêu cầu JWT theo cấu hình hiện tại.

Lý do: `DevPlanSummary.md` hiện đang tách `Import .xlsx` và `Organization` thành các plan/module tương lai; docs Data Architecture cũng đặt quan hệ `Vocab` - `VocabSet` phụ thuộc Organization module chưa triển khai.

## 6. Kiến trúc hiện tại

Package gốc:

```text
net.runsystem.duyptk.BaiTap2_HoiNhapKyThuat_AI
```

Các layer hiện có:

```text
controller
service
repository
domain/table
domain/requestDTO
domain/responseDTO
config
util
```

Auth/Authz đã cấu hình:

- JWT stateless security.
- Các endpoint không public sẽ yêu cầu authenticated user.
- Response wrapper `RestResponse`.
- Exception handler tập trung.
- Lombok là convention hiện tại cho entity/DTO/constructor injection.

## 7. Thiết kế database/entity

### 7.1. Entity `Vocab`

Field Java dự kiến:

- `Long id`.
- `String word`.
- `String meaning`.
- `String ipa`.
- `String audioUrl`.

Mapping dự kiến:

- Entity table: `vocabs`.
- `id` map column `vocab_id`, auto-increment.
- `word` map column `word`, not null.
- `meaning` map column `meaning`, nullable theo docs.
- `ipa` map column `ipa`, nullable theo Data Architecture nhưng bị ràng buộc bởi business rule khi không tự động resolve được.
- `audioUrl` map column `audio_url`, nullable.

Không thêm audit field cho `Vocab` trong plan này vì docs Vocabulary/Data Architecture chưa yêu cầu audit field cho bảng `vocabs`.

## 8. Thiết kế API dự kiến

### 8.1. `POST /api/v1/vocabs`

Request params:

```text
POST /api/v1/vocabs?word=hello&meaning=xin%20chao
POST /api/v1/vocabs?word=hello&meaning=xin%20chao&ipa=h%C9%99%CB%88l%C9%99%CA%8A&audioUrl=https://example.com/hello.mp3
```

Behavior:

- Tạo từ vựng mới.
- Nếu request params thiếu `ipa`, service gọi automation boundary để thử tìm IPA.
- Nếu request params thiếu `audioUrl`, service gọi automation boundary để thử tạo audio URL.
- Nếu vẫn không có IPA và request thiếu `meaning` hoặc `ipa`, trả lỗi validation/business.

Response:

```json
{
  "id": 1,
  "word": "hello",
  "meaning": "xin chào",
  "ipa": "həˈləʊ",
  "audio_url": "https://example.com/hello.mp3"
}
```

### 8.1.1. `POST /api/v1/vocabs?vocabSetId={id}`

Request:

```text
POST /api/v1/vocabs?vocabSetId=12
```

Body:

```json
{
  "word": "go",
  "meaning": "di chuyen",
  "ipa": "gəʊ"
}
```

Behavior:

- Tạo vocab mới theo toàn bộ rule của `POST /api/v1/vocabs`.
- Sau khi tạo thành công, gắn vocab mới vào vocab set `12`.
- Nếu `vocabSetId` không thuộc current user, trả lỗi và không gắn.

Response:

```json
{
  "vocabSet": {
    "id": 12,
    "name": "Common verbs",
    "description": "Basic daily verbs",
    "parentId": 11,
    "vocabCount": 3
  },
  "vocab": {
    "id": 5,
    "word": "go",
    "meaning": "di chuyen",
    "ipa": "gəʊ",
    "audio_url": "/api/v1/vocabs/audio/go.mp3"
  },
  "added": true
}
```

### 8.1.2. `POST /api/v1/vocabs/bulk?vocabSetId={id}`

Request:

```text
POST /api/v1/vocabs/bulk?vocabSetId=12
```

Behavior:

- Import `.xlsx` theo rule hiện có.
- Với mỗi dòng tạo vocab thành công, gắn vocab đó vào vocab set `12`.
- Dòng lỗi không được gắn và vẫn xuất hiện trong response lỗi từng dòng.
- Nếu `vocabSetId` không thuộc current user, trả lỗi trước khi xử lý file.

## 9. Danh sách file dự kiến tạo/chỉnh sửa

| File | Loại thay đổi | Mục đích |
|---|---|---|
| `src/main/java/net/runsystem/duyptk/BaiTap2_HoiNhapKyThuat_AI/domain/table/Vocab.java` | Tạo mới | Entity table `vocabs` |
| `src/main/java/net/runsystem/duyptk/BaiTap2_HoiNhapKyThuat_AI/repository/VocabRepository.java` | Tạo mới | Repository cho `Vocab` |
| `src/main/java/net/runsystem/duyptk/BaiTap2_HoiNhapKyThuat_AI/domain/responseDTO/ResVocabDTO.java` | Tạo mới | Response DTO cho từ vựng |
| `src/main/java/net/runsystem/duyptk/BaiTap2_HoiNhapKyThuat_AI/service/VocabAutomationService.java` | Tạo mới | Boundary tự động resolve IPA/audio |
| `src/main/java/net/runsystem/duyptk/BaiTap2_HoiNhapKyThuat_AI/service/DumbVocabAutomationService.java` | Tạo mới | Dumb provider để kiểm thử pipeline IPA/audio trước provider thật |
| `src/main/java/net/runsystem/duyptk/BaiTap2_HoiNhapKyThuat_AI/service/VocabService.java` | Tạo mới | Business logic Vocabulary |
| `src/main/java/net/runsystem/duyptk/BaiTap2_HoiNhapKyThuat_AI/controller/VocabController.java` | Tạo mới | API `/api/v1/vocabs` |
| `src/main/java/net/runsystem/duyptk/BaiTap2_HoiNhapKyThuat_AI/service/organization/VocabSetMembershipService.java` | Chỉnh sửa nếu cần | Tái sử dụng/bổ sung method gắn vocab mới vào vocab set |
| `src/main/java/net/runsystem/duyptk/BaiTap2_HoiNhapKyThuat_AI/domain/responseDTO/ResVocabSetVocabDTO.java` | Tái sử dụng | Response tạo vocab kèm vocab set |
| `src/main/java/net/runsystem/duyptk/BaiTap2_HoiNhapKyThuat_AI/util/error/GlobalExceptionHandler.java` | Chỉnh sửa nếu cần | Bổ sung mapping lỗi business nếu exception hiện tại chưa đủ |
| `src/test/java/net/runsystem/duyptk/BaiTap2_HoiNhapKyThuat_AI/service/VocabServiceTests.java` | Tạo mới | Unit test service layer |
| `src/test/java/net/runsystem/duyptk/BaiTap2_HoiNhapKyThuat_AI/report/TestHtmlReportGenerator.java` | Chỉnh sửa nếu cần | Map test/coverage report cho module `vocabulary_management` |

## 10. Vị trí thay đổi dự kiến

| File | Class/Component | Method/Khu vực | Nội dung thay đổi |
|---|---|---|---|
| `Vocab.java` | `Vocab` | Fields/mapping | `vocab_id`, `word`, `meaning`, `ipa`, `audio_url` |
| `VocabRepository.java` | `VocabRepository` | Interface methods | `JpaRepository<Vocab, Long>` |
| `ResVocabDTO.java` | `ResVocabDTO` | Fields/json mapping | Trả dữ liệu an toàn của `Vocab` |
| `VocabAutomationService.java` | `VocabAutomationService` | Resolve methods | Interface/boundary `resolveIpa`, `resolveAudioUrl` cho dumb provider và provider thật sau này |
| `DumbVocabAutomationService.java` | `DumbVocabAutomationService` | Resolve methods | Trả IPA/audio URL giả có format ổn định để kiểm thử pipeline |
| `VocabService.java` | `VocabService` | `create`, `convertToDTO` | Business rule tạo vocab qua request params và dumb automation |
| `VocabController.java` | `VocabController` | API methods | `POST /api/v1/vocabs` với `@RequestParam` |
| `VocabService.java` | `VocabService` | `createWithVocabSet` hoặc tương đương | Tạo vocab rồi gắn vào vocab set khi có `vocabSetId` |
| `VocabBulkImportService.java` | `VocabBulkImportService` | `importFile` overload hoặc tương đương | Bulk import và gắn các dòng thành công vào vocab set khi có `vocabSetId` |
| `VocabController.java` | `VocabController` | `create`, `bulkImport` | Nhận optional query param `vocabSetId` |
| `GlobalExceptionHandler.java` | Exception handler | Handler methods | Dùng `IdInvalidException` nếu đủ; chỉ thêm exception mới nếu cần |
| `VocabServiceTests.java` | Unit tests | Test methods | Tạo vocab, resolve IPA/audio boundary, reject invalid manual import |
| `TestHtmlReportGenerator.java` | Report generator | Module mapping/filter | Thêm module `vocabulary_management` vào HTML report nếu có test mới |

## 11. Unit test dự kiến

Unit test bằng JUnit cho `VocabService`:

- Create thành công khi request có đủ `word`, `meaning`, `ipa`.
- Create thành công khi request thiếu `ipa` nhưng automation resolve được IPA.
- Create thất bại khi thiếu `ipa`, automation không resolve được IPA và thiếu `meaning` hoặc `ipa`.
- Create tự động gán `audioUrl` khi automation resolve được audio và request chưa gửi `audioUrl`.
- Dumb provider trả IPA/audio URL giả đúng format dự kiến cho một English word.
- Controller/service nhận dữ liệu tạo vocab từ request params.
- Create vocab kèm `vocabSetId` trả thông tin vocab set/vocab/added.
- Bulk import kèm `vocabSetId` chỉ gắn các dòng thành công vào vocab set và vẫn giữ Partial Failure.

Không thêm application context test/repository test nếu người dùng không yêu cầu riêng.

## 12. Rủi ro và lưu ý

- Provider thật đã được định hướng là `hcoles/voices`, nhưng scope đầu tiên chỉ dùng dumb provider để kiểm thử pipeline.
- Tích hợp thật `hcoles/voices` có thể phát sinh dependency/model/dictionary, license và lưu trữ audio file, nên cần plan hoặc approval riêng.
- Docs chưa chỉ định unique constraint cho `word`, nên plan này không thêm unique để tránh tự ý mở rộng requirement.
- Bulk `.xlsx` có Partial Failure là requirement quan trọng nhưng sẽ cần plan riêng để không trộn scope với manual Vocabulary Management.
- Organization/VocabSet chưa triển khai, nên chưa implement quan hệ `vocab_vocab_set`.

## 13. Verification dự kiến

Sau khi được phê duyệt và triển khai, chạy:

```text
./gradlew test jacocoTestReport testHtmlReport checkstyleMain checkstyleTest pmdMain pmdTest
```

## 14. Output dự kiến

- API quick manual vocabulary `POST /api/v1/vocabs` hoạt động cho người dùng đã đăng nhập.
- `Vocab` được lưu đúng schema docs.
- Request params có thể dùng nhanh để thử dumb pipeline trước khi có CRUD đầy đủ.
- Automation IPA/audio có boundary rõ ràng và dumb provider để kiểm thử pipeline, không phụ thuộc network trong scope này.
- Unit test `VocabServiceTests` pass.
- HTML test report hiển thị thêm module `vocabulary_management` nếu có test tương ứng.

## 15. Lịch sử cập nhật

| Ngày | Nội dung | Người cập nhật |
|---|---|---|
| 2026-08-04 | Cập nhật hướng provider `hcoles/voices` và điều chỉnh scope đầu tiên sang dumb provider/API để kiểm thử pipeline IPA/audio trước khi tích hợp thật | Codex |
| 2026-08-04 | Thu hẹp quick dumb test còn `POST /api/v1/vocabs` với request params; chuyển GET/list/PATCH sang ngoài scope phase đầu | Codex |
| 2026-08-04 | Người dùng phê duyệt quick dumb test và cho phép bắt đầu triển khai | Người dùng |
| 2026-08-04 | Triển khai quick dumb test `POST /api/v1/vocabs` với dumb IPA/audio provider và unit test | Codex |
| 2026-08-05 | Cập nhật plan chờ phê duyệt cho `vocabSetId` query param khi tạo vocab hoặc bulk import | Codex |
| 2026-08-05 | Người dùng phê duyệt triển khai `vocabSetId` query param khi tạo vocab hoặc bulk import | Người dùng |
| 2026-08-05 | Triển khai `vocabSetId` query param khi tạo vocab hoặc bulk import | Codex |

## 16. Developer Plan cập nhật: Real `hcoles/voices` Provider

### 16.1. Trạng thái

- Trạng thái phê duyệt: Đã phê duyệt.
- Trạng thái triển khai: Đã triển khai.
- Ngày tạo cập nhật plan: 2026-08-04.
- Agent tạo cập nhật plan: Codex.
- Lý do cập nhật: Người dùng yêu cầu nhận URL tới audio file thật và nhìn thấy phoneme mà provider sử dụng.

### 16.2. Căn cứ tài liệu/source

- `.claude/docs/modules/Vocabulary_Module.md`
  - Mục 3.1: provider mục tiêu `hcoles/voices`.
  - Mục 3.2: `audio_url` phải trỏ tới file thật; response phải hiển thị `phoneme`.
- Repository `hcoles/voices`: `https://github.com/hcoles/voices`
  - Thư viện Java local TTS, không dùng external API.
  - Có English phonemizer và dictionary `en_uk`/`en_us`.
  - Usage chính dùng `Chorus`, `Voice`, `Audio audio = voice.say(text)`, `audio.save(path)`.
  - Dependency README nêu `chorus`, model như `alba`, dictionary `en_uk`/`en_us`, và `onnxruntime`.

### 16.3. Mục tiêu

- Thay dumb provider bằng real provider adapter dùng `hcoles/voices`.
- `POST /api/v1/vocabs` sinh được audio file thật từ English word.
- `audio_url` trong response và database trỏ tới URL HTTP có thể truy cập được.
- Response trả thêm `phoneme` để người dùng nhìn thấy phoneme provider đã dùng trong pipeline.

### 16.4. Phạm vi thực hiện

- Thêm dependency cần thiết cho `hcoles/voices` vào Gradle Kotlin DSL sau khi xác nhận artifact khả dụng:
  - `org.pitest.voices:chorus:0.0.9`.
  - Một model dependency, ưu tiên model nhỏ như `org.pitest.voices:alba:0.0.9`.
  - Dictionary, ưu tiên `org.pitest.voices:en_us:0.0.9` để tránh rủi ro GPL của `en_uk` trong phase đầu.
  - `com.microsoft.onnxruntime:onnxruntime:1.22.0`.
- Tạo provider thật, ví dụ `VoicesVocabAutomationService`, implement `VocabAutomationService`.
- Mở rộng contract automation để trả đủ:
  - `phoneme`.
  - `ipa`.
  - `audioUrl`.
- Sinh audio file thật vào thư mục cấu hình, ví dụ `${app.vocab.audio-storage-dir}`.
- Expose file audio qua HTTP URL, ví dụ `/api/v1/vocabs/audio/{fileName}` hoặc static resource handler được cấu hình rõ.
- Cập nhật `ResVocabDTO` thêm field `phoneme` dạng response-only.
- Giữ schema `vocabs` hiện tại: chỉ lưu `audio_url` và `ipa`, không thêm cột `phoneme` trong phase này vì Data Architecture chưa yêu cầu.
- Cập nhật unit test cho provider/service bằng test double hoặc mock boundary để không phụ thuộc model nặng khi unit test.

### 16.5. Phạm vi không thực hiện

- Không thêm cột `phoneme` vào bảng `vocabs` nếu chưa có approval riêng.
- Không triển khai `GET /api/v1/vocabs`, `PATCH /api/v1/vocabs/{id}` hoặc CRUD đầy đủ.
- Không dùng GPU runtime.
- Không dùng model downloader runtime nếu model dependency Maven Central đủ cho phase đầu.
- Không bulk import `.xlsx`.
- Không Organization/VocabSet.

### 16.6. API/output dự kiến

Request:

```text
POST /api/v1/vocabs?word=hello&meaning=xin%20chao
```

Response dự kiến:

```json
{
  "id": 1,
  "word": "hello",
  "meaning": "xin chao",
  "phoneme": "<provider phoneme output>",
  "ipa": "<ipa text>",
  "audio_url": "/api/v1/vocabs/audio/hello-<hash>.wav"
}
```

### 16.7. Rủi ro/lưu ý

- Cần xác nhận chính xác API public của `hcoles/voices` để lấy phoneme riêng; README mô tả phonemizer nhưng ví dụ chính chỉ thể hiện `voice.say(text)` và `audio.save(path)`.
- Nếu thư viện không expose phoneme trực tiếp từ `Voice.say`, có thể cần dùng phonemizer/dictionary class riêng hoặc adapter nội bộ của thư viện.
- Dependency/model có thể làm test/build nặng hơn.
- Audio storage cần chính sách file naming, overwrite/cache và dọn file ở phase sau.
- `en_uk` có lưu ý GPL trong README; phase đầu ưu tiên `en_us` nếu phù hợp.

### 16.8. Verification dự kiến

Sau khi được phê duyệt và triển khai, chạy:

```text
./gradlew test jacocoTestReport testHtmlReport checkstyleMain checkstyleTest pmdMain pmdTest
```

Ngoài ra cần kiểm tra thủ công:

```text
POST /api/v1/vocabs?word=hello&meaning=xin%20chao
GET  <audio_url trả về>
```

### 16.9. Lịch sử cập nhật

| Ngày | Nội dung | Người cập nhật |
|---|---|---|
| 2026-08-04 | Tạo plan chờ phê duyệt cho real `hcoles/voices` provider: audio file thật, audio URL thật và phoneme trong response | Codex |
| 2026-08-04 | Người dùng phê duyệt tiếp tục test để lấy audio file thật và phoneme provider sử dụng | Người dùng |
| 2026-08-04 | Triển khai real `hcoles/voices` provider, tạo WAV thật, trả audio URL thật và phoneme trong response | Codex |

## 17. Developer Plan cập nhật: Oxford Dictionaries API Provider

### 17.1. Trạng thái

- Trạng thái phê duyệt: Đã phê duyệt theo yêu cầu trực tiếp của người dùng.
- Trạng thái triển khai: Đã triển khai.
- Ngày tạo cập nhật plan: 2026-08-04.
- Agent tạo cập nhật plan: Codex.
- Lý do cập nhật: Người dùng đánh giá pipeline `hcoles/voices` chưa tốt và yêu cầu thử Oxford Dictionaries API.

### 17.2. Căn cứ tài liệu/source

- `.claude/docs/modules/Vocabulary_Module.md`
  - Mục 3.1: provider mục tiêu là Oxford Dictionaries API.
  - Mục 3.2: `audio_url` phải trỏ tới audio URL thật từ provider; response hiển thị `phoneme`.
- API người dùng cung cấp:
  - Base API: `https://od-api.oxforddictionaries.com/api/v2/words/en-gb?q=<WORD>`.
  - Credentials dùng qua biến cấu hình, không hard-code vào source hoặc collection:
    - `OXFORD_APP_ID`.
    - `OXFORD_API_KEY`.

### 17.3. Mục tiêu

- Thay provider chính từ `hcoles/voices` sang Oxford Dictionaries API.
- `POST /api/v1/vocabs` gọi Oxford bằng English word để lấy pronunciation.
- Response trả:
  - `phoneme`: pronunciation/phonetic text provider dùng.
  - `ipa`: IPA text tương ứng.
  - `audio_url`: URL audio thật từ Oxford nếu có.
- Không tự tải/sao chép audio file về backend trong phase này nếu Oxford trả URL audio trực tiếp.

### 17.4. Phạm vi thực hiện

- Thêm cấu hình:
  - `app.oxford.base-url`.
  - `app.oxford.app-id`.
  - `app.oxford.api-key`.
- Tạo provider mới, ví dụ `OxfordVocabAutomationService`, implement `VocabAutomationService`.
- Dùng HTTP client có sẵn trong Spring/Java để gọi Oxford, không thêm dependency nếu không cần.
- Parse response JSON bằng Jackson.
- Ưu tiên pronunciation đầu tiên có IPA/phonetic text và audio URL.
- Giữ `phoneme` response-only, không thêm cột DB.
- Xóa hoặc ngừng dùng provider `VoicesVocabAutomationService` khỏi Spring bean chính để tránh model nặng và pipeline cũ.
- Cập nhật unit test bằng mock HTTP/client hoặc tách parser để test không gọi network thật.
- Cập nhật Postman collection dùng biến `oxfordAppId` và `oxfordApiKey` thay vì hard-code secret.

### 17.5. Phạm vi không thực hiện

- Không hard-code Oxford app id/api key vào source, docs hoặc Postman collection.
- Không bulk import `.xlsx`.
- Không CRUD đầy đủ ngoài `POST /api/v1/vocabs`.
- Không thêm cột `phoneme`.
- Không cache audio file Oxford về local trong phase này.
- Không giữ `hcoles/voices` là provider chính.

### 17.6. Output dự kiến

Request:

```text
POST /api/v1/vocabs?word=hello&meaning=xin%20chao
```

Response dự kiến:

```json
{
  "id": 1,
  "word": "hello",
  "meaning": "xin chao",
  "phoneme": "<Oxford phonetic text>",
  "ipa": "<Oxford IPA text>",
  "audio_url": "<Oxford audio file URL>"
}
```

### 17.7. Rủi ro/lưu ý

- Oxford API có thể giới hạn quota hoặc trả response khác shape theo word.
- Audio URL có thể không tồn tại cho một số từ; khi đó chỉ lưu `audio_url` nếu provider trả thật.
- Nếu API không trả IPA, rule BM1 vẫn yêu cầu request có đủ `Word`, `Meaning`, `IPA`.
- Cần network để test provider thật; unit test nên mock parser/client.

### 17.8. Verification dự kiến

Sau khi triển khai, chạy:

```text
./gradlew test jacocoTestReport testHtmlReport checkstyleMain checkstyleTest pmdMain pmdTest
```

Manual/network check nếu user muốn test thật:

```text
OXFORD_APP_ID=<value> OXFORD_API_KEY=<value> ./gradlew bootRun
POST /api/v1/vocabs?word=hello&meaning=xin%20chao
```

### 17.9. Lịch sử cập nhật

| Ngày | Nội dung | Người cập nhật |
|---|---|---|
| 2026-08-04 | Tạo plan Oxford API provider, thay thế pipeline `hcoles/voices`, dùng env vars cho credentials | Codex |
| 2026-08-04 | Triển khai Oxford API provider, parse phoneme/audio URL, cập nhật test và Postman collection | Codex |

## 18. Developer Plan cập nhật: Free Dictionary API Provider

### 18.1. Trạng thái

- Trạng thái phê duyệt: Đã phê duyệt theo yêu cầu trực tiếp của người dùng.
- Trạng thái triển khai: Đã triển khai.
- Ngày tạo cập nhật plan: 2026-08-04.
- Agent tạo cập nhật plan: Codex.
- Lý do cập nhật: Người dùng tìm provider tốt hơn và yêu cầu dùng Free Dictionary API để lấy IPA.

### 18.2. Căn cứ tài liệu/source

- `.claude/docs/modules/Vocabulary_Module.md`
  - Mục 3.1: provider mục tiêu hiện tại là Free Dictionary API.
  - Mục 3.2: `phoneme` response-only, `audio_url` không dùng fake URL.
- API người dùng cung cấp:
  - Base API: `https://freedictionaryapi.com/api/v1/entries/{language}/{word}`.
  - Language: `en`.
  - Query param: không có.
  - Priority: trong `pronunciations`, chọn `text` của item đầu tiên có `type = "ipa"`.

### 18.3. Mục tiêu

- Thay provider chính từ Oxford sang Free Dictionary API.
- `POST /api/v1/vocabs` gọi Free Dictionary bằng English word để lấy IPA.
- Response trả:
  - `phoneme`: IPA text provider chọn.
  - `ipa`: cùng IPA text provider chọn.
  - `audio_url`: null nếu request không gửi, vì audio sẽ được xử lý bằng lib IPA -> voice trong phase sau.

### 18.4. Phạm vi thực hiện

- Thêm cấu hình:
  - `app.free-dictionary.base-url`.
  - `app.free-dictionary.language`.
- Tạo provider mới `FreeDictionaryVocabAutomationService`, implement `VocabAutomationService`.
- Dùng `RestClient` gọi Free Dictionary API.
- Parse JSON bằng Jackson.
- Ưu tiên pronunciation đầu tiên có `type = "ipa"`.
- Không dùng Oxford credentials.
- Không dùng `hcoles/voices` làm provider chính.
- Cập nhật unit test parser và service.
- Cập nhật Postman collection theo Free Dictionary.

### 18.5. Phạm vi không thực hiện

- Không triển khai audio generation.
- Không lưu `audio_url` giả.
- Không thêm cột `phoneme`.
- Không CRUD đầy đủ ngoài `POST /api/v1/vocabs`.
- Không bulk import `.xlsx`.
- Không Organization/VocabSet.

### 18.6. Output dự kiến

Request:

```text
POST /api/v1/vocabs?word=hello&meaning=xin%20chao
```

Response dự kiến:

```json
{
  "id": 1,
  "word": "hello",
  "meaning": "xin chao",
  "phoneme": "/həˈləʊ/",
  "ipa": "/həˈləʊ/",
  "audio_url": null
}
```

### 18.7. Rủi ro/lưu ý

- Free Dictionary API có thể không có IPA cho một số từ.
- Nếu provider không trả IPA, rule BM1 vẫn yêu cầu request có đủ `Word`, `Meaning`, `IPA`.
- Audio sẽ phụ thuộc provider/lib IPA -> voice sau này.

### 18.8. Verification dự kiến

Sau khi triển khai, chạy:

```text
./gradlew test jacocoTestReport testHtmlReport checkstyleMain checkstyleTest pmdMain pmdTest
```

### 18.9. Lịch sử cập nhật

| Ngày | Nội dung | Người cập nhật |
|---|---|---|
| 2026-08-04 | Tạo plan Free Dictionary API provider, thay thế Oxford, chỉ lấy IPA và chưa xử lý audio | Codex |
| 2026-08-04 | Triển khai Free Dictionary API provider, chọn IPA đầu tiên theo `pronunciations[type=ipa].text`, cập nhật test/Postman | Codex |
| 2026-08-04 | Dọn implementation dummy provider và Postman variable audio dư sau khi Free Dictionary là provider hiện tại | Codex |

## 19. Developer Plan cập nhật: Loại bỏ `phoneme` khỏi Vocabulary process

### 19.1. Trạng thái

- Trạng thái: Đã triển khai.
- Agent tạo cập nhật plan: Codex.
- Lý do cập nhật: Người dùng yêu cầu loại bỏ `phoneme` khỏi toàn bộ process.

### 19.2. Căn cứ tài liệu/source

- `.claude/docs/modules/Vocabulary_Module.md`
  - Mục 3.2: không dùng `phoneme` trong Vocabulary process, response DTO, provider result, Postman hoặc report/test scope.
  - API tạo vocab chỉ cần trả `ipa` là IPA text provider chọn.

### 19.3. Mục tiêu

- Loại bỏ field `phoneme` khỏi response DTO và automation result.
- Free Dictionary provider chỉ resolve và trả `ipa`.
- `POST /api/v1/vocabs` không trả `phoneme`.
- Khi cần sinh audio, dùng `GoogleTtsService.synthesizeIpa(word, ipa, languageCode)` để tạo MP3 bytes, lưu file và set `audio_url`.
- Postman collection không lưu/kiểm tra `phoneme`.
- Unit test và HTML report scope không còn kỳ vọng `phoneme`.

### 19.4. Phạm vi thực hiện

- Cập nhật `VocabAutomationResult` bỏ field `phoneme`.
- Cập nhật `ResVocabDTO` bỏ field `phoneme`.
- Cập nhật `FreeDictionaryVocabAutomationService` chỉ build result với `ipa`.
- Cập nhật `VocabService` không truyền/convert `phoneme`.
- Inject/use `GoogleTtsService` trong vocabulary audio flow để gọi `synthesizeIpa(...)` sau khi có `ipa`.
- Lưu byte MP3 trả về thành file thật trong thư mục cấu hình.
- Expose file audio qua URL HTTP và lưu URL đó vào `Vocab.audioUrl`.
- Thêm cấu hình cần thiết cho audio storage/static URL nếu chưa có.
- Cập nhật `VocabServiceTests`.
- Cập nhật `TestHtmlReportGenerator` nếu coverage/report scope còn nhắc tới class/field liên quan.
- Cập nhật Postman collection.

### 19.5. Phạm vi không thực hiện

- Không đổi schema `vocabs`.
- Không thêm provider mới.
- Không đổi endpoint ngoài response shape của `POST /api/v1/vocabs`.
- Không gọi Google TTS trong unit test thật; mock `GoogleTtsService` để tránh network/credential dependency.

### 19.6. Output dự kiến

Request:

```text
POST /api/v1/vocabs?word=hello&meaning=xin%20chao
```

Response dự kiến:

```json
{
  "id": 1,
  "word": "hello",
  "meaning": "xin chao",
  "ipa": "/həˈləʊ/",
  "audio_url": "/api/v1/vocabs/audio/hello-<hash>.mp3"
}
```

### 19.7. Verification dự kiến

Sau khi được phê duyệt và triển khai, chạy:

```text
./gradlew test jacocoTestReport testHtmlReport checkstyleMain checkstyleTest pmdMain pmdTest
```

### 19.8. Lịch sử cập nhật

| Ngày | Nội dung | Người cập nhật |
|---|---|---|
| 2026-08-04 | Tạo plan chờ phê duyệt để loại bỏ `phoneme` khỏi toàn bộ Vocabulary process | Codex |
| 2026-08-04 | Cập nhật plan chờ phê duyệt: sinh audio file bằng `GoogleTtsService.synthesizeIpa(...)` | Codex |
| 2026-08-04 | Triển khai `POST /api/v1/vocabs`: bỏ `phoneme`, sinh audio qua `GoogleTtsService.synthesizeIpa(...)` và trả `audio_url` | Codex |

## 20. Developer Plan cập nhật: Refactor service package theo module

### 20.1. Trạng thái

- Trạng thái: Đã triển khai.
- Agent tạo cập nhật plan: Codex.
- Lý do cập nhật: Người dùng nhận xét package `service` đang lộn xộn và chọn phương án tách service theo subpackage.

### 20.2. Căn cứ hiện trạng/source

- Các service hiện đang nằm phẳng trong `src/main/java/.../service`:
  - `UserService`
  - `VocabService`
  - `VocabAutomationService`
  - `FreeDictionaryVocabAutomationService`
  - `VocabAudioService`
  - `GoogleTtsService`
- Unit test service hiện nằm phẳng trong `src/test/java/.../service`.
- Đây là refactor package/import, không đổi requirement nghiệp vụ trong `.claude/docs`.

### 20.3. Mục tiêu

- Tách package service theo module/concern để dễ scan và mở rộng:

```text
service/
  auth/
    UserService.java
  vocab/
    VocabService.java
    VocabAutomationService.java
    FreeDictionaryVocabAutomationService.java
    VocabAudioService.java
  tts/
    GoogleTtsService.java
```

- Giữ nguyên behavior API, database schema, security và response.
- Cập nhật import ở controller, tests, report generator và các class liên quan.

### 20.4. Phạm vi thực hiện

- Move `UserService` sang package `service.auth`.
- Move các service thuộc Vocabulary sang package `service.vocab`.
- Move `GoogleTtsService` sang package `service.tts`.
- Cập nhật imports ở:
  - Auth controller/tests nếu tham chiếu `UserService`.
  - Vocabulary controller/tests nếu tham chiếu vocab/tts services.
  - `TestHtmlReportGenerator` coverage class prefixes.
- Giữ nguyên class name và public method hiện tại.

### 20.5. Phạm vi không thực hiện

- Không đổi endpoint/API contract.
- Không đổi schema/entity/repository.
- Không đổi logic Free Dictionary hoặc Google TTS.
- Không đổi dependency Gradle.
- Không refactor DTO/controller/repository sang package con trong scope này.

### 20.6. Rủi ro/lưu ý

- Import/package sai có thể gây lỗi compile; verification bắt buộc sẽ chạy full.
- HTML report coverage mapping cần đổi package path để coverage module vẫn đúng.
- Component scanning vẫn hoạt động vì subpackage nằm dưới root package Spring Boot.

### 20.7. Verification dự kiến

Sau khi được phê duyệt và triển khai, chạy:

```text
./gradlew test jacocoTestReport testHtmlReport checkstyleMain checkstyleTest pmdMain pmdTest
```

### 20.8. Lịch sử cập nhật

| Ngày | Nội dung | Người cập nhật |
|---|---|---|
| 2026-08-04 | Tạo plan chờ phê duyệt để refactor service package thành `auth`, `vocab`, `tts` | Codex |
| 2026-08-04 | Triển khai refactor service package thành `auth`, `vocab`, `tts` và cập nhật imports/tests/report mapping | Codex |

## 21. Developer Plan cập nhật: Dùng Request DTO cho `POST /api/v1/vocabs`

### 21.1. Trạng thái

- Trạng thái: Đã triển khai.
- Agent tạo cập nhật plan: Codex.
- Lý do cập nhật: Người dùng yêu cầu API tạo vocab dùng Request DTO và không nhận `audioUrl` từ request.

### 21.2. Căn cứ tài liệu/source

- `.claude/docs/modules/Vocabulary_Module.md`
  - BM1: `POST /api/v1/vocabs` dùng JSON request body DTO với `word`, `meaning`, optional `ipa`.
  - Request tạo vocab không nhận `audioUrl`; audio URL phải do backend sinh từ `word` và `ipa`.

### 21.3. Mục tiêu

- Đổi API tạo vocab từ request params sang JSON body DTO.
- Tạo request DTO, ví dụ `ReqCreateVocabDTO`, gồm:
  - `word`
  - `meaning`
  - `ipa` optional
- Loại bỏ khả năng client gửi `audioUrl` khi tạo vocab.
- Audio luôn do backend sinh bằng `GoogleTtsService.synthesizeIpa(...)` sau khi có `ipa`.
- Response giữ nguyên shape hiện tại: `id`, `word`, `meaning`, `ipa`, `audio_url`.

### 21.4. Phạm vi thực hiện

- Tạo request DTO class theo convention Lombok.
- Cập nhật `VocabController.create()` dùng `@RequestBody ReqCreateVocabDTO`.
- Cập nhật `VocabService.create(...)` nhận DTO thay vì `Map<String, String>`.
- Bỏ constant/logic đọc `AUDIO_URL_PARAM` khỏi create flow.
- Giữ fallback: nếu request không gửi `ipa`, service gọi Free Dictionary; nếu Free Dictionary không trả IPA thì BM1 yêu cầu request có `ipa`.
- Cập nhật unit tests cho `VocabService`.
- Cập nhật Postman collection sang JSON body.
- Cập nhật HTML report mapping nếu request DTO cần vào coverage scope.

### 21.5. Phạm vi không thực hiện

- Không đổi endpoint path/method.
- Không đổi response DTO.
- Không đổi database schema.
- Không thêm list/detail/update/delete API.
- Không đổi provider Free Dictionary hoặc Google TTS behavior.

### 21.6. Output dự kiến

Request:

```http
POST /api/v1/vocabs
Content-Type: application/json
Authorization: Bearer <token>
```

```json
{
  "word": "hello",
  "meaning": "xin chao"
}
```

Request manual IPA fallback:

```json
{
  "word": "hello",
  "meaning": "xin chao",
  "ipa": "/həˈləʊ/"
}
```

Response:

```json
{
  "id": 1,
  "word": "hello",
  "meaning": "xin chao",
  "ipa": "/həˈləʊ/",
  "audio_url": "/api/v1/vocabs/audio/hello-<hash>.mp3"
}
```

### 21.7. Verification dự kiến

Sau khi được phê duyệt và triển khai, chạy:

```text
./gradlew test jacocoTestReport testHtmlReport checkstyleMain checkstyleTest pmdMain pmdTest
```

### 21.8. Lịch sử cập nhật

| Ngày | Nội dung | Người cập nhật |
|---|---|---|
| 2026-08-04 | Tạo plan chờ phê duyệt để dùng JSON request DTO cho `POST /api/v1/vocabs` và bỏ `audioUrl` request | Codex |
| 2026-08-04 | Triển khai `POST /api/v1/vocabs` dùng `ReqCreateVocabDTO`, bỏ `audioUrl` request và cập nhật tests/Postman | Codex |

## 22. Developer Plan cập nhật: Get/Update Vocab Lookup và Unique Word

### 22.1. Trạng thái

- Trạng thái: Đã triển khai.
- Agent tạo cập nhật plan: Codex.
- Lý do cập nhật: Người dùng yêu cầu implement get vocab by id, update vocab với input có thể là id hoặc word, và đảm bảo word unique khi thêm vocab.

### 22.2. Căn cứ tài liệu/source

- `.claude/docs/modules/Vocabulary_Module.md`
  - BM1: `word` phải unique trong bảng `vocabs`.
  - Cho phép lấy chi tiết vocab bằng `id` hoặc `word`.
  - Cho phép update vocab bằng `id` hoặc `word`; request update chỉ được sửa `meaning`.
- `.claude/docs/Data_Architecture.md`
  - Bảng `vocabs.word`: Not Null, Unique.

### 22.3. Mục tiêu

- Enforce unique `word` ở entity/database mapping và service create validation.
- Thêm API get vocab theo `id` hoặc `word`.
- Thêm API update vocab theo `id` hoặc `word`, chỉ cập nhật `meaning`.
- Giữ response shape `ResVocabDTO`: `id`, `word`, `meaning`, `ipa`, `audio_url`.

### 22.4. API contract dự kiến

Get vocab:

```http
GET /api/v1/vocabs/lookup?id=1
GET /api/v1/vocabs/lookup?word=hello
```

Update vocab meaning:

```http
PATCH /api/v1/vocabs/lookup?id=1
PATCH /api/v1/vocabs/lookup?word=hello
Content-Type: application/json
```

```json
{
  "meaning": "xin chao moi"
}
```

### 22.5. Phạm vi thực hiện

- Cập nhật `Vocab` entity:
  - `@Column(name = "word", nullable = false, unique = true)`.
- Cập nhật `VocabRepository`:
  - `Optional<Vocab> findByWord(String word)`.
  - `boolean existsByWord(String word)`.
- Tạo request DTO update, ví dụ `ReqUpdateVocabDTO`, chỉ gồm `meaning`.
- Cập nhật `VocabService`:
  - validate duplicate word khi create.
  - helper lookup bằng `id` hoặc `word`.
  - `get(Long id, String word)`.
  - `update(Long id, String word, ReqUpdateVocabDTO request)`.
- Cập nhật `VocabController`:
  - `GET /api/v1/vocabs/lookup`.
  - `PATCH /api/v1/vocabs/lookup`.
- Cập nhật unit tests cho `VocabService`.
- Cập nhật Postman collection.
- Cập nhật HTML report mapping nếu có request DTO mới trong coverage scope.

### 22.6. Phạm vi không thực hiện

- Không implement list paging.
- Không implement delete.
- Không update `word`, `ipa`, `audio_url`.
- Không regenerate IPA/audio khi update meaning.
- Không thêm migration tool mới trong scope này; dùng JPA mapping hiện tại của project.

### 22.7. Validation/error dự kiến

- Nếu cả `id` và `word` đều thiếu: throw `IdInvalidException`.
- Nếu cả `id` và `word` cùng được gửi: ưu tiên `id` để lookup, bỏ qua `word`.
- Nếu không tìm thấy vocab: throw `IdInvalidException`.
- Nếu tạo vocab với word đã tồn tại: throw `IdInvalidException`.
- Nếu update thiếu/blank `meaning`: throw `IdInvalidException`.

### 22.8. Verification dự kiến

Sau khi được phê duyệt và triển khai, chạy:

```text
./gradlew test jacocoTestReport testHtmlReport checkstyleMain checkstyleTest pmdMain pmdTest
```

### 22.9. Lịch sử cập nhật

| Ngày | Nội dung | Người cập nhật |
|---|---|---|
| 2026-08-04 | Tạo plan chờ phê duyệt cho get/update vocab theo `id` hoặc `word` và unique constraint cho `word` | Codex |
| 2026-08-04 | Triển khai get/update vocab theo `id` hoặc `word`, unique `word`, tests và Postman collection | Codex |

## 23. Developer Plan cập nhật: Bulk Import Vocab `.xlsx`

### 23.1. Trạng thái

- Trạng thái: Đã triển khai.
- Agent tạo cập nhật plan: Codex.
- Lý do cập nhật: Người dùng yêu cầu implement bulk add vocab feature theo template `src/main/resources/VocabImportTemplate.xlsx`.

### 23.2. Căn cứ tài liệu/source

- `.claude/docs/modules/Vocabulary_Module.md`
  - BM2: Bulk Import `.xlsx`.
  - Rule Critical: Partial Failure, một dòng lỗi thì bỏ qua dòng đó và tiếp tục dòng tiếp theo.
  - Template chính thức: `src/main/resources/VocabImportTemplate.xlsx`.
- Template `src/main/resources/VocabImportTemplate.xlsx`
  - Header row: row 2.
  - Data start row: row 3.
  - Cột `A`: `STT`.
  - Cột `B`: `Từ vựng (word)`.
  - Cột `C`: `Phiên âm (có thể bỏ trống)`.
  - Cột `D`: `Dịch nghĩa`.

### 23.3. Mục tiêu

- Thêm API bulk import vocab từ file `.xlsx`.
- Parse file theo template chính thức.
- Với mỗi dòng hợp lệ:
  - lấy `word`, `ipa` optional, `meaning`.
  - áp dụng cùng create flow hiện tại: unique `word`, resolve IPA nếu thiếu, sinh audio URL bằng Google TTS.
  - lưu vocab nếu thành công.
- Với mỗi dòng lỗi:
  - không rollback toàn bộ batch.
  - ghi nhận row number, word nếu có, message lỗi.
  - tiếp tục xử lý dòng tiếp theo.
- Response trả summary import.

### 23.4. API contract dự kiến

```http
POST /api/v1/vocabs/bulk
Content-Type: multipart/form-data
Authorization: Bearer <token>
```

Form-data:

```text
file=<VocabImportTemplate.xlsx>
```

Response:

```json
{
  "total_rows": 5,
  "success_count": 4,
  "failure_count": 1,
  "items": [
    {
      "row_number": 3,
      "word": "option",
      "success": true,
      "vocab": {
        "id": 1,
        "word": "option",
        "meaning": "lựa chọn",
        "ipa": "/...",
        "audio_url": "/api/v1/vocabs/audio/option-<hash>.mp3"
      },
      "error": null
    },
    {
      "row_number": 4,
      "word": "word",
      "success": false,
      "vocab": null,
      "error": "Từ vựng đã tồn tại"
    }
  ]
}
```

### 23.5. Phạm vi thực hiện

- Thêm dependency đọc `.xlsx`:
  - `org.apache.poi:poi-ooxml`.
- Tạo response DTO cho bulk import:
  - `ResVocabBulkImportDTO`.
  - `ResVocabBulkImportItemDTO`.
- Tạo parser/import service trong `service.vocab`, ví dụ:
  - `VocabBulkImportService`.
  - đọc sheet đầu tiên.
  - header row 2, data từ row 3.
  - bỏ qua dòng trống hoàn toàn.
  - map cột B/C/D sang create request.
- Cập nhật `VocabController`:
  - `POST /api/v1/vocabs/bulk` nhận `@RequestPart` hoặc `@RequestParam MultipartFile file`.
- Reuse `VocabService.create(ReqCreateVocabDTO)` để đảm bảo cùng rule single create.
- Cập nhật unit test:
  - parse/process một file `.xlsx` test hoặc template fixture.
  - partial failure khi có duplicate/invalid row.
  - không dùng application context/repository integration test.
- Cập nhật HTML report mapping cho DTO/service mới.
- Cập nhật Postman collection:
  - thêm request multipart upload file.

### 23.6. Phạm vi không thực hiện

- Không import quan hệ VocabSet/Organization.
- Không list/detail/delete mới ngoài API bulk.
- Không thay đổi template file trong scope này nếu không cần.
- Không rollback toàn batch khi một dòng lỗi.
- Không tạo migration tool mới.

### 23.7. Validation/error dự kiến

- Nếu file null/empty: trả lỗi bad request.
- Nếu file không phải `.xlsx`: trả lỗi bad request.
- Nếu sheet không có dữ liệu: summary `total_rows = 0`.
- Nếu dòng thiếu `word`: dòng đó failure.
- Nếu thiếu `ipa` và provider không resolve được IPA: dòng đó failure theo BM1.
- Nếu duplicate `word` trong DB hoặc trong chính file: dòng đó failure; dòng đầu tiên thành công nếu hợp lệ.
- Lỗi Google TTS/provider/audio của một dòng chỉ fail dòng đó.

### 23.8. Verification dự kiến

Sau khi được phê duyệt và triển khai, chạy:

```text
./gradlew test jacocoTestReport testHtmlReport checkstyleMain checkstyleTest pmdMain pmdTest
```

### 23.9. Lịch sử cập nhật

| Ngày | Nội dung | Người cập nhật |
|---|---|---|
| 2026-08-04 | Tạo plan chờ phê duyệt cho bulk import `.xlsx` theo `VocabImportTemplate.xlsx` với Partial Failure | Codex |
| 2026-08-04 | Triển khai API bulk import `.xlsx`, unit test Partial Failure và cập nhật Postman collection | Codex |
