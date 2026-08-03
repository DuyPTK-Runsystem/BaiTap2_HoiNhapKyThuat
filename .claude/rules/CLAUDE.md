# RULE.md

## 1. Mục đích

Tài liệu này quy định các nguyên tắc bắt buộc mà AI Agent phải tuân thủ khi phân tích, lập kế hoạch, triển khai, chỉnh sửa hoặc đánh giá mã nguồn trong repository.

Mọi yêu cầu trong tài liệu này có tính bắt buộc, trừ khi người dùng đưa ra chỉ dẫn mới ghi đè rõ ràng lên một quy định cụ thể.

---

## 2. Quy trình bắt buộc trước khi thực hiện code

Trước khi tạo mới, chỉnh sửa hoặc xóa bất kỳ mã nguồn nào, AI Agent phải thực hiện các bước sau.

### 2.1. Đọc tài liệu và phân tích hệ thống hiện tại

AI Agent phải kiểm tra và đọc các tài liệu liên quan đến tính năng hoặc module cần triển khai, bao gồm nhưng không giới hạn:

* Software Requirements Specification (SRS)
* Business Requirements (BR)
* Functional Requirements
* Technical Design
* Architecture Document
* API Specification
* Database Design
* Developer Plan
* Các tài liệu kỹ thuật khác có liên quan

AI Agent phải phân tích kiến trúc và mã nguồn hiện tại trước khi đề xuất giải pháp.

Không được tự ý giả định kiến trúc, luồng xử lý, cấu trúc module hoặc yêu cầu nghiệp vụ nếu thông tin có thể được xác định từ tài liệu hoặc mã nguồn hiện có.

---

### 2.2. Kiểm tra Developer Plan

Trước khi bắt đầu code, AI Agent phải kiểm tra xem module hoặc feature đang được yêu cầu đã có **Developer Plan được phê duyệt** hay chưa.

#### Trường hợp đã có Developer Plan được phê duyệt

AI Agent phải:

1. Đọc toàn bộ phần Developer Plan liên quan.
2. Đảm bảo phương án triển khai tuân thủ Developer Plan đã được phê duyệt.
3. Không tự ý thay đổi kiến trúc, phạm vi hoặc phương án kỹ thuật đã được phê duyệt.
4. Báo cáo nội dung theo quy định tại phần **2.3** trước khi tiến hành code.

#### Trường hợp chưa có Developer Plan

AI Agent phải:

1. Phân tích requirement, kiến trúc hiện tại và các thành phần bị ảnh hưởng.
2. Tạo Developer Plan cho module hoặc feature.
3. Trình bày Developer Plan để người dùng xem xét và phê duyệt.
4. Chỉ được bắt đầu code sau khi Developer Plan đã được người dùng phê duyệt rõ ràng.

Không được tự ý triển khai code khi chưa có Developer Plan được phê duyệt.

---

### 2.3. Báo cáo bắt buộc trước khi code

Kể cả khi Developer Plan và các tài liệu kỹ thuật đã được phê duyệt, AI Agent vẫn phải báo cáo cho người dùng trước khi thực hiện code.

Báo cáo phải bao gồm đầy đủ các nội dung sau:

#### 1. Kiến trúc hiện tại

Mô tả:

* Các module hoặc service liên quan.
* Luồng xử lý hiện tại.
* Các layer hoặc component bị ảnh hưởng.
* Các dependency quan trọng.
* Các điểm tích hợp với module hoặc hệ thống khác.

#### 2. Feature hoặc module sẽ triển khai

Mô tả:

* Mục tiêu của feature hoặc module.
* Chức năng sẽ được thêm mới hoặc thay đổi.
* Phạm vi thực hiện.
* Các hành vi dự kiến sau khi hoàn thành.

#### 3. Vị trí đặc tả trong tài liệu

Chỉ rõ:

* Tên tài liệu.
* Đường dẫn hoặc vị trí tài liệu nếu có.
* Tên chương, mục hoặc section.
* Mã requirement, user story, use case hoặc mã đặc tả nếu có.
* Nội dung requirement tương ứng.

Không được chỉ ghi chung chung như “theo tài liệu” mà phải xác định được vị trí cụ thể của đặc tả.

#### 4. Danh sách file bị ảnh hưởng

Liệt kê các file dự kiến:

* Tạo mới.
* Chỉnh sửa.
* Xóa hoặc thay thế, nếu có.

Mỗi file phải kèm theo mục đích thay đổi.

#### 5. Vị trí thay đổi trong từng file

Đối với mỗi file, cần mô tả:

* Class, interface, component hoặc module bị ảnh hưởng.
* Method, function hoặc khu vực dự kiến chỉnh sửa.
* Nội dung sẽ được thêm hoặc thay đổi.
* Lý do thực hiện thay đổi.

#### 6. Output dự kiến

