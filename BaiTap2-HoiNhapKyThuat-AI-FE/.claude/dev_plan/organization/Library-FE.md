# Development Plan FE - Library / Organization

## 1. Thông tin chung

- Module/feature: Library Frontend, tương ứng Organization Module Backend
- Repository thực hiện: `BaiTap2-HoiNhapKyThuat-AI-FE`
- Ngày lập plan: 2026-08-06
- Trạng thái: Hoàn thành baseline; follow-up 2026-08-07 chờ phê duyệt
- Phụ thuộc: `ProjectFoundation-FE.md`, `AuthAuthz-FE.md` và `RoutingNavigation-FE.md` đã hoàn thành

## 2. Mục tiêu

- Cho phép tạo Folder hoặc VocabSet trong root hoặc trong Folder hiện tại.
- Cho phép tìm kiếm item theo tên và mở item bằng `itemPath`.
- Cho phép mở chi tiết VocabSet và gắn các Vocab đã tồn tại vào VocabSet bằng thao tác đơn hoặc bulk.
- Giữ đúng API contract, response wrapper, auth flow và design system hiện có.

## 3. Requirement và business rule liên quan

- Chỉ actor `Logged-in User` được truy cập vùng Library.
- `Folder` có thể chứa `Folder` hoặc `VocabSet`; cây dùng `parentId`.
- `VocabSet` thuộc một Folder và liên kết với Vocab theo quan hệ n-n.
- Tên item sau khi trim phải duy nhất trong cùng parent của cùng user, áp dụng chung giữa Folder và VocabSet.
- Search dùng contains/LIKE và trả danh sách phẳng có `itemPath`.
- Get by path resolve từng segment theo direct child; path có thể có leading/trailing slash.
- Add Vocab idempotent; bulk add xử lý Partial Failure theo từng `vocabId`.
- Không tạo mới hoặc cập nhật nội dung Vocab trong scope này; chỉ gắn Vocab đã tồn tại vào VocabSet.

## 4. Tài liệu và tài nguyên đã đối chiếu

- `.claude/rules/CLAUDE.md`, `.claude/workflows/WORKFLOW.md`, `.claude/skills/SKILL.md`
- `../BaiTap2-HoiNhapKyThuat-AI-BE/.claude/docs/ApplicationContext.md`
  - Mục 2: Vocabulary Hierarchy và Logged-in User.
  - Mục 4: Organization Module.
- `../BaiTap2-HoiNhapKyThuat-AI-BE/.claude/docs/Frontend_API_Guide.md`
  - Mục 5.1-5.7: Organization API contract.
  - Mục 2.2-2.3 và Mục 9: response wrapper, authentication và lỗi.
- `../BaiTap2-HoiNhapKyThuat-AI-BE/.claude/docs/modules/Organization_Module.md`
  - Business rules, search/path, ownership và add/bulk add Vocab.
- `../BaiTap2-HoiNhapKyThuat-AI-BE/.claude/docs/modules/Vocabulary_Module.md`
  - Shape Vocab và giới hạn create/update Vocab; dùng để xác định boundary của Library.
- `Html-template/vocabLib_folder/{DESIGN.md,code.html}`
  - Layout sidebar tree, search, breadcrumb, table, action và responsive direction.
- `Html-template/create_folder_popup/{DESIGN.md,code.html}`
- `Html-template/create_vocabSet_popup/{DESIGN.md,code.html}`
- `Html-template/vocabLib_vocabSetDetail/{DESIGN.md,code.html}`
- `Html-template/AddVocab_popup/{DESIGN.md,code.html}`
- `Html-template/BulkAddVocab_popup/{DESIGN.md,code.html}`
- `../BaiTap2-HoiNhapKyThuat-AI-BE/postman/BaiTap2-HoiNhapKyThuat-AI.postman_collection.json`
  - Organization requests từ `Create Folder` đến `Bulk Add Vocabs To Vocab Set`.
- `.claude/dev_plan/ProjectFoundation-FE.md`
- `.claude/dev_plan/AuthAuthz-FE.md`
- `.claude/dev_plan/RoutingNavigation-FE.md`

## 5. Hiện trạng Frontend

