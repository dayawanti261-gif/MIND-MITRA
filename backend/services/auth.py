from fastapi import Header, HTTPException
from firebase_admin import auth as firebase_auth
from services.firebase import db


def get_current_uid(authorization: str = Header(...)) -> str:
    """
    Every request from the app must send:
        Authorization: Bearer <Firebase ID token>
    (the app gets this from FirebaseAuth.getInstance().currentUser.getIdToken())

    Without this, any caller could pass any user_id in a form/body and
    write into a stranger's data - there was previously nothing checking
    who was actually making the request.
    """
    if not authorization.startswith("Bearer "):
        raise HTTPException(status_code=401, detail="Missing bearer token")

    token = authorization.removeprefix("Bearer ").strip()

    try:
        decoded = firebase_auth.verify_id_token(token)
    except Exception:
        raise HTTPException(status_code=401, detail="Invalid or expired token")

    return decoded["uid"]


def assert_can_write(caller_uid: str, target_user_id: str) -> None:
    """
    Same rule as firestore.rules: caller can write to target_user_id's data
    if caller IS target_user_id, OR caller's own users/{caller_uid} doc has
    linkedPatientId == target_user_id.
    """
    if caller_uid == target_user_id:
        return

    caller_doc = db.collection("users").document(caller_uid).get()

    if not caller_doc.exists:
        raise HTTPException(status_code=403, detail="Not authorized for this patient")

    if caller_doc.to_dict().get("linkedPatientId") != target_user_id:
        raise HTTPException(status_code=403, detail="Not authorized for this patient")


def assert_can_access_photo_path(caller_uid: str, file_path: str) -> None:
    """Signed URLs may only be issued for the caller's own or linked patient's media."""
    if not file_path or not file_path.startswith("memories/"):
        raise HTTPException(status_code=400, detail="Invalid photo path")

    parts = file_path.split("/")
    if len(parts) < 2 or not parts[1]:
        raise HTTPException(status_code=400, detail="Invalid photo path")

    owner_id = parts[1]
    assert_can_write(caller_uid, owner_id)