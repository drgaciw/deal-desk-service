#!/bin/bash

# Set environment variables
export DOCKER_BUILDKIT=1
export COMPOSE_DOCKER_CLI_BUILD=1

# Function to display usage
show_usage() {
    echo "Usage: $0 [local|prod] [service-name]"
    echo "Options:"
    echo "  local         - Deploy services locally using docker-compose"
    echo "  prod          - Deploy services to Kubernetes cluster"
    echo "  service-name  - Optional: Specific service to deploy (deal-core|rules-runtime|tcv-processors)"
    exit 1
}

# Function to deploy locally using docker-compose
deploy_local() {
    local service=$1
    echo "🚀 Starting local deployment..."
    
    if [ -n "$service" ]; then
        echo "📦 Building and starting $service..."
        docker-compose build $service
        docker-compose up -d $service
    else
        echo "📦 Building and starting all services..."
        docker-compose build
        docker-compose up -d
    fi
    
    echo "🔍 Checking service health..."
    sleep 10
    docker-compose ps
}

# Function to deploy to Kubernetes
deploy_prod() {
    local service=$1
    echo "🚀 Starting production deployment..."
    
    # Check if kubectl is available
    if ! command -v kubectl &> /dev/null; then
        echo "❌ kubectl is not installed"
        exit 1
    }
    
    # Build and push Docker images
    if [ -n "$service" ]; then
        echo "📦 Building $service..."
        docker build -t deal-desk-service/$service:latest -f k8s/$service/Dockerfile .
        
        echo "🚀 Deploying $service to Kubernetes..."
        kubectl apply -f k8s/$service/deployment.yaml
        kubectl apply -f k8s/$service/service.yaml
    else
        echo "📦 Building all services..."
        for svc in deal-core rules-runtime tcv-processors; do
            docker build -t deal-desk-service/$svc:latest -f k8s/$svc/Dockerfile .
        done
        
        echo "🚀 Deploying all services to Kubernetes..."
        kubectl apply -f k8s/deal-core/
        kubectl apply -f k8s/rules-runtime/
        kubectl apply -f k8s/tcv-processors/
    fi
    
    echo "🔍 Checking deployment status..."
    kubectl get pods
}

# Main script logic
if [ $# -lt 1 ]; then
    show_usage
fi

case "$1" in
    "local")
        deploy_local $2
        ;;
    "prod")
        deploy_prod $2
        ;;
    *)
        show_usage
        ;;
esac

echo "✅ Deployment completed!"