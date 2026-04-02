#!/usr/bin/env bash

set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

cd "$SCRIPT_DIR"

echo "Compilando modulos necesarios..."
mvn -pl webapp -am compile

echo "Levantando Jetty en http://localhost:8000/ ..."
mvn -pl webapp -am jetty:run
