
# This script is called when a request is received with a message
# It should respond with the following format:
# RESPONSE_TYPE [MESSAGE]

# Here are a few examples:
# ACK_WITH_TEXT <message goes here> # Acknowlege the message and reply with a message
# ACK_ONLY # Just acknowlege the message
# IGNORE # Ignore the message

# This example responds to messages to 'MYCALL' sending static date

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