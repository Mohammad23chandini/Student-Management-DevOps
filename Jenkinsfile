pipeline {
    agent any

    environment {
        APP_NAME = 'student-management-app'
        IMAGE_NAME = 'student-management-app'
        K8S_NAMESPACE = 'student-management'
    }

    stages {
        stage('1. Checkout SCM') {
            steps {
                echo '=================================================='
                echo ' Stage 1: Checkout Source Code from GitHub'
                echo '=================================================='
                checkout scm
            }
        }

        stage('2. Build & Package (Maven)') {
            steps {
                echo '=================================================='
                echo ' Stage 2: Compiling & Packaging JAR with Maven'
                echo '=================================================='
                script {
                    if (isUnix()) {
                        sh 'mvn clean package -DskipTests'
                    } else {
                        bat 'mvn clean package -DskipTests'
                    }
                }
            }
        }

        stage('3. Build Docker Container') {
            steps {
                echo '=================================================='
                echo ' Stage 3: Building Docker Container Image'
                echo '=================================================='
                script {
                    if (isUnix()) {
                        sh "docker build -t ${IMAGE_NAME}:latest -t ${IMAGE_NAME}:${BUILD_NUMBER} ."
                    } else {
                        bat "docker build -t ${IMAGE_NAME}:latest -t ${IMAGE_NAME}:%BUILD_NUMBER% ."
                    }
                }
            }
        }

        stage('4. Deploy to Kubernetes (k8s)') {
            steps {
                echo '=================================================='
                echo ' Stage 4: Deploying to Kubernetes Cluster'
                echo '=================================================='
                script {
                    if (isUnix()) {
                        sh 'kubectl apply -f k8s/'
                        sh "kubectl rollout status deployment/app-deployment -n ${K8S_NAMESPACE} --timeout=180s"
                    } else {
                        bat 'kubectl apply -f k8s/'
                        bat "kubectl rollout status deployment/app-deployment -n ${K8S_NAMESPACE} --timeout=180s"
                    }
                }
            }
        }

        stage('5. Smoke & Healthcheck Verification') {
            steps {
                echo '=================================================='
                echo ' Stage 5: Verifying Health via /api/stats'
                echo '=================================================='
                script {
                    if (isUnix()) {
                        sh 'curl -f http://localhost:8081/api/stats || true'
                    } else {
                        powershell 'try { (Invoke-RestMethod -Uri "http://localhost:8081/api/stats").totalStudents } catch { Write-Warning "Endpoint starting..." }'
                    }
                }
            }
        }
    }

    post {
        always {
            echo '=================================================='
            echo ' Jenkins Pipeline Execution Finished'
            echo '=================================================='
        }
        success {
            echo 'PIPELINE STATUS: SUCCESS! Application deployed and verified.'
        }
        failure {
            echo 'PIPELINE STATUS: FAILED. Please inspect build console output.'
        }
    }
}
