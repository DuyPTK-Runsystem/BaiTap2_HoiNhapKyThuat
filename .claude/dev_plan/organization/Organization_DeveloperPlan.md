# Developer Plan: Organization Module

## 1. Trạng thái

- Trạng thái phê duyệt: Đã phê duyệt một phần cho phase đầu (`POST /api/v1/folders`, `POST /api/v1/vocab-sets`).
- Trạng thái triển khai: Đã triển khai phase đầu (`POST /api/v1/folders`, `POST /api/v1/vocab-sets`).
- Ngày tạo plan: 2026-08-04.
- Agent tạo plan: Codex.
- Ngày cập nhật gần nhất: 2026-08-04.
- Agent cập nhật gần nhất: Codex.
- Lý do tạo/cập nhật plan: Đã triển khai Organization phase đầu gồm `POST folder` và `POST vocab set`.

## 2. Mục tiêu

Triển khai nền tảng Organization để quản lý cấu trúc cây học liệu theo docs:

- `Item` là entity gốc cho cây.
- `Folder` và `VocabSet` kế thừa từ `Item`.
- `Folder` chứa `Folder` hoặc `VocabSet` qua `parent_id`.
- `VocabSet` liên kết nhiều-nhiều với `Vocab` qua bảng `vocab_vocab_set`.
- Mọi thao tác protected endpoint yêu cầu authenticated user, không dùng Role/Permission.

## 3. Tài liệu đối chiếu

- `.claude/docs/modules/Organization_Module.md`
  - Mục 1: Entity inheritance model gồm `Item`, `Folder`, `VocabSet`.
  - Mục 2: `Folder -> VocabSet` là quan hệ 1-n; `VocabSet <-> Vocab` là quan hệ n-n qua bảng trung gian.
  - Mục 3: Mỗi `VocabSet` phải có chủ sở hữu `user_id`; hỗ trợ cấu trúc cây vô hạn bằng `parent_id`.
- `.claude/docs/Data_Architecture.md`
  - Mục 2.2: Bảng `items`, `folders`, `vocab_sets`; `type` gồm `FOLDER`, `VOCAB_SET`; `user_id` là FK đến `users.user_id`; `parent_id` là FK đến `items.item_id`.
  - Mục 2.3: Bảng `vocab_vocab_set` liên kết `vocabs` và `vocab_sets`.
  - Mục 3.1: Khi cần lấy vocab từ một `item_id`, backend dùng recursive CTE để tìm item con, lọc `VOCAB_SET`, join `vocab_vocab_set`.
- `.claude/docs/ApplicationContext.md`
  - Mục 2: `Folder` và `VocabSet` là subclass của `Item`; `Folder` chứa `Folder` hoặc `VocabSet`; một `VocabSet` chỉ nằm trong một `Folder`; `Vocab <-> VocabSet` là n-n.
  - Mục 4: Organization Module là module riêng.
- `.claude/rules/CLAUDE.md`
  - Bắt buộc có Developer Plan được phê duyệt trước khi code.
- `.claude/workflows/WORKFLOW.md`
  - Bước 1, 2, 3 quy định đọc docs, kiểm tra plan và báo cáo trước khi code.

## 4. Phạm vi thực hiện

### 4.0. Scope đã được phê duyệt triển khai trước

Triển khai trước:

- `POST /api/v1/folders`.
- `POST /api/v1/vocab-sets`.
- Entity/repository/DTO/service nền bắt buộc để hai API trên hoạt động đúng.

Chưa triển khai trong phase đầu:

- `GET /api/v1/items/children`.
- `POST /api/v1/vocab-sets/{vocabSetId}/vocabs/{vocabId}`.
- `DELETE /api/v1/vocab-sets/{vocabSetId}/vocabs/{vocabId}`.

### 4.1. Entity và repository

Triển khai các entity theo Class Table Inheritance:

- `Item` map bảng `items`.
- `Folder` map bảng `folders`.
- `VocabSet` map bảng `vocab_sets`.
- Enum `ItemType` gồm `FOLDER`, `VOCAB_SET`.
- Quan hệ owner từ `Item` đến `User`.
- Quan hệ parent từ `Item` đến `Item`.
- Quan hệ n-n từ `VocabSet` đến `Vocab` qua `vocab_vocab_set`.

Repository dự kiến:

- `ItemRepository`.
- `FolderRepository`.
- `VocabSetRepository`.

### 4.2. API Organization tối thiểu

Docs chưa đặc tả endpoint cụ thể. Đề xuất API phase đầu để vận hành đúng business rule Organization:

```text
POST /api/v1/folders
POST /api/v1/vocab-sets
GET  /api/v1/items/children
POST /api/v1/vocab-sets/{vocabSetId}/vocabs/{vocabId}
DELETE /api/v1/vocab-sets/{vocabSetId}/vocabs/{vocabId}
```

