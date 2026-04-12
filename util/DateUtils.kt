package com.ravaroj.habitcurrency.util

import java.time.LocalDate
import java.time.format.DateTimeFormatter

object DateUtils {
    private val formatter: DateTimeFormatter = DateTimeFormatter.ISO_LOCAL_DATE

    fun todayString(): String = LocalDate.now().format(formatter)

    fun parse(date: String): LocalDate = LocalDate.parse(date, formatter)

    fun format(date: LocalDate): String = date.format(formatter)

    fun previousDate(date: String): String = format(parse(date).minusDays(1))

    fun minusDays(date: String, days: Long): String = format(parse(date).minusDays(days))

    fun datesBetweenExclusive(startDate: String, endDate: String): List<String> {
        val start = parse(startDate)
        val end = parse(endDate)

        if (!start.isBefore(end)) return emptyList()

        val dates = mutableListOf<String>()
        var current = start.plusDays(1)
        while (!current.isAfter(end)) {
            dates.add(format(current))
            current = current.plusDays(1)
        }
        return dates
    }
}
