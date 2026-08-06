# WORKFLOW.md

## 1. Mục đích

Tài liệu này quy định quy trình bắt buộc mà AI Agent phải tuân thủ khi thực hiện yêu cầu liên quan đến mã nguồn Frontend, bao gồm:

* Tạo mới chức năng hoặc màn hình.
* Chỉnh sửa giao diện.
* Tích hợp API.
* Sửa lỗi.
* Refactor.
* Bổ sung hoặc cập nhật test.
* Thay đổi cấu hình Frontend.
* Thay đổi mã nguồn React và TypeScript.

Hai repository của dự án được đặt trong cùng một thư mục cha:

```text
BaiTap2-HoiNhapKyThuat-AI-BE
BaiTap2-HoiNhapKyThuat-AI-FE
```

Mọi thay đổi Frontend phải được thực hiện trong:

```text
BaiTap2-HoiNhapKyThuat-AI-FE
```

Repository Backend chứa tài liệu nghiệp vụ, API contract và Postman collection dùng để phát triển Frontend.

---

# 2. Xác định thư mục làm việc

## 2.1. Kiểm tra thư mục hiện tại

Trước khi đọc tài liệu, lập Development Plan hoặc thay đổi mã nguồn, AI Agent phải chạy:

```bash
pwd
ls
```

Không được giả định terminal đang đứng tại repository Frontend.

---

## 2.2. Trường hợp đang đứng tại thư mục cha

Nếu kết quả `ls` có dạng:

```text
BaiTap2-HoiNhapKyThuat-AI-BE
BaiTap2-HoiNhapKyThuat-AI-FE
```

AI Agent phải chuyển vào repository Frontend:

```bash
cd BaiTap2-HoiNhapKyThuat-AI-FE
```

Sau đó xác nhận lại:

```bash
pwd
ls
```

Mọi thao tác liên quan đến source code Frontend, Development Plan, lint, test và build phải được thực hiện từ repository Frontend.

---

## 2.3. Trường hợp đang đứng tại repository Frontend

Khi terminal đang đứng tại:

```text
BaiTap2-HoiNhapKyThuat-AI-FE
```

Repository Backend được truy cập bằng:

```text
../BaiTap2-HoiNhapKyThuat-AI-BE
```

Các đường dẫn chính khi làm việc từ repository FE:

```text
.claude
.claude/dev_plan
Html-template
src
package.json

../BaiTap2-HoiNhapKyThuat-AI-BE/.claude/docs
../BaiTap2-HoiNhapKyThuat-AI-BE/postman
```

Không được dùng đường dẫn:

```text
BaiTap2-HoiNhapKyThuat-AI-BE/...
```

khi đang đứng tại repository FE, vì đường dẫn này sẽ được hiểu là một thư mục con của repository FE.

---

## 2.4. Quy tắc sử dụng đường dẫn

Sau khi đã chuyển vào repository Frontend:

* Đường dẫn thuộc FE phải được viết tương đối từ root FE.
* Đường dẫn thuộc BE phải bắt đầu bằng:

```text
../BaiTap2-HoiNhapKyThuat-AI-BE
```

Không được:

* Sử dụng đường dẫn tuyệt đối phụ thuộc vào máy cá nhân.
* Giả định cấu trúc thư mục mà chưa kiểm tra.
* Tạo Development Plan FE trong repository BE.
* Thay đổi source code BE khi task chỉ yêu cầu Frontend.
* Thay đổi Postman collection hoặc tài liệu BE nếu chưa được yêu cầu rõ ràng.
* Dùng đường dẫn tương đối chưa được xác minh.

---

# 3. Nguồn tài liệu và tài nguyên dự án

## 3.1. Frontend API Guide

Tài liệu quan trọng nhất đối với việc tích hợp Backend là:

```text
../BaiTap2-HoiNhapKyThuat-AI-BE/.claude/docs/Frontend_API_Guide.md
```

Đây là nguồn sự thật chính cho:

