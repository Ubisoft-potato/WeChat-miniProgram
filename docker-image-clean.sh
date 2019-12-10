echo "----------------start remove old images----------------"
docker rmi -f $(docker images -f "dangling=true" -q)
echo -e "\n\n"
docker images