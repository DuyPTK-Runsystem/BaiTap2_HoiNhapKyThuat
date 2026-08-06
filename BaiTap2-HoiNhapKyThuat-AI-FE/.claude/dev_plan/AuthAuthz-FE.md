# Development Plan FE - Auth & Authz

## 1. Thông tin chung

- Module/feature: Auth & Authz Frontend
- Repository thực hiện: `BaiTap2-HoiNhapKyThuat-AI-FE`
- Ngày lập plan: 2026-08-06
- Trạng thái: Hoàn thành
- Phụ thuộc: `ProjectFoundation-FE.md` đã hoàn thành

## 2. Mục tiêu

Hoàn thiện luồng xác thực và bảo vệ vùng ứng dụng Frontend theo contract Backend hiện tại:

- Đăng nhập bằng email/password.
- Đăng ký tài khoản bằng email/password.
- Lấy current user bằng access token.
- Làm mới phiên bằng refresh-token cookie HTTP-only thông qua API refresh.
- Đăng xuất và xóa session phía client.
- Bảo vệ màn hình app shell, chỉ cho `Logged-in User` truy cập.

Không thêm role/permission/authorization model mới vì tài liệu dự án chỉ quy định một actor là `Logged-in User`.

## 3. Tài liệu và tài nguyên đã đối chiếu

- `.claude/rules/CLAUDE.md`
- `.claude/workflows/WORKFLOW.md`
- `.claude/skills/SKILL.md`
- `../BaiTap2-HoiNhapKyThuat-AI-BE/.claude/rules/CLAUDE.md`
- `../BaiTap2-HoiNhapKyThuat-AI-BE/.claude/docs/ApplicationContext.md`
  - Mục 2: `User Model`, hệ thống chỉ có `Logged-in User`.
  - Mục 4: Auth Module quản lý người dùng và phiên làm việc.
- `../BaiTap2-HoiNhapKyThuat-AI-BE/.claude/docs/modules/Auth_Module.md`
  - User Registration.
  - User Login.
  - Email unique, password hash là rule Backend.
- `../BaiTap2-HoiNhapKyThuat-AI-BE/.claude/docs/Frontend_API_Guide.md`
  - Mục 2.2: response wrapper.
  - Mục 2.3: Bearer token, access token, refresh-token cookie HTTP-only.
  - Mục 3.1-3.5: register/login/account/refresh/logout APIs.
  - Mục 8.1: recommended initial auth flow.
  - Mục 9: error handling.
- `../BaiTap2-HoiNhapKyThuat-AI-BE/postman/BaiTap2-HoiNhapKyThuat-AI.postman_collection.json`
  - Auth requests: Register, Login, Account, Refresh Token.
  - Cleanup request: Logout.
- `Html-template/login_page/code.html`
- `Html-template/register_page/code.html`

## 4. Hiện trạng Frontend

- Foundation đã có:
  - `src/api/client.ts`: API request wrapper dùng `fetch`, Bearer token theo options, `credentials: include`.
  - `src/services/authService.ts`: register/login/account/refresh/logout services.
  - `src/hooks/useAuthSession.ts`: state session tối thiểu, localStorage access token, login/register/logout/refresh helpers.
  - `src/pages/LoginPage.tsx`: login form tối thiểu.
  - `src/pages/RegisterPage.tsx`: register form tối thiểu nhưng chưa nối vào `App`.
  - `src/layouts/AuthLayout.tsx`: auth layout tối thiểu.
  - `src/App.tsx`: nếu chưa authenticated thì hiển thị login, nếu authenticated thì hiển thị app shell.
- Chưa có:
  - Chuyển đổi Login/Register trong UI.
  - Trạng thái kiểm tra session ban đầu rõ ràng trước khi hiện login.
  - Refresh access token khi `GET /account` gặp `401`.
  - Authz boundary component tách riêng cho protected/guest view.
  - Prevent duplicate submission ở login/register/logout.
  - Validate confirm password đầy đủ hơn ngoài mismatch sau khi nhập.
  - Success state sau register.

## 5. Phạm vi triển khai được đề xuất

### 5.1. Auth UI

- Hoàn thiện `LoginPage` theo template login:
  - Email/password inputs.
  - Loading, error, disabled state.
  - Link chuyển sang Register.
  - Không hiển thị token hoặc dữ liệu nhạy cảm.
- Hoàn thiện `RegisterPage` theo template register:
  - Email/password/confirm password inputs.
  - Kiểm tra client-side tối thiểu: required, password length >= 8, có chữ cái, có số, confirm password khớp.
  - Loading, error, disabled, success state.
  - Link chuyển sang Login.
- Không thêm Tailwind CDN, Font Awesome CDN hoặc ảnh remote từ template.

### 5.2. Auth session

- Cập nhật `useAuthSession`:
  - Phân biệt `checking`, `authenticated`, `guest`.
  - Khi app khởi động có access token, gọi `GET /api/v1/auth/account`.
  - Nếu account trả `401`, gọi `GET /api/v1/auth/refresh` để lấy access token mới từ refresh cookie HTTP-only.
  - Nếu refresh thành công, lưu access token mới và user.
  - Nếu refresh thất bại, xóa access token client.
  - Không đọc refresh-token cookie bằng JavaScript.
