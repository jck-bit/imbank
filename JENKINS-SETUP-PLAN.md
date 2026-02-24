# Jenkins Setup Plan (For Presentation)

## Goal
Get Jenkins running and create a basic CI/CD pipeline that builds your Docker images automatically when you push code to GitHub.

## Step-by-Step Plan (1-2 hours)

### Phase 1: Install Jenkins (20 minutes)

#### Option A: Jenkins on Your Mac (Easiest for demo)
```bash
# Install Jenkins with Homebrew
brew install jenkins-lts

# Start Jenkins
brew services start jenkins-lts

# Access at: http://localhost:8080
```

#### Option B: Jenkins in Docker (Cleaner, isolated)
```bash
# Run Jenkins in a container
docker run -d \
  --name jenkins \
  -p 8080:8080 -p 50000:50000 \
  -v jenkins_home:/var/jenkins_home \
  -v /var/run/docker.sock:/var/run/docker.sock \
  jenkins/jenkins:lts

# Get initial admin password
docker exec jenkins cat /var/jenkins_home/secrets/initialAdminPassword
```

**Initial Setup:**
1. Open http://localhost:8080
2. Enter admin password
3. Install suggested plugins
4. Create admin user
5. Keep default Jenkins URL

---

### Phase 2: Install Required Plugins (10 minutes)

Go to: **Manage Jenkins → Plugins → Available**

Install these plugins:
- ✅ **Docker Pipeline** - Build Docker images
- ✅ **Git** - Pull code from GitHub
- ✅ **Pipeline** - Create Jenkins pipelines
- ✅ **GitHub** - GitHub integration
- ✅ **AWS Steps** (optional for now) - Deploy to AWS later

Click "Download and install after restart"

---

### Phase 3: Create Your First Pipeline (15 minutes)

#### 3.1 Create Jenkins Job
1. Click **"New Item"**
2. Enter name: `imbank-microservices-pipeline`
3. Select **"Pipeline"**
4. Click **OK**

#### 3.2 Configure Pipeline
1. Under **"Pipeline"** section:
   - Definition: **Pipeline script from SCM**
   - SCM: **Git**
   - Repository URL: Your GitHub repo URL
   - Branch: `*/microservices` (or your branch name)
   - Script Path: `Jenkinsfile`

2. Click **Save**

---

### Phase 4: Create Basic Jenkinsfile (20 minutes)

Create this file in your project root:

**File: `/Users/jack.kinyanjui/IdeaProjects/imbank/Jenkinsfile`**

```groovy
pipeline {
    agent any

    environment {
        DOCKER_REGISTRY = 'docker.io'  // Change to ECR later
        DOCKER_CREDENTIALS_ID = 'dockerhub-credentials'
    }

    stages {
        stage('Checkout') {
            steps {
                echo '📥 Checking out code from GitHub...'
                checkout scm
            }
        }

        stage('Build Docker Images') {
            steps {
                echo '🐳 Building Docker images...'
                script {
                    // Build all services
                    sh 'docker-compose build'
                }
            }
        }

        stage('List Images') {
            steps {
                echo '📋 Listing built images...'
                sh 'docker images | grep imbank'
            }
        }

        stage('Verify Services') {
            steps {
                echo '✅ All services built successfully!'
            }
        }
    }

    post {
        success {
            echo '🎉 Pipeline completed successfully!'
        }
        failure {
            echo '❌ Pipeline failed. Check logs above.'
        }
        always {
            echo '🧹 Cleaning up workspace...'
            cleanWs()
        }
    }
}
```

---

### Phase 5: Run Your First Build (10 minutes)

1. Go to your pipeline: `imbank-microservices-pipeline`
2. Click **"Build Now"**
3. Watch the build progress in **"Console Output"**
4. See stages complete one by one

**What happens:**
- ✅ Jenkins pulls your code
- ✅ Jenkins runs `docker-compose build`
- ✅ All 6 Docker images are built
- ✅ Build completes successfully

---

### Phase 6: Setup GitHub Webhook (Optional - 10 minutes)

