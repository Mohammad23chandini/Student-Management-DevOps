# Teardown Student Management System from Kubernetes
Write-Host "Tearing down Student Management System from Kubernetes..." -ForegroundColor Yellow
kubectl delete -f k8s/07-app-service.yaml --ignore-not-found
kubectl delete -f k8s/06-app-deployment.yaml --ignore-not-found
kubectl delete -f k8s/05-mysql-service.yaml --ignore-not-found
kubectl delete -f k8s/04-mysql-deployment.yaml --ignore-not-found
kubectl delete -f k8s/03-mysql-pvc.yaml --ignore-not-found
kubectl delete -f k8s/02-app-configmap.yaml --ignore-not-found
kubectl delete -f k8s/01-mysql-secret.yaml --ignore-not-found
kubectl delete -f k8s/00-namespace.yaml --ignore-not-found
Write-Host "Teardown complete." -ForegroundColor Green
