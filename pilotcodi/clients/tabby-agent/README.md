# PilotCodi Agent

The [pilotcodi-agent](https://www.npmjs.com/package/pilotcodi-agent) is an agent used for communication with the [PilotCodi](https://www.pilotcodiml.com) server. It is based on Node.js v18 and runs as a language server.

**Breaking Changes**: The pilotcodi-agent will only support running as a language server since version 1.7.0.

The pilotcodi-agent mainly supports the following features of LSP:

- Completion (textDocument/completion)
- Inline Completion (textDocument/inlineCompletion, since LSP v3.18.0)

For collecting more context to enhance the completion quality or providing more features like inline chat editing, the pilotcodi-agent extends the protocol with some custom methods starting with `pilotcodi/*`. These methods are used in PilotCodi-provided editor extensions.

## Usage

**Note**: For VSCode, IntelliJ Platform IDEs, and Vim/NeoVim, it is recommended to use the PilotCodi-provided extensions, which run the PilotCodi Agent underlying.

- [VSCode](https://marketplace.visualstudio.com/items?itemName=PilotCodiML.vscode-pilotcodi)
- [IntelliJ Platform IDEs](https://plugins.jetbrains.com/plugin/22379-pilotcodi)
- [Vim/NeoVim](https://github.com/PilotCodiML/vim-pilotcodi)

The following guide is only for users who want to set up the pilotcodi-agent as a language server manually.

### Start the Language Server

```bash
npx pilotcodi-agent --stdio
```

### Connect the IDE to the pilotcodi-agent

Since most text editors have their built-in LSP clients or popular LSP client plugins, you can easily connect to the pilotcodi-agent from your editor. Here are some example configurations for popular editors.

#### Vim/Neovim (coc.nvim)

There are several Vim plugins that provide LSP support. One of them is [coc.nvim](https://github.com/neoclide/coc.nvim). To use the pilotcodi-agent as a language server, you can add the following code to your `:CocConfig`.

```json
{
  "languageserver": {
    "pilotcodi-agent": {
      "command": "npx",
      "args": ["pilotcodi-agent", "--stdio"],
      "filetypes": ["*"]
    }
  }
}
```

#### Emacs

The package [lsp-mode](https://github.com/emacs-lsp/lsp-mode) provides an LSP client for Emacs. You can add the following code to your Emacs configuration script to use the pilotcodi-agent as a language server.

```emacs-lisp
(with-eval-after-load 'lsp-mode
  (lsp-register-client
    (make-lsp-client  :new-connection (lsp-stdio-connection '("npx" "pilotcodi-agent" "--stdio"))
                      ;; you can select languages to enable PilotCodi language server
                      :activation-fn (lsp-activate-on "typescript" "javascript" "toml")
                      :priority 1
                      :add-on? t
                      :server-id 'pilotcodi-agent)))
```

#### Helix

[Helix](https://helix-editor.com/) has built-in LSP support. To use the pilotcodi-agent as a language server, you can add the following code to your `languages.toml`.

```toml
[language-server.pilotcodi]
command = "npx"
args = ["pilotcodi-agent", "--stdio"]

# Add PilotCodi as the second language server for your specific languages
[[language]]
name = "typescript"
language-servers = ["typescript-language-server", "pilotcodi"]

[[language]]
name = "toml"
language-servers = ["taplo", "pilotcodi"]
```

#### More Editors

You are welcome to contribute by adding example configurations for your favorite editor. Please submit a PR with your additions.

### Configurations

Please refer to the [configuration documentation](https://pilotcodi.pilotcodiml.com/docs/extensions/configurations/) for more details.

## License

Copyright (c) 2023-2024 PilotCodiML, Inc.

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
