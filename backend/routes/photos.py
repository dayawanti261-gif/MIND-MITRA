from fastapi import APIRouter, UploadFile, File, Form, HTTPException, Depends
from services.photo_storage import upload_photo, get_signed_url
from services.firebase import db
from services.auth import get_current_uid, assert_can_write, assert_can_access_photo_path

router = APIRouter(prefix="/photos", tags=["Photos"])

# CHANGED: Writes into users/{uid}/memories (subcollection), matching the
# same migration as memories.py / games.py / routine.py, so photos
# uploaded here actually show up when memories.py reads them back.
# Also stores "photo_path" (not "imageUrl") since that's the field
# memories.py resolves to a signed photo_url on read — no need to
# resolve the signed URL again here at upload time.


@router.post("/upload")
def upload_photo_api(
        user_id: str = Form(...),
        file: UploadFile = File(...),
        title: str = Form(...),
        description: str = Form(...),
        people: str = Form(""),
        place: str = Form(""),
        year: int | None = Form(None),
        category: str = Form(""),
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

    memory_ref = db.collection("users").document(user_id) \
        .collection("memories").document()

    # Store people as a list to match Memory schema / JSON memories API.
    people_list = [
        p.strip() for p in (people or "").split(",") if p.strip()
    ]

    memory_ref.set({
        "id": memory_ref.id,
        "title": title,
        "description": description,
        "category": category,
        "photo_path": result["file_path"],
        "people": people_list,
        "place": place,
        "year": year
    })

    return {
        "message": "Photo uploaded and memory saved successfully!",
        "user_id": user_id,
        "filename": file.filename,
        "content_type": file.content_type,
        "file_path": result["file_path"]
    }


@router.get("/signed-url")
def get_photo_url(file_path: str, caller_uid: str = Depends(get_current_uid)):
    assert_can_access_photo_path(caller_uid, file_path)
    result = get_signed_url(file_path)

    return {
        "file_path": file_path,
        "signed_url": result["signedURL"]
    }