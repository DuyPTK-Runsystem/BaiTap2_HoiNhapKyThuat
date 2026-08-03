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