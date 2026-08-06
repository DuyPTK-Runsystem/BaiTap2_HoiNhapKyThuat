# Testing & Learning Module Specification

## 1. Entities & Data Structures

### 1.1. BM6a: Test Session (Thông tin Bài kiểm tra)
Đây là thực thể quản lý phiên học/kiểm tra của người dùng.
*   `test_id` (Long, PK): Định danh duy nhất.
*   `user_id` (Long, FK): Chủ sở hữu bài test.
*   `vocab_source` (List<Item>): Danh sách các Folder hoặc VocabSet làm nguồn (Nếu `null`, lấy ngẫu nhiên toàn bộ CSDL).
*   `number_of_question` (Int, Min=1): Số lượng câu hỏi cần tạo.
*   `time_in_minute` (Int, nullable): Giới hạn thời gian.
    *   `0` hoặc `null`: Không giới hạn thời gian.
    *   `> 0`: Có giới hạn thời gian.
*   `correct_answer_count` (Int): Số câu trả lời đúng (cập nhật sau khi kết thúc).
*   `incorrect_answer_count` (Int): Số câu trả lời sai (cập nhật sau khi kết thúc).

### 1.2. BM6b: Question (Thông tin Câu hỏi)
Câu hỏi được **tạo tự động (Auto-generated)**, không lấy từ ngân hàng câu hỏi có sẵn.
*   `question_id` (Long, PK): Định danh duy nhất.
*   `test_id` (Long, FK): Thuộc bài test nào.
*   `vocab_id` (Long, FK): Từ vựng làm gốc để đặt câu hỏi.
*   `question_content` (String): Nội dung câu hỏi (theo template).
*   `correct_answer` (String): Đáp án đúng (giá trị text).
*   `audio_url` (String, nullable): Link audio của từ vựng đó.

### 1.3. BM6c: Option (Thông tin Đáp án)
Mỗi câu hỏi sẽ có các tùy chọn đáp án.
*   `option_id` (Long, PK): Định danh duy nhất.
*   `question_id` (Long, FK): Thuộc câu hỏi nào.
*   `option_order` (Int, Min=1, Max=4): Thứ tự hiển thị (1, 2, 3, hoặc 4).
*   `option_content` (String): Nội dung của đáp án.
*   `is_correct` (Boolean): Đánh dấu đây có phải đáp án đúng hay không.
*   `audio_url` (String, nullable): Link audio (nếu câu hỏi yêu cầu nghe).

### 1.4. BM6d: Test Answer (Đáp án cuối cùng của người dùng)
Lưu đáp án cuối cùng mà người dùng nộp khi kết thúc bài test.
*   `test_answer_id` (Long, PK): Định danh duy nhất.
*   `test_id` (Long, FK): Thuộc bài test nào.
*   `question_id` (Long, FK): Câu hỏi được trả lời.
*   `selected_option_id` (Long, FK, nullable): Option người dùng chọn.
*   `is_correct` (Boolean): Kết quả đúng/sai của đáp án đã chọn.

---

## 2. Business Rules & Constraints (Quy tắc Nghiệp vụ)

### 2.1. Quy tắc về Đáp án (Option Rules)
1.  **Tính duy nhất của đáp án đúng:** Mỗi `question_id` chỉ được có **duy nhất một** `Option` có `is_correct = true`.
2.  **Tính đồng nhất:** Giá trị `Option.option_content` của đáp án đúng **phải trùng khớp** hoàn toàn với `Question.correct_answer`.
3.  **Ràng buộc Unique:** Phải đảm bảo tính duy nhất cho cặp `(question_id, option_order)`.
4.  **Số lượng:** Mỗi câu hỏi phải có đúng 4 tùy chọn (tương ứng với `option_order` từ 1 đến 4).

