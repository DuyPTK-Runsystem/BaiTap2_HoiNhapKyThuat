# Development Plan FE - Library / Organization

## 1. Thông tin chung

- Module/feature: Library Frontend, tương ứng Organization Module Backend
- Repository thực hiện: `BaiTap2-HoiNhapKyThuat-AI-FE`
- Ngày lập plan: 2026-08-06
- Trạng thái: Hoàn thành
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
