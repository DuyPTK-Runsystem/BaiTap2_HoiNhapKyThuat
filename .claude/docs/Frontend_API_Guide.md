# Frontend API Guide

## 1. Mục đích

Tài liệu này mô tả contract API cho Frontend tích hợp với Backend hiện tại.

Nguồn đối chiếu:

- `.claude/docs/ApplicationContext.md`
- `.claude/docs/modules/Auth_Module.md`
- `.claude/docs/modules/Vocabulary_Module.md`
- `.claude/docs/modules/Organization_Module.md`
- `.claude/docs/modules/Testing_Module.md`
- Source code controller/DTO hiện tại

## 2. Quy ước chung

### 2.1. Base URL

```text
http://localhost:8081
```

### 2.2. Response wrapper

Hầu hết JSON response được wrap theo cấu trúc:

```json
{
  "statusCode": 200,
  "error": null,
  "message": "CALL API SUCCESS",
  "data": {}
}
```

Frontend nên đọc dữ liệu chính từ:

```text
response.data.data
```

Với request audio file, response là binary `audio/mpeg`, không dùng wrapper JSON.

### 2.3. Authentication

Các API ngoài register/login/refresh cần Bearer token:

```text
Authorization: Bearer <access_token>
```

Login/refresh trả access token trong field:

```json
{
  "data": {
    "access_token": "..."
  }
}
```

Refresh token được Backend set bằng cookie `refresh_token` HTTP-only.

## 3. Auth APIs

### 3.1. Register

```text
POST /api/v1/auth/register
```

Request:

```json
{
  "email": "demo@example.com",
  "password": "secret123"
}
```

Response `data`:

```json
{
  "id": 1,
  "email": "demo@example.com"
}
```

### 3.2. Login

```text
POST /api/v1/auth/login
```

Request:

```json
{
  "email": "demo@example.com",
  "password": "secret123"
}
```

Response `data`:

```json
{
  "access_token": "...",
  "user": {
    "id": 1,
    "email": "demo@example.com"
  }
}
```

### 3.3. Account

```text
GET /api/v1/auth/account
```

Response `data`: current user.

### 3.4. Refresh

```text
GET /api/v1/auth/refresh
```

Response `data`: same shape as login.

### 3.5. Logout

```text
POST /api/v1/auth/logout
```

Clears refresh token server-side and cookie.

## 4. Vocabulary APIs

### 4.1. Create Vocab

```text
POST /api/v1/vocabs
POST /api/v1/vocabs?vocabSetId={vocabSetId}
```

Request:

```json
{
  "word": "hello",
  "meaning": "xin chao",
  "ipa": null
}
```

Response `data` includes vocab fields such as:

```json
{
  "id": 1,
  "word": "hello",
  "meaning": "xin chao",
  "ipa": "...",
  "audioUrl": "/api/v1/vocabs/audio/..."
}
```

If `vocabSetId` is provided, Backend creates vocab and attaches it to that vocab set.

### 4.2. Bulk Import Vocab

```text
POST /api/v1/vocabs/bulk
POST /api/v1/vocabs/bulk?vocabSetId={vocabSetId}
```

Request type: `multipart/form-data`

Field:

```text
file: .xlsx
```

Response follows Partial Failure: invalid rows are skipped and reported; valid rows are processed.

### 4.3. Get Vocab

```text
GET /api/v1/vocabs/lookup?id={vocabId}
GET /api/v1/vocabs/lookup?word={word}
```

### 4.4. Update Vocab Meaning

```text
PATCH /api/v1/vocabs/lookup?id={vocabId}
PATCH /api/v1/vocabs/lookup?word={word}
```

Request:

```json
{
  "meaning": "new meaning"
}
```

### 4.5. Get Audio

```text
GET /api/v1/vocabs/audio/{fileName}
```

Response:

```text
Content-Type: audio/mpeg
```

Frontend should render this as an audio player.

## 5. Organization APIs

### 5.1. Create Folder

```text
POST /api/v1/folders
```

Request:

```json
{
  "folderName": "My Folder",
  "parentId": null
}
```

### 5.2. Create Vocab Set

```text
POST /api/v1/vocab-sets
```

Request:

```json
{
  "vocabSetName": "Core Set",
  "vocabSetDescription": "Basic words",
  "parentId": 1
}
```

### 5.3. Get Children

```text
GET /api/v1/items/children
GET /api/v1/items/children?parentId={folderId}
```

Response `data`: list of items.

Item shape:

```json
{
  "id": 1,
  "type": "FOLDER",
  "name": "My Folder",
  "description": null,
  "parentId": null,
  "vocabCount": null,
  "itemPath": "/My Folder"
}
```

`type` can be:

```text
FOLDER
VOCAB_SET
```

### 5.4. Search Items

```text
GET /api/v1/items/search?name={keyword}
```

### 5.5. Get Item By Path

```text
GET /api/v1/items/by-path?path=/My%20Folder/Core%20Set
```

### 5.6. Add Vocab To Vocab Set

```text
POST /api/v1/vocab-sets/{vocabSetId}/vocabs/{vocabId}
```

### 5.7. Bulk Add Vocabs To Vocab Set

```text
POST /api/v1/vocab-sets/{vocabSetId}/vocabs/bulk
```

Request:

```json
{
  "vocabIds": [1, 2, 3]
}
```

## 6. Testing APIs

### 6.1. Create Test

```text
POST /api/v1/tests
```

Request:

```json
{
  "sourceItemIds": [10],
  "numberOfQuestion": 5,
  "timeInMinute": 15
}
```

Rules:

- `sourceItemIds = null` or `[]`: Backend selects vocab randomly from the whole database.
- `sourceItemIds` can contain Folder or VocabSet IDs.
- Folder sources are resolved recursively.
- `numberOfQuestion >= 1`.
- `timeInMinute = null` or `0`: no time limit.
- `timeInMinute > 0`: limited test.

