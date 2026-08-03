# Auth Module Specification

## 1. Entities
- **User**: `id`, `email` (unique), `hash_password`, `refresh_token`.

## 2. User Stories & API
- **User Registration**: Đăng ký tài khoản mới.
- **User Login**: Đăng nhập và nhận token.

## 3. Business Rules
- Password phải được Hash trước khi lưu trữ.
- Email là duy nhất trong hệ thống.