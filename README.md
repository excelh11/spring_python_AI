# AI 자동 답변 시스템

이 프로젝트는 OpenAI API를 사용하여 댓글에 자동으로 답변을 생성하는 웹 애플리케이션입니다.
프론트에서 "답변생성" 버튼을 누르면 -> python에서 전처리과정후 -> spring AI가 openAPI를 활용하여 답변을 해주는 프로젝트입니다.  

## 프로젝트 구조

```
demo/
├── frontend/          # 프론트엔드 (HTML, CSS, JavaScript)
├── backend/           # 백엔드 (Spring Boot)
└── python-preprocessing/  # Python 전처리 스크립트
```

## 설정 방법

### 1. OpenAI API 키 설정

백엔드의 `application.properties` 파일에 OpenAI API 키를 설정해야 합니다.

**방법 1: application.properties 파일에 직접 설정**
```properties
openai.api.key=your-api-key-here
```

**방법 2: 환경 변수로 설정 (권장)**
```bash
# Windows (PowerShell)
$env:OPENAI_API_KEY="your-api-key-here"

# Windows (CMD)
set OPENAI_API_KEY=your-api-key-here

# Linux/Mac
export OPENAI_API_KEY="your-api-key-here"
```

그리고 `application.properties`에서:
```properties
openai.api.key=${OPENAI_API_KEY:}
```

### 2. 백엔드 실행

```bash
cd backend
./gradlew bootRun
```

또는 Windows의 경우:
```bash
cd backend
gradlew.bat bootRun
```

백엔드는 `http://localhost:8080`에서 실행됩니다.

### 3. 프론트엔드 실행

프론트엔드는 정적 파일이므로 웹 서버를 통해 실행하거나 브라우저에서 직접 열 수 있습니다.

**방법 1: Python HTTP 서버 사용**
```bash
cd frontend
python -m http.server 8000
```

그 다음 브라우저에서 `http://localhost:8000` 접속

**방법 2: Node.js http-server 사용**
```bash
npx http-server frontend -p 8000
```

**방법 3: 브라우저에서 직접 열기**
`frontend/index.html` 파일을 브라우저에서 직접 열기 (CORS 문제가 발생할 수 있음)

## 사용 방법

1. 웹 페이지를 열면 예시 댓글 목록이 표시됩니다.
2. 각 댓글의 "🤖 리플달기" 버튼을 클릭하면 OpenAI가 자동으로 답변을 생성합니다.
3. 또는 상단의 텍스트 영역에 내용을 입력하고 "답변 생성" 버튼을 클릭할 수 있습니다.

## API 엔드포인트

- `POST /api/ai/reply` - 댓글에 대한 답변 생성
  - Request Body: `{ "comment": "댓글 내용" }`
  - Response: `{ "reply": "생성된 답변" }`

- `POST /api/ai/answer` - 일반 내용에 대한 답변 생성
  - Request Body: `{ "content": "내용" }`
  - Response: `{ "answer": "생성된 답변" }`

- `GET /api/ai/health` - 서비스 상태 확인
  - Response: `{ "status": "ok", "message": "..." }`

## 기술 스택

- **프론트엔드**: HTML, CSS, JavaScript (Vanilla JS)
- **백엔드**: Spring Boot, Spring WebFlux
- **AI**: OpenAI GPT API
- **빌드 도구**: Gradle

## 문제 해결

### OpenAI API 키 오류
- API 키가 올바르게 설정되었는지 확인하세요.
- OpenAI 계정에 충분한 크레딧이 있는지 확인하세요.

### CORS 오류
- 백엔드 컨트롤러에 `@CrossOrigin(origins = "*")` 어노테이션이 설정되어 있습니다.
- 여전히 문제가 발생하면 브라우저 콘솔을 확인하세요.

### 연결 오류
- 백엔드 서버가 실행 중인지 확인하세요 (`http://localhost:8080/api/ai/health`로 확인 가능)
- 프론트엔드의 `API_BASE_URL`이 올바른지 확인하세요.

