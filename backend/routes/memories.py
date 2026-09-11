from fastapi import APIRouter
from models.schemas import Memory
from services.firebase import db

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
        "description": memory.description
    })

    return {
        "message": "Memory added successfully!",
        "memory": memory.model_dump()
    }