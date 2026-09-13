from fastapi import APIRouter, Depends, HTTPException
from models.schemas import User, LinkPatientRequest
from services.firebase import db
from services.auth import get_current_uid, assert_can_write
from firebase_admin import auth as firebase_auth

router = APIRouter(
    prefix="/users",
    tags=["Users"]
)


# GET all users (authenticated)
@router.get("/")
def get_users(caller_uid: str = Depends(get_current_uid)):
    users_ref = db.collection("users").stream()

    users = []

    for user in users_ref:
        user_data = user.to_dict()
        users.append(user_data)

    return {
        "users": users
    }


# GET one user
@router.get("/{user_id}")
def get_user(user_id: str, caller_uid: str = Depends(get_current_uid)):
    assert_can_write(caller_uid, user_id)
    user_ref = db.collection("users").document(user_id)
    user = user_ref.get()

    if not user.exists:
        return {
            "message": "User not found"
        }

    return user.to_dict()


# CREATE a user
@router.post("/")
def create_user(user: User, caller_uid: str = Depends(get_current_uid)):
    assert_can_write(caller_uid, user.user_id)
    user_ref = db.collection("users").document(user.user_id)

    # CHANGED: was a plain .set(), which overwrites the whole document.
    # The Android app stores connectionPin, linkedPatientId/linkedCaregiverId,
    # email, and role on this same users/{uid} doc. If this endpoint is ever
    # called after the app has already written a profile, a non-merge .set()
    # silently wipes the caregiver<->patient link and connectionPin. merge=True
    # only touches the fields listed here.
    user_ref.set({
        "user_id": user.user_id,
        "name": user.name,
        "age": user.age
    }, merge=True)

    return {
        "message": "User created successfully!",
        "user": user.model_dump()
    }


@router.post("/link-patient")
def link_patient(body: LinkPatientRequest, caller_uid: str = Depends(get_current_uid)):
    """
    Link an authenticated caregiver to a patient using email + connection PIN.
    Uses Firebase Admin SDK so caregivers do not need pre-link Firestore read access.
    """
    raw_email = body.patient_email.strip()
    email = raw_email.lower()
    pin = body.connection_pin.strip()

    if not email:
        raise HTTPException(status_code=400, detail="Please enter the patient's email.")
    if len(pin) != 6 or not pin.isdigit():
        raise HTTPException(
            status_code=400,
            detail="Invalid connection PIN. Please check the 6-digit PIN.",
        )

    caregiver_ref = db.collection("users").document(caller_uid)
    caregiver_doc = caregiver_ref.get()
    caregiver_data = caregiver_doc.to_dict() if caregiver_doc.exists else {}

    existing_patient = caregiver_data.get("linkedPatientId")
    if existing_patient:
        raise HTTPException(
            status_code=409,
            detail="You are already connected to a patient.",
        )

    patient_id = None
    patient_data = None

    email_variants = {email, raw_email}
    for email_variant in email_variants:
        for doc in db.collection("users").where("email", "==", email_variant).stream():
            data = doc.to_dict() or {}
            stored_pin = str(data.get("connectionPin", "")).strip()
            role = str(data.get("role", "User") or "User")
            if stored_pin == pin and role in ("User", ""):
                patient_id = doc.id
                patient_data = data
                break
        if patient_id is not None:
            break

    if patient_id is None:
        email_exists = any(
            True
            for variant in email_variants
            for _ in db.collection("users").where("email", "==", variant).limit(1).stream()
        )
        if email_exists:
            raise HTTPException(
                status_code=401,
                detail="Invalid connection PIN. Please check the 6-digit PIN.",
            )
        raise HTTPException(
            status_code=404,
            detail="Patient not found. Please check the email.",
        )

    if patient_data.get("linkedCaregiverId"):
        raise HTTPException(
            status_code=409,
            detail="This patient is already linked to another caregiver.",
        )

    caregiver_email = caregiver_data.get("email")
    if not caregiver_email:
        try:
            caregiver_email = firebase_auth.get_user(caller_uid).email or ""
        except Exception:
            caregiver_email = ""

    batch = db.batch()
    batch.set(
        caregiver_ref,
        {
            "uid": caller_uid,
            "linkedPatientId": patient_id,
            "role": "Caregiver",
            "email": caregiver_email,
        },
        merge=True,
    )
    batch.set(
        db.collection("users").document(patient_id),
        {"linkedCaregiverId": caller_uid},
        merge=True,
    )
    batch.commit()

    return {
        "message": "Patient connected successfully.",
        "patient_id": patient_id,
        "patient_name": patient_data.get("name", ""),
    }