Mô tả kết quả đầu ra của feature hoặc module, bao gồm khi phù hợp:

* API request và response.
* Dữ liệu được tạo, cập nhật hoặc trả về.
* Thay đổi trên giao diện.
* Hành vi của hệ thống.
* Luồng xử lý sau khi triển khai.
* Các trường hợp lỗi hoặc exception quan trọng.

---

### 2.4. Mẫu báo cáo trước khi code

AI Agent phải sử dụng cấu trúc sau:

```text
## Báo cáo trước khi triển khai

### 1. Kiến trúc hiện tại
- ...

### 2. Feature/module sẽ triển khai
- ...

### 3. Đặc tả trong tài liệu
- Tài liệu:
- Chương/mục:
- Requirement/User Story:
- Nội dung liên quan:

### 4. Các file dự kiến thay đổi

| File | Loại thay đổi | Mục đích |
|---|---|---|
| `...` | Tạo mới/Chỉnh sửa/Xóa | ... |

### 5. Vị trí thay đổi

| File | Class/Component | Method/Khu vực | Nội dung thay đổi |
|---|---|---|---|
| `...` | `...` | `...` | ... |

### 6. Output dự kiến
- ...

### 7. Rủi ro hoặc ảnh hưởng
- ...

### 8. Trạng thái
- Chờ người dùng xác nhận trước khi tiến hành code.
```

---

## 3. Quy định trong quá trình code

### 3.1. Tuân thủ Developer Plan

AI Agent phải:

* Triển khai đúng phạm vi đã được phê duyệt.
* Không tự ý thêm chức năng ngoài requirement.
* Không tự ý refactor các khu vực không liên quan.
* Không thay đổi kiến trúc đã được phê duyệt nếu chưa có sự đồng ý của người dùng.
* Báo cáo và xin phê duyệt lại nếu phát hiện Developer Plan không còn phù hợp.

Nếu trong quá trình triển khai phát hiện yêu cầu mâu thuẫn với kiến trúc hiện tại, AI Agent phải dừng phần thay đổi bị ảnh hưởng và báo cáo vấn đề.

---

### 3.2. Quy ước mã nguồn

Người dùng sẽ cung cấp cấu hình hoặc thông tin liên quan đến:

* PMD
* Checkstyle
* Các coding convention khác

Sau khi được cung cấp, AI Agent phải:

* Đọc và áp dụng các quy tắc tương ứng.
* Viết code tuân thủ convention của dự án.
* Không tự ý bỏ qua các quy tắc của PMD hoặc Checkstyle.
* Không sử dụng annotation hoặc cấu hình suppress warning nếu chưa có lý do kỹ thuật hợp lý.
* Báo cáo các rule không thể đáp ứng cùng nguyên nhân và phương án xử lý.

---

### 3.3. Chất lượng mã nguồn

Mã nguồn được tạo hoặc chỉnh sửa phải:

* Đúng requirement.
* Phù hợp với kiến trúc hiện tại.
* Dễ đọc và dễ bảo trì.
* Hạn chế trùng lặp mã nguồn.
* Không tạo dependency không cần thiết.
* Có xử lý lỗi phù hợp.
* Đảm bảo hiệu năng phù hợp với ngữ cảnh.
* Đảm bảo các yêu cầu bảo mật liên quan.
* Không làm thay đổi hành vi của các chức năng ngoài phạm vi.

---

## 4. Quy định về phạm vi thay đổi

AI Agent chỉ được thay đổi các file và khu vực cần thiết để hoàn thành feature hoặc module đã được phê duyệt.

Không được:

* Refactor diện rộng ngoài phạm vi.
* Đổi tên hàng loạt nếu không cần thiết.
* Thay đổi format toàn bộ file chỉ để chỉnh sửa một phần nhỏ.
* Chỉnh sửa code cũ không liên quan.
* Tự ý cập nhật dependency.
* Tự ý thay đổi cấu hình build hoặc CI/CD.

Nếu cần thay đổi ngoài phạm vi ban đầu, AI Agent phải:

1. Giải thích lý do.
2. Mô tả ảnh hưởng.
3. Cập nhật Developer Plan.
4. Chờ người dùng phê duyệt.

---

## 5. Báo cáo sau khi hoàn thành

Sau khi hoàn thành triển khai, AI Agent phải báo cáo:

* Các file đã tạo hoặc chỉnh sửa.
* Các thay đổi chính trong từng file.
* Các requirement đã đáp ứng.
* Output hoặc hành vi đạt được.
* Unit test hoặc test case đã thêm/chỉnh sửa.
* Kết quả kiểm tra PMD, Checkstyle hoặc các công cụ liên quan nếu có.
* Các vấn đề còn tồn tại hoặc giới hạn hiện tại.

Không được báo cáo hoàn thành nếu chưa kiểm tra mức độ phù hợp giữa code và Developer Plan.
