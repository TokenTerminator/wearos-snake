package com.example.wearsnake.game

import android.content.Context
import android.os.Vibrator
import android.os.VibratorManager
import android.os.Build
import android.os.VibrationEffect
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlin.random.Random

data class GridPoint(val x: Int, val y: Int) {
    operator fun plus(other: GridPoint) = GridPoint(x + other.x, y + other.y)
    operator fun minus(other: GridPoint) = GridPoint(x - other.x, y - other.y)
}

enum class Direction(val dx: Int, val dy: Int) {
    UP(0, -1),
    DOWN(0, 1),
    LEFT(-1, 0),
    RIGHT(1, 0);

    fun isOpposite(other: Direction): Boolean {
        return this.dx + other.dx == 0 && this.dy + other.dy == 0
    }
}

enum class FoodType {
    APPLE, CHERRY, BANANA, BLUEBERRY
}

enum class PowerUpType {
    SPEED, BONUS_LIFE
}

enum class Difficulty(val baseDelayMs: Long) {
    EASY(320L),
    MEDIUM(220L),
    HARD(140L)
}

enum class ThemeType {
    GRASS, FOREST, DESERT, LAKE, MOON, MARS
}

data class LevelConfig(
    val levelNumber: Int,
    val theme: ThemeType,
    val targetFoodCount: Int,
    val obstacles: Set<GridPoint>,
    val portalPair: Pair<GridPoint, GridPoint>? = null
)

enum class GameStatus {
    MENU, PLAYING, PAUSED, GAME_OVER, LEVEL_COMPLETED, VICTORY, LIFE_LOST, NEW_HIGH_SCORE
}

data class LeaderboardEntry(val initials: String, val score: Int)

data class GameState(
    val status: GameStatus = GameStatus.MENU,
    val snake: List<GridPoint> = emptyList(),
    val direction: Direction = Direction.RIGHT,
    val food: Map<GridPoint, FoodType> = emptyMap(),
    val powerUp: Pair<GridPoint, PowerUpType>? = null,
    val activePowerUp: PowerUpType? = null,
    val powerUpTicksRemaining: Int = 0,
    val score: Int = 0,
    val foodEaten: Int = 0,
    val lives: Int = 3,
    val currentLevelIndex: Int = 0,
    val difficulty: Difficulty = Difficulty.MEDIUM,
    val snakeColor: Long = 0xFF4CAF50, // Default Green
    val hasWalls: Boolean = false,
    val isCircular: Boolean = true,
    val justTeleported: Boolean = false
)

class GameEngine(private val context: Context) {
    // Grid Size
    val gridSize = 20
    private val centerCoord = 9.5f
    private val circleRadius = 9.8f // Slightly padded to fill round watch face nicely

    private val _state = MutableStateFlow(GameState())
    val state: StateFlow<GameState> = _state.asStateFlow()

    private var nextDirection = Direction.RIGHT
    private var canChangeDirection = true

    // Level configurations
    val levels = listOf(
        LevelConfig(
            levelNumber = 1,
            theme = ThemeType.GRASS,
            targetFoodCount = 5,
            obstacles = emptySet()
        ),
        LevelConfig(
            levelNumber = 2,
            theme = ThemeType.FOREST,
            targetFoodCount = 6,
            obstacles = setOf(
                GridPoint(5, 5), GridPoint(6, 5), GridPoint(7, 5),
                GridPoint(14, 14), GridPoint(13, 14), GridPoint(12, 14)
            )
        ),
        LevelConfig(
            levelNumber = 3,
            theme = ThemeType.DESERT,
            targetFoodCount = 7,
            obstacles = setOf(
                GridPoint(10, 4), GridPoint(10, 5), GridPoint(10, 6),
                GridPoint(10, 13), GridPoint(10, 14), GridPoint(10, 15),
                GridPoint(4, 10), GridPoint(5, 10)
            )
        ),
        LevelConfig(
            levelNumber = 4,
            theme = ThemeType.LAKE,
            targetFoodCount = 8,
            obstacles = setOf(
                GridPoint(3, 3), GridPoint(4, 3), GridPoint(3, 4),
                GridPoint(16, 16), GridPoint(15, 16), GridPoint(16, 15)
            ),
            portalPair = Pair(GridPoint(5, 14), GridPoint(14, 5))
        ),
        LevelConfig(
            levelNumber = 5,
            theme = ThemeType.MOON,
            targetFoodCount = 9,
            obstacles = setOf(
                GridPoint(9, 8), GridPoint(10, 8)
            ),
            portalPair = Pair(GridPoint(3, 10), GridPoint(16, 10))
        ),
        LevelConfig(
            levelNumber = 6,
            theme = ThemeType.MARS,
            targetFoodCount = 10,
            obstacles = setOf(
                GridPoint(5, 5), GridPoint(14, 5), GridPoint(5, 14), GridPoint(14, 14),
                GridPoint(9, 4), GridPoint(10, 4), GridPoint(9, 15), GridPoint(10, 15)
            ),
            portalPair = Pair(GridPoint(4, 9), GridPoint(15, 9))
        )
    )

