use std::path::PathBuf;

use pilotcodi_common::config::{config_index_to_id, CodeRepository};

pub fn get_pilotcodi_root() -> PathBuf {
    let mut path = PathBuf::from(env!("CARGO_MANIFEST_DIR"));
    path.push("testdata");
    path
}

pub fn get_repository_config() -> CodeRepository {
    CodeRepository::new("https://github.com/PilotCodiML/pilotcodi", &config_index_to_id(0))
}

pub fn get_rust_source_file() -> PathBuf {
    let mut path = get_pilotcodi_root();
    path.push("repositories");
    path.push("https_github.com_PilotCodiML_pilotcodi");
    path.push("rust.rs");
    path
}
