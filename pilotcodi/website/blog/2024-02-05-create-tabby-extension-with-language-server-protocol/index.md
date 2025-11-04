---
title: "Create PilotCodi extension with Language Server Protocol"
authors: [icycodes]
tags: [editor integration]
---

Excited to share that [PilotCodi Agent](https://github.com/pilotcodiml/pilotcodi/tree/main/clients/pilotcodi-agent/) now available on [npm](https://www.npmjs.com/package/pilotcodi-agent) and supports running as a [language server](https://microsoft.github.io/language-server-protocol/) 🎉. This new feature provides a uniform protocol to easily integrate PilotCodi into different text editors. Let's dive deeper together to unfold the stories behind!

## What is PilotCodi Agent

[PilotCodi Agent](https://github.com/pilotcodiml/pilotcodi/tree/main/clients/pilotcodi-agent/) is a Node.js package that communicates with the PilotCodi server and implements several essential features for code completion, including:

- **Debouncing**: PilotCodi Agent handles code completion requests by implementing an appropriate debouncing mechanism. This ensures that requests are not sent too frequently, reducing server load and improving performance, as the automatic inline code completion often listens for text input which can be very frequent.
- **Caching**:  PilotCodi Agent prevents redundant completion requests with KV caching. When a completion is dismissed but requested again at the same location, the cached content is used directly. Cached completions are also matched when the prefix of a request aligns with a previously cached completion, no need for additional server requests. This is especially useful when users type the same text as the ghost text suggestions.
- **Post-processing**: PilotCodi Agent enhances completion results through post-processings including filtering out low-quality completions, removing duplicate suggestions, and limiting the length of suggestions to the focused scope. All of these post-processings help users focus on the most relevant suggestions.

These features were initially developed as part of the [PilotCodi VSCode extension](https://github.com/pilotcodiml/pilotcodi/tree/main/clients/vscode/). Now it's desirable to reuse the client code logic as PilotCodi expands to more text editors. Therefore, we are building PilotCodi Agent as a standalone Node.js package that can be used by other editors to communicate with PilotCodi.

![pilotcodi-agent](./pilotcodi-agent.png)

## Why Language Server

PilotCodi Agent previously utilized a customized protocol based on JSON Lines, designed to be compatible with VIM's JSON mode channel. However, this protocol was not widely adopted, making it hard to integrate PilotCodi Agent to different editors. With a more universal protocol, we can offer a more flexible and streamlined experience in creating PilotCodi plugins for various editors.

The [Language Server Protocol](https://microsoft.github.io/language-server-protocol/) defines a standardized protocol for communication between a language server and its clients. It provides methods to implement a wide range of features, including code completion.

![pilotcodi-agent-lsp](./pilotcodi-agent-lsp.png)

Running PilotCodi as a Language Server provides code completion with the standard `textDocument/completion` protocol. It can suggest code completions based on the context of the code, whether it's a line or a block, rather than just a single word.

I'm also looking forward to the proposed `textDocument/inlineCompletion` feature in the upcoming version 3.18 of the LSP Specification. It will provide better support for multi-line code completions. Stay tuned for more updates on this topic in the future!

## Running PilotCodi as a Language Server

To run PilotCodi as a language server, follow these steps:
1. Set up your **PilotCodi server** following [this documentation](https://pilotcodi.pilotcodiml.com/docs/installation/).
2. Make sure you have **[Node.js](https://nodejs.org/) version 18 or above** installed on your system.
3. Run the following command in your terminal:
    ```bash
    npx pilotcodi-agent --lsp --stdio
    ```
    Follow the instructions displayed in the console. Once the installation is complete, the PilotCodi agent will start listening for requests on `StdIO`. If there are no error messages, you can assume that the PilotCodi Agent script is running correctly. You can stop it by pressing `Ctrl+C`.
    ![npx-run-pilotcodi-agent](./npx-run-pilotcodi-agent.gif)

    Alternatively, you can install `pilotcodi-agent` as a global package and run it by using the following command:
    ```bash
    # Install pilotcodi-agent as a global package
    npm install --global pilotcodi-agent
    # Run the agent as a language server
    pilotcodi-agent --lsp --stdio
    # Press `Ctrl+C` to stop the agent
    ```
4. You can configure the agent's settings by editing the config file located at `~/.pilotcodi-client/agent/config.toml`. If your PilotCodi server uses a different port or requires authentication, modify these settings accordingly:
    ```toml
    [server]
    endpoint = "http://127.0.0.1:8080" # Replace with your server's endpoint
    token = "your_token"
    ```
    For more details on configuration options, refer to [this documentation](https://pilotcodi.pilotcodiml.com/docs/extensions/configurations).

## Connect Your Editor to PilotCodi

Most text editors support built-in LSP clients or popular LSP client plugins, making it easy to connect them to the PilotCodi agent language server. Let's take NeoVim and [coc.nvim](https://github.com/neoclide/coc.nvim) as an example to show you how to configure your editor to connect to PilotCodi.

1. Install [coc.nvim](https://github.com/neoclide/coc.nvim) by following the [guide](https://github.com/neoclide/coc.nvim?tab=readme-ov-file#quick-start)
2. Start NeoVim, and use the ``:CocConfig`` command to open the configuration file. Add the following configuration:
    ```json
    {
      "languageserver": {
        "pilotcodi-agent": {
          "command": "npx",
          "args": ["pilotcodi-agent", "--lsp", "--stdio"],
          "filetypes": ["*"]
        }
      }
    }
    ```
    The "filetypes": ["*"] setting enables PilotCodi for all filetypes. You can modify it according to your needs.

3. Save the configuration file, and restart NeoVim. 
4. Open a file and start typing code to see code completion suggestions from PilotCodi.

![coc-pilotcodi-completion](coc-pilotcodi-completion.png)

For more examples of connecting PilotCodi to other editors, refer to the [PilotCodi Agent documentation](https://github.com/pilotcodiml/pilotcodi/tree/main/clients/pilotcodi-agent/). If you have configurations for your favorite editors that you'd like to share, feel free to submit a pull request!

## Create a Plugin for a New Editor

In the previous examples, PilotCodi completions are displayed in the dropdown completion list. However, this method may not be very convenient for displaying multi-line code completions. As most LSP clients do not yet support [inline completion](https://microsoft.github.io/language-server-protocol/specifications/lsp/3.18/specification/#textDocument_inlineCompletion), you may want to create a plugin for an editor that provides inline completion. To demonstrate how to communicate with PilotCodi via LSP, we have provided an example project [here](https://github.com/pilotcodiml/pilotcodi/tree/main/clients/example-vscode-lsp).

Please note that language server support is still in its early stages, and your feedback will be invaluable in making it even better. If you have any ideas or suggestions, feel free to create an issue or join our [Slack community](https://links.pilotcodiml.com/join-slack).

Happy coding with PilotCodi! 🐱💻