* API endpoint.
* HTTP method.
* Request payload.
* Response payload.
* Response wrapper.
* Authentication flow.
* Access token.
* Refresh-token cookie.
* Error response.
* Binary và audio response.
* Vocabulary flow.
* Organization flow.
* Testing flow.
* Flashcard flow.

Frontend không được tự ý:

* Thay đổi API contract.
* Đổi tên request hoặc response field.
* Tạo endpoint không tồn tại.
* Thay đổi HTTP method.
* Giả định Backend hỗ trợ hành vi chưa được tài liệu quy định.
* Đọc HTTP-only refresh-token cookie bằng JavaScript.

---

## 3.2. Application Context

Tài liệu context tổng thể của dự án:

```text
../BaiTap2-HoiNhapKyThuat-AI-BE/.claude/docs/ApplicationContext.md
```

AI Agent phải sử dụng tài liệu này để hiểu:

* Mục tiêu của hệ thống.
* Phạm vi chức năng.
* Domain chính.
* Thuật ngữ nghiệp vụ.
* Quan hệ giữa các module.
* Luồng sử dụng tổng thể.
* Business rule mà Backend đã dựa vào để triển khai.

Frontend không được tự tạo:

* Domain concept mới.
* Business rule mới.
* Field mới.
* Trạng thái nghiệp vụ mới.
* Authorization model mới.

trừ khi được yêu cầu rõ ràng trong tài liệu hoặc được người dùng phê duyệt.

---

## 3.3. Tài liệu module

Ngoài hai tài liệu bắt buộc trên, AI Agent phải đọc các tài liệu module liên quan tại:

```text
../BaiTap2-HoiNhapKyThuat-AI-BE/.claude/docs
```

Ví dụ:

```text
../BaiTap2-HoiNhapKyThuat-AI-BE/.claude/docs/modules
```

Chỉ cần đọc các tài liệu liên quan trực tiếp đến task và dependency cần thiết.

Không cần đọc toàn bộ tài liệu của những module không liên quan.

---

## 3.4. Mẫu thiết kế HTML

Mẫu thiết kế giao diện được đặt tại:

```text
Html-template
```

AI Agent phải kiểm tra các file HTML, CSS, JavaScript, asset hoặc tài nguyên liên quan trong thư mục này trước khi triển khai màn hình tương ứng.

Mẫu thiết kế HTML là nguồn tham chiếu cho:

* Bố cục màn hình.
* Visual hierarchy.
* Typography.
* Màu sắc.
* Khoảng cách.
* Border và border radius.
* Trạng thái component.
* Vị trí button và action.
* Popup hoặc dialog.
* Navigation.
* Cách trình bày dữ liệu.
* Responsive behavior nếu mẫu có định nghĩa.

Khi chuyển mẫu HTML sang React, AI Agent phải:

* Chuyển cấu trúc giao diện sang component phù hợp.
* Tuân thủ kiến trúc React hiện tại.
* Tái sử dụng shared component và design token hiện có.
* Không sao chép JavaScript imperative từ template nếu React có cách quản lý state phù hợp hơn.
* Không nhúng nguyên file HTML vào React chỉ để tái hiện giao diện.
* Không tạo duplicate component nếu project đã có component tương đương.
* Không thay đổi thiết kế ngoài phạm vi yêu cầu.
* Không suy diễn business behavior chỉ từ HTML template.

Mẫu HTML chỉ mô tả giao diện và interaction được thể hiện trong mẫu. Requirement nghiệp vụ và API behavior vẫn phải lấy từ tài liệu dự án.

Nếu mẫu HTML mâu thuẫn với requirement hoặc API contract, AI Agent phải báo cáo mâu thuẫn và chờ quyết định.

---

## 3.5. Postman collection

Postman collection của Backend được đặt tại:

```text
../BaiTap2-HoiNhapKyThuat-AI-BE/postman/BaiTap2-HoiNhapKyThuat-AI.postman_collection.json
```

