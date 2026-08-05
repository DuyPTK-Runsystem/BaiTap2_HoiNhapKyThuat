# Developer Plan: Organization Module

## 1. Trạng thái

- Trạng thái phê duyệt: Đã phê duyệt một phần cho phase `POST /api/v1/folders`, `POST /api/v1/vocab-sets`, `GET /api/v1/items/children`, add/bulk add vocab vào vocab set, search item/get item by path, unique tên item trong cùng parent.
- Trạng thái triển khai: Đã triển khai phase `POST /api/v1/folders`, `POST /api/v1/vocab-sets`, `GET /api/v1/items/children`, add/bulk add vocab vào vocab set, search item/get item by path, unique tên item trong cùng parent.
- Ngày tạo plan: 2026-08-04.
- Agent tạo plan: Codex.
- Ngày cập nhật gần nhất: 2026-08-05.
- Agent cập nhật gần nhất: Codex.
- Lý do tạo/cập nhật plan: Đã triển khai unique tên item trong cùng parent.

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
  - Mục 3: Search/get item by path chỉ trả item thuộc authenticated user hiện tại; response có `itemPath`.
  - Mục 3: Tên item unique trong cùng parent của cùng authenticated user, áp dụng chung `Folder` và `VocabSet`.
  - Mục 4.1: Add one vocab to vocab set trả thông tin `VocabSet`, `Vocab`, `added`.
  - Mục 4.2: Bulk add xử lý nhiều `vocabId` độc lập với Partial Failure.
  - Mục 4.3: Search item theo tên bằng contains/LIKE, trả danh sách phẳng có `itemPath`.
  - Mục 4.4: Get item by path resolve từ virtual super root tương tự `GET /items/children`.
- `.claude/docs/Data_Architecture.md`
  - Mục 2.2: Bảng `items`, `folders`, `vocab_sets`; `type` gồm `FOLDER`, `VOCAB_SET`; `user_id` là FK đến `users.user_id`; `parent_id` là FK đến `items.item_id`.
  - Mục 2.3: Bảng `vocab_vocab_set` liên kết `vocabs` và `vocab_sets`.
  - Mục 3.2: Unique tên item trong cùng parent được đảm bảo ở application level do tên nằm ở hai bảng dẫn xuất khác nhau.
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

### 4.0.1. Scope đã được phê duyệt triển khai tiếp theo

Triển khai tiếp:

- `GET /api/v1/items/children`.

Contract đã được người dùng xác nhận:

- Không có `parentId`: trả tất cả root items của authenticated user, tức các item có `parent_id = null`.
- Có `parentId`: `parentId` phải là `Folder` thuộc authenticated user, trả direct children của folder đó.
- Có thể hiểu `parentId = null` là lấy children của một virtual super root folder, nhưng không tạo virtual root trong database.
- Không trả recursive tree trong phase này.

### 4.0.2. Scope đã được phê duyệt triển khai tiếp theo

Triển khai tiếp:

- `POST /api/v1/vocab-sets/{vocabSetId}/vocabs/{vocabId}`.
- `POST /api/v1/vocab-sets/{vocabSetId}/vocabs/bulk`.

Contract:

- Single add chỉ tạo quan hệ n-n giữa `VocabSet` và `Vocab`; không tạo vocab mới.
- Single add response trả thông tin `VocabSet`, thông tin `Vocab`, và cờ `added`.
- Bulk add nhận danh sách `vocabIds`.
- Bulk add xử lý từng `vocabId` độc lập theo Partial Failure.
- Bulk add response trả summary `total`, `success`, `failed`, thông tin `VocabSet`, và kết quả từng item.
- Gắn vocab đã tồn tại trong vocab set là success idempotent với `added = false`.

### 4.0.3. Scope đã được phê duyệt triển khai tiếp theo

Triển khai tiếp:

- `GET /api/v1/items/search?name={keyword}`.
- `GET /api/v1/items/by-path?path={itemPath}`.
- Bổ sung `itemPath` vào response item cho hai API trên.

Contract đề xuất:

- Search dùng contains/LIKE, không exact name.
- Search áp dụng cho cả `Folder.folderName` và `VocabSet.vocabSetName`.
- Search chỉ trả danh sách phẳng các item mà authenticated user hiện tại sở hữu/truy cập được.
- `itemPath` là đường dẫn tên từ virtual super root tới item, ví dụ `/IELTS/Unit 1/Common verbs`.
- Get item by path resolve từng segment từ virtual super root:
  - Segment đầu tiên match root item có `parent_id = null`.
  - Segment tiếp theo match direct child của segment trước đó.
  - Không tạo virtual root trong database.
- Segment trong path match exact name sau khi trim, không dùng LIKE.
- Nếu path đi tiếp dưới một `VOCAB_SET`, trả lỗi validation vì `VOCAB_SET` không chứa item con.
- Nếu có nhiều sibling cùng tên làm path mơ hồ, trả lỗi conflict/validation thay vì chọn ngầm.

### 4.0.4. Scope đã được phê duyệt triển khai tiếp theo

Triển khai tiếp:

- Unique tên item trong cùng parent của cùng authenticated user.
- Rule áp dụng chung giữa `Folder.folderName` và `VocabSet.vocabSetName`.
- Root items được xem là cùng thuộc virtual super root, nên tên root item cũng unique theo user.

Contract đề xuất:

- Khi tạo folder, tên sau khi trim không được trùng với folder hoặc vocab set sibling của cùng user trong cùng parent.
- Khi tạo vocab set, tên sau khi trim không được trùng với folder hoặc vocab set sibling của cùng user trong cùng parent.
- Kiểm tra trùng tên dùng exact name sau khi trim.
- Scope này không đổi API request/response.
- Với schema hiện tại, tên item nằm ở hai bảng con (`folders.folder_name`, `vocab_sets.vocab_set_name`), nên triển khai bằng service-level validation thay vì database unique constraint chung.
- Không thêm cột `item_name` vào bảng `items`, không migration dữ liệu cũ và không refactor inheritance schema trong scope này.

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
- Search item theo tên LIKE trong phạm vi item thuộc user hiện tại.
- Lấy item theo path tên từ virtual super root trong phạm vi item thuộc user hiện tại.
- Từ chối tạo folder/vocab set nếu tên item bị trùng với sibling trong cùng parent của cùng user.
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
- Search item yêu cầu keyword không blank sau khi trim.
- Get item by path yêu cầu path có ít nhất một segment không blank sau normalize.
- Path segment không được blank; ví dụ `/IELTS//Unit 1` là invalid.
- Path mơ hồ do sibling trùng tên là conflict/validation error.
- Tên folder/vocab set sau trim phải unique trong cùng parent của cùng user.
- Unique name được kiểm tra trước khi save trong service để bao phủ cả hai bảng `folders` và `vocab_sets`.

## 5. Phạm vi không thực hiện

Không thực hiện trong plan này:

- Move item giữa các folder.
- Đổi tên folder hoặc vocab set.
- Xóa folder hoặc vocab set.
- Lấy toàn bộ tree bằng recursive API.
- Recursive CTE để lấy vocab từ folder; phần này sẽ cần cho Testing/Learning hoặc API browse sâu hơn.
- Bulk import vocab trực tiếp vào vocab set.
- Tạo hoặc lưu virtual super root trong database.
- Database-level unique constraint chung cho tên item trong cùng parent nếu chưa có phê duyệt refactor schema riêng.
- Refactor schema để thêm cột tên chung vào `items`.
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

### 8.4.1. `POST /api/v1/vocab-sets/{vocabSetId}/vocabs/bulk`

Request:

```json
{
  "vocabIds": [5, 6, 7]
}
```

Behavior:

- Gắn nhiều vocab đã tồn tại vào vocab set thuộc user hiện tại.
- Mỗi `vocabId` được xử lý độc lập.
- Nếu một `vocabId` không tồn tại hoặc không hợp lệ, item đó thất bại nhưng các item hợp lệ vẫn được xử lý.
- Nếu quan hệ đã tồn tại, item đó success idempotent với `added = false`.

Response:

```json
{
  "vocabSet": {
    "id": 12,
    "name": "Common verbs",
    "description": "Basic daily verbs",
    "parentId": 11,
    "vocabCount": 5
  },
  "total": 3,
  "success": 2,
  "failed": 1,
  "items": [
    {
      "vocabId": 5,
      "success": true,
      "added": true,
      "vocab": {
        "id": 5,
        "word": "go",
        "meaning": "di chuyen",
        "ipa": "gəʊ",
        "audio_url": "/api/v1/vocabs/audio/go.mp3"
      }
    },
    {
      "vocabId": 999,
      "success": false,
      "added": false,
      "error": "Vocab không tồn tại"
    }
  ]
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

### 8.6. `GET /api/v1/items/search`

Request:

```text
GET /api/v1/items/search?name=verb
```

Behavior:

- Search theo contains/LIKE, không exact name.
- Tìm trong cả folder name và vocab set name.
- Chỉ trả item thuộc authenticated user hiện tại.
- Response là danh sách phẳng, sắp xếp ổn định theo `id` tăng dần sau khi merge kết quả folder/vocab set.
- Mỗi item có `itemPath`.

Response:

```json
[
  {
    "id": 12,
    "type": "VOCAB_SET",
    "name": "Common verbs",
    "description": "Basic daily verbs",
    "parentId": 11,
    "vocabCount": 5,
    "itemPath": "/IELTS/Unit 1/Common verbs"
  }
]
```

### 8.7. `GET /api/v1/items/by-path`

Request:

```text
GET /api/v1/items/by-path?path=/IELTS/Unit%201/Common%20verbs
```

Behavior:

- Resolve path từ virtual super root theo từng segment.
- Leading slash và trailing slash được normalize.
- Segment đầu tiên tìm trong root items của user hiện tại.
- Segment tiếp theo tìm trong direct children của item trước đó.
- Segment match exact name sau trim.
- Nếu có nhiều sibling cùng tên tại một segment, trả lỗi conflict/validation vì path không đủ rõ.
- Nếu không tìm thấy item, trả lỗi resource not found/validation theo convention exception hiện tại.

Response:

```json
{
  "id": 12,
  "type": "VOCAB_SET",
  "name": "Common verbs",
  "description": "Basic daily verbs",
  "parentId": 11,
  "vocabCount": 5,
  "itemPath": "/IELTS/Unit 1/Common verbs"
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
| `src/main/java/net/runsystem/duyptk/BaiTap2_HoiNhapKyThuat_AI/domain/requestDTO/ReqBulkAddVocabToSetDTO.java` | Tạo mới | Request bulk add vocab vào vocab set |
| `src/main/java/net/runsystem/duyptk/BaiTap2_HoiNhapKyThuat_AI/domain/responseDTO/ResVocabSetSummaryDTO.java` | Tạo mới | Tóm tắt vocab set trong response add/bulk add |
| `src/main/java/net/runsystem/duyptk/BaiTap2_HoiNhapKyThuat_AI/domain/responseDTO/ResVocabSetBulkAddDTO.java` | Tạo mới | Response tổng hợp bulk add |
| `src/main/java/net/runsystem/duyptk/BaiTap2_HoiNhapKyThuat_AI/domain/responseDTO/ResVocabSetBulkAddItemDTO.java` | Tạo mới | Response từng item trong bulk add |
| `src/main/java/net/runsystem/duyptk/BaiTap2_HoiNhapKyThuat_AI/service/organization/OrganizationService.java` | Tạo mới | Business logic Organization |
| `src/main/java/net/runsystem/duyptk/BaiTap2_HoiNhapKyThuat_AI/service/organization/OrganizationItemLookupService.java` | Tạo mới | Business logic search item và get item by path |
| `src/main/java/net/runsystem/duyptk/BaiTap2_HoiNhapKyThuat_AI/service/organization/OrganizationItemNameValidationService.java` | Tạo mới | Service-level validation unique tên item trong cùng parent |
| `src/main/java/net/runsystem/duyptk/BaiTap2_HoiNhapKyThuat_AI/controller/OrganizationController.java` | Tạo mới | API Organization |
| `src/test/java/net/runsystem/duyptk/BaiTap2_HoiNhapKyThuat_AI/service/organization/OrganizationServiceTests.java` | Tạo mới | Unit test service layer |
| `src/test/java/net/runsystem/duyptk/BaiTap2_HoiNhapKyThuat_AI/service/organization/OrganizationItemNameValidationServiceTests.java` | Tạo mới | Unit test unique tên item trong cùng parent |
| `src/test/java/net/runsystem/duyptk/BaiTap2_HoiNhapKyThuat_AI/service/organization/OrganizationItemSearchServiceTests.java` | Tạo mới | Unit test search item |
| `src/test/java/net/runsystem/duyptk/BaiTap2_HoiNhapKyThuat_AI/service/organization/OrganizationItemPathServiceTests.java` | Tạo mới | Unit test get item by path |
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
| `FolderRepository.java` | `FolderRepository` | Search/path query methods | Tìm folder theo owner và `folderName` contains/LIKE; tìm exact name theo root/direct parent |
| `FolderRepository.java` | `FolderRepository` | Unique name query methods | Kiểm tra folder sibling trùng tên theo user, parent/root |
| `VocabSetRepository.java` | `VocabSetRepository` | Query methods | Tìm vocab set theo id và owner |
| `VocabSetRepository.java` | `VocabSetRepository` | Search/path query methods | Tìm vocab set theo owner và `vocabSetName` contains/LIKE; tìm exact name theo root/direct parent |
| `VocabSetRepository.java` | `VocabSetRepository` | Unique name query methods | Kiểm tra vocab set sibling trùng tên theo user, parent/root |
| `ReqCreateFolderDTO.java` | DTO | Fields/validation | `folderName`, `parentId` |
| `ReqCreateVocabSetDTO.java` | DTO | Fields/validation | `vocabSetName`, `vocabSetDescription`, `parentId` |
| `ResItemDTO.java` | DTO | Fields/json mapping | `id`, `type`, `name`, `description`, `parentId`, `vocabCount`, `itemPath` |
| `ResVocabSetVocabDTO.java` | DTO | Fields | `vocabSetId`, `vocabId` |
| `ReqBulkAddVocabToSetDTO.java` | DTO | Fields | Danh sách `vocabIds` |
| `ResVocabSetSummaryDTO.java` | DTO | Fields | `id`, `name`, `description`, `parentId`, `vocabCount` |
| `ResVocabSetBulkAddDTO.java` | DTO | Fields | `vocabSet`, `total`, `success`, `failed`, `items` |
| `ResVocabSetBulkAddItemDTO.java` | DTO | Fields | `vocabId`, `success`, `added`, `vocab`, `error` |
| `OrganizationService.java` | Service | `createFolder` | Tạo folder với owner/current user và parent folder hợp lệ |
| `OrganizationService.java` | Service | `createVocabSet` | Tạo vocab set với owner/current user và parent folder hợp lệ |
| `OrganizationService.java` | Service | `createFolder`, `createVocabSet` | Gọi validation unique sibling name trước khi save |
| `OrganizationItemNameValidationService.java` | Service | `validateUniqueSiblingName` | Từ chối tạo item nếu sibling folder/vocab set trùng tên trong cùng parent |
| `OrganizationService.java` | Service | `getChildren` | Lấy item con thuộc current user theo parent |
| `OrganizationItemLookupService.java` | Service | `searchItems` | Search folder/vocab set theo tên LIKE trong phạm vi current user và build `itemPath` |
| `OrganizationItemLookupService.java` | Service | `getItemByPath` | Resolve item path từ virtual super root theo direct child |
| `OrganizationItemLookupService.java` | Service | Path helpers | Normalize path, lấy display name theo item type, build path từ parent chain |
| `OrganizationService.java` | Service | `addVocabToSet`, `removeVocabFromSet` | Quản lý quan hệ n-n vocab/vocab set |
| `OrganizationService.java` | Service | `bulkAddVocabsToSet` | Gắn nhiều vocab với Partial Failure |
| `OrganizationController.java` | Controller | API methods | Expose các endpoint phase đầu và search/path nếu được phê duyệt |
| `OrganizationServiceTests.java` | Unit tests | Test methods | Kiểm tra create/list/add/remove và validation ownership |
| `OrganizationItemNameValidationServiceTests.java` | Unit tests | Test methods | Kiểm tra duplicate root/child sibling name |
| `OrganizationItemSearchServiceTests.java` | Unit tests | Test methods | Kiểm tra search item, ownership filter và itemPath |
| `OrganizationItemPathServiceTests.java` | Unit tests | Test methods | Kiểm tra resolve path, validation và path mơ hồ |
| `TestHtmlReportGenerator.java` | Report generator | Module mapping/filter | Thêm module Organization nếu report hiện tại cần map test mới |

## 11. Unit test dự kiến

Unit test bằng JUnit cho `OrganizationService`:

- Tạo root folder thành công cho authenticated user.
- Tạo folder con thành công khi parent là folder thuộc user hiện tại.
- Tạo folder thất bại khi parent là vocab set hoặc không thuộc user hiện tại.
- Tạo folder thất bại khi tên trùng folder hoặc vocab set sibling cùng parent.
- Tạo vocab set thành công khi parent là folder thuộc user hiện tại.
- Tạo vocab set thất bại khi thiếu tên.
- Tạo vocab set thất bại khi tên trùng folder hoặc vocab set sibling cùng parent.
- Lấy root children chỉ trả item thuộc user hiện tại.
- Gắn vocab đã tồn tại vào vocab set thuộc user hiện tại.
- Gắn vocab idempotent khi quan hệ đã tồn tại.
- Gỡ vocab khỏi vocab set thuộc user hiện tại.
- Từ chối thao tác vocab set không thuộc user hiện tại.
- Search item theo tên contains/LIKE trả folder và vocab set match.
- Search item chỉ trả item của authenticated user hiện tại.
- Search item trả `itemPath` đúng với cây parent.
- Search item reject keyword blank.
- Get item by path resolve root item và nested item đúng user.
- Get item by path trả `itemPath` đúng.
- Get item by path reject path blank hoặc có segment blank.
- Get item by path reject path mơ hồ khi có sibling trùng tên.
- Get item by path reject path đi tiếp dưới `VOCAB_SET`.

Không thêm application context test hoặc repository test trừ khi người dùng phê duyệt riêng.

## 12. Rủi ro và lưu ý

- Docs Organization chưa đặc tả endpoint chính thức; các endpoint trong plan này là đề xuất phase đầu để hiện thực hóa business rule đã có.
- Class Table Inheritance với `@Inheritance(strategy = JOINED)` cần kiểm tra kỹ DDL do Spring/Hibernate sinh để đảm bảo khớp bảng `items`, `folders`, `vocab_sets`.
- `type` vừa là discriminator vừa là business field; khi triển khai cần tránh mapping trùng cột sai cách.
- Quan hệ `VocabSet.vocabs` có thể gây N+1 nếu response cần `vocabCount`; phase đầu chỉ dùng count từ collection đã load trong service test hoặc query bổ sung nếu cần.
- Recursive CTE chưa nằm trong scope phase đầu; các module Testing/Learning có thể cần plan bổ sung.
- Nếu cần uniqueness theo tên trong cùng folder, docs chưa yêu cầu nên không tự thêm.
- Với schema hiện tại, không thể tạo database unique constraint chung cho `Folder.folderName` và `VocabSet.vocabSetName` trong cùng parent nếu không thêm cột tên chung hoặc trigger.
- Plan này đề xuất service-level validation để đáp ứng business rule trong scope nhỏ.
- Dữ liệu trùng đã tồn tại trong database cũ không được tự động dọn trong scope này.
- Race condition vẫn có thể xảy ra nếu hai request tạo cùng tên đồng thời; cần DB-level constraint/refactor schema nếu muốn đảm bảo tuyệt đối ở database.
- Path theo tên có thể mơ hồ với dữ liệu cũ đã trùng; dữ liệu mới sau validation sẽ tránh trùng sibling name.
- `itemPath` là derived field, cần build trong transaction hoặc tránh LazyInitialization khi đọc parent chain.

## 13. Verification dự kiến

Sau khi được phê duyệt và triển khai, chạy:

```text
./gradlew test jacocoTestReport testHtmlReport checkstyleMain checkstyleTest pmdMain pmdTest
```

Nếu phát sinh lỗi từ code cũ ngoài phạm vi Organization, báo cáo rõ lỗi cũ và không tự ý refactor ngoài plan.