- `src/services/organizationService.ts` đã có toàn bộ service cho create Folder, create VocabSet, children, search, by-path, add one và bulk add.
- `src/types/organization.ts` đã có `Item`, request create và bulk request; cần bổ sung type state/view-model nếu implementation cần.
- `src/types/vocabulary.ts` đã có `Vocab`, `AddVocabToSetResponse` và `BulkAddVocabsToSetResponse`.
- `src/pages/LibraryPage.tsx` hiện chỉ là placeholder, chưa gọi API.
- Route `/library` và `/library/search` đã được khai báo trong `src/App.tsx`; source hiện tại đang tham chiếu `LibrarySearchPage` nhưng file này chưa có trong danh sách source kiểm tra, cần xác nhận/tạo trong implementation scope.
- `src/layouts/AppLayout.tsx` đã có shell/sidebar và active route; sẽ tái sử dụng, chỉ chỉnh khi cần cho Library UI.
- `src/api/client.ts` đã hỗ trợ Bearer token, query, response wrapper và `ApiError`.

## 6. Phạm vi triển khai

### 6.1. Library tree và item navigation

- Load root children bằng `GET /api/v1/items/children`.
- Load children của Folder bằng `GET /api/v1/items/children?parentId={folderId}`.
- Render tree Folder/VocabSet, trạng thái expand/collapse, active item và `vocabCount` khi Backend trả về.
- Chọn Folder để load children tương ứng; chọn VocabSet để hiển thị detail state theo khả năng contract.
- Hiển thị loading, empty, error, retry và disabled state phù hợp.

### 6.2. Create item

- Modal tạo Folder: `folderName`, `parentId`.
- Modal tạo VocabSet: `vocabSetName`, `vocabSetDescription`, `parentId`.
- Required/trim validation ở client; khóa submit khi đang gửi để ngăn duplicate submission.
- Sau success đóng modal, cập nhật tree hoặc reload children hiện tại và hiển thị success state.
- Hiển thị `message` từ API cho lỗi duplicate name, validation, unauthorized hoặc forbidden.

### 6.3. Search và path

- Search theo `name` với `GET /api/v1/items/search?name={keyword}`.
- Không gọi search với keyword rỗng sau trim; xử lý loading, empty và lỗi.
- Kết quả hiển thị type, name, description, `itemPath`, `vocabCount`.
- Cho phép mở kết quả bằng `GET /api/v1/items/by-path?path={itemPath}` và đồng bộ selection/tree khi phù hợp.
- Tái sử dụng route `/library/search`, không tạo route mới.

### 6.4. VocabSet detail và add Vocab

- Hiển thị thông tin summary của VocabSet và các action theo template.
- Add một Vocab đã tồn tại bằng `POST /api/v1/vocab-sets/{vocabSetId}/vocabs/{vocabId}`.
- Bulk add bằng `POST /api/v1/vocab-sets/{vocabSetId}/vocabs/bulk` với `vocabIds: number[]`.
- Hiển thị kết quả Partial Failure theo từng item, success/failed count và lỗi từng Vocab.
- Không hiển thị token hoặc field nhạy cảm; không tự tạo API lấy danh sách VocabSet nếu contract chưa có.

## 7. Phạm vi không thực hiện

- Không sửa Backend, tài liệu Backend, Postman collection hoặc `Html-template`.
- Không tạo API contract mới, không đổi method, endpoint, field hoặc response wrapper.
- Không triển khai create/bulk import/update Vocab; đây là Vocabulary Management scope.
- Không triển khai test/flashcard từ VocabSet; chỉ giữ action/route hiện có nếu template thể hiện nhưng không gọi API ngoài scope.
- Không thêm dependency, state-management library, form library hoặc UI library mới.
- Không triển khai delete/rename item vì API contract hiện tại không mô tả các endpoint này.

## 8. API dự kiến tích hợp

| Method | Endpoint | Request/query | Response sử dụng |
|---|---|---|---|
| `POST` | `/api/v1/folders` | `{ folderName, parentId }` | `Item` mới |
| `POST` | `/api/v1/vocab-sets` | `{ vocabSetName, vocabSetDescription, parentId }` | `Item` mới |
| `GET` | `/api/v1/items/children` | optional `parentId` | `Item[]` |
| `GET` | `/api/v1/items/search` | `name` | `Item[]` phẳng có `itemPath` |
| `GET` | `/api/v1/items/by-path` | `path` | `Item` có `itemPath` |
| `POST` | `/api/v1/vocab-sets/{vocabSetId}/vocabs/{vocabId}` | path params | `vocabSet`, `vocab`, `added` |
| `POST` | `/api/v1/vocab-sets/{vocabSetId}/vocabs/bulk` | `{ vocabIds: number[] }` | counts và kết quả từng item |

