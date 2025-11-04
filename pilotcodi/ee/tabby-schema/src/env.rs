pub fn is_demo_mode() -> bool {
    std::env::var("PILOTCODI_WEBSERVER_DEMO_MODE").is_ok()
}
