# Set environment variables
$env:DOCKER_BUILDKIT = 1
$env:COMPOSE_DOCKER_CLI_BUILD = 1

# Function to display usage
function Show-Usage {
    Write-Host "Usage: .\deploy.ps1 [local|prod] [service-name]"
    Write-Host "Options:"
    Write-Host "  local         - Deploy services locally using docker-compose"
    Write-Host "  prod          - Deploy services to Kubernetes cluster"
    Write-Host "  service-name  - Optional: Specific service to deploy (deal-core|rules-runtime|tcv-processors)"
    exit 1
}

# Function to deploy locally using docker-compose
function Deploy-Local {
    param($service)
    Write-Host "🚀 Starting local deployment..."
    
    if ($service) {
        Write-Host "📦 Building and starting $service..."
        docker-compose build $service
        docker-compose up -d $service
    }
    else {
        Write-Host "📦 Building and starting all services..."
        docker-compose build
        docker-compose up -d
    }
    
    Write-Host "🔍 Checking service health..."
    Start-Sleep -Seconds 10
    docker-compose ps
}

# Function to deploy to Kubernetes
function Deploy-Prod {
    param($service)
    Write-Host "🚀 Starting production deployment..."
    
    # Check if kubectl is available
    if (!(Get-Command kubectl -ErrorAction SilentlyContinue)) {
        Write-Host "❌ kubectl is not installed"
        exit 1
    }
    
    # Build and push Docker images
    if ($service) {
        Write-Host "📦 Building $service..."
        docker build -t deal-desk-service/$service`:latest -f k8s/$service/Dockerfile .
        
        Write-Host "🚀 Deploying $service to Kubernetes..."
        kubectl apply -f k8s/$service/deployment.yaml
        kubectl apply -f k8s/$service/service.yaml
    }
    else {
        Write-Host "📦 Building all services..."
        @("deal-core", "rules-runtime", "tcv-processors") | ForEach-Object {
            docker build -t deal-desk-service/$_`:latest -f k8s/$_/Dockerfile .
        }
        
        Write-Host "🚀 Deploying all services to Kubernetes..."
        kubectl apply -f k8s/deal-core/
        kubectl apply -f k8s/rules-runtime/
        kubectl apply -f k8s/tcv-processors/
    }
    
    Write-Host "🔍 Checking deployment status..."
    kubectl get pods
}

# Main script logic
if ($args.Count -lt 1) {
    Show-Usage
}

switch ($args[0]) {
    "local" { Deploy-Local $args[1] }
    "prod" { Deploy-Prod $args[1] }
    default { Show-Usage }
}

Write-Host "✅ Deployment completed!"