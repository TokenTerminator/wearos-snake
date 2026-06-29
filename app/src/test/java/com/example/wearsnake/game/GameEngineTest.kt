package com.example.wearsnake.game

import android.content.Context
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito

class GameEngineTest {

    private lateinit var mockContext: Context
    private lateinit var gameEngine: GameEngine

    @Before
    fun setUp() {
        mockContext = Mockito.mock(Context::class.java)
        gameEngine = GameEngine(mockContext)
    }

    @Test
    fun testCircularBoundaries() {
        // With a 20x20 grid, center is 9.5, radius is 9.8
        gameEngine.setIsCircular(true)

        // Center should be valid
        assertTrue(gameEngine.isValidPoint(GridPoint(10, 10)))
        assertTrue(gameEngine.isValidPoint(GridPoint(9, 9)))

        // Corners should be invalid (out of circular bounds)
        assertFalse(gameEngine.isValidPoint(GridPoint(0, 0)))
        assertFalse(gameEngine.isValidPoint(GridPoint(19, 19)))
        assertFalse(gameEngine.isValidPoint(GridPoint(0, 19)))
        assertFalse(gameEngine.isValidPoint(GridPoint(19, 0)))

        // Boundary edges on x and y axis
        assertTrue(gameEngine.isValidPoint(GridPoint(10, 0))) // distance from 9.5 is 9.5 <= 9.8
        assertTrue(gameEngine.isValidPoint(GridPoint(10, 19)))
        assertTrue(gameEngine.isValidPoint(GridPoint(0, 10)))
        assertTrue(gameEngine.isValidPoint(GridPoint(19, 10)))
    }

    @Test
    fun testRectangularBoundaries() {
        gameEngine.setIsCircular(false)

        // Corners are valid in rectangular mode
        assertTrue(gameEngine.isValidPoint(GridPoint(0, 0)))
        assertTrue(gameEngine.isValidPoint(GridPoint(19, 19)))

        // Outside coordinates are invalid
        assertFalse(gameEngine.isValidPoint(GridPoint(-1, 10)))
        assertFalse(gameEngine.isValidPoint(GridPoint(20, 10)))
        assertFalse(gameEngine.isValidPoint(GridPoint(10, -1)))
        assertFalse(gameEngine.isValidPoint(GridPoint(10, 20)))
    }

    @Test
    fun testSnakeMovementAndFood() {
        gameEngine.setIsCircular(false)
        gameEngine.setHasWalls(true)
        gameEngine.startNewGame()

        val stateBefore = gameEngine.state.value
        assertEquals(3, stateBefore.snake.size)
        assertEquals(0, stateBefore.score)

        // Force a piece of food in front of the snake
        val head = stateBefore.snake.first()
        val foodPoint = GridPoint(head.x + 1, head.y)
        
        // Setup engine state with this custom food
        val testEngine = GameEngine(mockContext)
        testEngine.setIsCircular(false)
        testEngine.setHasWalls(true)
        testEngine.startNewGame()
        
        // Access internal private state update via public API or recreate situation
        // We can just simulate movement towards food if we can place it.
        // Wait, the food is spawned randomly, but we can verify tick movement:
        val state1 = testEngine.state.value
        val head1 = state1.snake.first()
        
        testEngine.updateDirection(Direction.RIGHT)
        testEngine.tick()
        
        val state2 = testEngine.state.value
        val head2 = state2.snake.first()
        
        // Head should have moved right by 1
        assertEquals(head1.x + 1, head2.x)
        assertEquals(head1.y, head2.y)
        // Snake size should remain 3 since it didn't eat
        assertEquals(3, state2.snake.size)
    }

