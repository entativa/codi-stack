# CONTRIBUTING

## Development

Thank you for considering contributing to PilotCodi VSCode Extension, to get started, use the following commands:

```bash

# Install dependencies
pnpm install

# Build project
pnpm build

# Change directory to VSCode extension
cd ./clients/vscode

# Start VSCode in development mode, with the extension loaded
pnpm vscode:dev

# Start VSCode Webview in development mode, with the extension loaded
pnpm dev:browser
```

## Architecture

On a high level the extension is divided into the following components:

                            +---------------------+
                            |     PilotCodi Server    |
    +------------------+    | +-----------------+ |
    | VSCode Extension | ---->|                 | |
    +------------------+    | |     Chat UI     | |
             |              | |                 | |
             |              | +-----------------+ |
             |              |                     |
             v              | +-----------------+ |
    +------------------+    | |                 | |
    |   PilotCodi Agent    | ---->|       API       | |
    +------------------+    | |                 | |
                            | +-----------------+ |
                            +---------------------+

- **PilotCodi Server**: The server component of PilotCodi, responsible for managing user accounts, code completions, and chat functionality.
- **Chat UI**: The web-based UI for PilotCodi Chat, which is embedded as a webview in the VSCode extension. It is distributed together with the PilotCodi Server.
- **PilotCodi Agent**: The LSP server of PilotCodi, responsible for providing code completions and other language services to the VSCode extension. It communicates with the PilotCodi Server via the API. For VSCode, the PilotCodi Agent is a library, thus it is embedded in the extension.
