from fastapi import FastAPI, Request
from fastapi.responses import JSONResponse

from app.exceptions.api_exception import ApiException


def register_exception_handlers(app: FastAPI):
    @app.exception_handler(ApiException)
    async def api_exception_handler(
        request: Request,
        exc: ApiException,
    ):
        return JSONResponse(
            status_code=exc.status_code,
            content={
                "success": False,
                "message": exc.message,
            },
        )

    @app.exception_handler(Exception)
    async def global_exception_handler(
        request: Request,
        exc: Exception,
    ):
        return JSONResponse(
            status_code=500,
            content={
                "success": False,
                "message": "Internal server error.",
            },
        )