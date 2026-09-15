package com.example.model

enum class PrayerCategory(val titleFa: String, val iconName: String) {
    DUA("ادعیه مشهور", "dua"),
    ZIYARAT("زیارات", "ziyarat"),
    SURAH("سوره‌های منتخب", "quran"),
    MUNAJAT("مناجات و اعمال", "munajat"),
    SALAWAT("صلوات و تعقیبات", "salawat")
}

data class PrayerVerse(
    val id: Int,
    val arabic: String,
    val persian: String,
    val note: String? = null,
    val targetRepeats: Int = 1 // e.g., 100x for Ziyarat Ashura Lan or Salam
)

data class PrayerItem(
    val id: String,
    val title: String,
    val category: PrayerCategory,
    val subtitle: String,
    val virtue: String,
    val recommendedTime: String,
    val verses: List<PrayerVerse>,
    val isFavorite: Boolean = false,
    val isSpecial: Boolean = false
)

enum class LightShapeMode(val titleFa: String, val description: String) {
    MANDALA_STAR("شمس و اسلیمی", "ستاره ۸ و ۱۲ پر قدسی با پرتوهای دورانی"),
    CRESCENT_GALAXY("هلال و کهکشان", "هلال ماه نورانی با ذرات ستاره‌ای معلق"),
    SACRED_GEOMETRY("هندسه نئونی", "امواج چندضلعی متحدالمرکز تپنده"),
    AURORA_WAVES("امواج آرورا", "خطوط نئونی مواج سیال و اکولایزر نور"),
    KALEIDOSCOPE_LOTUS("کالدوسکوپ نیلوفر", "شکوفایی گلبرگ‌های منشور رنگین‌کمان"),
    DISCO_STROBE("لیزر و دیسکو", "فلاش‌های ریتمیک پرانرژی و پرتوهای متقاطع"),
    PULSE_HEART("ضربان هاله نور", "تپش ریتمیک قلب نورانی با موج هاله")
}

enum class LightPalette(val titleFa: String) {
    SPIRITUAL_GOLD("زمرد و طلای قدسی"),
    RAINBOW_NEON("نئون رنگین‌کمان"),
    CYAN_AURORA("فیروزه‌ای آسمانی"),
    COSMIC_VIOLET("ارغوانی کهکشانی"),
    WARM_AMBER("طلایی آتشین"),
    DIAMOND_WHITE("سفید الماسی")
}

data class LightShowSettings(
    val shapeMode: LightShapeMode = LightShapeMode.MANDALA_STAR,
    val palette: LightPalette = LightPalette.SPIRITUAL_GOLD,
    val speedBpm: Float = 60f, // 30 to 180 BPM
    val isFlashlightEnabled: Boolean = false,
    val isVibrationEnabled: Boolean = true,
    val isSoundPulsing: Boolean = false,
    val brightnessMultiplier: Float = 1.0f,
    val isPlaying: Boolean = true
)

enum class BorderLightShape(val titleFa: String, val description: String) {
    FLOWING_NEON("پرتو نئونی گردان", "پرتو نورانی چرخشی با دنباله رنگین‌کمانی دور لبه‌های صفحه"),
    MANDALA_CORNERS("شمس‌های چهارگوشه", "ستاره‌های اسلیمی در ۴ گوشه با اضلاع نئونی تپنده"),
    RUNNING_PEARLS("تسبیح نورانی محیطی", "دانه‌های نورانی تسبیح‌مانند در حال گردش دور کادر"),
    AURORA_FRAME("هاله تنفسی آرورا", "امواج نوری نرم با پالس و انبساط دور تا دور صفحه"),
    CRESCENT_GEM("هلال و نگین‌های درخشان", "هلال ماه و نگین‌های الماس در گردش در امتداد حاشیه"),
    DISCO_STROBE_BORDER("چشمک‌زن دیسکو دور صفحه", "رقص نور چندشکلی پرهیجان در کادر حاشیه‌ای")
}

data class BorderLightSettings(
    val isEnabled: Boolean = false,
    val shape: BorderLightShape = BorderLightShape.FLOWING_NEON,
    val palette: LightPalette = LightPalette.RAINBOW_NEON,
    val speedBpm: Float = 65f,
    val strokeWidthDp: Float = 8f,
    val isGlobalEnabled: Boolean = false
)

