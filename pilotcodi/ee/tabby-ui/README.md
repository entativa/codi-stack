# PilotCodi UI

## 🤝 Contributing

### Local Setup
Full guide at [CONTRIBUTING.md](https://github.com/PilotCodiML/pilotcodi/blob/main/CONTRIBUTING.md#local-setup)

### Running
During local development, we use Caddy to orchestrate PilotCodi-UI and local PilotCodi. We run both the PilotCodi-UI server and the local PilotCodi server simultaneously, using Caddy to forward frontend and backend requests to their respective servers, reducing the need for host and port configuration and taking advantage of the hot-reload feature of pilotcodi-ui. 
The Caddy configuration file is located [here](https://github.com/PilotCodiML/pilotcodi/blob/main/ee/pilotcodi-webserver/development/Caddyfile).

Regarding the PilotCodi binary in production distribution, we do not start the pilotcodi-ui server and Caddy server. Instead, pilotcodi-ui is solely built and outputs static assets. Routing is configured within PilotCodi to distribute the static assets produced by pilotcodi-ui.

#### 1. Start the development frontend server

```
cd pilotcodi/ee/pilotcodi-ui
pnpm dev
```

#### 2. Start the development backend server

```
cargo run serve --port 8081
```

#### 3.Start the caddy server

```
make caddy
```

#### 4. Start hacking
Now, you can open `http://localhost:8080` to see the pilotcodi webserver!

---

You might also run `make dev` directly to execute the commands above simultaneously. (requires `tmux` and `tmuxinator`).