AI Agent phải kiểm tra collection này khi task có liên quan đến tích hợp API.

Postman collection được dùng để đối chiếu:

* Endpoint thực tế.
* HTTP method.
* Path parameter.
* Query parameter.
* Request header.
* Authentication header.
* Request body.
* Multipart form-data.
* Ví dụ request.
* Luồng gọi API.
* Biến môi trường hoặc base URL được collection sử dụng.

Postman collection là tài nguyên kỹ thuật hỗ trợ đối chiếu và kiểm thử API. Nó không mặc nhiên thay thế tài liệu requirement hoặc `Frontend_API_Guide.md`.

AI Agent không được:

* Sao chép token, cookie hoặc credential từ Postman vào source code.
* Hard-code base URL từ Postman nếu FE đã dùng environment variable.
* Giả định response contract chỉ từ request example trong Postman.
* Chỉnh sửa Postman collection nếu task không yêu cầu.
* Dùng Postman collection để tự tạo business rule chưa được tài liệu mô tả.

Nếu Postman collection mâu thuẫn với `Frontend_API_Guide.md`, AI Agent phải:

1. Ghi rõ endpoint hoặc field đang mâu thuẫn.
2. Nêu nội dung trong từng nguồn.
3. Kiểm tra source code Backend khi cần và khi có quyền đọc.
4. Không tự ý chọn một contract.
5. Ghi nhận mâu thuẫn trong Development Plan.
6. Chờ người dùng quyết định hoặc cập nhật tài liệu.

---

# 4. Development Plan của Frontend

Frontend phải quản lý Development Plan riêng tại:

```text
.claude/dev_plan
```

File tổng hợp:

```text
.claude/dev_plan/DevPlanSummary-FE.md
```

Development Plan chi tiết phải được tổ chức theo module.

Cấu trúc tham khảo:

```text
.claude/dev_plan
├── DevPlanSummary-FE.md
├── auth
│   └── *.md
├── vocabulary
│   └── *.md
├── organization
│   └── *.md
├── testing
│   └── *.md
├── flashcard
│   └── *.md
└── shared
    └── *.md
```

Tên folder phải phù hợp với module thực tế của Frontend.

Nếu thư mục hoặc file tổng hợp chưa tồn tại, AI Agent phải tạo:

```text
.claude/dev_plan
.claude/dev_plan/DevPlanSummary-FE.md
```

Mỗi khi một Development Plan được tạo hoặc cập nhật, AI Agent phải cập nhật:

```text
.claude/dev_plan/DevPlanSummary-FE.md
```

Không được:

* Dùng Development Plan của Backend thay cho Frontend.
* Đặt Development Plan FE trong repository Backend.
* Sửa Development Plan Backend để ghi nhận thay đổi FE.
* Xóa hoặc viết lại lịch sử của agent khác.
* Thay đổi attribution của nội dung do agent khác tạo.
* Đổi trạng thái plan thành đã phê duyệt khi chưa có xác nhận của người dùng.

---

# 5. Quy trình xử lý yêu cầu code

## Bước 1: Xác định repository

AI Agent phải:

1. Chạy `pwd`.
2. Chạy `ls`.
3. Xác định terminal đang đứng tại thư mục cha hay repository FE.
4. Chuyển vào repository FE nếu cần.
5. Xác nhận lại thư mục hiện tại.

Không được thực hiện thay đổi code khi chưa xác định đúng repository.

---

## Bước 2: Đọc rule, workflow và skill

AI Agent phải đọc và tuân thủ:

```text
.claude/rules/CLAUDE.md
.claude/workflows/WORKFLOW.md
.claude/skills/SKILL.md
```

AI Agent phải xác định:

* Quy tắc bắt buộc.
* Coding convention.
* Quy trình phê duyệt.
* Tiêu chí review.
* Yêu cầu test.
* Yêu cầu kiểm tra chất lượng.
* Quy tắc ghi nhận lịch sử và attribution.

