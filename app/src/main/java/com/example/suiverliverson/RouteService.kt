package com.example.suiverliverson

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

suspend fun fetchRoadRoute(
    apiKey: String,
    origin: GeoPoint,
    pickup: GeoPoint,
    destination: GeoPoint
): List<GeoPoint> {
    return try {
        fetchOpenSourceRoadRoute(origin, pickup, destination)
    } catch (_: Exception) {
        try {
            fetchGoogleRoadRoute(apiKey, origin, pickup, destination)
        } catch (googleError: Exception) {
            throw IllegalStateException(
                "OpenStreetMap and Google road routing both failed.",
                googleError
            )
        }
    }
}

private suspend fun fetchOpenSourceRoadRoute(
    origin: GeoPoint,
    pickup: GeoPoint,
    destination: GeoPoint
): List<GeoPoint> = withContext(Dispatchers.IO) {
    val coordinates = listOf(origin, pickup, destination)
        .joinToString(";") { "${it.longitude},${it.latitude}" }
    val url = URL(
        "https://router.project-osrm.org/route/v1/driving/$coordinates?overview=full&geometries=polyline"
    )
    val connection = (url.openConnection() as HttpURLConnection).apply {
        connectTimeout = 10_000
        readTimeout = 10_000
        setRequestProperty("User-Agent", "Suiverliverson/1.0")
    }

    try {
        val responseText = readResponse(connection)
        val response = JSONObject(responseText)
        check(response.optString("code") == "Ok") { response.optString("message", "No road route found.") }
        val encodedPolyline = response.getJSONArray("routes")
            .getJSONObject(0)
            .getString("geometry")
        decodePolyline(encodedPolyline)
    } finally {
        connection.disconnect()
    }
}

private suspend fun fetchGoogleRoadRoute(
    apiKey: String,
    origin: GeoPoint,
    pickup: GeoPoint,
    destination: GeoPoint
): List<GeoPoint> = withContext(Dispatchers.IO) {
    check(apiKey.isNotBlank()) { "Google Maps API key is missing." }
    val requestBody = JSONObject().apply {
        put("origin", waypoint(origin))
        put("destination", waypoint(destination))
        put("intermediates", JSONArray().put(waypoint(pickup)))
        put("travelMode", "DRIVE")
        put("polylineQuality", "HIGH_QUALITY")
    }

    val connection = (URL("https://routes.googleapis.com/directions/v2:computeRoutes")
        .openConnection() as HttpURLConnection).apply {
        requestMethod = "POST"
        connectTimeout = 10_000
        readTimeout = 10_000
        doOutput = true
        setRequestProperty("Content-Type", "application/json")
        setRequestProperty("X-Goog-Api-Key", apiKey)
        setRequestProperty("X-Goog-FieldMask", "routes.polyline.encodedPolyline")
    }

    try {
        connection.outputStream.use { it.write(requestBody.toString().toByteArray(Charsets.UTF_8)) }
        val responseText = readResponse(connection)
        val encodedPolyline = JSONObject(responseText)
            .getJSONArray("routes")
            .getJSONObject(0)
            .getJSONObject("polyline")
            .getString("encodedPolyline")
        decodePolyline(encodedPolyline)
    } finally {
        connection.disconnect()
    }
}

private fun readResponse(connection: HttpURLConnection): String {
    val responseCode = connection.responseCode
    val stream = if (responseCode in 200..299) connection.inputStream else connection.errorStream
    val responseText = BufferedReader(InputStreamReader(stream)).use { it.readText() }
    check(responseCode in 200..299) { "Route request failed ($responseCode): $responseText" }
    return responseText
}

private fun waypoint(point: GeoPoint) = JSONObject().put(
    "location",
    JSONObject().put(
        "latLng",
        JSONObject()
            .put("latitude", point.latitude)
            .put("longitude", point.longitude)
    )
)

private fun decodePolyline(encoded: String): List<GeoPoint> {
    val points = mutableListOf<GeoPoint>()
    var index = 0
    var latitude = 0
    var longitude = 0

    while (index < encoded.length) {
        var result = 0
        var shift = 0
        var byte: Int
        do {
            byte = encoded[index++].code - 63
            result = result or ((byte and 0x1f) shl shift)
            shift += 5
        } while (byte >= 0x20)
        latitude += if ((result and 1) != 0) (result shr 1).inv() else result shr 1

        result = 0
        shift = 0
        do {
            byte = encoded[index++].code - 63
            result = result or ((byte and 0x1f) shl shift)
            shift += 5
        } while (byte >= 0x20)
        longitude += if ((result and 1) != 0) (result shr 1).inv() else result shr 1

        points += GeoPoint(latitude / 100_000.0, longitude / 100_000.0)
    }

    return points
}

fun pointAlongRoute(route: List<GeoPoint>, progress: Float): GeoPoint? {
    if (route.isEmpty()) return null
    if (route.size == 1 || progress <= 0f) return route.first()
    if (progress >= 1f) return route.last()

    val segmentLengths = route.zipWithNext { a, b -> distanceMeters(a, b) }
    val totalLength = segmentLengths.sum()
    if (totalLength <= 0.0) return route.first()

    val targetDistance = totalLength * progress
    var traversed = 0.0
    segmentLengths.forEachIndexed { index, segmentLength ->
        if (traversed + segmentLength >= targetDistance) {
            val segmentProgress = ((targetDistance - traversed) / segmentLength).toFloat()
            val start = route[index]
            val end = route[index + 1]
            return GeoPoint(
                start.latitude + (end.latitude - start.latitude) * segmentProgress,
                start.longitude + (end.longitude - start.longitude) * segmentProgress
            )
        }
        traversed += segmentLength
    }
    return route.last()
}

fun progressAtPoint(route: List<GeoPoint>, point: GeoPoint): Float {
    if (route.size < 2) return 0.5f
    val segmentLengths = route.zipWithNext { a, b -> distanceMeters(a, b) }
    val totalLength = segmentLengths.sum()
    if (totalLength <= 0.0) return 0.5f

    var traversed = 0.0
    var closestProgress = 0.5f
    var closestDistance = Double.MAX_VALUE
    route.forEachIndexed { index, routePoint ->
        val distanceToPoint = distanceMeters(routePoint, point)
        if (distanceToPoint < closestDistance) {
            closestDistance = distanceToPoint
            closestProgress = (traversed / totalLength).toFloat()
        }
        if (index < segmentLengths.size) traversed += segmentLengths[index]
    }
    return closestProgress.coerceIn(0.05f, 0.95f)
}

private fun distanceMeters(a: GeoPoint, b: GeoPoint): Double {
    val earthRadius = 6_371_000.0
    val lat1 = Math.toRadians(a.latitude)
    val lat2 = Math.toRadians(b.latitude)
    val deltaLat = Math.toRadians(b.latitude - a.latitude)
    val deltaLng = Math.toRadians(b.longitude - a.longitude)
    val haversine = sin(deltaLat / 2) * sin(deltaLat / 2) +
        cos(lat1) * cos(lat2) * sin(deltaLng / 2) * sin(deltaLng / 2)
    return earthRadius * 2 * atan2(sqrt(haversine), sqrt(1 - haversine))
}
