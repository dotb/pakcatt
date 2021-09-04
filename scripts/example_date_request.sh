#!/bin/sh
# This script is called when a request is received with a message
# It should respond with the following format:
# RESPONSE_TYPE [MESSAGE]

# Here are a few examples:
# ACK_WITH_TEXT <message goes here> # Acknowledge the message and reply with a message
# ACK_ONLY # Just Acknowledge the message
# IGNORE # Ignore the message

# The input to the script is JSON, for example:
# '{"remoteCallsign":"VK3LIT-1","addressedToCallsign":"AB3CDE","content":"Hello friend!","location":null}'
# This example script uses the jq command to decode the input JSON, and the sed command to remove " characters

# This example responds to messages to 'MYCALL' sending static date

# Here's an example JSON payload that's passed to the script:
# {"remoteCallsign":"VK3LIT-7","remoteCallsignWithoutSSID":"VK3LIT","addressedToCallsign":"VK3LIT-1","message":"WIDE1-1","viaRepeaterOne":"WIDE2-1","viaRepeaterTwo":"date","canReceiveMessage":true,"location":null,"userContext":{"eolSequence":""}}

MYCALL=`echo $1 | jq .addressedToCallsign | sed 's/"//g'`
THEIR_CALL=`echo $1 | jq .remoteCallsign | sed 's/"//g'`
MESSAGE=`echo $1 | jq .message | sed 's/"//g'`

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