---

## Bước 3: Đọc tài liệu dự án

AI Agent phải đọc tối thiểu:

```text
../BaiTap2-HoiNhapKyThuat-AI-BE/.claude/docs/Frontend_API_Guide.md
../BaiTap2-HoiNhapKyThuat-AI-BE/.claude/docs/ApplicationContext.md
```

Sau đó đọc tài liệu module liên quan tại:

```text
../BaiTap2-HoiNhapKyThuat-AI-BE/.claude/docs
```

AI Agent phải xác định:

* Requirement liên quan.
* Business rule liên quan.
* API endpoint liên quan.
* Request và response contract.
* Authentication requirement.
* Trạng thái thành công và thất bại.
* Module, màn hình hoặc component bị ảnh hưởng.
* Các giới hạn đã được tài liệu quy định.

---

## Bước 4: Đọc mẫu HTML và Postman collection

Nếu task liên quan đến giao diện, AI Agent phải kiểm tra:

```text
Html-template
```

Nếu task liên quan đến API, AI Agent phải kiểm tra:

```text
../BaiTap2-HoiNhapKyThuat-AI-BE/postman/BaiTap2-HoiNhapKyThuat-AI.postman_collection.json
```

AI Agent phải xác định:

* Mẫu HTML hoặc màn hình tương ứng.
* Component state được thể hiện trong mẫu.
* Endpoint và method tương ứng.
* Header, query parameter và request body.
* Điểm khác biệt giữa template, API Guide, Postman và source code hiện tại.
* Các phần phải chuyển đổi sang React thay vì sao chép trực tiếp.

Nếu không tìm thấy template hoặc request tương ứng, AI Agent phải ghi rõ điều đó trong Development Plan.

---

## Bước 5: Kiểm tra source code Frontend hiện tại

Trước khi thiết kế giải pháp, AI Agent phải kiểm tra khi phù hợp:

* Application entry point.
* Route configuration.
* Page.
* Layout.
* Feature component.
* Shared component.
* Custom hook.
* Context.
* State management.
* API client.
* API service.
* Request và response interceptor.
* TypeScript type.
* Form validation.
* Utility.
* Constant.
* Styling system.
* Asset.
* Test.
* Mock.
* Environment configuration.
* Build configuration.

AI Agent phải ưu tiên convention và pattern hiện có.

Không được thêm architecture pattern, state-management library, form library, UI library hoặc dependency nếu chưa được phê duyệt.

---

## Bước 6: Kiểm tra Development Plan

AI Agent phải kiểm tra:

```text
.claude/dev_plan
.claude/dev_plan/DevPlanSummary-FE.md
```

để xác định feature hoặc module đã có Development Plan được phê duyệt hay chưa.

### Trường hợp chưa có Development Plan được phê duyệt

AI Agent phải:

1. Phân tích requirement.
2. Phân tích API contract.
3. Phân tích Application Context.
4. Phân tích HTML template liên quan.
5. Phân tích Postman request liên quan.
6. Phân tích kiến trúc Frontend hiện tại.
7. Xác định route, component, hook, service và type bị ảnh hưởng.
8. Xác định rủi ro kỹ thuật.
9. Tạo hoặc cập nhật Development Plan trong module tương ứng.
10. Cập nhật `DevPlanSummary-FE.md`.
11. Trình bày plan cho người dùng.
12. Dừng lại và chờ người dùng phê duyệt rõ ràng.

Khi Development Plan chưa được phê duyệt:

* Không được tạo hoặc chỉnh sửa source code.
* Không được triển khai một phần feature.
* Không được thay đổi route.
* Không được thêm dependency.
* Không được thay đổi cấu hình.
* Không được tự chọn phương án kỹ thuật thay cho người dùng.

### Nội dung tối thiểu của Development Plan

Development Plan phải bao gồm:

