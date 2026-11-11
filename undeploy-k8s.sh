#!/usr/bin/env bash
echo "Tearing down Student Management System from Kubernetes..."
kubectl delete -f k8s/ --ignore-not-found
kubectl delete namespace student-management --ignore-not-found
echo "Teardown complete."
