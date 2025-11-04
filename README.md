# CodiBase Stack

Complete vibe coding platform with Git hosting, AI-powered editor, and code completion.

## Components

- **CodiBase** (port 6610): Git hosting, CI/CD, project management
- **Vibecoda** (desktop app): Code editor with AI integration
- **PilotCodi** (port 8081): AI code completion service
- **Telemetry Service** (port 8080): Usage analytics and monitoring

## Quick Start

### Build All
```bash
./build-all.sh
```

### Deploy Locally
```bash
./deploy.sh
```

### Development

Each component can be developed independently:

```bash
# Vibecoda
cd vibecoda
npm install
npm run watch

# CodiBase
cd codibase
./gradlew bootRun

# PilotCodi
cd pilotcodi
cargo run

# Telemetry
cd telemetry-service
pip install -r requirements.txt
python api.py
```

## Architecture

```
┌─────────────┐
│  Vibecoda   │ ◄──┐
│  (Desktop)  │    │
└─────────────┘    │
       │           │ AI Completions
       │           │
       ▼           │
┌─────────────┐   │
│  CodiBase   │   │
│  (Web)      │   │
└─────────────┘   │
       │          │
       │          │
       ▼          │
┌─────────────┐  │
│  PilotCodi  │──┘
│  (AI)       │
└─────────────┘
       │
       ▼
┌─────────────┐
│ Telemetry   │
│  Service    │
└─────────────┘
```

## Telemetry

View stats: `curl http://localhost:8080/stats`

## License

MIT

---
Built with vibe 🚀
