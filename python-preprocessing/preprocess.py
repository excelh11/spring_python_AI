#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
데이터 전처리 스크립트
웹사이트 글을 AI 모델에 적합한 형태로 전처리합니다.
"""

import re
import json
import sys
from typing import Dict, List


def clean_text(text: str) -> str:
    """텍스트 정제"""
    if not text:
        return ""
    
    # HTML 태그 제거
    text = re.sub(r'<[^>]+>', '', text)
    
    # 연속된 공백 제거
    text = re.sub(r'\s+', ' ', text)
    
    # 앞뒤 공백 제거
    text = text.strip()
    
    return text


def extract_keywords(text: str, max_keywords: int = 10) -> List[str]:
    """키워드 추출 (간단한 버전)"""
    if not text:
        return []
    
    # 불용어 제거
    stopwords = {'은', '는', '이', '가', '을', '를', '의', '에', '와', '과', '도', '로', '으로', 
                 '에서', '에게', '께', '한테', '더', '그', '그것', '이것', '저것', '그런', '이런', '저런'}
    
    # 단어 분리 (간단한 버전)
    words = re.findall(r'\b[가-힣]{2,}\b', text)
    words = [w for w in words if w not in stopwords]
    
    # 빈도수 기반 키워드 추출
    word_freq = {}
    for word in words:
        word_freq[word] = word_freq.get(word, 0) + 1
    
    # 빈도수 순으로 정렬
    sorted_words = sorted(word_freq.items(), key=lambda x: x[1], reverse=True)
    keywords = [word for word, freq in sorted_words[:max_keywords]]
    
    return keywords


def summarize_text(text: str, max_length: int = 200) -> str:
    """텍스트 요약 (간단한 버전 - 첫 문장들 추출)"""
    if not text:
        return ""
    
    # 문장 분리
    sentences = re.split(r'[.!?]\s+', text)
    
    # 첫 몇 문장 추출
    summary_sentences = []
    current_length = 0
    
    for sentence in sentences:
        if current_length + len(sentence) <= max_length:
            summary_sentences.append(sentence)
            current_length += len(sentence)
        else:
            break
    
    return '. '.join(summary_sentences) + '.' if summary_sentences else text[:max_length]


def preprocess(content: str) -> Dict:
    """메인 전처리 함수"""
    if not content:
        return {
            "cleaned_text": "",
            "keywords": [],
            "summary": "",
            "word_count": 0
        }
    
    # 텍스트 정제
    cleaned_text = clean_text(content)
    
    # 키워드 추출
    keywords = extract_keywords(cleaned_text)
    
    # 요약 생성
    summary = summarize_text(cleaned_text)
    
    # 단어 수 계산
    word_count = len(cleaned_text.split())
    
    return {
        "cleaned_text": cleaned_text,
        "keywords": keywords,
        "summary": summary,
        "word_count": word_count
    }


def main():
    """메인 함수"""
    # Windows에서 한글 출력을 위한 인코딩 설정
    import sys
    import io
    if sys.platform == 'win32':
        sys.stdout = io.TextIOWrapper(sys.stdout.buffer, encoding='utf-8')
        sys.stderr = io.TextIOWrapper(sys.stderr.buffer, encoding='utf-8')
    
    if len(sys.argv) < 2:
        print("Usage: python preprocess.py <content>")
        sys.exit(1)
    
    # 입력 인코딩 처리
    content = sys.argv[1]
    if isinstance(content, bytes):
        content = content.decode('utf-8')
    
    # 전처리 수행
    result = preprocess(content)
    
    # JSON 형식으로 출력 (UTF-8 인코딩 명시)
    json_output = json.dumps(result, ensure_ascii=False, indent=2)
    print(json_output)
    sys.stdout.flush()  # 버퍼 강제 출력


if __name__ == "__main__":
    main()

