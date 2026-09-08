from io import BytesIO

from fastapi import APIRouter
from fastapi import File
from fastapi import UploadFile
from PIL import Image

from app.dependencies.container import embedding_service

from app.models.response.base_response import BaseResponse
from app.models.response.embedding_response import EmbeddingResponse

router = APIRouter()


@router.post(
    "/embedding",
    response_model=BaseResponse[EmbeddingResponse],
)
async def create_embedding(
    image: UploadFile = File(...)
):
    """
    Sinh embedding từ ảnh upload
    """

    contents = await image.read()

    pil_image = Image.open(BytesIO(contents))

    embedding = embedding_service.generate_embedding(
        pil_image
    )

    return BaseResponse(
        success=True,
        message="Embedding generated successfully.",
        data=EmbeddingResponse(
            dimension=len(embedding),
            embedding=embedding,
        ),
    )