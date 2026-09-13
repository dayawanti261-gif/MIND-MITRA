from fastapi import APIRouter
from models.schemas import User
from services.firebase import db

router = APIRouter(
    prefix="/users",
    tags=["Users"]
)


# GET all users
@router.get("/")
def get_users():
    users_ref = db.collection("users").stream()

    users = []

    for user in users_ref:
        user_data = user.to_dict()
        users.append(user_data)

    return {
        "users": users
    }


# GET one user
@router.get("/{user_id}")
def get_user(user_id: str):
    user_ref = db.collection("users").document(user_id)
    user = user_ref.get()

    if not user.exists:
        return {
            "message": "User not found"
        }

    return user.to_dict()


# CREATE a user
@router.post("/")
def create_user(user: User):
    user_ref = db.collection("users").document(user.user_id)

    # CHANGED: was a plain .set(), which overwrites the whole document.
    # The Android app stores connectionPin, linkedPatientId/linkedCaregiverId,
    # email, and role on this same users/{uid} doc. If this endpoint is ever
    # called after the app has already written a profile, a non-merge .set()
    # silently wipes the caregiver<->patient link and connectionPin. merge=True
    # only touches the fields listed here.
    user_ref.set({
        "user_id": user.user_id,
        "name": user.name,
        "age": user.age
    }, merge=True)

    return {
        "message": "User created successfully!",
        "user": user.model_dump()
    }