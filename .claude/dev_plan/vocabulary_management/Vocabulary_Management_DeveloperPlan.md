# Developer Plan: Vocabulary Management

## 1. Trạng thái

- Trạng thái phê duyệt: Chờ người dùng phê duyệt.
- Trạng thái triển khai: Chưa triển khai.
- Ngày tạo plan: 2026-08-03.
- Agent tạo plan: Codex.
- Lý do tạo plan: Người dùng yêu cầu triển khai Vocabulary Management nhưng module này chưa có Developer Plan được phê duyệt.

## 2. Mục tiêu

Triển khai nền tảng quản lý từ vựng theo tài liệu source-of-truth, gồm entity `Vocab`, API thêm từ thủ công, cập nhật nghĩa của từ và chuẩn bị điểm mở rộng cho tự động xử lý IPA/audio.

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

### 4.1. Manual Vocabulary Management

Triển khai:

- Entity `Vocab` map table `vocabs`.
- Repository `VocabRepository`.
- Request DTO thêm từ thủ công.
- Request DTO cập nhật từ.
- Response DTO trả thông tin từ vựng.
- Service xử lý nghiệp vụ tạo/cập nhật/tìm từ vựng.
- Controller cho các API cơ bản.

API dự kiến:

```text
POST  /api/v1/vocabs
GET   /api/v1/vocabs/{id}
GET   /api/v1/vocabs
PATCH /api/v1/vocabs/{id}
```

### 4.2. Validation và business rules

- `word` bắt buộc khi tạo.
- Khi tạo, service cố gắng tự động bổ sung IPA nếu request không gửi IPA.
- Nếu sau bước tự động vẫn không có IPA thì request phải có đầy đủ `word`, `meaning`, `ipa`; nếu thiếu `meaning` hoặc `ipa` thì trả lỗi.
- `audio_url` có thể được request gửi lên hoặc được automation service tạo nếu có thể.
- Khi update, chỉ cho phép sửa `meaning`.
- Không cho update `word`, `ipa`, `audio_url` trong API update thuộc scope này.

### 4.3. Automation boundary

Do docs chỉ yêu cầu tự động IPA/audio nhưng chưa chỉ định provider hoặc external API:

- Tạo interface/service nội bộ để resolve IPA/audio.
- Implementation mặc định trong scope này không gọi network ngoài.
- Mặc định trả `Optional.empty()` cho IPA/audio nếu không có provider cấu hình.
- Thiết kế này giữ đúng rule: nếu hệ thống không tìm được IPA thì người dùng phải nhập đủ dữ liệu.

## 5. Phạm vi không thực hiện

Không thực hiện trong plan này:

- Bulk import `.xlsx`.
- Partial Failure implementation cho `.xlsx`.
- Entity `Item`, `Folder`, `VocabSet`.
- Junction table/entity `vocab_vocab_set`.
- API gắn `Vocab` vào `VocabSet`.
- Flashcard hoặc Testing/Learning.
- External dictionary/TTS provider thật.
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

Request:

```json
{
  "word": "hello",
  "meaning": "xin chào",
  "ipa": "həˈləʊ",
  "audio_url": "https://example.com/hello.mp3"
}
```

Behavior:

- Tạo từ vựng mới.
- Nếu thiếu `ipa`, service gọi automation boundary để thử tìm IPA.
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

### 8.2. `GET /api/v1/vocabs/{id}`

- Trả thông tin một từ theo `vocab_id`.
- Nếu không tồn tại, trả lỗi.

### 8.3. `GET /api/v1/vocabs`

- Trả danh sách từ vựng dạng paging.
- Dùng `Pageable` nếu phù hợp với Spring Data hiện tại.

### 8.4. `PATCH /api/v1/vocabs/{id}`

Request:

```json
{
  "meaning": "xin chào; lời chào"
}
```

Behavior:

- Chỉ cập nhật `meaning`.
- Không cập nhật `word`, `ipa`, `audio_url`.

## 9. Danh sách file dự kiến tạo/chỉnh sửa

