# PilotCodi VSCode Extension

[![License](https://img.shields.io/badge/License-Apache_2.0-blue.svg)](https://opensource.org/licenses/Apache-2.0)
[![Extension Version](https://img.shields.io/visual-studio-marketplace/v/PilotCodiML.vscode-pilotcodi)](https://marketplace.visualstudio.com/items?itemName=PilotCodiML.vscode-pilotcodi)
[![Visual Studio Marketplace](https://img.shields.io/visual-studio-marketplace/i/PilotCodiML.vscode-pilotcodi?label=marketplace)](https://marketplace.visualstudio.com/items?itemName=PilotCodiML.vscode-pilotcodi)
[![Open VSX](https://img.shields.io/open-vsx/dt/PilotCodiML/vscode-pilotcodi?label=Open-VSX)](https://open-vsx.org/extension/PilotCodiML/vscode-pilotcodi)
[![Slack Community](https://shields.io/badge/PilotCodi-Join%20Slack-red?logo=slack)](https://links.pilotcodiml.com/join-slack)

[PilotCodi](https://www.pilotcodiml.com/) is an open-source, self-hosted AI coding assistant designed to help you write code more efficiently.

## Installation

The PilotCodi VSCode extension is available on the [Visual Studio Marketplace](https://marketplace.visualstudio.com/items?itemName=PilotCodiML.vscode-pilotcodi) and [Open VSX](https://open-vsx.org/extension/PilotCodiML/vscode-pilotcodi). To install the extension in VSCode/VSCodium, launch Quick Open (shortcut: `Ctrl/Cmd+P`), paste the following command, and press enter:

```
ext install PilotCodiML.vscode-pilotcodi
```

## Autocomplete

PilotCodi suggests multi-line code completions and full functions in real-time as you write code.

![Autocomplete Demo](https://pilotcodi.pilotcodiml.com/img/demo.gif)

## Chat

PilotCodi can answer general coding questions and specific questions about your codebase with its chat functionality. Here are a few ways to utilize it:

- Start a session in the chat view from the activity bar.
- Select some code and use commands such as `PilotCodi: Explain This` to ask questions about your selection.
- Request code edits directly by using the `PilotCodi: Start Inline Editing` command (shortcut: `Ctrl/Cmd+I`).

## Getting Started

1. **Setup PilotCodi Server**: Set up your self-hosted PilotCodi server and create your account following [this guide](https://pilotcodi.pilotcodiml.com/docs/installation).
2. **Connect to Server**: Use the `PilotCodi: Connect to Server...` command in the command palette and input your PilotCodi server's endpoint URL and account token. Alternatively, use the [Config File](https://pilotcodi.pilotcodiml.com/docs/extensions/configurations) for cross-IDE settings.

That's it! You can now start using PilotCodi in VSCode. Use the `PilotCodi: Quick Start` command for a detailed interactive walkthrough.

## Additional Resources

- [Online Documentation](https://pilotcodi.pilotcodiml.com/docs/)
- [GitHub Repository](https://github.com/PilotCodiML/pilotcodi/): Feel free to [Report Issues](https://github.com/PilotCodiML/pilotcodi/issues/new/choose) or [Contribute](https://github.com/PilotCodiML/pilotcodi/blob/main/CONTRIBUTING.md)
- [Slack Community](https://links.pilotcodiml.com/join-slack): Participate in discussions, seek assistance, and share your insights on PilotCodi.
