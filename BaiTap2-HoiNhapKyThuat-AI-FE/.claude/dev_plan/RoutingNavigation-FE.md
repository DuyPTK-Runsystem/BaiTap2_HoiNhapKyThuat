# Development Plan FE - Routing & Navigation

## 1. Thông tin chung

- Module/feature: Routing & Navigation Frontend
- Repository thực hiện: `BaiTap2-HoiNhapKyThuat-AI-FE`
- Ngày lập plan: 2026-08-06
- Trạng thái: Hoàn thành
- Phụ thuộc:
  - `ProjectFoundation-FE.md` đã hoàn thành
  - `AuthAuthz-FE.md` đã hoàn thành

## 2. Mục tiêu

Thiết lập route map chính thức cho Frontend theo danh sách người dùng cung cấp, thay state-based auth view hiện tại bằng URL-based navigation.

Route map dự kiến:

```text
/login
/register

/library
/library/search

/study/create
/tests/:testId
/tests/:testId/result
/tests/:testId/review

/flashcards/session
```

## 3. Tài liệu và tài nguyên đã đối chiếu

- `.claude/workflows/WORKFLOW.md`
  - Quy trình FE, Development Plan, đường dẫn FE/BE.
- `../BaiTap2-HoiNhapKyThuat-AI-BE/.claude/docs/Frontend_API_Guide.md`
  - Mục 2.3: auth, Bearer token, refresh cookie HTTP-only.
  - Mục 3: Auth APIs.
  - Mục 5: Organization APIs cho `/library`, `/library/search`.
  - Mục 6: Testing APIs cho `/study/create`, `/tests/:testId`, result.
  - Mục 7: Flashcard APIs cho `/flashcards/session`.
- `../BaiTap2-HoiNhapKyThuat-AI-BE/.claude/docs/modules/Organization_Module.md`
  - Search items, get children, item path, authenticated user ownership.
- `../BaiTap2-HoiNhapKyThuat-AI-BE/.claude/docs/modules/Testing_Module.md`
  - Test session, test taking, result/review, flashcard session.
- `Html-template/vocabLib_folder/DESIGN.md`
  - Sidebar/header/workspace layout, navigation tree.
- `Html-template/Test_createTestAndFlashcard/DESIGN.md`
  - Create test/flashcard entry flow.
- `Html-template/Test_taking/DESIGN.md`
  - Test taking layout.

## 4. Hiện trạng Frontend

- `package.json` chưa có routing dependency.
- `src/App.tsx` đang dùng React state `authView` để switch Login/Register.
- `AuthGate` đang nhận `guest` và `protectedContent` qua props, chưa điều phối theo URL.
- `AppLayout` có sidebar nav button tĩnh, chưa dùng route active state.
- Các page nghiệp vụ hiện mới có `DashboardPage`, `LoginPage`, `RegisterPage`; chưa có placeholder page tương ứng route map.

## 5. Phạm vi triển khai được đề xuất

### 5.1. Routing library

Đề xuất thêm dependency:

```text
react-router-dom
```

Lý do:

- Route map có dynamic segment `:testId`.
- Cần protected route/guest route rõ ràng.
- Cần điều hướng URL-based giữa login/register/library/study/test/flashcard.
- React Router là thư viện tiêu chuẩn cho React SPA, tránh tự viết router thủ công.

Vì đây là dependency mới, cần người dùng phê duyệt rõ ràng trước khi chạy `npm install react-router-dom`.

### 5.2. Route grouping

Nhóm public/guest routes:

- `/login`: Login page.
- `/register`: Register page.

Nhóm protected routes:

- `/library`: Vocabulary library main page.
- `/library/search`: Search page.
- `/study/create`: Create test/flashcard entry page.
- `/tests/:testId`: Test taking page.
- `/tests/:testId/result`: Test result page.
- `/tests/:testId/review`: Test review page.
- `/flashcards/session`: Flashcard session page.

Redirect behavior đề xuất:

