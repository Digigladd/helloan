#!/bin/sh
docker stack rm helloan-publication
docker stack rm helloan-seance
docker stack rm helloan-sync

cd /root/docker/helloan
sbt clean docker:publishLocal

docker stack deploy -c docker/seance.yml helloan-seance
docker stack deploy -c docker/sync.yml helloan-sync
docker stack deploy -c docker/publication.yml helloan-publication
