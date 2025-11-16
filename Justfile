# Name of this service
SERVICE := 'microservice-1'

build:
    docker build -t $(SERVICE):latest .

load-image:
    minikube image load $(SERVICE):latest --profile webshop

start:
    just --justfile ../infra/Justfile start

stop:
    just --justfile ../infra/Justfile stop

delete:
    just --justfile ../infra/Justfile delete