# Skill: Review Pull Request

## Mục đích

Skill này dùng để review **chỉ nội dung của một Pull Request (PR)** và tạo báo cáo review bằng tiếng Việt. Review phải tập trung vào các thay đổi thuộc PR, không đánh giá các vấn đề đã tồn tại trong code cũ nếu chúng nằm ngoài phạm vi diff của PR.

## Điều kiện sử dụng

Sử dụng skill khi người dùng yêu cầu review PR hoặc cung cấp một trong các loại đầu vào sau:

- PR URL hoặc PR number.
- Commit range của PR.
- Patch/diff của PR.
- Danh sách file và nội dung thay đổi thuộc PR.

Nếu chưa có dữ liệu PR, không được tự suy đoán nội dung thay đổi và không được review diff local của workspace. Hãy yêu cầu người dùng cung cấp đầu vào PR phù hợp.

## Nguyên tắc bắt buộc

1. Chỉ kiểm tra các file, dòng code và hành vi được thay đổi trong PR.
2. Không dùng diff local của workspace làm nguồn thay thế cho diff PR.
3. Có thể đọc code xung quanh diff để hiểu ngữ cảnh, nhưng chỉ báo cáo issue nếu:
   - Issue do thay đổi trong PR gây ra; hoặc
   - Issue cũ được PR chạm tới và việc thay đổi làm lộ rõ hoặc ảnh hưởng trực tiếp đến issue đó.
4. Không báo cáo issue hoàn toàn nằm ngoài scope diff của PR.
5. Mọi nội dung báo cáo phải viết bằng tiếng Việt, bao gồm tiêu đề, mô tả, giải pháp và kết luận.
6. Mỗi issue phải kèm giải pháp cụ thể, khả thi và phù hợp với phạm vi PR.
7. Không tự ý mở rộng requirement hoặc đề xuất refactor không cần thiết.
8. Phân biệt rõ:
   - `CRITICAL`: Có thể gây mất dữ liệu, lỗ hổng bảo mật nghiêm trọng, lỗi production diện rộng hoặc làm chức năng chính không thể sử dụng.
   - `MAJOR`: Lỗi chức năng, vi phạm requirement, lỗi hiệu năng/bảo mật đáng kể hoặc thiếu test quan trọng có khả năng gây regression.
   - `MINOR`: Vấn đề chất lượng, khả năng bảo trì, duplicate nhỏ, convention hoặc tối ưu chưa nghiêm trọng.
9. Chỉ tạo issue khi có bằng chứng từ diff PR hoặc từ mối liên hệ trực tiếp với thay đổi trong PR. Nếu không chắc chắn, nêu rõ mức độ không chắc chắn hoặc không báo cáo issue.

## Quy trình review

### 1. Xác định nguồn dữ liệu PR

Ghi nhận chính xác nguồn review:

- PR number/URL hoặc commit range.
- Base branch và head branch nếu có.
- Danh sách file thuộc PR.
- Nội dung diff PR.

Nếu công cụ cung cấp cả thay đổi local và thay đổi PR, chỉ sử dụng dữ liệu PR. Không chạy review trên toàn bộ working tree để thay thế cho PR diff.

### 2. Hiểu requirement và phạm vi

Đọc description, acceptance criteria và các tài liệu đặc tả liên quan đến PR. Xác định:

- PR cần giải quyết vấn đề gì.
- Hành vi nào được thêm, sửa hoặc xóa.
- File/module nào nằm trong scope.
- Những thay đổi nào là ngoài scope.

Nếu requirement không đủ rõ, ghi nhận giới hạn này trong báo cáo thay vì tự giả định.

### 3. Kiểm tra code quality và correctness

Đối với từng thay đổi trong diff, kiểm tra:

- Logic có đúng requirement và các edge case không.
- Có thể phát sinh lỗi runtime, exception không được xử lý, nullability hoặc race condition không.
- Có làm thay đổi ngược hành vi ngoài scope không.
- Có xử lý input không hợp lệ, boundary value và lỗi downstream không.
- Có vi phạm contract API, transaction, persistence hoặc tương thích ngược không.
- Có duplicate code hoặc abstraction không cần thiết do PR tạo ra không.
- Code đã tối ưu hợp lý chưa; chú ý N+1 query, vòng lặp dư thừa, truy vấn lặp, I/O không cần thiết, xử lý dữ liệu quá lớn và việc dùng tài nguyên.

Không coi việc khác style cá nhân là issue nếu không vi phạm convention hoặc gây ảnh hưởng thực tế.

### 4. Kiểm tra security

Kiểm tra các thay đổi liên quan đến:

- Authentication, authorization và kiểm tra quyền truy cập tài nguyên.
- Validation và việc tin tưởng dữ liệu từ client.
- Injection (SQL, command, template, expression), XSS, SSRF và path traversal.
- Lộ secret, token, thông tin cá nhân hoặc dữ liệu nhạy cảm qua log/API/error message.
- Mật khẩu, credential, session, cookie và cấu hình CORS/CSRF.
- Upload/download file, deserialize dữ liệu và giới hạn tài nguyên.

Chỉ báo cáo lỗ hổng nếu thay đổi trong PR tạo ra hoặc làm tăng khả năng khai thác.

### 5. Kiểm tra hook rule và ESLint

Nếu PR có JavaScript/TypeScript/React hoặc framework có hook:

