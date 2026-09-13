"""Saathi AI agent — OpenRouter (free models only)."""

import json
import logging
import os
from typing import Any

import requests

logger = logging.getLogger(__name__)

from tools import (
    create_reminder,
    get_family_member,
    get_game_progress,
    get_pending_tasks,
    get_today_schedule,
    open_game,
    recommend_game,
    search_memories,
    show_memory,
)

# Comma-separated free models, e.g.:
# OPENROUTER_MODELS=openrouter/free,meta-llama/llama-3.3-70b-instruct:free
# Falls back to legacy OPENROUTER_MODEL when OPENROUTER_MODELS is unset.
DEFAULT_MODELS = "openrouter/free"

LANGUAGE_HINTS = {
    "en": "Respond in simple, clear English.",
    "as": "Respond in simple Assamese (অসমীয়া) where possible.",
    "bn": "Respond in simple Bengali (বাংলা) where possible.",
    "mni": "Respond in simple Manipuri/Meitei (মৈতৈলোন্) where possible.",
    "hi": "Respond in simple Hindi or Hinglish where possible.",
}

TOOL_DECLARATIONS = [
    {
        "type": "function",
        "function": {
            "name": "get_today_schedule",
            "description": "Get today's schedule for the user.",
            "parameters": {
                "type": "object",
                "properties": {"user_id": {"type": "string"}},
                "required": ["user_id"],
            },
        },
    },
    {
        "type": "function",
        "function": {
            "name": "get_pending_tasks",
            "description": "Get pending tasks for the user.",
            "parameters": {
                "type": "object",
                "properties": {"user_id": {"type": "string"}},
                "required": ["user_id"],
            },
        },
    },
    {
        "type": "function",
        "function": {
            "name": "get_family_member",
            "description": "Find a family member by relationship.",
            "parameters": {
                "type": "object",
                "properties": {
                    "user_id": {"type": "string"},
                    "relation": {"type": "string"},
                },
                "required": ["user_id", "relation"],
            },
        },
    },
    {
        "type": "function",
        "function": {
            "name": "search_memories",
            "description": "Search the user's memories.",
            "parameters": {
                "type": "object",
                "properties": {
                    "user_id": {"type": "string"},
                    "query": {"type": "string"},
                },
                "required": ["user_id", "query"],
            },
        },
    },
    {
        "type": "function",
        "function": {
            "name": "get_game_progress",
            "description": "Get cognitive game progress.",
            "parameters": {
                "type": "object",
                "properties": {"user_id": {"type": "string"}},
                "required": ["user_id"],
            },
        },
    },
    {
        "type": "function",
        "function": {
            "name": "recommend_game",
            "description": "Recommend a cognitive game.",
            "parameters": {
                "type": "object",
                "properties": {"user_id": {"type": "string"}},
                "required": ["user_id"],
            },
        },
    },
    {
        "type": "function",
        "function": {
            "name": "open_game",
            "description": "Open a game at a level.",
            "parameters": {
                "type": "object",
                "properties": {
                    "user_id": {"type": "string"},
                    "game": {"type": "string"},
                    "level": {"type": "integer"},
                },
                "required": ["user_id", "game", "level"],
            },
        },
    },
    {
        "type": "function",
        "function": {
            "name": "show_memory",
            "description": "Display a specific memory.",
            "parameters": {
                "type": "object",
                "properties": {
                    "user_id": {"type": "string"},
                    "memory_id": {"type": "string"},
                },
                "required": ["user_id", "memory_id"],
            },
        },
    },
    {
        "type": "function",
        "function": {
            "name": "create_reminder",
            "description": "Create a reminder for the user.",
            "parameters": {
                "type": "object",
                "properties": {
                    "user_id": {"type": "string"},
                    "task": {"type": "string"},
                    "time": {"type": "string"},
                },
                "required": ["user_id", "task", "time"],
            },
        },
    },
]

TOOL_MAP = {
    "get_today_schedule": get_today_schedule,
    "get_pending_tasks": get_pending_tasks,
    "get_family_member": get_family_member,
    "search_memories": search_memories,
    "get_game_progress": get_game_progress,
    "recommend_game": recommend_game,
    "open_game": open_game,
    "show_memory": show_memory,
    "create_reminder": create_reminder,
}


def _get_openrouter_api_key() -> str | None:
    return os.getenv("OPENROUTER_API_KEY")


def is_agent_configured() -> bool:
    return bool(_get_openrouter_api_key())


def validate_free_model(model: str) -> str:
    """Accept openrouter/free or model IDs ending with :free only."""
    trimmed = (model or "").strip()
    if not trimmed:
        raise RuntimeError("INVALID_MODEL")
    if trimmed == "openrouter/free":
        return trimmed
    if trimmed.endswith(":free"):
        return trimmed
    raise RuntimeError("INVALID_MODEL")


def parse_model_list(raw: str) -> list[str]:
    return [part.strip() for part in raw.split(",") if part.strip()]


def raw_model_candidates() -> list[str]:
    """Resolve configured model IDs from env (OPENROUTER_MODELS preferred)."""
    models_raw = os.getenv("OPENROUTER_MODELS")
    if models_raw and models_raw.strip():
        return parse_model_list(models_raw)
    legacy = os.getenv("OPENROUTER_MODEL", DEFAULT_MODELS)
    if "," in legacy:
        return parse_model_list(legacy)
    trimmed = (legacy or "").strip()
    return [trimmed] if trimmed else [DEFAULT_MODELS]


