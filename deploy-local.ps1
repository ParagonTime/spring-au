#Requires -Version 5.1
$ErrorActionPreference = "Stop"

$KindClusterName = "local-dev"
$KubeContext = "kind-local-dev"

Write-Host "=== Check kind cluster ===" -ForegroundColor Cyan
$clusters = & kind get clusters 2>&1 | ForEach-Object { "$_" }
if ($clusters -notcontains $KindClusterName) {
    Write-Host "Cluster '$KindClusterName' not found. Create it first:" -ForegroundColor Red
    Write-Host "  cd D:\k8s"
    Write-Host "  .\create-cluster.ps1"
    Write-Host "  .\install-ingress.ps1"
    exit 1
}

kubectl config use-context $KubeContext | Out-Null

Write-Host "=== Build jars ===" -ForegroundColor Cyan
./gradlew bootJar

Write-Host "=== Build images ===" -ForegroundColor Cyan
docker build -t gateway:dev ./gateway
docker build -t auth:dev ./auth
docker build -t app:dev ./app
docker build -t search:dev ./search

Write-Host "=== Load images into kind ===" -ForegroundColor Cyan
kind load docker-image gateway:dev --name $KindClusterName
kind load docker-image auth:dev --name $KindClusterName
kind load docker-image app:dev --name $KindClusterName
kind load docker-image search:dev --name $KindClusterName

Write-Host "=== Deploy manifests ===" -ForegroundColor Cyan
kubectl apply -f k8s/dev/namespace.yaml
kubectl apply -f k8s/dev/

Write-Host "=== Restart app deployments ===" -ForegroundColor Cyan
kubectl rollout restart deployment/gateway deployment/auth-service deployment/app-service deployment/search-service -n dev
kubectl rollout status deployment/gateway -n dev --timeout=180s
kubectl rollout status deployment/auth-service -n dev --timeout=180s
kubectl rollout status deployment/app-service -n dev --timeout=180s
kubectl rollout status deployment/search-service -n dev --timeout=180s

Write-Host ""
kubectl get pods -n dev
Write-Host ""
Write-Host "Ready: http://localhost:30080" -ForegroundColor Green
