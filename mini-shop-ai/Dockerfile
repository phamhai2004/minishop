FROM python:3.12-slim

# =========================
# Environment
# =========================
ENV PYTHONDONTWRITEBYTECODE=1
ENV PYTHONUNBUFFERED=1
ENV PIP_NO_CACHE_DIR=1

WORKDIR /app

# =========================
# Install dependencies
# =========================
COPY requirements.txt .

RUN pip install --upgrade pip \
    && pip install -r requirements.txt

# =========================
# Copy source code
# =========================
COPY . .

# =========================
# Expose port
# =========================
EXPOSE 8000

# =========================
# Start FastAPI
# =========================
CMD ["uvicorn", "app.main:app", "--host", "0.0.0.0", "--port", "8000"]