"""
エントリーシート・履歴書チェックAPI (ルーター版)
"""

from fastapi import APIRouter, HTTPException
from pydantic import BaseModel
import ollama

# APIRouterを使用（api.pyから呼び出される）
router = APIRouter()

class CheckRequest(BaseModel):
    text_to_check: str

@router.post("/check")
async def check_resume(request: CheckRequest):
    prompt = """
あなたはプロの就活アドバイザーです。
エントリーシートや履歴書（志望動機、自己PR、学生時代力を入れたこと、長所短所など）を以下の観点でチェックし、訂正してください。

【チェック項目】
1. 基本的なミス
    - 誤字脱字
    - 文法ミス
    - 敬語の誤り（謙譲語・尊敬語の使い分け含む）

2. 表現の問題
    - 不自然な表現
    - 曖昧な表現
    - 冗長な表現（「私は」「趣味は」など不要な主語や自明な繰り返し）

3. 内容の質
    - 具体性があるか（数字や固有名詞があるか）
    - 熱意が伝わるか
    - 論理的か
    - 結論が明確か

4. ビジネス文書としての適切さ
    - ビジネスマナーに沿っているか
    - 読みやすいか

【重要】
- 元の文章の改行や段落構成は維持してください
- 訂正した文章のみを出力してください
- 説明や表形式は不要です
- 問題がない場合は元の文章をそのまま出力してください
"""
    
    try:
        response = ollama.chat(
            model='gemma2:9b',
            messages=[
                {'role': 'system', 'content': prompt},
                {'role': 'user', 'content': request.text_to_check}
            ]
        )
        return {"result": response['message']['content']}
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))