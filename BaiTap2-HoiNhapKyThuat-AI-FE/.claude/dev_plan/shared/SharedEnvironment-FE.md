# Development Plan FE - Shared Environment Configuration

## 1. Thông tin chung

- Module/feature: Shared environment configuration cho Backend và Frontend
- Repository chính: workspace cha; thay đổi liên quan đến `BaiTap2-HoiNhapKyThuat-AI-BE` và `BaiTap2-HoiNhapKyThuat-AI-FE`
- Ngày lập plan: 2026-08-07
- Trạng thái: Chờ phê duyệt
- Phụ thuộc: Project Foundation, Auth/Authz và Library/Organization Frontend đã hoàn thành

## 2. Mục tiêu

- Tập trung các biến cấu hình dùng chung cho local development vào một mẫu env thống nhất.
- FE lấy API base URL từ biến `VITE_API_BASE_URL` thay vì hard-code.
- BE tiếp tục dùng cơ chế environment placeholder hiện tại của Spring Boot.
- Không đưa credential, access token, private key hoặc secret thật vào repository.

## 3. Hiện trạng đã kiểm tra

- BE `application.properties` đã hỗ trợ biến môi trường cho server port, database, CORS, JWT, Free Dictionary, audio storage, TTS và multipart.
- BE đang có default `VOCAB_AUDIO_STORAGE_DIR=build/vocab-audio`, là đường dẫn tương đối theo working directory của process.
- FE `src/api/config.ts` đang hard-code `http://localhost:8081`.
- Chưa có file `.env`, `.env.example` hoặc cơ chế root env dùng chung.
- FE dùng Vite nên chỉ tự động expose biến có prefix `VITE_`.
- Spring Boot không tự đọc file `.env`; biến phải được export/inject từ shell, IDE hoặc script chạy ứng dụng.

## 4. Phương án đề xuất

- Tạo root `.env.example` làm template dùng chung, chỉ chứa placeholder an toàn.
- Tạo hoặc cập nhật root `.gitignore` để ignore `.env`, `.env.*.local` và các file secret; vẫn track `.env.example`.
- FE đọc `import.meta.env.VITE_API_BASE_URL` với fallback an toàn cho local development.
- Chọn một cơ chế nạp root env cho FE, ưu tiên cấu hình Vite `envDir` trỏ về workspace root nếu phù hợp với cấu trúc hiện tại.
- BE nhận các biến từ môi trường process theo placeholder đã có trong `application.properties`; không thêm dotenv dependency nếu chưa được phê duyệt.
- Cập nhật README/run instructions để nạp env trước khi chạy BE và FE.

## 5. Biến dự kiến trong shared env

| Biến | Consumer | Mục đích | Secret |
|---|---|---|---|
| `VITE_API_BASE_URL` | FE | Base URL API | Không |
| `SERVER_PORT` | BE | Port Backend | Không |
| `DB_URL` | BE | JDBC URL | Có thể chứa thông tin hạ tầng |
| `DB_USERNAME` | BE | Database user | Nhạy cảm |
| `DB_PASSWORD` | BE | Database password | Có |
| `CORS_ALLOWED_ORIGINS` | BE | Origin FE được phép | Không |
| `JWT_SECRET` | BE | JWT signing secret | Có |
| `JWT_ACCESS_TOKEN_VALIDITY` | BE | Access token lifetime | Không |
| `JWT_REFRESH_TOKEN_VALIDITY` | BE | Refresh token lifetime | Không |
| `FREE_DICTIONARY_BASE_URL` | BE | IPA provider URL | Không |
| `FREE_DICTIONARY_LANGUAGE` | BE | IPA language | Không |
| `VOCAB_AUDIO_STORAGE_DIR` | BE | Audio storage path | Không |
| `VOCAB_AUDIO_BASE_URL` | BE | Audio public path | Không |
| `VOCAB_TTS_LANGUAGE_CODE` | BE | TTS language | Không |
| `MAX_FILE_SIZE` | BE | Multipart file limit | Không |
| `MAX_REQUEST_SIZE` | BE | Multipart request limit | Không |
| `DEFAULT_PAGE_SIZE` | BE | Pageable default | Không |
| `MAX_PAGE_SIZE` | BE | Pageable maximum | Không |

Không đưa `GOOGLE_APPLICATION_CREDENTIALS` hoặc nội dung service account JSON vào shared env/example; chỉ tham chiếu file secret local nếu Backend hiện yêu cầu.

## 6. Phạm vi không thực hiện

- Không commit `.env` chứa giá trị local hoặc secret thật.
- Không đổi database, JWT model, CORS policy, API contract hoặc authentication flow.
- Không tự động đổi `application.properties` sang dotenv library.
- Không di chuyển audio file hiện tại hoặc xóa thư mục `build/` chưa được theo dõi.
- Không chỉnh sửa Postman collection hoặc Html-template.

## 7. File dự kiến thay đổi

| File | Loại | Nội dung |
|---|---|---|
| `.env.example` tại workspace root | Tạo | Danh sách biến dùng chung, giá trị mẫu không nhạy cảm |
| `.gitignore` tại workspace root | Tạo/Sửa | Ignore env local và secret files |
| `BaiTap2-HoiNhapKyThuat-AI-FE/vite.config.ts` | Sửa nếu cần | Cho Vite đọc root env qua `envDir` |
| `BaiTap2-HoiNhapKyThuat-AI-FE/src/api/config.ts` | Sửa | Đọc `VITE_API_BASE_URL` |
| `BaiTap2-HoiNhapKyThuat-AI-FE/README.md` | Sửa | Hướng dẫn nạp env và chạy FE |
| `BaiTap2-HoiNhapKyThuat-AI-BE/README.md` hoặc tài liệu chạy BE | Sửa nếu có file phù hợp | Hướng dẫn export biến cho Spring Boot |
| `.claude/dev_plan/shared/SharedEnvironment-FE.md` | Tạo | Plan và kết quả implementation |
| `.claude/dev_plan/DevPlanSummary-FE.md` | Sửa | Thêm plan ở dòng cuối, không sửa lịch sử cũ |

## 8. Kiểm tra dự kiến

- Xác nhận `.env` thật không nằm trong Git tracking.
- Chạy FE với `VITE_API_BASE_URL` tùy chỉnh và kiểm tra URL request.
- Chạy BE với các biến môi trường được export và kiểm tra port, CORS, database/audio path.
- Chạy `npm run lint` và `npm run build` tại FE.
- Chạy quality task hiện có của BE theo Developer Plan Backend nếu thay đổi Backend config.

## 9. Rủi ro và quyết định cần xác nhận

- Một file `.env` tại workspace root không tự được Spring Boot đọc; cần export biến hoặc thêm script chạy. Plan không tự thêm dependency dotenv.
- Vite cần `VITE_` prefix và cấu hình `envDir`; nếu không muốn đổi Vite config, phương án thay thế là đặt `.env` riêng trong FE, không phải shared file thật sự.
- Root workspace hiện có file audio chưa tracked tại `build/vocab-audio`; plan không đụng vào file này.
- Cần xác nhận có cho phép tạo/sửa file ở workspace root và tài liệu Backend hay chỉ muốn `.env.example` cho FE.

## 10. Trạng thái

- Chờ người dùng phê duyệt plan trước khi tạo hoặc sửa file cấu hình/source.