Hành vi:

- Tạo root folder khi `parentId` null.
- Tạo folder con khi `parentId` là folder thuộc authenticated user.
- Tạo vocab set khi `parentId` là folder thuộc authenticated user hoặc null nếu người dùng muốn vocab set ở root.
- Lấy danh sách item con theo `parentId`; `parentId` null nghĩa là lấy root items của user hiện tại.
- Gắn một vocab đã tồn tại vào vocab set thuộc user hiện tại.
- Gỡ một vocab khỏi vocab set thuộc user hiện tại.

### 4.3. Ownership và authorization

- Dùng authenticated-only authorization theo cấu hình security hiện tại.
- Không thêm Role, Permission hoặc claim Role/Permission vào JWT.
- Service lấy current user từ `SecurityUtil.getCurrentUserLogin()` và `UserRepository.findByEmail(...)`.
- Mọi thao tác trên item/vocab set phải kiểm tra item thuộc user hiện tại.
- Nếu item/vocab set không thuộc user hiện tại, trả lỗi như resource không tồn tại hoặc không có quyền truy cập trong phạm vi service.

### 4.4. Validation và business rules

- `folderName` bắt buộc khi tạo folder.
- `vocabSetName` bắt buộc khi tạo vocab set.
- `parentId`, nếu có, phải trỏ đến `Folder` thuộc user hiện tại.
- Không cho parent là `VocabSet`, vì `VocabSet` không chứa item con.
- Không tạo cycle vì API create chỉ gán parent khi tạo mới; plan này chưa có move item.
- `VocabSet` luôn có owner qua `Item.user`.
- `VocabSet` chỉ thuộc một folder cha qua `Item.parent`.
- `Vocab` được gắn vào `VocabSet` theo quan hệ n-n; không tạo mới `Vocab` trong Organization module.

## 5. Phạm vi không thực hiện

Không thực hiện trong plan này:

- Move item giữa các folder.
- Đổi tên folder hoặc vocab set.
- Xóa folder hoặc vocab set.
- Lấy toàn bộ tree bằng recursive API.
- Recursive CTE để lấy vocab từ folder; phần này sẽ cần cho Testing/Learning hoặc API browse sâu hơn.
- Bulk import vocab trực tiếp vào vocab set.
- Flashcard, Multiple Choice, Test generation.
- Role, Permission, Role/Permission claim.
- Thay đổi public endpoint list.
- Thay đổi dependency/build nếu không bắt buộc.
- Thay đổi `.claude/docs` requirement nếu người dùng chưa yêu cầu.

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

Trạng thái hiện tại:

- Auth/Authz đã triển khai JWT stateless.
- Public endpoint chỉ nằm trong auth/register/login/refresh và docs/actuator.
- `User` entity chỉ gồm `user_id`, `email`, `hash_password`, `refresh_token`, audit fields.
- `Vocab` entity đã triển khai bảng `vocabs`, có `word` unique, `meaning`, `ipa`, `audio_url`.
- Chưa có entity/repository/controller/service cho Organization.
- Lombok đang được dùng cho entity/DTO và `@RequiredArgsConstructor` cho constructor injection.

## 7. Thiết kế database/entity

### 7.1. `Item`

Mapping dự kiến:

- `@Entity`
- `@Table(name = "items")`
- `@Inheritance(strategy = InheritanceType.JOINED)`
- `@DiscriminatorColumn(name = "type")`

Field:

- `Long id` map `item_id`.
- `ItemType type` map `type`.
- `User user` map `user_id`.
- `Item parent` map `parent_id`, nullable.

### 7.2. `Folder`

Mapping dự kiến:

- `@Entity`
- `@Table(name = "folders")`
- `@PrimaryKeyJoinColumn(name = "folder_id")`
- `@DiscriminatorValue("FOLDER")`

Field:

- `String folderName` map `folder_name`, not null.

### 7.3. `VocabSet`

Mapping dự kiến:

- `@Entity`
- `@Table(name = "vocab_sets")`
- `@PrimaryKeyJoinColumn(name = "vocab_set_id")`
- `@DiscriminatorValue("VOCAB_SET")`

Field:

- `String vocabSetName` map `vocab_set_name`, not null.
- `String vocabSetDescription` map `vocab_set_descp`, nullable.
- `Set<Vocab> vocabs` map join table `vocab_vocab_set`.

### 7.4. `ItemType`

Enum:

```text
FOLDER
VOCAB_SET
```

## 8. Thiết kế API dự kiến

### 8.1. `POST /api/v1/folders`

Request:

```json
{
  "folderName": "Unit 1",
  "parentId": 10
}
```

Response:

