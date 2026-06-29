package com.example.wearsnake.ui

import androidx.compose.ui.graphics.Color
import com.example.wearsnake.game.ThemeType

data class ThemeStyle(
    val themeType: ThemeType,
    val name: String,
    val backgroundColor: Color,
    val gridColor: Color,
    val obstacleColor: Color,
    val borderColor: Color,
    val portalColor: Color,
    val foodColors: Map<com.example.wearsnake.game.FoodType, Color>
)

object ThemeStyles {
    private val defaultFoodColors = mapOf(
        com.example.wearsnake.game.FoodType.APPLE to Color(0xFFFF1744),       // Neon Pink-Red
        com.example.wearsnake.game.FoodType.CHERRY to Color(0xFFFF4081),      // Neon Cherry Pink
        com.example.wearsnake.game.FoodType.BANANA to Color(0xFFFFEA00),      // Neon Gold Yellow
        com.example.wearsnake.game.FoodType.BLUEBERRY to Color(0xFF00E5FF)    // Neon Electric Cyan
    )

    private val moonFoodColors = mapOf(
        com.example.wearsnake.game.FoodType.APPLE to Color(0xFFFF8A80),       // Glowing pink
        com.example.wearsnake.game.FoodType.CHERRY to Color(0xFFEA80FC),      // Glowing violet
        com.example.wearsnake.game.FoodType.BANANA to Color(0xFFFFFF8D),      // Glowing neon yellow
        com.example.wearsnake.game.FoodType.BLUEBERRY to Color(0xFF82B1FF)    // Glowing neon blue
    )

    val themes = mapOf(
        ThemeType.GRASS to ThemeStyle(
            themeType = ThemeType.GRASS,
            name = "Grass Meadow",
            backgroundColor = Color(0xFF33691E), // Dark grass green
            gridColor = Color(0xFF558B2F),       // Lighter lawn green
            obstacleColor = Color(0xFF795548),   // Wood/Log brown
            borderColor = Color(0xFF1B5E20),     // Thick dark green edge
            portalColor = Color(0xFFAB47BC),     // Portal purple
            foodColors = defaultFoodColors
        ),
        ThemeType.FOREST to ThemeStyle(
            themeType = ThemeType.FOREST,
            name = "Deep Forest",
            backgroundColor = Color(0xFF1B5E20), // Dense pine green
            gridColor = Color(0xFF2E7D32),       // Mossy green
            obstacleColor = Color(0xFF4E342E),   // Dark pine trunk
            borderColor = Color(0xFF0D3310),     // Very dark outline
            portalColor = Color(0xFF9575CD),     // Soft violet portal
            foodColors = defaultFoodColors
        ),
        ThemeType.DESERT to ThemeStyle(
            themeType = ThemeType.DESERT,
            name = "Sizzling Desert",
            backgroundColor = Color(0xFFF57F17), // Rich sand yellow-orange
            gridColor = Color(0xFFFBC02D),       // Dusty light yellow
            obstacleColor = Color(0xFF2E7D32),   // Cactus green!
            borderColor = Color(0xFFD84315),     // Clay red border
            portalColor = Color(0xFF00E676),     // Neon green portal
            foodColors = defaultFoodColors
        ),
        ThemeType.LAKE to ThemeStyle(
            themeType = ThemeType.LAKE,
            name = "Mystic Lake",
            backgroundColor = Color(0xFF0D47A1), // Deep lake water blue
            gridColor = Color(0xFF1565C0),       // Rippling light blue
            obstacleColor = Color(0xFF607D8B),   // Wet grey rocks
            borderColor = Color(0xFF0D1B2A),     // Navy border
            portalColor = Color(0xFFFFB74D),     // Amber warm portal
            foodColors = defaultFoodColors
        ),
        ThemeType.MOON to ThemeStyle(
            themeType = ThemeType.MOON,
            name = "Lunar Crater",
            backgroundColor = Color(0xFF1A1A1A), // Lunar dark basalt
            gridColor = Color(0xFF333333),       // Slate gray dust
            obstacleColor = Color(0xFF78909C),   // Metereolite silver/grey
            borderColor = Color(0xFFE0E0E0),     // Bright silver border
            portalColor = Color(0xFF00E5FF),     // Electric cyan portal
            foodColors = moonFoodColors          // Glowing foods on moon
        ),
        ThemeType.MARS to ThemeStyle(
            themeType = ThemeType.MARS,
            name = "Mars Canyons",
            backgroundColor = Color(0xFF8C2D19), // Rusty red dust
            gridColor = Color(0xFFB84A39),       // Iron oxide red
            obstacleColor = Color(0xFF3E2723),   // Obsidian black rock
            borderColor = Color(0xFFE64A19),     // Active volcanic orange border
            portalColor = Color(0xFFFFEB3B),     // Glowing yellow portal
            foodColors = defaultFoodColors
        )
    )

    fun getStyle(themeType: ThemeType): ThemeStyle {
        return themes[themeType] ?: themes.values.first()
    }
}
