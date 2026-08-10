Write-Host "=== Building jars ==="
./gradlew bootJar

Write-Host "=== Loading images into K8s ==="
kubectl delete pod -n dev -l app=gateway --force --grace-period=0 2>$null
kubectl delete pod -n dev -l app=auth-service --force --grace-period=0 2>$null
kubectl delete pod -n dev -l app=app-service --force --grace-period=0 2>$null
kubectl delete pod -n dev -l app=search-service --force --grace-period=0 2>$null

Write-Host "=== Deploying to K8s ==="
kubectl apply -f k8s/dev/namespace.yaml
kubectl apply -f k8s/dev/

Write-Host "=== Waiting for pods ==="
Start-Sleep -Seconds 15

kubectl get pods -n dev
Write-Host "=== Done ==="