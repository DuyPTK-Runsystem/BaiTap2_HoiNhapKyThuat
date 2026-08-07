# Development Plan FE - Vocabulary Page

## 1. Thông tin chung

- Module/feature: Vocabulary management page
- Route mới: `/vocab`
- Navigation label: `Vocabulary`
- Ngày lập plan: 2026-08-07
- Trạng thái: Chờ phê duyệt
- Phụ thuộc: `ProjectFoundation-FE.md`, `AuthAuthz-FE.md`, `RoutingNavigation-FE.md` và Library/Organization plan đã hoàn thành

## 2. Mục tiêu

- Thêm tab `Vocabulary` vào left navigation.
- Tạo màn hình `/vocab` hiển thị danh sách Vocab dạng bảng theo template Vocabulary detail.
- Hỗ trợ phân trang, chọn số dòng mỗi trang, loading/empty/error/disabled state.
- Hỗ trợ lọc theo item cha nếu API contract được xác nhận.
- Hiển thị `word`, `meaning`, `ipa`, audio player từ `audioUrl` và trạng thái `mastered`.

## 3. Tài liệu và tài nguyên đã đối chiếu

- `.claude/rules/CLAUDE.md`, `.claude/workflows/WORKFLOW.md`, `.claude/skills/SKILL.md`
- `../BaiTap2-HoiNhapKyThuat-AI-BE/.claude/docs/ApplicationContext.md`
  - Vocabulary hierarchy và actor `Logged-in User`.
- `../BaiTap2-HoiNhapKyThuat-AI-BE/.claude/docs/Frontend_API_Guide.md`
  - Mục 4.1-4.5: create, bulk import, lookup, update meaning và audio.
  - Chưa mô tả endpoint list Vocab có pagination.
- `../BaiTap2-HoiNhapKyThuat-AI-BE/.claude/docs/modules/Vocabulary_Module.md`
  - Vocab fields, update chỉ cho phép `meaning`, audio và `mastered`.
- `../BaiTap2-HoiNhapKyThuat-AI-BE/postman/BaiTap2-HoiNhapKyThuat-AI.postman_collection.json`
  - Có create/lookup/update/bulk/audio; chưa có request GET list Vocab pagination.
- `Html-template/vocabLib_vocabSetDetail/code.html`
  - Bảng Word/Meaning/IPA/Audio/Actions và pagination toolbar.
- `Html-template/vocabLib_vocabSetDetail/DESIGN.md`
  - Design token, table, pagination, typography và responsive behavior.
- Source Frontend hiện tại:
  - `src/layouts/AppLayout.tsx`: navigation và route title.
  - `src/App.tsx`: protected route tree.
  - `src/services/vocabularyService.ts`: create, bulk import, lookup, update, audio; chưa có list pagination service.
  - `src/types/vocabulary.ts`: `Vocab` đã có các field cần hiển thị.

## 4. Mâu thuẫn API cần xác nhận

- Yêu cầu hiện tại nêu query parameter `parentId`.
- Backend source hiện có `GET /api/v1/vocabs` nhận `vocabSetId`, `Pageable` và `Specification<Vocab>`; service cũng lọc theo `vocabSetId`.
- `Frontend_API_Guide.md` chưa công bố contract GET all Vocab pagination.
- Postman collection chưa có request tương ứng.
- Không tự chọn giữa `parentId` và `vocabSetId`. Cần xác nhận contract chính thức trước khi code:
  1. Dùng `GET /api/v1/vocabs?page={page}&size={size}&vocabSetId={id}` theo Backend hiện tại; hoặc
  2. Backend bổ sung/đổi contract sang `parentId`.

## 5. Phạm vi triển khai dự kiến

### 5.1. Navigation và route

- Thêm navigation item `{ label: 'Vocabulary', to: '/vocab', match: ['/vocab'] }`.
- Thêm protected route `/vocab` trong `App.tsx`.
- Thêm route title `Vocabulary` cho topbar.
- Không thay đổi các route hiện có.

### 5.2. Vocabulary list

- Tạo `VocabPage`.
- Gọi service list khi vào page, đổi page, đổi page size hoặc đổi filter.
- Render table responsive theo template.
- Audio dùng `getAudioUrl` và `<audio controls>`; không hiển thị raw token hoặc dữ liệu nhạy cảm.
- Chỉ cho phép edit `meaning` nếu action Edit được duyệt; không tự triển khai Delete vì API contract không có endpoint Delete.

### 5.3. Pagination

- Chuyển đổi page index theo quy ước Backend hiện tại: `spring.data.web.pageable.one-indexed-parameters=true`.
- Hiển thị current page, total elements, total pages, page size và nút Previous/Next.
- Disable nút ở boundary hoặc khi request đang loading.
- Không gửi request trùng khi đang chuyển trang.