* Mục tiêu.
* Requirement liên quan.
* Tài liệu đã đối chiếu.
* HTML template hoặc wireframe liên quan.
* Postman request liên quan.
* API contract liên quan.
* Phạm vi thực hiện.
* Phạm vi không thực hiện.
* Kiến trúc hiện tại.
* Phương án triển khai.
* Component hierarchy dự kiến.
* Data flow dự kiến.
* State ownership.
* Route bị ảnh hưởng.
* API service bị ảnh hưởng.
* TypeScript type dự kiến.
* Validation dự kiến.
* Loading, empty, error, disabled và success state.
* Danh sách file dự kiến tạo.
* Danh sách file dự kiến sửa.
* Vị trí dự kiến thay đổi.
* Test dự kiến.
* Rủi ro và phương án xử lý.
* Output dự kiến.

### Trường hợp đã có Development Plan được phê duyệt

AI Agent phải:

1. Đọc toàn bộ Development Plan.
2. Đối chiếu với requirement hiện tại.
3. Đối chiếu với `Frontend_API_Guide.md`.
4. Đối chiếu với `ApplicationContext.md`.
5. Đối chiếu với HTML template.
6. Đối chiếu với Postman collection.
7. Đối chiếu với source code FE hiện tại.
8. Xác nhận plan vẫn còn phù hợp.
9. Báo cáo trước khi code.
10. Chờ xác nhận nếu rule yêu cầu.

Không được tự ý thay đổi:

* Phạm vi.
* Component architecture.
* Data flow.
* State-management approach.
* API integration approach.
* Dependency.
* Route.
* Phương án kỹ thuật đã được phê duyệt.

Nếu cần thay đổi, AI Agent phải cập nhật Development Plan và chờ phê duyệt lại.

---

## Bước 7: Báo cáo trước khi code

AI Agent phải sử dụng cấu trúc sau:

```text
## Báo cáo trước khi triển khai

### 1. Cấu trúc hiện tại
- Kiến trúc Frontend liên quan:
- Route liên quan:
- Page, layout và component liên quan:
- Hook và state management liên quan:
- API client hoặc service liên quan:
- TypeScript type liên quan:
- Styling convention liên quan:

### 2. Feature/module sẽ thực hiện
- ...

### 3. Tài liệu và tài nguyên đã đối chiếu
- Rule:
- Workflow:
- Frontend skill:
- Application Context:
- Frontend API Guide:
- Tài liệu module:
- HTML template:
- Postman collection:
- Development Plan:

### 4. Phạm vi ảnh hưởng

| Thành phần | Loại ảnh hưởng | Nội dung thay đổi |
| ---------- | -------------- | ----------------- |
| `...`      | Tạo mới/Sửa    | ...               |

### 5. Các file dự kiến thay đổi

| File  | Loại thay đổi | Vị trí | Nội dung |
| ----- | ------------- | ------ | -------- |
| `...` | Tạo mới/Sửa   | `...`  | ...      |

### 6. API dự kiến tích hợp

| Method | Endpoint | Request | Response được sử dụng |
| ------ | -------- | ------- | --------------------- |
| `...`  | `...`    | `...`   | `...`                 |

### 7. Output dự kiến
- Giao diện:
- Hành vi người dùng:
- Loading state:
- Empty state:
- Error state:
- Disabled state:
- Success state:
- Validation:
- Điều hướng:

### 8. Rủi ro hoặc lưu ý
- ...

### 9. Trạng thái
- Chờ phê duyệt trước khi tiến hành code.
```

AI Agent chỉ được code sau khi người dùng phê duyệt hoặc xác nhận theo rule của project.

---

## Bước 8: Thực hiện code

Trong quá trình implementation, AI Agent phải:

