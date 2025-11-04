#!/usr/bin/env python3
"""
CodiBase Stack Rebrand & Setup Script
Rebrands VS Code → Vibecoda, OneDev → CodiBase, Tabby → PilotCodi
Sets up telemetry pipeline and build/deploy orchestration
"""

import subprocess
import shutil
import os
import sys
import json
import re
from pathlib import Path
from typing import Dict, List, Tuple

class Colors:
    GREEN = '\033[92m'
    YELLOW = '\033[93m'
    RED = '\033[91m'
    BLUE = '\033[94m'
    MAGENTA = '\033[95m'
    CYAN = '\033[96m'
    RESET = '\033[0m'
    BOLD = '\033[1m'

def log(message, color=Colors.BLUE):
    print(f"{color}{message}{Colors.RESET}")

def log_success(message):
    print(f"{Colors.GREEN}✓ {message}{Colors.RESET}")

def log_error(message):
    print(f"{Colors.RED}✗ {message}{Colors.RESET}")

def log_warning(message):
    print(f"{Colors.YELLOW}⚠ {message}{Colors.RESET}")

def log_section(title):
    print(f"\n{Colors.BOLD}{Colors.CYAN}{'='*70}{Colors.RESET}")
    print(f"{Colors.BOLD}{Colors.CYAN}{title.center(70)}{Colors.RESET}")
    print(f"{Colors.BOLD}{Colors.CYAN}{'='*70}{Colors.RESET}\n")

def run_command(cmd, cwd=None):
    """Run shell command"""
    try:
        subprocess.run(cmd, shell=True, cwd=cwd, check=True)
        return True
    except subprocess.CalledProcessError:
        return False

def replace_in_file(file_path: Path, replacements: List[Tuple[str, str]]):
    """Replace text in a file"""
    try:
        content = file_path.read_text(encoding='utf-8', errors='ignore')
        modified = False
        
        for old, new in replacements:
            if old in content:
                content = content.replace(old, new)
                modified = True
        
        if modified:
            file_path.write_text(content, encoding='utf-8')
            return True
        return False
    except Exception as e:
        log_error(f"Error processing {file_path}: {e}")
        return False

def find_and_replace_recursive(directory: Path, replacements: List[Tuple[str, str]], 
                               extensions: List[str] = None, exclude_dirs: List[str] = None):
    """Recursively find and replace in files"""
    if exclude_dirs is None:
        exclude_dirs = ['node_modules', '.git', 'dist', 'out', 'build', '__pycache__']
    
    count = 0
    for file_path in directory.rglob('*'):
        # Skip excluded directories
        if any(excluded in file_path.parts for excluded in exclude_dirs):
            continue
        
        # Skip if not a file
        if not file_path.is_file():
            continue
        
        # Check extension filter
        if extensions and file_path.suffix not in extensions:
            continue
        
        if replace_in_file(file_path, replacements):
            count += 1
    
    return count

def move_to_root(workspace_dir: Path, target_name: str):
    """Move codebase from workspace to root"""
    source = workspace_dir / target_name
    dest = workspace_dir.parent / target_name
    
    if not source.exists():
        log_error(f"{target_name} not found in workspace")
        return False
    
    if dest.exists():
        log_warning(f"{target_name} already exists at root")
        response = input(f"  Overwrite? (y/n): ").lower()
        if response != 'y':
            return False
        shutil.rmtree(dest)
    
    shutil.move(str(source), str(dest))
    log_success(f"Moved {target_name} to root: {dest}")
    return dest

