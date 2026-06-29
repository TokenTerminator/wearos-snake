package com.example.wearsnake.ui

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.wearsnake.game.*

// Vibrant custom color choices for the snake
val SNAKE_COLORS = listOf(
    Pair(0xFF4CAF50, "Green"),
    Pair(0xFFE53935, "Red"),
    Pair(0xFF00ACC1, "Cyan"),
    Pair(0xFFFFB300, "Yellow"),
    Pair(0xFF8E24AA, "Violet"),
    Pair(0xFFF4511E, "Orange")
)

@Composable
fun MainMenuScreen(
    state: GameState,
    onStartGame: () -> Unit,
    onOpenSettings: () -> Unit,
    onOpenLevelSelect: () -> Unit,
    onOpenLeaderboard: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Title breathing animation
    val infiniteTransition = rememberInfiniteTransition(label = "titleBreath")
    val titleScale by infiniteTransition.animateFloat(
        initialValue = 0.96f,
        targetValue = 1.04f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "titleScale"
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.radialGradient(
                    colors = listOf(Color(0xFF2C3E50), Color(0xFF0F2027)),
                    radius = 350f
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            // Breathing Title
            Text(
                text = "WATCH SNAKE",
                fontSize = 17.sp,
                fontWeight = FontWeight.Black,
                color = Color(state.snakeColor),
                letterSpacing = 2.sp,
                modifier = Modifier
                    .padding(bottom = 6.dp)
                    .graphicsLayer {
                        scaleX = titleScale
                        scaleY = titleScale
                    }
            )

            // Play Button
            Button(
                onClick = onStartGame,
                colors = ButtonDefaults.buttonColors(containerColor = Color(state.snakeColor)),
                contentPadding = PaddingValues(horizontal = 24.dp, vertical = 4.dp),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier
                    .fillMaxWidth(0.75f)
                    .height(32.dp)
            ) {
                Text("PLAY", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.Black)
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Level Select Button
            Button(
                onClick = onOpenLevelSelect,
                colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(alpha = 0.15f)),
                contentPadding = PaddingValues(vertical = 4.dp),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier
                    .fillMaxWidth(0.75f)
                    .height(30.dp)
            ) {
                Text("LEVELS", fontSize = 10.sp, fontWeight = FontWeight.SemiBold, color = Color.White)
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Leaderboard Button
            Button(
                onClick = onOpenLeaderboard,
                colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(alpha = 0.15f)),
                contentPadding = PaddingValues(vertical = 4.dp),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier
                    .fillMaxWidth(0.75f)
                    .height(30.dp)
            ) {
                Text("LEADERBOARD", fontSize = 10.sp, fontWeight = FontWeight.SemiBold, color = Color.White)
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Settings Button
            Button(
                onClick = onOpenSettings,
                colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(alpha = 0.15f)),
                contentPadding = PaddingValues(vertical = 4.dp),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier
                    .fillMaxWidth(0.75f)
                    .height(30.dp)
            ) {
                Text("SETTINGS", fontSize = 10.sp, fontWeight = FontWeight.SemiBold, color = Color.White)
            }
        }
    }
}