def get_models_for_request() -> list[str]:
    """Return validated free-model list; raises INVALID_MODEL if any entry is invalid."""
    candidates = raw_model_candidates()
    if not candidates:
        raise RuntimeError("INVALID_MODEL")
    return [validate_free_model(model) for model in candidates]


def is_model_configured() -> bool:
    try:
        return len(get_models_for_request()) > 0
    except RuntimeError:
        return False


def _system_instruction(language: str | None) -> str:
    lang_hint = LANGUAGE_HINTS.get((language or "en").lower(), LANGUAGE_HINTS["en"])
    return f"""You are Saathi, the personal AI care assistant inside MIND-MITRA.

MIND-MITRA supports elderly users and caregivers with memory assistance,
daily routines, reminders, and cognitive games.

{lang_hint}

Rules:
1. Use tools for personalized schedule, memories, family, games, reminders.
2. Never invent memories, family, or appointments.
3. No medical diagnosis. Be warm, patient, and concise.
4. At the end, output ONLY JSON:
{{"message": "short reply", "action": null}}
or with action: {{"type": "ACTION_NAME", "payload": {{}}}}
"""


def execute_tool(tool_name: str, arguments: dict[str, Any]) -> dict:
    function = TOOL_MAP.get(tool_name)
    if function is None:
        return {"success": False, "message": f"Unknown tool: {tool_name}"}
    try:
        return function(**arguments)
    except Exception as error:
        return {"success": False, "message": str(error)}


def parse_final_response(text: str) -> dict:
    text = (text or "").strip()
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
        return {"message": text or "I'm here to help you.", "action": None}


def _is_fallback_status(status_code: int) -> bool:
    return status_code in {404, 408, 429} or status_code >= 500


def _chat_completion(messages: list[dict], model: str) -> dict:
    api_key = _get_openrouter_api_key()
    if not api_key:
        raise RuntimeError("OPENROUTER_NOT_CONFIGURED")

    try:
        response = requests.post(
            "https://openrouter.ai/api/v1/chat/completions",
            headers={
                "Authorization": f"Bearer {api_key}",
                "Content-Type": "application/json",
                "HTTP-Referer": "https://mind-mitra.app",
                "X-Title": "MIND-MITRA Saathi",
            },
            json={
                "model": model,
                "messages": messages,
                "tools": TOOL_DECLARATIONS,
                "temperature": 0.2,
            },
            timeout=60,
        )
    except requests.Timeout:
        logger.warning("OpenRouter model %s failed: timeout", model)
        raise RuntimeError("OPENROUTER_FALLBACK")
    except requests.RequestException:
        logger.warning("OpenRouter model %s failed: network error", model)
        raise RuntimeError("OPENROUTER_FALLBACK")

    status = response.status_code
    if status in {401, 403}:
        logger.error("OpenRouter authentication failed with status %s", status)
        raise RuntimeError("OPENROUTER_AUTH_ERROR")
    if _is_fallback_status(status):
        logger.warning("OpenRouter model %s failed: HTTP %s", model, status)
        raise RuntimeError("OPENROUTER_FALLBACK")
    if status >= 400:
        logger.warning("OpenRouter model %s failed: HTTP %s", model, status)
        raise RuntimeError(f"OPENROUTER_ERROR_{status}")

    logger.info("OpenRouter model %s succeeded", model)
    return response.json()


def _chat_completion_with_fallback(messages: list[dict]) -> dict:
    models = get_models_for_request()
    last_error = "ALL_MODELS_FAILED"

    for model in models:
        logger.info("OpenRouter request trying model: %s", model)
        try:
            return _chat_completion(messages, model)
        except RuntimeError as exc:
            code = str(exc)
            if code == "OPENROUTER_AUTH_ERROR":
                raise
            if code == "OPENROUTER_NOT_CONFIGURED":
                raise
            last_error = code
            continue

    logger.warning("OpenRouter all configured models failed")
    raise RuntimeError("ALL_MODELS_FAILED")


def run_agent(user_id: str, message: str, language: str | None = None) -> dict:
    if not _get_openrouter_api_key():
        raise RuntimeError("OPENROUTER_NOT_CONFIGURED")
    get_models_for_request()

    messages = [
        {"role": "system", "content": _system_instruction(language)},
        {
            "role": "user",
            "content": f"User ID: {user_id}\n\nUser request:\n{message}",
        },
    ]

    for _ in range(5):
        data = _chat_completion_with_fallback(messages)
        choice = (data.get("choices") or [{}])[0]
        assistant_message = choice.get("message") or {}
        tool_calls = assistant_message.get("tool_calls") or []

        if not tool_calls:
            content = assistant_message.get("content") or ""
            return parse_final_response(content)

        messages.append(assistant_message)

        for tool_call in tool_calls:
            fn = tool_call.get("function") or {}
            tool_name = fn.get("name", "")
            try:
                arguments = json.loads(fn.get("arguments") or "{}")
            except json.JSONDecodeError:
                arguments = {}
            if "user_id" not in arguments:
                arguments["user_id"] = user_id
            result = execute_tool(tool_name, arguments)
            messages.append(
                {
                    "role": "tool",
                    "tool_call_id": tool_call.get("id", tool_name),
                    "content": json.dumps(result),
                }
            )

    return {
        "message": "I'm sorry, I could not complete that request.",
        "action": None,
    }