    fun getCurrentLevelConfig(): LevelConfig {
        return levels[_state.value.currentLevelIndex.coerceIn(0, levels.size - 1)]
    }

    // Helper to check if a grid point is inside the active arena boundary
    fun isValidPoint(point: GridPoint): Boolean {
        if (point.x < 0 || point.x >= gridSize || point.y < 0 || point.y >= gridSize) {
            return false
        }
        if (_state.value.isCircular) {
            // Distance from center of grid
            val dx = point.x - centerCoord
            val dy = point.y - centerCoord
            return (dx * dx + (dy * dy)) <= (circleRadius * circleRadius)
        }
        return true
    }

    // Helper to check if a grid point is suitable for item spawning (away from circumference/walls)
    fun isValidSpawnPoint(point: GridPoint): Boolean {
        if (!isValidPoint(point)) return false
        if (_state.value.isCircular) {
            val dx = point.x - centerCoord
            val dy = point.y - centerCoord
            val spawnRadius = 8.2f // Safe radius to keep items fully clear of the edge
            return (dx * dx + (dy * dy)) <= (spawnRadius * spawnRadius)
        }
        return point.x > 0 && point.x < gridSize - 1 && point.y > 0 && point.y < gridSize - 1
    }

    fun setDifficulty(difficulty: Difficulty) {
        _state.update { it.copy(difficulty = difficulty) }
    }

    fun setSnakeColor(colorHex: Long) {
        _state.update { it.copy(snakeColor = colorHex) }
    }

    fun setHasWalls(hasWalls: Boolean) {
        _state.update { it.copy(hasWalls = hasWalls) }
    }

    fun setIsCircular(isCircular: Boolean) {
        _state.update { it.copy(isCircular = isCircular) }
    }

    fun setStartingLevel(levelIndex: Int) {
        _state.update { it.copy(currentLevelIndex = levelIndex.coerceIn(0, levels.size - 1)) }
    }

    fun updateDirection(newDir: Direction) {
        if (canChangeDirection && !newDir.isOpposite(_state.value.direction)) {
            nextDirection = newDir
            canChangeDirection = false
        }
    }

    fun togglePause() {
        _state.update {
            if (it.status == GameStatus.PLAYING) {
                it.copy(status = GameStatus.PAUSED)
            } else if (it.status == GameStatus.PAUSED) {
                it.copy(status = GameStatus.PLAYING)
            } else {
                it
            }
        }
    }

    fun startNewGame() {
        val currentLevelIndex = _state.value.currentLevelIndex
        resetSnakeAndItems(currentLevelIndex, resetScoreAndLives = true)
    }