Response `data`:

```json
{
  "id": 1,
  "numberOfQuestion": 5,
  "timeInMinute": 15,
  "correctAnswerCount": 0,
  "incorrectAnswerCount": 0,
  "remainingTimeInSeconds": 899,
  "finished": false,
  "questions": [
    {
      "id": 100,
      "vocabId": 20,
      "questionContent": "Từ 'apple' có ý nghĩa gì?",
      "correctAnswer": "qua tao",
      "audioUrl": null,
      "options": [
        {
          "id": 1000,
          "optionOrder": 1,
          "optionContent": "qua tao",
          "correct": true,
          "audioUrl": null
        }
      ],
      "answer": null
    }
  ]
}
```

Frontend display notes:

- Each question has exactly 4 options.
- Questions in one test can use different templates.
- If `question.audioUrl` exists, render an audio player for the question.
- If an option has `audioUrl`, render audio player for that option instead of plain URL text.
- `correctAnswer` and `option.correct` are currently returned; FE can hide them during test-taking and show them on result screen.

### 6.2. Get Test

```text
GET /api/v1/tests/{testId}
```

Response `data`: same shape as Create Test.

Use this API to reload a test page.

### 6.3. Finish Test

```text
POST /api/v1/tests/{testId}/finish
```

Request:

```json
{
  "answers": [
    {
      "questionId": 100,
      "optionId": 1000
    },
    {
      "questionId": 101,
      "optionId": 1005
    }
  ]
}
```

Rules:

- FE submits final answers once.
- `questionId` must belong to the test.
- `optionId` must belong to the question.
- Duplicate `questionId` is rejected.
- Missing question answers are saved as incorrect with `selectedOptionId = null`.
- Finished test cannot be finished again.
- Expired test cannot be finished.

Response `data`: result shape with `finished = true`.

Question `answer` field after finish:

```json
{
  "id": 1,
  "questionId": 100,
  "selectedOptionId": 1000,
  "selectedOptionContent": "qua tao",
  "correct": true
}
```

### 6.4. Get Test Result

```text
GET /api/v1/tests/{testId}/result
```

Response `data`: same shape as finished test.

If test is not finished, Backend returns validation error.

## 7. Flashcard APIs

### 7.1. Create Flashcards

```text
POST /api/v1/flashcards
```

Request:

```json
{
  "sourceItemIds": [10],
  "numberOfFlashcards": 10
}
```

Rules:

- `sourceItemIds = null` or `[]`: Backend selects vocab from whole database.
- Flashcard is dynamic and not persisted.
- Backend randomly chooses `frontType` for each flashcard based on available vocab data.
- If vocab has both `meaning` and `audioUrl`, front can be `MEANING` or `AUDIO`.
- If vocab has only `meaning`, front is `MEANING`.
- If vocab has only `audioUrl`, front is `AUDIO`.
- Vocab without both meaning and audio is skipped.

Response `data`:

```json
{
  "sourceItemIds": [10],
  "numberOfFlashcards": 10,
  "flashcards": [
    {
      "vocabId": 1,
      "frontType": "MEANING",
      "frontText": "qua tao",
      "frontAudioUrl": null,
      "backWord": "apple",
      "backMeaning": "qua tao",
      "backAudioUrl": "/api/v1/vocabs/audio/apple.mp3"
    },
    {
      "vocabId": 2,
      "frontType": "AUDIO",
      "frontText": null,
      "frontAudioUrl": "/api/v1/vocabs/audio/book.mp3",
      "backWord": "book",
      "backMeaning": "quyen sach",
      "backAudioUrl": "/api/v1/vocabs/audio/book.mp3"
    }
  ]
}
```

Frontend display notes:

- If `frontType = MEANING`, show `frontText` on card front.
- If `frontType = AUDIO`, render `frontAudioUrl` as audio player on card front.
- Card back should show `backWord`.
- FE can additionally show `backMeaning` and/or render `backAudioUrl` when present.

## 8. Recommended FE Flow

### 8.1. Initial auth flow

1. Call `POST /api/v1/auth/login`.
2. Store `data.access_token`.
3. Attach Bearer token to protected requests.
4. Use `GET /api/v1/auth/refresh` when access token expires.

### 8.2. Vocabulary and organization setup

1. Create folders with `POST /api/v1/folders`.
2. Create vocab sets with `POST /api/v1/vocab-sets`.
3. Create vocabs with `POST /api/v1/vocabs`.
4. Add vocabs to vocab set with add/bulk add APIs.
5. Use `GET /api/v1/items/children` to render folder tree.

### 8.3. Test flow

1. User chooses source folder/vocab set.
2. FE calls `POST /api/v1/tests`.
3. FE renders questions/options.
4. FE tracks local selected option by `questionId`.
5. FE submits once with `POST /api/v1/tests/{testId}/finish`.
6. FE renders result from finish response or `GET /api/v1/tests/{testId}/result`.

### 8.4. Flashcard flow

1. User chooses source folder/vocab set.
2. FE calls `POST /api/v1/flashcards`.
3. FE renders card front based on `frontType`.
4. FE flips card locally; no Backend call is needed for progress.

## 9. Error Handling

Common error wrapper:

```json
{
  "statusCode": 400,
  "error": "Illegal argument exception occurs...",
  "message": "Số lượng câu hỏi phải lớn hơn hoặc bằng 1",
  "data": null
}
```

FE should display `message` when available.

Common status codes:

- `400`: invalid request or validation error.
- `401`: missing/invalid authentication.
- `403`: access denied.
- `404`: resource not found or resource not owned by current user.
- `502`: external provider error.
