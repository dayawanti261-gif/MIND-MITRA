# backend/tools.py

from data import USER


def get_today_schedule(user_id: str):
    if user_id != USER["user_id"]:
        return {
            "success": False,
            "message": "User not found."
        }

    return {
        "success": True,
        "schedule": USER["schedule"]
    }


def get_pending_tasks(user_id: str):
    if user_id != USER["user_id"]:
        return {
            "success": False,
            "message": "User not found."
        }

    return {
        "success": True,
        "tasks": [
            "Complete today's memory game",
            "Attend doctor appointment at 16:00"
        ]
    }


def get_family_member(user_id: str, relation: str):
    if user_id != USER["user_id"]:
        return {
            "success": False,
            "message": "User not found."
        }

    for member in USER["family"]:

        if member["relation"].lower() == relation.lower():

            return {
                "success": True,
                "member": member
            }

    return {
        "success": False,
        "message": "Family member not found."
    }


def search_memories(user_id: str, query: str):
    if user_id != USER["user_id"]:
        return {
            "success": False,
            "message": "User not found."
        }

    query = query.lower()

    results = []

    for memory in USER["memories"]:

        searchable_text = (
            memory["title"]
            + " "
            + memory["description"]
            + " "
            + " ".join(memory["people"])
        ).lower()

        if query in searchable_text:
            results.append(memory)

    # If no exact result, return all memories for broader matching.
    if not results:
        results = USER["memories"]

    return {
        "success": True,
        "results": results
    }


def get_game_progress(user_id: str):
    if user_id != USER["user_id"]:
        return {
            "success": False,
            "message": "User not found."
        }

    return {
        "success": True,
        "progress": USER["game_progress"]
    }


def recommend_game(user_id: str):
    if user_id != USER["user_id"]:
        return {
            "success": False,
            "message": "User not found."
        }

    progress = USER["game_progress"]

    # For demo purposes, recommend the game
    # with the lowest accuracy.
    weakest_game = min(
        progress,
        key=lambda game: game["accuracy"]
    )

    return {
        "success": True,
        "recommended_game": weakest_game["game"],
        "level": weakest_game["level"],
        "accuracy": weakest_game["accuracy"],
        "reason": (
            f"{weakest_game['game']} has "
            f"{weakest_game['accuracy']}% accuracy."
        )
    }


def open_game(user_id: str, game: str, level: int):
    if user_id != USER["user_id"]:
        return {
            "success": False,
            "message": "User not found."
        }

    return {
        "success": True,
        "action": {
            "type": "OPEN_GAME",
            "payload": {
                "game": game,
                "level": level
            }
        }
    }


def show_memory(user_id: str, memory_id: str):
    if user_id != USER["user_id"]:
        return {
            "success": False,
            "message": "User not found."
        }

    for memory in USER["memories"]:

        if memory["id"] == memory_id:

            return {
                "success": True,
                "action": {
                    "type": "SHOW_MEMORY",
                    "payload": memory
                }
            }

    return {
        "success": False,
        "message": "Memory not found."
    }


def create_reminder(user_id: str, task: str, time: str):
    if user_id != USER["user_id"]:
        return {
            "success": False,
            "message": "User not found."
        }

    new_reminder = {
        "id": f"reminder_{len(USER['schedule']) + 1}",
        "activity": task,
        "time": time
    }

    USER["schedule"].append(new_reminder)

    return {
        "success": True,
        "message": f"Reminder created for {task} at {time}.",
        "reminder": new_reminder
    }