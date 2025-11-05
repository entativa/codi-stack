# CodiBase

<div align="center">

![CodiBase Logo](docs/cblogo.png)

**The AI-Native Development Platform That GitHub Wishes It Built**

[![License](https://img.shields.io/badge/license-MIT-blue.svg)](LICENSE)
[![Beta](https://img.shields.io/badge/status-beta-yellow.svg)](https://codibase.dev)
[![Discord](https://img.shields.io/discord/placeholder?color=7289da&logo=discord&logoColor=white)](https://discord.gg/codibase)
[![Twitter Follow](https://img.shields.io/twitter/follow/codibase?style=social)](https://twitter.com/codibase)

[Website](https://codibase.dev) • [Docs](https://docs.codibase.dev) • [Discord](https://discord.gg/codibase) • [Twitter](https://twitter.com/codibase) • [Blog](https://blog.codibase.dev)

</div>

---

## 🚀 What is CodiBase?

CodiBase is a **complete development platform** that integrates Git hosting, AI-powered code completion, intelligent CI/CD, and real-time collaboration into one seamless experience. 

We're not just adding AI to an existing platform—we built the entire stack from the ground up with AI at its core.

### The Stack

```
┌─────────────────────────────────────────────────────────────┐
│                     🏠 CodiBase                              │
│              Git Hosting • CI/CD • Projects                  │
└─────────────────────────────────────────────────────────────┘
                            │
        ┌───────────────────┼───────────────────┐
        │                   │                   │
┌───────▼────────┐  ┌──────▼───────┐  ┌────────▼────────┐
│  💻 Vibecoda   │  │  🤖 PilotCodi │  │  📊 Telemetry   │
│  AI Editor     │  │  Hybrid AI    │  │  Analytics      │
└────────────────┘  └───────────────┘  └─────────────────┘
```

---

## ✨ Why CodiBase?

### vs GitHub
- ✅ **Built-in AI** at every layer (not bolted on like Copilot)
- ✅ **Free unlimited** private repos with AI features
- ✅ **Smart merge** conflict resolution
- ✅ **Real-time** collaborative editing
- ✅ **AI-generated** CI/CD pipelines
- ✅ **Semantic** code search

### vs GitLab
- ✅ **Better AI** integration (Copilot-level, but native)
- ✅ **Simpler setup** (no complex runners)
- ✅ **Real-time** collaboration
- ✅ **Better DX** (developer experience)

### vs Cursor
- ✅ **Full platform** (Git + Editor + CI/CD)
- ✅ **Own your data** and infrastructure
- ✅ **Team features** built-in
- ✅ **Lower cost** at scale

---

## 🎯 Key Features

### 🤖 AI-Powered Everything

#### **AI Code Review**
Automatic PR reviews with actionable feedback
```bash
# Every PR gets instant AI review
✓ Security vulnerabilities detected: 2
✓ Performance improvements: 5
✓ Best practice suggestions: 3
```

#### **Smart Merge**
AI resolves merge conflicts intelligently
```bash
# No more manual conflict resolution
git merge feature-branch
⚡ PilotCodi analyzing conflicts...
✓ Resolved 8/8 conflicts intelligently
```

#### **Code Completion**
Context-aware suggestions as you type
- Fast completions via PilotCodi (in-house Tabby)
- Complex refactoring via Claude API
- Learns from your codebase

#### **AI Pair Programming**
Ask questions, get answers, refactor code
```typescript
// Select code and ask: "make this more efficient"
// AI refactors and explains the changes
```

### 🔥 Git Hosting (All the GitHub stuff you expect)

- **Repositories**: Public, private, unlimited
- **Issues & PRs**: Templates, labels, milestones, assignees
- **Code Review**: Inline comments, suggestions, approvals
- **Projects**: Kanban boards with automation
- **Wikis**: Per-repo documentation
- **Releases**: Version management, changelogs
- **Actions/CI**: Built-in pipelines, custom workflows
- **Social**: Fork, star, watch, sponsor
- **Organizations**: Teams, permissions, billing

### ⚡ Developer Experience

#### **Vibecoda Editor**
Desktop app for "vibe coding" - flow state guaranteed
- Built on VS Code (but better)
- Native CodiBase integration
- AI at your fingertips
- Cross-platform (Mac, Windows, Linux)

#### **Real-time Collaboration**
Google Docs for code
- Live cursors
- Instant sync
- Integrated chat
- Conflict-free editing

#### **Semantic Search**
Find code by what it does, not just keywords
```bash
# Natural language search
"function that validates email addresses"
→ Found in 12 files across 5 repos
```

---

## 🚀 Quick Start

### For Users

#### 1. Sign Up
```bash
# Create account at codibase.dev
https://codibase.dev/signup
```

#### 2. Install Vibecoda
```bash
# Download for your OS
https://codibase.dev/download

# Or via CLI
brew install codibase/tap/vibecoda  # macOS
choco install vibecoda              # Windows
snap install vibecoda               # Linux
```

#### 3. Clone a Repository
```bash
# Via Vibecoda
Cmd+Shift+P → "CodiBase: Clone Repository"

# Or via CLI
git clone https://codibase.dev/username/repo.git
```

#### 4. Start Coding with AI
```typescript
// Just start typing, AI completes
function calculateTotal(items) {
  // AI suggests: return items.reduce((sum, item) => sum + item.price, 0);
}
```

### For Self-Hosting

#### Prerequisites
- Docker & Docker Compose
- 4GB RAM minimum
- 20GB disk space

#### Deploy
```bash
# Clone repository
git clone https://github.com/codibase/codibase-stack.git
cd codibase-stack

# Configure
cp .env.example .env
# Edit .env with your settings

# Build and deploy
./build-all.sh
./deploy.sh

# Access at http://localhost:6610
```

#### Configuration
```bash
# .env
CODIBASE_DOMAIN=codibase.yourcompany.com
CLAUDE_API_KEY=sk-ant-xxxxx
DATABASE_URL=postgresql://user:pass@host:5432/codibase
SMTP_HOST=smtp.gmail.com
SMTP_USER=noreply@yourcompany.com
```

---

## 📚 Documentation

### User Guides
- [Getting Started](docs/getting-started.md)
- [Vibecoda Tutorial](docs/vibecoda-tutorial.md)
- [AI Features Guide](docs/ai-features.md)
- [Project Management](docs/project-management.md)
- [CI/CD Pipelines](docs/cicd.md)

### Developer Docs
- [Architecture](docs/ARCHITECTURE.md)
- [Integration Guide](docs/INTEGRATION_GUIDE.md)
- [API Reference](docs/api/README.md)
- [Plugin Development](docs/plugins.md)
- [Contributing](CONTRIBUTING.md)

### Self-Hosting
- [Installation](docs/self-hosting/installation.md)
- [Configuration](docs/self-hosting/configuration.md)
- [Backup & Restore](docs/self-hosting/backup.md)
- [Troubleshooting](docs/self-hosting/troubleshooting.md)

---

## 🏗️ Architecture

### Components

#### CodiBase (Port 6610)
Git hosting, CI/CD, project management
- **Tech**: Java/Kotlin, Spring Boot
- **Database**: PostgreSQL
- **Cache**: Redis

#### Vibecoda (Desktop App)
AI-powered code editor
- **Tech**: TypeScript, Electron, VS Code base
- **Platform**: macOS, Windows, Linux

#### PilotCodi (Port 8081)
Hybrid AI completion service
- **Tech**: Rust
- **Models**: Tabby (fast) + Claude API (complex)
- **Features**: Code completion, chat, review

#### API Gateway (Port 8000)
Unified routing and load balancing
- **Tech**: Kotlin, Spring Cloud Gateway
- **Features**: Rate limiting, auth, metrics

#### Telemetry (Port 8080)
Usage analytics and monitoring
- **Tech**: Python, FastAPI
- **Database**: SQLite (dev), PostgreSQL (prod)

### Data Flow

```
User → Vibecoda → API Gateway → CodiBase
                      ↓
                  PilotCodi (AI)
                      ↓
                  Telemetry
```

---

## 🎨 Screenshots

### AI Code Review
![AI Review](docs/assets/screenshots/ai-review.png)

### Smart Merge
![Smart Merge](docs/assets/screenshots/smart-merge.png)

### Real-time Collaboration
![Collaboration](docs/assets/screenshots/collaboration.png)

### Vibecoda Editor
![Vibecoda](docs/assets/screenshots/vibecoda.png)

---

## 💰 Pricing

### Free Tier
Perfect for individuals and small teams
- ✅ Unlimited public repositories
- ✅ Unlimited private repositories
- ✅ Basic AI features (1000 completions/month)
- ✅ 5 collaborators
- ✅ Community support

### Pro ($10/month)
For serious developers
- ✅ Everything in Free
- ✅ Unlimited AI features
- ✅ Unlimited collaborators
- ✅ Advanced analytics
- ✅ Priority support
- ✅ Custom domains

### Enterprise ($50/user/month)
For teams and companies
- ✅ Everything in Pro
- ✅ Self-hosted option
- ✅ Custom AI models trained on your code
- ✅ SSO/SAML
- ✅ Audit logs
- ✅ SLA guarantee
- ✅ Dedicated support
- ✅ On-premise deployment

[Compare Plans →](https://codibase.dev/pricing)

---

## 🗺️ Roadmap

### ✅ Phase 1: Beta (Dec 2025)
- Core platform
- AI code review
- Vibecoda desktop app
- Basic telemetry

### 🔄 Phase 2: Launch (Q1 2026)
- Real-time collaboration
- Semantic code search
- Mobile apps
- Plugin marketplace

### 🔮 Phase 3: Enterprise (Q2 2026)
- Self-hosted option
- SSO/SAML
- Custom AI models
- Advanced security

### 🚀 Phase 4: Ecosystem (Q3-Q4 2026)
- Plugin marketplace
- Public API v2
- Community features
- International expansion

[Full Roadmap →](ROADMAP.md)

---

## 🤝 Contributing

We welcome contributions! CodiBase is built in the open.

### Ways to Contribute
- 🐛 Report bugs
- 💡 Suggest features
- 📝 Improve documentation
- 🔧 Submit pull requests
- 🌍 Translate to your language

### Getting Started
```bash
# Fork and clone
git clone https://github.com/yourusername/codibase-stack.git

# Create a branch
git checkout -b feature/awesome-feature

# Make changes and test
./build-all.sh
./deploy.sh

# Submit PR
git push origin feature/awesome-feature
```

See [CONTRIBUTING.md](CONTRIBUTING.md) for detailed guidelines.

---

## 🏆 Built With

### Open Source Foundations
- [OneDev](https://github.com/theonedev/onedev) → CodiBase
- [VS Code](https://github.com/microsoft/vscode) → Vibecoda
- [Tabby](https://github.com/TabbyML/tabby) → PilotCodi

### Technologies
- **Backend**: Java, Kotlin, Spring Boot, Rust
- **Frontend**: TypeScript, React, Electron
- **Database**: PostgreSQL, Redis
- **AI**: Anthropic Claude API, Custom models
- **Infrastructure**: Docker, Kubernetes, AWS

---

## 📊 Stats

<div align="center">

![GitHub Stars](https://img.shields.io/github/stars/codibase/codibase-stack?style=social)
![GitHub Forks](https://img.shields.io/github/forks/codibase/codibase-stack?style=social)
![GitHub Issues](https://img.shields.io/github/issues/codibase/codibase-stack)
![GitHub PRs](https://img.shields.io/github/issues-pr/codibase/codibase-stack)

</div>

---

## 🌟 Star History

<div align="center">

[![Star History Chart](https://api.star-history.com/svg?repos=codibase/codibase-stack&type=Date)](https://star-history.com/#codibase/codibase-stack&Date)

</div>

---

## 💬 Community

### Get Help
- 💬 [Discord](https://discord.gg/codibase) - Chat with the community
- 📧 [Email](mailto:support@codibase.dev) - Direct support
- 🐦 [Twitter](https://twitter.com/codibase) - Updates and announcements
- 📝 [Blog](https://blog.codibase.dev) - Tutorials and deep dives

### Stay Updated
- ⭐ Star this repository
- 👀 Watch for updates
- 🐦 Follow [@codibase](https://twitter.com/codibase)
- 📧 [Subscribe to newsletter](https://codibase.dev/newsletter)

---

## 📜 License

**Note**: While the codebase is currently MIT licensed during beta, we're evaluating more appropriate licensing for the production release to ensure sustainability while maintaining openness.

- CodiBase Platform: MIT (evaluating alternatives)
- Vibecoda Editor: MIT (evaluating alternatives)  
- PilotCodi: Apache 2.0
- Documentation: CC BY 4.0

See [LICENSE](LICENSE) for details.

---

## 🙏 Acknowledgments

### Inspiration
- GitHub for showing what's possible
- Cursor for proving AI-native editors work
- The open source community for building amazing tools

### Contributors
Thanks to all our contributors! 🎉

<a href="https://github.com/codibase/codibase-stack/graphs/contributors">
  <img src="https://contrib.rocks/image?repo=codibase/codibase-stack" />
</a>

---

## 📞 Contact

- **Website**: https://codibase.dev
- **Email**: hello@codibase.dev
- **Twitter**: [@codibase](https://twitter.com/codibase)
- **Discord**: [Join our community](https://discord.gg/codibase)
- **LinkedIn**: [CodiBase](https://linkedin.com/company/codibase)

---

## 🚀 Status

<div align="center">

**🎯 Current Focus**: Shipping Beta by December 31, 2025

![Status](https://img.shields.io/badge/status-building-orange)
![Users](https://img.shields.io/badge/users-coming%20soon-blue)
![Uptime](https://img.shields.io/badge/uptime-99.9%25-success)

</div>

---

<div align="center">

**Built with ❤️ for developers who deserve better tools**

[Get Started](https://codibase.dev/signup) • [Documentation](https://docs.codibase.dev) • [Community](https://discord.gg/codibase)

---

### ⭐ Star us on GitHub — it helps!

[⬆ Back to Top](#codibase)

</div>

---

<sub>Copyright © 2025 CodiBase. All rights reserved.</sub>
