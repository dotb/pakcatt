#!/bin/sh

echo $1 > /tmp/input

if [ $1 = '{"channelIdentifier":"144.875Mhz","remoteCallsign":"REM1C","remoteCallsignWithoutSSID":"MYCALL","addressedToCallsign":"MYCALL","message":"date","channelIsInteractive":true,"viaRepeaterOne":null,"viaRepeaterTwo":null,"canReceiveMessage":false,"location":null,"userContext":null}' ]; then
    echo "ACK_WITH_TEXT The date is 1 Jan"
  elif [ $1 = '{"channelIdentifier":"144.875Mhz","remoteCallsign":"REM1C","remoteCallsignWithoutSSID":"MYCALL","addressedToCallsign":"MYCALL","message":"ack","channelIsInteractive":true,"viaRepeaterOne":null,"viaRepeaterTwo":null,"canReceiveMessage":false,"location":null,"userContext":null}' ]; then
    echo "ACK_ONLY"
  else
      echo "IGNORE"
  fi
else
  echo "IGNORE"
fi