### 5.4. States

- Loading: trạng thái tải bảng.
- Empty: không có Vocab theo filter/page.
- Error: hiển thị `ApiError.message` và retry.
- Disabled: khóa pagination/filter khi request pending.
- Success: hiển thị dữ liệu và audio player nếu `audioUrl` tồn tại.

## 6. API dự kiến sau khi contract được xác nhận

| Method | Endpoint | Query | Response |
|---|---|---|---|
| `GET` | `/api/v1/vocabs` | `page`, `size`, và `vocabSetId` hoặc `parentId` theo quyết định | Response wrapper chứa `ResultPaginationDTO` gồm danh sách Vocab và metadata |
| `GET` | `/api/v1/vocabs/audio/{fileName}` | path param | Binary `audio/mpeg`, dùng cho audio player |

Các API create/bulk/update/lookup không nằm trong phase đầu của `/vocab` trừ khi được phê duyệt bổ sung.

## 7. File dự kiến tạo/sửa

| File | Loại | Nội dung |
|---|---|---|
| `src/pages/VocabPage.tsx` | Tạo | Màn hình list, pagination và UI states |
| `src/features/vocabulary/components/VocabTable.tsx` | Tạo | Bảng Word/Meaning/IPA/Audio/Mastered |
| `src/features/vocabulary/components/VocabPagination.tsx` | Tạo | Pagination controls |
| `src/services/vocabularyService.ts` | Sửa | Thêm service GET list sau khi contract chốt |
| `src/types/vocabulary.ts` | Sửa | Thêm pagination response type theo DTO thực tế |
| `src/layouts/AppLayout.tsx` | Sửa | Thêm tab Vocabulary và route title |
| `src/App.tsx` | Sửa | Thêm protected route `/vocab` |
| `src/App.css` | Sửa | Style màn hình/table/pagination responsive nếu cần |
| `.claude/dev_plan/vocabulary/VocabPage-FE.md` | Sửa cuối file | Ghi nhận implementation, không sửa lịch sử cũ |
| `.claude/dev_plan/DevPlanSummary-FE.md` | Sửa cuối lịch sử | Ghi nhận plan mới ở dòng cuối |

## 8. Phạm vi không thực hiện

- Không sửa Backend, API Guide hoặc Postman collection trong task Frontend.
- Không tự tạo hoặc đổi API contract `parentId`/`vocabSetId`.
- Không thêm dependency mới.
- Không triển khai delete, bulk import, create vocab hoặc edit meaning nếu chưa được yêu cầu riêng.
- Không thay đổi HTML template.

## 9. Kiểm tra dự kiến

- Test component/service nếu test runner hiện có hỗ trợ.
- Kiểm tra thủ công route `/vocab`, navigation active state, pagination boundary, empty/error/loading và audio.
- `npm run lint`.
- `npm run build` (bao gồm `tsc -b` và production build).
- Không gọi API thật nếu Backend contract chưa được xác nhận/chạy sẵn.

## 10. Trạng thái

- Chờ người dùng xác nhận API contract và phê duyệt plan trước khi thay đổi source code.

## 11. Kết quả triển khai

- Đã dùng `vocabSetId` theo xác nhận của người dùng.
- Đã thêm protected route `/vocab` và tab `Vocabulary` trên sidebar.
- Đã thêm `getVocabs(vocabSetId, page, size, token)` gọi `GET /api/v1/vocabs?vocabSetId={id}&page={page}&size={size}`.
- Đã thêm type `PaginatedVocabs` và metadata `page`, `pageSize`, `totalPages`, `totalItems` theo Backend `ResultPaginationDTO`.
- Đã tạo bảng hiển thị Word, Meaning, IPA, Audio và Mastered.
- Đã tạo pagination với page size 10/20/50, Previous/Next, loading và boundary disabled state.
- `/vocab` không có `vocabSetId` sẽ hiển thị trạng thái yêu cầu chọn VocabSet, không tự hard-code ID.
- Đã xử lý loading, empty, error/retry, audio URL và responsive table.
- Đã chạy `npm run lint`: Pass.
- Đã chạy `npm run build`: Pass, bao gồm `tsc -b` và `vite build`.
- Đã chạy `git diff --check`: Pass.
- Số vòng lặp code-debug: 1.

## 12. Follow-up - Folder panel, See all vocabulary và chọn VocabSet

### 12.1. Mục tiêu

- Thêm panel cây Folder/VocabSet ở bên trái màn hình `/vocab`, tái sử dụng `LibraryTree` và Organization service hiện có.
- Thêm checkbox `See all vocabulary`.
- Khi checkbox bật, gọi `GET /api/v1/vocabs?page={page}&size={size}` không truyền `vocabSetId` và khóa chọn Folder/VocabSet.
- Khi checkbox tắt, gọi `GET /api/v1/vocabs?vocabSetId={selectedVocabSetId}&page={page}&size={size}` theo VocabSet đang chọn.
- Giữ selected VocabSet qua query parameter `vocabSetId` trên URL để reload/share được màn hình.

