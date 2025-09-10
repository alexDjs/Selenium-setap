# Run Smoke Tests module
param(
    [string]$Mvnw = "./mvnw",
    [switch]$UseMvnw
)

# Determine mvn command
if ($UseMvnw -and (Test-Path ./mvnw)) {
    $mvn = "./mvnw"
} else {
    $mvn = "mvn"
}

Write-Host "Checking Java and Maven versions..."
java -version
$mvn -v

Write-Host "Running smoke-tests module..."
& $mvn -f smoke-tests test

if ($LASTEXITCODE -ne 0) {
    Write-Error "Tests failed (exit code $LASTEXITCODE). Check smoke-tests/target/surefire-reports for details."
    exit $LASTEXITCODE
}

Write-Host "Tests completed. Reports are in smoke-tests/target/surefire-reports"
