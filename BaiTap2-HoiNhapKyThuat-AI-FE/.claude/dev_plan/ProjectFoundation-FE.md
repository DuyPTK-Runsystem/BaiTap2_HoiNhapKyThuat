# Development Plan FE - Project Foundation

## 1. Thông tin chung

- Module/feature: Project Foundation Frontend
- Repository thực hiện: `BaiTap2-HoiNhapKyThuat-AI-FE`
- Ngày lập plan: 2026-08-06
- Trạng thái: Hoàn thành

## 2. Mục tiêu

Thiết lập nền tảng Frontend cho ứng dụng Vocab Library từ Vite starter hiện tại, tạo cấu trúc dự án React/TypeScript có thể mở rộng cho các module Auth, Vocabulary, Organization, Testing và Flashcard.

Foundation chỉ dựng khung kỹ thuật và UI shell tối thiểu, chưa triển khai đầy đủ từng màn hình nghiệp vụ nếu chưa có Development Plan riêng cho module đó.

## 3. Tài liệu và tài nguyên đã đối chiếu

- `.claude/rules/CLAUDE.md`
- `.claude/workflows/WORKFLOW.md`
- `.claude/skills/SKILL.md`
- `../BaiTap2-HoiNhapKyThuat-AI-BE/.claude/rules/CLAUDE.md`
- `../BaiTap2-HoiNhapKyThuat-AI-BE/.claude/docs/ApplicationContext.md`
- `../BaiTap2-HoiNhapKyThuat-AI-BE/.claude/docs/Data_Architecture.md`
- `../BaiTap2-HoiNhapKyThuat-AI-BE/.claude/docs/Frontend_API_Guide.md`
- `../BaiTap2-HoiNhapKyThuat-AI-BE/.claude/docs/modules/Auth_Module.md`
- `../BaiTap2-HoiNhapKyThuat-AI-BE/.claude/docs/modules/Organization_Module.md`
- `../BaiTap2-HoiNhapKyThuat-AI-BE/.claude/docs/modules/Vocabulary_Module.md`
- `../BaiTap2-HoiNhapKyThuat-AI-BE/.claude/docs/modules/Testing_Module.md`
- `../BaiTap2-HoiNhapKyThuat-AI-BE/postman/BaiTap2-HoiNhapKyThuat-AI.postman_collection.json`
- `Html-template/login_page/code.html`
- `Html-template/register_page/code.html`
- `Html-template/vocabLib_folder/DESIGN.md`
- `Html-template/Test_createTestAndFlashcard/DESIGN.md`

## 4. Hiện trạng Frontend

- Project đang là Vite React TypeScript starter.
- `src/App.tsx` đang hiển thị nội dung demo Vite/React và counter.
- `src/App.css` và `src/index.css` đang dùng style starter, chưa theo design system Vocab Library.
- Chưa có cấu trúc `api`, `types`, `components`, `layouts`, `pages`, `hooks`, `services` hoặc `utils`.
- Chưa có routing library trong dependency; `package.json` chỉ có React, React DOM, TypeScript, Vite và ESLint.
- Package manager: `npm` theo `package-lock.json`.

## 5. Phạm vi triển khai được đề xuất

### 5.1. Cấu trúc thư mục

Tạo cấu trúc nền tảng:

- `src/api`: API client, endpoint helpers, error handling.
- `src/types`: type contract dùng chung cho Auth, Organization, Vocabulary, Testing, Flashcard.
- `src/services`: service function theo module, gọi API client.
- `src/components`: UI component dùng chung.
- `src/layouts`: app shell, auth layout.
- `src/pages`: page placeholder theo module.
- `src/hooks`: custom hooks nền tảng.
- `src/utils`: helper thuần.
- `src/styles`: design tokens/global styles nếu tách khỏi `index.css`.

### 5.2. API foundation

Tạo HTTP client dùng `fetch` native để tránh thêm dependency mới:

- Base URL mặc định: `http://localhost:8081`.
- Đọc response wrapper theo `response.data.data`.
- Gắn `Authorization: Bearer <access_token>` cho protected request khi có token.
- Hỗ trợ JSON request/response.
- Hỗ trợ `FormData` cho bulk import.
- Hỗ trợ binary/audio URL bằng helper tạo full URL từ `audioUrl`.
- Chuẩn hóa lỗi API theo `message` từ error wrapper khi có.
- Không đọc refresh-token cookie HTTP-only bằng JavaScript.

### 5.3. Auth foundation

Tạo nền tảng session phía FE:

