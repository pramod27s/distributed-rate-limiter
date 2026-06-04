pipeline {
    agent any

    tools {
        // You will need to configure these tools in Jenkins > Global Tool Configuration
        maven 'Maven 3.x'
        jdk 'JDK 21' // Assuming you are using Java 21 based on previous logs
    }


    stages {
        stage('Stop Existing Server') {
            steps {
                script {
                    try {
                        // Attempt to stop existing Java process on port 8080 before building
                        bat '''
                        FOR /F "tokens=5" %%T IN ('netstat -ano ^| findstr "LISTENING" ^| findstr ":8080"') DO (
                            IF NOT "%%T"=="0" TaskKill /PID %%T /F
                        )
                        '''
                    } catch (Exception e) {
                        echo "No process found on port 8080 or failed to kill"
                    }
                }
            }
        }

        stage('Checkout') {
            steps {
                // Checkout code from GitHub
                git branch: 'main', url: 'https://github.com/pramod27s/distributed-rate-limiter.git'
            }
        }

        stage('Run Tests') {
            steps {
                // Run Maven test explicitly as its own stage
                // This ensures if tests fail, the build stops before packaging
                bat 'mvn clean test'
            }
        }

        stage('Build Spring Boot Backend') {
            steps {
                // Run Maven package to compile and build the .jar file
                // -Dmaven.test.skip=true used here because tests already ran in the previous stage!
                bat 'mvn package -Dmaven.test.skip=true'
            }
        }

        stage('Deploy to Server') {
            steps {
                script {
                    echo "Deploying to NGD Application/Server..."

                    // Override Jenkin's Process Tree Killer so it doesn't assassinate the java process
                    // Using Jenkins specific environment variable BUILD_ID bypass approach
                    bat 'set JENKINS_NODE_COOKIE=dontKillMe && start /B java -jar target/distributed-rate-limiter-1.2.0.jar'
                }
            }
        }
    }


    post {
        success {
            echo "✅ Pipeline completed successfully! Application deployed and running."
        }
        failure {
            echo "❌ Pipeline failed! Please inspect the Jenkins logs."
            // Future Add: send email/slack notification here
        }
        always {
            // General cleanup could go here
            echo "Pipeline run finished."
        }
    }
}

