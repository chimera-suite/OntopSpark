# /bin/bash

git rebase feature/spark

tag=$(git rev-parse --short feature/spark)

docker build -t chimera/ontop:${tag} .