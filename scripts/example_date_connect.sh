
# This script is called when a connection request is received.
# It should respond with the following format:
# RESPONSE_TYPE [MESSAGE]

# Here are a few examples:
# ACK_WITH_TEXT <message goes here> # Accept the request and reply with a message
# ACK_ONLY # Just accept the request, and do not send a message
# IGNORE # Ignore the request to connect

# This example accepts a request from the to the callsign 'MYCALL' and responds with a static date

#!/bin/sh
MYCALL=$1
THEIR_CALL=$2
MESSAGE=$3

if [ $MYCALL = 'MYCALL' ]; then
  if [ $MESSAGE = 'date' ]; then
    echo "ACK_WITH_TEXT The date is 1 Jan"
  elif [ $MESSAGE = 'ack' ]; then
    echo "ACK_ONLY"
  else
      echo "IGNORE"
  fi
else
  echo "IGNORE"
fi