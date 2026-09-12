from pydantic import BaseModel


class User(BaseModel):
    user_id: str
    name: str
    age: int


class Memory(BaseModel):
    user_id: str
    title: str
    description: str
    photo_path: str | None = None


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