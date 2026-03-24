package com.wlitkopa.thoughts.notification

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

/**
 * Simple 5-field cron parser: minute hour day-of-month month day-of-week
 * Supports: * | n | n,m | n-m | *\/n
 * day-of-week: 0=Sun, 1=Mon, ..., 6=Sat
 */
object CronParser {

    fun isValid(expression: String): Boolean {
        val fields = expression.trim().split("\\s+".toRegex())
        if (fields.size != 5) return false
        val ranges = listOf(0..59, 0..23, 1..31, 1..12, 0..6)
        return fields.zip(ranges).all { (field, range) -> isFieldValid(field, range) }
    }

    /** Returns the next Calendar after [from] that matches [expression], or null if none found. */
    fun nextExecution(expression: String, from: Calendar = Calendar.getInstance()): Calendar? {
        if (!isValid(expression)) return null
        val fields = expression.trim().split("\\s+".toRegex())
        val (minF, hourF, domF, monF, dowF) = fields

        val cal = from.clone() as Calendar
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        cal.add(Calendar.MINUTE, 1) // start from the next whole minute

        val limit = from.clone() as Calendar
        limit.add(Calendar.YEAR, 2)

        var safety = 0
        while (cal.before(limit) && safety++ < 200_000) {
            // Month
            if (!matches(monF, cal.get(Calendar.MONTH) + 1)) {
                cal.set(Calendar.DAY_OF_MONTH, 1)
                cal.set(Calendar.HOUR_OF_DAY, 0)
                cal.set(Calendar.MINUTE, 0)
                cal.add(Calendar.MONTH, 1)
                continue
            }
            // Day — if both dom and dow are non-wildcard, either match is enough (POSIX)
            val domStar = domF == "*"
            val dowStar = dowF == "*"
            val domMatch = matches(domF, cal.get(Calendar.DAY_OF_MONTH))
            val dowMatch = matches(dowF, cal.get(Calendar.DAY_OF_WEEK) - 1)
            val dayMatch = when {
                !domStar && !dowStar -> domMatch || dowMatch
                !domStar -> domMatch
                !dowStar -> dowMatch
                else -> true
            }
            if (!dayMatch) {
                cal.set(Calendar.HOUR_OF_DAY, 0)
                cal.set(Calendar.MINUTE, 0)
                cal.add(Calendar.DAY_OF_MONTH, 1)
                continue
            }
            // Hour
            if (!matches(hourF, cal.get(Calendar.HOUR_OF_DAY))) {
                cal.set(Calendar.MINUTE, 0)
                cal.add(Calendar.HOUR_OF_DAY, 1)
                continue
            }
            // Minute
            if (!matches(minF, cal.get(Calendar.MINUTE))) {
                cal.add(Calendar.MINUTE, 1)
                continue
            }
            return cal
        }
        return null
    }

    /** Returns human-readable labels for the next [count] executions. */
    fun describeNext(expression: String, count: Int = 3): List<String> {
        val fmt = SimpleDateFormat("EEE, d MMM HH:mm", Locale.getDefault())
        val results = mutableListOf<String>()
        var from = Calendar.getInstance()
        repeat(count) {
            val next = nextExecution(expression, from) ?: return results
            results.add(fmt.format(next.time))
            from = next
        }
        return results
    }

    // ── Internals ────────────────────────────────────────────────────────────

    private fun matches(field: String, value: Int): Boolean {
        if (field == "*") return true
        if (field.startsWith("*/")) {
            val step = field.substring(2).toIntOrNull() ?: return false
            return value % step == 0
        }
        return field.split(",").any { part ->
            if ("-" in part) {
                val (a, b) = part.split("-").map { it.toInt() }
                value in a..b
            } else {
                part.toIntOrNull() == value
            }
        }
    }

    private fun isFieldValid(field: String, range: IntRange): Boolean {
        if (field == "*") return true
        if (field.startsWith("*/")) return field.substring(2).toIntOrNull()?.let { it > 0 } ?: false
        return field.split(",").all { part ->
            if ("-" in part) {
                val parts = part.split("-")
                if (parts.size != 2) return@all false
                val a = parts[0].toIntOrNull() ?: return@all false
                val b = parts[1].toIntOrNull() ?: return@all false
                a in range && b in range && a <= b
            } else {
                (part.toIntOrNull() ?: return@all false) in range
            }
        }
    }
}
