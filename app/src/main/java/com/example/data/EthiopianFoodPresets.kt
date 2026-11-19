package com.example.data

data class PresetFood(
    val nameAm: String,
    val defaultPrice: Double = 0.0,
    val category: String = "ምግብ"
) {
    val nameEn: String get() = nameAm
}

object EthiopianFoodPresets {
    val list = listOf(
        PresetFood("ልዩ ጥብስ", 500.0, "ምግብ"),
        PresetFood("የሸክላ ጥብስ", 450.0, "ምግብ"),
        PresetFood("ልዩ ክትፎ", 550.0, "ምግብ"),
        PresetFood("የዶሮ ወጥ", 600.0, "ምግብ"),
        PresetFood("ጎረድ ጎረድ", 500.0, "ምግብ"),
        PresetFood("ሽሮ ተጋቢኖ", 200.0, "ምግብ"),
        PresetFood("የጾም በያይነቱ", 250.0, "ምግብ"),
        PresetFood("ቋንጣ ፍርፍር", 350.0, "ምግብ"),
        PresetFood("ዱለት", 300.0, "ምግብ"),
        PresetFood("ቢራ", 100.0, "መጠጥ"),
        PresetFood("ለስላሳ", 50.0, "መጠጥ"),
        PresetFood("የታሸገ ውሃ", 40.0, "መጠጥ"),
        PresetFood("ማኪያቶ", 60.0, "መጠጥ"),
        PresetFood("የጀበና ቡና", 35.0, "መጠጥ"),
        PresetFood("ሻይ", 25.0, "መጠጥ"),
        PresetFood("ስፕሪስ ጁስ", 120.0, "መጠጥ")
    )
}
