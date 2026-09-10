# backend/agent.py

import json
import os

from dotenv import load_dotenv
from google import genai
from google.genai import types

from tools import (
    get_today_schedule,
    get_pending_tasks,
    get_family_member,
    search_memories,
    get_game_progress,
    recommend_game,
    open_game,
    show_memory,
    create_reminder
)


# --------------------------------------------------
# LOAD ENVIRONMENT VARIABLES
# --------------------------------------------------

load_dotenv()

API_KEY = os.getenv("GEMINI_API_KEY")

MODEL_NAME = os.getenv(
    "MODEL_NAME",
    "gemini-2.5-flash-lite"
)

if not API_KEY:
    raise ValueError(
        "GEMINI_API_KEY is missing. "
        "Please add it to your .env file."
    )


# --------------------------------------------------
# GEMINI CLIENT
# --------------------------------------------------

client = genai.Client(
    api_key=API_KEY
)


# --------------------------------------------------
# SYSTEM INSTRUCTION
# --------------------------------------------------

SYSTEM_INSTRUCTION = """
You are Saathi, the personal AI care assistant inside
MIND-MITRA.

MIND-MITRA is an AI-based cognitive gaming and memory
assistance platform for elderly people.

You are NOT just a chatbot.

Your behavior must follow:

UNDERSTAND → DECIDE → ACT → RESPOND

Your responsibilities:

1. Help the elderly user using simple Hindi, Hinglish
   or English.

2. Use personalized information such as:
   - name
   - family members
   - memories
   - photos
   - daily schedule
   - preferences
   - cognitive game progress

3. Use tools whenever personalized information is needed.

4. Help with:
   - daily schedule
   - pending tasks
   - family information
   - memories
   - cognitive games
   - game recommendations
   - reminders
   - application navigation

5. Recommend games using game progress.

6. If the user asks to see a memory or family photo,
   search the memory database.

7. If the user wants to play a game, check their
   progress and recommend an appropriate game.

8. Never invent:
   - memories
   - family members
   - appointments
   - personal information

9. Never provide medical diagnosis.

10. Do not replace doctors, caregivers, counselors,
    or family members.

11. Be warm, respectful, patient and encouraging.

12. Keep answers short and easy for elderly users.

13. Encourage connection with family and caregivers.

IMPORTANT:
At the end of your reasoning, provide ONLY a JSON object
with this structure:

{
    "message": "short response to the user",
    "action": {
        "type": "ACTION_NAME",
        "payload": {}
    }
}

If no application action is required:

{
    "message": "short response to the user",
    "action": null
}

Allowed action types:

SHOW_MEMORY
OPEN_GAME
SHOW_PROGRESS
SHOW_SCHEDULE
NAVIGATE_HOME
NAVIGATE_GAMES
NAVIGATE_MEMORY
NAVIGATE_SCHEDULE
NAVIGATE_PROGRESS
NAVIGATE_PROFILE
"""


# --------------------------------------------------
# TOOL DECLARATIONS
# --------------------------------------------------

TOOL_DECLARATIONS = [

    {
        "name": "get_today_schedule",
        "description": "Get today's schedule for the elderly user.",
        "parameters": {
            "type": "object",
            "properties": {
                "user_id": {
                    "type": "string"
                }
            },
            "required": ["user_id"]
        }
    },

    {
        "name": "get_pending_tasks",
        "description": "Get pending tasks for the user.",
        "parameters": {
            "type": "object",
            "properties": {
                "user_id": {
                    "type": "string"
                }
            },
            "required": ["user_id"]
        }
    },

    {
        "name": "get_family_member",
        "description": "Find a family member by relationship.",
        "parameters": {
            "type": "object",
            "properties": {
                "user_id": {
                    "type": "string"
                },
                "relation": {
                    "type": "string"
                }
            },
            "required": [
                "user_id",
                "relation"
            ]
        }
    },

    {
        "name": "search_memories",
        "description": "Search the user's personalized memories and family photos.",
        "parameters": {
            "type": "object",
            "properties": {
                "user_id": {
                    "type": "string"
                },
                "query": {
                    "type": "string"
                }
            },
            "required": [
                "user_id",
                "query"
            ]
        }
    },

    {
        "name": "get_game_progress",
        "description": "Get the user's cognitive game progress.",
        "parameters": {
            "type": "object",
            "properties": {
                "user_id": {
                    "type": "string"
                }
            },
            "required": ["user_id"]
        }
    },

    {
        "name": "recommend_game",
        "description": "Recommend a cognitive game based on performance.",
        "parameters": {
            "type": "object",
            "properties": {
                "user_id": {
                    "type": "string"
                }
            },
            "required": ["user_id"]
        }
    },

    {
        "name": "open_game",
        "description": "Open a cognitive game at a particular level.",
        "parameters": {
            "type": "object",
            "properties": {
                "user_id": {
                    "type": "string"
                },
                "game": {
                    "type": "string"
                },
                "level": {
                    "type": "integer"
                }
            },
            "required": [
                "user_id",
                "game",
                "level"
            ]
        }
    },

    {
        "name": "show_memory",
        "description": "Display a specific memory.",
        "parameters": {
            "type": "object",
            "properties": {
                "user_id": {
                    "type": "string"
                },
                "memory_id": {
                    "type": "string"
                }
            },
            "required": [
                "user_id",
                "memory_id"
            ]
        }
    },

    {
        "name": "create_reminder",
        "description": "Create a reminder for the user.",
        "parameters": {
            "type": "object",
            "properties": {
                "user_id": {
                    "type": "string"
                },
                "task": {
                    "type": "string"
                },
                "time": {
                    "type": "string"
                }
            },
            "required": [
                "user_id",
                "task",
                "time"
            ]
        }
    }
]


