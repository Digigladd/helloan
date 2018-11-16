#!/bin/sh
cd /root/docker/helloan
sbt clean docker:publishLocal
docker run -p 9001:9000 digigladd/helloan/seance:1.0