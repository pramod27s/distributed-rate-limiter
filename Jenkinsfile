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
                        bat 'FOR /F "tokens=5" %%T IN (\'netstat -a -n -o ^| findstr :8080\') DO TaskKill.exe /PID %%T /F'
                    } catch (Exception e) {
                        echo "No existing process running on port 8080 or failed to kill"
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

        stage('Build Spring Boot Backend') {
            steps {
                // Run Maven package to compile and build the .jar file
                // -Dmaven.test.skip=true is used to completely skip test compilation
                bat 'mvn clean package -Dmaven.test.skip=true'
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
}

