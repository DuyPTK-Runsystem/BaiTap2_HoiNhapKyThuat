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

## 5. Lịch sử cập nhật

| Ngày | Nội dung | Người cập nhật |
|---|---|---|
| 2026-08-05 | Bổ sung contract add vocab vào vocab set với response có thông tin vocab/vocab set và bulk add Partial Failure | Codex |
| 2026-08-05 | Điều chỉnh ví dụ response Organization dùng `audio_url` theo `ResVocabDTO` hiện có | Codex |