1. Tuân thủ rule, workflow, skill và Development Plan đã được phê duyệt.
2. Chỉ thay đổi file và khu vực nằm trong phạm vi.
3. Tuân thủ kiến trúc và convention hiện tại.
4. Bám sát HTML template đã được phê duyệt.
5. Tuân thủ API contract đã đối chiếu.
6. Ưu tiên tái sử dụng component, hook, API client, service, type, utility, constant và design token.
7. Hạn chế code duplication.
8. Sử dụng functional component và React hook nếu project không có convention khác.
9. Không gọi hook có điều kiện.
10. Không sử dụng `any` nếu không có lý do kỹ thuật.
11. Không dùng type assertion không an toàn để che lỗi TypeScript.
12. Không tắt ESLint rule chỉ để làm code pass.
13. Không dùng `@ts-ignore` hoặc `@ts-nocheck` tùy tiện.
14. Không hard-code API URL nếu project đã có environment configuration.
15. Không sao chép token hoặc credential từ Postman collection.
16. Không đọc HTTP-only refresh-token cookie bằng JavaScript.
17. Không hiển thị access token hoặc dữ liệu nhạy cảm.
18. Không hiển thị field cần ẩn trong luồng làm bài.
19. Xử lý loading, empty, error, disabled và success state khi phù hợp.
20. Ngăn gửi request trùng lặp đối với thao tác không idempotent.
21. Cleanup timer, listener, subscription, request và object URL khi cần.
22. Sử dụng semantic HTML và accessible label phù hợp.
23. Không thay đổi UI convention ngoài phạm vi yêu cầu.

AI Agent không được tự ý:

* Thêm feature ngoài requirement.
* Refactor diện rộng.
* Thay đổi kiến trúc.
* Thay đổi API contract.
* Thay đổi authentication model.
* Thay đổi dependency.
* Thay đổi build tool.
* Thay đổi cấu hình CI/CD.
* Chỉnh sửa code không liên quan.
* Thay đổi source code Backend trong task chỉ dành cho Frontend.
* Chỉnh sửa HTML template hoặc Postman collection nếu không nằm trong phạm vi.

Nếu Development Plan không còn phù hợp, AI Agent phải dừng, báo cáo, cập nhật plan và chờ phê duyệt lại.

---

## Bước 9: Tuân thủ TypeScript, ESLint và convention

AI Agent phải kiểm tra các file cấu hình hiện có, bao gồm khi tồn tại:

```text
package.json
eslint.config.js
eslint.config.ts
.eslintrc
.eslintrc.json
.prettierrc
.prettierrc.json
tsconfig.json
tsconfig.app.json
tsconfig.node.json
vite.config.ts
```

AI Agent phải tuân thủ:

* TypeScript rule.
* React rule.
* React Hooks rule.
* React Refresh rule.
* Naming convention.
* Import convention.
* Formatting convention.
* File organization convention.
* Path alias convention.

Không được:

* Vô hiệu hóa rule mà không có lý do kỹ thuật.
* Thêm `eslint-disable` tùy tiện.
* Sử dụng `@ts-ignore` để che lỗi.
* Sử dụng `@ts-nocheck`.
* Sửa cấu hình lint để hợp thức hóa code sai.
* Làm yếu TypeScript configuration nếu chưa được phê duyệt.

---

## Bước 10: Kiểm tra chất lượng

Trước khi trả kết quả cuối cùng, AI Agent phải đọc:

```text
package.json
```

và xác định package manager dựa trên lock file:

```text
package-lock.json
pnpm-lock.yaml
yarn.lock
bun.lock
bun.lockb
```

Không được tự giả định project dùng `npm`, `pnpm`, `yarn` hoặc `bun`.

AI Agent phải chạy các script có sẵn tương ứng với:

1. ESLint.
2. TypeScript type-check.
3. Formatting check, nếu có.
4. Unit hoặc component test liên quan.
5. Production build.
6. End-to-end test liên quan, nếu có.
7. Quality hoặc report task khác trong `package.json`.

Khi API Backend có thể chạy trong môi trường hiện tại, AI Agent có thể dùng request trong Postman collection để kiểm tra contract, nhưng không được dùng việc gọi API thủ công thay cho test Frontend cần thiết.

