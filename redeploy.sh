kubectl delete -f  ./k8s-deployment/photoshare-service/deployment.yaml
docker rmi --force photoshare:dev
mvn clean package
docker build -t photoshare:dev .
kubectl apply -f  ./k8s-deployment/photoshare-service/deployment.yaml
