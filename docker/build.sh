#!/bin/sh
cd /root/docker/helloan
sbt clean docker:publishLocal
docker run -p 9001:9000 -e "APPLICATION_SECRET=jenesuispasunheros" digigladd/helloan/seance:1.0