    private fun resetSnakeAndItems(levelIndex: Int, resetScoreAndLives: Boolean) {
        val levelConfig = levels[levelIndex.coerceIn(0, levels.size - 1)]
        
        // Find a valid starting segment for the snake (3 units horizontally, if possible near center)
        var startHead = GridPoint(10, 10)
        while (!isValidPoint(startHead) || levelConfig.obstacles.contains(startHead)) {
            startHead = GridPoint(startHead.x - 1, startHead.y)
        }
        
        val startSnake = listOf(
            startHead,
            GridPoint(startHead.x - 1, startHead.y),
            GridPoint(startHead.x - 2, startHead.y)
        )
        
        nextDirection = Direction.RIGHT
        canChangeDirection = true
        
        _state.update { current ->
            current.copy(
                status = GameStatus.PLAYING,
                snake = startSnake,
                direction = Direction.RIGHT,
                food = emptyMap(),
                powerUp = null,
                activePowerUp = null,
                powerUpTicksRemaining = 0,
                score = if (resetScoreAndLives) 0 else current.score,
                foodEaten = 0,
                lives = if (resetScoreAndLives) 3 else current.lives,
                currentLevelIndex = levelIndex,
                justTeleported = false
            )
        }
        
        // Spawn initial food
        spawnFood()
    }

    fun continueAfterLifeLost() {
        resetSnakeAndItems(_state.value.currentLevelIndex, resetScoreAndLives = false)
    }

    fun getLeaderboard(): List<LeaderboardEntry> {
        val prefs = context.getSharedPreferences("wearsnake_leaderboard", Context.MODE_PRIVATE)
        val list = mutableListOf<LeaderboardEntry>()
        for (i in 1..3) {
            val initials = prefs.getString("initials_$i", when(i) {
                1 -> "AAA"
                2 -> "BBB"
                else -> "CCC"
            }) ?: ""
            val score = prefs.getInt("score_$i", when(i) {
                1 -> 150
                2 -> 100
                else -> 50
            })
            list.add(LeaderboardEntry(initials, score))
        }
        return list.sortedByDescending { it.score }
    }

    fun isHighScore(score: Int): Boolean {
        val leaders = getLeaderboard()
        if (leaders.size < 3) return true
        return score > leaders.last().score
    }

    fun saveHighScore(initials: String, score: Int) {
        val leaders = getLeaderboard().toMutableList()
        leaders.add(LeaderboardEntry(initials, score))
        val sorted = leaders.sortedByDescending { it.score }.take(3)
        val prefs = context.getSharedPreferences("wearsnake_leaderboard", Context.MODE_PRIVATE)
        prefs.edit().apply {
            sorted.forEachIndexed { index, entry ->
                putString("initials_${index + 1}", entry.initials)
                putInt("score_${index + 1}", entry.score)
            }
            apply()
        }
    }

    fun goToMenu() {
        _state.update { it.copy(status = GameStatus.MENU) }
    }

