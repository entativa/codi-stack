use std::sync::Arc;

use pilotcodi_common::config::ModelConfig;
use pilotcodi_inference::Embedding;

use super::model;

pub async fn create(config: &ModelConfig) -> Arc<dyn Embedding> {
    model::load_embedding(config).await
}
