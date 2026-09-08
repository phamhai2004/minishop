from fastapi import APIRouter

from app.models.request.chat_request import ChatRequest
from app.models.response.chat_response import ChatResponse
from app.services.chat_service import chat as chat_service


router = APIRouter(
    prefix="/chat",
    tags=["Chat"]
)


@router.post("", response_model=ChatResponse)
def chat(request: ChatRequest):

    answer = chat_service(
        request.message,
        request.product_context,
        request.intent
    )

    return ChatResponse(
        message=answer
    )