## 9. Đối chiếu API Guide và Postman

- API Guide quy định bulk request là `vocabIds: [1, 2, 3]` dạng số.
- Postman collection dùng placeholder chuỗi trong raw JSON (`"{{vocabId}}"`, ...).
- Đây là điểm không nhất quán. Theo rule, implementation sẽ bám `Frontend_API_Guide.md` và dùng `number[]`; không tự chọn contract Postman thay thế.
- API Guide và template VocabSet detail mô tả danh sách Vocab/metrics, nhưng API Guide không công bố endpoint lấy danh sách Vocab theo `vocabSetId`. Cần người dùng xác nhận phạm vi hiển thị detail: chỉ summary/action, hay bổ sung contract Backend trước khi code.

## 10. Kiến trúc và data flow dự kiến

- `LibraryPage` sở hữu `selectedParentId`, tree expansion, selected item, create modal và refresh key.
- `LibrarySearchPage` sở hữu keyword, search result, loading/error/empty state và điều hướng selection.
- Component đề xuất:
  - `src/features/library/components/LibraryTree.tsx`
  - `src/features/library/components/LibraryTreeItem.tsx`
  - `src/features/library/components/CreateFolderModal.tsx`
  - `src/features/library/components/CreateVocabSetModal.tsx`
  - `src/features/library/components/LibrarySearchResults.tsx`
  - `src/features/library/components/VocabSetDetail.tsx`
  - `src/features/library/components/AddVocabModal.tsx`
  - `src/features/library/components/BulkAddVocabModal.tsx`
- Service hiện có được tái sử dụng; chỉ chỉnh type/service nếu phát hiện mapping contract cần thiết.
- Token lấy từ `useAuthSession`; request không tự đọc refresh-token cookie.
- Sau mutation, ưu tiên invalidate/reload children hoặc selected item thay vì cập nhật state suy diễn thiếu dữ liệu.

## 11. File dự kiến tạo/sửa

| File | Loại | Mục đích |
|---|---|---|
| `.claude/dev_plan/organization/Library-FE.md` | Tạo | Development Plan module này |
| `.claude/dev_plan/DevPlanSummary-FE.md` | Sửa | Ghi nhận plan Library ở trạng thái Chờ phê duyệt |
| `src/pages/LibraryPage.tsx` | Sửa | Triển khai tree, selection, create và detail flow |
| `src/pages/LibrarySearchPage.tsx` | Tạo hoặc sửa | Triển khai search route; hiện trạng file cần xác nhận |
| `src/features/library/components/*` | Tạo | Tách component theo hierarchy dự kiến |
| `src/types/organization.ts` | Sửa nếu cần | Bổ sung type UI/response cần thiết, không đổi API contract |
| `src/App.css` hoặc CSS module Library hiện có | Sửa nếu cần | Style theo design token/template và responsive |

Không dự kiến xóa file hoặc sửa Backend, Postman và HTML template.

## 12. State và trạng thái UI

- Loading: skeleton/StatusMessage cho tree, search và mutation.
- Empty: root không có item, Folder không có child, search không có kết quả.
- Error: hiển thị `ApiError.message`, có retry/reload khi phù hợp.
- Disabled: khóa form/action trong lúc submit; ngăn bulk add gửi trùng.
- Success: thông báo tạo item/add Vocab thành công, cập nhật số lượng/tree.
- Validation: trim tên, required field, description tùy chọn, danh sách `vocabIds` không rỗng và là số dương.
- Unauthorized/forbidden: để auth/session boundary xử lý theo cơ chế hiện tại; không hiển thị dữ liệu riêng tư.

## 13. Test và kiểm tra dự kiến

- Kiểm tra component/service cho: load root/children, expand tree, create folder/set, search, path resolution, add one, bulk partial failure và duplicate submit.
- Nếu project chưa có test runner, không thêm dependency test ngoài phê duyệt; kiểm tra thủ công theo route và API mock phù hợp với convention hiện có.
- Chạy theo package manager `npm` (`package-lock.json`):
  - `npm run lint`
  - `npm run build` (bao gồm `tsc -b` và production build)
