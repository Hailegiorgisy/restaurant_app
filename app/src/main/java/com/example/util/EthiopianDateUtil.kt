package com.example.util

import java.util.Calendar
import java.util.Date

/**
 * Utility for Ethiopian Calendar (የኢትዮጵያ ዘመን አቆጣጠር) and Ethiopian Time (የኢትዮጵያ ሰዓት).
 *
 * In Ethiopia:
 * 1. Time System (የሰዓት አቆጣጠር):
 *    - The 12-hour day clock starts at dawn/sunrise (Western 6:00 AM = 12:00 ጠዋት).
 *    - Morning begins at 12:00 (Western 6:00 AM).
 *    - Day ends at 12:00 (Western 6:00 PM / Sunset).
 *    - Western Hour H (0..23):
 *      If H in 6..17: Ethiopian Hour = H - 6 (if 0 -> 12)
 *      If H in 18..23: Ethiopian Hour = H - 18 (if 0 -> 12)
 *      If H in 0..5: Ethiopian Hour = H + 6
 *
 * 2. Calendar System (የቀን አቆጣጠር):
 *    - 12 months of 30 days each + 13th month Pagume (ጳጉሜ) of 5 or 6 days.
 *    - 7 to 8 years behind Gregorian calendar.
 *    - New Year (1 መስከረም) is September 11 (or September 12 in leap year).
 */
object EthiopianDateUtil {

    val ETHIOPIAN_MONTHS = listOf(
        "መስከረም",  // 1
        "ጥቅምት",   // 2
        "ኅዳር",     // 3
        "ታኅሣሥ",    // 4
        "ጥር",      // 5
        "የካቲት",    // 6
        "መጋቢት",    // 7
        "ሚያዝያ",    // 8
        "ግንቦት",    // 9
        "ሰኔ",      // 10
        "ሐምሌ",     // 11
        "ነሐሴ",     // 12
        "ጳጉሜ"     // 13
    )

    val ETHIOPIAN_DAYS_OF_WEEK = listOf(
        "እሑድ",    // 0: Sunday
        "ሰኞ",     // 1: Monday
        "ማክሰኞ",   // 2: Tuesday
        "ረቡዕ",    // 3: Wednesday
        "ሐሙስ",    // 4: Thursday
        "ዓርብ",    // 5: Friday
        "ቅዳሜ"     // 6: Saturday
    )

    data class EthiopianDate(
        val year: Int,
        val month: Int, // 1..13
        val day: Int,   // 1..30 (or 1..6)
        val dayOfWeekIndex: Int, // 0..6
        val monthName: String,
        val dayOfWeekName: String
    ) {
        val dateKey: String get() = "%04d-%02d-%02d".format(year, month, day)
        val formattedDate: String get() = "$monthName $day ቀን $year ዓ.ም"
        val fullDateWithDay: String get() = "$dayOfWeekName፣ $monthName $day ቀን $year ዓ.ም"
    }

    data class EthiopianTime(
        val hour: Int,       // 1..12
        val minute: Int,     // 0..59
        val periodAmharic: String, // ጠዋት, ቀን, ከሰዓት, ምሽት, ሌሊት, etc.
        val formattedTime: String  // e.g. "ጠዋት 12:00", "ቀን 8:30"
    )

    /**
     * Converts a millisecond timestamp to Ethiopian Date.
     */
    fun toEthiopianDate(timestamp: Long = System.currentTimeMillis()): EthiopianDate {
        val cal = Calendar.getInstance().apply { timeInMillis = timestamp }
        return gregorianToEthiopian(
            year = cal.get(Calendar.YEAR),
            month = cal.get(Calendar.MONTH) + 1, // Calendar.MONTH is 0-indexed
            day = cal.get(Calendar.DAY_OF_MONTH)
        )
    }

