# Vocabulary Management Module Specification

## 1. Entities
- **Vocab**: `vocab_id`, `word`, `meaning`, `ipa`, `audio_url`.

## 2. Functional Requirements
### BM1: Manual Import
- Cho phép thêm từ lẻ.
- **Rule**: Nếu không tìm thấy IPA, bắt buộc phải có đầy đủ `Word`, `Meaning`, `IPA` mới được lưu.
- **Rule Update**: Khi update, chỉ cho phép sửa `Meaning`.

### BM2: Bulk Import (.xlsx)
- Nhập hàng loạt từ file.
- **Rule (Critical)**: Áp dụng cơ chế **Partial Failure**. Nếu 1 dòng lỗi, bỏ qua dòng đó và tiếp tục dòng tiếp theo.

## 3. Automation
- Tự động chuyển hóa từ vựng sang phiên âm IPA.
- Tự động xử lý Audio URL.