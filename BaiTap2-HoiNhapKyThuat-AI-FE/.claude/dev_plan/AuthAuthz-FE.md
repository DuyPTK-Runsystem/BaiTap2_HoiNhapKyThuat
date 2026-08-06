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

## 15. Follow-up Auth Fix - Register Không Tự Động Login

- Ngày lập follow-up: 2026-08-06
- Trạng thái: Hoàn thành
- Phạm vi: Chỉ Frontend, không thay đổi Backend, API contract, Postman collection hoặc HTML template.

### 15.1. Vấn đề hiện tại

- `src/hooks/useAuthSession.ts` thực hiện `register(credentials)` rồi tiếp tục gọi `login(credentials)` trong cùng `signUp` flow.
- Hành vi này khiến đăng ký thành công tự động tạo session, chuyển người dùng vào vùng protected thay vì yêu cầu đăng nhập theo flow tách biệt.
- `Frontend_API_Guide.md` mô tả Register và Login là hai API riêng; response Register chỉ trả thông tin user, không trả access token.

### 15.2. Mục tiêu sửa chữa

- Sau khi register thành công, không gọi API Login tự động.
- Giữ người dùng ở guest state và hiển thị thông báo đăng ký thành công.
- Cho phép người dùng chuyển sang `/login` bằng action hiện có, sau đó tự nhập thông tin để đăng nhập.
- Giữ nguyên request/response contract và không lưu token sau Register.
- Bổ sung loading state rõ ràng cho quá trình đăng ký và trạng thái kiểm tra session ban đầu.

### 15.3. Tài liệu và tài nguyên đã đối chiếu

- `.claude/rules/CLAUDE.md` và `../BaiTap2-HoiNhapKyThuat-AI-BE/.claude/rules/CLAUDE.md`.
- `.claude/workflows/WORKFLOW.md` và `.claude/skills/SKILL.md`.
- `../BaiTap2-HoiNhapKyThuat-AI-BE/.claude/docs/ApplicationContext.md`.
- `../BaiTap2-HoiNhapKyThuat-AI-BE/.claude/docs/Frontend_API_Guide.md`, mục 2.2, 2.3, 3.1, 3.2 và 8.1.
- `../BaiTap2-HoiNhapKyThuat-AI-BE/.claude/docs/modules/Auth_Module.md`.
- `../BaiTap2-HoiNhapKyThuat-AI-BE/postman/BaiTap2-HoiNhapKyThuat-AI.postman_collection.json`, các request Register và Login.
- `Html-template/register_page/code.html` và `Html-template/login_page/code.html`.
- Source hiện tại: `src/hooks/useAuthSession.ts`, `src/pages/RegisterPage.tsx`, `src/pages/LoginPage.tsx`, `src/components/AuthGate.tsx` và `src/App.tsx`.

### 15.4. Phạm vi triển khai

- Sửa `signUp` để chỉ gọi `register`, sau đó đặt state về guest với success message và không gọi `setSession`.
- Giữ duplicate submission guard thông qua `submitting`.
- Bổ sung hoặc chuẩn hóa loading state cho Register:
  - Nút submit hiển thị trạng thái đang đăng ký.
  - Khóa các input và action chuyển trang trong lúc request đang chạy.
  - Không cho phép gửi lại form khi đang loading.
- Giữ loading state kiểm tra session ban đầu trong `AuthGate` khi status là `checking`.
- Đảm bảo sau khi register thành công, success message được hiển thị trong guest view và không bị biến thành authenticated state.
- Kiểm tra lại Login flow độc lập: chỉ Login mới lưu access token và chuyển vào protected route.

### 15.5. Phạm vi không thực hiện

- Không gọi thêm API mới.
- Không thay đổi endpoint, HTTP method, request field hoặc response field.
- Không thay đổi Backend, Postman collection, HTML template, dependency, route map hoặc authorization model.
- Không tự động điền hoặc gửi lại password từ Register sang Login.

### 15.6. File dự kiến thay đổi