### 12.2. Luồng `/library` sang `/vocab`

- Double-click trên item `VOCAB_SET` trong `LibraryTree` gọi callback mở Vocab page.
- `LibraryPage` điều hướng tới `/vocab?vocabSetId={vocabSetId}`.
- Double-click Folder không điều hướng tới `/vocab`.
- Không thay đổi single-click selection hoặc expand/collapse behavior hiện tại.

### 12.3. Phạm vi file dự kiến

| File | Loại | Nội dung |
|---|---|---|
| `src/pages/VocabPage.tsx` | Sửa | Folder panel, checkbox mode, selected VocabSet và query state |
| `src/features/vocabulary/components/VocabSourcePanel.tsx` | Tạo | Panel tree và checkbox `See all vocabulary` |
| `src/features/library/components/LibraryTree.tsx` | Sửa | Callback double-click và disabled selection state |
| `src/pages/LibraryPage.tsx` | Sửa | Điều hướng khi double-click VocabSet |
| `src/services/vocabularyService.ts` | Sửa | Cho phép `vocabSetId` optional khi gọi list |
| `src/App.css` | Sửa | Layout hai cột và disabled folder panel |
| `.claude/dev_plan/vocabulary/VocabPage-FE.md` | Sửa cuối file | Ghi nhận kết quả follow-up |
| `.claude/dev_plan/organization/Library-FE.md` | Sửa cuối file | Ghi nhận double-click behavior |
| `.claude/dev_plan/DevPlanSummary-FE.md` | Sửa cuối lịch sử | Ghi nhận follow-up ở dòng cuối |

### 12.4. States và ràng buộc

- Loading/empty/error cho root children và danh sách Vocab.
- Khi `See all vocabulary = true`, tree panel bị disabled; không cho chọn hoặc double-click item.
- Khi `See all vocabulary = false` mà chưa có VocabSet, hiển thị empty state và không gọi list API.
- Khi đổi mode hoặc VocabSet, reset page về 1 và tránh duplicate request.
- Không hard-code VocabSet ID.

### 12.5. Trạng thái

- Chờ người dùng phê duyệt follow-up trước khi code.

## 13. Kết quả triển khai follow-up

- Đã tạo `VocabSourcePanel` ở bên trái `/vocab`, tải cây Folder/VocabSet bằng Organization API.
- Đã thêm checkbox `See all vocabulary`; khi bật, gọi list API không truyền `vocabSetId` và khóa thao tác trên tree.
- Khi tắt checkbox, chọn VocabSet sẽ cập nhật URL `vocabSetId`, reset page và tải Vocab theo VocabSet.
- Đã giữ pagination, loading, empty, error/retry, audio và responsive layout.
- Đã chạy `npm run lint`: Pass.
- Đã chạy `npm run build`: Pass, bao gồm `tsc -b` và `vite build`.
- Đã chạy `git diff --check`: Pass.
- Số vòng lặp code-debug follow-up: 0.

## 14. Follow-up - Authenticated audio request

- Audio request now sends `Authorization: Bearer <access_token>` and `credentials: include`.
- Response audio is converted to a Blob URL before rendering in the `<audio>` element, avoiding unauthenticated direct media requests.
- AbortController cancels stale requests and object URLs are revoked during cleanup.
- `npm run lint`: Pass.
- `npm run build`: Pass, including `tsc -b` and `vite build`.
- `git diff --check`: Pass.
- Số vòng lặp code-debug: 2.

## 15. Follow-up - Hiển thị kết quả bulk import theo response Backend

- Đã map response Backend `totalRows`, `successCount`, `failureCount` và `items`.
- Modal Bulk Add hiển thị rõ tổng số dòng, số dòng thành công và số dòng thất bại.
- Các dòng thất bại được liệt kê theo `rowNumber`, `word` và `error` từ API.
- Thông báo success của Library cũng dùng đúng các field count từ Backend.
- `npm run lint`: Pass.
- `npm run build`: Pass, bao gồm `tsc -b` và `vite build`.
- `git diff --check`: Pass.
- Số vòng lặp code-debug: 0.

## 16. Follow-up - Map snake_case bulk import counts

- Backend trả count fields theo JSON `total_rows`, `success_count`, `failure_count`; Frontend đã cập nhật type và UI theo đúng contract.
- `npm run lint`: Pass.
- `npm run build`: Pass, bao gồm `tsc -b` và `vite build`.
- `git diff --check`: Pass.
- Số vòng lặp code-debug: 0.