@Composable
fun SettingsScreen(
    state: GameState,
    onDifficultyChanged: (Difficulty) -> Unit,
    onSnakeColorChanged: (Long) -> Unit,
    onWallsToggle: (Boolean) -> Unit,
    onIsCircularToggle: (Boolean) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF0F2027))
            .padding(horizontal = 16.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(vertical = 24.dp)
        ) {
            Text(
                text = "SETTINGS",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White.copy(alpha = 0.7f),
                modifier = Modifier.padding(bottom = 12.dp)
            )

            // 1. Difficulty Level Selector
            Text("DIFFICULTY", fontSize = 10.sp, color = Color.Gray)
            Row(
                modifier = Modifier.padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Difficulty.values().forEach { diff ->
                    val isSelected = state.difficulty == diff
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (isSelected) Color(state.snakeColor) else Color.White.copy(alpha = 0.1f))
                            .clickable { onDifficultyChanged(diff) }
                            .padding(horizontal = 10.dp, vertical = 5.dp)
                    ) {
                        Text(
                            text = diff.name,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isSelected) Color.Black else Color.White
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // 2. Snake Color Picker
            Text("SNAKE COLOR", fontSize = 10.sp, color = Color.Gray)
            Row(
                modifier = Modifier.padding(vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                SNAKE_COLORS.forEach { (colorHex, _) ->
                    val isSelected = state.snakeColor == colorHex
                    Box(
                        modifier = Modifier
                            .size(20.dp)
                            .clip(CircleShape)
                            .background(Color(colorHex))
                            .clickable { onSnakeColorChanged(colorHex) }
                            .padding(2.dp)
                    ) {
                        if (isSelected) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clip(CircleShape)
                                    .background(Color.White.copy(alpha = 0.4f))
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // 3. Walls/Wrap setting
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier
                    .fillMaxWidth(0.85f)
                    .padding(vertical = 2.dp)
            ) {
                Text("WALL COLLISION", fontSize = 10.sp, color = Color.White)
                Switch(
                    checked = state.hasWalls,
                    onCheckedChange = onWallsToggle,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color(state.snakeColor),
                        checkedTrackColor = Color(state.snakeColor).copy(alpha = 0.5f)
                    ),
                    modifier = Modifier.scale(0.75f)
                )
            }

            // 4. Circular screen settings
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier
                    .fillMaxWidth(0.85f)
                    .padding(vertical = 2.dp)
            ) {
                Text("ROUND ARENA", fontSize = 10.sp, color = Color.White)
                Switch(
                    checked = state.isCircular,
                    onCheckedChange = onIsCircularToggle,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color(state.snakeColor),
                        checkedTrackColor = Color(state.snakeColor).copy(alpha = 0.5f)
                    ),
                    modifier = Modifier.scale(0.75f)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Back Button
            Button(
                onClick = onBack,
                colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(alpha = 0.2f)),
                contentPadding = PaddingValues(horizontal = 18.dp, vertical = 4.dp),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.height(30.dp)
            ) {
                Text("BACK", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.White)
            }
        }
    }
}

@Composable
fun LevelSelectScreen(
    state: GameState,
    gameEngine: GameEngine,
    onLevelSelected: (Int) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF0F2027))
            .padding(horizontal = 16.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(vertical = 24.dp)
        ) {
            Text(
                text = "STARTING LEVEL",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White.copy(alpha = 0.7f),
                modifier = Modifier.padding(bottom = 12.dp)
            )

            // List of levels
            gameEngine.levels.forEachIndexed { index, config ->
                val isSelected = state.currentLevelIndex == index
                val themeStyle = ThemeStyles.getStyle(config.theme)

                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.85f)
                        .padding(vertical = 3.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            if (isSelected) Color(state.snakeColor)
                            else themeStyle.backgroundColor.copy(alpha = 0.3f)
                        )
                        .clickable { onLevelSelected(index) }
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Lvl ${config.levelNumber}: ${themeStyle.name}",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isSelected) Color.Black else Color.White
                            )
                            Text(
                                text = "Target: ${config.targetFoodCount} food items",
                                fontSize = 8.sp,
                                color = if (isSelected) Color.Black.copy(alpha = 0.7f) else Color.Gray
                            )
                        }
                        if (config.portalPair != null) {
                            Text(
                                text = "🌀",
                                fontSize = 10.sp
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Back Button
            Button(
                onClick = onBack,
                colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(alpha = 0.2f)),
                contentPadding = PaddingValues(horizontal = 18.dp, vertical = 4.dp),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.height(30.dp)
            ) {
                Text("BACK", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.White)
            }
        }
    }
}

