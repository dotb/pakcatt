package pakcatt.application.shared.model

data class Location(public val latitudeDegrees: String,
                    public val longitudeDegrees: String,
                    public val locationAmbiguity: Int,
                    public val speedKmh: Double,
                    public val speedKnots: Double,
                    public val courseDegrees: Int)