- Lưu access token ở client state và localStorage.
- Cung cấp helpers login, register, account, refresh, logout theo API guide.
- App shell phân biệt trạng thái authenticated/unauthenticated ở mức placeholder.
- Chưa thêm authorization model mới ngoài Bearer token đã được contract quy định.

### 5.4. Type foundation

Định nghĩa các type theo `Frontend_API_Guide.md`:

- `ApiResponse<T>`, `ApiError`.
- `User`, login/register request/response.
- `Item`, `ItemType`.
- `Vocab`.
- `Test`, `Question`, `Option`, `TestAnswer`.
- `Flashcard`.
- Request/response DTO chính cho create/finish/bulk operations.

Không tự thêm field ngoài contract. Với điểm tài liệu có tên field khác nhau như `audioUrl` và `audio_url`, type sẽ bám theo từng response shape được tài liệu mô tả và ghi chú rõ ở code/doc nếu cần.

### 5.5. UI foundation

Thay màn hình Vite starter bằng app shell tối thiểu theo Vocab Library design:

- Brand/header nền tảng.
- Sidebar navigation placeholder cho Vocabulary Library, Tests, Flashcards.
- Main workspace placeholder.
- Auth area placeholder hoặc link trạng thái đăng nhập.
- Design tokens từ `Html-template/vocabLib_folder/DESIGN.md`: màu primary blue, surface, border, typography, radius 8px.
- Không triển khai chi tiết login/register/vocab/test flows trong foundation nếu chưa được duyệt ở plan module riêng.

### 5.6. Quality foundation

- Giữ TypeScript strict theo cấu hình hiện tại.
- Không dùng `any`, `@ts-ignore`, `@ts-nocheck`.
- Không thêm dependency mới.
- Không chỉnh sửa Backend, `Html-template` hoặc Postman collection.
- Chạy kiểm tra sau khi triển khai:
  - `npm run lint`
  - `npm run build`

Project hiện chưa khai báo script test, format hoặc type-check riêng; `npm run build` đã chạy `tsc -b`.

## 6. API liên quan

Foundation sẽ tạo service/hàm gọi cho các endpoint sau, theo contract trong `Frontend_API_Guide.md`:

- Auth:
  - `POST /api/v1/auth/register`
  - `POST /api/v1/auth/login`
  - `GET /api/v1/auth/account`
  - `GET /api/v1/auth/refresh`
  - `POST /api/v1/auth/logout`
- Organization:
  - `POST /api/v1/folders`
  - `POST /api/v1/vocab-sets`
  - `GET /api/v1/items/children`
  - `GET /api/v1/items/search`
  - `GET /api/v1/items/by-path`
  - `POST /api/v1/vocab-sets/{vocabSetId}/vocabs/{vocabId}`
  - `POST /api/v1/vocab-sets/{vocabSetId}/vocabs/bulk`
- Vocabulary:
  - `POST /api/v1/vocabs`
  - `POST /api/v1/vocabs/bulk`
  - `GET /api/v1/vocabs/lookup`
  - `PATCH /api/v1/vocabs/lookup`
  - `GET /api/v1/vocabs/audio/{fileName}`
- Testing:
  - `POST /api/v1/tests`
  - `GET /api/v1/tests/{testId}`
  - `POST /api/v1/tests/{testId}/finish`
  - `GET /api/v1/tests/{testId}/result`
- Flashcard:
  - `POST /api/v1/flashcards`

## 7. File dự kiến thay đổi khi triển khai

| File | Loại thay đổi | Mục đích |
|---|---|---|
| `src/App.tsx` | Chỉnh sửa | Thay Vite starter bằng app shell nền tảng |
| `src/App.css` | Chỉnh sửa | Style app shell hoặc giảm phụ thuộc style starter |
| `src/index.css` | Chỉnh sửa | Global reset, design tokens, typography nền tảng |
| `src/api/client.ts` | Tạo mới | HTTP client, response wrapper, auth header, error handling |
| `src/api/config.ts` | Tạo mới | Base URL và helper tạo URL |
| `src/api/errors.ts` | Tạo mới | Type/utility lỗi API |
| `src/types/api.ts` | Tạo mới | Generic API wrapper và common types |
| `src/types/auth.ts` | Tạo mới | Auth DTO/type |
| `src/types/organization.ts` | Tạo mới | Item/folder/vocab set type |
| `src/types/vocabulary.ts` | Tạo mới | Vocab DTO/type |
| `src/types/testing.ts` | Tạo mới | Test/question/option/result DTO/type |
| `src/types/flashcard.ts` | Tạo mới | Flashcard DTO/type |
| `src/services/authService.ts` | Tạo mới | Auth API functions |
| `src/services/organizationService.ts` | Tạo mới | Organization API functions |
| `src/services/vocabularyService.ts` | Tạo mới | Vocabulary API functions |
| `src/services/testingService.ts` | Tạo mới | Testing API functions |
| `src/services/flashcardService.ts` | Tạo mới | Flashcard API functions |
| `src/components/BrandMark.tsx` | Tạo mới | Logo/brand dùng chung |
| `src/components/StatusMessage.tsx` | Tạo mới | Hiển thị loading/error/empty/success đơn giản |
| `src/layouts/AppLayout.tsx` | Tạo mới | Sidebar/header/workspace layout |
| `src/layouts/AuthLayout.tsx` | Tạo mới | Layout nền cho login/register sau này |
| `src/pages/DashboardPage.tsx` | Tạo mới | Placeholder workspace sau đăng nhập |
| `src/pages/LoginPage.tsx` | Tạo mới | Placeholder form auth tối thiểu nếu cần app flow |
| `src/pages/RegisterPage.tsx` | Tạo mới | Placeholder form register tối thiểu nếu cần app flow |
| `src/hooks/useAuthSession.ts` | Tạo mới | Quản lý access token và current user phía FE |
| `src/utils/storage.ts` | Tạo mới | Helper localStorage an toàn |