AI Agent phải:

* Sửa lỗi phát sinh trong phạm vi thay đổi.
* Chạy lại kiểm tra sau khi sửa.
* Ghi nhận chính xác lệnh đã chạy.
* Ghi nhận kết quả cuối cùng.
* Không bỏ qua lỗi chỉ để hoàn thành nhanh.
* Không xóa hoặc làm yếu test để khiến kiểm tra pass.
* Không báo cáo thành công khi lint, type-check, test hoặc build thất bại.

Nếu lỗi đến từ code cũ ngoài phạm vi, AI Agent phải phân biệt, cung cấp bằng chứng và báo cáo rõ ảnh hưởng.

---

## Bước 11: Giới hạn vòng lặp code và debug

Số vòng lặp tối đa cho quá trình:

* Thay đổi code.
* Chạy lint.
* Chạy type-check.
* Chạy test.
* Chạy build.
* Kiểm tra API.
* Phân tích lỗi.
* Sửa lỗi.
* Chạy lại.

là **10 vòng lặp**.

Một vòng lặp được tính theo chuỗi:

```text
Thay đổi code
→ Chạy kiểm tra
→ Phát hiện lỗi
→ Phân tích nguyên nhân
→ Sửa code
→ Chạy lại
```

Nếu sau tối đa 10 vòng lặp vẫn chưa giải quyết được vấn đề, AI Agent phải dừng và báo cáo:

1. Số vòng lặp đã thực hiện.
2. Vấn đề còn tồn tại.
3. Lỗi hoặc log quan trọng.
4. Nguyên nhân đã xác định.
5. Các phương án đã thử.
6. Kết quả của từng phương án.
7. Lý do chưa thể giải quyết.
8. Thông tin còn thiếu.
9. Những thay đổi đã hoàn thành.
10. Những phần chưa hoàn thành.
11. Đề xuất bước tiếp theo.

Không được thử ngẫu nhiên sau vòng thứ 10 hoặc báo cáo thành công khi chưa có kết quả xác nhận.

---

## Bước 12: Cập nhật Development Plan

Sau implementation, AI Agent phải cập nhật Development Plan để ghi nhận:

* Nội dung đã hoàn thành.
* File thực tế đã thay đổi.
* Thay đổi so với plan ban đầu.
* API thực tế đã tích hợp.
* Test và kiểm tra đã chạy.
* Limitation hoặc risk còn lại.
* Trạng thái hoàn thành.

Đồng thời phải cập nhật:

```text
.claude/dev_plan/DevPlanSummary-FE.md
```

Không được thay đổi lịch sử hoặc attribution của agent khác.

---

## Bước 13: Báo cáo kết quả

AI Agent phải báo cáo theo cấu trúc:

```text
# Báo cáo kết quả

## 1. Tóm tắt
- Feature/module đã triển khai:
- Requirement đã đáp ứng:
- Output đạt được:

## 2. Tài liệu và tài nguyên đã kiểm tra
- Project rule:
- Workflow:
- Frontend skill:
- Application Context:
- Frontend API Guide:
- Tài liệu module:
- HTML template:
- Postman collection:
- Development Plan:

## 3. Thay đổi mã nguồn

| File  | Loại thay đổi   | Nội dung chính |
| ----- | --------------- | -------------- |
| `...` | Tạo mới/Sửa/Xóa | ...            |

## 4. API đã tích hợp

| Method | Endpoint | Mục đích |
| ------ | -------- | -------- |
| `...`  | `...`    | ...      |

## 5. Kiểm tra chất lượng

### ESLint
- Lệnh:
- Kết quả:
- Số lỗi:

### TypeScript
- Lệnh:
- Kết quả:
- Số lỗi:

### Formatting
- Lệnh:
- Kết quả:

### Test
- Lệnh:
- Kết quả:
- Số test pass:
- Số test fail:

### Production build
- Lệnh:
- Kết quả:

### API verification
- Nguồn request:
- Lệnh hoặc công cụ:
- Kết quả:

### Kiểm tra khác
- Lệnh:
- Kết quả:

## 6. Đối chiếu Development Plan
- Nội dung đã hoàn thành:
- Nội dung chưa hoàn thành:
- Thay đổi phát sinh:
- Lý do:

## 7. Vấn đề còn tồn tại
- Không có.

Hoặc:

- Mô tả:
- Nguyên nhân:
- Ảnh hưởng:
- Hướng xử lý đề xuất:

## 8. Giới hạn và rủi ro
- ...

## 9. Số vòng lặp code-debug
- Tổng số vòng lặp:
```

