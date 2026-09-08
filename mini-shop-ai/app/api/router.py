from fastapi import APIRouter

from app.api.routes import embedding
from app.api.routes import health
from app.api.routes import chat

api_router = APIRouter()

api_router.include_router(
    health.router,
    tags=["Health"],
)

api_router.include_router(
    embedding.router,
    tags=["Embedding"],
)

api_router.include_router(
    chat.router,
    tags=["Chat"],
)