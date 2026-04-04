#!/usr/bin/env bash

set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

cd "$SCRIPT_DIR"

echo "Instalando modulos necesarios..."
mvn clean install -DskipTests

echo "Levantando Jetty en http://localhost:8000/ ..."
mvn -f webapp/pom.xml org.eclipse.jetty:jetty-maven-plugin:9.4.58.v20250814:run