- Không có script `test`, `format`, `type-check` riêng trong `package.json` hiện tại; sẽ ghi nhận chính xác nếu chưa có.
- Nếu Backend chạy được, đối chiếu request bằng Postman collection nhưng không dùng Postman thay cho kiểm tra Frontend.

## 14. Rủi ro và quyết định cần xác nhận

- **Rủi ro contract:** Chưa có endpoint trong API Guide để lấy danh sách Vocab của VocabSet; không thể triển khai table detail đầy đủ mà không tự tạo contract.
- **Mâu thuẫn request:** Postman dùng chuỗi placeholder cho `vocabIds`, API Guide dùng số; plan chọn API Guide và chờ xác nhận nếu Backend thực tế yêu cầu khác.
- **Source hiện trạng:** `App.tsx` đang import `LibrarySearchPage` nhưng file chưa được xác nhận trong source listing; implementation cần tạo/sửa file này trong phạm vi Library.
- **Responsive:** Template là desktop-first; cần giữ tree/table usable trên mobile bằng layout responsive, không thay đổi business behavior.
- **API thật:** Chưa xác nhận Backend đang chạy tại `http://localhost:8081`; không cam kết kiểm thử integration nếu môi trường chưa sẵn sàng.

## 15. Output dự kiến

- `/library` hiển thị cây Folder/VocabSet từ Backend, cho phép mở rộng và chọn item.
- Tạo Folder/VocabSet đúng parent, có validation và trạng thái request đầy đủ.
- `/library/search` tìm item theo tên và mở kết quả theo `itemPath`.
- VocabSet có action add one/bulk Vocab đã tồn tại, hiển thị kết quả partial failure.
- UI bám template Vocab Library: sidebar tree, search, card/table, modal, màu xanh primary, typography Hanken Grotesk/design token hiện có.
- Không thêm route/API/dependency ngoài phạm vi.

## 16. Trạng thái phê duyệt

- Hoàn thành theo phạm vi đã được phê duyệt.

## 17. Kết quả triển khai

- `/library` đã load root children bằng `GET /api/v1/items/children`.
- Folder có thể expand/collapse và load children bằng `GET /api/v1/items/children?parentId={folderId}`.
- Có selection cho Folder/VocabSet và detail panel tương ứng.
- Có modal tạo Folder bằng `POST /api/v1/folders`.
- Có modal tạo VocabSet bằng `POST /api/v1/vocab-sets`.
- `/library/search` đã tìm item bằng `GET /api/v1/items/search?name={keyword}`.
- Search result có thể mở bằng `GET /api/v1/items/by-path?path={itemPath}` và điều hướng về `/library`.
- VocabSet detail hiển thị summary/action; không hiển thị danh sách vocab vì API Guide chưa có endpoint list vocab theo VocabSet.
- Add one Vocab dùng `POST /api/v1/vocab-sets/{vocabSetId}/vocabs/{vocabId}`.
- Bulk add dùng `POST /api/v1/vocab-sets/{vocabSetId}/vocabs/bulk` với request `number[]` theo API Guide.
- Đã xử lý loading, empty, error, success, disabled và duplicate submit guard ở các form/action chính.
- Đã chạy `npm run lint`: Pass.
- Đã chạy `npm run build`: Pass, bao gồm `tsc -b` và `vite build`.
- Số vòng lặp code-debug: 2.

## 18. Warning/Risk sau triển khai

- Chưa test API thật nếu Backend chưa chạy tại `http://localhost:8081`.
- Không triển khai danh sách vocab trong VocabSet vì thiếu endpoint contract.
- Không chạy `npm audit fix`; project vẫn có cảnh báo npm audit từ phase routing trước đó.

## 19. Follow-up 2026-08-07 - Clear selection ngoài vùng chọn và đổi Add Vocab sang tạo vocab mới

### 19.1. Trạng thái

- Trạng thái: Chờ phê duyệt.
- Lý do cần cập nhật plan: yêu cầu hiện tại thay đổi hành vi đã ghi trong section 6.4/17. Baseline trước đó đang dùng API gắn Vocab đã tồn tại vào VocabSet; yêu cầu mới xác nhận hai thao tác `Add Vocab` và `Bulk Add` phải là tạo/import Vocab mới vào VocabSet.
- Chưa được phép sửa source code cho follow-up này cho đến khi người dùng phê duyệt rõ ràng.

