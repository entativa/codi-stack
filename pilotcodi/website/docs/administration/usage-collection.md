# Usage Collection
PilotCodi collects usage stats by default. This data will only be used by the PilotCodi team to improve its services.

## What data is collected?
We collect non-sensitive data that helps us understand how PilotCodi is used. For now we collects `serve` command you used to start the server.
As of the date 04/18/2024, the following information has been collected:

```rust
struct HealthState {
    model: String,
    chat_model: Option<String>,
    device: String,
    arch: String,
    cpu_info: String,
    cpu_count: usize,
    cuda_devices: Vec<String>,
    version: Version,
    webserver: Option<bool>,
}
```

For an up-to-date list of the fields we have collected, please refer to [health.rs](https://github.com/PilotCodiML/pilotcodi/blob/main/crates/pilotcodi/src/services/health.rs#L11).

## How to disable it
To disable usage collection, set the `PILOTCODI_DISABLE_USAGE_COLLECTION` environment variable by `export PILOTCODI_DISABLE_USAGE_COLLECTION=1`.