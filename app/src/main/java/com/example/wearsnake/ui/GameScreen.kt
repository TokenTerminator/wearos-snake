package com.example.wearsnake.ui

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.focusable
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.rotary.onRotaryScrollEvent
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.wearsnake.game.*
import kotlinx.coroutines.delay
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun GameScreen(
    gameEngine: GameEngine,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val state by gameEngine.state.collectAsState()
    val focusRequester = remember { FocusRequester() }

    val hudAlpha by animateFloatAsState(
        targetValue = if (state.status == GameStatus.PAUSED) 1f else 0.0f,
        animationSpec = tween(300),
        label = "hudAlpha"
    )

    // Coroutine loop for game ticks
    LaunchedEffect(state.status) {
        if (state.status == GameStatus.PLAYING) {
            focusRequester.requestFocus()
            while (state.status == GameStatus.PLAYING) {
                val baseDelay = state.difficulty.baseDelayMs
                val levelDelayFactor = state.currentLevelIndex * 15L
                val baseSpeed = (baseDelay - levelDelayFactor).coerceAtLeast(60L)
                val tickDelay = if (state.activePowerUp == PowerUpType.SPEED) {
                    (baseSpeed * 0.65f).toLong() // 35% faster!
                } else {
                    baseSpeed
                }
                delay(tickDelay)
                gameEngine.tick()
            }
        }
    }

    // Portal rotation and glow animations
    val infiniteTransition = rememberInfiniteTransition(label = "portalRotation")
    val portalAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(2500, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "portalAngle"
    )
    val portalPulse by infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = 1.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = SineIntensityEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "portalPulse"
    )

    // Lightning bolt pulse for speed power-up
    val speedPulse by infiniteTransition.animateFloat(
        initialValue = 0.7f,
        targetValue = 1.3f,
        animationSpec = infiniteRepeatable(
            animation = tween(600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "speedPulse"
    )

    // Food pulsing scale animation
    val foodPulse by infiniteTransition.animateFloat(
        initialValue = 0.85f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "foodPulse"
    )

    val themeStyle = ThemeStyles.getStyle(gameEngine.getCurrentLevelConfig().theme)
    val currentConfig = gameEngine.getCurrentLevelConfig()

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black)
            // 1. Intercept rotary crown input
            .onRotaryScrollEvent {
                if (it.verticalScrollPixels > 0) {
                    // Rotate Right (CW)
                    turnSnakeRight(state.direction, gameEngine)
                } else if (it.verticalScrollPixels < 0) {
                    // Rotate Left (CCW)
                    turnSnakeLeft(state.direction, gameEngine)
                }
                true
            }
            .focusRequester(focusRequester)
            .focusable()
            // 2. Unified Swipe and Tap Controls with Double-Tap to Pause
            .pointerInput(state.direction, state.status) {
                if (state.status != GameStatus.PLAYING) return@pointerInput
                awaitPointerEventScope {
                    var lastTapTime = 0L
                    val doubleTapTimeout = 300L
                    
                    while (true) {
                        val down = awaitFirstDown(requireUnconsumed = false)
                        var dragLimitExceeded = false
                        var totalDragX = 0f
                        var totalDragY = 0f
                        val dragThreshold = 30f
                        
                        val pointerId = down.id
                        var lastPosition = down.position
                        
                        do {
                            val event = awaitPointerEvent()
                            val dragEvent = event.changes.firstOrNull { it.id == pointerId }
                            if (dragEvent != null) {
                                if (dragEvent.pressed) {
                                    val dragAmount = dragEvent.position - lastPosition
                                    totalDragX += dragAmount.x
                                    totalDragY += dragAmount.y
                                    lastPosition = dragEvent.position
                                    
                                    if (abs(totalDragX) > dragThreshold || abs(totalDragY) > dragThreshold) {
                                        dragLimitExceeded = true
                                        // Trigger swipe direction change instantly!
                                        if (abs(totalDragX) > abs(totalDragY)) {
                                            if (totalDragX > 0) {
                                                gameEngine.updateDirection(Direction.RIGHT)
                                            } else {
                                                gameEngine.updateDirection(Direction.LEFT)
                                            }
                                        } else {
                                            if (totalDragY > 0) {
                                                gameEngine.updateDirection(Direction.DOWN)
                                            } else {
                                                gameEngine.updateDirection(Direction.UP)
                                            }
                                        }
                                        dragEvent.consume()
                                        break // Break out of tracking loop immediately
                                    }
                                    dragEvent.consume()
                                }
                            }
                        } while (event.changes.any { it.pressed })
                        
                        if (dragLimitExceeded) {
                            // Wait for the finger to be released completely before accepting next gesture
                            var released = false
                            while (!released) {
                                val event = awaitPointerEvent()
                                event.changes.forEach { it.consume() }
                                released = !event.changes.any { it.pressed }
                            }
                        } else {
                            val now = System.currentTimeMillis()
                            if (now - lastTapTime < doubleTapTimeout) {
                                gameEngine.togglePause()
                            } else {
                                val screenWidth = size.width
                                if (lastPosition.x < screenWidth / 2) {
                                    turnSnakeLeft(state.direction, gameEngine)
                                } else {
                                    turnSnakeRight(state.direction, gameEngine)
                                }
                            }
                            lastTapTime = now
                        }
                    }
                }
            }
    ) {
        // Core game board Canvas (Unclipped, manual Even-Odd mask applied at draw time)
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .padding(4.dp)
        ) {
            val canvasSize = size.minDimension
            val startX = (size.width - canvasSize) / 2f
            val startY = (size.height - canvasSize) / 2f

            val cellWidth = canvasSize / gameEngine.gridSize
            val cellHeight = canvasSize / gameEngine.gridSize

            // Helper to get pixel coordinates
            fun getCellRect(pt: GridPoint): Offset {
                return Offset(startX + pt.x * cellWidth, startY + pt.y * cellHeight)
            }

            // Sparkle drawing helper (draws a shiny 4-pointed vector star)
            fun drawSparkle(center: Offset, size: Float, rotationAngle: Float) {
                val halfSize = size / 2f
                val angleRad = Math.toRadians(rotationAngle.toDouble())
                val cosVal = cos(angleRad).toFloat()
                val sinVal = sin(angleRad).toFloat()
                val path = Path().apply {
                    val pTop = center + Offset(-sinVal * halfSize, -cosVal * halfSize)
                    val pBottom = center + Offset(sinVal * halfSize, cosVal * halfSize)
                    val pLeft = center + Offset(-cosVal * halfSize, sinVal * halfSize)
                    val pRight = center + Offset(cosVal * halfSize, -sinVal * halfSize)
                    
                    moveTo(pTop.x, pTop.y)
                    quadraticTo(center.x, center.y, pRight.x, pRight.y)
                    quadraticTo(center.x, center.y, pBottom.x, pBottom.y)
                    quadraticTo(center.x, center.y, pLeft.x, pLeft.y)
                    quadraticTo(center.x, center.y, pTop.x, pTop.y)
                    close()
                }
                drawPath(path = path, color = Color.White)
            }

            // Draw theme background (entire grid)
            for (x in 0 until gameEngine.gridSize) {
                for (y in 0 until gameEngine.gridSize) {
                    val pt = GridPoint(x, y)
                    val isEven = (x + y) % 2 == 0
                    val color = if (isEven) themeStyle.backgroundColor else themeStyle.gridColor
                    drawRect(
                        color = color,
                        topLeft = getCellRect(pt),
                        size = Size(cellWidth + 0.5f, cellHeight + 0.5f) // overlapping subpixels
                    )
                }
            }

            // Draw obstacles
            currentConfig.obstacles.forEach { obstacle ->
                val offset = getCellRect(obstacle)
                drawRect(
                    color = themeStyle.obstacleColor,
                    topLeft = offset + Offset(1f, 1f),
                    size = Size(cellWidth - 2f, cellHeight - 2f)
                )
                // Draw inner details (wood rings, cactus lines, space rock shine)
                drawRect(
                    color = themeStyle.borderColor.copy(alpha = 0.5f),
                    topLeft = offset + Offset(3f, 3f),
                    size = Size(cellWidth - 6f, cellHeight - 6f),
                    style = Stroke(width = 1.dp.toPx())
                )
            }

            // Draw wormhole portals (consistent cyan color, dark black center event horizon, and orbiting vortex particles)
            currentConfig.portalPair?.let { (portalA, portalB) ->
                val cyanPortalColor = Color(0xFF00E5FF)
                listOf(portalA, portalB).forEach { portal ->
                    val offset = getCellRect(portal)
                    val center = offset + Offset(cellWidth / 2f, cellHeight / 2f)
                    val radius = (cellWidth / 2f) * portalPulse

                    // 1. Outer glowing cyan halo
                    drawCircle(
                        color = cyanPortalColor.copy(alpha = 0.2f),
                        radius = cellWidth * 0.65f * portalPulse,
                        center = center
                    )

                    // 2. Swirling cyan ring
                    drawCircle(
                        color = cyanPortalColor,
                        radius = cellWidth * 0.45f * portalPulse,
                        center = center,
                        style = Stroke(width = 2.dp.toPx())
                    )

                    // 3. Dark spot / black hole core (event horizon)
                    drawCircle(
                        color = Color.Black,
                        radius = cellWidth * 0.25f,
                        center = center
                    )

                    // 4. Orbiting vortex particles
                    val linesCount = 4
                    for (i in 0 until linesCount) {
                        val angleRad = Math.toRadians((portalAngle + (i * (360f / linesCount))).toDouble())
                        val tickRadius = cellWidth * 0.45f * portalPulse
                        val tickEndX = center.x + cos(angleRad).toFloat() * tickRadius
                        val tickEndY = center.y + sin(angleRad).toFloat() * tickRadius
                        drawCircle(
                            color = Color(0xFFE0F7FA), // Glowing white-cyan particle
                            radius = 1.5.dp.toPx(),
                            center = Offset(tickEndX, tickEndY)
                        )
                    }
                }
            }

            // Draw food
            state.food.forEach { (pt, type) ->
                val offset = getCellRect(pt)
                val center = offset + Offset(cellWidth / 2f, cellHeight / 2f)
                val foodColor = themeStyle.foodColors[type] ?: Color.Red
                val pulseWidth = cellWidth * foodPulse
                val pulseHeight = cellHeight * foodPulse

                when (type) {
                    FoodType.APPLE -> {
                        val appleRadius = pulseWidth * 0.48f

                        // 1. Black border outline
                        drawCircle(
                            color = Color.Black,
                            radius = appleRadius + 1.5.dp.toPx(),
                            center = center
                        )
                        // 2. Apple Body
                        drawCircle(
                            color = foodColor,
                            radius = appleRadius,
                            center = center
                        )
                        // 3. Specular Highlight (Glossy shine)
                        drawCircle(
                            color = Color.White.copy(alpha = 0.75f),
                            radius = pulseWidth * 0.12f,
                            center = center + Offset(-pulseWidth * 0.15f, -pulseWidth * 0.15f)
                        )
                        // 4. Green Leaf
                        drawPath(
                            path = Path().apply {
                                moveTo(center.x, center.y - pulseWidth * 0.44f)
                                quadraticTo(
                                    center.x + pulseWidth * 0.2f, center.y - pulseWidth * 0.67f,
                                    center.x + pulseWidth * 0.3f, center.y - pulseWidth * 0.72f
                                )
                                quadraticTo(
                                    center.x + pulseWidth * 0.1f, center.y - pulseWidth * 0.52f,
                                    center.x, center.y - pulseWidth * 0.44f
                                )
                            },
                            color = Color(0xFF4CAF50)
                        )
                        // 5. Star Sparkle
                        drawSparkle(
                            center = center + Offset(appleRadius * 0.5f, -appleRadius * 0.5f),
                            size = pulseWidth * 0.25f,
                            rotationAngle = portalAngle * 1.5f
                        )
                    }
                    FoodType.CHERRY -> {
                        val cherryRadius = pulseWidth * 0.26f
                        val c1 = center + Offset(-pulseWidth * 0.16f, pulseHeight * 0.15f)
                        val c2 = center + Offset(pulseWidth * 0.16f, pulseHeight * 0.15f)
                        val topStem = center + Offset(0f, -pulseHeight * 0.35f)

                        // 1. Black stems outline
                        drawLine(color = Color.Black, start = c1, end = topStem, strokeWidth = 4.dp.toPx())
                        drawLine(color = Color.Black, start = c2, end = topStem, strokeWidth = 4.dp.toPx())

                        // 2. Black body outline
                        drawCircle(color = Color.Black, radius = cherryRadius + 1.5.dp.toPx(), center = c1)
                        drawCircle(color = Color.Black, radius = cherryRadius + 1.5.dp.toPx(), center = c2)

                        // 3. Cherry stems (brown)
                        drawLine(color = Color(0xFF8D6E63), start = c1, end = topStem, strokeWidth = 2.dp.toPx())
                        drawLine(color = Color(0xFF8D6E63), start = c2, end = topStem, strokeWidth = 2.dp.toPx())

                        // 4. Cherry bodies
                        drawCircle(color = foodColor, radius = cherryRadius, center = c1)
                        drawCircle(color = foodColor, radius = cherryRadius, center = c2)
                        
                        // 5. Specular Highlights (Glossy shine)
                        drawCircle(
                            color = Color.White.copy(alpha = 0.75f),
                            radius = pulseWidth * 0.08f,
                            center = c1 + Offset(-pulseWidth * 0.09f, -pulseWidth * 0.09f)
                        )
                        drawCircle(
                            color = Color.White.copy(alpha = 0.75f),
                            radius = pulseWidth * 0.08f,
                            center = c2 + Offset(-pulseWidth * 0.09f, -pulseWidth * 0.09f)
                        )

                        // 6. Star Sparkle
                        drawSparkle(
                            center = c2 + Offset(cherryRadius * 0.4f, -cherryRadius * 0.4f),
                            size = pulseWidth * 0.22f,
                            rotationAngle = portalAngle * 1.5f
                        )
                    }
                    FoodType.BANANA -> {
                        val bananaSize = Size(pulseWidth * 0.78f, pulseHeight * 0.78f)
                        val bananaOffset = offset + Offset((cellWidth - pulseWidth * 0.78f) / 2f, (cellHeight - pulseHeight * 0.78f) / 2f)

                        // 1. Black border outline
                        drawArc(
                            color = Color.Black,
                            startAngle = 30f,
                            sweepAngle = 120f,
                            useCenter = false,
                            topLeft = bananaOffset,
                            size = bananaSize,
                            style = Stroke(width = 8.dp.toPx(), cap = StrokeCap.Round)
                        )
                        // 2. Yellow arc Body
                        drawArc(
                            color = foodColor,
                            startAngle = 30f,
                            sweepAngle = 120f,
                            useCenter = false,
                            topLeft = bananaOffset,
                            size = bananaSize,
                            style = Stroke(width = 5.dp.toPx(), cap = StrokeCap.Round)
                        )
                        // 3. Specular highlight along the arc
                        drawArc(
                            color = Color.White.copy(alpha = 0.6f),
                            startAngle = 50f,
                            sweepAngle = 40f,
                            useCenter = false,
                            topLeft = offset + Offset((cellWidth - pulseWidth * 0.72f) / 2f, (cellHeight - pulseHeight * 0.72f) / 2f),
                            size = Size(pulseWidth * 0.72f, pulseHeight * 0.72f),
                            style = Stroke(width = 1.5.dp.toPx(), cap = StrokeCap.Round)
                        )
                        // 4. Star Sparkle
                        drawSparkle(
                            center = center + Offset(0f, -pulseHeight * 0.3f),
                            size = pulseWidth * 0.22f,
                            rotationAngle = portalAngle * 1.5f
                        )
                    }
                    FoodType.BLUEBERRY -> {
                        val berryRadius = pulseWidth * 0.22f
                        val b1 = center + Offset(-pulseWidth * 0.12f, 0f)
                        val b2 = center + Offset(pulseWidth * 0.12f, 0f)
                        val b3 = center + Offset(0f, -pulseHeight * 0.15f)

                        // 1. Black border outline
                        drawCircle(color = Color.Black, radius = berryRadius + 1.5.dp.toPx(), center = b1)
                        drawCircle(color = Color.Black, radius = berryRadius + 1.5.dp.toPx(), center = b2)
                        drawCircle(color = Color.Black, radius = berryRadius + 1.5.dp.toPx(), center = b3)

                        // 2. Berry cluster bodies
                        drawCircle(color = foodColor, radius = berryRadius, center = b1)
                        drawCircle(color = foodColor, radius = berryRadius, center = b2)
                        drawCircle(color = foodColor, radius = berryRadius, center = b3)

                        // 3. Specular Highlights (Glossy shine)
                        drawCircle(
                            color = Color.White.copy(alpha = 0.75f),
                            radius = pulseWidth * 0.07f,
                            center = b1 + Offset(-pulseWidth * 0.08f, -pulseWidth * 0.08f)
                        )
                        drawCircle(
                            color = Color.White.copy(alpha = 0.75f),
                            radius = pulseWidth * 0.07f,
                            center = b2 + Offset(-pulseWidth * 0.08f, -pulseWidth * 0.08f)
                        )
                        drawCircle(
                            color = Color.White.copy(alpha = 0.75f),
                            radius = pulseWidth * 0.07f,
                            center = b3 + Offset(-pulseWidth * 0.08f, -pulseWidth * 0.08f)
                        )
                        // 4. Star Sparkle
                        drawSparkle(
                            center = b3 + Offset(berryRadius * 0.4f, -berryRadius * 0.4f),
                            size = pulseWidth * 0.22f,
                            rotationAngle = portalAngle * 1.5f
                        )
                    }
                }
            }

            // Draw spawned Power-Up
            state.powerUp?.let { (pt, type) ->
                val offset = getCellRect(pt)
                val center = offset + Offset(cellWidth / 2f, cellHeight / 2f)

                when (type) {
                    PowerUpType.SPEED -> {
                        // Lightning bolt
                        val pSize = cellWidth * 0.45f * speedPulse
                        drawPath(
                            path = Path().apply {
                                moveTo(center.x + pSize * 0.1f, center.y - pSize)
                                lineTo(center.x - pSize, center.y + pSize * 0.1f)
                                lineTo(center.x - pSize * 0.1f, center.y + pSize * 0.1f)
                                lineTo(center.x - pSize * 0.2f, center.y + pSize)
                                lineTo(center.x + pSize, center.y - pSize * 0.1f)
                                lineTo(center.x + pSize * 0.1f, center.y - pSize * 0.1f)
                                close()
                            },
                            color = Color(0xFFFFD600) // Neon gold yellow
                        )
                    }
                    PowerUpType.BONUS_LIFE -> {
                        // Heart
                        val hSize = cellWidth * 0.4f * speedPulse
                        drawPath(
                            path = Path().apply {
                                moveTo(center.x, center.y + hSize * 0.3f)
                                cubicTo(
                                    center.x - hSize * 0.9f, center.y - hSize * 0.8f,
                                    center.x - hSize * 0.9f, center.y - hSize * 1.5f,
                                    center.x, center.y - hSize * 0.5f
                                )
                                cubicTo(
                                    center.x + hSize * 0.9f, center.y - hSize * 1.5f,
                                    center.x + hSize * 0.9f, center.y - hSize * 0.8f,
                                    center.x, center.y + hSize * 0.3f
                                )
                            },
                            color = Color(0xFFF44336) // Red heart
                        )
                    }
                }
            }

            // Draw Snake
            val snakeColor = Color(state.snakeColor)
            state.snake.forEachIndexed { index, pt ->
                val offset = getCellRect(pt)
                val isHead = index == 0
                val sizeVal = if (isHead) cellWidth else cellWidth * 0.85f
                val margin = (cellWidth - sizeVal) / 2f
                val bodyColor = if (isHead) snakeColor else snakeColor.copy(alpha = 0.85f - (index * 0.015f).coerceAtLeast(0.3f))

                // 1. Draw solid black outline border
                drawCircle(
                    color = Color.Black,
                    radius = sizeVal / 2f,
                    center = offset + Offset(cellWidth / 2f, cellHeight / 2f)
                )

                // 2. Draw colored body circle inside the outline
                drawCircle(
                    color = bodyColor,
                    radius = (sizeVal / 2f) - 1.dp.toPx(),
                    center = offset + Offset(cellWidth / 2f, cellHeight / 2f)
                )

                // Draw details on head (Eyes!)
                if (isHead) {
                    val eyeRadius = cellWidth * 0.09f
                    val pupilRadius = cellWidth * 0.04f
                    val center = offset + Offset(cellWidth / 2f, cellHeight / 2f)

                    // Draw eyes positioned based on direction
                    val (eyeOffset1, eyeOffset2) = when (state.direction) {
                        Direction.UP -> Pair(Offset(-cellWidth * 0.2f, -cellHeight * 0.2f), Offset(cellWidth * 0.2f, -cellHeight * 0.2f))
                        Direction.DOWN -> Pair(Offset(-cellWidth * 0.2f, cellHeight * 0.2f), Offset(cellWidth * 0.2f, cellHeight * 0.2f))
                        Direction.LEFT -> Pair(Offset(-cellWidth * 0.2f, -cellHeight * 0.2f), Offset(-cellWidth * 0.2f, cellHeight * 0.2f))
                        Direction.RIGHT -> Pair(Offset(cellWidth * 0.2f, -cellHeight * 0.2f), Offset(cellWidth * 0.2f, cellHeight * 0.2f))
                    }

                    // Eye whites
                    drawCircle(color = Color.White, radius = eyeRadius, center = center + eyeOffset1)
                    drawCircle(color = Color.White, radius = eyeRadius, center = center + eyeOffset2)

                    // Pupils
                    val pupilOffset = Offset(state.direction.dx * 1.dp.toPx(), state.direction.dy * 1.dp.toPx())
                    drawCircle(color = Color.Black, radius = pupilRadius, center = center + eyeOffset1 + pupilOffset)
                    drawCircle(color = Color.Black, radius = pupilRadius, center = center + eyeOffset2 + pupilOffset)
                }
            }

            // Draw Even-Odd Black Mask (for smooth circular border)
            if (state.isCircular) {
                val arenaRadius = canvasSize / 2f - 4.dp.toPx()
                val center = Offset(size.width / 2f, size.height / 2f)
                val outerPath = Path().apply {
                    addRect(Rect(0f, 0f, size.width, size.height))
                    addOval(Rect(center, arenaRadius))
                    fillType = PathFillType.EvenOdd
                }
                drawPath(
                    path = outerPath,
                    color = Color.Black
                )

                // Draw outer arena boundary circle on top of the mask edge
                drawCircle(
                    color = themeStyle.borderColor,
                    radius = arenaRadius,
                    center = center,
                    style = Stroke(width = 4.dp.toPx())
                )
            } else {
                // Draw standard rectangular boundary
                drawRect(
                    color = themeStyle.borderColor,
                    topLeft = Offset(startX, startY),
                    size = Size(canvasSize, canvasSize),
                    style = Stroke(width = 4.dp.toPx())
                )
            }
        }

        // --- HUD Layout overlays ---
        // Score & Level stats
        Column(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .graphicsLayer { alpha = hudAlpha }
                .padding(top = 18.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "${themeStyle.name} (Lvl ${state.currentLevelIndex + 1})",
                color = Color.White.copy(alpha = 0.9f),
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(1.dp))
            Text(
                text = "Food: ${state.foodEaten}/${currentConfig.targetFoodCount} | Score: ${state.score}",
                color = if (state.activePowerUp == PowerUpType.SPEED) Color(0xFFFFD600) else Color.White,
                fontSize = 11.sp,
                fontWeight = FontWeight.Black
            )
        }

        // Lives indicator and Power-up duration indicator
        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .graphicsLayer { alpha = hudAlpha }
                .padding(bottom = 18.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Lives (Hearts)
            for (i in 0 until 5) {
                val heartColor = if (i < state.lives) Color(0xFFF44336) else Color.White.copy(alpha = 0.2f)
                Text(
                    text = "♥",
                    color = heartColor,
                    fontSize = 14.sp,
                    modifier = Modifier.padding(horizontal = 1.dp)
                )
            }
        }

        // Active speed boost remaining time bar
        if (state.activePowerUp == PowerUpType.SPEED) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.5f)
                    .height(2.dp)
                    .background(Color.DarkGray)
                    .align(Alignment.BottomCenter)
                    .graphicsLayer { alpha = hudAlpha }
                    .padding(bottom = 26.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(state.powerUpTicksRemaining / 60f)
                        .fillMaxHeight()
                        .background(Color(0xFFFFD600))
                )
            }
        }

        // PAUSE overlay overlay layout
        if (state.status == GameStatus.PAUSED) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.7f))
                    .pointerInput(Unit) { detectTapGestures { gameEngine.togglePause() } },
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("PAUSED", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("Tap to Resume", color = Color.White.copy(alpha = 0.7f), fontSize = 10.sp)
                }
            }
        }

        // LIFE_LOST overlay layout
        if (state.status == GameStatus.LIFE_LOST) {
            LifeLostScreen(
                state = state,
                gameEngine = gameEngine,
                onContinue = { gameEngine.continueAfterLifeLost() }
            )
        }
    }
}

// Steering helper: Turns 90 degrees Left (Counter-Clockwise)
private fun turnSnakeLeft(currentDir: Direction, gameEngine: GameEngine) {
    val newDir = when (currentDir) {
        Direction.UP -> Direction.LEFT
        Direction.LEFT -> Direction.DOWN
        Direction.DOWN -> Direction.RIGHT
        Direction.RIGHT -> Direction.UP
    }
    gameEngine.updateDirection(newDir)
}

// Steering helper: Turns 90 degrees Right (Clockwise)
private fun turnSnakeRight(currentDir: Direction, gameEngine: GameEngine) {
    val newDir = when (currentDir) {
        Direction.UP -> Direction.RIGHT
        Direction.RIGHT -> Direction.DOWN
        Direction.DOWN -> Direction.LEFT
        Direction.LEFT -> Direction.UP
    }
    gameEngine.updateDirection(newDir)
}

// Custom Easing for portal animations
private val SineIntensityEasing = Easing { fraction ->
    sin(fraction * Math.PI / 2).toFloat()
}
