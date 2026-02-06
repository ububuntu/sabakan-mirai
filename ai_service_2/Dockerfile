FROM python:3.11-slim

WORKDIR /app

# Ollamaなどのライブラリが必要とする可能性がある基本的なツールをインストール
RUN apt-get update && apt-get install -y \
    gcc \
    && rm -rf /var/lib/apt/lists/*

COPY requirements.txt .
RUN pip install --no-cache-dir -r requirements.txt

COPY . .

# AI2は api.py なので、実行コマンドは api:app になります
CMD ["uvicorn", "api:app", "--host", "0.0.0.0", "--port", "8000"]