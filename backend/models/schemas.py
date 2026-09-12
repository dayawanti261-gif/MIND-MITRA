from typing import Optional
from pydantic import BaseModel


class User(BaseModel):
    user_id: str
    name: str
    age: int


class Memory(BaseModel):
    user_id: str
    title: str
    description: str
    category: str = ""
    # CHANGED: was "photo_path". The Android app's MemoryItem/FirebaseRepository
    # reads/writes a field called "imageUrl", not "photo_path", so memories
    # created here were invisible to the app. Renamed to match.
    imageUrl: Optional[str] = None
    people: list[str] = []


class Routine(BaseModel):
    user_id: str
    title: str
    time: str


class GameProgress(BaseModel):
    user_id: str
    game_id: str
    accuracy: float
    time_taken: float
    mistakes: int
    attempts: int
    level: int
    completed: bool