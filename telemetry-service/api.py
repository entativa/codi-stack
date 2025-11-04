#!/usr/bin/env python3
"""
CodiBase Telemetry Collection Service
Collects telemetry from Vibecoda, PilotCodi, and CodiBase
"""

from fastapi import FastAPI, Request
from fastapi.middleware.cors import CORSMiddleware
from datetime import datetime
import json
import sqlite3
from pathlib import Path

app = FastAPI(title="CodiBase Telemetry API")

# CORS for local development
app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

# Database setup
DB_PATH = Path(__file__).parent / "telemetry.db"

def init_db():
    conn = sqlite3.connect(DB_PATH)
    c = conn.cursor()
    c.execute('''
        CREATE TABLE IF NOT EXISTS events (
            id INTEGER PRIMARY KEY AUTOINCREMENT,
            timestamp TEXT NOT NULL,
            source TEXT NOT NULL,
            event_name TEXT NOT NULL,
            session_id TEXT,
            properties TEXT,
            created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
        )
    ''')
    conn.commit()
    conn.close()

init_db()

@app.post("/collect")
async def collect_event(request: Request):
    """Collect telemetry event"""
    try:
        data = await request.json()
        
        conn = sqlite3.connect(DB_PATH)
        c = conn.cursor()
        c.execute(
            "INSERT INTO events (timestamp, source, event_name, session_id, properties) VALUES (?, ?, ?, ?, ?)",
            (
                data.get('timestamp', datetime.utcnow().isoformat()),
                data.get('source', 'vibecoda'),
                data.get('event', 'unknown'),
                data.get('sessionId'),
                json.dumps(data.get('properties', {}))
            )
        )
        conn.commit()
        conn.close()
        
        return {"status": "success"}
    except Exception as e:
        return {"status": "error", "message": str(e)}

@app.get("/stats")
async def get_stats():
    """Get telemetry statistics"""
    conn = sqlite3.connect(DB_PATH)
    c = conn.cursor()
    
    # Total events
    c.execute("SELECT COUNT(*) FROM events")
    total_events = c.fetchone()[0]
    
    # Events by source
    c.execute("SELECT source, COUNT(*) FROM events GROUP BY source")
    by_source = dict(c.fetchall())
    
    # Top events
    c.execute("SELECT event_name, COUNT(*) FROM events GROUP BY event_name ORDER BY COUNT(*) DESC LIMIT 10")
    top_events = dict(c.fetchall())
    
    conn.close()
    
    return {
        "total_events": total_events,
        "by_source": by_source,
        "top_events": top_events
    }

@app.get("/health")
async def health():
    return {"status": "healthy"}

if __name__ == "__main__":
    import uvicorn
    uvicorn.run(app, host="0.0.0.0", port=8080)
