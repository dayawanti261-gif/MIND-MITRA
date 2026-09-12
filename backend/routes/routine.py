from fastapi import APIRouter
from models.schemas import Routine
from services.firebase import db

router = APIRouter(
    prefix="/routine",
    tags=["Routine"]
)


# GET all routine items
@router.get("/")
def get_routine():
    routine_ref = db.collection("routine").stream()

    routines = []

    for item in routine_ref:
        routines.append(item.to_dict())

    return {
        "routine": routines
    }


# GET one routine item
@router.get("/{routine_id}")
def get_routine_item(routine_id: str):
    routine_ref = db.collection("routine").document(routine_id)
    routine = routine_ref.get()

    if not routine.exists:
        return {
            "message": "Routine item not found"
        }

    return routine.to_dict()


# CREATE a routine item
@router.post("/")
def add_routine(routine: Routine):
    routine_ref = db.collection("routine").document()

    routine_ref.set({
        "user_id": routine.user_id,
        "title": routine.title,
        "time": routine.time
    })

    return {
        "message": "Routine added successfully!",
        "routine": routine.model_dump()
    }