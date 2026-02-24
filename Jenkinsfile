pipeline {
    agent {
        docker {
            image 'maven:3.9-eclipse-temurin-21-alpine'
            args '-v $HOME/.m2:/root/.m2'
        }
    }

    stages {
        stage('Checkout Code') {
            steps {
                echo '=================================='
                echo 'Checking out code from repository'
                echo '=================================='
                checkout scm
            }
        }

        stage('Verify Environment') {
            steps {
                echo '=================================='
                echo 'Verifying build environment'
                echo '=================================='
                sh '''
                    echo "Java Version:"
                    java -version
                    
                    echo "\nMaven Version:"
                    mvn -version
                    
                    echo "\nWorkspace Directory:"
                    pwd
                    
                    echo "\nProject Structure:"
                    ls -la
                    
                    echo "\nMicroservices:"
                    ls -d imbank-*/
                '''
            }
        }

        stage('Build Config Server') {
            steps {
                echo '=================================='
                echo 'Building Config Server (Port 8888)'
                echo '=================================='
                sh '''
                    cd imbank-config-server
                    mvn clean package -DskipTests
                    echo "\nBuild artifacts:"
                    ls -lh target/*.jar | grep -v ".original"
                '''
            }
        }

        stage('Build Eureka Server') {
            steps {
                echo '=================================='
                echo 'Building Eureka Server (Port 8761)'
                echo '=================================='
                sh '''
                    cd imbank-eureka-server
                    mvn clean package -DskipTests
                    echo "\nBuild artifacts:"
                    ls -lh target/*.jar | grep -v ".original"
                '''
            }
        }

        stage('Build API Gateway') {
            steps {
                echo '=================================='
                echo 'Building API Gateway (Port 8080)'
                echo '=================================='
                sh '''
                    cd imbank-api-gateway
                    mvn clean package -DskipTests
                    echo "\nBuild artifacts:"
                    ls -lh target/*.jar | grep -v ".original"
                '''
            }
        }

        stage('Build Auth Service') {
            steps {
                echo '=================================='
                echo 'Building Auth Service (Port 8081)'
                echo '=================================='
                sh '''
                    cd imbank-auth-service
                    mvn clean package -DskipTests
                    echo "\nBuild artifacts:"
                    ls -lh target/*.jar | grep -v ".original"
                '''
            }
        }

        stage('Build Employee Service') {
            steps {
                echo '=================================='
                echo 'Building Employee Service (Port 8082)'
                echo '=================================='
                sh '''
                    cd imbank-employee-service
                    mvn clean package -DskipTests
                    echo "\nBuild artifacts:"
                    ls -lh target/*.jar | grep -v ".original"
                '''
            }
        }

        stage('Build Department Service') {
            steps {
                echo '=================================='
                echo 'Building Department Service (Port 8083)'
                echo '=================================='
                sh '''
                    cd imbank-department-service
                    mvn clean package -DskipTests
                    echo "\nBuild artifacts:"
                    ls -lh target/*.jar | grep -v ".original"
                '''
            }
        }

        stage('Verify All Builds') {
            steps {
                echo '=================================='
                echo 'Verifying all build artifacts'
                echo '=================================='
                sh '''
                    echo "All JAR files built:"
                    find . -name "*.jar" -path "*/target/*" -not -name "*-sources.jar" -not -name "*-javadoc.jar" | grep -v ".jar.original"
                    
                    echo "\nBuild artifact count:"
                    find . -name "*.jar" -path "*/target/*" -not -name "*-sources.jar" -not -name "*-javadoc.jar" | grep -v ".jar.original" | wc -l
                '''
            }
        }

        stage('Build Summary') {
            steps {
                echo '=================================='
                echo 'BUILD SUMMARY'
                echo '=================================='
                echo 'Config Server     - BUILT'
                echo 'Eureka Server     - BUILT'
                echo 'API Gateway       - BUILT'
                echo 'Auth Service      - BUILT'
                echo 'Employee Service  - BUILT'
                echo 'Department Service- BUILT'
                echo '=================================='
                echo 'All 6 microservices built successfully'
                echo '=================================='
            }
        }
    }

    post {
        success {
            echo '=================================='
            echo 'PIPELINE COMPLETED SUCCESSFULLY'
            echo '=================================='
            echo ''
            echo 'Build artifacts ready for:'
            echo '  - Docker image creation'
            echo '  - Container deployment'

            echo '=================================='
        }

        failure {
            echo '=================================='
            echo 'PIPELINE FAILED'
            echo '=================================='
            echo 'Check the logs above for error details'
            echo '=================================='
        }

        always {
            echo '=================================='
            echo 'Pipeline execution completed'
            echo '=================================='
        }
    }
}