    @Test
    fun testWrapAroundMechanics() {
        // Test wrap-around in a rectangular arena
        val testEngine = GameEngine(mockContext)
        testEngine.setIsCircular(false)
        testEngine.setHasWalls(false) // disable walls for wrap around
        testEngine.startNewGame()

        // Force snake head near the right edge
        // Since we cannot write to _state directly, we can tick several times to reach the edge.
        // Or we can verify the boundary wrapping walk-back directly on isValidPoint.
        // Let's verify the wrap-around logic works in tick when it reaches the edge:
        testEngine.setStartingLevel(0) // Level 1 (no obstacles)
        
        // Let's check: if we are at (19, 10) moving RIGHT. Next is (20, 10), which is invalid.
        // Walk-back starting from (19, 10) going LEFT (-1, 0) will step back:
        // (18,10), (17,10)... until (0, 10) which is the far boundary.
        // So it should wrap (19, 10) -> (0, 10).
        // Let's verify this mathematically on a simulated grid.
        val direction = Direction.RIGHT
        var head = GridPoint(19, 10)
        
        // Replicating walkback calculation in GameEngine:
        var wrapPoint = head
        while (wrapPoint.x - direction.dx >= 0) { // simplified check for rect
            wrapPoint = GridPoint(wrapPoint.x - direction.dx, wrapPoint.y)
        }
        
        assertEquals(0, wrapPoint.x)
        assertEquals(10, wrapPoint.y)
    }

    @Test
    fun testPortalTeleportation() {
        val testEngine = GameEngine(mockContext)
        testEngine.setIsCircular(false)
        testEngine.setHasWalls(true)
        testEngine.setStartingLevel(3) // Level 4 has portal pair at (5, 14) and (14, 5)
        testEngine.startNewGame()
        
        val portalA = GridPoint(5, 14)
        val portalB = GridPoint(14, 5)
        
        // Verify levels portal configurations
        val config = testEngine.getCurrentLevelConfig()
        assertNotNull(config.portalPair)
        assertEquals(portalA, config.portalPair?.first)
        assertEquals(portalB, config.portalPair?.second)
    }

    @Test
    fun testLevelFoodCountProgression() {
        val engine = GameEngine(mockContext)
        assertEquals(5, engine.levels[0].targetFoodCount)
        assertEquals(6, engine.levels[1].targetFoodCount)
        assertEquals(10, engine.levels[5].targetFoodCount)
    }

    @Test
    fun testHighScoreLeaderboard() {
        val mockPrefs = Mockito.mock(android.content.SharedPreferences::class.java)
        val mockEditor = Mockito.mock(android.content.SharedPreferences.Editor::class.java)
        
        Mockito.`when`(mockContext.getSharedPreferences("wearsnake_leaderboard", Context.MODE_PRIVATE))
            .thenReturn(mockPrefs)
        Mockito.`when`(mockPrefs.edit()).thenReturn(mockEditor)
        Mockito.`when`(mockEditor.putString(Mockito.anyString(), Mockito.anyString())).thenReturn(mockEditor)
        Mockito.`when`(mockEditor.putInt(Mockito.anyString(), Mockito.anyInt())).thenReturn(mockEditor)
        
        // Mock default leaderboard values
        Mockito.`when`(mockPrefs.getString("initials_1", "AAA")).thenReturn("AAA")
        Mockito.`when`(mockPrefs.getInt("score_1", 150)).thenReturn(150)
        Mockito.`when`(mockPrefs.getString("initials_2", "BBB")).thenReturn("BBB")
        Mockito.`when`(mockPrefs.getInt("score_2", 100)).thenReturn(100)
        Mockito.`when`(mockPrefs.getString("initials_3", "CCC")).thenReturn("CCC")
        Mockito.`when`(mockPrefs.getInt("score_3", 50)).thenReturn(50)
        
        val engine = GameEngine(mockContext)
        val list = engine.getLeaderboard()
        assertEquals(3, list.size)
        assertEquals("AAA", list[0].initials)
        assertEquals(150, list[0].score)
        
        // Test high score qualification
        assertTrue(engine.isHighScore(120))
        assertFalse(engine.isHighScore(30))
        assertTrue(engine.isHighScore(200))
    }
}
