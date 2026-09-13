# MIND-MITRA

An Android + FastAPI app to support elderly users and caregivers with memory assistance, routines, cognitive games, and the Saathi AI agent.

## Architecture

- **Android (Jetpack Compose):** Firebase Auth, Firestore listeners, unified Retrofit client → Railway FastAPI
- **Backend (FastAPI):** Firestore data, Supabase Storage for photos, Gemini agent (`/api/agent/chat`)
- **Media:** Supabase bucket `mind-mitra-media` at `memories/{user_id}/...`

## Backend (local)

Requirements: Python 3.10+

```bash
cd backend
python -m venv .venv
source .venv/bin/activate
pip install -r requirements.txt
cp .env.example .env   # fill in values locally — never commit .env
uvicorn main:app --reload --port 8000
```

Health check: `GET /health`

## Android

1. Open in Android Studio
2. `app/build.gradle.kts` → `BuildConfig.BASE_URL` points to Railway by default
3. For local backend on emulator, set `BASE_URL` to `http://10.0.2.2:8000/`
4. Build & run

## Firebase

- Deploy `firestore.rules` to your Firebase project
- Set `FIREBASE_CREDENTIALS_JSON` on Railway (or use `serviceAccountKey.json` locally)

## Railway deployment (manual — do not auto-deploy)

1. Push your branch to GitHub (when ready).
2. In Railway, create/connect a service from the repo; set root directory to `backend/`.
3. Set environment variables (in Railway dashboard only — never commit):
   - `SUPABASE_URL`
   - `SUPABASE_SECRET_KEY`
   - `FIREBASE_CREDENTIALS_JSON` (full service account JSON string)
   - `GEMINI_API_KEY` (optional; agent returns 503 without it)
   - `MODEL_NAME` (optional, default `gemini-2.5-flash-lite`)
4. Railway uses `backend/Procfile`: `uvicorn main:app --host 0.0.0.0 --port $PORT`
5. After deploy, verify `GET https://<your-service>.up.railway.app/health`
6. Update Android `BuildConfig.BASE_URL` in `app/build.gradle.kts` if the URL changes.

## Firebase

- Deploy `firestore.rules` from the repo root via Firebase Console or CLI.

## Key flows

| Flow | Path |
|------|------|
| Memory Vault | `GET/POST /memories`, `POST /photos/upload` |
| Routines | `GET/POST /routine` |
| Games | `GET/POST /games/progress` |
| Saathi agent | `POST /api/agent/chat` (Bearer token required) |
