# Review PR #2

## Phạm vi review
- Nguồn dữ liệu: https://github.com/DuyPTK-Runsystem/BaiTap2_HoiNhapKyThuat/pull/2
- Base branch: `master` (`25b5eac108d84d66d9c4417e6a6620a640c49f60`)
- Head branch: `vocab` (`9705379477e52e46a983e0215fe13681e9e577d2`)
- Chỉ review: diff của PR #2
- Không review: thay đổi local ngoài diff PR
- Requirement/tài liệu đối chiếu: `.claude/docs/modules/Testing_Module.md`, `.claude/docs/Data_Architecture.md`, `.claude/dev_plan/testing_learning/Testing_Learning_DeveloperPlan.md`

## MAJOR

- [MAJOR] `src/main/java/net/runsystem/duyptk/BaiTap2_HoiNhapKyThuat_AI/service/testing/QuestionFactory.java:21`
  - Vấn đề: `QuestionFactory.create(...)` random một `QuestionType` chỉ dựa trên dữ liệu của vocab gốc, nhưng không kiểm tra loại đó có đủ 3 distractor hợp lệ trong source hay không. Sau đó `OptionGenerator.generate(...)` mới phát hiện thiếu distractor và throw lỗi.
  - Ảnh hưởng: Tạo test có thể fail ngẫu nhiên dù cùng source có thể tạo được câu hỏi bằng template khác. Ví dụ vocab gốc có audio nên factory chọn `PRONUNCIATION_OF_WORD`, nhưng các vocab còn lại không có audio; lúc đó không đủ distractor audio và API `POST /api/v1/tests` thất bại. Lần gọi khác có thể chọn template meaning và thành công, gây hành vi không ổn định.
  - Giải pháp: Chọn question type dựa trên cả vocab gốc và khả năng tạo đủ distractor từ `sourceVocabs`. Có thể để `TestService`/`QuestionFactory` thử lần lượt các type đã shuffle, gọi một helper kiểm tra đủ 3 optionContent duy nhất trước khi chốt type; chỉ throw khi không type nào hợp lệ.
  - Căn cứ scope: PR thêm `QuestionFactory.create(...)` random type ở dòng 21-22 và `OptionGenerator.generate(...)` throw khi thiếu distractor ở `OptionGenerator.java:21-23`.

- [MAJOR] `src/main/java/net/runsystem/duyptk/BaiTap2_HoiNhapKyThuat_AI/service/testing/FlashcardService.java:24`
  - Vấn đề: Flashcard gọi `vocabSourceResolver.resolve(..., numberOfFlashcards)` trước, resolver chỉ trả đúng số vocab đã request, rồi service mới filter vocab không hợp lệ tại dòng 27-33.
  - Ảnh hưởng: API `POST /api/v1/flashcards` có thể báo "Không đủ từ vựng hợp lệ" dù source thực tế vẫn còn đủ vocab hợp lệ. Với source có 10 vocab, request 3 flashcards, nếu resolver random trả 3 vocab trong đó có 1 vocab thiếu cả meaning/audio, service fail thay vì lấy vocab hợp lệ khác trong source.
  - Giải pháp: Resolve một tập đủ lớn trước khi filter, hoặc bổ sung resolver riêng cho flashcard để lấy toàn bộ vocab từ source rồi filter/shuffle sau. Validation "không đủ vocab hợp lệ" nên dựa trên tổng số vocab hợp lệ trong source, không dựa trên mẫu random đã bị giới hạn trước.
  - Căn cứ scope: PR thêm `FlashcardService.create(...)` với luồng resolve ở dòng 24-26 và filter/limit/throw ở dòng 27-33.

- [MAJOR] `src/main/java/net/runsystem/duyptk/BaiTap2_HoiNhapKyThuat_AI/service/testing/TestResultService.java:95`
  - Vấn đề: Duplicate `questionId` được phát hiện bằng `Map.put(...) != null`. Nếu answer đầu tiên của một question có `optionId = null`, lần `put` tiếp theo trả về `null`, nên duplicate không bị phát hiện.
  - Ảnh hưởng: Request finish không hợp lệ có thể vượt validation và ghi kết quả theo answer cuối cùng, trái với rule Phase 3 là "Nếu có questionId trùng trong request, từ chối". Điều này làm API nhận payload mơ hồ và có thể lưu final answer khác với ý định client.
  - Giải pháp: Kiểm tra duplicate bằng `containsKey(questionId)` trước khi `put`, hoặc dùng `Set<Long>` để add questionId và reject khi `add` trả `false`, không phụ thuộc vào giá trị optionId.
  - Căn cứ scope: PR thêm logic validate duplicate trong `selectedOptionIdsByQuestionId(...)` tại dòng 90-102.

## MINOR

- [MINOR] `postman/BaiTap2-HoiNhapKyThuat-AI.postman_collection.json:195`
  - Vấn đề: Script Login lưu token bằng `data.accessToken`, nhưng `ResLoginDTO` serialize field với `@JsonProperty("access_token")`, nên response thực tế là `data.access_token`.
  - Ảnh hưởng: Chạy collection tuần tự sẽ không set được `accessToken`; các request cần Bearer token phía sau dễ bị 401 dù login thành công.
  - Giải pháp: Đổi script thành `pm.collectionVariables.set('accessToken', data.access_token);`.
  - Căn cứ scope: PR cập nhật Postman collection và script Login ở dòng 192-195.

## Kiểm tra unit test
- Module tương tự có test: Có, PR bổ sung unit test cho source resolver, question factory, option generator, test service, result service và flashcard service.
- Test trong PR: Thiếu một số edge case quan trọng.
- Chi tiết: Chưa có test cho việc factory chọn type có đủ distractor trước khi tạo question; chưa có test flashcard khi source có đủ vocab hợp lệ nhưng batch đầu sau resolver chứa vocab không hợp lệ; chưa có test duplicate finish answer khi answer đầu tiên có `optionId = null`.

## Kết luận
- Kết quả: Request changes
- Tóm tắt: PR triển khai phần lớn Testing & Learning, nhưng còn các lỗi ngẫu nhiên/validation trong luồng tạo test, flashcard và finish test có thể gây thất bại API hoặc nhận payload không hợp lệ. Nên sửa các issue MAJOR trước khi merge.
