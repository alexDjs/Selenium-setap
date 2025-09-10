param(
    [string]$RepoUrl,
    [string]$Branch = 'main',
    [string]$CommitMessage = 'Initial commit: add project and CI workflows',
    [switch]$ForceRemote
)

if (-not $RepoUrl) {
    Write-Host "Usage: .\push-to-github.ps1 -RepoUrl <git-url> [-Branch main] [-CommitMessage 'msg'] [-ForceRemote]"
    exit 1
}

# Ensure git is available
if (-not (Get-Command git -ErrorAction SilentlyContinue)) {
    Write-Error "git is not installed or not in PATH. Install Git and retry."
    exit 2
}

# Initialize repo if needed
if (-not (Test-Path .git)) {
    git init
}

# Add all files
git add .

# Commit (ignore if nothing to commit)
$commitExit = 0
try {
    git commit -m "$CommitMessage"
} catch {
    Write-Host "No changes to commit or commit failed: $_"
}

# Configure remote
$existing = git remote get-url origin 2>$null
if ($LASTEXITCODE -ne 0 -or $ForceRemote) {
    if ($LASTEXITCODE -ne 0) {
        git remote add origin $RepoUrl
    } else {
        git remote remove origin
        git remote add origin $RepoUrl
    }
} else {
    if ($existing -ne $RepoUrl) {
        Write-Host "Existing origin ($existing) differs from provided URL. Use -ForceRemote to overwrite."
        exit 3
    }
}

# Ensure branch name and push
git branch -M $Branch

Write-Host "Pushing to $RepoUrl (branch $Branch)..."
if (-not (git push -u origin $Branch)) {
    Write-Error "git push failed. Check remote URL, credentials and network access."
    exit 4
}

Write-Host "Push completed successfully. Open your GitHub repository to view Actions runs."
