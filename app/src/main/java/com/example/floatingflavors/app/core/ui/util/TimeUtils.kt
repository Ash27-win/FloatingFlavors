package com.example.floatingflavors.app.util

import java.time.Duration
import java.time.Instant
import java.time.OffsetDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

fun computeTimeAgoFromIso(iso: String?): String {
    if (iso.isNullOrBlank()) return ""

    val instant = try {
        // prefer OffsetDateTime, handles offsets like +00:00
        OffsetDateTime.parse(iso, DateTimeFormatter.ISO_OFFSET_DATE_TIME).toInstant()
    } catch (e: Exception) {
        try {
            Instant.parse(iso)
        } catch (e2: Exception) {
            return ""
        }
    }

    val now = Instant.now()
    val diff = Duration.between(instant, now)
    val seconds = diff.seconds

    return when {
        seconds < 60 -> "just now"
        seconds < 3600 -> {
            val m = (seconds / 60).toInt()
            if (m == 1) "1 min ago" else "$m mins ago"
        }
        seconds < 86400 -> {
            val h = (seconds / 3600).toInt()
            if (h == 1) "1 hr ago" else "$h hrs ago"
        }
        seconds < 604800 -> {
            val d = (seconds / 86400).toInt()
            if (d == 1) "1 day ago" else "$d days ago"
        }
        else -> {
            // format date
            val odt = instant.atZone(ZoneId.systemDefault())
            val fmt = DateTimeFormatter.ofPattern("dd MMM, yyyy")
            odt.format(fmt)
        }
    }
}
