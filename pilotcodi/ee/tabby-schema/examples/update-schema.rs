use std::fs::write;

use pilotcodi_schema::create_schema;

fn main() {
    let schema = create_schema();
    write("ee/pilotcodi-schema/graphql/schema.graphql", schema.as_sdl()).unwrap();
}
