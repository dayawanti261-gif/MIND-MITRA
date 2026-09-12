import time

from fastapi import APIRouter, Depends
from models.schemas import Routine
from services.firebase import db
from services.auth import get_current_uid, assert_can_write

router = APIRouter(
    prefix="/routine",
    tags=["Routine"]
)

# CHANGED: The Android app stores routine items under users/{uid}/routines
# (subcollection, plural "routines"), not a top-level "routine" collection.
# Also added "isCompleted" so items created here match RoutineItem's shape.


# GET all routine items for one user
@router.get("/user/{user_id}")
def get_routine(user_id: str, caller_uid: str = Depends(get_current_uid)):
    assert_can_write(caller_uid, user_id)

    routine_ref = db.collection("users").document(user_id) \
        .collection("routines").order_by("timestamp").stream()

    routines = []

    for item in routine_ref:
        data = item.to_dict()
        data["id"] = item.id
        routines.append(data)

    return {
        "routine": routines
    }


# GET one routine item
@router.get("/{user_id}/{routine_id}")
def get_routine_item(user_id: str, routine_id: str):
    routine_ref = db.collection("users").document(user_id) \
        .collection("routines").document(routine_id)
    routine = routine_ref.get()

    if not routine.exists:
        return {
            "message": "Routine item not found"
        }

    data = routine.to_dict()
    data["id"] = routine.id
    return data


# CREATE a routine item
@router.post("/")
def add_routine(routine: Routine, caller_uid: str = Depends(get_current_uid)):
    assert_can_write(caller_uid, routine.user_id)   # NEW

    routine_ref = db.collection("users").document(routine.user_id) \
        .collection("routines").document()

    routine_ref.set({
        "id": routine_ref.id,
        "title": routine.title,
        "time": routine.time,
        # Date (yyyy-MM-dd) the activity was last completed, or None.
        # The Android app derives "completed" from whether this equals
        # today's date, so activities automatically reset each day.
        "lastCompletedDate": None,
        # Plain epoch-millis Long, same as Kotlin's System.currentTimeMillis(),
        # because RoutineItem.timestamp is a Long — a Firestore server
        # Timestamp object here would fail to deserialize on the Android side.
        "timestamp": int(time.time() * 1000)
    })

    return {
        "message": "Routine added successfully!",
        "routine": routine.model_dump()
    }