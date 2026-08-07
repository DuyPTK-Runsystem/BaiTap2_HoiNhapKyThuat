# Development Plan FE - Separate Environment Files

## 1. Thông tin chung

- Module/feature: Tách file `.env` cho Backend và Frontend
- Phạm vi repository: `BaiTap2-HoiNhapKyThuat-AI-BE` và `BaiTap2-HoiNhapKyThuat-AI-FE`
- Ngày lập plan: 2026-08-07
- Trạng thái: Chờ phê duyệt
- Thay thế phương án shared root `.env` trong `SharedEnvironment-FE.md` theo yêu cầu mới nhất.

## 2. Mục tiêu

- Tạo file env local riêng cho BE và FE, không dùng shared root env.
- FE đọc API URL qua `VITE_API_BASE_URL`.
- BE nhận biến môi trường qua Spring Boot placeholders hiện có.
- Không ghi secret thật vào Git và không expose secret sang Frontend.

## 3. File dự kiến

| File | Loại | Nội dung |
|---|---|---|
| `BaiTap2-HoiNhapKyThuat-AI-BE/.env` | Tạo local | Port, database, CORS, JWT, audio, TTS và multipart variables |
| `BaiTap2-HoiNhapKyThuat-AI-FE/.env` | Tạo local | `VITE_API_BASE_URL` |
| `BaiTap2-HoiNhapKyThuat-AI-BE/.gitignore` | Sửa | Ignore `.env` và local env files |
| `BaiTap2-HoiNhapKyThuat-AI-FE/.gitignore` | Sửa | Ignore `.env` và local env files |
| `BaiTap2-HoiNhapKyThuat-AI-FE/.env.example` | Tạo nếu được duyệt | Template không chứa secret thật |
| `BaiTap2-HoiNhapKyThuat-AI-BE/.env.example` | Tạo nếu được duyệt | Template không chứa secret thật |
| `.claude/dev_plan/shared/EnvironmentFiles-FE.md` | Tạo | Plan và kết quả thay đổi |
| `.claude/dev_plan/DevPlanSummary-FE.md` | Sửa cuối lịch sử | Ghi nhận plan mới ở dòng cuối, không sửa lịch sử cũ |

## 4. Giá trị dự kiến

### Backend

```env
SERVER_PORT=8081
DB_URL=jdbc:mysql://localhost:3306/english_learning_support
DB_USERNAME=root
DB_PASSWORD=
CORS_ALLOWED_ORIGINS=http://localhost:5174,http://localhost:5173
JWT_SECRET=
JWT_ACCESS_TOKEN_VALIDITY=86400
JWT_REFRESH_TOKEN_VALIDITY=604800
VOCAB_AUDIO_STORAGE_DIR=build/vocab-audio
VOCAB_AUDIO_BASE_URL=/api/v1/vocabs/audio
VOCAB_TTS_LANGUAGE_CODE=en-GB
MAX_FILE_SIZE=50MB
MAX_REQUEST_SIZE=50MB
DEFAULT_PAGE_SIZE=20
MAX_PAGE_SIZE=2000
```

### Frontend

```env
VITE_API_BASE_URL=http://localhost:8081
```

Không đưa `service-for-tts-key.json`, JWT secret thật, database password thật, access token hoặc credential thật vào file được track.

## 5. Phương án tích hợp

- FE cập nhật `src/api/config.ts` để đọc `import.meta.env.VITE_API_BASE_URL` và giữ fallback local.
- BE giữ `application.properties` làm nơi mapping `${VARIABLE:default}`.
- Vì Spring Boot không tự đọc `.env`, cần export các biến từ file trước khi chạy `./gradlew bootRun`, hoặc cấu hình file env trong IDE/Run Configuration.
- Không thêm dotenv dependency và không thay đổi API/auth model.

## 6. Kiểm tra dự kiến

- Xác nhận cả hai `.env` bị ignore và không xuất hiện trong Git diff.
- Kiểm tra FE build với `VITE_API_BASE_URL`.
- Kiểm tra BE khởi động với `VOCAB_AUDIO_STORAGE_DIR` và audio được ghi đúng vị trí.
- Chạy `npm run lint` và `npm run build` tại FE.
- Chạy quality checks Backend nếu thay đổi file Backend được phê duyệt.

## 7. Rủi ro và giới hạn

- File `.env` Backend không tự được Spring Boot load; cần cách inject rõ ràng khi chạy.
- Giá trị `VITE_*` là public trong bundle; không đặt secret vào `.env` Frontend.
- Không xử lý hoặc xóa audio chưa tracked tại workspace `build/` trong scope này.

## 8. Trạng thái

- Chờ người dùng phê duyệt rõ ràng trước khi tạo hoặc sửa file `.env`, `.gitignore`, config và source.

## 9. Kết quả triển khai

- Đã tạo `.env` và `.env.example` riêng cho Backend.
- Đã tạo `.env` và `.env.example` riêng cho Frontend.
- Đã cập nhật FE đọc `VITE_API_BASE_URL` từ `import.meta.env` với fallback local.
- `.env` thật được ignore; không thêm DB password hoặc JWT secret thật vào file local đã tạo.
- Backend tiếp tục dùng mapping environment variables trong `application.properties`; `.env` cần được export trước khi chạy `./gradlew bootRun` vì Spring Boot không tự đọc dotenv.
- Đã chạy `npm run lint`: Pass.
- Đã chạy `npm run build`: Pass, bao gồm `tsc -b` và `vite build`.
- Đã chạy `git diff --check`: Pass.
- Không thay đổi Backend source, Postman collection hoặc Html-template.
