# MIND-MITRA

An Android + FastAPI app to support elderly users and caregivers with memory assistance, routines, cognitive games, and the Saathi AI agent.

## Architecture

- **Android (Jetpack Compose):** Firebase Auth, Firestore listeners, unified Retrofit client → Railway FastAPI
- **Backend (FastAPI):** Firestore data, Supabase Storage for photos, Saathi AI via OpenRouter (`/api/agent/chat`)
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

## Saathi AI (OpenRouter)

The Saathi conversational agent runs on the backend only. The Android app calls `POST /api/agent/chat` with a Firebase Bearer token; the API key never ships in the app.

Set these in `backend/.env` locally or in Railway (never commit real keys):

| Variable | Description |
|----------|-------------|
| `OPENROUTER_API_KEY` | Your OpenRouter API key (backend-only) |
| `OPENROUTER_MODELS` | Comma-separated list of **free** models to try, in order |

Example (no real key):

```env
OPENROUTER_API_KEY=
OPENROUTER_MODELS=openrouter/free
```

**Free models only.** Each entry must be either:

- `openrouter/free`, or
- a model ID ending with `:free` (e.g. `meta-llama/llama-3.3-70b-instruct:free`)

Paid models are rejected at startup/request validation.

**Automatic fallback:** For each chat request, the backend tries the first configured model once. If OpenRouter returns a temporary failure (rate limit, timeout, unavailable model, or 5xx), it tries the next model in the list. If every model fails, the API returns a friendly 503. Authentication or configuration errors are reported as configuration failures, not retried across models.

Legacy `OPENROUTER_MODEL` is still supported when `OPENROUTER_MODELS` is unset; prefer `OPENROUTER_MODELS` for multiple fallbacks.

Without a valid key and model configuration, the agent endpoint returns 503 and the rest of the API continues to work.

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
   - `OPENROUTER_API_KEY` (optional; agent returns 503 without it)
   - `OPENROUTER_MODELS` (optional; default `openrouter/free` — free models only, comma-separated for fallback)
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
| Caregiver link | `POST /users/link-patient` (email + 6-digit PIN) |
| Reminders | `GET/POST /reminders` |
