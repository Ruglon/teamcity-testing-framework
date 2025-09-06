# setup-selenoid.ps1 - Automates Selenoid setup without running it
# Run in PowerShell from project folder: .\setup-selenoid.ps1

# Step 1: Check if Docker is installed
Write-Host "Checking if Docker is installed..."
$dockerVersion = docker --version 2>$null
if (-not $dockerVersion) {
    Write-Host "Docker not found! Please install Docker Desktop from https://www.docker.com/products/docker-desktop"
    Write-Host "After installation, restart PowerShell and re-run this script."
    Write-Host "Also, add your user to docker-users group: net localgroup 'docker-users' '$env:USERNAME' /add (run as admin)."
    exit
} else {
    Write-Host "Docker found: $dockerVersion"
}

# Step 2: Download Configuration Manager (CM)
Write-Host "Downloading CM..."
Invoke-WebRequest -Uri "https://github.com/aerokube/cm/releases/latest/download/cm_windows_amd64.exe" -OutFile "cm.exe"
if (Test-Path "cm.exe") {
    Write-Host "CM downloaded successfully."
} else {
    Write-Host "Failed to download CM. Check internet or try manual download from https://github.com/aerokube/cm/releases/latest"
    exit
}

# Step 3: Create config folder and browsers.json
Write-Host "Creating config folder and browsers.json..."
New-Item -ItemType Directory -Force -Path "config" | Out-Null
$browsersJson = @'
{
  "chrome": {
    "default": "latest",
    "versions": {
      "latest": {
        "image": "selenoid/chrome:128.0",
        "port": "4444",
        "path": "/"
      }
    }
  },
  "firefox": {
    "default": "latest",
    "versions": {
      "latest": {
        "image": "selenoid/firefox:110.0",
        "port": "4444",
        "path": "/wd/hub"
      }
    }
  }
}
'@
Set-Content -Path "config\browsers.json" -Value $browsersJson
if (Test-Path "config\browsers.json") {
    Write-Host "browsers.json created. Versions are current as of August 18, 2025. Check https://aerokube.com/images/latest/ for updates."
} else {
    Write-Host "Failed to create browsers.json."
    exit
}

# Step 4: Pull Docker images for browsers (without running)
Write-Host "Pulling Docker images..."
docker pull selenoid/chrome:128.0
docker pull selenoid/firefox:110.0
docker pull aerokube/selenoid:latest
docker pull selenoid/video-recorder:latest-release
docker pull aerokube/selenoid-ui:latest

Write-Host "Images pulled. Check with 'docker images'."

Write-Host "Setup complete! To run Selenoid: .\cm.exe selenoid start --browsers-json config\browsers.json --vnc"
Write-Host "To run UI: docker run -d --name selenoid-ui -p 8080:8080 aerokube/selenoid-ui --selenoid-uri http://host.docker.internal:4444"