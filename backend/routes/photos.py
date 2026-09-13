from fastapi import APIRouter, UploadFile, File, Form, HTTPException
from services.photo_storage import upload_photo, get_signed_url
from services.firebase import db

router = APIRouter(prefix="/photos", tags=["Photos"])


@router.post("/upload")
def upload_photo_api(
        user_id: str = Form(...),
        file: UploadFile = File(...),
        title: str = Form(...),
        description: str = Form(...)
):
    allowed_types = ["image/jpeg", "image/png", "image/webp"]

    if file.content_type not in allowed_types:
        raise HTTPException(
            status_code=400,
            detail="Only JPG, PNG, and WebP images are allowed."
        )

    MAX_FILE_SIZE = 6 * 1024 * 1024

    file_content = file.file.read()

    if len(file_content) > MAX_FILE_SIZE:
        raise HTTPException(
            status_code=400,
            detail="Image size must be less than 6 MB."
        )

    file.file.seek(0)

    result = upload_photo(file, user_id)

    memory_ref = db.collection("memories").document()

    memory_ref.set({
        "user_id": user_id,
        "title": title,
        "description": description,
        "photo_path": result["file_path"]
    })

    return {
        "message": "Photo uploaded and memory saved successfully!",
        "user_id": user_id,
        "filename": file.filename,
        "content_type": file.content_type,
        "file_path": result["file_path"]
    }


@router.get("/signed-url")
def get_photo_url(file_path: str):
    result = get_signed_url(file_path)

    return {
        "file_path": file_path,
        "signed_url": result["signedURL"]
    }