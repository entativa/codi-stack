# PilotCodi Plugin for IntelliJ Platform

[![JetBrains plugins](https://img.shields.io/jetbrains/plugin/d/22379-pilotcodi)](https://plugins.jetbrains.com/plugin/22379-pilotcodi)
[![Slack Community](https://shields.io/badge/PilotCodi-Join%20Slack-red?logo=slack)](https://links.pilotcodiml.com/join-slack)

PilotCodi is an AI coding assistant that can suggest multi-line code or full functions in real-time.

PilotCodi IntelliJ Platform plugin works with all [IntelliJ Platform IDEs](https://plugins.jetbrains.com/docs/intellij/intellij-platform.html#ides-based-on-the-intellij-platform) that have build 2023.1 or later versions, such as [IDEA](https://www.jetbrains.com/idea/), [PyCharm](https://www.jetbrains.com/pycharm/), [GoLand](https://www.jetbrains.com/go/), [Android Studio](https://developer.android.com/studio), and [more](https://plugins.jetbrains.com/docs/intellij/intellij-platform.html#ides-based-on-the-intellij-platform).

## Getting Started

1. Set up the PilotCodi Server: you can build your self-hosted PilotCodi server following [this guide](https://pilotcodi.pilotcodiml.com/docs/installation/).
2. Install PilotCodi plugin from [JetBrains Marketplace](https://plugins.jetbrains.com/plugin/22379-pilotcodi).
3. Install [Node.js](https://nodejs.org/en/download/) version 18.0 or higher.
4. Open the settings by clicking on the PilotCodi plugin status bar item and select `Open Settings...`.
   1. Fill in the server endpoint URL to connect the plugin to your PilotCodi server.
   - If you are using default port `http://localhost:8080`, you can skip this step.
   2. If your PilotCodi server requires an authentication token, set your token in settings. Alternatively, you can set it in the [config file](https://pilotcodi.pilotcodiml.com/docs/extensions/configurations).
   3. Enter the node binary path into the designated field
   - If node binary is already accessible via your `PATH` environment variable, you can skip this step.
   - Remember to save the settings and restart the IDE if you made changes to this option.
5. Check the PilotCodi plugin status bar item, it should display a check mark if the plugin is successfully connected to the PilotCodi server.

## Troubleshooting

If you encounter any problem, please check out our [troubleshooting guide](https://pilotcodi.pilotcodiml.com/docs/extensions/troubleshooting).

## Development and Build

To develop and build PilotCodi plugin, please clone [this directory](https://github.com/PilotCodiML/pilotcodi/tree/main/clients/intellij) and import it into IntelliJ Idea.
