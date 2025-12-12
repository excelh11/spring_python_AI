// API 설정
const API_BASE_URL = 'http://localhost:8080/api/ai';

// DOM 요소
const contentInput = document.getElementById('content-input');
const generateBtn = document.getElementById('generate-btn');
const answerOutput = document.getElementById('answer-output');
const loadingDiv = document.getElementById('loading');
const commentsList = document.getElementById('comments-list');

// 예시 댓글 데이터
const exampleComments = [
    {
        id: 1,
        author: '김철수',
        time: '2024-01-15 14:30',
        content: '이 프로젝트 정말 유용해 보이네요! Spring AI를 활용한 자동 답변 시스템이 어떻게 작동하는지 궁금합니다.'
    },
    {
        id: 2,
        author: '이영희',
        time: '2024-01-15 15:45',
        content: 'Python 전처리와 Spring AI를 함께 사용하는 것이 인상적입니다. 실제로 어떤 전처리 과정을 거치나요?'
    },
    {
        id: 3,
        author: '박민수',
        time: '2024-01-15 16:20',
        content: '프론트엔드와 백엔드 통신이 잘 되는지, 그리고 API 응답 시간은 어느 정도인지 궁금합니다.'
    },
    {
        id: 4,
        author: '최지은',
        time: '2024-01-15 17:10',
        content: 'ChatGPT API를 사용할 때 비용이 많이 드나요? 그리고 다른 AI 모델도 사용할 수 있나요?'
    }
];

// 간단 전처리 (클라이언트 측)
async function preprocessContent(content) {
    // HTML 태그 제거
    let cleaned = content.replace(/<[^>]+>/g, '');
    // 연속된 공백 제거
    cleaned = cleaned.replace(/\s+/g, ' ').trim();

    return {
        cleaned_text: cleaned,
        word_count: cleaned ? cleaned.split(' ').length : 0
    };
}

// 답변 생성 함수 (/answer)
async function generateAnswer() {
    if (!contentInput) return;  // 방어 코드

    const content = contentInput.value.trim();

    if (!content) {
        alert('내용을 입력해주세요.');
        return;
    }

    // UI 업데이트
    if (generateBtn) generateBtn.disabled = true;
    if (loadingDiv) loadingDiv.classList.remove('hidden');
    if (answerOutput) {
        answerOutput.innerHTML = '<p class="placeholder">답변을 생성하는 중...</p>';
        answerOutput.classList.remove('has-content');
    }

    try {
        // 전처리 수행
        const preprocessedData = await preprocessContent(content);

        // API 요청
        const response = await fetch(`${API_BASE_URL}/answer`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
            },
            body: JSON.stringify({
                content: content,
                preprocessedData: JSON.stringify(preprocessedData)
            })
        });

        if (!response.ok) {
            throw new Error(`HTTP error! status: ${response.status}`);
        }

        const data = await response.json();

        // ⚠️ 백엔드 응답 구조 확인 필요
        // 예: { "answer": "..." } 라면 data.answer 사용
        const answerText = data.answer || data.reply || '답변을 생성할 수 없습니다.';

        if (answerOutput) {
            answerOutput.innerHTML = answerText;
            answerOutput.classList.add('has-content');
        }

    } catch (error) {
        console.error('Error (generateAnswer):', error);
        if (answerOutput) {
            answerOutput.innerHTML = `<p style="color: red;">오류가 발생했습니다: ${error.message}</p>`;
            answerOutput.classList.add('has-content');
        }
    } finally {
        if (generateBtn) generateBtn.disabled = false;
        if (loadingDiv) loadingDiv.classList.add('hidden');
    }
}

// 이벤트 리스너 (answer용)
if (generateBtn) {
    generateBtn.addEventListener('click', generateAnswer);
}

if (contentInput) {
    contentInput.addEventListener('keydown', (e) => {
        if (e.ctrlKey && e.key === 'Enter') {
            generateAnswer();
        }
    });
}

// HTML 이스케이프 함수
function escapeHtml(text) {
    const div = document.createElement('div');
    div.textContent = text == null ? '' : text;
    return div.innerHTML;
}

// 댓글 요소 생성
function createCommentElement(comment) {
    const commentDiv = document.createElement('div');
    commentDiv.className = 'comment-item';
    commentDiv.dataset.commentId = comment.id;

    commentDiv.innerHTML = `
        <div class="comment-header">
            <span class="comment-author">${escapeHtml(comment.author)}</span>
            <span class="comment-time">${escapeHtml(comment.time)}</span>
        </div>
        <div class="comment-content">${escapeHtml(comment.content)}</div>
        <div class="comment-actions">
            <button class="btn-reply" data-comment-id="${comment.id}">
                🤖 답변 달기
            </button>
        </div>
        <div class="reply-container" id="reply-${comment.id}"></div>
    `;

    // 답변 달기 버튼 이벤트
    const replyBtn = commentDiv.querySelector('.btn-reply');
    replyBtn.addEventListener('click', () => generateReply(comment.id, comment.content));

    return commentDiv;
}

