echo "stopping possible old docker images..."
sudo docker stop naurandir-discord-bot
sudo docker rm naurandir-discord-bot
sudo docker rmi naurandir/discord-bot-java

echo "building new docker image..."
sudo docker build -t naurandir/discord-bot-java .
sudo docker-compose up -d
