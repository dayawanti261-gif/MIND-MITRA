from fastapi import UploadFile
from services.supabase_storage import supabase, BUCKET_NAME
from uuid import uuid4


def upload_photo(file: UploadFile, user_id: str):
    unique_filename = f"{uuid4()}_{file.filename}"
    file_path = f"memories/{user_id}/{unique_filename}"

    file_content = file.file.read()

    response = supabase.storage.from_(BUCKET_NAME).upload(
        path=file_path,
        file=file_content,
        file_options={
            "content-type": file.content_type or "application/octet-stream",
            "upsert": "false"
        }
    )

    return {
        "file_path": file_path,
        "response": response
    }

def get_signed_url(file_path: str):
    response = supabase.storage.from_(BUCKET_NAME).create_signed_url(
        file_path,
        3600
    )

    return response


def delete_photo(file_path: str) -> bool:
    """Delete a stored photo. Returns True if deleted or already absent."""
    if not file_path:
        return True
    try:
        supabase.storage.from_(BUCKET_NAME).remove([file_path])
        return True
    except Exception:
        return False