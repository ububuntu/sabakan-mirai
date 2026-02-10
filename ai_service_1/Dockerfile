FROM python:3.11-slim

WORKDIR /app

# パッケージ名を最新のもの（libgl1）に変更してインストール
RUN apt-get update && apt-get install -y \
    gcc \
    g++ \
    python3-dev \
    libgl1 \
    libglib2.0-0 \
    && rm -rf /var/lib/apt/lists/*

RUN pip install --upgrade pip

COPY requirements.txt .
RUN pip install --no-cache-dir -r requirements.txt

COPY . .

# AI1は Flask/Gunicorn 構成
CMD ["gunicorn", "-w", "4", "-b", "0.0.0.0:8001", "app:app"]