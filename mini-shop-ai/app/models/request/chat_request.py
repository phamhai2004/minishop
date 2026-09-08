from pydantic import BaseModel


class ChatRequest(BaseModel):
    message: str
    product_context: str = ""
    intent: str