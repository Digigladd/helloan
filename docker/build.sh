#!/bin/sh
docker stack rm helloan-publication
docker stack rm helloan-seance
docker stack rm helloan-sync

cd /root/docker/helloan
sbt clean docker:stage

cd /root/docker/helloan/helloan-seance-impl/target/docker/stage
docker build -t digigladd/helloan/seance:1.0 .

cd /root/docker/helloan/helloan-sync-impl/target/docker/stage
docker build -t digigladd/helloan/sync:1.0 .

cd /root/docker/helloan/helloan-publication-impl/target/docker/stage
docker build -t digigladd/helloan/publication:1.0 .

docker stack deploy -c docker/seance.yml helloan-seance
#docker stack deploy -c docker/sync.yml helloan-sync
#docker stack deploy -c docker/publication.yml helloan-publication
