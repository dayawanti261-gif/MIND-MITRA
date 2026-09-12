from dotenv import load_dotenv

load_dotenv()

from fastapi import FastAPI

from routes.users import router as users_router
from routes.memories import router as memories_router
from routes.routine import router as routine_router
from routes.games import router as games_router
from routes.photos import router as photos_router

from services.firebase import db
from services.supabase_storage import (
supabase,
SUPABASE_URL,
SUPABASE_SECRET_KEY
)
app = FastAPI(title="MIND-MITRA API")

app.include_router(users_router)
app.include_router(memories_router)
app.include_router(routine_router)
app.include_router(games_router)
app.include_router(photos_router)

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