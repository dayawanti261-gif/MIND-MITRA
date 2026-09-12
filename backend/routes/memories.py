from fastapi import APIRouter
from models.schemas import Memory
from services.firebase import db
from services.photo_storage import get_signed_url

router = APIRouter(
    prefix="/memories",
    tags=["Memories"]
)


# GET all memories
@router.get("/")
def get_memories():
    memories_ref = db.collection("memories").stream()

    memories = []

    for memory in memories_ref:
        memories.append(memory.to_dict())

    return {
        "memories": memories
    }

@router.get("/user/{user_id}")
def get_user_memories(user_id: str):

    memories_ref = db.collection("memories").where(
        "user_id", "==", user_id
    ).stream()

    memories = []

    for memory in memories_ref:
        data = memory.to_dict()

        photo_path = data.get("photo_path")
        photo_url = None

        if photo_path:
            try:
                signed_url_result = get_signed_url(photo_path)
                photo_url = signed_url_result["signedURL"]
            except Exception:
                photo_url = None

        memories.append({
            "memory_id": memory.id,
            "user_id": data.get("user_id"),
            "title": data.get("title"),
            "description": data.get("description"),
            "category": data.get("category"),
            "photo_path": photo_path,
            "photo_url": photo_url,
            "people": data.get("people"),
            "place": data.get("place"),
            "year": data.get("year")
        })

    return {
        "user_id": user_id,
        "memories": memories
    }


# GET one memory
@router.get("/{memory_id}")
def get_memory(memory_id: str):
    memory_ref = db.collection("memories").document(memory_id)
    memory = memory_ref.get()

    if not memory.exists:
        return {
            "message": "Memory not found"
        }

    return memory.to_dict()


# CREATE a memory
@router.post("/")
def add_memory(memory: Memory):
    memory_ref = db.collection("memories").document()

    memory_ref.set({
        "user_id": memory.user_id,
        "title": memory.title,
        "description": memory.description,
        "photo_path": memory.photo_path
    })

    return {
        "message": "Memory added successfully!",
        "memory": memory.model_dump()
    }