| File | Loại thay đổi | Vị trí | Nội dung |
|---|---|---|---|
| `src/hooks/useAuthSession.ts` | Chỉnh sửa | `signUp` và state transition | Bỏ lời gọi Login sau Register; trả về guest state và success message; giữ loading/duplicate guard |
| `src/pages/RegisterPage.tsx` | Chỉnh sửa nếu cần | Form submit và hiển thị state | Bảo đảm loading, disabled, success/error state đúng sau Register |
| `src/pages/LoginPage.tsx` | Chỉnh sửa nếu cần | Form state | Bảo đảm Login vẫn là flow riêng và loading state không bị ảnh hưởng |
| `src/App.tsx` | Chỉnh sửa nếu cần | `RegisterRoute`/`LoginRoute` | Giữ điều hướng guest giữa Register và Login sau success |
| `.claude/dev_plan/AuthAuthz-FE.md` | Đã cập nhật | Follow-up section | Ghi nhận nguyên nhân, phạm vi, kế hoạch và tiêu chí hoàn thành |
| `.claude/dev_plan/DevPlanSummary-FE.md` | Đã cập nhật | Auth & Authz row | Đánh dấu follow-up đã hoàn thành |

### 15.7. API liên quan

| Method | Endpoint | Vai trò trong flow |
|---|---|---|
| `POST` | `/api/v1/auth/register` | Tạo tài khoản; đây là API duy nhất được gọi khi submit Register |
| `POST` | `/api/v1/auth/login` | Chỉ được gọi sau khi người dùng chủ động submit Login; không gọi từ Register flow |

### 15.8. Data flow và trạng thái dự kiến

1. Người dùng submit Register.
2. `submitting = true`; form và action chuyển Login bị disabled, nút hiển thị loading.
3. FE gọi `POST /api/v1/auth/register` một lần.
4. Khi thành công: không lưu token, không gọi Login, đặt trạng thái `guest`, tắt loading và hiển thị success message.
5. Người dùng chọn `Đăng nhập`, chuyển đến `/login` và chủ động submit Login.
6. Khi Login thành công: mới lưu access token, đặt authenticated state và chuyển protected route.
7. Nếu Register thất bại: tắt loading, giữ guest state và hiển thị `ApiError.message` nếu có.

### 15.9. Tiêu chí nghiệm thu

- Network trace của Register chỉ có request `POST /api/v1/auth/register`; không có request Login phát sinh tự động.
- Register thành công không tạo access token/session và không redirect vào `/library`.
- Success message hiển thị rõ ràng sau Register.
- Trong thời gian Register request: nút, input và link chuyển Login bị disabled; không thể duplicate submission.
- Khi khởi động với token cũ, `AuthGate` vẫn hiển thị loading state trong lúc kiểm tra account/refresh.
- Login chủ động vẫn hoạt động, có loading/disabled/error state và tạo session đúng contract.
- Register failure không làm mất session hợp lệ hiện có ngoài phạm vi flow guest hiện tại.

### 15.10. Kiểm tra dự kiến sau implementation

- `npm run lint`.
- `npm run build` (bao gồm `tsc -b` và production build).
- Kiểm tra thủ công hoặc test phù hợp cho các case: Register success không Login, Register error, duplicate submit, loading state và Login success.
- Không có script `test`, `format` hoặc `type-check` riêng trong `package.json` hiện tại.

### 15.11. Rủi ro và lưu ý

- Nếu Backend tự động tạo cookie sau Register, FE vẫn không đọc hoặc dùng cookie đó; hành vi FE vẫn phải chờ Login chủ động theo contract.
- Success message có thể bị xóa khi người dùng chuyển route; đây là hành vi chấp nhận được nếu không có yêu cầu giữ message xuyên route.
- Người dùng đã phê duyệt follow-up plan trong phiên làm việc hiện tại; triển khai phải giữ đúng phạm vi đã mô tả.

### 15.12. Kết quả triển khai

- Đã sửa `src/hooks/useAuthSession.ts` để `signUp` chỉ gọi `register(credentials)`.
- Sau Register thành công, FE xóa access token client nếu có, giữ `status = 'guest'`, không lưu session và hiển thị thông báo `Đăng ký thành công. Vui lòng đăng nhập để tiếp tục.`.
- Đã giữ nguyên Login flow độc lập: chỉ `signIn` mới gọi `login(credentials)` và lưu access token.
- Không thay đổi Backend, API contract, Postman collection, HTML template, dependency, route map hoặc authorization model.
- `npm run lint`: không chạy được qua PowerShell wrapper do execution policy chặn `npm.ps1`.
- `npm.cmd run lint`: Pass.
- `npm.cmd run build`: Pass, bao gồm `tsc -b` và `vite build`.
- Không có script `test`, `format` hoặc `type-check` riêng trong `package.json`; `build` đã bao gồm type-check.
- Số vòng lặp code-debug: 1.

## 16. Follow-up Auth UX - Register Success Message 3s Và Redirect Login