### 19.2. Requirement người dùng

- Khi người dùng nhấn ra ngoài vùng chọn trong màn hình Library, `selectedItem` phải được set về `null`.
- Hai thao tác trên VocabSet detail:
  - `Add Vocab`: tạo một vocab mới và gắn ngay vào VocabSet hiện tại.
  - `Bulk Add`: import nhiều vocab mới từ file `.xlsx` và gắn các dòng hợp lệ vào VocabSet hiện tại.
- Không dùng hai API gắn vocab đã tồn tại cho hai nút này:
  - `POST /api/v1/vocab-sets/{vocabSetId}/vocabs/{vocabId}`
  - `POST /api/v1/vocab-sets/{vocabSetId}/vocabs/bulk`

### 19.3. Tài liệu và tài nguyên đã đối chiếu

- `.claude/rules/CLAUDE.md`, `.claude/workflows/WORKFLOW.md`, `.claude/skills/SKILL.md`.
- `../BaiTap2-HoiNhapKyThuat-AI-BE/.claude/rules/CLAUDE.md`.
- `../BaiTap2-HoiNhapKyThuat-AI-BE/.claude/docs/ApplicationContext.md`:
  - Vocabulary Hierarchy: `Folder`/`VocabSet` là `Item`, `Vocab` và `VocabSet` có quan hệ n-n.
- `../BaiTap2-HoiNhapKyThuat-AI-BE/.claude/docs/Frontend_API_Guide.md`:
  - Section 4.1 `Create Vocab`: `POST /api/v1/vocabs?vocabSetId={vocabSetId}`, body `{ word, meaning, ipa }`; khi có `vocabSetId`, Backend tạo vocab và gắn vào vocab set.
  - Section 4.2 `Bulk Import Vocab`: `POST /api/v1/vocabs/bulk?vocabSetId={vocabSetId}`, `multipart/form-data`, field `file: .xlsx`, Partial Failure.
  - Section 5.6/5.7 là API gắn vocab đã tồn tại, không dùng cho hai nút theo yêu cầu mới.
- `../BaiTap2-HoiNhapKyThuat-AI-BE/.claude/docs/modules/Vocabulary_Module.md`:
  - BM1: tạo vocab bằng `word`, `meaning`, optional `ipa`, optional `vocabSetId`.
  - BM2: bulk import `.xlsx`, Partial Failure, optional `vocabSetId`.
- `../BaiTap2-HoiNhapKyThuat-AI-BE/.claude/docs/modules/Organization_Module.md`:
  - API add/bulk add trong module này là gắn `Vocab` đã tồn tại.
- `Html-template/AddVocab_popup/{DESIGN.md,code.html}`:
  - Modal mô tả thêm thủ công một từ mới, có `word`, `meaning`, optional `ipa`.
- `Html-template/BulkAddVocab_popup/{DESIGN.md,code.html}`:
  - Modal upload file, định dạng hỗ trợ `.xlsx`, cột bắt buộc `word`, `meaning`, cột tùy chọn `ipa`.
- `Html-template/vocabLib_folder/{DESIGN.md,code.html}` và `Html-template/vocabLib_vocabSetDetail/{DESIGN.md,code.html}`:
  - Layout Library tree/detail và action `Add Vocab`.
- `../BaiTap2-HoiNhapKyThuat-AI-BE/postman/BaiTap2-HoiNhapKyThuat-AI.postman_collection.json`:
  - Có request `Create Vocab In Vocab Set`: `POST {{baseUrl}}/api/v1/vocabs?vocabSetId={{vocabSetId}}`.
  - Có request `Bulk Import Vocab In Vocab Set`: `POST {{baseUrl}}/api/v1/vocabs/bulk?vocabSetId={{vocabSetId}}`, file `src/main/resources/VocabImportTemplate.xlsx`.

### 19.4. Hiện trạng source Frontend

- `src/pages/LibraryPage.tsx` đang sở hữu `selectedItem`, `selectedVocabSet`, modal state, submit state, success/error state.
- `src/pages/LibraryPage.tsx` hiện gọi:
  - `addVocabToVocabSet(selectedVocabSet.id, vocabId, accessToken)`.
  - `bulkAddVocabsToVocabSet(selectedVocabSet.id, { vocabIds }, accessToken)`.