    // Main tick loop logic
    fun tick() {
        if (_state.value.status != GameStatus.PLAYING) return

        canChangeDirection = true
        var head = _state.value.snake.firstOrNull() ?: return
        val direction = nextDirection
        _state.update { it.copy(direction = direction) }

        var nextPoint = head + GridPoint(direction.dx, direction.dy)
        val hasWalls = _state.value.hasWalls
        val levelConfig = getCurrentLevelConfig()

        // 1. Check Boundary Collision
        if (!isValidPoint(nextPoint)) {
            if (hasWalls) {
                handleCrash()
                return
            } else {
                // Wrap around logic using walk-back algorithm
                var wrapPoint = head
                while (isValidPoint(wrapPoint - GridPoint(direction.dx, direction.dy))) {
                    wrapPoint = wrapPoint - GridPoint(direction.dx, direction.dy)
                }
                nextPoint = wrapPoint
            }
        }

        // 2. Check Obstacle Collision
        if (levelConfig.obstacles.contains(nextPoint)) {
            handleCrash()
            return
        }

        // 3. Check Self Collision (excluding tail if not eating, but to be safe check all body except maybe tail)
        if (_state.value.snake.contains(nextPoint) && nextPoint != _state.value.snake.last()) {
            handleCrash()
            return
        }

        // 4. Portal Teleportation
        var justTeleported = _state.value.justTeleported
        levelConfig.portalPair?.let { (portalA, portalB) ->
            if (!justTeleported) {
                if (nextPoint == portalA) {
                    nextPoint = portalB
                    justTeleported = true
                    vibrate(120)
                } else if (nextPoint == portalB) {
                    nextPoint = portalA
                    justTeleported = true
                    vibrate(120)
                }
            } else {
                // Reset portal state when we leave the portals
                if (nextPoint != portalA && nextPoint != portalB) {
                    justTeleported = false
                }
            }
        }

        // 5. Eating Food
        val foodType = _state.value.food[nextPoint]
        val newSnake = ArrayList(_state.value.snake)
        newSnake.add(0, nextPoint)

        if (foodType != null) {
            // Eaten food!
            vibrate(60)
            
            // Remove food from map
            val newFoodMap = _state.value.food.toMutableMap()
            newFoodMap.remove(nextPoint)

            // Calculate score
            val isDouble = _state.value.activePowerUp == PowerUpType.SPEED
            val pointsGained = when (foodType) {
                FoodType.APPLE -> 10
                FoodType.CHERRY -> 20
                FoodType.BANANA -> 15
                FoodType.BLUEBERRY -> 25
            } * (if (isDouble) 2 else 1)

            val newScore = _state.value.score + pointsGained
            val newFoodEaten = _state.value.foodEaten + 1

            _state.update { current ->
                current.copy(
                    snake = newSnake,
                    food = newFoodMap,
                    score = newScore,
                    foodEaten = newFoodEaten,
                    justTeleported = justTeleported
                )
            }

            // Check if level target food count is met
            if (newFoodEaten >= levelConfig.targetFoodCount) {
                handleLevelComplete()
                return
            }

            // Spawn next food
            spawnFood()
            
            // Randomly spawn powerups (15% chance when eating food if none currently exists on board)
            if (_state.value.powerUp == null && Random.nextFloat() < 0.15f) {
                spawnPowerUp()
            }
        } else {
            // Check if eating power-up
            val powerUpItem = _state.value.powerUp
            if (powerUpItem != null && nextPoint == powerUpItem.first) {
                vibrate(150)
                val type = powerUpItem.second
                
                if (type == PowerUpType.BONUS_LIFE) {
                    _state.update { current ->
                        current.copy(
                            lives = (current.lives + 1).coerceAtMost(5),
                            powerUp = null
                        )
                    }
                } else if (type == PowerUpType.SPEED) {
                    _state.update { current ->
                        current.copy(
                            activePowerUp = PowerUpType.SPEED,
                            powerUpTicksRemaining = 60, // Boost lasts for 60 ticks
                            powerUp = null
                        )
                    }
                }
            } else {
                // Normal move, shrink tail
                if (newSnake.isNotEmpty()) {
                    newSnake.removeAt(newSnake.size - 1)
                }
            }

            _state.update { current ->
                var ticks = current.powerUpTicksRemaining
                var active = current.activePowerUp
                if (active != null) {
                    ticks--
                    if (ticks <= 0) {
                        active = null
                    }
                }

                current.copy(
                    snake = newSnake,
                    justTeleported = justTeleported,
                    activePowerUp = active,
                    powerUpTicksRemaining = ticks
                )
            }
        }
    }

    private fun handleCrash() {
        vibrate(listOf(0L, 100L, 80L, 100L)) // double vibrate
        val remainingLives = _state.value.lives - 1
        if (remainingLives <= 0) {
            val score = _state.value.score
            if (isHighScore(score)) {
                _state.update { it.copy(status = GameStatus.NEW_HIGH_SCORE, lives = 0) }
            } else {
                _state.update { it.copy(status = GameStatus.GAME_OVER, lives = 0) }
            }
        } else {
            _state.update { it.copy(lives = remainingLives, status = GameStatus.LIFE_LOST) }
        }
    }

