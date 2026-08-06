# Vocabulary Management Module Specification

## 1. Entities
- **Vocab**: `vocab_id`, `word`, `meaning`, `ipa`, `audio_url`, `mastered`.
- `mastered` là Boolean, mặc định `false` khi tạo vocab mới.

## 2. Functional Requirements
### BM1: Manual Import
- Cho phép thêm từ lẻ.
- **Rule**: Nếu không tìm thấy IPA, bắt buộc phải có đầy đủ `Word`, `Meaning`, `IPA` mới được lưu.
- **Rule Update**: Khi update, chỉ cho phép sửa `Meaning`.
- `word` phải unique trong bảng `vocabs`; không cho tạo trùng từ vựng đã có.
- `POST /api/v1/vocabs` dùng JSON request body DTO với `word`, `meaning`, optional `ipa`.
- `POST /api/v1/vocabs` có thể nhận optional query param `vocabSetId` để tạo từ mới và gắn ngay vào một `VocabSet`.
- Khi `vocabSetId` được truyền, `vocabSetId` phải thuộc authenticated user hiện tại.
- Khi tạo từ mới kèm `vocabSetId`, response phải trả thông tin vocab vừa tạo, thông tin vocab set và cờ `added`.
- Request tạo vocab không nhận `audioUrl`; audio URL phải do backend sinh từ `word` và `ipa`.
- Cho phép lấy chi tiết vocab bằng `id` hoặc `word`.
- Cho phép update vocab bằng `id` hoặc `word`; request update chỉ được sửa `meaning`.

### BM2: Bulk Import (.xlsx)
- Nhập hàng loạt từ file.
- **Rule (Critical)**: Áp dụng cơ chế **Partial Failure**. Nếu 1 dòng lỗi, bỏ qua dòng đó và tiếp tục dòng tiếp theo.
- Template import chính thức: `src/main/resources/VocabImportTemplate.xlsx`.
- Sheet đầu tiên dùng header ở row 2, data bắt đầu từ row 3.
- Cột template:
  - `A`: `STT`, chỉ dùng để hiển thị thứ tự, không lưu DB.
  - `B`: `Từ vựng (word)`, bắt buộc.
  - `C`: `Phiên âm (có thể bỏ trống)`, optional `ipa`.
  - `D`: `Dịch nghĩa`, bắt buộc theo rule BM1 khi provider không resolve được IPA.
- Bulk import phải áp dụng cùng rule tạo vocab lẻ: `word` unique, tự resolve IPA nếu thiếu, tự sinh `audio_url` bằng Google TTS sau khi có IPA.
- `POST /api/v1/vocabs/bulk` có thể nhận optional query param `vocabSetId` để gắn từng từ import thành công vào `VocabSet`.
- Khi bulk import có `vocabSetId`, chỉ các dòng tạo vocab thành công mới được gắn vào vocab set; dòng lỗi vẫn theo Partial Failure của bulk import.
- Bulk import response phải thể hiện tổng số dòng xử lý, số dòng thành công, số dòng thất bại và lỗi theo từng dòng.

## 3. Automation
- Tự động chuyển hóa từ vựng sang phiên âm IPA.
- Tự động xử lý Audio URL.

### 3.1. IPA/Audio Provider Direction
- Provider mục tiêu hiện tại cho IPA là Free Dictionary API.
- Base API: `https://freedictionaryapi.com/api/v1/entries/{language}/{word}`.
- Language mặc định: `en`.
- Query param: không có.
- Pipeline IPA mục tiêu: `English word -> Free Dictionary entries -> pronunciations[type=ipa].text`.
- Khi `pronunciations` có nhiều item `type = "ipa"`, chọn item đầu tiên.
- Pipeline audio mục tiêu: `word + ipa -> GoogleTtsService.synthesizeIpa(word, ipa, languageCode) -> MP3 file -> audio_url`.
- Audio file phải được tạo bằng `GoogleTtsService.synthesizeIpa(...)` khi cần sinh audio từ IPA.
- Hướng `hcoles/voices` và Oxford Dictionaries API đã được thử nhưng không còn là provider chính cho pipeline hiện tại.
- Không giữ implementation dummy provider trong code chạy sau khi Free Dictionary đã là provider hiện tại.

### 3.2. Real Provider Output Requirements
- Không dùng `phoneme` trong Vocabulary process, response DTO, provider result, Postman hoặc report/test scope.
- Khi tích hợp provider IPA thật, response của API tạo từ vựng chỉ cần trả `ipa` là IPA text provider chọn.
- Nếu Free Dictionary không trả IPA, API phải áp dụng rule BM1: nếu không tìm thấy IPA thì request phải có đủ `Word`, `Meaning`, `IPA`.
- Không lưu `audio_url` giả; nếu sinh audio thành công thì `audio_url` trỏ tới file MP3 thật được expose qua HTTP.

## 4. Lịch sử cập nhật
| Ngày | Nội dung | Người cập nhật |
|---|---|---|
| 2026-08-04 | Bổ sung hướng provider `hcoles/voices` và yêu cầu kiểm thử pipeline bằng dumb API trước khi tích hợp thật | Codex |
| 2026-08-04 | Bổ sung yêu cầu audio URL phải trỏ tới file thật và response phải hiển thị phoneme provider sử dụng | Codex |
| 2026-08-04 | Chuyển provider mục tiêu sang Oxford Dictionaries API; credentials dùng biến môi trường, không hard-code | Codex |
| 2026-08-04 | Chuyển provider IPA sang Free Dictionary API; chọn `pronunciations[type=ipa].text` đầu tiên, audio để phase sau | Codex |
| 2026-08-04 | Dọn implementation dummy provider khỏi code chạy sau khi chuyển sang Free Dictionary provider | Codex |
| 2026-08-04 | Loại bỏ yêu cầu `phoneme` khỏi Vocabulary process; API tạo vocab chỉ cần trả `ipa` | Codex |
| 2026-08-04 | Bổ sung hướng sinh audio bằng `GoogleTtsService.synthesizeIpa(...)` từ `word` và `ipa` | Codex |
| 2026-08-04 | Triển khai API tạo vocab theo hướng Free Dictionary IPA và Google TTS audio, không trả field `phoneme` | Codex |
| 2026-08-04 | Cập nhật contract tạo vocab: dùng JSON request DTO và không nhận `audioUrl` từ client | Codex |
| 2026-08-04 | Bổ sung yêu cầu `word` unique, get/update vocab theo `id` hoặc `word`, update chỉ sửa `meaning` | Codex |
| 2026-08-04 | Bổ sung yêu cầu bulk import `.xlsx` theo `VocabImportTemplate.xlsx` và Partial Failure theo từng dòng | Codex |
| 2026-08-05 | Bổ sung yêu cầu `vocabSetId` query param khi tạo vocab hoặc bulk import để gắn từ mới vào vocab set | Codex |
| 2026-08-06 | Bổ sung thuộc tính `mastered` kiểu Boolean cho từng vocab, mặc định `false` | RunSystem Assistant |
