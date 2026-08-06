# Auth Module Specification

## 1. Entities
- **User**: `id`, `email` (unique), `hash_password`, `refresh_token`, `created_at`, `updated_at`, `created_by`, `updated_by`.

## 2. User Stories & API
- **User Registration**: Đăng ký tài khoản mới.
- **User Login**: Đăng nhập và nhận token.

## 3. Business Rules
- Password phải được Hash trước khi lưu trữ.
- Email là duy nhất trong hệ thống.
- Các entity phải có audit fields để theo dõi thời điểm và tác nhân tạo/cập nhật dữ liệu.
