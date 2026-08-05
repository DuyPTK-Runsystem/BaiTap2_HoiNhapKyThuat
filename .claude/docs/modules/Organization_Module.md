# Organization Module Specification

## 1. Entities (Inheritance Model)
- **Item (Base Class)**: `item_id`, `type` (FOLDER/VOCAB_SET), `user_id`, `parent_id`.
- **Folder**: Kế thừa từ `Item`. Chứa các `Item` con.
- **VocabSet**: Kế thừa từ `Item`. Chứa danh sách `Vocab_id`.

## 2. Relationships
- `Folder` $\rightarrow$ `VocabSet`: 1-n (Một VocabSet chỉ thuộc 1 Folder).
- `VocabSet` $\leftrightarrow$ `Vocab`: n-n (Thông qua bảng trung gian `vocab_vocab_set`).

## 3. Business Rules
- Mỗi `VocabSet` phải có chủ sở hữu (`user_id`).
- Hỗ trợ cấu trúc cây vô hạn thông qua `parent_id`.
- Gắn `Vocab` vào `VocabSet` chỉ tạo quan hệ n-n, không tạo `Vocab` mới.
- API gắn một `Vocab` vào `VocabSet` phải trả thông tin tóm tắt của cả `VocabSet` và `Vocab`.
- Bulk add cho phép gắn nhiều `Vocab` đã tồn tại vào một `VocabSet` trong một request.
- Bulk add phải áp dụng cơ chế Partial Failure: vocab lỗi hoặc không tồn tại thì ghi nhận lỗi item đó, các vocab hợp lệ vẫn được gắn.
- Gắn lại vocab đã có trong vocab set phải được xử lý idempotent, không tạo duplicate trong bảng `vocab_vocab_set`.
- Search item theo tên phải dùng contains/LIKE, không yêu cầu exact name.
- Search item và get item by path chỉ được trả item thuộc authenticated user hiện tại.
- Response item cho search và get by path phải có `itemPath`.
- `itemPath` là đường dẫn tên từ virtual super root tới item, dùng `/` làm separator; virtual super root không được lưu trong database.
- Get item by path resolve từng segment theo direct child, tương tự cách `GET /api/v1/items/children` xem root items là children của virtual super root.
- Tên item phải unique trong cùng một parent của cùng một authenticated user.
- Unique tên item trong cùng parent áp dụng chung giữa `Folder.folderName` và `VocabSet.vocabSetName`.
- Root items được xem là cùng thuộc virtual super root; vì vậy tên root item cũng phải unique trong phạm vi authenticated user.
- So sánh unique name dùng tên sau khi trim.

## 4. API Requirements

### 4.1. Add one vocab to vocab set

```text
POST /api/v1/vocab-sets/{vocabSetId}/vocabs/{vocabId}
```

Behavior:

- `vocabSetId` phải thuộc authenticated user hiện tại.
- `vocabId` phải tồn tại trong bảng `vocabs`.
- Nếu quan hệ đã tồn tại, trả success idempotent.
- Response phải có thông tin `VocabSet` và `Vocab`.

Response shape:

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

### 4.2. Bulk add vocabs to vocab set

```text
POST /api/v1/vocab-sets/{vocabSetId}/vocabs/bulk
```

Request:

```json
{
  "vocabIds": [5, 6, 7]
}
```

Behavior:

- `vocabSetId` phải thuộc authenticated user hiện tại.
- Mỗi `vocabId` được xử lý độc lập.
- Nếu một `vocabId` không tồn tại hoặc không hợp lệ, chỉ item đó thất bại.
- Các vocab hợp lệ vẫn được gắn vào vocab set.
- Nếu quan hệ đã tồn tại, item đó được tính là success idempotent với `added = false`.

Response shape:

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

### 4.3. Search items by name

```text
GET /api/v1/items/search?name={keyword}
```

Behavior:

- Endpoint yêu cầu authenticated user.
- `name` là từ khóa bắt buộc sau khi trim.
- Tìm kiếm theo contains/LIKE, không exact match.
- Search áp dụng cho cả `Folder.folderName` và `VocabSet.vocabSetName`.
- Chỉ trả item thuộc authenticated user hiện tại.
- Response trả danh sách phẳng, không trả recursive tree.
- Mỗi item trong response phải có `itemPath`.

Response shape:

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

### 4.4. Get item by path

```text
GET /api/v1/items/by-path?path={itemPath}
```

Behavior:

- Endpoint yêu cầu authenticated user.
- `path` là đường dẫn tên item, dùng `/` làm separator.
- Leading slash và trailing slash được phép; service normalize trước khi resolve.
- Path được resolve từ virtual super root:
  - Segment đầu tiên match với root item của authenticated user.
  - Các segment tiếp theo match với direct child của item trước đó.
- Match path segment là exact name sau khi trim, không dùng LIKE.
- Nếu segment hiện tại là `VOCAB_SET` và path còn segment tiếp theo, request thất bại vì `VOCAB_SET` không chứa item con.
- Chỉ trả item thuộc authenticated user hiện tại.
- Response phải có `itemPath`.
- Nếu có nhiều item cùng tên trong cùng một parent khiến path mơ hồ, trả lỗi conflict/validation thay vì chọn ngầm một item.

Response shape:

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

## 5. Lịch sử cập nhật

| Ngày | Nội dung | Người cập nhật |
|---|---|---|
| 2026-08-05 | Bổ sung contract add vocab vào vocab set với response có thông tin vocab/vocab set và bulk add Partial Failure | Codex |
| 2026-08-05 | Điều chỉnh ví dụ response Organization dùng `audio_url` theo `ResVocabDTO` hiện có | Codex |
| 2026-08-05 | Bổ sung yêu cầu search item theo tên LIKE, trả `itemPath`, và get item by path theo virtual super root | Codex |
| 2026-08-05 | Bổ sung yêu cầu tên item unique trong cùng parent của cùng user, áp dụng chung Folder và VocabSet | Codex |
