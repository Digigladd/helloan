#!/bin/sh
docker stack rm helloan-publication
docker stack rm helloan-seance
docker stack rm helloan-sync

cd /root/docker/helloan

sbt clean docker:clean
sbt docker:publishLocal

cd docker/
echo "deploying helloan seance"
docker stack deploy -c seance.yml helloan-seance

wait 9001

docker stack deploy -c sync.yml helloan-sync

wait 9002

docker stack deploy -c publication.yml helloan-publication

wait() {
	PORT = $1
	while true
	do
	  STATUS=$(curl -s -o /dev/null -w '%{http_code}' http://localhost:$PORT/status)
	  if [ $STATUS -eq 200 ]; then
		echo "Got 200! All done!"
		break
	  else
		echo "Got $STATUS :( Not done yet..."
	  fi
	  sleep 10
	done
}
