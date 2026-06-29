package com.example.wearsnake

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.example.wearsnake.game.Difficulty
import com.example.wearsnake.game.GameEngine
import com.example.wearsnake.game.GameStatus
import com.example.wearsnake.theme.WearSnakeTheme
import com.example.wearsnake.ui.*

enum class AppScreen {
    MAIN_MENU, SETTINGS, LEVEL_SELECT, GAME, LEADERBOARD
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            WearSnakeTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val isRound = androidx.compose.ui.platform.LocalConfiguration.current.isScreenRound
                    val gameEngine = remember { 
                        GameEngine(applicationContext).apply {
                            setIsCircular(isRound)
                        }
                    }
                    AppNavigator(gameEngine = gameEngine)
                }
            }
        }
    }
}

@Composable
fun AppNavigator(gameEngine: GameEngine) {
    var currentScreen by remember { mutableStateOf(AppScreen.MAIN_MENU) }
    val state by gameEngine.state.collectAsState()

    // Handle back press to navigate back to menu instead of exiting
    if (currentScreen != AppScreen.MAIN_MENU) {
        BackHandler {
            gameEngine.goToMenu()
            currentScreen = AppScreen.MAIN_MENU
        }
    }

    when (currentScreen) {
        AppScreen.MAIN_MENU -> {
            MainMenuScreen(
                state = state,
                onStartGame = {
                    gameEngine.startNewGame()
                    currentScreen = AppScreen.GAME
                },
                onOpenSettings = {
                    currentScreen = AppScreen.SETTINGS
                },
                onOpenLevelSelect = {
                    currentScreen = AppScreen.LEVEL_SELECT
                },
                onOpenLeaderboard = {
                    currentScreen = AppScreen.LEADERBOARD
                }
            )
        }
        AppScreen.LEADERBOARD -> {
            LeaderboardScreen(
                leaderboard = gameEngine.getLeaderboard(),
                onBack = { currentScreen = AppScreen.MAIN_MENU }
            )
        }
        AppScreen.SETTINGS -> {
            SettingsScreen(
                state = state,
                onDifficultyChanged = { gameEngine.setDifficulty(it) },
                onSnakeColorChanged = { gameEngine.setSnakeColor(it) },
                onWallsToggle = { gameEngine.setHasWalls(it) },
                onIsCircularToggle = { gameEngine.setIsCircular(it) },
                onBack = { currentScreen = AppScreen.MAIN_MENU }
            )
        }
        AppScreen.LEVEL_SELECT -> {
            LevelSelectScreen(
                state = state,
                gameEngine = gameEngine,
                onLevelSelected = { levelIndex ->
                    gameEngine.setStartingLevel(levelIndex)
                    gameEngine.startNewGame()
                    currentScreen = AppScreen.GAME
                },
                onBack = { currentScreen = AppScreen.MAIN_MENU }
            )
        }
        AppScreen.GAME -> {
            when (state.status) {
                GameStatus.MENU -> {
                    currentScreen = AppScreen.MAIN_MENU
                }
                GameStatus.PLAYING, GameStatus.PAUSED, GameStatus.LIFE_LOST -> {
                    GameScreen(
                        gameEngine = gameEngine,
                        onNavigateBack = {
                            gameEngine.goToMenu()
                            currentScreen = AppScreen.MAIN_MENU
                        }
                    )
                }
                GameStatus.NEW_HIGH_SCORE -> {
                    NewHighScoreScreen(
                        state = state,
                        onSave = { initials ->
                            gameEngine.saveHighScore(initials, state.score)
                            gameEngine.goToMenu()
                            currentScreen = AppScreen.LEADERBOARD
                        }
                    )
                }
                GameStatus.GAME_OVER -> {
                    GameOverScreen(
                        state = state,
                        onRetry = {
                            gameEngine.startNewGame()
                        },
                        onMenu = {
                            gameEngine.goToMenu()
                            currentScreen = AppScreen.MAIN_MENU
                        }
                    )
                }
                GameStatus.LEVEL_COMPLETED -> {
                    LevelCompletedScreen(
                        state = state,
                        gameEngine = gameEngine,
                        onNextLevel = {
                            gameEngine.advanceToNextLevel()
                        }
                    )
                }
                GameStatus.VICTORY -> {
                    VictoryScreen(
                        state = state,
                        onPlayAgain = {
                            gameEngine.setStartingLevel(0)
                            gameEngine.startNewGame()
                        },
                        onMenu = {
                            gameEngine.goToMenu()
                            currentScreen = AppScreen.MAIN_MENU
                        }
                    )
                }
            }
        }
    }
}