- Kiểm tra Rules of Hooks: chỉ gọi hook ở top-level của function component hoặc custom hook.
- Không gọi hook trong condition, loop, nested function hoặc callback không phù hợp.
- Kiểm tra dependency array của `useEffect`, `useMemo`, `useCallback` và các hook tương tự.
- Kiểm tra stale closure, cleanup và khả năng chạy effect ngoài ý muốn.
- Đối chiếu với ESLint config và kết quả lint nếu có trong PR/CI.
- Không báo cáo lỗi ESLint chỉ dựa trên suy đoán khi chưa có cấu hình hoặc bằng chứng phù hợp; có thể ghi rõ cần chạy lint để xác nhận.

Nếu repository không dùng hook hoặc không có JavaScript/TypeScript trong diff, ghi nhận mục này là không áp dụng khi cần thiết.

### 6. Kiểm tra unit test

Xác định module hoặc behavior được thay đổi trong PR, sau đó:

1. Kiểm tra các module tương tự có unit test tương ứng hay không.
2. Kiểm tra PR đã cập nhật hoặc bổ sung đầy đủ test cho behavior mới/thay đổi chưa.
3. Đánh giá happy path, validation/error path và các edge case quan trọng.
4. Kiểm tra test có thực sự verify behavior hay chỉ tăng coverage hình thức.
5. Không yêu cầu test cho code không đổi hoặc thay đổi thuần formatting nếu không ảnh hưởng behavior.

Nếu các module tương tự có test nhưng PR không bổ sung test cần thiết, báo cáo theo mức độ ảnh hưởng thực tế và kèm test case/giải pháp đề xuất.

### 7. Đối chiếu phạm vi và tạo báo cáo

Đối chiếu toàn bộ file và thay đổi với requirement. Phát hiện các trường hợp:

- Thiếu phần triển khai cần thiết.
- Có thay đổi ngoài phạm vi.
- Có file hoặc dependency không liên quan bị chỉnh sửa.
- Có behavior mới không được mô tả trong requirement.

Mỗi PR phải được trình bày thành một phần riêng. Trong từng PR, nhóm issue theo thứ tự:

1. `CRITICAL`
2. `MAJOR`
3. `MINOR`

Không tạo heading cấp độ nếu không có issue ở cấp độ đó.

## Định dạng issue

Mỗi issue nên có cấu trúc:

```text
- [CRITICAL|MAJOR|MINOR] `đường/dẫn/file.ext:dòng`
  - Vấn đề: Mô tả chính xác lỗi và điều kiện xảy ra.
  - Ảnh hưởng: Nêu hậu quả kỹ thuật hoặc nghiệp vụ.
  - Giải pháp: Đề xuất cách sửa cụ thể.
  - Căn cứ scope: Chỉ rõ thay đổi nào trong PR gây ra issue.
```

Dòng phải trỏ đến vị trí trong diff PR nếu công cụ hỗ trợ. Không dùng vị trí chỉ có trong working tree local mà không thuộc PR.

## Định dạng output

File output mặc định:

```text
BaiTap2-HoiNhapKyThuat-AI-FE/.claude/reviews/review-fe-v1.md
```

Nếu thư mục chưa tồn tại, tạo thư mục `REVIEWs` trước khi ghi file. Không ghi đè báo cáo khác nếu người dùng không yêu cầu.

Mẫu báo cáo:

```markdown
# Review PR <số PR hoặc mã PR>

## Phạm vi review
- Nguồn dữ liệu: <PR URL/number/commit range>
- Chỉ review: diff của PR
- Không review: thay đổi local ngoài diff PR
- Requirement/tài liệu đối chiếu: <danh sách hoặc “Không được cung cấp”>

## CRITICAL

<!-- Liệt kê issue hoặc xóa section nếu không có -->

## MAJOR

<!-- Liệt kê issue hoặc xóa section nếu không có -->

## MINOR

<!-- Liệt kê issue hoặc xóa section nếu không có -->

## Kiểm tra unit test
- Module tương tự có test: <Có/Không/Không xác định>
- Test trong PR: <Đầy đủ/Thiếu/Không áp dụng/Không xác định>
- Chi tiết: <nhận xét và test case còn thiếu nếu có>

## Kết luận
- Kết quả: <Approve/Request changes/Không đủ dữ liệu để kết luận>
- Tóm tắt: <ngắn gọn, bằng tiếng Việt>
```

Nếu không phát hiện issue, ghi rõ `Không phát hiện issue thuộc phạm vi diff PR` dưới các mục phù hợp hoặc dùng một mục tổng kết rõ ràng. Không biến việc không có issue thành lý do để báo cáo các vấn đề cũ ngoài scope.

Nếu không có dữ liệu PR, không tạo kết luận “Approve”. Kết luận phải là `Không đủ dữ liệu để kết luận` và nêu rõ cần PR URL/number, commit range hoặc patch/diff.

## Tiêu chí hoàn thành

Review chỉ được coi là hoàn thành khi:

- Đã xác định nguồn dữ liệu là diff PR.
- Đã đối chiếu requirement và phạm vi thay đổi.
- Đã kiểm tra code quality, performance, security, hook/ESLint khi áp dụng.
- Đã kiểm tra sự đầy đủ của unit test so với các module tương tự.
- Mọi issue đều có cấp độ, vị trí, ảnh hưởng và giải pháp.
- Báo cáo hoàn toàn bằng tiếng Việt.
- Báo cáo được ghi vào `BaiTap2-HoiNhapKyThuat-AI-FE/.claude/reviews/review-fe-v1.md` theo yêu cầu.