- Ngày lập follow-up: 2026-08-06
- Trạng thái: Hoàn thành
- Phạm vi: Chỉ Frontend, không thay đổi Backend, API contract, Postman collection, HTML template, dependency hoặc authorization model.
- Ghi chú thay đổi so với section 15: section 15 giữ người dùng ở Register và cho phép tự bấm `Đăng nhập`; follow-up này cập nhật theo yêu cầu mới là tự redirect về `/login` sau khi hiển thị thông báo thành công trong 3 giây.

### 16.1. Yêu cầu mới

- Khi người dùng đăng ký thành công, hiển thị thông báo dạng message hoặc popup trong 3 giây rồi tự tắt.
- Sau khi đăng ký thành công, tự động redirect về trang Login.
- Không tự động gọi API Login sau Register.

### 16.2. Tài liệu và tài nguyên đã đối chiếu

- `.claude/rules/CLAUDE.md` và `../BaiTap2-HoiNhapKyThuat-AI-BE/.claude/rules/CLAUDE.md`.
- `.claude/workflows/WORKFLOW.md` và `.claude/skills/SKILL.md`.
- `../BaiTap2-HoiNhapKyThuat-AI-BE/.claude/docs/ApplicationContext.md`, mục `User Model`.
- `../BaiTap2-HoiNhapKyThuat-AI-BE/.claude/docs/Frontend_API_Guide.md`, mục 2.2, 2.3, 3.1, 3.2 và 8.1.
- `../BaiTap2-HoiNhapKyThuat-AI-BE/.claude/docs/modules/Auth_Module.md`.
- `../BaiTap2-HoiNhapKyThuat-AI-BE/postman/BaiTap2-HoiNhapKyThuat-AI.postman_collection.json`, Auth requests Register và Login.
- `Html-template/register_page/code.html` và `Html-template/login_page/code.html`.
- Source hiện tại: `src/hooks/useAuthSession.ts`, `src/pages/RegisterPage.tsx`, `src/pages/LoginPage.tsx`, `src/App.tsx`, `src/components/AuthGate.tsx`, `src/App.css`.

### 16.3. Kiến trúc hiện tại liên quan

- `src/hooks/useAuthSession.ts` đang sở hữu `successMessage`, `errorMessage`, `submitting`, `status`, `accessToken` và các action `signIn`, `signUp`, `signOut`.
- `signUp` hiện đã chỉ gọi `POST /api/v1/auth/register`, không gọi Login tự động và không lưu token.
- `src/App.tsx` có `RegisterRoute` dùng `useNavigate()` và render `RegisterPage`.
- `src/pages/RegisterPage.tsx` đã render `successMessage` bằng `.form-success`.
- CSS `.form-success` hiện là inline message nằm trong form, phù hợp lựa chọn `message hiển thị trong 3s` mà không cần thêm dependency hoặc popup component mới.

### 16.4. Phạm vi triển khai

- Dùng inline success message hiện có (`.form-success`) làm thông báo sau Register thay vì tạo popup mới.
- Bổ sung action xóa success message trong `useAuthSession`, ví dụ `clearSuccess()`, để thông báo tự tắt đúng sau 3 giây.
- Trong `RegisterRoute`, khi `session.successMessage` xuất hiện:
  - tạo timer 3 giây;
  - sau timer, xóa success message;
  - redirect về `/login` bằng `navigate('/login', { replace: true })`.
- Cleanup timer khi component unmount hoặc success message thay đổi để tránh stale timeout.
- Giữ Register form disabled/loading behavior hiện tại trong lúc request đang chạy.
- Giữ Login flow độc lập: Login chỉ xảy ra khi người dùng submit form Login.

### 16.5. Phạm vi không thực hiện

- Không gọi thêm API mới.
- Không đổi endpoint, method, request field hoặc response field.
- Không thay đổi Backend, Postman collection, HTML template, dependency, route map hoặc authorization model.
- Không tự động điền email/password từ Register sang Login.
- Không hiển thị token hoặc dữ liệu nhạy cảm.

### 16.6. File dự kiến thay đổi

