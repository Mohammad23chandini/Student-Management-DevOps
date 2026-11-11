# Deploy Student Management System to Kubernetes
Write-Host "==========================================================" -ForegroundColor Cyan
Write-Host "  Deploying Student Management System to Kubernetes (k8s)" -ForegroundColor Cyan
Write-Host "==========================================================" -ForegroundColor Cyan

# 1. Verify kubectl
if (-not (Get-Command kubectl -ErrorAction SilentlyContinue)) {
    Write-Error "kubectl is not installed or not in PATH."
    exit 1
}

# 2. Tag local image if demo-app exists
Write-Host "`n[1/4] Ensuring container images are ready..." -ForegroundColor Yellow
docker tag demo-app:latest student-management-app:latest 2>$null
docker tag demo-app:latest student-management:v1.0 2>$null

# 3. Apply Kubernetes Manifests
Write-Host "`n[2/4] Applying Kubernetes manifests in order..." -ForegroundColor Yellow
kubectl apply -f k8s/00-namespace.yaml
kubectl apply -f k8s/01-mysql-secret.yaml
kubectl apply -f k8s/02-app-configmap.yaml
kubectl apply -f k8s/03-mysql-pvc.yaml
kubectl apply -f k8s/04-mysql-deployment.yaml
kubectl apply -f k8s/05-mysql-service.yaml
kubectl apply -f k8s/06-app-deployment.yaml
kubectl apply -f k8s/07-app-service.yaml

# 4. Wait for Deployments
Write-Host "`n[3/4] Waiting for database pod to initialize..." -ForegroundColor Yellow
kubectl rollout status deployment/mysql-deployment -n student-management --timeout=180s

Write-Host "`n[4/4] Waiting for application pods to achieve readiness..." -ForegroundColor Yellow
kubectl rollout status deployment/app-deployment -n student-management --timeout=180s

# 5. Display Status
Write-Host "`n==========================================================" -ForegroundColor Green
Write-Host "  Kubernetes Deployment Complete!" -ForegroundColor Green
Write-Host "==========================================================" -ForegroundColor Green

Write-Host "`n[Pods]" -ForegroundColor Cyan
kubectl get pods -n student-management -o wide

Write-Host "`n[Services]" -ForegroundColor Cyan
kubectl get svc -n student-management

Write-Host "`nAccess the macOS Dashboard at:" -ForegroundColor Green
Write-Host "  - Direct URL: http://localhost:8081" -ForegroundColor Yellow
Write-Host "  - NodePort:   http://localhost:30081" -ForegroundColor Yellow
Write-Host "`nUseful Commands:" -ForegroundColor Gray
Write-Host "  kubectl logs -f -l app=student-management-app -n student-management" -ForegroundColor Gray
Write-Host "  kubectl get all -n student-management" -ForegroundColor Gray