### 2.2. Quy tắc về Đáp án cuối cùng của người dùng (Test Answer Rules)
1.  Khi người dùng kết thúc bài test, BE phải lưu đáp án cuối cùng của từng câu hỏi vào `TestAnswer`.
2.  Mỗi cặp `(test_id, question_id)` chỉ có tối đa một `TestAnswer`.
3.  `selected_option_id` phải thuộc đúng `question_id` tương ứng.
4.  Nếu người dùng không gửi đáp án cho một câu hỏi, BE vẫn tạo `TestAnswer` cho câu đó với `selected_option_id = null` và `is_correct = false`.
5.  Sau khi lưu final answers, BE cập nhật `correct_answer_count` và `incorrect_answer_count` trên `Test`.
6.  Nếu câu trả lời của một `Question` đúng, BE cập nhật `Question.vocab.mastered = true`.
7.  Nếu câu trả lời sai, BE không thay đổi giá trị hiện tại của `Question.vocab.mastered`.

### 2.3. Quy tắc về Logic Tạo Câu hỏi (Question Generation Factory)
Hệ thống phải dựa vào dữ liệu của `Vocab` để "lắp" vào các template sau:

| STT   | Loại câu hỏi               | Nội dung Câu hỏi (`Question.question_content`) | Dữ liệu cho Đáp án (`Option.option_content`)    | Dữ liệu cho Audio |
| :---- | :------------------------- | :--------------------------------------------- | :---------------------------------------------- | :---------------- |
| **1** | **Nghĩa của từ**           | `"Từ '{word}' có ý nghĩa gì?"`                 | Lấy từ `Vocab.meaning`                          | Không cần         |
| **2** | **Phát âm của từ**         | `"Từ '{word}' phát âm như thế nào?"`           | Lấy từ `Vocab.audio_url` (hiển thị dạng player) | `Vocab.audio_url` |
| **3** | **Tìm từ theo nghĩa**      | `"Từ nào có ý nghĩa là '{meaning}'?"`          | Lấy từ `Vocab.word`                             | Không cần         |
| **4** | **Tìm từ theo nghĩa + âm** | `"Từ nào có ý nghĩa là '{meaning}'?"`          | Lấy từ `Vocab.word`                             | `Vocab.audio_url` |
| **5** | **Tìm từ theo âm thanh**   | `"Từ nào có phát âm như sau?"`                 | Lấy từ `Vocab.word`                             | `Vocab.audio_url` |

**Lưu ý về hiển thị Audio:**
*   Nếu trường dữ liệu là `Vocab.audio_url`, hệ thống BE trả về URL, nhưng FE sẽ không hiển thị chuỗi text URL mà sẽ render một trình phát âm thanh (Audio Player).

### 2.4. Quy tắc về Nguồn dữ liệu (VocabSource)
*   **Nếu `VocabSource` là danh sách `Item` (Folder/VocabSet):**
    *   BE phải thực hiện truy vấn đệ quy (Recursive Query) để thu thập toàn bộ `vocab_id` nằm trong `Item` đó và tất cả các `Item` con của nó.
    *   **Ràng buộc (Type = Kiểm tra):** `number_of_question` $\leq$ Tổng số lượng `vocab_id` thu thập được.
*   **Nếu `VocabSource = null`:**
    *   Lấy ngẫu nhiên từ toàn bộ bảng `Vocab` trong CSDL.

### 2.5. Quy tắc về Phiên học tập (Flashcard - BM7)
*   **Khác biệt với Kiểm tra:** Không lưu `Test`, `Question`, hay `Option` vào Database.
*   **Cấu trúc hiển thị:**
    *   **Mặt trước (Front):** `Meaning` HOẶC `Audio`.
    *   **Mặt sau (Back):** `Word` VÀ [`Audio` HOẶC `Meaning`].

---

## 3. Developer Checklist (Dành cho BE)
- [ ] Implement logic đệ quy lấy `vocab_id` từ `Folder`.
- [ ] Implement `QuestionFactory` với 5 template trên.
- [ ] Implement logic Randomize đáp án (đảm bảo đáp án đúng nằm ở các `option_order` ngẫu nhiên 1-4).
- [ ] Đảm bảo `is_correct` chỉ có một giá trị `true` mỗi câu.
- [ ] Xử lý logic `time_in_minute` để trả về thời gian còn lại cho client.

## 4. Lịch sử cập nhật

| Ngày | Nội dung | Người cập nhật |
|---|---|---|
| 2026-08-06 | Bổ sung rule: đáp án đúng đánh dấu Vocab tương ứng `mastered = true`; đáp án sai không thay đổi trạng thái | RunSystem Assistant |
