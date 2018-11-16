#!/bin/sh
cd /root/docker/helloan
sbt clean docker:publishLocal
docker run -p 9001:9000 -e "APPLICATION_SECRET=jenesuispasunheros" digigladd/helloan/seance:1.0

while true
do
  STATUS=$(curl -s -o /dev/null -w '%{http_code}' http://localhost:9001/status)
  if [ $STATUS -eq 200 ]; then
    echo "Got 200! All done!"
    break
  else
    echo "Got $STATUS :( Not done yet..."
  fi
  sleep 10
done