| File | Loại thay đổi | Vị trí | Nội dung |
|---|---|---|---|
| `src/hooks/useAuthSession.ts` | Chỉnh sửa | `AuthSession` interface và state action | Bổ sung action xóa success message, giữ `signUp` không gọi Login |
| `src/App.tsx` | Chỉnh sửa | `RegisterRoute` | Thêm `useEffect` timer 3 giây để clear message và redirect `/login` |
| `src/pages/RegisterPage.tsx` | Chỉnh sửa nếu cần | Success message render | Giữ hoặc tinh chỉnh message inline, không đổi validation/API |
| `.claude/dev_plan/AuthAuthz-FE.md` | Chỉnh sửa | Section 16 | Ghi nhận plan, kết quả sau triển khai |
| `.claude/dev_plan/DevPlanSummary-FE.md` | Chỉnh sửa | Auth & Authz row | Cập nhật trạng thái follow-up |

### 16.7. API liên quan

| Method | Endpoint | Vai trò |
|---|---|---|
| `POST` | `/api/v1/auth/register` | API duy nhất được gọi khi submit Register |
| `POST` | `/api/v1/auth/login` | Không gọi từ Register; chỉ gọi khi người dùng submit Login |

### 16.8. Data flow dự kiến

1. Người dùng submit Register.
2. FE khóa form bằng `submitting` và gọi `POST /api/v1/auth/register`.
3. Register thành công: FE giữ guest state, không lưu token, đặt `successMessage`.
4. `RegisterPage` hiển thị inline success message.
5. `RegisterRoute` đếm 3 giây, sau đó clear success message và redirect `/login`.
6. Login page được hiển thị; người dùng tự submit Login nếu muốn đăng nhập.
7. Register thất bại: không redirect, hiển thị `errorMessage` từ API nếu có.

### 16.9. Loading, empty, error, disabled và success state

- Loading: giữ `Đang đăng ký` trên submit button.
- Empty: không áp dụng.
- Error: lỗi Register vẫn hiển thị trên Register page và không auto redirect.
- Disabled: input, submit button và link chuyển Login bị disabled khi `submitting`.
- Success: inline message hiển thị tối đa 3 giây, sau đó tự tắt và redirect.

### 16.10. Tiêu chí nghiệm thu

- Register success chỉ gọi `POST /api/v1/auth/register`, không gọi `POST /api/v1/auth/login`.
- Success message hiển thị trong khoảng 3 giây.
- Sau khoảng 3 giây, app tự redirect về `/login`.
- Success message không còn hiển thị dai dẳng sau redirect.
- Register error không tự redirect và vẫn hiển thị lỗi.
- Timer được cleanup khi route/component unmount.
- Login flow hiện tại không bị ảnh hưởng.

### 16.11. Kiểm tra dự kiến sau implementation

- `npm.cmd run lint`.
- `npm.cmd run build` (bao gồm `tsc -b` và production build).
- Ghi nhận rõ `npm run lint` nếu còn bị PowerShell execution policy chặn `npm.ps1`.
- Không có script `test`, `format` hoặc `type-check` riêng trong `package.json`.

### 16.12. Rủi ro và lưu ý

- Nếu người dùng rời Register page trước khi hết 3 giây, timer phải cleanup và không redirect ngoài ý muốn.
- Nếu Backend tự set cookie sau Register, FE vẫn không đọc cookie đó và vẫn không tạo authenticated session.
- Người dùng đã phê duyệt follow-up plan trong phiên làm việc hiện tại; triển khai phải giữ đúng phạm vi đã mô tả.

### 16.13. Kết quả triển khai

- Đã bổ sung `clearSuccess()` trong `useAuthSession` để xóa success message theo timer.
- Đã cập nhật `signUp` success text thành `Đăng ký thành công. Đang chuyển về trang đăng nhập.`.
- Đã thêm timer 3 giây trong `RegisterRoute`; sau timer, FE clear success message và redirect `/login` bằng `replace`.
- Đã cleanup timer khi `RegisterRoute` unmount hoặc message thay đổi.
- Đã clear success message khi người dùng chuyển thủ công giữa Login/Register để tránh message cũ hiển thị sai route.
- Đã thêm `role="status"` cho success message trong `RegisterPage`.
- Không gọi Login tự động sau Register; Login flow vẫn chỉ chạy khi người dùng submit Login.
- Không thay đổi Backend, API contract, Postman collection, HTML template, dependency, route map hoặc authorization model.
- `npm.cmd run lint`: Pass, 0 lỗi, 0 warning sau khi sửa dependency warning của `useEffect`.
- `npm.cmd run build`: Pass, bao gồm `tsc -b` và `vite build`.
- Không có script `test`, `format` hoặc `type-check` riêng trong `package.json`; `build` đã bao gồm type-check.
- Số vòng lặp code-debug: 1.
