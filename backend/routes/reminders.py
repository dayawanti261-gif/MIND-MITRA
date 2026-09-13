import time

from fastapi import APIRouter, Depends, HTTPException
from models.schemas import Reminder, ReminderComplete
from services.firebase import db
from services.auth import get_current_uid, assert_can_write

router = APIRouter(
    prefix="/reminders",
    tags=["Reminders"]
)


def _serialize_reminder(doc_id: str, data: dict) -> dict:
    payload = dict(data)
    payload["id"] = doc_id
    return payload


@router.get("/user/{user_id}")
def get_reminders(user_id: str, caller_uid: str = Depends(get_current_uid)):
    assert_can_write(caller_uid, user_id)

    reminders_ref = (
        db.collection("users")
        .document(user_id)
        .collection("reminders")
        .order_by("timestamp")
        .stream()
    )

    reminders = [_serialize_reminder(item.id, item.to_dict() or {}) for item in reminders_ref]

    return {"reminders": reminders}


@router.post("/")
def add_reminder(reminder: Reminder, caller_uid: str = Depends(get_current_uid)):
    assert_can_write(caller_uid, reminder.user_id)

    reminder_ref = (
        db.collection("users")
        .document(reminder.user_id)
        .collection("reminders")
        .document()
    )

    reminder_ref.set(
        {
            "id": reminder_ref.id,
            "title": reminder.title,
            "description": reminder.description or "",
            "date": reminder.date or "",
            "time": reminder.time,
            "repeat": reminder.repeat or "none",
            "enabled": reminder.enabled,
            "createdBy": caller_uid,
            "lastCompletedDate": None,
            "timestamp": int(time.time() * 1000),
        }
    )

    return {
        "message": "Reminder created successfully!",
        "reminder": reminder.model_dump(),
        "id": reminder_ref.id,
    }


@router.patch("/{user_id}/{reminder_id}")
def update_reminder_completion(
    user_id: str,
    reminder_id: str,
    body: ReminderComplete,
    caller_uid: str = Depends(get_current_uid),
):
    assert_can_write(caller_uid, user_id)

    reminder_ref = (
        db.collection("users")
        .document(user_id)
        .collection("reminders")
        .document(reminder_id)
    )
    reminder = reminder_ref.get()

    if not reminder.exists:
        raise HTTPException(status_code=404, detail="Reminder not found.")

    from datetime import date

    reminder_ref.update(
        {
            "lastCompletedDate": date.today().isoformat() if body.completed else None,
        }
    )

    return {"message": "Reminder updated.", "id": reminder_id}