    /**
     * Converts a Gregorian Date to Ethiopian Date using Julian Day Number (JDN).
     */
    fun gregorianToEthiopian(year: Int, month: Int, day: Int): EthiopianDate {
        val a = (14 - month) / 12
        val y = year + 4800 - a
        val m = month + 12 * a - 3
        val jdn = day + (153 * m + 2) / 5 + 365 * y + y / 4 - y / 100 + y / 400 - 32045

        val r = (jdn - 1723856) % 1461
        val n = (r % 365) + 365 * (r / 1460)
        val ey = 4 * ((jdn - 1723856) / 1461) + (r / 365) - (r / 1460)
        val em = (n / 30) + 1
        val ed = (n % 30) + 1

        val dow = (jdn + 1) % 7 // 0 = Sunday, 1 = Monday, ..., 6 = Saturday
        val monthIdx = (em - 1).coerceIn(0, ETHIOPIAN_MONTHS.size - 1)
        val dowIdx = dow.coerceIn(0, ETHIOPIAN_DAYS_OF_WEEK.size - 1)

        return EthiopianDate(
            year = ey,
            month = em,
            day = ed,
            dayOfWeekIndex = dowIdx,
            monthName = ETHIOPIAN_MONTHS[monthIdx],
            dayOfWeekName = ETHIOPIAN_DAYS_OF_WEEK[dowIdx]
        )
    }

    /**
     * Converts a timestamp to Ethiopian Time.
     * In Ethiopia:
     * - Morning starts at 12:00 (Western 6:00 AM)
     * - Day ends at 12:00 (Western 6:00 PM)
     */
    fun toEthiopianTime(timestamp: Long = System.currentTimeMillis()): EthiopianTime {
        val cal = Calendar.getInstance().apply { timeInMillis = timestamp }
        val westernHour = cal.get(Calendar.HOUR_OF_DAY) // 0..23
        val minute = cal.get(Calendar.MINUTE)

        val ethHour = when {
            westernHour == 6 -> 12
            westernHour in 7..17 -> westernHour - 6
            westernHour == 18 -> 12
            westernHour in 19..23 -> westernHour - 18
            westernHour == 0 -> 6
            else -> westernHour + 6 // 1..5 -> 7..11
        }

        val period = when (westernHour) {
            6 -> "ጠዋት" // 6 AM = 12:00 ጠዋት (morning starts at 12!)
            in 7..10 -> "ጠዋት"
            11 -> "ረፋድ" // 11 AM = 5:00 ረፋድ
            12 -> "እኩለ ቀን" // 12 PM = 6:00 እኩለ ቀን
            in 13..16 -> "ከሰዓት" // 1 PM - 4 PM
            17 -> "ከሰዓት" // 5 PM = 11:00 ከሰዓት
            18 -> "ምሽት" // 6 PM = 12:00 ምሽት (day ends at 12!)
            in 19..22 -> "ማታ" // 7 PM - 10 PM
            23 -> "ሌሊት"
            0 -> "እኩለ ሌሊት" // 12 AM = 6:00 እኩለ ሌሊት
            else -> "ሌሊት" // 1 AM - 5 AM
        }

        val minStr = minute.toString().padStart(2, '0')
        val formatted = "$period $ethHour:$minStr"

        return EthiopianTime(
            hour = ethHour,
            minute = minute,
            periodAmharic = period,
            formattedTime = formatted
        )
    }

    /**
     * Formats both Ethiopian Date & Ethiopian Time into a readable string.
     * e.g., "ጳጉሜ 5 ቀን 2018 ዓ.ም - ቀን 8:30"
     */
    fun formatEthiopianDateTime(timestamp: Long = System.currentTimeMillis()): String {
        val ethDate = toEthiopianDate(timestamp)
        val ethTime = toEthiopianTime(timestamp)
        return "${ethDate.formattedDate} - ${ethTime.formattedTime}"
    }

    /**
     * Header string with day of week.
     * e.g., "ሐሙስ፣ ጳጉሜ 5 ቀን 2018 ዓ.ም | ቀን 8:30"
     */
    fun formatEthiopianHeader(timestamp: Long = System.currentTimeMillis()): String {
        val ethDate = toEthiopianDate(timestamp)
        val ethTime = toEthiopianTime(timestamp)
        return "${ethDate.fullDateWithDay} | ${ethTime.formattedTime}"
    }
}