def rebrand_vscode(vscode_dir: Path, telemetry_endpoint: str):
    """Rebrand VS Code to Vibecoda and inject custom telemetry"""
    log_section("REBRANDING VS CODE → VIBECODA")
    
    # 1. Strip Microsoft telemetry
    log("Step 1: Stripping Microsoft telemetry...")
    
    telemetry_files = [
        vscode_dir / "src/vs/platform/telemetry",
        vscode_dir / "src/vs/code/electron-main/app.ts",
        vscode_dir / "src/vs/workbench/contrib/telemetry",
    ]
    
    # Disable telemetry in product.json
    product_json = vscode_dir / "product.json"
    if product_json.exists():
        with open(product_json, 'r') as f:
            product = json.load(f)
        
        # Remove MS telemetry endpoints
        product.pop('aiConfig', None)
        product.pop('enableTelemetry', None)
        product['telemetryEndpoint'] = telemetry_endpoint
        
        # Rebrand
        product['nameShort'] = 'Vibecoda'
        product['nameLong'] = 'Vibecoda'
        product['applicationName'] = 'vibecoda'
        product['dataFolderName'] = '.vibecoda'
        product['quality'] = 'stable'
        product['updateUrl'] = 'https://updates.vibecoda.dev'
        
        with open(product_json, 'w') as f:
            json.dump(product, f, indent=2)
        
        log_success("Updated product.json")
    
    # 2. Inject custom telemetry
    log("\nStep 2: Injecting custom telemetry...")
    
    custom_telemetry = vscode_dir / "src/vs/platform/telemetry/common/vibecoda-telemetry.ts"
    custom_telemetry.parent.mkdir(parents=True, exist_ok=True)
    
    telemetry_code = f"""/*---------------------------------------------------------------------------------------------
 *  Copyright (c) Vibecoda. All rights reserved.
 *  Licensed under the MIT License.
 *--------------------------------------------------------------------------------------------*/

export class VibecodaTelemetry {{
    private endpoint = '{telemetry_endpoint}';
    private sessionId = this.generateSessionId();
    
    private generateSessionId(): string {{
        return `${{Date.now()}}-${{Math.random().toString(36).substr(2, 9)}}`;
    }}
    
    public async sendEvent(eventName: string, properties?: Record<string, any>): Promise<void> {{
        try {{
            const payload = {{
                event: eventName,
                sessionId: this.sessionId,
                timestamp: new Date().toISOString(),
                properties: properties || {{}},
                version: require('../../../../package.json').version
            }};
            
            await fetch(this.endpoint, {{
                method: 'POST',
                headers: {{
                    'Content-Type': 'application/json',
                }},
                body: JSON.stringify(payload)
            }});
        }} catch (error) {{
            // Silently fail - don't break editor functionality
            console.debug('Telemetry error:', error);
        }}
    }}
    
    public trackCodeCompletion(accepted: boolean, model: string, latency: number): void {{
        this.sendEvent('code_completion', {{
            accepted,
            model,
            latency
        }});
    }}
    
    public trackEditorAction(action: string, context?: Record<string, any>): void {{
        this.sendEvent('editor_action', {{
            action,
            ...context
        }});
    }}
}}

export const vibeTelemetry = new VibecodaTelemetry();
"""
    
    custom_telemetry.write_text(telemetry_code)
    log_success("Created custom telemetry module")
    
    # 3. Rebrand strings
    log("\nStep 3: Rebranding strings throughout codebase...")
    
    replacements = [
        ('Visual Studio Code', 'Vibecoda'),
        ('vscode', 'vibecoda'),
        ('VSCode', 'Vibecoda'),
        ('VS Code', 'Vibecoda'),
        ('Code - OSS', 'Vibecoda'),
        ('code-oss', 'vibecoda'),
    ]
    
    extensions = ['.ts', '.js', '.json', '.html', '.css', '.md', '.txt']
    count = find_and_replace_recursive(vscode_dir, replacements, extensions)
    log_success(f"Rebranded {count} files")
    
    # 4. Update package.json
    log("\nStep 4: Updating package.json...")
    package_json = vscode_dir / "package.json"
    if package_json.exists():
        with open(package_json, 'r') as f:
            package = json.load(f)
        
        package['name'] = 'vibecoda'
        package['productName'] = 'Vibecoda'
        package['description'] = 'The editor for vibe coding'
        
        with open(package_json, 'w') as f:
            json.dump(package, f, indent=2)
        
        log_success("Updated package.json")
    
    log_success("\n✓ Vibecoda rebranding complete!")
    return True

