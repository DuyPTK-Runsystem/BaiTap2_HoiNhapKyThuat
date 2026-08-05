# Developer Plan Summary

## 1. Mục đích

File này là mục lục và bản tóm tắt của toàn bộ Developer Plan trong thư mục `.claude/dev_plan`.

Mỗi khi thêm Developer Plan mới, cần cập nhật file này với:

- Tên và đường dẫn file.
- Module/feature.
- Mục tiêu chính.
- Phạm vi bao gồm và loại trừ.
- API/database bị ảnh hưởng nếu có.
- Trạng thái phê duyệt.
- Trạng thái triển khai.

## 2. Quy tắc quản lý Developer Plan

- Developer Plan phải được tạo trước khi chỉnh sửa mã nguồn cho feature tương ứng.
- Không được triển khai code khi Developer Plan chưa được người dùng phê duyệt rõ ràng.
- Developer Plan phải phù hợp với requirement, kiến trúc và workflow của project.
- Nếu phát hiện phạm vi hoặc phương án không còn phù hợp, phải cập nhật plan và xin phê duyệt lại.
- Không tự ý thêm feature, dependency, refactor diện rộng hoặc thay đổi kiến trúc ngoài plan đã duyệt.
- Sau khi triển khai, cần đối chiếu code thực tế với Developer Plan và cập nhật trạng thái.

## 3. Danh sách Developer Plan

| Developer Plan                                                                           | Module/Feature                                | Trạng thái phê duyệt | Trạng thái triển khai | Tóm tắt                                                                                                           |
| ---------------------------------------------------------------------------------------- | --------------------------------------------- | -------------------- | --------------------- | ----------------------------------------------------------------------------------------------------------------- |
| [`auth_authz/Auth_Authz_DeveloperPlan.md`](auth_authz/Auth_Authz_DeveloperPlan.md)       | Authentication và authorization tối giản      | Đã phê duyệt         | Đã triển khai         | Cấu hình Spring Boot, User, JWT, BCrypt, register/login/refresh/account/logout; không có Roles và Permissions     |
| [`auth_authz/Auth_UnitTest_DeveloperPlan.md`](auth_authz/Auth_UnitTest_DeveloperPlan.md) | Unit Test cho Authentication và Authorization | Đã phê duyệt         | Đã triển khai         | JUnit test cho `UserService` và HTML report có test case, module và coverage; không test app/repo/`SecurityUtil` |
| [`vocabulary_management/Vocabulary_Management_DeveloperPlan.md`](vocabulary_management/Vocabulary_Management_DeveloperPlan.md) | Vocabulary Management | Đã phê duyệt `vocabSetId` query param | Đã triển khai `vocabSetId` query param | `POST /api/v1/vocabs?vocabSetId=...` và `POST /api/v1/vocabs/bulk?vocabSetId=...` tạo từ mới rồi gắn vào vocab set |
| [`organization/Organization_DeveloperPlan.md`](organization/Organization_DeveloperPlan.md) | Organization | Đã phê duyệt unique sibling name | Đã triển khai unique sibling name | Service-level validation để tên folder/vocab set unique trong cùng parent của cùng user |

## 4. Tóm tắt từng Developer Plan

### 4.1. Authentication và Authorization tối giản

**File:** [`auth_authz/Auth_Authz_DeveloperPlan.md`](auth_authz/Auth_Authz_DeveloperPlan.md)

**Mục tiêu:**

- Cấu hình project theo boilerplate.
- Xây dựng nền tảng Authentication bằng JWT và BCrypt.
- Bảo vệ endpoint bằng trạng thái authenticated.

**Bao gồm:**

- Build/dependency configuration.
- User entity và UserRepository.
- UserService.
- Request/response DTO.
- AuthController.
- Register, login, account, refresh và logout.
- JWT, BCrypt, CORS, OpenAPI và exception handling.
- Unit/integration test cho Auth và Security.

**Không bao gồm:**

- Role.
- Permission.
- Role/Permission repository, service, DTO hoặc controller.
- Quan hệ User–Role hoặc Role–Permission.
- Role/Permission claims trong JWT.
- Vocabulary, Organization và Testing implementation trong phase này.

**Các quyết định kỹ thuật dự kiến:**