---

# 6. Thứ tự ưu tiên

Khi có xung đột, áp dụng thứ tự ưu tiên:

1. Chỉ dẫn trực tiếp mới nhất của người dùng.
2. Rule của Frontend.
3. Development Plan FE đã được phê duyệt.
4. `WORKFLOW.md` của Frontend.
5. `Frontend_API_Guide.md`.
6. `ApplicationContext.md`.
7. Tài liệu module trong Backend `.claude/docs`.
8. HTML template hoặc wireframe đã được phê duyệt.
9. Postman collection.
10. Cấu hình TypeScript, ESLint, test và build.
11. Convention hoặc pattern hiện có trong source code.

Postman collection không được dùng để ghi đè requirement hoặc response contract trong tài liệu.

Nếu có mâu thuẫn không thể giải quyết, AI Agent phải báo cáo chính xác điểm mâu thuẫn, nêu các phương án và chờ người dùng quyết định.

---

# 7. Checklist bắt buộc

Trước khi trả kết quả cuối cùng, AI Agent phải xác nhận:

* [ ] Đã chạy `pwd` và `ls`.
* [ ] Đã xác nhận đang làm việc trong repository FE.
* [ ] Đã đọc rule của Frontend.
* [ ] Đã đọc `WORKFLOW.md`.
* [ ] Đã đọc Frontend skill.
* [ ] Đã đọc `Frontend_API_Guide.md`.
* [ ] Đã đọc `ApplicationContext.md`.
* [ ] Đã đọc tài liệu module liên quan.
* [ ] Đã kiểm tra HTML template liên quan.
* [ ] Đã kiểm tra Postman collection khi task có tích hợp API.
* [ ] Đã kiểm tra source code Frontend hiện tại.
* [ ] Đã xác định Development Plan.
* [ ] Development Plan đã được phê duyệt.
* [ ] Đã cập nhật `DevPlanSummary-FE.md` khi cần.
* [ ] Đã báo cáo cấu trúc hiện tại.
* [ ] Đã báo cáo phạm vi ảnh hưởng.
* [ ] Đã báo cáo API liên quan.
* [ ] Đã báo cáo output dự kiến.
* [ ] Đã tuân thủ Development Plan.
* [ ] Không có thay đổi ngoài phạm vi được phê duyệt.
* [ ] Không có thay đổi ngoài ý muốn trong repository BE.
* [ ] Không chỉnh sửa HTML template hoặc Postman collection ngoài phạm vi.
* [ ] Code tuân thủ TypeScript và ESLint.
* [ ] Không sử dụng `any` hoặc suppression không được giải thích.
* [ ] Đã xử lý loading, error, empty, disabled và success state khi phù hợp.
* [ ] Đã xác định đúng package manager.
* [ ] Đã chạy ESLint.
* [ ] Đã chạy TypeScript type-check.
* [ ] Đã chạy formatting check nếu có.
* [ ] Đã chạy test liên quan.
* [ ] Đã chạy production build.
* [ ] Đã đối chiếu API với Postman collection khi phù hợp.
* [ ] Số vòng lặp code-debug không vượt quá 10.
* [ ] Đã cập nhật Development Plan sau implementation.
* [ ] Đã báo cáo rõ các vấn đề còn tồn tại.
