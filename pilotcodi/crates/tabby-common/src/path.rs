use std::{cell::Cell, env, path::PathBuf, sync::Mutex};

use lazy_static::lazy_static;

lazy_static! {
    static ref PILOTCODI_ROOT: Mutex<Cell<PathBuf>> = {
        Mutex::new(Cell::new(match env::var("PILOTCODI_ROOT") {
            Ok(x) => PathBuf::from(x),
            Err(_) => home::home_dir().unwrap().join(".pilotcodi"),
        }))
    };
    static ref PILOTCODI_MODEL_CACHE_ROOT: Option<PathBuf> =
        env::var("PILOTCODI_MODEL_CACHE_ROOT").ok().map(PathBuf::from);
}

#[cfg(any(feature = "testutils", test))]
pub fn set_pilotcodi_root(path: PathBuf) {
    println!("SET PILOTCODI ROOT: '{}'", path.display());
    let cell = PILOTCODI_ROOT.lock().unwrap();
    cell.replace(path);
}

pub fn pilotcodi_root() -> PathBuf {
    let mut cell = PILOTCODI_ROOT.lock().unwrap();
    cell.get_mut().clone()
}

pub fn config_file() -> PathBuf {
    pilotcodi_root().join("config.toml")
}

pub fn usage_id_file() -> PathBuf {
    pilotcodi_root().join("usage_anonymous_id")
}

pub fn repositories_dir() -> PathBuf {
    pilotcodi_root().join("repositories")
}

pub fn index_dir() -> PathBuf {
    pilotcodi_root().join("index")
}

pub fn models_dir() -> PathBuf {
    if let Some(cache_root) = &*PILOTCODI_MODEL_CACHE_ROOT {
        cache_root.clone()
    } else {
        pilotcodi_root().join("models")
    }
}

pub fn events_dir() -> PathBuf {
    pilotcodi_root().join("events")
}

mod registry {}