- `src/features/library/components/AddVocabModal.tsx` hiện chỉ nhập `Vocab ID`, validate số nguyên dương.
- `src/features/library/components/BulkAddVocabModal.tsx` hiện nhập danh sách `Vocab ID`, parse ID và hiển thị kết quả theo `vocabId`.
- `src/features/library/components/VocabSetDetail.tsx` đang nhận `AddVocabToSetResponse` và hiển thị thông báo theo add-existing response.
- `src/services/vocabularyService.ts` đã có sẵn đúng service cần dùng:
  - `createVocab(request, token, vocabSetId?)`.
  - `bulkImportVocabs(file, token, vocabSetId?)`.
- `src/types/vocabulary.ts` đã có sẵn:
  - `CreateVocabRequest`.
  - `CreateVocabInSetResponse`.
  - `BulkImportVocabResponse`.

### 19.5. Phạm vi triển khai được đề xuất

- Chỉ sửa Frontend trong module Library/Vocabulary UI; không sửa Backend, Postman collection hoặc `Html-template`.
- Không thêm dependency, route, authorization model, API contract hoặc kiến trúc mới.
- Không xóa hai service add-existing trong `organizationService.ts`; chỉ không dùng chúng cho hai nút `Add Vocab` và `Bulk Add` ở VocabSet detail.
- Không triển khai endpoint danh sách vocab trong VocabSet vì API Guide vẫn chưa công bố contract này.
- Không triển khai download file mẫu nếu source static/endpoint tải template chưa có contract trong FE hiện tại.

### 19.6. Phương án clear `selectedItem`

- `LibraryPage` tiếp tục sở hữu `selectedItem`.
- Thêm handler click/mouse down ở container `section.library-page`.
- Khi event target nằm ngoài các vùng tương tác chính của Library thì gọi `setSelectedItem(null)`:
  - ngoài `library-tree-panel`;
  - ngoài `library-detail-panel`;
  - ngoài toolbar/action đang dùng để tạo item;
  - ngoài modal/backdrop đang mở.
- Click vào tree item, detail card, action button hoặc modal không clear selection.
- Khi clear selection, detail panel trở về empty state hiện có `Chưa chọn item`.
- Không thay đổi logic expand/collapse folder.

### 19.7. Phương án đổi Add Vocab

- Đổi `AddVocabModal` từ input `Vocab ID` sang form tạo vocab mới:
  - `word`: required, trim, không cho submit nếu rỗng.
  - `meaning`: required, trim, không cho submit nếu rỗng.
  - `ipa`: optional, trim; gửi `null` nếu rỗng.
- `LibraryPage.handleAddVocab` nhận `CreateVocabRequest`, gọi:
  - `createVocab(request, accessToken, selectedVocabSet.id)`.
- Khi success:
  - đóng modal;
  - lưu result để `VocabSetDetail` hiển thị thông báo tạo/gắn thành công;
  - refresh parent của selected VocabSet để cập nhật `vocabCount` nếu Backend trả count mới ở tree.
- Khi lỗi:
  - hiển thị `modalError` từ `ApiError.message`/fallback.
- Giữ duplicate submit guard bằng `submitting`.

### 19.8. Phương án đổi Bulk Add

- Đổi `BulkAddVocabModal` từ textarea ID sang file input `.xlsx`.
- Validate client:
  - bắt buộc chọn file;
  - chỉ cho phép extension `.xlsx`;
  - disabled submit khi đang gửi.
- `LibraryPage.handleBulkAdd` nhận `File`, gọi:
  - `bulkImportVocabs(file, accessToken, selectedVocabSet.id)`.
- Hiển thị kết quả `BulkImportVocabResponse`:
  - `total`, `success`, `failed`;
  - danh sách `failures` nếu Backend trả về.
- Với `items?: unknown[]`, chỉ hiển thị summary hoặc thông tin an toàn nếu type đã xác định; không dùng `any`.
- Không còn parse hoặc gửi `vocabIds`.

### 19.9. API dự kiến tích hợp

| Method | Endpoint | Request | Response sử dụng |
|---|---|---|---|
| `POST` | `/api/v1/vocabs?vocabSetId={vocabSetId}` | JSON `{ word, meaning, ipa }` | `CreateVocabInSetResponse` gồm `vocabSet`, `vocab`, `added` |
| `POST` | `/api/v1/vocabs/bulk?vocabSetId={vocabSetId}` | `multipart/form-data`, field `file` là `.xlsx` | `BulkImportVocabResponse` gồm `total`, `success`, `failed`, `failures` |

