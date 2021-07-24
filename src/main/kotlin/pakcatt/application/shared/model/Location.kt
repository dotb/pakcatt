package pakcatt.application.shared.model

data class Location(val latitudeDegreesMinutesHundredths: String,
                    val longitudeDegreesMinutesHundredths: String,
                    val latitudeDecimalDegreesNorth: Double,
                    val longitudeDecimalDegreesEast: Double,
                    val locationAmbiguity: Int,
                    val speedKmh: Double,
                    val speedKnots: Double,
                    val courseDegrees: Int)