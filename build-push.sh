# /bin/bash

git checkout feature/spark

tag=$(git rev-parse --short HEAD)

docker build -t chimera/ontop:${tag} .

