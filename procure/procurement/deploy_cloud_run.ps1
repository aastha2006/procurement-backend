$IMAGE_TAG = "gcr.io/gen-lang-client-0407210458/procurement-backend:v5"
$SERVICE_NAME = "procurement-backend"
$REGION = "asia-south1"

Write-Host "Deploying with Tag: $IMAGE_TAG"

# Build the image using Cloud Build
gcloud builds submit --tag $IMAGE_TAG .

if ($?) {
    Write-Host "Deploying to Cloud Run..."
    gcloud run deploy $SERVICE_NAME `
        --image $IMAGE_TAG `
        --platform managed `
        --region $REGION `
        --allow-unauthenticated `
        --quiet
} else {
    Write-Host "Build failed. Aborting deployment."
}
