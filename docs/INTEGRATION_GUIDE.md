# Integration Guide

## Setting Up CodiBase Stack

### Prerequisites
- Docker & Docker Compose
- Java 17+
- Node.js 18+
- Rust 1.70+
- Kotlin 1.9+

### Quick Start

1. **Clone the repository**
```bash
git clone https://github.com/yourusername/codibase-stack
cd codibase-stack
```

2. **Build all components**
```bash
./build-all.sh
```

3. **Start services**
```bash
./deploy.sh
```

4. **Access services**
- CodiBase: http://localhost:6610
- PilotCodi API: http://localhost:8081
- Telemetry Dashboard: http://localhost:8080/stats
- API Gateway: http://localhost:8000

## Integrating Vibecoda with CodiBase

### Install Extension
1. Open Vibecoda
2. Go to Extensions
3. Search "CodiBase Integration"
4. Install and reload

### Configure
```json
{
  "codibase.serverUrl": "http://localhost:6610",
  "codibase.apiToken": "your-api-token"
}
```

### Usage

#### Clone Repository
- Cmd+Shift+P → "CodiBase: Clone Repository"
- Enter repo URL
- Repository cloned with full history

#### Create Pull Request
- Cmd+Shift+P → "CodiBase: Create Pull Request"
- Fill in details
- PR created with AI review queued

#### AI Code Review
- Select code
- Cmd+Shift+P → "CodiBase: AI Code Review"
- Review appears in side panel

#### Smart Merge
- Open file with conflicts
- Cmd+Shift+P → "CodiBase: Smart Merge"
- AI resolves conflicts automatically

## API Integration

### Authentication
```bash
curl -X POST http://localhost:6610/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"your-user","password":"your-pass"}'
```

Response:
```json
{
  "token": "eyJhbGc...",
  "expiresIn": 3600
}
```

### Using AI Completion
```javascript
const response = await fetch('http://localhost:8000/api/v1/ai/complete', {
  method: 'POST',
  headers: {
    'Authorization': 'Bearer YOUR_TOKEN',
    'Content-Type': 'application/json'
  },
  body: JSON.stringify({
    complexity: 'simple',
    prompt: 'function to reverse a string',
    language: 'javascript'
  })
});

const completion = await response.json();
console.log(completion.code);
```

### Creating Repository
```bash
curl -X POST http://localhost:6610/api/v1/projects \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "my-awesome-project",
    "description": "Building something cool",
    "visibility": "private"
  }'
```

### Triggering AI Review
```bash
curl -X POST http://localhost:6610/api/v1/pullrequests/123/review \
  -H "Authorization: Bearer YOUR_TOKEN"
```

## Custom Integration Examples

### Slack Bot Integration
```python
from slack_bolt import App
import requests

app = App(token="xoxb-your-token")

@app.command("/codibase-review")
def trigger_review(ack, command, say):
    ack()
    
    pr_url = command['text']
    response = requests.post(
        f"http://localhost:6610/api/v1/ai/review",
        headers={"Authorization": f"Bearer {TOKEN}"},
        json={"url": pr_url}
    )
    
    review = response.json()
    say(f"AI Review: {review['summary']}")

if __name__ == "__main__":
    app.start(port=3000)
```

### GitHub Actions Integration
```yaml
name: CodiBase AI Review

on:
  pull_request:
    types: [opened, synchronize]

jobs:
  ai-review:
    runs-on: ubuntu-latest
    steps:
      - name: Trigger AI Review
        run: |
          curl -X POST https://codibase.dev/api/v1/ai/review \
            -H "Authorization: Bearer ${{ secrets.CODIBASE_TOKEN }}" \
            -H "Content-Type: application/json" \
            -d '{"pr_number": "${{ github.event.pull_request.number }}"}'
```

### VS Code Extension
```typescript
import * as vscode from 'vscode';

export function activate(context: vscode.ExtensionContext) {
    const disposable = vscode.commands.registerCommand(
        'codibase.aiExplain',
        async () => {
            const editor = vscode.window.activeTextEditor;
            if (!editor) return;
            
            const selection = editor.document.getText(editor.selection);
            
            const response = await fetch('http://localhost:8000/api/v1/ai/complete', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({
                    complexity: 'complex',
                    prompt: `Explain this code: ${selection}`
                })
            });
            
            const result = await response.json();
            vscode.window.showInformationMessage(result.explanation);
        }
    );
    
    context.subscriptions.push(disposable);
}
```

## Telemetry Integration

### Track Custom Events
```javascript
fetch('http://localhost:8080/collect', {
  method: 'POST',
  headers: { 'Content-Type': 'application/json' },
  body: JSON.stringify({
    timestamp: new Date().toISOString(),
    source: 'my-service',
    event: 'custom_action',
    sessionId: 'session-123',
    properties: {
      userId: 'user-456',
      action: 'button_clicked',
      metadata: { button: 'deploy' }
    }
  })
});
```

### View Analytics
```bash
curl http://localhost:8080/stats
```

## Troubleshooting

### Services Not Starting
```bash
# Check service logs
docker-compose logs -f codibase
docker-compose logs -f pilotcodi
docker-compose logs -f telemetry

# Restart specific service
docker-compose restart codibase
```

### AI Completions Slow
- Check if using Claude API (expected latency)
- Switch to Tabby-only mode for faster responses
- Increase PilotCodi replicas

### Merge Conflicts Not Resolving
- Ensure conflicts are properly formatted
- Check AI service connectivity
- Verify API token permissions

## Production Deployment

### Docker Swarm
```bash
docker stack deploy -c docker-compose.prod.yml codibase
```

### Kubernetes
```bash
kubectl apply -f k8s/
```

### Environment-specific Config
```bash
# Production
export CODIBASE_ENV=production
export DATABASE_URL=postgresql://prod-db:5432/codibase
export CLAUDE_API_KEY=sk-ant-...

# Staging
export CODIBASE_ENV=staging
export DATABASE_URL=postgresql://staging-db:5432/codibase
```

## Contributing

See [CONTRIBUTING.md](CONTRIBUTING.md) for guidelines.

## License

MIT License - see [LICENSE](LICENSE) for details.
