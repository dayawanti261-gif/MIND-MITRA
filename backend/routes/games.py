from fastapi import APIRouter
from models.schemas import GameProgress
from services.firebase import db

router = APIRouter(
    prefix="/games",
    tags=["Games"]
)


# GET all game progress
@router.get("/progress")
def get_game_progress():
    progress_ref = db.collection("game_progress").stream()

    progress_list = []

    for item in progress_ref:
        progress_list.append(item.to_dict())

    return {
        "game_progress": progress_list
    }


# SAVE game progress
@router.post("/progress")
def save_game_progress(progress: GameProgress):

    progress_ref = db.collection("game_progress").document()

    progress_ref.set({
        "user_id": progress.user_id,
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