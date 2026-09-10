# backend/data.py

USER = {
    "user_id": "demo_user_001",
    "name": "Didi",
    "preferred_language": "Hindi",

    "family": [
        {
            "id": "f1",
            "name": "Priya",
            "relation": "daughter"
        },
        {
            "id": "f2",
            "name": "Raj",
            "relation": "son"
        },
        {
            "id": "f3",
            "name": "Aarav",
            "relation": "grandson"
        }
    ],

    "preferences": {
        "favorite_activity": "music",
        "favorite_game": "memory",
        "favorite_music": "old Hindi songs"
    },

    "memories": [
        {
            "id": "m1",
            "title": "Guwahati Family Trip",
            "description": "A family trip to Guwahati.",
            "people": ["Priya", "Raj", "Aarav"],
            "image_url": "https://example.com/guwahati.jpg"
        },
        {
            "id": "m2",
            "title": "Priya's Wedding",
            "description": "Beautiful memories from Priya's wedding.",
            "people": ["Priya"],
            "image_url": "https://example.com/wedding.jpg"
        },
        {
            "id": "m3",
            "title": "Family Festival",
            "description": "A family festival celebration.",
            "people": ["Priya", "Raj", "Aarav"],
            "image_url": "https://example.com/festival.jpg"
        }
    ],

    "schedule": [
        {
            "id": "s1",
            "activity": "Breakfast",
            "time": "08:30"
        },
        {
            "id": "s2",
            "activity": "Memory Game",
            "time": "10:00"
        },
        {
            "id": "s3",
            "activity": "Lunch",
            "time": "13:00"
        },
        {
            "id": "s4",
            "activity": "Doctor Appointment",
            "time": "16:00"
        },
        {
            "id": "s5",
            "activity": "Dinner",
            "time": "19:30"
        }
    ],

    "game_progress": [
        {
            "game": "memory",
            "level": 3,
            "accuracy": 82,
            "response_time": 8,
            "mistakes": 2
        },
        {
            "game": "pattern",
            "level": 2,
            "accuracy": 65,
            "response_time": 12,
            "mistakes": 5
        },
        {
            "game": "recall",
            "level": 2,
            "accuracy": 74,
            "response_time": 10,
            "mistakes": 3
        }
    ]
}