# --------------------------------------------------
# TOOL MAP
# --------------------------------------------------

TOOL_MAP = {
    "get_today_schedule": get_today_schedule,
    "get_pending_tasks": get_pending_tasks,
    "get_family_member": get_family_member,
    "search_memories": search_memories,
    "get_game_progress": get_game_progress,
    "recommend_game": recommend_game,
    "open_game": open_game,
    "show_memory": show_memory,
    "create_reminder": create_reminder
}


# --------------------------------------------------
# EXECUTE TOOL
# --------------------------------------------------

def execute_tool(tool_name, arguments):

    function = TOOL_MAP.get(tool_name)

    if function is None:

        return {
            "success": False,
            "message": f"Unknown tool: {tool_name}"
        }

    try:

        return function(**arguments)

    except Exception as error:

        return {
            "success": False,
            "message": str(error)
        }


# --------------------------------------------------
# PARSE FINAL RESPONSE
# --------------------------------------------------

def parse_final_response(text):

    text = text.strip()

    # Remove markdown code fences if Gemini adds them.
    if text.startswith("```json"):
        text = text[7:]

    if text.startswith("```"):
        text = text[3:]

    if text.endswith("```"):
        text = text[:-3]

    text = text.strip()

    try:

        result = json.loads(text)

        if "message" not in result:
            result["message"] = "I'm here to help you."

        if "action" not in result:
            result["action"] = None

        return result

    except json.JSONDecodeError:

        return {
            "message": text,
            "action": None
        }


# --------------------------------------------------
# MAIN AGENT
# --------------------------------------------------

def run_agent(user_id: str, message: str):

    initial_prompt = f"""
User ID: {user_id}

User request:
{message}
"""

    contents = [
        types.Content(
            role="user",
            parts=[
                types.Part.from_text(
                    text=initial_prompt
                )
            ]
        )
    ]

    tools = [
        types.Tool(
            function_declarations=TOOL_DECLARATIONS
        )
    ]

    # Maximum number of tool-calling rounds.
    # This prevents accidental infinite loops.
    for _ in range(5):

        response = client.models.generate_content(

            model=MODEL_NAME,

            contents=contents,

            config=types.GenerateContentConfig(

                system_instruction=SYSTEM_INSTRUCTION,

                tools=tools,

                temperature=0.2
            )
        )

        # Check whether Gemini requested a tool.
        function_calls = []

        for candidate in response.candidates:

            if not candidate.content:
                continue

            for part in candidate.content.parts:

                if part.function_call:
                    function_calls.append(
                        part.function_call
                    )

        # --------------------------------------------------
        # NO TOOL CALL → FINAL ANSWER
        # --------------------------------------------------

        if not function_calls:

            text = response.text or ""

            return parse_final_response(text)

        # --------------------------------------------------
        # ADD MODEL RESPONSE TO CONVERSATION
        # --------------------------------------------------

        contents.append(
            response.candidates[0].content
        )

        # --------------------------------------------------
        # EXECUTE REQUESTED TOOLS
        # --------------------------------------------------

        function_response_parts = []

        for function_call in function_calls:

            tool_name = function_call.name

            arguments = dict(
                function_call.args or {}
            )

            print(
                f"[AI TOOL] {tool_name} "
                f"{arguments}"
            )

            result = execute_tool(
                tool_name,
                arguments
            )

            function_response_parts.append(
                types.Part.from_function_response(
                    name=tool_name,
                    response=result
                )
            )

        # Send tool results back to Gemini.
        contents.append(
            types.Content(
                role="user",
                parts=function_response_parts
            )
        )

    return {
        "message": "I'm sorry, I could not complete that request.",
        "action": None
    }