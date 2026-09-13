from fastapi import APIRouter, Depends
from models.schemas import GameProgress, DifficultyRequest, DifficultyResponse
from services.firebase import db
from services.auth import get_current_uid, assert_can_write

router = APIRouter(
    prefix="/games",
    tags=["Games"]
)


def _local_difficulty(recent_results: list) -> str:
    if not recent_results:
        return "easy"
    last = recent_results[-3:]
    avg = sum(float(r.get("accuracy", 0)) for r in last) / max(len(last), 1)
    if avg >= 80:
        return "hard" if avg >= 90 and len(last) >= 3 else "medium"
    if avg < 50:
        return "easy"
    return "medium"


@router.post("/difficulty")
def recommend_difficulty(
    body: DifficultyRequest,
    caller_uid: str = Depends(get_current_uid),
):
    recent = [r.model_dump() for r in body.recent_results]
    difficulty = _local_difficulty(recent)
    return DifficultyResponse(difficulty=difficulty, source="local")


@router.get("/progress")
def get_all_game_progress(caller_uid: str = Depends(get_current_uid)):
    progress_ref = db.collection_group("gameProgress").stream()
    progress_list = []
    for item in progress_ref:
        data = item.to_dict()
        data["id"] = item.id
        progress_list.append(data)
    return {"game_progress": progress_list}


@router.get("/progress/{user_id}")
def get_game_progress(user_id: str, caller_uid: str = Depends(get_current_uid)):
    assert_can_write(caller_uid, user_id)
    progress_ref = (
        db.collection("users")
        .document(user_id)
        .collection("gameProgress")
        .stream()
    )
    progress_list = []
    for item in progress_ref:
        data = item.to_dict()
        data["id"] = item.id
        progress_list.append(data)
    return {"game_progress": progress_list}


@router.post("/progress")
def save_game_progress(progress: GameProgress, caller_uid: str = Depends(get_current_uid)):
    assert_can_write(caller_uid, progress.user_id)

    existing = (
        db.collection("users")
        .document(progress.user_id)
        .collection("gameProgress")
        .where("game_id", "==", progress.game_id)
        .limit(1)
        .stream()
    )
    progress_ref = None
    for item in existing:
        progress_ref = item.reference
        break
    if progress_ref is None:
        progress_ref = (
            db.collection("users")
            .document(progress.user_id)
            .collection("gameProgress")
            .document()
        )

    progress_ref.set(
        {
            "game_id": progress.game_id,
            "accuracy": progress.accuracy,
            "time_taken": progress.time_taken,
            "mistakes": progress.mistakes,
            "attempts": progress.attempts,
            "level": progress.level,
            "completed": progress.completed,
        }
    )

    return {
        "message": "Game progress saved successfully!",
        "progress": progress.model_dump(),
    }
