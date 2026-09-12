# backend/main.py

from fastapi import FastAPI
from fastapi.middleware.cors import CORSMiddleware
from pydantic import BaseModel

from agent import run_agent


app = FastAPI(
    title="MIND-MITRA AI Agent",
    description="Saathi - Personal AI Care Agent",
    version="1.0.0"
)


# --------------------------------------------------
# CORS
# --------------------------------------------------

app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)


# --------------------------------------------------
# REQUEST MODEL
# --------------------------------------------------

class AgentRequest(BaseModel):

    user_id: str

    message: str


# --------------------------------------------------
# ROOT
# --------------------------------------------------

@app.get("/")
def root():

    return {
        "status": "running",
        "project": "MIND-MITRA",
        "agent": "Saathi"
    }


# --------------------------------------------------
# HEALTH CHECK
# --------------------------------------------------

@app.get("/health")
def health():

    return {
        "status": "healthy"
    }


# --------------------------------------------------
# AI AGENT
# --------------------------------------------------

@app.post("/api/agent/chat")
def chat(request: AgentRequest):

    result = run_agent(
        user_id=request.user_id,
        message=request.message
    )

    return result