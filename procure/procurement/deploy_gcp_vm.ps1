$ErrorActionPreference = "Stop"

# Configuration
$PROJECT_ID = "procuredemo-123" # REPLACE THIS WITH YOUR PROJECT ID
$INSTANCE_NAME = "procuredemo-vm"
$ZONE = "us-central1-a" # Free tier eligible zone
$MACHINE_TYPE = "e2-micro" # Free tier eligible machine type
$IMAGE_FAMILY = "ubuntu-2204-lts"
$IMAGE_PROJECT = "ubuntu-os-cloud"
$FIREWALL_RULE = "allow-procuredemo-8080"

Write-Host "=== GCP Backend Deployment Script ===" -ForegroundColor Cyan
Write-Host "This script will deploy your app to a $MACHINE_TYPE VM in $ZONE."

# Check for gcloud
if (-not (Get-Command "gcloud" -ErrorAction SilentlyContinue)) {
    Write-Error "gcloud CLI is not installed or not in PATH. Please install Google Cloud SDK."
}

# 1. Archive Source Code
Write-Host "`n[1/6] Zipping source code..." -ForegroundColor Yellow
$filesToZip = @("src", "pom.xml", "Dockerfile")
Compress-Archive -Path $filesToZip -DestinationPath "deploy.zip" -Force
Write-Host "Source code zipped to deploy.zip" -ForegroundColor Green

# 2. Configure Project (Optional prompt)
# Uncomment to force login check
# gcloud auth login
# gcloud config set project $PROJECT_ID

# 3. Create Firewall Rule
Write-Host "`n[2/6] Creating/Updating Firewall Rule for port 8080..." -ForegroundColor Yellow
gcloud compute firewall-rules create $FIREWALL_RULE `
    --allow tcp:8080 `
    --description "Allow incoming traffic on 8080 for ProcureDemo" `
    --target-tags "http-server" `
    --quiet
# Ignore error if exists

# 4. Create VM Instance
Write-Host "`n[3/6] Creating VM Instance ($INSTANCE_NAME)..." -ForegroundColor Yellow
# Check if exists
$vmExists = gcloud compute instances list --filter="name=($INSTANCE_NAME)" --format="value(name)"
if ($vmExists) {
    Write-Host "VM $INSTANCE_NAME already exists. Skipping creation." -ForegroundColor Gray
} else {
    gcloud compute instances create $INSTANCE_NAME `
        --zone=$ZONE `
        --machine-type=$MACHINE_TYPE `
        --image-family=$IMAGE_FAMILY `
        --image-project=$IMAGE_PROJECT `
        --tags=http-server `
        --scopes=cloud-platform `
        --quiet
}

Write-Host "Waiting for VM to be ready..." 
Start-Sleep -Seconds 30

# 5. SCP Source Code
Write-Host "`n[4/6] Uploading code to VM..." -ForegroundColor Yellow
gcloud compute scp "deploy.zip" "${INSTANCE_NAME}:~/deploy.zip" --zone=$ZONE --quiet

# 6. Remote Setup & Deploy (Docker install + Build + Run)
Write-Host "`n[5/6] running remote setup (Docker install, Build, Run)..." -ForegroundColor Yellow
# We use a HERE-STRING for the remote script
$remoteScript = @'
#!/bin/bash
set -e

echo "--- Updating System ---"
sudo apt-get update -y

echo "--- Installing Docker ---"
if ! command -v docker &> /dev/null; then
    sudo apt-get install -y docker.io unzip
    sudo usermod -aG docker $USER
else
    echo "Docker already installed"
fi

echo "--- Setting up Swap (2GB) to prevent OOM build ---"
if [ ! -f /swapfile ]; then
    sudo fallocate -l 2G /swapfile
    sudo chmod 600 /swapfile
    sudo mkswap /swapfile
    sudo swapon /swapfile
    echo '/swapfile none swap sw 0 0' | sudo tee -a /etc/fstab
fi

echo "--- Preparing App ---"
rm -rf app
mkdir -p app && cd app
unzip -o ~/deploy.zip

echo "--- Building Docker Image ---"
# Check if container is running and stop it
if [ "$(sudo docker ps -q -f name=procuredemo)" ]; then
    sudo docker stop procuredemo
    sudo docker rm procuredemo
fi

sudo docker build -t procuredemo .

echo "--- Running Container ---"
sudo docker run -d -p 8080:8080 --restart always --name procuredemo procuredemo
'@

# Save remote script locally just to pipe it (or send it via command)
# Using direct command execution via SSH
gcloud compute ssh $INSTANCE_NAME --zone=$ZONE --command "$remoteScript" --quiet

# 7. Get IP
Write-Host "`n[6/6] fetching details..." -ForegroundColor Yellow
$ip = gcloud compute instances describe $INSTANCE_NAME --zone=$ZONE --format='get(networkInterfaces[0].accessConfigs[0].natIP)'

Write-Host "`n=== DEPLOYMENT COMPLETE ===" -ForegroundColor Green
Write-Host "VM Public IP: $ip" -ForegroundColor Cyan
Write-Host "App URL: http://$($ip):8080" -ForegroundColor Cyan
Write-Host "Action Required: Whitelist this IP ($ip) in your AWS RDS Security Group." -ForegroundColor Magenta