### 19.10. File dự kiến sửa

| File | Loại thay đổi | Nội dung |
|---|---|---|
| `src/pages/LibraryPage.tsx` | Sửa | Dùng `createVocab`/`bulkImportVocabs`, đổi handler modal, thêm clear selected item khi click ngoài vùng chọn |
| `src/features/library/components/AddVocabModal.tsx` | Sửa | Đổi form từ `Vocab ID` sang `word`, `meaning`, optional `ipa` |
| `src/features/library/components/BulkAddVocabModal.tsx` | Sửa | Đổi từ textarea ID sang file input `.xlsx`, hiển thị bulk import result |
| `src/features/library/components/VocabSetDetail.tsx` | Sửa | Đổi type/result message từ add-existing sang create-new vocab response |
| `src/App.css` | Sửa nếu cần | Style bổ sung cho file input/result state, giữ design token hiện có |
| `src/types/vocabulary.ts` | Sửa nếu cần | Chỉ bổ sung type guard/view model nếu cần hiển thị `items` an toàn, không đổi API contract |
| `.claude/dev_plan/organization/Library-FE.md` | Sửa | Ghi nhận follow-up, implementation result sau khi code |
| `.claude/dev_plan/DevPlanSummary-FE.md` | Sửa | Cập nhật trạng thái follow-up |

### 19.11. Output dự kiến

- Click ra ngoài vùng tree/detail/modal/action sẽ bỏ chọn item hiện tại và detail panel hiện `Chưa chọn item`.
- Click trong tree/detail/modal vẫn giữ selection hoặc xử lý action bình thường.
- Nút `Add Vocab` mở popup tạo vocab mới bằng `word`, `meaning`, optional `ipa`; success tự đóng popup và hiển thị thông báo.
- Nút `Bulk Add` mở popup chọn file `.xlsx`; success hiển thị summary import và lỗi từng dòng nếu có.
- Hai nút này không còn yêu cầu nhập `Vocab ID`.
- Không hiển thị token hoặc dữ liệu nhạy cảm.

### 19.12. Loading, empty, error, disabled, success state

- Loading/submitting: dùng state `submitting`, disable submit/close phù hợp.
- Empty: chưa chọn file bulk hoặc chưa nhập required field thì không submit.
- Error: hiển thị lỗi trong modal qua `modalError`.
- Disabled: chặn duplicate submit khi `submitting = true`.
- Success: `successMessage` ở `LibraryPage`, result message trong `VocabSetDetail`/modal theo dữ liệu response.
- Validation: trim string; `.xlsx` extension cho file bulk; không dùng `any` hoặc suppression.

### 19.13. Kiểm tra dự kiến sau implementation

- Package manager: `npm` theo `package-lock.json`.
- Chạy `npm.cmd run lint`.
- Chạy `npm.cmd run build`; script này bao gồm TypeScript type-check (`tsc -b`) và production build (`vite build`).
- Không có script `test`, `format`, `type-check` riêng trong `package.json`; sẽ ghi nhận trong báo cáo cuối nếu vẫn không có.
- Không gọi API thật nếu Backend không được yêu cầu/chưa chạy; Postman chỉ dùng để đối chiếu request.

### 19.14. Rủi ro và lưu ý

- Đây là thay đổi phạm vi so với section 6.4/17 của baseline Library plan; yêu cầu mới của người dùng sẽ supersede phần add-existing cho hai nút hiện tại.
- `createVocab` hiện có return type union `Vocab | CreateVocabInSetResponse`; khi gọi kèm `vocabSetId`, UI cần xử lý theo response có `vocabSet`, `vocab`, `added` mà không assertion không an toàn.
- `BulkImportVocabResponse.items` đang là `unknown[]`; nếu cần hiển thị chi tiết item import, phải thêm type guard rõ ràng hoặc chỉ hiển thị `failures`.
- Click-outside behavior cần tránh đóng/clear khi người dùng đang thao tác trong modal hoặc click vào item/action hợp lệ.
- Chưa có endpoint list vocab trong VocabSet, nên sau add/import vẫn chỉ refresh summary/tree nếu Backend trả count qua item children.
