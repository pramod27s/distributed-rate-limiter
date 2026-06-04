# Comprehensive Project & DevOps CI/CD Report: Distributed Rate Limiter

## Part 1: Project Overview & Architecture

### 1.1 Executive Summary
The **Distributed Rate Limiter** is a highly scalable backend gateway service designed to control and regulate the volume of incoming network traffic to APIs. By limiting the number of requests a user or client can make within a specific timeframe, the system protects underlying microservices from Denial-of-Service (DoS) attacks, brute-force activity, and accidental traffic spikes, while also enabling API monetization strategies (e.g., Free vs. Premium tiers).

### 1.2 Technology Stack
* **Backend Core:** Java 21, Spring Boot 3.5.x
* **Datastore / Caching:** Redis Cloud (Centralized state management)
* **Connection Pooling:** Lettuce (High-performance Redis client)
* **Build Automation:** Apache Maven
* **Frontend Dashboard:** React, Vite, Tailwind CSS (For real-time monitoring and analytics)
* **DevOps / CI/CD Pipeline:** Jenkins, Git, GitHub, Windows Batch/PowerShell Scripting

### 1.3 Core Application Features
#### A. Centralized Distributed Tracking (Redis)
Unlike local rate limiters that store counters in the memory of a single server (which fail when a system scales to multiple servers behind a load balancer), this project utilizes **Redis** as a centralized, high-speed datastore. Every incoming request checks and updates the global token count in Redis, ensuring accurate rate limiting across distributed server nodes.

#### B. Rate Limiting Algorithms
The system enforces API limits using the **Token Bucket Algorithm**. 
* **Mechanism:** Each API key is assigned a "bucket" with a maximum capacity of tokens. Tokens are consumed as requests are made, and the bucket is refilled at a consistent, predefined rate. 
* **Benefit:** This allows for sudden short bursts of traffic while strictly maintaining an overall long-term rate limit.

#### C. API Key & Tier Management
Traffic is identified and regulated via secure API keys submitted in HTTP headers header. The application supports dynamic tiering configured in `application.properties`:
* **Premium Users:** High capacity (e.g., 120 tokens) and fast refill rate (40/sec).
* **Standard Users:** Lower capacity (e.g., 20 tokens) and standard refill rate (5/sec).
Requests exceeding these limits are intercepted natively, returning a standard `429 Too Many Requests` HTTP error code to the client.

#### D. Real-Time Observability Dashboard
The backend Spring Boot app uses Spring Boot Actuator and Micrometer to securely expose `/actuator/health` and `/actuator/metrics` endpoints. The React/Vite frontend actively polls these endpoints to render dynamic, real-time charts displaying API Key usage, active requests-per-second, and system health.

---

## Part 2: DevOps & CI/CD Pipeline (Jenkins)

### 2.1 DevOps CI/CD Objective
The primary objective of the DevOps pipeline is to establish a seamless **Continuous Integration and Continuous Deployment (CI/CD)** workflow. The goal is to eliminate manual compilation, testing, and deployment overhead. When a developer pushes new code, it should be packaged and autonomously deployed to the NGD (Next Generation Deployment) Application/Server environment.

### 2.2 Jenkins Server Configuration
To allow Jenkins to build a modern Spring Boot application, the Jenkins environment specifically required Global Tool Configurations:
* **JDK 21:** Configured using the local system's Java path, allowing Jenkins to natively compile Java 21 bytecode.
* **Maven 3.x:** Configured to download automatically via Apache, enabling Jenkins to resolve `pom.xml` dependencies.

### 2.3 Automation Trigger Mechanism
* **SCM Polling (`H/2 * * * *`):** The Jenkins pipeline uses a Source Control Management (SCM) polling strategy. It checks the remote GitHub repository exactly every 2 minutes. 
* If a new commit hash is detected on the `main` branch, Jenkins immediately allocates a workspace and triggers the deployment pipeline.

### 2.4 Declarative Pipeline Stages (`Jenkinsfile`)
The pipeline follows a strict "Infrastructure as Code" paradigm, running the following critical stages sequentially:

#### Stage 1: Port Cleanup & Server Teardown (`Stop Existing Server`)
Before checking out new code, Jenkins must ensure the environment is clean. It executes a Windows batch script utilizing `netstat` and `findstr` to locate any existing Java process listening on port `8080`. It then calls `TaskKill.exe` to forcefully terminate it.

#### Stage 2: Source Checkout
Jenkins connects to the GitHub repository, resolves remote Deltas, and clones the most recent version of the codebase securely into a hidden Jenkins workspace (`C:\ProgramData\Jenkins\.jenkins\workspace\...`).

#### Stage 3: Maven Build Phase (`Build Spring Boot Backend`)
The pipeline runs the command `mvn clean package -Dmaven.test.skip=true`. 
* The `clean` directive wipes legacy build artifacts.
* The `package` directive instructs Maven to download external dependencies from Maven Central, compile Java code, and generate a fat, executable Spring Boot `.jar` file (`distributed-rate-limiter-1.2.0.jar`). Test compilation is explicitly skipped to ensure rapid micro-deployments.

#### Stage 4: Background Deployment (`Deploy to Server`)
Once the binary is prepared, Jenkins initiates the `.jar` using a non-blocking batch execution. This ensures the web application bounds to port `8080` in the background seamlessly.

### 2.5 DevOps Challenges & Technical Resolutions
During the development and testing of this automated pipeline, two major architectural impediments were encountered and strategically resolved:

#### Challenge 1: The "Locked File" Compilation Error (Windows I/O)
* **The Issue:** Maven's `clean` phase historically failed and threw a file-lock exception. The active Spring Boot application running from the previous successful deployment had an exclusive OS-level read-lock on the `.jar` file.
* **The Resolution:** We restructured the `Jenkinsfile` lifecycle. By executing the server shutdown (`TaskKill`) at the *very beginning* of the pipeline rather than the deployment stage, the file lock is released gracefully *before* Maven attempts to clean the workspace directory.

#### Challenge 2: Jenkins Process Tree Assassin (Child Orphaned Jobs)
* **The Issue:** Jenkins is heavily guarded against memory leaks. When the pipeline finished successfully, Jenkins’ internal Process Tree Killer swept the OS and automatically killed the newly spawned Java server, because it was technically a child process of the Jenkins Build job.
* **The Resolution:** Implemented official Jenkins variable bypassing: `set JENKINS_NODE_COOKIE=dontKillMe`. This command modifies the tracking identifier of the environment, conceptually "un-parenting" the task. The Java server safely escapes the final cleanup sweep and persists indefinitely post-deployment.

### 2.6 Presentation Workflow
The practical workflow that proves the success of this infrastructure demonstrates True DevOps:
1. A developer identifies a configuration issue, API adjustment, or code enhancement.
2. The code is modified locally and committed utilizing Git.
3. The developer executes `git push origin main`.
4. Within two minutes, without human interjection, Jenkins polls GitHub, detects the delta, purges the legacy server, builds the newly updated `.jar`, deploys the server natively in the background, and restores the Rate Limiting APIs functionality completely upgraded.
