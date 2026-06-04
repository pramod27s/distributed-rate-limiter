# DevOps CI/CD Pipeline Summary

### Infrastructure as Code (IaC)
The project utilizes a fully declarative GitOps approach. All deployment logic is codified in a `Jenkinsfile` stored directly alongside the application source code in GitHub. This ensures the CI/CD pipeline is version-controlled, reproducible, and seamlessly bridges the development layer with operations.

### Continuous Integration (CI)
The pipeline employs automated **Poll SCM** (`H/2 * * * *`). Every two minutes, Jenkins actively checks the GitHub repository for new commits on the `main` branch. Upon detecting a change, it automatically allocates a local agent workspace and securely clones the latest source code. 
To ensure strict quality control, the pipeline runs a dedicated **Test Stage** (`mvn clean test`), halting instantly if any unit logic fails. This guarantees that broken code will never reach production. Built upon success, the subsequent **Build Stage** uses **Apache Maven** (`mvn package -Dmaven.test.skip=true`) to compile the Java 21 bytecode rapidly without re-running tests, outputting a standalone Spring Boot `.jar` artifact.

### Continuous Deployment (CD)
To guarantee zero-touch deployments without manual SSH intervention, the pipeline executes a complex two-step process:
1. **Automated Teardown & OS Port Management:** Before building, a Windows batch script (`netstat`, `findstr`, `TaskKill`) locates and terminates the old live Java process running on port 8080. This successfully drops the OS-level file-lock, preventing Maven `clean` I/O crashes.
2. **Background Native Execution:** Bypassing traditional blocking terminals, the pipeline leverages background execution (`start /B java -jar`). To prevent Jenkins' internal 'Process Tree Killer' from terminating the newly launched server upon job completion, a systemic environment variable block (`set JENKINS_NODE_COOKIE=dontKillMe`) is injected, perfectly orchestrating a continuous, uninterrupted application lifecycle.

### Pipeline Lifecycle & Post-Execution Hooks
The pipeline incorporates robust `post` block hooks to manage the complete lifecycle, emitting structured notifications for both `success` (active deployment logs) and `failure` (halting pipeline instantly to notify operators).

## System Architecture & CI/CD Pipeline Workflow (Pipeline Diagram Mandatory)

```text
====================    (1) Code Push    =======================
|  Developer (Git) | -----------------> | GitHub Repository    |
====================                    =======================
                                                   |
===================================================|======================
Jenkins CI/CD Pipeline (Windows Node)              | (2) Poll SCM (H/2)
                                                   v
               +---------------------------------------------------------+
               | STAGE 1: Stop Existing Server                           |
               | -> Audit Port 8080 (netstat, findstr)                   |
               | -> Force-kill existing Java Process (TaskKill)          |
               +---------------------------------------------------------+
                                            |
                                            v
               +---------------------------------------------------------+
               | STAGE 2: Source Control Checkout                        |
               | -> Clone latest repository state (branch: main)         |
               +---------------------------------------------------------+
                                            |
                                            v
               +---------------------------------------------------------+
               | STAGE 3: Run Tests (Continuous Integration)             |
               | -> Toolchain: Java 21, Maven 3.x                        |
               | -> Command: mvn clean test                              |
               | -> Action: Halts pipeline on Unit/Integration failures  |
               +---------------------------------------------------------+
                                            |
                                            v
               +---------------------------------------------------------+
               | STAGE 4: Build and Package (Spring Boot Backend)        |
               | -> Command: mvn package -Dmaven.test.skip=true          |
               | -> Output Artifact: distributed-rate-limiter-1.2.0.jar  |
               +---------------------------------------------------------+
                                            |
                                            v
               +---------------------------------------------------------+
               | STAGE 5: Continuous Deployment (CD) & Execution         |
               | -> Bypass Process Killer: JENKINS_NODE_COOKIE=dontKillMe|
               | -> Native Background Run: start /B java -jar target/... |
               | -> Live Application State: Serving REST API on Port 8080|
               +---------------------------------------------------------+
                                            |
                                            v
               +---------------------------------------------------------+
               | POST EXECUTION (Lifecycle Hooks)                        |
               | -> Success / Failure Notification Routing               |
               +---------------------------------------------------------+
```

## System Architecture Diagram

```text
                                  +-----------------------+
                                  |    Client Devices     |
                                  | (Web Dashboard, APIs) |
                                  +-----------+-----------+
                                              |
                                              | HTTP Traffic
                                              v
+-----------------------------------------------------------------------------------+
|                        Distributed Rate Limiter Ecosystem                           |
|                                                                                   |
|   +-----------------------+       +-----------------------+       +-----------+   |
|   |   Node 1 (Port 8080)  |       |   Node 2 (Port 8081)  |       | Node N... |   |
|   | +-------------------+ |       | +-------------------+ |       |           |   |
|   | | Spring Boot App   | |       | | Spring Boot App   | |       |           |   |
|   | | - REST Controller | |       | | - REST Controller | |       |           |   |
|   | | - Rate Limit Core | |.......| | - Rate Limit Core | |.......|           |   |
|   | | - Metrics/Config  | |       | | - Metrics/Config  | |       |           |   |
|   | +---------+---------+ |       | +---------+---------+ |       |           |   |
|   +-----------|-----------+       +-----------|-----------+       +-----------+   |
|               |                               |                                   |
|               |    Redis Protocol / TCP       |                                   |
|               v                               v                                   |
|   +---------------------------------------------------------------------------+   |
|   |                         Distributed State Layer                           |   |
|   |                                                                           |   |
|   |   +-------------------------------------------------------------------+   |   |
|   |   |                           REDIS CLUSTER                           |   |   |
|   |   | - Centralized Token Buckets / Sliding Windows / Key Configurations|   |   |
|   |   | - Evaluates LuA Scripts for Atomic Consistency Validation         |   |   |
|   |   +-------------------------------------------------------------------+   |   |
|   +---------------------------------------------------------------------------+   |
+-----------------------------------------------------------------------------------+
```
