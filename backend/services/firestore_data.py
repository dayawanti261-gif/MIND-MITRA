"""Firestore-backed data access for the Saathi AI agent tools."""

import time as time_module

from services.firebase import db


def _user_doc(user_id: str) -> dict | None:
    doc = db.collection("users").document(user_id).get()
    return doc.to_dict() if doc.exists else None


def get_today_schedule(user_id: str) -> dict:
    routines = (
        db.collection("users")
        .document(user_id)
        .collection("routines")
        .order_by("timestamp")
        .stream()
    )
    schedule = []
    for item in routines:
        data = item.to_dict() or {}
        schedule.append(
            {
                "id": item.id,
                "activity": data.get("title", ""),
                "time": data.get("time", ""),
                "completed": bool(data.get("lastCompletedDate")),
            }
        )
    return {"success": True, "schedule": schedule}


def get_pending_tasks(user_id: str) -> dict:
    result = get_today_schedule(user_id)
    if not result.get("success"):
        return result
    pending = [
        f"{entry['time']} — {entry['activity']}"
        for entry in result["schedule"]
        if not entry.get("completed")
    ]
    return {"success": True, "tasks": pending}


def get_family_member(user_id: str, relation: str) -> dict:
    members = (
        db.collection("users")
        .document(user_id)
        .collection("family")
        .stream()
    )
    for member in members:
        data = member.to_dict() or {}
        if str(data.get("relation", "")).lower() == relation.lower():
            return {
                "success": True,
                "member": {
                    "id": member.id,
                    "name": data.get("name", ""),
                    "relation": data.get("relation", ""),
                },
            }
    return {"success": False, "message": "Family member not found."}


def search_memories(user_id: str, query: str) -> dict:
    memories_ref = (
        db.collection("users").document(user_id).collection("memories").stream()
    )
    results = []
    query_lower = (query or "").lower()
    for memory in memories_ref:
        data = memory.to_dict() or {}
        people = data.get("people") or []
        if isinstance(people, str):
            people = [p.strip() for p in people.split(",") if p.strip()]
        searchable = " ".join(
            [
                str(data.get("title", "")),
                str(data.get("description", "")),
                " ".join(people),
                str(data.get("place", "")),
            ]
        ).lower()
        if not query_lower or query_lower in searchable:
            results.append(
                {
                    "id": memory.id,
                    "title": data.get("title", ""),
                    "description": data.get("description", ""),
                    "people": people,
                    "category": data.get("category", ""),
                }
            )
    if not results and query_lower:
        return {"success": True, "results": []}
    return {"success": True, "results": results}


def get_game_progress(user_id: str) -> dict:
    progress_ref = (
        db.collection("users")
        .document(user_id)
        .collection("gameProgress")
        .stream()
    )
    progress = []
    for item in progress_ref:
        data = item.to_dict() or {}
        progress.append(
            {
                "game": data.get("game_id", "unknown"),
                "accuracy": data.get("accuracy", 0),
                "level": data.get("level", 1),
                "attempts": data.get("attempts", 0),
            }
        )
    return {"success": True, "progress": progress}


def recommend_game(user_id: str) -> dict:
    result = get_game_progress(user_id)
    if not result.get("success"):
        return result
    progress = result["progress"]
    if not progress:
        return {
            "success": True,
            "recommended_game": "family_memory_matching",
            "level": 1,
            "accuracy": 0,
            "reason": "Try the Family Memory Matching game to get started.",
        }
    weakest = min(progress, key=lambda g: g.get("accuracy", 0))
    return {
        "success": True,
        "recommended_game": weakest["game"],
        "level": weakest.get("level", 1),
        "accuracy": weakest.get("accuracy", 0),
        "reason": f"{weakest['game']} has {weakest.get('accuracy', 0)}% accuracy.",
    }


def open_game(user_id: str, game: str, level: int) -> dict:
    return {
        "success": True,
        "action": {
            "type": "OPEN_GAME",
            "payload": {"game": game, "level": level},
        },
    }


def show_memory(user_id: str, memory_id: str) -> dict:
    memory_ref = (
        db.collection("users")
        .document(user_id)
        .collection("memories")
        .document(memory_id)
    )
    memory = memory_ref.get()
    if not memory.exists:
        return {"success": False, "message": "Memory not found."}
    data = memory.to_dict() or {}
    data["id"] = memory.id
    return {
        "success": True,
        "action": {"type": "SHOW_MEMORY", "payload": data},
    }


def create_reminder(user_id: str, task: str, time: str) -> dict:
    reminder_ref = (
        db.collection("users").document(user_id).collection("reminders").document()
    )
    reminder_ref.set(
        {
            "id": reminder_ref.id,
            "title": task,
            "description": "",
            "date": "",
            "time": time,
            "repeat": "none",
            "enabled": True,
            "lastCompletedDate": None,
            "timestamp": int(time_module.time() * 1000),
        }
    )
    return {
        "success": True,
        "message": f"Reminder created for {task} at {time}.",
        "reminder": {"id": reminder_ref.id, "activity": task, "time": time},
    }
