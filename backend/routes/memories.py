from fastapi import APIRouter, Depends
from models.schemas import Memory
from services.firebase import db
from services.auth import get_current_uid, assert_can_write
from services.photo_storage import get_signed_url

router = APIRouter(
    prefix="/memories",
    tags=["Memories"]
)

# CHANGED: The Android app reads memories from users/{uid}/memories
# (subcollection), not a top-level "memories" collection — same
# migration as games.py / routine.py. Auth checks added so a caller
# can only read/write a patient's memories if they ARE that patient
# or are their linked caregiver.


def _with_photo_url(data: dict) -> dict:
    photo_path = data.get("photo_path")
    data["photo_url"] = None

    if photo_path:
        try:
            signed_url_result = get_signed_url(photo_path)
            data["photo_url"] = signed_url_result["signedURL"]
        except Exception:
            data["photo_url"] = None

    return data


# GET all memories across all users (admin/debug use)
@router.get("/")
def get_memories():
    memories_ref = db.collection_group("memories").stream()

    memories = []

    for memory in memories_ref:
        data = memory.to_dict()
        data["id"] = memory.id
        memories.append(data)

    return {
        "memories": memories
    }


# GET all memories for one user
@router.get("/user/{user_id}")
def get_user_memories(user_id: str, caller_uid: str = Depends(get_current_uid)):
    assert_can_write(caller_uid, user_id)   # same check gates reads too

    memories_ref = db.collection("users").document(user_id) \
        .collection("memories").stream()

    memories = []

    for memory in memories_ref:
        data = memory.to_dict()
        data["id"] = memory.id
        data = _with_photo_url(data)
        memories.append(data)

    return {
        "user_id": user_id,
        "memories": memories
    }


# GET one memory
@router.get("/{user_id}/{memory_id}")
def get_memory(user_id: str, memory_id: str, caller_uid: str = Depends(get_current_uid)):
    assert_can_write(caller_uid, user_id)

    memory_ref = db.collection("users").document(user_id) \
        .collection("memories").document(memory_id)
    memory = memory_ref.get()

    if not memory.exists:
        return {
            "message": "Memory not found"
        }

    data = memory.to_dict()
    data["id"] = memory.id
    data = _with_photo_url(data)
    return data


# CREATE a memory
@router.post("/")
def add_memory(memory: Memory, caller_uid: str = Depends(get_current_uid)):
    # NEW: caller must be the patient themself or their linked caregiver.
    assert_can_write(caller_uid, memory.user_id)

    memory_ref = db.collection("users").document(memory.user_id) \
        .collection("memories").document()

    memory_ref.set({
        "id": memory_ref.id,
        "title": memory.title,
        "description": memory.description,
        "category": memory.category,
        "photo_path": memory.photo_path,
        "people": memory.people,
        "place": memory.place,
        "year": memory.year
    })

    return {
        "message": "Memory added successfully!",
        "memory": memory.model_dump()
    }