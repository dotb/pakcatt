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