@Composable
fun GameOverScreen(
    state: GameState,
    onRetry: () -> Unit,
    onMenu: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF2C0B0E)), // Reddish dark background
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "FINAL SCORE",
                fontSize = 20.sp,
                fontWeight = FontWeight.Black,
                color = Color(state.snakeColor),
                letterSpacing = 1.sp
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "${state.score} POINTS",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )

            Spacer(modifier = Modifier.height(14.dp))

            Button(
                onClick = onRetry,
                colors = ButtonDefaults.buttonColors(containerColor = Color(state.snakeColor)),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth(0.75f)
                    .height(34.dp)
            ) {
                Text("RETRY", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.Black)
            }

            Spacer(modifier = Modifier.height(6.dp))

            Button(
                onClick = onMenu,
                colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(alpha = 0.15f)),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth(0.75f)
                    .height(32.dp)
            ) {
                Text("MAIN SCREEN", fontSize = 10.sp, fontWeight = FontWeight.SemiBold, color = Color.White)
            }
        }
    }
}

@Composable
fun LevelCompletedScreen(
    state: GameState,
    gameEngine: GameEngine,
    onNextLevel: () -> Unit,
    modifier: Modifier = Modifier
) {
    val themeStyle = ThemeStyles.getStyle(gameEngine.getCurrentLevelConfig().theme)

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF0F2027)),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "LEVEL COMPLETE!",
                fontSize = 16.sp,
                fontWeight = FontWeight.Black,
                color = themeStyle.portalColor,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "Score: ${state.score}",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )

            Spacer(modifier = Modifier.height(14.dp))

            Button(
                onClick = onNextLevel,
                colors = ButtonDefaults.buttonColors(containerColor = themeStyle.portalColor),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth(0.75f)
                    .height(34.dp)
            ) {
                Text("NEXT LEVEL", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.Black)
            }
        }
    }
}

@Composable
fun VictoryScreen(
    state: GameState,
    onPlayAgain: () -> Unit,
    onMenu: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Rainbow colors for text pulse
    val infiniteTransition = rememberInfiniteTransition(label = "rainbow")
    val hueShift by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "hue"
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.radialGradient(
                    colors = listOf(Color(0xFF1B4F72), Color(0xFF0D1B2A)),
                    radius = 350f
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "🏆 VICTORY! 🏆",
                fontSize = 18.sp,
                fontWeight = FontWeight.Black,
                color = Color.Yellow,
                letterSpacing = 1.sp,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "You beat the game!",
                fontSize = 11.sp,
                color = Color.White.copy(alpha = 0.8f)
            )

            Text(
                text = "Final Score: ${state.score}",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                modifier = Modifier.padding(vertical = 4.dp)
            )

            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = onPlayAgain,
                colors = ButtonDefaults.buttonColors(containerColor = Color(state.snakeColor)),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth(0.7f)
                    .height(34.dp)
            ) {
                Text("PLAY AGAIN", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.Black)
            }

            Spacer(modifier = Modifier.height(6.dp))

            Button(
                onClick = onMenu,
                colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(alpha = 0.15f)),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth(0.7f)
                    .height(32.dp)
            ) {
                Text("MAIN MENU", fontSize = 9.sp, fontWeight = FontWeight.SemiBold, color = Color.White)
            }
        }
    }
}