def rebrand_onedev(onedev_dir: Path):
    """Rebrand OneDev to CodiBase"""
    log_section("REBRANDING ONEDEV → CODIBASE")
    
    log("Step 1: Rebranding strings...")
    
    replacements = [
        ('OneDev', 'CodiBase'),
        ('onedev', 'codibase'),
        ('ONEDEV', 'CODIBASE'),
    ]
    
    extensions = ['.java', '.xml', '.html', '.js', '.css', '.properties', '.md', '.gradle', '.yml', '.yaml']
    count = find_and_replace_recursive(onedev_dir, replacements, extensions)
    log_success(f"Rebranded {count} files")
    
    # Update main config files
    log("\nStep 2: Updating configuration files...")
    
    # Update build.gradle if exists
    build_gradle = onedev_dir / "build.gradle"
    if build_gradle.exists():
        replace_in_file(build_gradle, replacements)
        log_success("Updated build.gradle")
    
    # Update application properties
    for props_file in onedev_dir.rglob("application*.properties"):
        replace_in_file(props_file, replacements)
        log_success(f"Updated {props_file.name}")
    
    # Rename main package directories
    log("\nStep 3: Renaming package directories...")
    for java_dir in onedev_dir.rglob("io/onedev"):
        if java_dir.is_dir():
            new_dir = java_dir.parent / "io" / "codibase"
            new_dir.parent.mkdir(parents=True, exist_ok=True)
            if not new_dir.exists():
                shutil.move(str(java_dir), str(new_dir))
                log_success(f"Renamed package directory")
    
    log_success("\n✓ CodiBase rebranding complete!")
    return True

def rebrand_tabby(tabby_dir: Path):
    """Rebrand Tabby to PilotCodi"""
    log_section("REBRANDING TABBY → PILOTCODI")
    
    log("Step 1: Rebranding strings...")
    
    replacements = [
        ('Tabby', 'PilotCodi'),
        ('tabby', 'pilotcodi'),
        ('TABBY', 'PILOTCODI'),
    ]
    
    extensions = ['.rs', '.toml', '.yaml', '.yml', '.json', '.md', '.html', '.js', '.ts']
    count = find_and_replace_recursive(tabby_dir, replacements, extensions)
    log_success(f"Rebranded {count} files")
    
    # Update Cargo.toml files
    log("\nStep 2: Updating Cargo.toml files...")
    for cargo_file in tabby_dir.rglob("Cargo.toml"):
        replace_in_file(cargo_file, replacements)
        log_success(f"Updated {cargo_file.relative_to(tabby_dir)}")
    
    log_success("\n✓ PilotCodi rebranding complete!")
    return True

def setup_telemetry_pipeline(root_dir: Path, telemetry_endpoint: str):
    """Set up telemetry collection and processing pipeline"""
    log_section("SETTING UP TELEMETRY PIPELINE")
    
    telemetry_dir = root_dir / "telemetry-service"
    telemetry_dir.mkdir(exist_ok=True)
    
    # Create telemetry API service
    log("Creating telemetry API service...")
    
    api_code = f"""#!/usr/bin/env python3
\"\"\"
CodiBase Telemetry Collection Service
Collects telemetry from Vibecoda, PilotCodi, and CodiBase
\"\"\"

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
    \"\"\"Collect telemetry event\"\"\"
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
                json.dumps(data.get('properties', {{}}))
            )
        )
        conn.commit()
        conn.close()
        
        return {{"status": "success"}}
    except Exception as e:
        return {{"status": "error", "message": str(e)}}

@app.get("/stats")
async def get_stats():
    \"\"\"Get telemetry statistics\"\"\"
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
    
    return {{
        "total_events": total_events,
        "by_source": by_source,
        "top_events": top_events
    }}

@app.get("/health")
async def health():
    return {{"status": "healthy"}}

if __name__ == "__main__":
    import uvicorn
    uvicorn.run(app, host="0.0.0.0", port=8080)
"""
    
    (telemetry_dir / "api.py").write_text(api_code)
    log_success("Created telemetry API service")
    
    # Create requirements.txt
    requirements = """fastapi==0.104.1
uvicorn[standard]==0.24.0
python-multipart==0.0.6
"""
    (telemetry_dir / "requirements.txt").write_text(requirements)
    log_success("Created requirements.txt")
    
    # Create Dockerfile
    dockerfile = """FROM python:3.11-slim

WORKDIR /app

COPY requirements.txt .
RUN pip install --no-cache-dir -r requirements.txt

COPY api.py .

EXPOSE 8080

CMD ["python", "api.py"]
"""
    (telemetry_dir / "Dockerfile").write_text(dockerfile)
    log_success("Created Dockerfile")
    
    log_success("\n✓ Telemetry pipeline setup complete!")
    return True

