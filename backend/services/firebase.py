from pathlib import Path

import firebase_admin
from firebase_admin import credentials, firestore

# Find the backend folder
BASE_DIR = Path(__file__).resolve().parent.parent

# Load Firebase service account
cred = credentials.Certificate(BASE_DIR / "serviceAccountKey.json")

# Initialize Firebase
firebase_admin.initialize_app(cred)

# Connect to Firestore
db = firestore.client()