from pydantic import BaseModel


class User(BaseModel):
    user_id: str
    name: str
    age: int


class Memory(BaseModel):
    user_id: str
    title: str
    description: str
    category: str | None = None
    photo_path: str | None = None
    people: list[str] | None = None
    place: str | None = None
    year: int | None = None


class Routine(BaseModel):
    user_id: str
    title: str
    time: str
    days_of_week: str = "Every day"
    enabled: bool = True
    reminder_note: str | None = None


class GameProgress(BaseModel):
    user_id: str
    game_id: str
    accuracy: float
    time_taken: float
    mistakes: int
    attempts: int
    level: int
    completed: bool


class LinkPatientRequest(BaseModel):
    patient_email: str
    connection_pin: str


class Reminder(BaseModel):
    user_id: str
    title: str
    time: str
    description: str | None = None
    date: str | None = None
    repeat: str = "none"
    enabled: bool = True


class ReminderComplete(BaseModel):
    user_id: str
    completed: bool = True


class AgentChatRequest(BaseModel):
    message: str
    language: str | None = None


class GameResultEntry(BaseModel):
    score: float = 0
    accuracy: float = 0
    mistakes: int = 0
    completed: bool = False


class DifficultyRequest(BaseModel):
    game_type: str
    recent_results: list[GameResultEntry] = []


class DifficultyResponse(BaseModel):
    difficulty: str
    source: str = "local"