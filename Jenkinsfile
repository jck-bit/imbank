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
                echo 'Checking out code from repository...'
                echo '=================================='
                checkout scm
            }
        }

        stage('🔍 Verify Environment') {
            steps {
                echo '=================================='
                echo '🔍 Verifying build environment...'
                echo '=================================='
                sh '''
                    echo "Current Working Directory:"
                    pwd

                    echo "\nJava Version:"
                    java -version

                    echo "\nMaven Version:"
                    mvn -version

                    echo "\nProject Directory Contents:"
                    ls -la
                '''
            }
        }

        stage('📦 Build Microservices') {
            steps {
                echo '=================================='
                echo '📦 Building all 6 microservices...'
                echo '=================================='
                echo 'Services to build:'
                echo '  1. Config Server (Port 8888)'
                echo '  2. Eureka Server (Port 8761)'
                echo '  3. API Gateway (Port 8080)'
                echo '  4. Auth Service (Port 8081)'
                echo '  5. Employee Service (Port 8082)'
                echo '  6. Department Service (Port 8083)'
                echo '=================================='

                sh '''
                    echo "Building Config Server..."
                    cd imbank-config-server && mvn clean package -DskipTests

                    echo "\nBuilding Eureka Server..."
                    cd ../imbank-eureka-server && mvn clean package -DskipTests

                    echo "\nBuilding API Gateway..."
                    cd ../imbank-api-gateway && mvn clean package -DskipTests

                    echo "\nBuilding Auth Service..."
                    cd ../imbank-auth-service && mvn clean package -DskipTests

                    echo "\nBuilding Employee Service..."
                    cd ../imbank-employee-service && mvn clean package -DskipTests

                    echo "\nBuilding Department Service..."
                    cd ../imbank-department-service && mvn clean package -DskipTests
                '''
            }
        }

        stage('🐳 Ready for Docker') {
            steps {
                echo '=================================='
                echo '🐳 Build artifacts ready for Docker...'
                echo '=================================='
                echo 'JAR files can now be packaged into Docker images'
                echo '=================================='

                sh '''
                    echo "✅ All JAR files built successfully!"
                    echo ""
                    echo "Built artifacts:"
                    find . -name "*.jar" -path "*/target/*" -not -name "*-sources.jar" -not -name "*-javadoc.jar" | grep -v ".jar.original"
                '''
            }
        }

        stage('✅ Verify Build Artifacts') {
            steps {
                echo '=================================='
                echo '✅ Verifying all build artifacts...'
                echo '=================================='

                sh '''
                    echo "Checking Config Server JAR..."
                    ls -lh imbank-config-server/target/*.jar | grep -v ".original" || exit 1

                    echo "\nChecking Eureka Server JAR..."
                    ls -lh imbank-eureka-server/target/*.jar | grep -v ".original" || exit 1

                    echo "\nChecking API Gateway JAR..."
                    ls -lh imbank-api-gateway/target/*.jar | grep -v ".original" || exit 1

                    echo "\nChecking Auth Service JAR..."
                    ls -lh imbank-auth-service/target/*.jar | grep -v ".original" || exit 1

                    echo "\nChecking Employee Service JAR..."
                    ls -lh imbank-employee-service/target/*.jar | grep -v ".original" || exit 1

                    echo "\nChecking Department Service JAR..."
                    ls -lh imbank-department-service/target/*.jar | grep -v ".original" || exit 1

                    echo "\n✅ All 6 microservices built successfully!"
                '''
            }
        }

        stage('📊 Build Summary') {
            steps {
                echo '=================================='
                echo '📊 BUILD SUMMARY'
                echo '=================================='
                echo '✅ Config Server - BUILT'
                echo '✅ Eureka Server - BUILT'
                echo '✅ API Gateway - BUILT'
                echo '✅ Auth Service - BUILT'
                echo '✅ Employee Service - BUILT'
                echo '✅ Department Service - BUILT'
                echo '=================================='
                echo '🎉 All microservices built successfully!'
                echo '=================================='
            }
        }
    }

    post {
        success {
            echo '=================================='
            echo '🎉 PIPELINE COMPLETED SUCCESSFULLY!'
            echo '=================================='
            echo 'Next steps:'
            echo '  1. Run: docker-compose build (to build Docker images)'
            echo '  2. Run: docker-compose up (to start services)'
            echo '  3. Access API Gateway: http://localhost:8080'
            echo '=================================='
        }

        failure {
            echo '=================================='
            echo '❌ PIPELINE FAILED'
            echo '=================================='
            echo 'Check the logs above for error details'
            echo '=================================='
        }

        always {
            echo '=================================='
            echo '🧹 Cleaning up workspace...'
            echo '=================================='
        }
    }
}
