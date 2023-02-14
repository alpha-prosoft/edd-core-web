#!/bin/bash

set -e

rm -rf myCA
rm -rf privkey.pem

mkdir myCA && mkdir -p myCA/signedcerts && mkdir myCA/private

mkdir -p ~/.edd
touch ~/.edd/domain

DEFAULT_DOMAIN=$(cat ~/.edd/domain)
read -p "Enter domain name [$DEFAULT_DOMAIN]: " name
export DOMAIN=${name:-$DEFAULT_DOMAIN}
echo $DOMAIN > ~/.edd/domain

echo "Copy CA config"
export CA_ID=$(date +%s)
envsubst '$CA_ID' < caconfig.cnf > myCA/caconfig.cnf

echo "Copy certifiicate config"
envsubst '$DOMAIN' < localhost.cnf > myCA/localhost.cnf

cd myCA
echo '01' > serial && touch index.txt

echo "Create new x509 CA certificate"
export OPENSSL_CONF=caconfig.cnf
openssl req -x509 -newkey rsa:2048 \
	-out cacert.pem \
	-outform PEM -days 1825 \
	-passout pass:shadow-cljs

echo "Create new key and signing request"
export OPENSSL_CONF=localhost.cnf
openssl req -newkey rsa:2048 \
	 -keyout tempkey.pem \
	 -keyform PEM \
	 -out tempreq.pem \
	 -outform PEM  \
	 -passout pass:shadow-cljs \
	 -passout pass:shadow-cljs

openssl rsa -passin pass:shadow-cljs < tempkey.pem > server_key.pem

echo "Sign key request and create certificate"
export OPENSSL_CONF=caconfig.cnf
openssl ca -batch \
	-in tempreq.pem \
	-out server_crt.pem \
	--passin pass:shadow-cljs

echo "Combine certificate and key in same file"
cat server_key.pem server_crt.pem > hold.pem

echo "Create mew key-store and not used base key"
keytool -noprompt \
        -genkey \
        -dname "CN=base, OU=Development, O=LOCAL, L=Wien, S=Wien, C=AT" \
        -alias base \
        -keyalg RSA -keystore keystore.jks -keysize 2048 \
        -storepass shadow-cljs \
        -keypass shadow-cljs

echo "Create certificate and key file in PFX"  
openssl pkcs12 -export \
	-out localhost.pfx \
	-in hold.pem \
	-name "localhost" \
	-passout pass:shadow-cljs \
	-passin pass:shadow-cljs

echo "Import certificate into keystore!"
keytool -importkeystore \
        -srcstorepass shadow-cljs \
	-destkeystore keystore.jks \
	-srcstoretype PKCS12 \
	-srckeystore  localhost.pfx \
	-storepass shadow-cljs \
	-keypass shadow-cljs

cd ..
cp myCA/cacert.pem cacert.pem
cp myCA/keystore.jks keystore.jks
rm -rf myCA

