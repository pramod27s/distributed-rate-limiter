pipeline {
    agent any

    tools {
        // You will need to configure these tools in Jenkins > Global Tool Configuration
        maven 'Maven 3.x'
        jdk 'JDK 21' // Assuming you are using Java 21 based on previous logs
    }

    stages {
        stage('Checkout') {
            steps {
                // Checkout code from GitHub
                git branch: 'main', url: 'https://github.com/pramod27s/distributed-rate-limiter.git'
            }
        }

        stage('Build Spring Boot Backend') {
            steps {
                // Run Maven package to compile and build the .jar file
                // -DskipTests is used to speed up the demo, you can remove it later
                bat 'mvn clean package -DskipTests'
            }
        }

        stage('Deploy to Server') {
            steps {
                script {
                    echo "Deploying to NGD Application/Server..."
                    // For a local demo, we kill the existing process (if running) and start the new Jar
                    // Note: This is a simple bash/batch approach for demonstration.

                    try {
                        // Attempt to stop existing Java process on port 8080
                        bat 'FOR /F "tokens=5" %%T IN (\'netstat -a -n -o ^| findstr :8080\') DO TaskKill.exe /PID %%T /F'
                    } catch (Exception e) {
                        echo "No existing process running on port 8080 or failed to kill"
                    }

                    // Start the newly built jar in the background
                    // The jar name might change based on your pom.xml version
                    bat 'start java -jar target/distributed-rate-limiter-1.2.0.jar'
                }
            }
        }
    }
}