// 댓글 목록 렌더링
function renderComments() {
    if (!commentsList) return;
    commentsList.innerHTML = '';
    exampleComments.forEach(comment => {
        const commentElement = createCommentElement(comment);
        commentsList.appendChild(commentElement);
    });
}

// 댓글 답변 생성 (/reply)
async function generateReply(commentId, commentContent) {
    console.log('답변 달기 버튼 클릭됨:', commentId, commentContent);

    const replyBtn = document.querySelector(`.btn-reply[data-comment-id="${commentId}"]`);
    const replyContainer = document.getElementById(`reply-${commentId}`);

    if (!replyBtn || !replyContainer) {
        console.error('요소를 찾을 수 없습니다:', { replyBtn, replyContainer });
        return;
    }

    // UI 업데이트
    replyBtn.disabled = true;
    replyBtn.innerHTML = '<span class="reply-loading"></span>생성 중...';
    replyContainer.innerHTML = `
        <div class="reply-result">
            <div class="reply-result-content">답변을 생성하는 중...</div>
        </div>
    `;

    try {
        console.log('API 요청 시작:', `${API_BASE_URL}/reply`);
        console.log('요청 데이터:', { comment: commentContent });

        // API 요청
        const response = await fetch(`${API_BASE_URL}/reply`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
            },
            body: JSON.stringify({
                comment: commentContent
            })
        });

        console.log('API 응답 상태:', response.status, response.statusText);

        if (!response.ok) {
            const errorText = await response.text();
            console.error('API 오류 응답:', errorText);
            throw new Error(`HTTP ${response.status}: ${errorText || response.statusText}`);
        }

        // 응답 텍스트를 먼저 읽어서 확인
        const responseText = await response.text();
        console.log('API 응답 원본 텍스트:', responseText);

        let data;
        try {
            data = JSON.parse(responseText);
            console.log('API 응답 파싱 완료:', data);
        } catch (parseError) {
            console.error('JSON 파싱 오류:', parseError);
            console.error('파싱 실패한 텍스트:', responseText);
            throw new Error('서버 응답을 파싱할 수 없습니다: ' + responseText.substring(0, 100));
        }

        // ⚠️ 백엔드 응답 구조 확인:
        // - { "reply": "..." } 이면 data.reply
        // - { "answer": "..." } 이면 data.answer
        const replyTextRaw = data.reply || data.answer || '';
        console.log(
            '표시할 답변 텍스트:',
            replyTextRaw.substring(0, Math.min(100, replyTextRaw.length))  // length() X
        );

        if (!replyTextRaw || replyTextRaw.trim().length === 0) {
            throw new Error('답변이 비어있습니다.');
        }

        // 답변 표시 (HTML 이스케이프)
        replyContainer.innerHTML = `
            <div class="reply-result">
                <div class="reply-result-header">
                    <span>🤖 AI 답변</span>
                </div>
                <div class="reply-result-content">${escapeHtml(replyTextRaw)}</div>
            </div>
        `;

        console.log('답변 표시 완료, 컨테이너:', replyContainer);
        console.log('컨테이너 innerHTML 길이:', replyContainer.innerHTML.length);

    } catch (error) {
        console.error('답변 달기 오류:', error);
        console.error('오류 상세:', {
            message: error.message,
            stack: error.stack,
            name: error.name
        });

        let errorMessage = error.message;
        if (error.message.includes('Failed to fetch') || error.message.includes('NetworkError')) {
            errorMessage = '백엔드 서버에 연결할 수 없습니다. 서버가 실행 중인지 확인해주세요.';
        }

        replyContainer.innerHTML = `
            <div class="reply-result">
                <div class="reply-result-content" style="color: red;">
                    <strong>오류가 발생했습니다:</strong><br>
                    ${escapeHtml(errorMessage)}<br>
                    <small style="color: #999;">브라우저 콘솔(F12)에서 자세한 오류를 확인할 수 있습니다.</small>
                </div>
            </div>
        `;
    } finally {
        replyBtn.disabled = false;
        replyBtn.innerHTML = '🤖 답변 달기';
    }
}

// 페이지 로드 시 초기화
window.addEventListener('load', async () => {
    // 댓글 목록 렌더링
    renderComments();

    // API 연결 확인
    try {
        const response = await fetch(`${API_BASE_URL}/health`);
        if (response.ok) {
            console.log('API 연결 성공');
        }
    } catch (error) {
        console.warn('API 연결 실패:', error);
        // answerOutput이 있는 페이지에서만 표시
        if (answerOutput) {
            answerOutput.innerHTML =
                '<p style="color: orange;">백엔드 서버에 연결할 수 없습니다. 서버가 실행 중인지 확인해주세요.</p>';
        }
    }
});
