from pydantic import BaseModel


class EmbeddingResponse(BaseModel):
    dimension: int

    embedding: list[float]