- Đảm bảo login/register/logout không submit trùng khi đang loading.

### 5.3. Authz boundary

- Tạo component boundary ở FE:
  - `ProtectedRoute` hoặc `AuthGate` để chỉ render `AppLayout` khi authenticated.
  - `GuestGate` hoặc auth view switch để render login/register khi chưa authenticated.
- Vì chưa có `react-router-dom`, authorization sẽ được triển khai bằng state-based view guard trong React, không thêm route dependency.
- Authz chỉ kiểm tra trạng thái `Logged-in User`; không thêm role/permission.

### 5.4. API client behavior

- Giữ API contract hiện tại.
- Có thể bổ sung helper nhận `onUnauthorized` hoặc xử lý rõ lỗi `401` ở session hook.
- Không tự đổi endpoint/method/request/response field.
- Không hard-code credential/token.

## 6. API liên quan

- `POST /api/v1/auth/register`
  - Request: `{ "email": string, "password": string }`
  - Response data: `{ "id": number, "email": string }`
- `POST /api/v1/auth/login`
  - Request: `{ "email": string, "password": string }`
  - Response data: `{ "access_token": string, "user": { "id": number, "email": string } }`
- `GET /api/v1/auth/account`
  - Header: `Authorization: Bearer <access_token>`
  - Response data: current user.
- `GET /api/v1/auth/refresh`
  - Uses HTTP-only `refresh_token` cookie.
  - Response data: same shape as login.
- `POST /api/v1/auth/logout`
  - Clears refresh token server-side and cookie.

## 7. File dự kiến thay đổi khi triển khai

| File | Loại thay đổi | Mục đích |
|---|---|---|
| `src/App.tsx` | Chỉnh sửa | Kết nối auth view switch và authz boundary |
| `src/App.css` | Chỉnh sửa | Hoàn thiện style login/register/auth states theo template |
| `src/api/client.ts` | Chỉnh sửa | Bổ sung khả năng nhận/nhận diện lỗi unauthorized nếu cần |
| `src/hooks/useAuthSession.ts` | Chỉnh sửa | Hoàn thiện lifecycle session, refresh fallback, trạng thái auth |
| `src/pages/LoginPage.tsx` | Chỉnh sửa | Hoàn thiện login form, link register, duplicate submit guard |
| `src/pages/RegisterPage.tsx` | Chỉnh sửa | Hoàn thiện register form, validation, link login, success/error state |
| `src/layouts/AuthLayout.tsx` | Chỉnh sửa | Bổ sung slot phụ/link/auth card phù hợp |
| `src/components/AuthGate.tsx` | Tạo mới | Boundary bảo vệ app và guest view bằng authenticated state |
| `src/types/auth.ts` | Chỉnh sửa | Bổ sung type auth status nếu cần |

## 8. Vị trí thay đổi dự kiến

| File | Class/Component | Method/Khu vực | Nội dung thay đổi |
|---|---|---|---|
| `src/App.tsx` | `App` | Render root | Điều phối login/register/protected app |
| `src/hooks/useAuthSession.ts` | `useAuthSession` | init effect/actions | Account check, refresh fallback, signIn/signUp/signOut duplicate guard |
| `src/components/AuthGate.tsx` | `AuthGate` | Component render | Render loading/guest/protected theo trạng thái auth |
| `src/pages/LoginPage.tsx` | `LoginPage` | form state/submit | Gửi login, disabled/loading/error, switch register |
| `src/pages/RegisterPage.tsx` | `RegisterPage` | form state/submit | Validate password, gọi register, success/disabled/error, switch login |
| `src/layouts/AuthLayout.tsx` | `AuthLayout` | JSX layout | Hoàn thiện auth card/layout dùng chung |
| `src/App.css` | CSS auth section | `.auth-*`, `.form-*` | Style responsive, states, actions |
| `src/api/client.ts` | `apiRequest` | error handling | Giữ `ApiError.statusCode` để hook xử lý `401` |
| `src/types/auth.ts` | Auth types | type/interface | Thêm auth status/view type nếu cần |

## 9. Output dự kiến

- Người dùng chưa đăng nhập thấy Login page.
- Từ Login có thể chuyển sang Register và ngược lại.
- Register validate password client-side trước khi gọi API.
- Login/Register hiển thị loading, disabled, error và success phù hợp.
- Sau login/register thành công, app chuyển sang protected workspace.
- Khi reload app:
  - Nếu access token còn hợp lệ, app gọi account và vào workspace.
  - Nếu access token hết hạn nhưng refresh cookie còn hợp lệ, app gọi refresh và vào workspace.
  - Nếu không có session hợp lệ, app về Login.
- Logout gọi Backend logout rồi xóa session client.

## 10. Rủi ro và giới hạn

