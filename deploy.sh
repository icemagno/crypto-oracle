#! /bin/sh

mvn clean package

docker build --tag=magnoabreu/crypto-oracle:1.0 --rm=true .

docker run -p 36201:8888 --name oracle \
--hostname=oracle \
-v /srv/crypto-oracle:/data \
-d magnoabreu/crypto-oracle:1.0