@Composable
fun LifeLostScreen(
    state: GameState,
    gameEngine: GameEngine,
    onContinue: () -> Unit,
    modifier: Modifier = Modifier
) {
    val themeStyle = ThemeStyles.getStyle(gameEngine.getCurrentLevelConfig().theme)

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF252525).copy(alpha = 0.75f)) // Slight gray tint overlay
            .clickable { onContinue() },
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(12.dp)
        ) {
            Text(
                text = "LIFE LOST!",
                fontSize = 18.sp,
                fontWeight = FontWeight.Black,
                color = Color(0xFFE53935),
                letterSpacing = 1.sp
            )

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = "${themeStyle.name} (Lvl ${state.currentLevelIndex + 1})",
                fontSize = 10.sp,
                color = Color.White.copy(alpha = 0.7f),
                fontWeight = FontWeight.SemiBold
            )

            Text(
                text = "Score: ${state.score}",
                fontSize = 12.sp,
                color = Color.White,
                fontWeight = FontWeight.Bold
            )

            Row(
                modifier = Modifier.padding(vertical = 6.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Remaining hearts
                repeat(state.lives) {
                    Text(
                        text = "♥",
                        color = Color(0xFFF44336),
                        fontSize = 22.sp,
                        modifier = Modifier.padding(horizontal = 2.dp)
                    )
                }
                // Faded heart spent
                Text(
                    text = "♥",
                    color = Color.White.copy(alpha = 0.2f),
                    fontSize = 22.sp,
                    modifier = Modifier.padding(horizontal = 2.dp)
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "Tap to resume",
                fontSize = 9.sp,
                color = Color.White.copy(alpha = 0.5f),
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
fun NewHighScoreScreen(
    state: GameState,
    onSave: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var c1 by remember { mutableStateOf('A') }
    var c2 by remember { mutableStateOf('A') }
    var c3 by remember { mutableStateOf('A') }

    fun nextChar(c: Char): Char = if (c == 'Z') 'A' else c + 1
    fun prevChar(c: Char): Char = if (c == 'A') 'Z' else c - 1

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF0F2027)),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(12.dp)
        ) {
            Text(
                text = "NEW HIGH SCORE!",
                fontSize = 13.sp,
                fontWeight = FontWeight.Black,
                color = Color(0xFFFFB300), // Gold
                textAlign = TextAlign.Center
            )

            Text(
                text = "${state.score} pts",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                modifier = Modifier.padding(bottom = 4.dp)
            )

            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(vertical = 4.dp)
            ) {
                // Character 1
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "▲",
                        fontSize = 12.sp,
                        color = Color(state.snakeColor),
                        modifier = Modifier
                            .clickable { c1 = nextChar(c1) }
                            .padding(4.dp)
                    )
                    Text(
                        text = c1.toString(),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = "▼",
                        fontSize = 12.sp,
                        color = Color(state.snakeColor),
                        modifier = Modifier
                            .clickable { c1 = prevChar(c1) }
                            .padding(4.dp)
                    )
                }

                // Character 2
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "▲",
                        fontSize = 12.sp,
                        color = Color(state.snakeColor),
                        modifier = Modifier
                            .clickable { c2 = nextChar(c2) }
                            .padding(4.dp)
                    )
                    Text(
                        text = c2.toString(),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = "▼",
                        fontSize = 12.sp,
                        color = Color(state.snakeColor),
                        modifier = Modifier
                            .clickable { c2 = prevChar(c2) }
                            .padding(4.dp)
                    )
                }

                // Character 3
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "▲",
                        fontSize = 12.sp,
                        color = Color(state.snakeColor),
                        modifier = Modifier
                            .clickable { c3 = nextChar(c3) }
                            .padding(4.dp)
                    )
                    Text(
                        text = c3.toString(),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = "▼",
                        fontSize = 12.sp,
                        color = Color(state.snakeColor),
                        modifier = Modifier
                            .clickable { c3 = prevChar(c3) }
                            .padding(4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            Button(
                onClick = { onSave("$c1$c2$c3") },
                colors = ButtonDefaults.buttonColors(containerColor = Color(state.snakeColor)),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 2.dp),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth(0.55f)
                    .height(28.dp)
            ) {
                Text(
                    text = "SAVE",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
            }
        }
    }
}

@Composable
fun LeaderboardScreen(
    leaderboard: List<LeaderboardEntry>,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF0F2027))
            .padding(horizontal = 16.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(vertical = 24.dp)
        ) {
            Text(
                text = "LEADERBOARD",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Yellow,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            // Top 3 Entries
            leaderboard.forEachIndexed { index, entry ->
                val medal = when (index) {
                    0 -> "🥇"
                    1 -> "🥈"
                    else -> "🥉"
                }
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.08f)),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .fillMaxWidth(0.85f)
                        .padding(vertical = 2.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp, vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(text = medal, fontSize = 12.sp)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = entry.initials,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                        Text(
                            text = "${entry.score} pts",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.White.copy(alpha = 0.8f)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Back Button
            Button(
                onClick = onBack,
                colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(alpha = 0.2f)),
                contentPadding = PaddingValues(horizontal = 18.dp, vertical = 4.dp),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.height(30.dp)
            ) {
                Text("BACK", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.White)
            }
        }
    }
}

// Extension to scale modifiers
private fun Modifier.scale(scale: Float): Modifier = this.graphicsLayer {
    scaleX = scale
    scaleY = scale
}
