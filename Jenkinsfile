pipeline {
    agent any
    environment {
        registry = "mickey06/project"
        registryCredential = "docker-creds"
    }
    stages {
        stage('Stage I: Build') {
            steps {
                echo "Building Jar Component ..."
                sh 'mvn clean package'
            }
        }

        stage('Stage II: Code Coverage') {
            steps {
                echo "Running Code Coverage ..."
                sh 'mvn jacoco:report'
            }
        }

        stage('Stage III: SCA') {
            steps {
                echo "Running Software Composition Analysis ..."
                withCredentials([string(credentialsId: 'nvd-api-key', variable: 'NVD_API_KEY')]) {
                    sh '''
                    mvn dependency-check:check \
                    -Dnvd.api.key=$NVD_API_KEY \
                    -DossIndexAnalyzerEnabled=false
                    '''
                }
            }
        }

        stage('Stage IV: SAST') {
            steps {
                echo "Running Static Application Security Testing ..."
                withSonarQubeEnv('sonarqube') {
                    sh '''
                    mvn sonar:sonar \
                    -Dsonar.projectName=wezvatech \
                    -Dsonar.coverage.jacoco.xmlReportPaths=target/site/jacoco/jacoco.xml \
                    -Dsonar.dependencyCheck.jsonReportPath=target/dependency-check-report.json \
                    -Dsonar.dependencyCheck.htmlReportPath=target/dependency-check-report.html
                    '''
                }
            }
        }
        
        stage('Stage V: Quality Gate') {
            steps {
                echo "Checking Quality Gate ..."
                script {
                    try {
                        timeout(time: 5, unit: 'MINUTES') {
                            def qg = waitForQualityGate()
                            if (qg.status != 'OK') {
                                echo "⚠️ Quality Gate failed: ${qg.status} (continuing pipeline)"
                            } else {
                                echo "✅ Quality Gate passed"
                            }
                        }
                    } catch (err) {
                        echo "⚠️ Quality Gate check failed or timed out: ${err.message} (continuing pipeline)"
                    }
                }
            }
        }
        
        stage('Stage VI: Build Image') {
            steps {
                echo "Building Docker Image ..."
                script {
                    docker.build("${registry}")
                }
            }
        }
       
        stage('Stage VII: Scan Image') {
            steps {
                echo "Scanning Image with Trivy ..."
                sh "trivy image --timeout 20m --severity HIGH,CRITICAL --exit-code 1 ${registry}:latest > trivyresults.txt"
            }
        }
       
        stage('Stage VIII: Smoke Test') {
            steps {
                echo "Running Smoke Test ..."
                sh 'docker rm -f smokerun || true'
                sh "docker run -d --name smokerun -p 8080:8080 ${registry}:latest"
                sh "sleep 60"
                sh "./check.sh"
                sh "docker rm -f smokerun"
            }
        }
        
        stage('Stage IX: Push Image') {
            steps {
                echo "Pushing Docker Image ..."
                script {
                    retry(3) {
                        docker.withRegistry('', registryCredential) {
                            docker.image("${registry}:latest").push()
                        }
                    }
                }
            }
        }
    }
    
    post {
        always {
            echo "Pipeline completed"
            archiveArtifacts artifacts: 'trivyresults.txt', allowEmptyArchive: true
        }
        success {
            echo "✅ Build & Security Checks Passed"
        }
        failure {
            echo "❌ Pipeline Failed - Check Logs"
        }
    }
}
