# Review PR #1

## Phạm vi review
- Nguồn dữ liệu: https://github.com/DuyPTK-Runsystem/BaiTap2_HoiNhapKyThuat/pull/1
- Base branch/base SHA: `master` / `b1f17613c118ebf60ac72f13e27a4ad073a0d0dc`
- Head branch/head SHA: `vocab` / `477ca5b6d2fffb018083a7c5922296c746faf9e6`
- Chỉ review: diff của PR #1 lấy từ GitHub connector và `git diff <base_sha> <head_sha>`
- Không review: thay đổi local ngoài diff PR
- Requirement/tài liệu đối chiếu: `.claude/docs`, `.claude/dev_plan`, workflow review PR

## MAJOR

- [MAJOR] `src/main/java/net/runsystem/duyptk/BaiTap2_HoiNhapKyThuat_AI/util/error/GlobalException.java:40`
  - Vấn đề: PR thay exception handler cũ cho `BadCredentialsException`/`AuthenticationException` bằng handler mới trả `HttpStatus.BAD_REQUEST` cho `BadCredentialsException`.
  - Ảnh hưởng: Các lỗi xác thực như login sai, refresh token không hợp lệ, hoặc không xác định được account sẽ trả HTTP 400 thay vì 401. Điều này làm sai contract auth phổ biến, có thể khiến client/interceptor không nhận diện phiên hết hạn hoặc credential sai để logout/refresh/redirect đúng cách.
  - Giải pháp: Tách handler cho `BadCredentialsException` và các `AuthenticationException` liên quan để trả `HttpStatus.UNAUTHORIZED`, giữ message phù hợp. Nếu vẫn muốn 400 cho lỗi payload login, chỉ dùng 400 cho validation/request format, không dùng cho authentication failure.
  - Căn cứ scope: PR xóa `GlobalExceptionHandler` cũ vốn trả `UNAUTHORIZED` cho authentication exception và thêm `GlobalException` mới tại dòng 40-49 trả `BAD_REQUEST`.

## Kiểm tra unit test
- Module tương tự có test: Có.
- Test trong PR: Có unit test cho Auth, Vocabulary, Organization và HTML report.
- Chi tiết: Đã chạy full verification trên bản copy sạch của head SHA PR:

```text
./gradlew test jacocoTestReport testHtmlReport checkstyleMain checkstyleTest pmdMain pmdTest
```

Kết quả: `BUILD SUCCESSFUL`.

## Kết luận
- Kết quả: Request changes.
- Tóm tắt: PR build/test/checkstyle/PMD pass, nhưng có regression trong HTTP status của lỗi authentication cần sửa trước khi merge.
