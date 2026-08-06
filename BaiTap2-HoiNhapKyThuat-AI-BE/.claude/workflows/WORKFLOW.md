# WORKFLOW.md

## 1. Mục đích

Tài liệu này quy định quy trình bắt buộc mà AI Agent phải tuân thủ mỗi khi nhận yêu cầu tạo mới, chỉnh sửa, sửa lỗi, refactor hoặc bổ sung mã nguồn trong project.

Nguồn tài liệu chính của project nằm tại:

```text
.claude/docs
```

AI Agent phải đọc và sử dụng các tài liệu liên quan trong thư mục này trước khi lập kế hoạch hoặc thực hiện thay đổi mã nguồn.

---

# 2. Quy trình xử lý yêu cầu viết code

Mỗi khi nhận yêu cầu liên quan đến code, AI Agent phải thực hiện các bước theo đúng thứ tự dưới đây.

## Bước 1: Đọc RULE và tài liệu project

Trước khi phân tích hoặc thay đổi mã nguồn, AI Agent phải:

1. Xác định và đọc toàn bộ nội dung của `RULE.md`.
2. Tuân thủ tất cả quy định trong `RULE.md`.
3. Kiểm tra thư mục:

```text
.claude/docs
```

4. Đọc các tài liệu liên quan trực tiếp đến feature, module hoặc yêu cầu đang được thực hiện.
5. Xác định:

   * Requirement liên quan.
   * Kiến trúc hiện tại.
   * Module hoặc component bị ảnh hưởng.
   * Developer Plan hiện có.
   * Các quy ước kỹ thuật và coding convention của project.

Không được tự ý giả định requirement nếu thông tin đã tồn tại trong tài liệu hoặc mã nguồn.

---

## Bước 2: Kiểm tra Developer Plan

AI Agent phải kiểm tra xem feature hoặc module đang được yêu cầu đã có Developer Plan được phê duyệt hay chưa.

### Trường hợp chưa có Developer Plan

Nếu chưa tìm thấy Developer Plan hoặc Developer Plan chưa được người dùng phê duyệt, AI Agent phải:

1. Phân tích:

   * Requirement.
   * Kiến trúc hiện tại.
   * Luồng xử lý liên quan.
   * Các module và file có khả năng bị ảnh hưởng.
   * Rủi ro kỹ thuật.
   * Output dự kiến.

2. Tạo Developer Plan bao gồm tối thiểu:

   * Mục tiêu.
   * Requirement liên quan.
   * Phạm vi thực hiện.
   * Phạm vi không thực hiện.
   * Kiến trúc hiện tại.
   * Thiết kế hoặc phương án triển khai.
   * Luồng xử lý.
   * Danh sách module bị ảnh hưởng.
   * Danh sách file dự kiến tạo mới hoặc chỉnh sửa.
   * Vị trí dự kiến thay đổi trong từng file.
   * Thay đổi database hoặc API nếu có.
   * Unit test dự kiến.
   * Rủi ro và phương án xử lý.
   * Output dự kiến.

3. Trình bày Developer Plan cho người dùng.

4. Chờ người dùng phê duyệt rõ ràng.

Khi Developer Plan chưa được phê duyệt:

* Không được tạo hoặc chỉnh sửa mã nguồn.
* Không được tự ý triển khai một phần của feature.
* Không được tự ý chọn phương án kỹ thuật thay cho người dùng.

---

### Trường hợp đã có Developer Plan được phê duyệt

Nếu Developer Plan đã tồn tại và được phê duyệt, AI Agent phải:

1. Đọc Developer Plan.
2. Đối chiếu Developer Plan với requirement và kiến trúc hiện tại.
3. Xác nhận Developer Plan vẫn phù hợp với trạng thái mã nguồn hiện tại.
4. Báo cáo nội dung theo Bước 3.
5. Chờ người dùng xác nhận trước khi bắt đầu code nếu `RULE.md` yêu cầu phê duyệt ở bước này.

Không được tự ý thay đổi phương án kỹ thuật, kiến trúc hoặc phạm vi đã được phê duyệt.

---

## Bước 3: Báo cáo trước khi code

Khi đã có Developer Plan được phê duyệt, AI Agent phải báo cáo rõ ràng trước khi tiến hành code.

Báo cáo phải bao gồm các nội dung sau.

### 3.1. Cấu trúc hiện tại

Mô tả:

* Kiến trúc hiện tại của module hoặc hệ thống liên quan.
* Các module, service hoặc component liên quan.
* Luồng xử lý hiện tại.
* Các layer bị ảnh hưởng.
* Dependency quan trọng.
* Các điểm tích hợp với module khác.

---

### 3.2. Phạm vi ảnh hưởng

Mô tả:

* Feature hoặc module sẽ được triển khai.
* Các thành phần bị ảnh hưởng.
* Các file dự kiến tạo mới.
* Các file dự kiến chỉnh sửa.
* Các class, interface, component, method hoặc function dự kiến thay đổi.
* Các API, database, message queue hoặc hệ thống bên ngoài bị ảnh hưởng, nếu có.
* Các rủi ro hoặc ảnh hưởng đến chức năng hiện tại.