```json
{
  "id": 11,
  "type": "FOLDER",
  "name": "Unit 1",
  "parentId": 10
}
```

### 8.2. `POST /api/v1/vocab-sets`

Request:

```json
{
  "vocabSetName": "Common verbs",
  "vocabSetDescription": "Basic daily verbs",
  "parentId": 11
}
```

Response:

```json
{
  "id": 12,
  "type": "VOCAB_SET",
  "name": "Common verbs",
  "description": "Basic daily verbs",
  "parentId": 11,
  "vocabCount": 0
}
```

### 8.3. `GET /api/v1/items/children`

Request:

```text
GET /api/v1/items/children
GET /api/v1/items/children?parentId=11
```

Response:

```json
[
  {
    "id": 11,
    "type": "FOLDER",
    "name": "Unit 1",
    "parentId": null
  },
  {
    "id": 12,
    "type": "VOCAB_SET",
    "name": "Common verbs",
    "description": "Basic daily verbs",
    "parentId": 11,
    "vocabCount": 3
  }
]
```

### 8.4. `POST /api/v1/vocab-sets/{vocabSetId}/vocabs/{vocabId}`

Behavior:

- Gắn vocab đã tồn tại vào vocab set thuộc user hiện tại.
- Nếu quan hệ đã tồn tại, trả trạng thái thành công idempotent.

Response:

```json
{
  "vocabSetId": 12,
  "vocabId": 5
}
```

### 8.5. `DELETE /api/v1/vocab-sets/{vocabSetId}/vocabs/{vocabId}`

Behavior:

- Gỡ vocab khỏi vocab set thuộc user hiện tại.
- Nếu quan hệ không tồn tại, trả trạng thái thành công idempotent.

Response:

```json
{
  "vocabSetId": 12,
  "vocabId": 5
}
```

## 9. Danh sách file dự kiến tạo/chỉnh sửa

| File | Loại thay đổi | Mục đích |
|---|---|---|
| `src/main/java/net/runsystem/duyptk/BaiTap2_HoiNhapKyThuat_AI/domain/table/ItemType.java` | Tạo mới | Enum loại item theo docs |
| `src/main/java/net/runsystem/duyptk/BaiTap2_HoiNhapKyThuat_AI/domain/table/Item.java` | Tạo mới | Base entity map bảng `items` |
| `src/main/java/net/runsystem/duyptk/BaiTap2_HoiNhapKyThuat_AI/domain/table/Folder.java` | Tạo mới | Entity map bảng `folders` |
| `src/main/java/net/runsystem/duyptk/BaiTap2_HoiNhapKyThuat_AI/domain/table/VocabSet.java` | Tạo mới | Entity map bảng `vocab_sets` và join `vocab_vocab_set` |
| `src/main/java/net/runsystem/duyptk/BaiTap2_HoiNhapKyThuat_AI/repository/ItemRepository.java` | Tạo mới | Query item theo owner và parent |
| `src/main/java/net/runsystem/duyptk/BaiTap2_HoiNhapKyThuat_AI/repository/FolderRepository.java` | Tạo mới | Query folder theo id/owner |
| `src/main/java/net/runsystem/duyptk/BaiTap2_HoiNhapKyThuat_AI/repository/VocabSetRepository.java` | Tạo mới | Query vocab set theo id/owner |
| `src/main/java/net/runsystem/duyptk/BaiTap2_HoiNhapKyThuat_AI/domain/requestDTO/ReqCreateFolderDTO.java` | Tạo mới | Request tạo folder |
| `src/main/java/net/runsystem/duyptk/BaiTap2_HoiNhapKyThuat_AI/domain/requestDTO/ReqCreateVocabSetDTO.java` | Tạo mới | Request tạo vocab set |
| `src/main/java/net/runsystem/duyptk/BaiTap2_HoiNhapKyThuat_AI/domain/responseDTO/ResItemDTO.java` | Tạo mới | Response chung cho folder/vocab set |
| `src/main/java/net/runsystem/duyptk/BaiTap2_HoiNhapKyThuat_AI/domain/responseDTO/ResVocabSetVocabDTO.java` | Tạo mới | Response thao tác gắn/gỡ vocab |
| `src/main/java/net/runsystem/duyptk/BaiTap2_HoiNhapKyThuat_AI/service/organization/OrganizationService.java` | Tạo mới | Business logic Organization |
| `src/main/java/net/runsystem/duyptk/BaiTap2_HoiNhapKyThuat_AI/controller/OrganizationController.java` | Tạo mới | API Organization |
| `src/test/java/net/runsystem/duyptk/BaiTap2_HoiNhapKyThuat_AI/service/organization/OrganizationServiceTests.java` | Tạo mới | Unit test service layer |
| `src/test/java/net/runsystem/duyptk/BaiTap2_HoiNhapKyThuat_AI/report/TestHtmlReportGenerator.java` | Chỉnh sửa nếu cần | Bổ sung mapping/report cho module Organization |

