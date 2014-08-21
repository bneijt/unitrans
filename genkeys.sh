#!/bin/bash
mkdir -p keys
cd keys
export PASSWORD=G9W9hbas9cr8mc0JZP09
set -e
#Generate CA
TARGET=unitransCA
if [ ! -f unitransCA.key ]; then
    openssl genrsa -out unitransCA.key 4096
fi
if [ ! -f unitransCA.crt ]; then
    openssl req -x509 -new -nodes -key unitransCA.key -days 1024 -out unitransCA.crt <<EOF
NL
Utrecht
.
bneijt.nl
unitrans
unitransCA
.
EOF
fi

function generate() {
    TARGET=$1
if [ ! -f ${TARGET}.key ]; then
    openssl genrsa -out ${TARGET}.key 4096
fi
if [ ! -f ${TARGET}.crt ]; then
    openssl req -new -key ${TARGET}.key -out ${TARGET}.csr <<EOF
NL
.
.
bneijt.nl
unitrans
unitrans_${TARGET}
.


EOF
    openssl x509 -req -in ${TARGET}.csr -CA unitransCA.crt -CAcreateserial -CAkey unitransCA.key -out ${TARGET}.crt -days 512

fi
if [ ! -f ${TARGET}.pkcs12 ]; then
    openssl pkcs12 -inkey ${TARGET}.key -in ${TARGET}.crt -CAfile unitransCA.crt -chain -export -passout env:PASSWORD -out ${TARGET}.p12
fi
if [ ! -f ${TARGET}.jks ]; then
    keytool -importkeystore -srckeystore ${TARGET}.p12 -srcstoretype PKCS12 -destkeystore ${TARGET}.jks -srcstorepass:env PASSWORD -storepass:env PASSWORD
    echo yes | keytool -keystore ${TARGET}.jks -import -file unitransCA.crt -alias cacert -trustcacerts -storepass:env PASSWORD
    #keytool -keystore ${TARGET}.jks -import -file ${TARGET}.crt -alias cert -storepass:env PASSWORD
fi

}

generate client
generate server


# #Generate one for the unitrans client
# openssl genrsa -out client.key 4096

# openssl x509 -req -in client.csr -CA unitransCA.pem -CAkey unitransCA.key -set_serial 1 -out client.crt -days 512

# #openssl pkcs12 -export -in client.crt -out client.pkcs12 -password:

#keytool -keystore keystore -import -alias jetty -file unitransCA.crt -trustcacerts

