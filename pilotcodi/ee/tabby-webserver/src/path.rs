use std::path::PathBuf;

use pilotcodi_common::path::pilotcodi_root;

pub fn pilotcodi_ee_root() -> PathBuf {
    pilotcodi_root().join("ee")
}

pub fn db_file() -> PathBuf {
    if cfg!(feature = "prod") {
        pilotcodi_ee_root().join("db.sqlite")
    } else {
        pilotcodi_ee_root().join("dev-db.sqlite")
    }
}

pub fn background_jobs_dir() -> PathBuf {
    if cfg!(feature = "prod") {
        pilotcodi_ee_root().join("jobs")
    } else {
        pilotcodi_ee_root().join("dev-jobs")
    }
}