- Không có route dependency nên chưa có URL `/login` hoặc `/register`; view switch dùng React state.
- Authz chỉ ở mức `Logged-in User`, không có role/permission vì tài liệu không quy định.
- Access token lưu localStorage theo foundation hiện tại; rủi ro XSS vẫn là điểm cần chú ý chung của FE, nhưng không tự đổi sang cơ chế khác khi Backend contract trả access token cho FE.
- Không test API thật nếu Backend không chạy ở `http://localhost:8081`.
- Template dùng Tailwind/Font Awesome/ảnh remote; implementation sẽ dùng CSS/component hiện có, không thêm CDN/dependency.

## 11. Tiêu chí kiểm tra sau triển khai

- `npm run lint`
- `npm run build`

Không có script `test`, `format` hoặc `type-check` riêng trong `package.json`; `npm run build` bao gồm `tsc -b`.

## 12. Kết quả triển khai

- Đã thêm `AuthGate` để render trạng thái checking, guest hoặc protected content.
- Đã hoàn thiện `useAuthSession` với `checking/authenticated/guest`, duplicate submit guard, account check khi reload, refresh fallback khi account trả `401`, logout và clear session.
- Đã nối Login/Register view switch trong `App` bằng React state, không thêm routing dependency.
- Đã hoàn thiện Login UI với loading, disabled, error, success message và link chuyển Register.
- Đã hoàn thiện Register UI với password checklist, confirm password validation, loading, disabled, error, success message và link chuyển Login.
- Đã cập nhật `AuthLayout`, `AppLayout`, `App.css` và auth types phù hợp.
- Không thêm role/permission/authorization model mới.
- Không đọc refresh-token cookie HTTP-only bằng JavaScript.
- Đã chạy `npm run lint`: Pass.
- Đã chạy `npm run build`: Pass, bao gồm `tsc -b` và `vite build`.
- Số vòng lặp code-debug: 1.

## 13. Trạng thái phê duyệt

- Trạng thái hiện tại: Hoàn thành theo phạm vi đã được phê duyệt.

## 14. Follow-up UI Fix - Auth Form Label, Link And Logo

- Ngày lập follow-up: 2026-08-06
- Trạng thái: Hoàn thành

### 14.1. Yêu cầu

- Text link chuyển Login/Register phải nằm cùng dòng với text mô tả:
  - `Chưa có tài khoản? Đăng ký`
  - `Đã có tài khoản? Đăng nhập`
- Dấu `(*)` trong label field bắt buộc phải có màu đỏ.
- Phần text label còn lại vẫn màu đen.
- Đổi logo hiện tại từ CSS `A/Z` mark sang logo file người dùng cung cấp.

### 14.2. Phạm vi dự kiến

| File | Loại thay đổi | Mục đích |
|---|---|---|
| `src/pages/LoginPage.tsx` | Chỉnh sửa | Tách dấu `(*)` thành span riêng và giữ link cùng dòng |
| `src/pages/RegisterPage.tsx` | Chỉnh sửa | Tách dấu `(*)` thành span riêng và giữ link cùng dòng |
| `src/components/BrandMark.tsx` | Chỉnh sửa | Render logo image thay cho CSS text mark |
| `src/App.css` | Chỉnh sửa | Style `.required-mark` màu đỏ và chỉnh `.auth-switch` không xuống dòng ngoài ý muốn |
| `src/assets/<logo-file>` | Tạo mới | Lưu logo file người dùng cung cấp nếu chưa có trong workspace |

### 14.3. Output dự kiến

- Label hiển thị dạng `Mật khẩu (*)`, trong đó `Mật khẩu` màu đen, `(*)` màu đỏ.
- Link `Đăng ký`/`Đăng nhập` nằm cùng dòng với câu mô tả khi đủ chiều rộng như screenshot.
- Brand logo trong Auth layout và App shell dùng logo file mới.
- Không thay đổi API, auth flow, dependency hoặc Backend.

### 14.4. Điều kiện trước khi code

- Cần có logo file mới trong workspace hoặc người dùng xác nhận cho phép tạo asset từ file đã gửi trong cuộc trò chuyện.
- Nếu chưa xác định được đường dẫn asset, AI Agent phải hỏi lại thay vì tự chọn ảnh template.

### 14.5. Kiểm tra sau triển khai

- `npm run lint`
- `npm run build`

### 14.6. Kết quả triển khai

- Đã sử dụng `src/assets/logo.png` là logo file người dùng cung cấp.
- Đã cập nhật `BrandMark` để render logo image thay cho CSS `A/Z` mark.
- Đã tách dấu `(*)` trong Login/Register labels thành `.required-mark` màu đỏ.
- Đã giữ phần label text còn lại màu đen.
- Đã chỉnh auth switch để link `Đăng ký`/`Đăng nhập` nằm cùng dòng với text mô tả khi đủ chiều rộng.
- Đã chạy `npm run lint`: Pass.
- Đã chạy `npm run build`: Pass, bao gồm `tsc -b` và `vite build`.
- Số vòng lặp code-debug: 0.
