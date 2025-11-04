# CodiBase Stack Architecture

## Overview

CodiBase is a fully integrated development platform that combines:
- **CodiBase**: Git hosting, CI/CD, project management
- **Vibecoda**: AI-powered code editor
- **PilotCodi**: Hybrid AI completion (in-house + Claude)

## Architecture Diagram

```
┌─────────────────────────────────────────────────────────────┐
│                     CodiBase Platform                        │
├─────────────────────────────────────────────────────────────┤
│                                                               │
│  ┌──────────────┐      ┌──────────────┐      ┌───────────┐ │
│  │   Vibecoda   │◄────►│  API Gateway │◄────►│ CodiBase  │ │
│  │  (Desktop)   │      │   (Kotlin)   │      │   (Java)  │ │
│  └──────────────┘      └──────────────┘      └───────────┘ │
│         │                      │                     │       │
│         │                      │                     │       │
│         ▼                      ▼                     ▼       │
│  ┌──────────────────────────────────────────────────────┐  │
│  │              PilotCodi (Hybrid AI)                   │  │
│  ├──────────────────────────────────────────────────────┤  │
│  │  Tabby (Fast)  │  Claude API (Complex)  │  Router   │  │
│  └──────────────────────────────────────────────────────┘  │
│                           │                                  │
│                           ▼                                  │
│                  ┌─────────────────┐                        │
│                  │  Telemetry API  │                        │
│                  └─────────────────┘                        │
└─────────────────────────────────────────────────────────────┘
```

## Key Features

### 1. AI-Powered Code Review
- Automatic PR review with actionable feedback
- Security vulnerability detection
- Performance optimization suggestions
- Best practice enforcement

### 2. Smart Merge Conflict Resolution
- AI analyzes both sides of conflict
- Proposes intelligent merge solutions
- Explains reasoning behind resolution

### 3. Intelligent CI/CD Pipeline Generation
- Analyzes project structure
- Generates optimized pipelines
- Includes security scanning, testing, deployment

### 4. Semantic Code Search
- Natural language queries
- Finds code by meaning, not just keywords
- Cross-repository search

### 5. Real-time Collaboration
- Google Docs-style editing
- Live cursors and selections
- Integrated chat

### 6. AI Pair Programming
- Context-aware suggestions
- Explains code on hover
- Generates tests automatically

## API Endpoints

### Unified API (`/api/v1`)

#### Health Check
```
GET /api/v1/health
```

#### AI Completion
```
POST /api/v1/ai/complete
{
  "complexity": "simple|complex",
  "prompt": "...",
  "context": {...}
}
```

#### Track Telemetry
```
POST /api/v1/telemetry/track
{
  "event": "...",
  "properties": {...}
}
```

## Integration Flow

### Vibecoda → CodiBase
1. User edits code in Vibecoda
2. Extension sends changes to CodiBase via WebSocket
3. CodiBase persists changes and broadcasts to collaborators
4. AI suggestions triggered based on context

### PilotCodi Routing
1. Request comes to API Gateway
2. Gateway analyzes complexity
3. Simple: Route to Tabby (fast, in-house)
4. Complex: Route to Claude API (accurate, powerful)
5. Response cached in Tabby for future use

### Telemetry Collection
1. All user actions tracked
2. Sent to telemetry service
3. Aggregated for analytics
4. Used to improve AI models

## Deployment

### Development
```bash
./deploy.sh
```

### Production
```bash
docker-compose -f docker-compose.prod.yml up -d
```

## Configuration

### Environment Variables
- `CODIBASE_URL`: CodiBase server URL
- `PILOTCODI_URL`: PilotCodi service URL
- `TELEMETRY_URL`: Telemetry service URL
- `CLAUDE_API_KEY`: Anthropic API key for complex requests
- `DATABASE_URL`: PostgreSQL connection string
- `JWT_SECRET`: Secret for authentication tokens

### Feature Flags
- `ENABLE_AI_REVIEW`: Enable/disable AI code reviews
- `ENABLE_SMART_MERGE`: Enable/disable smart merge
- `ENABLE_REALTIME_COLLAB`: Enable/disable real-time collaboration
- `USE_CLAUDE_API`: Use Claude for complex requests vs only Tabby

## Competitive Advantages

### vs GitHub
✅ Built-in AI at every layer (not bolted on)
✅ Free unlimited private repos with AI features
✅ Smart merge conflict resolution
✅ Real-time collaborative editing
✅ AI-generated CI/CD pipelines
✅ Semantic code search

### vs GitLab  
✅ Better AI integration (GitHub Copilot-level but integrated)
✅ Simpler setup (no complex runners)
✅ Real-time collaboration
✅ Better developer experience

### vs Cursor
✅ Full platform (not just editor)
✅ Own your data and infrastructure
✅ Git hosting included
✅ Team collaboration built-in
✅ Lower cost at scale

## Roadmap

### Phase 1 (Current) - Beta Launch
- ✅ Core platform rebranding
- ✅ AI code completion
- ✅ Basic telemetry
- ⏳ GitHub/GitLab competitive features
- ⏳ Vibecoda desktop app

### Phase 2 - Public Release
- AI code review
- Smart merge
- Real-time collaboration
- Semantic code search
- Mobile apps

### Phase 3 - Enterprise
- Self-hosted option
- Advanced security features
- Compliance certifications
- Enterprise SSO
- Audit logs

### Phase 4 - Ecosystem
- Plugin marketplace
- Third-party integrations
- Public API
- Webhooks
- CLI tools
