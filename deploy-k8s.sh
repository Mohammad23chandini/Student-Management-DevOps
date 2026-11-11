#!/usr/bin/env bash
set -e

echo "=========================================================="
echo "  Deploying Student Management System to Kubernetes (k8s)"
echo "=========================================================="

# 1. Verify kubectl
command -v kubectl >/dev/null 2>&1 || { echo "kubectl not found in PATH"; exit 1; }

# 2. Tag local image
echo -e "\n[1/4] Ensuring container images are ready..."
docker tag demo-app:latest student-management-app:latest 2>/dev/null || true
docker tag demo-app:latest student-management:v1.0 2>/dev/null || true

# 3. Apply Kubernetes Manifests
echo -e "\n[2/4] Applying Kubernetes manifests..."
kubectl apply -f k8s/00-namespace.yaml
kubectl apply -f k8s/01-mysql-secret.yaml
kubectl apply -f k8s/02-app-configmap.yaml
kubectl apply -f k8s/03-mysql-pvc.yaml
kubectl apply -f k8s/04-mysql-deployment.yaml
kubectl apply -f k8s/05-mysql-service.yaml
kubectl apply -f k8s/06-app-deployment.yaml
kubectl apply -f k8s/07-app-service.yaml

# 4. Wait for Rollouts
echo -e "\n[3/4] Waiting for database pod..."
kubectl rollout status deployment/mysql-deployment -n student-management --timeout=180s

echo -e "\n[4/4] Waiting for application pods..."
kubectl rollout status deployment/app-deployment -n student-management --timeout=180s

# 5. Display Status
echo -e "\n=========================================================="
echo "  Kubernetes Deployment Complete!"
echo "=========================================================="
kubectl get pods -n student-management -o wide
echo ""
kubectl get svc -n student-management
echo -e "\nApplication accessible at: http://localhost:8081 or http://localhost:30081"