**Automate builds when you push code:**

1. In Jenkins job → **Configure**:
   - Check ✅ "GitHub hook trigger for GITScm polling"
   - Save

2. In GitHub repo → **Settings → Webhooks**:
   - Add webhook
   - Payload URL: `http://your-jenkins-url:8080/github-webhook/`
   - Content type: `application/json`
   - Events: "Just the push event"
   - Active: ✅

**Now when you push code → Jenkins automatically builds!**

---

## For Your Presentation

### What to Show:

1. **Jenkins Dashboard**
   - Show your pipeline job
   - Show build history (green = success)

2. **Run a Build Live**
   - Click "Build Now"
   - Show "Console Output" with stages
   - Show Docker images being built

3. **Show the Jenkinsfile**
   - Explain stages: Checkout → Build → Verify
   - Show how it's version-controlled with your code

4. **Show Docker Images**
   - After build, run: `docker images | grep imbank`
   - Show all 6 services built

### Key Points to Mention:

✅ **Automated CI/CD**: Code push → Auto build → Docker images
✅ **Version Control**: Pipeline as code (Jenkinsfile in Git)
✅ **Multi-service**: Builds all 6 microservices in one pipeline
✅ **Docker Integration**: Jenkins builds containers automatically
✅ **Future**: Can extend to push to AWS ECR and deploy to ECS

---

## After Presentation - Deep Dive Topics

### 1. Jenkins Deep Dive
- How Jenkins works (master/agent architecture)
- Pipeline syntax (declarative vs scripted)
- Environment variables and credentials
- Build triggers and webhooks
- Jenkins plugins ecosystem

### 2. Docker Deep Dive
- Multi-stage builds explained
- Layer caching and optimization
- Container networking
- Docker Compose orchestration
- Health checks and dependencies

### 3. AWS Deployment
- Push images to ECR
- Create ECS cluster
- Deploy services
- Load balancer setup
- Auto-scaling

### 4. Complete CI/CD Pipeline
- Jenkins builds images
- Pushes to ECR
- Updates ECS services
- Zero-downtime deployments
- Rollback strategies

---

## Quick Commands Reference

```bash
# Start Jenkins (Homebrew)
brew services start jenkins-lts

# Stop Jenkins
brew services stop jenkins-lts

# Restart Jenkins
brew services restart jenkins-lts

# View Jenkins logs
tail -f /usr/local/var/log/jenkins-lts/jenkins.log

# If using Docker:
docker start jenkins
docker stop jenkins
docker logs -f jenkins

# Build manually (without Jenkins)
docker-compose build

# Test locally
docker-compose up
```

---

## Troubleshooting

### Jenkins can't access Docker
**Solution:** Add Jenkins user to docker group
```bash
# On Mac with Colima
export DOCKER_HOST="unix:///Users/$USER/.colima/default/docker.sock"

# In Jenkins system configuration
# Manage Jenkins → System → Environment variables
# Add: DOCKER_HOST = unix:///Users/jack.kinyanjui/.colima/default/docker.sock
```

### Build fails with "permission denied"
**Solution:** Give Jenkins access to Docker socket
```bash
# Check Docker socket
ls -l ~/.colima/default/docker.sock

# Run Jenkins with Docker access (if using Docker Jenkins)
docker run -v /var/run/docker.sock:/var/run/docker.sock ...
```

---

## Next Steps (After Presentation)

1. ✅ **Test locally**: `docker-compose up` (verify services work)
2. ✅ **Deep dive**: Understand Dockerfiles line by line
3. ✅ **Setup AWS**: ECR repositories, ECS cluster
4. ✅ **Deploy**: Push images and deploy to AWS
5. ✅ **Advanced Jenkins**: Add test stage, deploy stage

---

Good luck with your presentation! 🚀

**Time estimate:**
- Jenkins install: 20 min
- Plugin setup: 10 min
- Create pipeline: 15 min
- Create Jenkinsfile: 20 min
- First build: 10 min
- **Total: ~1 hour 15 minutes**

You'll have Jenkins running and building your microservices automatically!
