#Requires -Version 5.1
$ErrorActionPreference = "Stop"

Write-Host "=== Verify required tools ===" -ForegroundColor Cyan
java -version
docker version
kubectl version --client
kind version

Write-Host "=== Verify kind cluster ===" -ForegroundColor Cyan
kubectl config use-context kind-local-dev
kubectl cluster-info
kubectl get nodes

Write-Host "=== Build all modules ===" -ForegroundColor Cyan
.\gradlew.bat clean bootJar

Write-Host "=== Build Docker images ===" -ForegroundColor Cyan
docker build --no-cache -t gateway:dev ./gateway
docker build --no-cache -t auth:dev ./auth
docker build --no-cache -t app:dev ./app
docker build --no-cache -t search:dev ./search

Write-Host "=== Load images into kind ===" -ForegroundColor Cyan
kind load docker-image gateway:dev --name local-dev
kind load docker-image auth:dev --name local-dev
kind load docker-image app:dev --name local-dev
kind load docker-image search:dev --name local-dev

Write-Host "=== Deploy namespace ===" -ForegroundColor Cyan
kubectl apply -f k8s/dev/namespace.yaml

Write-Host "=== Deploy application configuration ===" -ForegroundColor Cyan
kubectl apply -f k8s/dev/configmap.yaml
kubectl apply -f k8s/dev/secrets.yaml

Write-Host "=== Deploy infrastructure ===" -ForegroundColor Cyan
kubectl apply -f k8s/dev/postgres.yaml
kubectl apply -f k8s/dev/kafka.yaml
kubectl apply -f k8s/dev/keycloak.yaml

Write-Host "=== Deploy Elasticsearch ===" -ForegroundColor Cyan
kubectl apply -f k8s/dev/elasticsearch.yaml

kubectl rollout status deployment/elasticsearch -n dev --timeout=10m

kubectl wait --for=condition=Ready pod -l app=elasticsearch -n dev --timeout=10m

Write-Host "=== Deploy OTel Collector ===" -ForegroundColor Cyan
kubectl apply -f k8s/dev/otel-collector.yaml

kubectl rollout status daemonset/otel-collector -n dev --timeout=5m

Write-Host "=== Deploy business applications ===" -ForegroundColor Cyan
kubectl apply -f k8s/dev/gateway.yaml
kubectl apply -f k8s/dev/app.yaml
kubectl apply -f k8s/dev/search.yaml
kubectl apply -f k8s/dev/auth.yaml

Write-Host "=== Wait for business applications ===" -ForegroundColor Cyan
kubectl rollout status deployment/gateway -n dev --timeout=180s
kubectl rollout status deployment/app-service -n dev --timeout=180s
kubectl rollout status deployment/search-service -n dev --timeout=180s
kubectl rollout status deployment/auth-service -n dev --timeout=180s

Write-Host "=== Deploy ingress ===" -ForegroundColor Cyan
kubectl apply -f k8s/dev/gateway-ingress.yaml

Write-Host "=== Show cluster state ===" -ForegroundColor Cyan
Write-Host "=== Pods ==="
kubectl get pods -n dev -o wide

Write-Host "=== Services ==="
kubectl get svc -n dev

Write-Host "=== Deployments ==="
kubectl get deployments -n dev

Write-Host "=== DaemonSets ==="
kubectl get daemonsets -n dev

Write-Host "=== Elasticsearch ==="
kubectl get deployment elasticsearch -n dev

Write-Host "=== Ingress ==="
kubectl get ingress -n dev

Write-Host "=== Recent Collector logs ==="
kubectl logs -n dev -l app=otel-collector --since=2m --tail=30