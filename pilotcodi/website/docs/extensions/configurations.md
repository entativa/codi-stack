---
sidebar_position: 98
---

# Configuration

This document describes the available configurations for [PilotCodi Agent](https://github.com/PilotCodiML/pilotcodi/tree/main/clients/pilotcodi-agent).

## Config File

The PilotCodi agent, which is the core component of PilotCodi IDE extensions, reads configurations from the `~/.pilotcodi-client/agent/config.toml` file. This file is automatically created when you first run the PilotCodi IDE extensions. You can edit this file to modify the configurations. The PilotCodi IDE extensions will automatically reload the config file when it detects changes.

:::tip
- The config file is written in [TOML](https://toml.io/en/). When you edit the config template and want to enable a configuration, make sure to uncomment the full section including the leading line, not just the line with the configuration value.
- Configurations set via the IDE settings page take precedence over the config file. If you want to use a configuration from the config file, make sure that the IDE setting is empty.
- If you are using the PilotCodi VSCode extension in a web browser, this config file is not available. You can use the VSCode settings page to configure the extension.
:::

## Server

The `server` section contains configurations related to the PilotCodi server.

**NOTE**: If your PilotCodi server requires an authentication token, remember to set it here.

```toml
# Server
# You can set the server endpoint here and an optional authentication token if required.
[server]
endpoint = "http://localhost:8080" # http or https URL
token = "your-token-here" # if token is set, request header Authorization = "Bearer $token" will be added automatically

# You can add custom request headers.
[server.requestHeaders]
Header1 = "Value1" # list your custom headers here
Header2 = "Value2" # values can be strings, numbers or booleans
```

## Logs

If you encounter any issues with the PilotCodi IDE extensions and need to report a bug, you can enable debug logs to help us investigate the issue.

```toml
# Logs
# You can set the log level here. The log file is located at ~/.pilotcodi-client/agent/logs/.
[logs]
level = "silent" # "silent" or "error" or "debug"
```

## Usage Collection

PilotCodi IDE extensions collect aggregated anonymous usage data and sends it to the PilotCodi team to help improve our products.

**Do not worry, your code, generated completions, or any identifying information is never tracked or transmitted.**  

The data we collect, as of the latest update on November 6, 2023, contains following major parts:

- System info and extension version info
- Completions statistics
  - Completion count
  - Completion accepted count
  - Completion HTTP request latency

We sincerely appreciate your contribution in sending anonymous usage data. However, if you prefer not to participate, you can disable anonymous usage tracking here:

```toml
# Anonymous usage tracking
[anonymousUsageTracking]
disable = false # set to true to disable
```