- Giữ Java 21 và Spring Boot 4.0.7.
- Không bắt buộc sử dụng UUID; mặc định dự kiến dùng `Long` hoặc kiểu ID quan hệ phù hợp với schema.
- Map database column thành `user_id` và `hash_password`.
- Email unique.
- Password luôn được hash trước khi lưu.
- Các endpoint ngoài nhóm public yêu cầu JWT hợp lệ.
- CORS mặc định dự kiến là `http://localhost:5173`, chờ xác nhận.
- Secret database/JWT lấy từ environment variables.

**API dự kiến:**

```text
POST /api/v1/auth/register
POST /api/v1/auth/login
GET  /api/v1/auth/account
GET  /api/v1/auth/refresh
POST /api/v1/auth/logout
```

**Rủi ro chính:**

- Khác biệt schema User giữa boilerplate và tài liệu kiến trúc.
- Dependency JWT có thể chưa đầy đủ.
- Cấu hình CORS và secret cần xác nhận.
- Boilerplate có cấu hình `.anyRequest().permitAll()` cần thay bằng `.anyRequest().authenticated()`.
- Phải loại bỏ toàn bộ tham chiếu Roles/Permissions.

### 4.2. Vocabulary Management

**File:** [`vocabulary_management/Vocabulary_Management_DeveloperPlan.md`](vocabulary_management/Vocabulary_Management_DeveloperPlan.md)

**Mục tiêu:**

- Triển khai quản lý từ vựng thủ công theo docs.
- Tạo entity `Vocab` map bảng `vocabs`.
- Tạo quick API thêm từ `POST /api/v1/vocabs` bằng request params.
- Chuẩn bị boundary cho automation IPA/audio với dumb provider trước khi tích hợp `hcoles/voices`.

**Bao gồm:**

- `Vocab` entity.
- `VocabRepository`.
- Response DTO.
- `VocabService`.
- `VocabAutomationService`.
- `FreeDictionaryVocabAutomationService`.
- `VocabController`.
- Unit test service layer.
- Cập nhật HTML test report module mapping nếu cần.

**Không bao gồm:**

- Bulk import `.xlsx`.
- Partial Failure implementation cho `.xlsx`.
- Organization `Item`/`Folder`/`VocabSet`.
- Quan hệ `vocab_vocab_set`.
- API lấy danh sách/lấy chi tiết/cập nhật nghĩa trong phase đầu.
- Flashcard hoặc Testing/Learning.
- `hcoles/voices` và Oxford không còn là provider chính; Free Dictionary là provider IPA hiện tại.

**Rủi ro chính:**

- Provider chính chuyển sang Free Dictionary API, không cần credentials.
- Response cần trả `ipa`; không dùng `phoneme` trong Vocabulary process; khi cần audio thì gọi `GoogleTtsService.synthesizeIpa(...)` và trả `audio_url` tới MP3 thật.
- Docs chưa yêu cầu unique `word`, nên không tự thêm unique.
- Bulk `.xlsx` và quan hệ VocabSet phụ thuộc plan/module khác.

### 4.3. Organization

**File:** [`organization/Organization_DeveloperPlan.md`](organization/Organization_DeveloperPlan.md)

**Mục tiêu:**

- Triển khai nền tảng Organization theo mô hình `Item` base và `Folder`/`VocabSet` kế thừa.
- Hỗ trợ cấu trúc cây qua `parent_id`.
- Hỗ trợ quan hệ n-n giữa `VocabSet` và `Vocab`.

**Bao gồm:**

- Entity `Item`, `Folder`, `VocabSet`.
- Enum `ItemType`.
- Repository cho item/folder/vocab set.
- API phase đầu để tạo folder, tạo vocab set, lấy children, gắn/gỡ vocab vào vocab set.
- API search item theo tên LIKE và get item by path, chỉ trả item user hiện tại truy cập được.
- Ownership theo authenticated user hiện tại.
- Unit test service layer cho Organization.

**Không bao gồm:**

- Move/rename/delete item.
- Recursive tree API hoặc recursive CTE lấy vocab từ folder.
- Bulk import trực tiếp vào vocab set.
- Tạo/lưu virtual super root trong database.
- Thêm uniqueness constraint cho tên item trong cùng parent nếu chưa có phê duyệt riêng.
- Flashcard, Multiple Choice hoặc Testing/Learning.
- Role, Permission hoặc JWT claims quyền.

**Rủi ro chính:**