Phạm vi thay đổi phải tuân thủ Developer Plan và requirement đã được phê duyệt.

---

### 3.3. Output dự kiến

Mô tả rõ kết quả sau khi hoàn thành, bao gồm khi phù hợp:

* API request.
* API response.
* Dữ liệu được tạo, cập nhật hoặc trả về.
* Hành vi của hệ thống.
* Luồng xử lý mới.
* Output trên giao diện.
* Trường hợp thành công.
* Trường hợp lỗi.
* Các exception hoặc validation quan trọng.

---

### 3.4. Mẫu báo cáo

AI Agent phải sử dụng cấu trúc sau:

```text
## Báo cáo trước khi triển khai

### 1. Cấu trúc hiện tại
- ...

### 2. Feature/module sẽ thực hiện
- ...

### 3. Phạm vi ảnh hưởng

| Thành phần | Loại ảnh hưởng    | Nội dung thay đổi |
| ---------- | ----------------- | ----------------- |
| `...`      | Tạo mới/Chỉnh sửa | ...               |

### 4. Các file dự kiến thay đổi

| File  | Loại thay đổi     | Vị trí           | Nội dung |
| ----- | ----------------- | ---------------- | -------- |
| `...` | Tạo mới/Chỉnh sửa | `Class.method()` | ...      |

### 5. Output dự kiến
- ...

### 6. Rủi ro hoặc lưu ý
- ...

### 7. Trạng thái
- Chờ phê duyệt trước khi tiến hành code.
```

AI Agent chỉ được tiến hành code sau khi người dùng đã phê duyệt hoặc xác nhận theo quy định của `RULE.md`.

---

## Bước 4: Thực hiện code

Trong quá trình coding, AI Agent phải:

1. Tuyệt đối tuân thủ `RULE.md`.

2. Triển khai đúng Developer Plan đã được phê duyệt.

3. Chỉ thay đổi các file và khu vực nằm trong phạm vi đã được phê duyệt.

4. Không tự ý:

   * Thêm feature ngoài requirement.
   * Refactor diện rộng.
   * Thay đổi kiến trúc.
   * Thay đổi dependency.
   * Thay đổi cấu hình build.
   * Thay đổi cấu hình CI/CD.
   * Chỉnh sửa code không liên quan.

5. Tuân thủ coding convention của project.

6. Ưu tiên tái sử dụng code hiện có khi phù hợp.

7. Hạn chế code duplication.

8. Đảm bảo code có khả năng đọc, bảo trì và mở rộng.

9. Xử lý lỗi phù hợp.

10. Đảm bảo không tạo rủi ro bảo mật hoặc suy giảm hiệu năng không cần thiết.

Nếu phát hiện Developer Plan không còn phù hợp trong quá trình coding, AI Agent phải:

1. Dừng thay đổi liên quan.
2. Báo cáo nguyên nhân.
3. Nêu ảnh hưởng.
4. Đề xuất phương án cập nhật.
5. Chờ người dùng phê duyệt phương án mới.

---

## Bước 5: Tuân thủ Checkstyle

Trong quá trình viết và chỉnh sửa mã nguồn, AI Agent phải tuân thủ toàn bộ code format và coding convention được cấu hình tại:

```text
config/checkstyle/checkstyle.xml
```

AI Agent phải:

1. Đọc file cấu hình Checkstyle trước khi hoàn tất code.

2. Áp dụng các quy tắc liên quan đến:

   * Format.
   * Indentation.
   * Naming.
   * Import.
   * Whitespace.
   * Độ dài dòng.
   * Cấu trúc class và method.
   * Các convention khác được định nghĩa trong cấu hình.

3. Không tự ý bỏ qua hoặc vô hiệu hóa rule.

4. Không sử dụng suppression nếu chưa có lý do kỹ thuật hợp lý.

5. Không trả output khi các lỗi Checkstyle liên quan đến phần code đã thay đổi vẫn chưa được xử lý.

---

## Bước 6: Kiểm tra Checkstyle và PMD

Trước khi trả output cuối cùng, AI Agent phải chạy:

1. Checkstyle.
2. PMD.
3. Các kiểm tra hoặc test liên quan đến phần code đã thay đổi, nếu có.

AI Agent phải:

* Sửa các lỗi phát hiện được trong phạm vi thay đổi.
* Chạy lại các công cụ sau khi sửa.
* Xác nhận kết quả kiểm tra cuối cùng.
* Không bỏ qua lỗi chỉ để hoàn thành nhanh.

Nếu lỗi đến từ code cũ ngoài phạm vi thay đổi, AI Agent phải:

1. Phân biệt rõ lỗi cũ và lỗi được tạo bởi thay đổi hiện tại.
2. Không tự ý refactor code ngoài phạm vi.
3. Báo cáo lỗi đó trong phần lưu ý nếu ảnh hưởng đến việc kiểm tra.
4. Không coi lỗi cũ ngoài phạm vi là lỗi của phần triển khai hiện tại.

---

## Bước 7: Giới hạn vòng lặp code và debug

Số vòng lặp tối đa cho quá trình:

* Viết code.
* Build.
* Chạy test.
* Phân tích lỗi.
* Sửa lỗi.
* Chạy lại kiểm tra.

là **10 vòng lặp**.

Một vòng lặp được tính khi AI Agent thực hiện chuỗi:

```text
Thay đổi code
→ Build hoặc chạy kiểm tra
→ Phát hiện lỗi
→ Phân tích nguyên nhân
→ Sửa code
→ Chạy lại
```

AI Agent phải theo dõi số vòng lặp đã sử dụng.

Nếu sau tối đa 10 vòng lặp vẫn không thể giải quyết vấn đề, AI Agent phải dừng việc thử nghiệm lặp lại và báo cáo:

1. Số vòng lặp đã thực hiện.
2. Lỗi hoặc vấn đề còn tồn tại.
3. Nguyên nhân đã xác định.
4. Các phương án đã thử.
5. Kết quả của từng phương án quan trọng.
6. Lý do chưa thể giải quyết.
7. Các thông tin còn thiếu.
8. Đề xuất bước tiếp theo.
9. Những thay đổi đã được thực hiện.
10. Những phần chưa hoàn thành.

Không được:

* Tiếp tục thử ngẫu nhiên sau vòng lặp thứ 10.
* Che giấu lỗi.
* Báo cáo thành công khi build, test, Checkstyle hoặc PMD vẫn thất bại.
* Khẳng định lỗi đã được sửa khi chưa có kết quả kiểm tra xác nhận.

---

## Bước 8: Báo cáo kết quả

Sau khi hoàn thành, AI Agent phải báo cáo:

### 1. Tóm tắt

* Feature hoặc module đã triển khai.
* Requirement đã đáp ứng.
* Output đạt được.

### 2. Thay đổi mã nguồn

| File  | Loại thay đổi     | Nội dung chính |
| ----- | ----------------- | -------------- |
| `...` | Tạo mới/Chỉnh sửa | ...            |

### 3. Kiểm tra chất lượng

* Checkstyle:

  * Kết quả:
  * Số lỗi:
* PMD:

  * Kết quả:
  * Số lỗi:
* Build:

  * Kết quả:
* Unit test:

  * Kết quả:
* Các kiểm tra khác:

  * Kết quả:

### 4. Đối chiếu Developer Plan

* Nội dung đã hoàn thành:
* Nội dung chưa hoàn thành:
* Thay đổi phát sinh:
* Lý do:

### 5. Vấn đề còn tồn tại

* Không có.

Hoặc:

* Mô tả vấn đề.
* Nguyên nhân.
* Ảnh hưởng.
* Hướng xử lý đề xuất.

---

# 3. Thứ tự ưu tiên

Khi có xung đột giữa các tài liệu hoặc chỉ dẫn, áp dụng thứ tự ưu tiên sau:

1. Chỉ dẫn trực tiếp mới nhất của người dùng.
2. `RULE.md`.
3. Developer Plan đã được phê duyệt.
4. `WORKFLOW.md`.
5. Tài liệu trong `.claude/docs`.
6. Coding convention và cấu hình Checkstyle/PMD.
7. Convention hoặc pattern hiện có trong mã nguồn.

Nếu không thể xác định cách xử lý do tài liệu mâu thuẫn, AI Agent phải báo cáo điểm mâu thuẫn và yêu cầu người dùng quyết định.

---

# 4. Checklist bắt buộc

Trước khi trả kết quả cuối cùng, AI Agent phải xác nhận:

* [ ] Đã đọc `RULE.md`.
* [ ] Đã kiểm tra tài liệu trong `.claude/docs`.
* [ ] Đã xác định Developer Plan.
* [ ] Developer Plan đã được phê duyệt.
* [ ] Đã báo cáo cấu trúc hiện tại.
* [ ] Đã báo cáo phạm vi ảnh hưởng.
* [ ] Đã báo cáo output dự kiến.
* [ ] Đã tuân thủ `RULE.md`.
* [ ] Đã tuân thủ Developer Plan.
* [ ] Đã đọc `config/checkstyle/checkstyle.xml`.
* [ ] Code tuân thủ Checkstyle.
* [ ] Đã chạy Checkstyle.
* [ ] Đã chạy PMD.
* [ ] Đã chạy build hoặc kiểm tra liên quan.
* [ ] Đã chạy unit test phù hợp.
* [ ] Số vòng lặp code-debug không vượt quá 10.
* [ ] Đã báo cáo rõ các vấn đề còn tồn tại.
* [ ] Không có thay đổi ngoài phạm vi được phê duyệt.
