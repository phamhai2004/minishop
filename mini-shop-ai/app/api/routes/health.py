from fastapi import APIRouter

from app.models.response.base_response import BaseResponse

router = APIRouter()


@router.get(
    "/health",
    response_model=BaseResponse[dict],
)
def health():
    return BaseResponse(
        success=True,
        message="Health check successful.",
        data={
            "status": "UP"
        },
    )