- Docs Organization chưa đặc tả endpoint chính thức, nên API trong plan là đề xuất phase đầu cần người dùng phê duyệt.
- Mapping discriminator `type` trong Class Table Inheritance cần triển khai cẩn thận để không trùng column.
- Recursive CTE chưa nằm trong scope phase đầu nhưng có thể cần cho Testing/Learning sau này.
- Path theo tên có thể mơ hồ khi sibling trùng tên; plan đề xuất trả lỗi conflict/validation thay vì chọn ngầm.

## 5. Developer Plan trong tương lai

Chưa có Developer Plan cho các module sau:

- Testing & Learning.
- Flashcard.
- Import `.xlsx`.

Khi tạo plan mới, cần thêm vào bảng ở mục 3 và phần tóm tắt tương ứng ở mục 4.

## 6. Lịch sử cập nhật

| Ngày       | Nội dung                                                                                                    | Người cập nhật      |
| ---------- | ----------------------------------------------------------------------------------------------------------- | ------------------- |
| 2026-08-03 | Tạo Developer Plan Summary và plan Auth/Authz ban đầu                                                       | RunSystem Assistant |
| 2026-08-03 | Cập nhật plan: project không bắt buộc sử dụng UUID                                                          | RunSystem Assistant |
| 2026-08-03 | Người dùng phê duyệt plan Auth/Authz; cho phép bắt đầu triển khai                                           | Người dùng          |
| 2026-08-03 | Cập nhật trạng thái Auth/Authz sang đã triển khai                                                           | Codex               |
| 2026-08-03 | Bổ sung audit fields cho entity Auth/User theo yêu cầu người dùng                                           | Codex               |
| 2026-08-03 | Tạo Developer Plan cho Unit Test Auth/Authz để ghi nhận trạng thái tạm dừng do chưa có phê duyệt triển khai | Codex               |
| 2026-08-03 | Di chuyển Developer Plan Unit Test vào thư mục `auth_authz` theo yêu cầu người dùng                         | Codex               |
| 2026-08-03 | Cập nhật và triển khai Unit Test Auth/Authz bằng JUnit, bổ sung PDF report generator                        | Codex               |
| 2026-08-03 | Điều chỉnh Unit Test Auth/Authz: thay PDF report bằng HTML report có danh sách test, module và coverage     | Codex               |
| 2026-08-03 | Điều chỉnh HTML Unit Test report: coverage chỉ filter theo module cần test                                  | Codex               |
| 2026-08-03 | Cập nhật plan Auth/Authz cho refactor Lombok theo convention boilerplate                                    | Codex               |
| 2026-08-03 | Tạo Developer Plan Vocabulary Management theo yêu cầu triển khai module                                     | Codex               |
| 2026-08-04 | Cập nhật docs/plan Vocabulary: dùng hướng provider `hcoles/voices`, triển khai dumb provider/API trước      | Codex               |
| 2026-08-04 | Thu hẹp quick dumb test Vocabulary còn `POST /api/v1/vocabs` với request params                            | Codex               |
| 2026-08-04 | Người dùng phê duyệt quick dumb test Vocabulary và cho phép bắt đầu triển khai                              | Người dùng          |
| 2026-08-04 | Triển khai quick dumb test Vocabulary với `POST /api/v1/vocabs`, dumb IPA/audio provider và unit test       | Codex               |
| 2026-08-04 | Thêm plan chờ phê duyệt cho real `hcoles/voices`: audio file thật, audio URL thật và phoneme trong response | Codex               |
| 2026-08-04 | Người dùng phê duyệt tiếp tục test real audio file và phoneme provider sử dụng cho Vocabulary               | Người dùng          |
| 2026-08-04 | Triển khai real `hcoles/voices` provider cho Vocabulary, trả phoneme và audio URL tới WAV thật              | Codex               |
| 2026-08-04 | Người dùng yêu cầu chuyển pipeline Vocabulary sang Oxford Dictionaries API                                  | Người dùng          |
| 2026-08-04 | Cập nhật docs/plan Vocabulary cho Oxford provider, dùng env vars thay vì hard-code credentials              | Codex               |
| 2026-08-04 | Triển khai Oxford API provider cho Vocabulary và cập nhật Postman collection                                | Codex               |
| 2026-08-04 | Người dùng yêu cầu chuyển provider Vocabulary sang Free Dictionary API                                      | Người dùng          |
| 2026-08-04 | Cập nhật docs/plan Vocabulary cho Free Dictionary provider, audio để phase IPA -> voice sau                 | Codex               |
| 2026-08-04 | Triển khai Free Dictionary API provider cho Vocabulary và cập nhật Postman collection                       | Codex               |
| 2026-08-04 | Dọn implementation dummy provider dư sau khi Free Dictionary là provider hiện tại                           | Codex               |
| 2026-08-04 | Cập nhật docs/plan chờ phê duyệt để loại bỏ `phoneme` khỏi toàn bộ Vocabulary process                       | Codex               |
| 2026-08-04 | Cập nhật docs/plan chờ phê duyệt để sinh audio bằng `GoogleTtsService.synthesizeIpa(...)`                   | Codex               |
| 2026-08-04 | Triển khai Vocabulary POST: bỏ `phoneme`, sinh audio qua `GoogleTtsService.synthesizeIpa(...)`              | Codex               |
| 2026-08-04 | Tạo plan chờ phê duyệt để refactor service package thành `auth`, `vocab`, `tts`                             | Codex               |
| 2026-08-04 | Triển khai refactor service package thành `auth`, `vocab`, `tts`                                            | Codex               |
| 2026-08-04 | Tạo plan chờ phê duyệt để đổi `POST /api/v1/vocabs` sang JSON request DTO và bỏ `audioUrl` request          | Codex               |
| 2026-08-04 | Triển khai đổi `POST /api/v1/vocabs` sang JSON request DTO và bỏ `audioUrl` request                         | Codex               |
| 2026-08-04 | Tạo plan chờ phê duyệt cho get/update vocab theo `id` hoặc `word` và unique constraint cho `word`           | Codex               |
| 2026-08-04 | Triển khai get/update vocab theo `id` hoặc `word` và unique constraint cho `word`                           | Codex               |
| 2026-08-04 | Tạo plan chờ phê duyệt cho bulk import `.xlsx` theo `VocabImportTemplate.xlsx`                              | Codex               |
| 2026-08-04 | Triển khai bulk import `.xlsx` theo `VocabImportTemplate.xlsx` với Partial Failure từng dòng                 | Codex               |
| 2026-08-04 | Tạo Developer Plan Organization chờ phê duyệt                                                               | Codex               |
| 2026-08-04 | Người dùng phê duyệt phase đầu Organization: `POST folder` và `POST vocab set`                              | Người dùng          |
| 2026-08-04 | Triển khai phase đầu Organization: `POST /api/v1/folders` và `POST /api/v1/vocab-sets`                      | Codex               |
| 2026-08-05 | Người dùng phê duyệt `GET /api/v1/items/children` cho root items và direct children theo `parentId`         | Người dùng          |
| 2026-08-05 | Triển khai `GET /api/v1/items/children` cho root items và direct children theo `parentId`                   | Codex               |
| 2026-08-05 | Cập nhật docs/plan chờ phê duyệt cho add vocab response giàu thông tin và bulk add vocab vào vocab set      | Codex               |
| 2026-08-05 | Người dùng phê duyệt triển khai add/bulk add vocab vào vocab set                                           | Người dùng          |
| 2026-08-05 | Triển khai add/bulk add vocab vào vocab set với response giàu thông tin và Partial Failure                  | Codex               |
| 2026-08-05 | Cập nhật docs/plan Vocabulary chờ phê duyệt cho `vocabSetId` query param khi tạo vocab hoặc bulk import     | Codex               |
| 2026-08-05 | Người dùng phê duyệt triển khai `vocabSetId` query param khi tạo vocab hoặc bulk import                    | Người dùng          |
| 2026-08-05 | Triển khai `vocabSetId` query param khi tạo vocab hoặc bulk import                                         | Codex               |
| 2026-08-05 | Cập nhật docs/plan Organization chờ phê duyệt cho search item theo tên LIKE, `itemPath`, và get item by path | Codex               |
| 2026-08-05 | Người dùng phê duyệt và triển khai search item theo tên LIKE, `itemPath`, và get item by path              | Codex               |
| 2026-08-05 | Cập nhật docs/plan Organization chờ phê duyệt cho unique tên item trong cùng parent                         | Codex               |
| 2026-08-05 | Người dùng phê duyệt và triển khai unique tên item trong cùng parent bằng service-level validation          | Codex               |
