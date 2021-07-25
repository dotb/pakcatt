

val myCallsigns = listOf<String>("YOUR", "INTERESTED", "CALLS", "HERE")
/***
 * This script parses location updates, and will respond to stations
 * that are in pre-defined locations.
 *
 * Add a list of locations to the locations variable, of type Location (see the class at the end of the file).
 * Then make sure you update the list of myCallsigns to select callsigns this scripts responds to.
 *
 * Note that a message is sent for every beacon within a pre-defined location, so, this script can make the APRS
 * airwaves quite chatty. I recommend adding some logic to limit the number of messages sent, as most radios will
 * ignore repeated messages, anyway.
 */
// Populate this list with callsigns that this script should respond to
// Add some locations to this list, along with a message that is sent when a beacon is received in that location
val locations = listOf<Location>(
    Location(0, 0, 40.0, "Welcome to the middle of the earth! :-)"),
)

var inputArguments = HashMap<String, String>()
val earthRadiusKm = 6371
parseInputAgruments()

val remoteCall: String? = argumentForKey("remoteCallsignWithoutSSID")
val speed: Double? = argumentForKey("speedKmh")?.toDouble()
val lat: Double? = argumentForKey("latitudeDecimalDegreesNorth")?.toDouble()
val lon: Double? = argumentForKey("longitudeDecimalDegreesEast")?.toDouble()

// Only respond if the remote station is in the list of myCallsigns, and we have valid data
var response = "IGNORE DEBUG remoteCall: $remoteCall speed: $speed lat: $lat lon: $lon args: $inputArguments"
if (null != remoteCall && null != speed &&
    null != lat && null != lon && myCallsigns.contains(remoteCall)) {
    // Is the station within any of the locations?
    for (location in locations) {
        val distanceMeters = distanceKm(location.lat, location.lon, lat, lon) * 1000
        if (distanceMeters < location.radiusMeters) {
            response = "ACK_WITH_TEXT ${location.message} Distance: ${Math.round(distanceMeters)}m"
        }
    }
}

// Send the response
println(response)

/** Functions **/
/**
 * Calculate the distance between two coordinates
 * using the Haversine formula.
 */
fun distanceKm(latOne: Double, lonOne: Double, latTwo: Double, lonTwo: Double): Double {
    // Work out the delta between lat and lon, as radians
    val latDelta = degreesToRadians(latTwo - latOne)
    val lonDelta = degreesToRadians(lonTwo - lonOne)

    // Convert both latitudes to radians
    val latRadiansOne = degreesToRadians(latOne)
    val latRadiansTwo = degreesToRadians(latTwo)

    var lengthA = Math.sin(latDelta/2) * Math.sin(latDelta/2) + Math.sin(lonDelta/2) * Math.sin(lonDelta/2) * Math.cos(latRadiansOne) * Math.cos(latRadiansTwo)
    var lengthC = 2 * Math.atan2(Math.sqrt(lengthA), Math.sqrt(1-lengthA))
    return earthRadiusKm * lengthC
}

/**
 * Convert degrees to radians
 */
fun degreesToRadians(degrees: Double): Double {
    return degrees * Math.PI / 180
}

/**
 * Return the value for an input argument
 */
fun argumentForKey(key: String): String? {
    if (inputArguments.containsKey(key)) {
        return inputArguments[key]
    }
    return null
}

/**
 * A quik-n-dirty JSON parser
 * Parse input arguments into a map.
 */
fun parseInputAgruments() {
    for (arg in args) {
        var simplerString = arg.replace("location:","")
        simplerString = simplerString.replace("[{}]".toRegex(), "") // Remove braces
        val keyValuePairs = simplerString.split(",")
        for (keyValuePair in keyValuePairs) {
            val indexOfValue = keyValuePair.indexOf(":")
            val key = keyValuePair.substring(0, indexOfValue).replace("\"", "")
            val value = keyValuePair.substring(indexOfValue + 1, keyValuePair.length).replace("\"", "")
            inputArguments.put(key, value)
        }
    }
}

/** Data Classes **/
data class Location(val lat: Double,
                    val lon: Double,
                    val radiusMeters: Double,
                    val message: String)
