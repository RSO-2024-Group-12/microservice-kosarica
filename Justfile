# Name of this service
SERVICE := 'microservice-kosarica'
CONTEXT := 'nakupify-platform'

build:
    docker build -t $(SERVICE):latest .

load-image:
    minikube image load $(SERVICE):latest --profile {{ CONTEXT }}

start:
    just --justfile ../infra/Justfile start

stop:
    just --justfile ../infra/Justfile stop

delete:
    just --justfile ../infra/Justfile delete