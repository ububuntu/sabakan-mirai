from fastapi import FastAPI
from starlette.middleware.cors import CORSMiddleware
import editing
import os
# Ollamaライブラリが参照するホスト先を、コンテナ名に変更

os.environ["OLLAMA_HOST"] = "http://ollama-server:11434"

app = FastAPI()
app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"]
)

app.include_router(editing.router)

@app.get("/", tags=["Health Check"])
def read_root():
    """
    APIが正常に動作しているか確認します。
    """
    return {"message": "Typo Correction API is running correctly."}