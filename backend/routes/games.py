from fastapi import APIRouter, Depends
from models.schemas import GameProgress
from services.firebase import db
from services.auth import get_current_uid, assert_can_write

router = APIRouter(
    prefix="/games",
    tags=["Games"]
)

# CHANGED: The Android app reads game progress from
# users/{uid}/gameProgress (subcollection), not a top-level
# "game_progress" collection.


# GET all game progress across all users (admin/debug use)
@router.get("/progress")
def get_all_game_progress():
    progress_ref = db.collection_group("gameProgress").stream()

    progress_list = []

    for item in progress_ref:
        data = item.to_dict()
        data["id"] = item.id
        progress_list.append(data)

    return {
        "game_progress": progress_list
    }


# GET all game progress for one user
@router.get("/progress/{user_id}")
def get_game_progress(user_id: str, caller_uid: str = Depends(get_current_uid)):
    assert_can_write(caller_uid, user_id)

    progress_ref = db.collection("users").document(user_id) \
        .collection("gameProgress").stream()

    progress_list = []

    for item in progress_ref:
        data = item.to_dict()
        data["id"] = item.id
        progress_list.append(data)

    return {
        "game_progress": progress_list
    }


# SAVE game progress
@router.post("/progress")
def save_game_progress(progress: GameProgress, caller_uid: str = Depends(get_current_uid)):
    assert_can_write(caller_uid, progress.user_id)   # NEW

    progress_ref = db.collection("users").document(progress.user_id) \
        .collection("gameProgress").document()

    progress_ref.set({
        "game_id": progress.game_id,
        "accuracy": progress.accuracy,
        "time_taken": progress.time_taken,
        "mistakes": progress.mistakes,
        "attempts": progress.attempts,
        "level": progress.level,
        "completed": progress.completed
    })

    return {
        "message": "Game progress saved successfully!",
        "progress": progress.model_dump()
    }