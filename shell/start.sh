#!/bin/sh

# Move to the directory containing this script
cd `dirname "$0"`

#
LANG=en_US.UTF-8
export LANG
JAVA_OPTS="$JAVA_OPTS -Dfile.encoding=UTF-8 -Djava.protocol.handler.pkgs=jcifs"

# The directory containing the indexes (must be exported)
OPENSEARCHSERVER_DATA=data
export OPENSEARCHSERVER_DATA

# The TCP port used by the server
SERVER_PORT=9090

# Any JAVA option. Often used to allocate more memory. Uncomment this line to allocate 1GB.
#JAVA_OPTS="$JAVA_OPTS -Xms1G -Xmx1G"

# Starting the server
eval java $JAVA_OPTS -jar /specific/oss/opensearchserver/target/opensearchserver-2.0-SNAPSHOT.jar \
        -extractDirectory server \
        -httpPort ${SERVER_PORT} \
        -uriEncoding UTF-8 \
        >> "logs/oss.log" 2>&1 &

# Writing the PID
echo $! > "logs/oss.pid"