def setup_build_deploy(root_dir: Path):
    """Set up build and deploy orchestration"""
    log_section("SETTING UP BUILD & DEPLOY ORCHESTRATION")
    
    # Create build script
    log("Creating build orchestration script...")
    
    build_script = """#!/bin/bash
set -e

echo "======================================"
echo "CodiBase Stack - Build All Components"
echo "======================================"

# Colors
GREEN='\\033[0;32m'
BLUE='\\033[0;34m'
NC='\\033[0m'

# Build Vibecoda
echo -e "${BLUE}Building Vibecoda...${NC}"
cd vibecoda
npm install
npm run build
echo -e "${GREEN}✓ Vibecoda built${NC}"
cd ..

# Build CodiBase
echo -e "${BLUE}Building CodiBase...${NC}"
cd codibase
./gradlew clean build -x test
echo -e "${GREEN}✓ CodiBase built${NC}"
cd ..

# Build PilotCodi
echo -e "${BLUE}Building PilotCodi...${NC}"
cd pilotcodi
cargo build --release
echo -e "${GREEN}✓ PilotCodi built${NC}"
cd ..

# Build Telemetry Service
echo -e "${BLUE}Building Telemetry Service...${NC}"
cd telemetry-service
docker build -t codibase-telemetry:latest .
echo -e "${GREEN}✓ Telemetry Service built${NC}"
cd ..

echo ""
echo -e "${GREEN}======================================"
echo "✓ All components built successfully!"
echo "======================================${NC}"
"""
    
    (root_dir / "build-all.sh").write_text(build_script)
    (root_dir / "build-all.sh").chmod(0o755)
    log_success("Created build-all.sh")
    
    # Create docker-compose for local development
    log("Creating docker-compose for local dev...")
    
    docker_compose = """version: '3.8'

services:
  telemetry:
    build: ./telemetry-service
    ports:
      - "8080:8080"
    volumes:
      - telemetry-data:/app/data
    restart: unless-stopped

  codibase:
    build: ./codibase
    ports:
      - "6610:6610"
    environment:
      - HIBERNATE_DIALECT=org.hibernate.dialect.PostgreSQLDialect
      - HIBERNATE_CONNECTION_DRIVER_CLASS=org.postgresql.Driver
      - HIBERNATE_CONNECTION_URL=jdbc:postgresql://postgres:5432/codibase
      - HIBERNATE_CONNECTION_USERNAME=codibase
      - HIBERNATE_CONNECTION_PASSWORD=codibase
    depends_on:
      - postgres
    restart: unless-stopped

  pilotcodi:
    build: ./pilotcodi
    ports:
      - "8081:8081"
    environment:
      - TELEMETRY_ENDPOINT=http://telemetry:8080/collect
    restart: unless-stopped

  postgres:
    image: postgres:15
    environment:
      - POSTGRES_DB=codibase
      - POSTGRES_USER=codibase
      - POSTGRES_PASSWORD=codibase
    volumes:
      - postgres-data:/var/lib/postgresql/data
    restart: unless-stopped

volumes:
  telemetry-data:
  postgres-data:
"""
    
    (root_dir / "docker-compose.yml").write_text(docker_compose)
    log_success("Created docker-compose.yml")
    
    # Create deploy script
    deploy_script = """#!/bin/bash
set -e

echo "======================================"
echo "CodiBase Stack - Deploy All Services"
echo "======================================"

# Build all components first
./build-all.sh

# Deploy with docker-compose
echo "Starting services with docker-compose..."
docker-compose up -d

echo ""
echo "✓ All services deployed!"
echo ""
echo "Service URLs:"
echo "  - Telemetry API: http://localhost:8080"
echo "  - CodiBase: http://localhost:6610"
echo "  - PilotCodi: http://localhost:8081"
echo ""
echo "Check status: docker-compose ps"
echo "View logs: docker-compose logs -f"
"""
    
    (root_dir / "deploy.sh").write_text(deploy_script)
    (root_dir / "deploy.sh").chmod(0o755)
    log_success("Created deploy.sh")
    
    # Create README
    readme = """# CodiBase Stack

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
"""
    
    (root_dir / "README.md").write_text(readme)
    log_success("Created README.md")
    
    log_success("\n✓ Build & deploy orchestration complete!")
    return True

