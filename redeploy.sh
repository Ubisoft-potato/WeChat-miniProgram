kubectl delete -f  ./k8s-deployment/photoshare-service/deployment.yaml
mvn clean package
docker rmi --force photoshare:dev
docker build -t photoshare:dev .
kubectl apply -f  ./k8s-deployment/photoshare-service/deployment.yaml
