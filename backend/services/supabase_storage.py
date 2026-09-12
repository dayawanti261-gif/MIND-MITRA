import os
from supabase import create_client, Client

SUPABASE_URL = os.getenv("SUPABASE_URL")
SUPABASE_SECRET_KEY = os.getenv("SUPABASE_SECRET_KEY")

print("SUPABASE URL loaded:", bool(SUPABASE_URL))
print("SUPABASE SECRET KEY loaded:", bool(SUPABASE_SECRET_KEY))

supabase: Client = create_client(
    SUPABASE_URL,
    SUPABASE_SECRET_KEY
)

BUCKET_NAME = "mind-mitra-media"