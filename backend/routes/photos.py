from fastapi import APIRouter, UploadFile, File, Form, HTTPException, Depends
from services.photo_storage import upload_photo, get_signed_url
from services.firebase import db
from services.auth import get_current_uid, assert_can_write

router = APIRouter(prefix="/photos", tags=["Photos"])


@router.post("/upload")
def upload_photo_api(
        user_id: str = Form(...),
        file: UploadFile = File(...),
        title: str = Form(...),
        description: str = Form(...),
        caller_uid: str = Depends(get_current_uid)   # NEW
):
    # NEW: confirms caller_uid is either the patient themself, or a
    # caregiver whose users/{caller_uid} doc has linkedPatientId == user_id.
    # Previously anyone could set user_id to any value in the form.
    assert_can_write(caller_uid, user_id)

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

    signed = get_signed_url(result["file_path"])
    image_url = signed.get("signedURL", "")

    memory_ref = db.collection("users").document(user_id) \
        .collection("memories").document()

    memory_ref.set({
        "id": memory_ref.id,
        "title": title,
        "category": "",
        "description": description,
        "imageUrl": image_url
    })

    return {
        "message": "Photo uploaded and memory saved successfully!",
        "user_id": user_id,
        "filename": file.filename,
        "content_type": file.content_type,
        "file_path": result["file_path"],
        "imageUrl": image_url
    }


@router.get("/signed-url")
def get_photo_url(file_path: str, caller_uid: str = Depends(get_current_uid)):
    result = get_signed_url(file_path)

    return {
        "file_path": file_path,
        "signed_url": result["signedURL"]
    }