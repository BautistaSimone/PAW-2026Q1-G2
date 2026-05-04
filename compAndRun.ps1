$ErrorActionPreference = "Stop"

$repoRoot = Split-Path -Parent $MyInvocation.MyCommand.Path
Set-Location $repoRoot

$listener = Get-NetTCPConnection -LocalPort 8000 -State Listen -ErrorAction SilentlyContinue |
    Select-Object -First 1

if ($listener) {
    Write-Host "Cerrando proceso previo en puerto 8000 (PID $($listener.OwningProcess))..."
    Stop-Process -Id $listener.OwningProcess -Force
    Start-Sleep -Seconds 2
}

Write-Host "Compilando e instalando modulos..."
mvn clean install -DskipTests

if ($LASTEXITCODE -ne 0) {
    exit $LASTEXITCODE
}

Write-Host "Levantando Jetty en http://localhost:8000/ ..."
mvn -f webapp/pom.xml org.eclipse.jetty:jetty-maven-plugin:9.4.58.v20250814:run