## 10. Vị trí thay đổi dự kiến

| File | Class/Component | Method/Khu vực | Nội dung thay đổi |
|---|---|---|---|
| `ItemType.java` | `ItemType` | Enum values | Khai báo `FOLDER`, `VOCAB_SET` |
| `Item.java` | `Item` | Entity mapping/fields | Map `items`, inheritance, owner, parent |
| `Folder.java` | `Folder` | Entity mapping/fields | Map `folders.folder_name` |
| `VocabSet.java` | `VocabSet` | Entity mapping/fields | Map `vocab_sets`, join table `vocab_vocab_set` |
| `ItemRepository.java` | `ItemRepository` | Query methods | Tìm item theo `user.id` và `parent.id` hoặc root |
| `FolderRepository.java` | `FolderRepository` | Query methods | Tìm folder theo id và owner |
| `VocabSetRepository.java` | `VocabSetRepository` | Query methods | Tìm vocab set theo id và owner |
| `ReqCreateFolderDTO.java` | DTO | Fields/validation | `folderName`, `parentId` |
| `ReqCreateVocabSetDTO.java` | DTO | Fields/validation | `vocabSetName`, `vocabSetDescription`, `parentId` |
| `ResItemDTO.java` | DTO | Fields/json mapping | `id`, `type`, `name`, `description`, `parentId`, `vocabCount` |
| `ResVocabSetVocabDTO.java` | DTO | Fields | `vocabSetId`, `vocabId` |
| `OrganizationService.java` | Service | `createFolder` | Tạo folder với owner/current user và parent folder hợp lệ |
| `OrganizationService.java` | Service | `createVocabSet` | Tạo vocab set với owner/current user và parent folder hợp lệ |
| `OrganizationService.java` | Service | `getChildren` | Lấy item con thuộc current user theo parent |
| `OrganizationService.java` | Service | `addVocabToSet`, `removeVocabFromSet` | Quản lý quan hệ n-n vocab/vocab set |
| `OrganizationController.java` | Controller | API methods | Expose các endpoint phase đầu |
| `OrganizationServiceTests.java` | Unit tests | Test methods | Kiểm tra create/list/add/remove và validation ownership |
| `TestHtmlReportGenerator.java` | Report generator | Module mapping/filter | Thêm module Organization nếu report hiện tại cần map test mới |

## 11. Unit test dự kiến

Unit test bằng JUnit cho `OrganizationService`:

- Tạo root folder thành công cho authenticated user.
- Tạo folder con thành công khi parent là folder thuộc user hiện tại.
- Tạo folder thất bại khi parent là vocab set hoặc không thuộc user hiện tại.
- Tạo vocab set thành công khi parent là folder thuộc user hiện tại.
- Tạo vocab set thất bại khi thiếu tên.
- Lấy root children chỉ trả item thuộc user hiện tại.
- Gắn vocab đã tồn tại vào vocab set thuộc user hiện tại.
- Gắn vocab idempotent khi quan hệ đã tồn tại.
- Gỡ vocab khỏi vocab set thuộc user hiện tại.
- Từ chối thao tác vocab set không thuộc user hiện tại.

Không thêm application context test hoặc repository test trừ khi người dùng phê duyệt riêng.

## 12. Rủi ro và lưu ý

- Docs Organization chưa đặc tả endpoint chính thức; các endpoint trong plan này là đề xuất phase đầu để hiện thực hóa business rule đã có.
- Class Table Inheritance với `@Inheritance(strategy = JOINED)` cần kiểm tra kỹ DDL do Spring/Hibernate sinh để đảm bảo khớp bảng `items`, `folders`, `vocab_sets`.
- `type` vừa là discriminator vừa là business field; khi triển khai cần tránh mapping trùng cột sai cách.
- Quan hệ `VocabSet.vocabs` có thể gây N+1 nếu response cần `vocabCount`; phase đầu chỉ dùng count từ collection đã load trong service test hoặc query bổ sung nếu cần.
- Recursive CTE chưa nằm trong scope phase đầu; các module Testing/Learning có thể cần plan bổ sung.
- Nếu cần uniqueness theo tên trong cùng folder, docs chưa yêu cầu nên không tự thêm.

## 13. Verification dự kiến

Sau khi được phê duyệt và triển khai, chạy:

```text
./gradlew test jacocoTestReport testHtmlReport checkstyleMain checkstyleTest pmdMain pmdTest
```

Nếu phát sinh lỗi từ code cũ ngoài phạm vi Organization, báo cáo rõ lỗi cũ và không tự ý refactor ngoài plan.