- `/`:
  - Nếu authenticated: redirect `/library`.
  - Nếu guest: redirect `/login`.
- Guest truy cập protected route: redirect `/login`.
- Authenticated truy cập `/login` hoặc `/register`: redirect `/library`.
- Unknown route: hiển thị Not Found hoặc redirect `/library`/`/login` theo auth state.

### 5.3. Page placeholders

Tạo placeholder page đúng route để các phase module sau triển khai UI/API chi tiết:

- `LibraryPage`
- `LibrarySearchPage`
- `StudyCreatePage`
- `TestTakingPage`
- `TestResultPage`
- `TestReviewPage`
- `FlashcardSessionPage`
- `NotFoundPage`

Placeholder chỉ mô tả trạng thái màn hình và giữ đúng shell/design token hiện có, không tự triển khai nghiệp vụ hoặc API ngoài plan module riêng.

### 5.4. Navigation

- Cập nhật `AppLayout` sidebar dùng route-aware navigation:
  - Library -> `/library`
  - Study -> `/study/create`
  - Flashcards -> `/flashcards/session`
- Header/workspace hiển thị title theo route hiện tại nếu cần.
- Không tạo route ngoài route map người dùng đưa.

### 5.5. Auth/Authz integration

- Cập nhật `AuthGate` hoặc tạo route guard:
  - `ProtectedRoute`
  - `GuestRoute`
- Tái sử dụng `useAuthSession`.
- Không thay đổi auth API contract.
- Không thêm role/permission model.

## 6. API liên quan

Routing phase không gọi API mới trực tiếp ngoài auth/session hiện có, nhưng route map sẽ tương ứng với API module sau:

- `/login`, `/register`:
  - `POST /api/v1/auth/login`
  - `POST /api/v1/auth/register`
  - `GET /api/v1/auth/account`
  - `GET /api/v1/auth/refresh`
  - `POST /api/v1/auth/logout`
- `/library`, `/library/search`:
  - `GET /api/v1/items/children`
  - `GET /api/v1/items/search?name={keyword}`
- `/study/create`, `/tests/:testId`, `/tests/:testId/result`, `/tests/:testId/review`:
  - `POST /api/v1/tests`
  - `GET /api/v1/tests/{testId}`
  - `POST /api/v1/tests/{testId}/finish`
  - `GET /api/v1/tests/{testId}/result`
- `/flashcards/session`:
  - `POST /api/v1/flashcards`

## 7. File dự kiến thay đổi khi triển khai

| File | Loại thay đổi | Mục đích |
|---|---|---|
| `package.json` | Chỉnh sửa | Thêm `react-router-dom` nếu được phê duyệt |
| `package-lock.json` | Chỉnh sửa | Cập nhật lockfile sau install |
| `src/App.tsx` | Chỉnh sửa | Thay state switch bằng router tree |
| `src/components/AuthGate.tsx` | Chỉnh sửa/Tách mới | Tích hợp protected/guest route guard |
| `src/layouts/AppLayout.tsx` | Chỉnh sửa | Route-aware sidebar navigation |
| `src/pages/LoginPage.tsx` | Chỉnh sửa | Chuyển link register sang navigate `/register` |
| `src/pages/RegisterPage.tsx` | Chỉnh sửa | Chuyển link login sang navigate `/login` |
| `src/pages/LibraryPage.tsx` | Tạo mới | Placeholder `/library` |
| `src/pages/LibrarySearchPage.tsx` | Tạo mới | Placeholder `/library/search` |
| `src/pages/StudyCreatePage.tsx` | Tạo mới | Placeholder `/study/create` |
| `src/pages/TestTakingPage.tsx` | Tạo mới | Placeholder `/tests/:testId` |
| `src/pages/TestResultPage.tsx` | Tạo mới | Placeholder `/tests/:testId/result` |
| `src/pages/TestReviewPage.tsx` | Tạo mới | Placeholder `/tests/:testId/review` |
| `src/pages/FlashcardSessionPage.tsx` | Tạo mới | Placeholder `/flashcards/session` |
| `src/pages/NotFoundPage.tsx` | Tạo mới | Unknown route fallback |
| `src/App.css` | Chỉnh sửa | Style active nav/page placeholders nếu cần |
| `.claude/dev_plan/RoutingNavigation-FE.md` | Chỉnh sửa | Cập nhật kết quả sau triển khai |
| `.claude/dev_plan/DevPlanSummary-FE.md` | Chỉnh sửa | Cập nhật trạng thái plan |

