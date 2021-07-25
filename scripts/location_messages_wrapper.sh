#!/bin/bash
# This script wraps a kotlin script, setting the path to the kotlin run time
# and passing the input arguments to it.

# I've installed the kotlin SDK using SDKMAN - https://sdkman.io/
# You might need to update the path below if you've installed it elsewhere
~/.sdkman/candidates/kotlin/current/bin/kotlin scripts/location_messages.kts "$@"