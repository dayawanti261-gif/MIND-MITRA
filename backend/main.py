# backend/main.py

from dotenv import load_dotenv

# Load environment variables FIRST
load_dotenv()

from fastapi import FastAPI
from fastapi.middleware.cors import CORSMiddleware
from pydantic import BaseModel

from agent import run_agent

# Routers
from routes.users import router as users_router
from routes.memories import router as memories_router
from routes.routine import router as routine_router
from routes.games import router as games_router
from routes.photos import router as photos_router

# Services
from services.firebase import db
from services.supabase_storage import (
    supabase,
    SUPABASE_URL,
    SUPABASE_SECRET_KEY
)


# ==================================================
# FASTAPI APP
# ==================================================

app = FastAPI(
    title="MIND-MITRA AI Agent",
    description="Saathi - Personal AI Care Agent",
    version="1.0.0"
)


# ==================================================
# CORS
# ==================================================

app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)


# ==================================================
# REQUEST MODEL
# ==================================================

class AgentRequest(BaseModel):
    user_id: str
    message: str


# ==================================================
# ROUTERS
# ==================================================

app.include_router(users_router)
app.include_router(memories_router)
app.include_router(routine_router)
app.include_router(games_router)
app.include_router(photos_router)


# ==================================================
# ROOT
# ==================================================

@app.get("/")
def root():
    return {
        "status": "running",
        "project": "MIND-MITRA",
        "agent": "Saathi"
    }


# ==================================================
# HEALTH CHECK
# ==================================================

@app.get("/health")
def health():
    return {
        "status": "healthy"
    }


# ==================================================
# AI AGENT
# ==================================================

@app.post("/api/agent/chat")
def chat(request: AgentRequest):

    result = run_agent(
        user_id=request.user_id,
        message=request.message
    )

    return result


# ==================================================
# FIREBASE TEST
# ==================================================

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


# ==================================================
# SUPABASE TEST
# ==================================================

@app.get("/supabase-test")
def supabase_test():

    try:

        import requests

        url = f"{SUPABASE_URL}/storage/v1/bucket/mind-mitra-media"

        headers = {
            "apikey": SUPABASE_SECRET_KEY,
            "Authorization": f"Bearer {SUPABASE_SECRET_KEY}"
        }

        response = requests.get(
            url,
            headers=headers
        )

        return {
            "status_code": response.status_code,
            "response": response.text[:500]
        }

    except Exception as e:

        return {
            "error": str(e)
        }