    private fun handleLevelComplete() {
        vibrate(listOf(0L, 100L, 50L, 100L, 50L, 250L)) // triumph vibration
        val nextLevel = _state.value.currentLevelIndex + 1
        if (nextLevel >= levels.size) {
            val score = _state.value.score
            if (isHighScore(score)) {
                _state.update { it.copy(status = GameStatus.NEW_HIGH_SCORE) }
            } else {
                _state.update { it.copy(status = GameStatus.VICTORY) }
            }
        } else {
            _state.update { it.copy(status = GameStatus.LEVEL_COMPLETED) }
        }
    }

    fun advanceToNextLevel() {
        val nextLevel = _state.value.currentLevelIndex + 1
        if (nextLevel < levels.size) {
            resetSnakeAndItems(nextLevel, resetScoreAndLives = false)
        } else {
            val score = _state.value.score
            if (isHighScore(score)) {
                _state.update { it.copy(status = GameStatus.NEW_HIGH_SCORE) }
            } else {
                _state.update { it.copy(status = GameStatus.VICTORY) }
            }
        }
    }

    private fun spawnFood() {
        val levelConfig = getCurrentLevelConfig()
        val invalidPoints = HashSet<GridPoint>().apply {
            addAll(_state.value.snake)
            addAll(levelConfig.obstacles)
            levelConfig.portalPair?.let {
                add(it.first)
                add(it.second)
            }
            addAll(_state.value.food.keys)
            _state.value.powerUp?.let { add(it.first) }
        }

        val candidates = mutableListOf<GridPoint>()
        for (x in 0 until gridSize) {
            for (y in 0 until gridSize) {
                val pt = GridPoint(x, y)
                if (isValidSpawnPoint(pt) && !invalidPoints.contains(pt)) {
                    candidates.add(pt)
                }
            }
        }

        if (candidates.isNotEmpty()) {
            val randomPoint = candidates[Random.nextInt(candidates.size)]
            val randomFoodType = FoodType.values()[Random.nextInt(FoodType.values().size)]
            val newFoodMap = _state.value.food.toMutableMap()
            newFoodMap[randomPoint] = randomFoodType
            _state.update { it.copy(food = newFoodMap) }
        }
    }

    private fun spawnPowerUp() {
        val levelConfig = getCurrentLevelConfig()
        val invalidPoints = HashSet<GridPoint>().apply {
            addAll(_state.value.snake)
            addAll(levelConfig.obstacles)
            levelConfig.portalPair?.let {
                add(it.first)
                add(it.second)
            }
            addAll(_state.value.food.keys)
        }

        val candidates = mutableListOf<GridPoint>()
        for (x in 0 until gridSize) {
            for (y in 0 until gridSize) {
                val pt = GridPoint(x, y)
                if (isValidSpawnPoint(pt) && !invalidPoints.contains(pt)) {
                    candidates.add(pt)
                }
            }
        }

        if (candidates.isNotEmpty()) {
            val randomPoint = candidates[Random.nextInt(candidates.size)]
            // Speed powerup is more common than extra life
            val type = if (Random.nextFloat() < 0.7f) PowerUpType.SPEED else PowerUpType.BONUS_LIFE
            _state.update { it.copy(powerUp = Pair(randomPoint, type)) }
        }
    }

    // Native vibration wrapper
    private fun vibrate(durationMs: Long) {
        val vibrator = getVibratorService()
        if (vibrator != null && vibrator.hasVibrator()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator.vibrate(VibrationEffect.createOneShot(durationMs, VibrationEffect.DEFAULT_AMPLITUDE))
            } else {
                @Suppress("DEPRECATION")
                vibrator.vibrate(durationMs)
            }
        }
    }

    private fun vibrate(pattern: List<Long>) {
        val vibrator = getVibratorService()
        if (vibrator != null && vibrator.hasVibrator()) {
            val longArray = pattern.toLongArray()
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator.vibrate(VibrationEffect.createWaveform(longArray, -1))
            } else {
                @Suppress("DEPRECATION")
                vibrator.vibrate(longArray, -1)
            }
        }
    }

    private fun getVibratorService(): Vibrator? {
        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
                vibratorManager?.defaultVibrator
            } else {
                @Suppress("DEPRECATION")
                context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
            }
        } catch (e: Exception) {
            null
        }
    }
}
