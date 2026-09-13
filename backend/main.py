from dotenv import load_dotenv

load_dotenv()

from fastapi import Depends, FastAPI, HTTPException
from fastapi.middleware.cors import CORSMiddleware

from models.schemas import AgentChatRequest
from routes.users import router as users_router
from routes.memories import router as memories_router
from routes.routine import router as routine_router
from routes.games import router as games_router
from routes.photos import router as photos_router
from routes.reminders import router as reminders_router

from services.firebase import db
from services.supabase_storage import (
    supabase,
    SUPABASE_URL,
    SUPABASE_SECRET_KEY,
)
from services.auth import get_current_uid

app = FastAPI(title="MIND-MITRA API")

app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

app.include_router(users_router)
app.include_router(memories_router)
app.include_router(routine_router)
app.include_router(games_router)
app.include_router(photos_router)
app.include_router(reminders_router)


@app.get("/")
def home():
    return {
        "message": "MIND-MITRA Backend is running!"
    }


@app.get("/health")
def health():
    return {
        "status": "healthy"
    }


@app.post("/api/agent/chat")
def agent_chat(body: AgentChatRequest, caller_uid: str = Depends(get_current_uid)):
    from agent import is_agent_configured, is_model_configured, run_agent

    if not is_agent_configured():
        raise HTTPException(
            status_code=503,
            detail="AI assistant is temporarily unavailable. Please try again later.",
        )

    if not is_model_configured():
        raise HTTPException(
            status_code=503,
            detail="AI assistant is not properly configured. Please try again later.",
        )

    try:
        return run_agent(
            user_id=caller_uid,
            message=body.message,
            language=body.language,
        )
    except RuntimeError as exc:
        if str(exc) == "INVALID_MODEL":
            raise HTTPException(
                status_code=503,
                detail="AI assistant is not properly configured. Please try again later.",
            )
        if str(exc) == "OPENROUTER_AUTH_ERROR":
            raise HTTPException(
                status_code=503,
                detail="AI assistant is not properly configured. Please try again later.",
            )
        if str(exc) in {
            "OPENROUTER_NOT_CONFIGURED",
            "RATE_LIMITED",
            "ALL_MODELS_FAILED",
            "OPENROUTER_FALLBACK",
        } or str(exc).startswith("OPENROUTER_ERROR"):
            raise HTTPException(
                status_code=503,
                detail="AI assistant is temporarily unavailable. Please try again later.",
            )
        raise HTTPException(
            status_code=503,
            detail="AI assistant is temporarily unavailable. Please try again later.",
        )
    except Exception:
        raise HTTPException(
            status_code=500,
            detail="Sorry, the AI assistant could not respond right now.",
        )


@app.get("/firebase-test")
def firebase_test():
    test_ref = db.collection("test").document("connection_test")

    test_ref.set({
        "message": "Firebase connection is working!"
    })

    data = test_ref.get()

    return {
        "firebase_connected": data.exists,
        "data": data.to_dict()
    }


@app.get("/supabase-test")
def supabase_test():
    try:
        import requests

        url = f"{SUPABASE_URL}/storage/v1/bucket/mind-mitra-media"

        headers = {
            "apikey": SUPABASE_SECRET_KEY,
            "Authorization": f"Bearer {SUPABASE_SECRET_KEY}"
        }

        response = requests.get(url, headers=headers)

        return {
            "status_code": response.status_code,
            "response": response.text[:500]
        }

    except Exception as e:
        return {
            "error": str(e)
        }
