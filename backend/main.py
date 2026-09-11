from fastapi import FastAPI
from routes.users import router as users_router
from routes.memories import router as memories_router
from routes.routine import router as routine_router
from routes.games import router as games_router
from services.firebase import db

app = FastAPI(title="MIND-MITRA API")

app.include_router(users_router)
app.include_router(memories_router)
app.include_router(routine_router)
app.include_router(games_router)

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