def main():
    log(f"\n{Colors.BOLD}{Colors.MAGENTA}╔═══════════════════════════════════════════════════════════╗{Colors.RESET}")
    log(f"{Colors.BOLD}{Colors.MAGENTA}║  CodiBase Stack - Rebrand & Setup Script                 ║{Colors.RESET}")
    log(f"{Colors.BOLD}{Colors.MAGENTA}╚═══════════════════════════════════════════════════════════╝{Colors.RESET}\n")
    
    # Get workspace location
    default_workspace = Path.cwd() / "codibase-workspace"
    workspace_input = input(f"Workspace directory [{default_workspace}]: ").strip()
    workspace_dir = Path(workspace_input) if workspace_input else default_workspace
    
    if not workspace_dir.exists():
        log_error(f"Workspace not found: {workspace_dir}")
        sys.exit(1)
    
    root_dir = workspace_dir.parent
    
    # Get telemetry endpoint
    telemetry_endpoint = input("Telemetry endpoint [http://localhost:8080/collect]: ").strip()
    if not telemetry_endpoint:
        telemetry_endpoint = "http://localhost:8080/collect"
    
    log(f"\n{Colors.YELLOW}This will:{Colors.RESET}")
    log(f"  1. Move codebases to root: {root_dir}")
    log(f"  2. Rebrand VS Code → Vibecoda")
    log(f"  3. Rebrand OneDev → CodiBase")
    log(f"  4. Rebrand Tabby → PilotCodi")
    log(f"  5. Setup telemetry pipeline")
    log(f"  6. Setup build/deploy orchestration")
    
    response = input(f"\n{Colors.YELLOW}Continue? (y/n): {Colors.RESET}").lower()
    if response != 'y':
        log_warning("Aborted")
        sys.exit(0)
    
    # Move codebases to root
    log_section("MOVING CODEBASES TO ROOT")
    
    vibecoda_dir = move_to_root(workspace_dir, "vibecoda")
    codibase_dir = move_to_root(workspace_dir, "codibase")
    pilotcodi_dir = move_to_root(workspace_dir, "pilotcodi")
    
    if not all([vibecoda_dir, codibase_dir, pilotcodi_dir]):
        log_error("Failed to move all codebases")
        sys.exit(1)
    
    # Rebrand each component
    try:
        rebrand_vscode(vibecoda_dir, telemetry_endpoint)
        rebrand_onedev(codibase_dir)
        rebrand_tabby(pilotcodi_dir)
        
        # Setup infrastructure
        setup_telemetry_pipeline(root_dir, telemetry_endpoint)
        setup_build_deploy(root_dir)
        
        # Clean up workspace directory
        if workspace_dir.exists() and not list(workspace_dir.iterdir()):
            workspace_dir.rmdir()
            log_success(f"Removed empty workspace directory")
        
        # Final summary
        log_section("SETUP COMPLETE")
        log_success("✓ All components rebranded successfully!")
        log_success("✓ Telemetry pipeline configured")
        log_success("✓ Build & deploy orchestration ready")
        
        log(f"\n{Colors.YELLOW}Next steps:{Colors.RESET}")
        log(f"  1. cd {root_dir}")
        log(f"  2. Review changes in each directory")
        log(f"  3. Run ./build-all.sh to build everything")
        log(f"  4. Run ./deploy.sh to start all services")
        log(f"  5. Access CodiBase at http://localhost:6610")
        
        log(f"\n{Colors.GREEN}🚀 Your CodiBase stack is ready to ship!{Colors.RESET}\n")
        
    except Exception as e:
        log_error(f"Setup failed: {e}")
        import traceback
        traceback.print_exc()
        sys.exit(1)

if __name__ == "__main__":
    try:
        main()
    except KeyboardInterrupt:
        log_error("\n\nScript interrupted by user")
        sys.exit(1)
