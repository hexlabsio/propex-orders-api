package io.hexlabs.propex

import org.joda.time.DateTime
import org.joda.time.chrono.ISOChronology
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.Locale

val utcPattern = DateTimeFormatter.ofPattern("yyyy-MM-dd")
val utcPatternJoda = org.joda.time.format.DateTimeFormat.forPattern("yyyy-MM-dd")
    .withLocale(Locale.ROOT)
    .withChronology(ISOChronology.getInstanceUTC())

fun org.joda.time.DateTime.toUtcString() = Instant
    .ofEpochMilli(millis)
    .atOffset(ZoneOffset.UTC)
    .format(utcPattern)

fun Instant.asJoda(): org.joda.time.DateTime = org.joda.time.DateTime(this.toEpochMilli())

fun jodaDateTimeFrom(utcString: String): org.joda.time.DateTime = DateTime.parse(utcString, utcPatternJoda)

fun instantFrom(utcString: String): java.time.Instant = LocalDate
    .parse(utcString, utcPattern)
    .atStartOfDay()
    .toInstant(ZoneOffset.UTC)