## 8. Vị trí thay đổi dự kiến

| File | Component/module | Method/Khu vực | Nội dung thay đổi |
|---|---|---|---|
| `src/App.tsx` | `App` | Router tree | Khai báo route map, redirect, protected/guest boundaries |
| `src/components/AuthGate.tsx` | `AuthGate`/guards | Render logic | Route guard cho checking/guest/authenticated |
| `src/layouts/AppLayout.tsx` | `AppLayout` | Sidebar nav | Dùng link/nav state theo current route |
| `src/pages/LoginPage.tsx` | `LoginPage` | Switch action | Navigate `/register` |
| `src/pages/RegisterPage.tsx` | `RegisterPage` | Switch action | Navigate `/login` |
| `src/pages/*` | Placeholder pages | Component body | Render route placeholder theo module |

## 9. Output dự kiến

- App hỗ trợ đúng route map người dùng đưa.
- `/login` và `/register` là guest routes.
- Protected routes yêu cầu authenticated user.
- `/` redirect theo auth state.
- Sidebar navigation hoạt động theo URL và active state.
- Các route nghiệp vụ có placeholder sẵn để module sau triển khai chi tiết.

## 10. Rủi ro và giới hạn

- Cần network để cài `react-router-dom`; nếu sandbox chặn, phải xin quyền escalated cho `npm install react-router-dom`.
- Nếu không được phép thêm dependency, phương án fallback là custom state/history router tối thiểu, nhưng không khuyến nghị vì dynamic route `:testId` dễ phát sinh bug.
- Routing phase chỉ dựng route shell/placeholder, chưa triển khai nghiệp vụ Library/Test/Flashcard chi tiết.
- Không test API thật nếu Backend chưa chạy.

## 11. Tiêu chí kiểm tra sau triển khai

- `npm run lint`
- `npm run build`

Không có script `test`, `format` hoặc `type-check` riêng trong `package.json`; `npm run build` bao gồm `tsc -b`.

## 12. Kết quả triển khai

- Đã cài `react-router-dom`.
- Đã thay state-based auth view trong `App` bằng `BrowserRouter` và `Routes`.
- Đã triển khai route map:
  - `/login`
  - `/register`
  - `/library`
  - `/library/search`
  - `/study/create`
  - `/tests/:testId`
  - `/tests/:testId/result`
  - `/tests/:testId/review`
  - `/flashcards/session`
- Đã thêm redirect `/` theo auth state.
- Đã cập nhật `AuthGate` thành guest/protected route guard.
- Đã cập nhật `AppLayout` sidebar dùng `NavLink` và active state theo URL.
- Đã tạo placeholder pages cho Library, Search, Study Create, Test Taking, Test Result, Test Review, Flashcard Session và Not Found.
- Đã chạy `npm run lint`: Pass.
- Đã chạy `npm run build`: Pass, bao gồm `tsc -b` và `vite build`.
- Số vòng lặp code-debug: 0.

## 13. Warning/Risk sau triển khai

- `npm install react-router-dom` báo `2 high severity vulnerabilities`.
- Chưa chạy `npm audit fix` vì thao tác đó có thể thay đổi dependency ngoài phạm vi route map đã duyệt.
- Routing phase chỉ dựng route shell/placeholder, chưa triển khai nghiệp vụ Library/Test/Flashcard chi tiết.

## 14. Trạng thái phê duyệt

- Trạng thái hiện tại: Hoàn thành theo phạm vi đã được phê duyệt.
- Người dùng đã cho phép thêm dependency `react-router-dom`.