| File | Loại thay đổi | Mục đích |
|---|---|---|
| `src/main/java/net/runsystem/duyptk/BaiTap2_HoiNhapKyThuat_AI/domain/table/Vocab.java` | Tạo mới | Entity table `vocabs` |
| `src/main/java/net/runsystem/duyptk/BaiTap2_HoiNhapKyThuat_AI/repository/VocabRepository.java` | Tạo mới | Repository cho `Vocab` |
| `src/main/java/net/runsystem/duyptk/BaiTap2_HoiNhapKyThuat_AI/domain/requestDTO/ReqCreateVocabDTO.java` | Tạo mới | Request DTO tạo từ thủ công |
| `src/main/java/net/runsystem/duyptk/BaiTap2_HoiNhapKyThuat_AI/domain/requestDTO/ReqUpdateVocabDTO.java` | Tạo mới | Request DTO update chỉ `meaning` |
| `src/main/java/net/runsystem/duyptk/BaiTap2_HoiNhapKyThuat_AI/domain/responseDTO/ResVocabDTO.java` | Tạo mới | Response DTO cho từ vựng |
| `src/main/java/net/runsystem/duyptk/BaiTap2_HoiNhapKyThuat_AI/service/VocabAutomationService.java` | Tạo mới | Boundary tự động resolve IPA/audio |
| `src/main/java/net/runsystem/duyptk/BaiTap2_HoiNhapKyThuat_AI/service/VocabService.java` | Tạo mới | Business logic Vocabulary |
| `src/main/java/net/runsystem/duyptk/BaiTap2_HoiNhapKyThuat_AI/controller/VocabController.java` | Tạo mới | API `/api/v1/vocabs` |
| `src/main/java/net/runsystem/duyptk/BaiTap2_HoiNhapKyThuat_AI/util/error/GlobalExceptionHandler.java` | Chỉnh sửa nếu cần | Bổ sung mapping lỗi business nếu exception hiện tại chưa đủ |
| `src/test/java/net/runsystem/duyptk/BaiTap2_HoiNhapKyThuat_AI/service/VocabServiceTests.java` | Tạo mới | Unit test service layer |
| `src/test/java/net/runsystem/duyptk/BaiTap2_HoiNhapKyThuat_AI/report/TestHtmlReportGenerator.java` | Chỉnh sửa nếu cần | Map test/coverage report cho module `vocabulary_management` |

## 10. Vị trí thay đổi dự kiến

| File | Class/Component | Method/Khu vực | Nội dung thay đổi |
|---|---|---|---|
| `Vocab.java` | `Vocab` | Fields/mapping | `vocab_id`, `word`, `meaning`, `ipa`, `audio_url` |
| `VocabRepository.java` | `VocabRepository` | Interface methods | `JpaRepository<Vocab, Long>` |
| `ReqCreateVocabDTO.java` | `ReqCreateVocabDTO` | Fields/validation | `word`, `meaning`, `ipa`, `audioUrl` |
| `ReqUpdateVocabDTO.java` | `ReqUpdateVocabDTO` | Fields/validation | Chỉ `meaning` |
| `ResVocabDTO.java` | `ResVocabDTO` | Fields/json mapping | Trả dữ liệu an toàn của `Vocab` |
| `VocabAutomationService.java` | `VocabAutomationService` | Resolve methods | `resolveIpa`, `resolveAudioUrl` không gọi external provider trong scope này |
| `VocabService.java` | `VocabService` | `create`, `findById`, `findAll`, `updateMeaning`, `convertToDTO` | Business rule manual import/update |
| `VocabController.java` | `VocabController` | API methods | `POST`, `GET by id`, `GET list`, `PATCH` |
| `GlobalExceptionHandler.java` | Exception handler | Handler methods | Dùng `IdInvalidException` nếu đủ; chỉ thêm exception mới nếu cần |
| `VocabServiceTests.java` | Unit tests | Test methods | Tạo vocab, resolve IPA/audio boundary, reject invalid manual import, update chỉ meaning |
| `TestHtmlReportGenerator.java` | Report generator | Module mapping/filter | Thêm module `vocabulary_management` vào HTML report nếu có test mới |

## 11. Unit test dự kiến

Unit test bằng JUnit cho `VocabService`:

- Create thành công khi request có đủ `word`, `meaning`, `ipa`.
- Create thành công khi request thiếu `ipa` nhưng automation resolve được IPA.
- Create thất bại khi thiếu `ipa`, automation không resolve được IPA và thiếu `meaning` hoặc `ipa`.
- Create tự động gán `audioUrl` khi automation resolve được audio và request chưa gửi `audioUrl`.
- Update chỉ thay đổi `meaning`.
- Find by id trả lỗi khi không tồn tại.

Không thêm application context test/repository test nếu người dùng không yêu cầu riêng.

## 12. Rủi ro và lưu ý

- Docs chưa chỉ định provider IPA/audio, nên implementation thật chỉ có boundary mặc định không gọi external API.
- Docs chưa chỉ định unique constraint cho `word`, nên plan này không thêm unique để tránh tự ý mở rộng requirement.
- Bulk `.xlsx` có Partial Failure là requirement quan trọng nhưng sẽ cần plan riêng để không trộn scope với manual Vocabulary Management.
- Organization/VocabSet chưa triển khai, nên chưa implement quan hệ `vocab_vocab_set`.

## 13. Verification dự kiến

Sau khi được phê duyệt và triển khai, chạy:

```text
./gradlew test jacocoTestReport testHtmlReport checkstyleMain checkstyleTest pmdMain pmdTest
```

## 14. Output dự kiến

- API manual vocabulary hoạt động cho người dùng đã đăng nhập.
- `Vocab` được lưu đúng schema docs.
- Update chỉ cho sửa `meaning`.
- Automation IPA/audio có boundary rõ ràng và không phụ thuộc network trong scope này.
- Unit test `VocabServiceTests` pass.
- HTML test report hiển thị thêm module `vocabulary_management` nếu có test tương ứng.