## 8. Vị trí thay đổi dự kiến

| File | Component/module | Method/khu vực | Nội dung thay đổi |
|---|---|---|---|
| `src/App.tsx` | `App` | Render root | Kết nối auth session và layout/page placeholder |
| `src/index.css` | Global CSS | `:root`, `body`, base elements | Thiết lập tokens và reset nền tảng |
| `src/App.css` | App CSS | Layout classes | Style shell/sidebar/header/workspace |
| `src/api/client.ts` | API client | `apiRequest`, helpers | Chuẩn hóa request, response wrapper, lỗi và auth header |
| `src/services/*` | Service modules | Public service functions | Mapping endpoint đúng contract |
| `src/types/*` | Type modules | Interfaces/types | Định nghĩa DTO theo tài liệu |
| `src/hooks/useAuthSession.ts` | Hook | State/actions | Quản lý token/current user/loading/error |
| `src/layouts/*` | Layout components | JSX/layout | Khung giao diện dùng chung |

## 9. Output dự kiến

- Project không còn hiển thị màn hình Vite starter.
- Có app shell Vocab Library tối thiểu, responsive cơ bản, theo design token đã đọc.
- Có nền API client/service/type để các module sau gọi lại.
- Có auth session foundation, không đọc HTTP-only refresh token.
- Build và lint chạy được nếu không có lỗi ngoài phạm vi.

## 10. Rủi ro và giới hạn

- Không có `react-router-dom`; foundation sẽ không thêm dependency mới. Nếu cần routing thực sự, phải có phê duyệt riêng để thêm dependency hoặc dùng state-based navigation tạm thời.
- Login/register template có dùng CDN Tailwind, Font Awesome và ảnh remote; foundation sẽ không thêm các dependency/CDN đó vào React app nếu chưa được phê duyệt.
- Tài liệu có xuất hiện cả `audioUrl` và `audio_url` trong các response ví dụ khác nhau. `Frontend_API_Guide.md` là nguồn chính; nếu khi code phát hiện mâu thuẫn cụ thể theo DTO/service, sẽ báo lại trước khi tự chọn.
- Foundation chỉ tạo khung và placeholder, chưa hoàn thiện nghiệp vụ chi tiết của từng module.

## 11. Tiêu chí kiểm tra sau triển khai

- `npm run lint`
- `npm run build`

Không có script `test`, `format` hoặc `type-check` riêng trong `package.json` hiện tại.

## 12. Kết quả triển khai

- Đã thay Vite starter bằng app shell Vocab Library tối thiểu.
- Đã tạo cấu trúc nền `api`, `types`, `services`, `components`, `layouts`, `pages`, `hooks`, `utils`.
- Đã thêm API client dùng `fetch` native, đọc response wrapper, gắn Bearer token, hỗ trợ `FormData` và chuẩn hóa lỗi API.
- Đã thêm service/type foundation cho Auth, Organization, Vocabulary, Testing và Flashcard theo `Frontend_API_Guide.md`.
- Đã thêm auth session foundation với access token trong localStorage và không đọc HTTP-only refresh-token cookie.
- Đã chạy `npm run lint`: Pass.
- Đã chạy `npm run build`: Pass, bao gồm `tsc -b` và `vite build`.
- Số vòng lặp code-debug: 1.

## 13. Trạng thái phê duyệt

- Trạng thái hiện tại: Hoàn thành theo phạm vi đã được phê duyệt.
