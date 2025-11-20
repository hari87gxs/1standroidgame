package com.athreya.mathworkout.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalContext
import com.athreya.mathworkout.data.MathTricks
import com.athreya.mathworkout.data.TrickProgressManager
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.random.Random

/**
 * Practice screen for a specific math trick
 * Generates 10 questions and tracks progress
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TrickPracticeScreen(
    trickId: String,
    onBackClick: () -> Unit,
    onComplete: (score: Int, total: Int) -> Unit = { _, _ -> },
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val progressManager = remember { TrickProgressManager(context) }
    val userPrefs = remember { com.athreya.mathworkout.data.UserPreferencesManager(context) }
    val trick = remember(trickId) { MathTricks.getTrickById(trickId) }
    
    if (trick == null) {
        // Error state
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Error") },
                    navigationIcon = {
                        IconButton(onClick = onBackClick) {
                            Icon(Icons.Default.ArrowBack, "Back")
                        }
                    }
                )
            }
        ) { padding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Text("Trick not found", color = MaterialTheme.colorScheme.error)
            }
        }
        return
    }
    
    // Generate questions for this trick
    val questions = remember(trickId) {
        generateQuestionsForTrick(trickId, count = 10)
    }
    
    var currentQuestionIndex by remember { mutableStateOf(0) }
    var userAnswer by remember { mutableStateOf("") }
    var correctAnswers by remember { mutableStateOf(0) }
    var showFeedback by remember { mutableStateOf(false) }
    var isCorrect by remember { mutableStateOf(false) }
    var isComplete by remember { mutableStateOf(false) }
    var showReviewDialog by remember { mutableStateOf(false) }
    var showCelebration by remember { mutableStateOf(false) }
    
    val currentQuestion = questions.getOrNull(currentQuestionIndex)
    val focusRequester = remember { FocusRequester() }
    val focusManager = LocalFocusManager.current
    val coroutineScope = rememberCoroutineScope()
    
    // Milestone tracking
    var showMilestoneDialog by remember { mutableStateOf(false) }
    var achievedMilestones by remember { mutableStateOf<List<com.athreya.mathworkout.data.TrickMilestone>>(emptyList()) }
    
    // Animation for feedback
    var animateCorrect by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (animateCorrect) 1.2f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "scale"
    )
    
    LaunchedEffect(currentQuestionIndex) {
        if (currentQuestionIndex < questions.size) {
            delay(300)
            focusRequester.requestFocus()
        }
    }
    
    // Review dialog
    if (showReviewDialog && trick != null) {
        ReviewTrickDialog(
            trick = trick,
            onDismiss = { showReviewDialog = false }
        )
    }
    
    // Milestone dialog
    if (showMilestoneDialog && achievedMilestones.isNotEmpty()) {
        MilestoneDialog(
            milestones = achievedMilestones,
            onDismiss = { showMilestoneDialog = false }
        )
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        text = "${trick.emoji} ${trick.name}",
                        maxLines = 1
                    ) 
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, "Back")
                    }
                },
                actions = {
                    // Review button
                    if (!isComplete) {
                        IconButton(onClick = { showReviewDialog = true }) {
                            Icon(
                                imageVector = Icons.Default.MenuBook,
                                contentDescription = "Review Trick",
                                tint = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { padding ->
        if (isComplete) {
            // Completion screen with celebration for perfect score
            CompletionScreen(
                score = correctAnswers,
                total = questions.size,
                isPerfect = correctAnswers == questions.size,
                onDone = onBackClick,
                onRetry = {
                    currentQuestionIndex = 0
                    correctAnswers = 0
                    userAnswer = ""
                    showFeedback = false
                    isComplete = false
                }
            )
        } else {
            Column(
                modifier = modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Progress indicator
                ProgressCard(
                    current = currentQuestionIndex + 1,
                    total = questions.size,
                    correct = correctAnswers
                )
                
                currentQuestion?.let { question ->
                    // Question card
                    QuestionCard(
                        question = question.problem,
                        questionNumber = currentQuestionIndex + 1
                    )
                    
                    // Answer input
                    OutlinedTextField(
                        value = userAnswer,
                        onValueChange = { 
                            if (it.all { char -> char.isDigit() || char == '-' }) {
                                userAnswer = it
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .focusRequester(focusRequester),
                        label = { Text("Your Answer") },
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Number,
                            imeAction = ImeAction.Done
                        ),
                        keyboardActions = KeyboardActions(
                            onDone = {
                                if (userAnswer.isNotEmpty()) {
                                    val correct = userAnswer.toIntOrNull() == question.answer
                                    isCorrect = correct
                                    showFeedback = true
                                    if (correct) {
                                        correctAnswers++
                                    }
                                    focusManager.clearFocus()
                                }
                            }
                        ),
                        enabled = !showFeedback,
                        singleLine = true,
                        textStyle = LocalTextStyle.current.copy(
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        )
                    )
                    
                    // Feedback section with animation
                    AnimatedVisibility(
                        visible = showFeedback,
                        enter = slideInVertically(
                            initialOffsetY = { it / 2 },
                            animationSpec = spring(
                                dampingRatio = Spring.DampingRatioMediumBouncy,
                                stiffness = Spring.StiffnessMedium
                            )
                        ) + fadeIn(),
                        exit = slideOutVertically() + fadeOut()
                    ) {
                        Box(modifier = Modifier.scale(if (isCorrect) scale else 1f)) {
                            FeedbackCard(
                                isCorrect = isCorrect,
                                correctAnswer = question.answer,
                                explanation = question.explanation
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Action button
                    Button(
                        onClick = {
                            if (showFeedback) {
                                // Move to next question
                                if (currentQuestionIndex < questions.size - 1) {
                                    currentQuestionIndex++
                                    userAnswer = ""
                                    showFeedback = false
                                } else {
                                    // Save progress before completing
                                    val wasCompleted = progressManager.isTrickCompleted(trickId)
                                    progressManager.savePracticeResult(
                                        trickId = trickId,
                                        score = correctAnswers,
                                        totalQuestions = questions.size
                                    )
                                    
                                    // Check for milestones
                                    val milestones = progressManager.checkMilestones()
                                    if (milestones.isNotEmpty()) {
                                        achievedMilestones = milestones
                                        showMilestoneDialog = true
                                    }
                                    
                                    // Award XP
                                    val xpEarned = progressManager.calculateXP(
                                        score = correctAnswers,
                                        totalQuestions = questions.size,
                                        isFirstTime = !wasCompleted && correctAnswers >= (questions.size * 0.7).toInt()
                                    )
                                    userPrefs.addXP(xpEarned)
                                    
                                    isComplete = true
                                    onComplete(correctAnswers, questions.size)
                                }
                            } else {
                                // Check answer
                                if (userAnswer.isNotEmpty()) {
                                    val correct = userAnswer.toIntOrNull() == question.answer
                                    isCorrect = correct
                                    showFeedback = true
                                    if (correct) {
                                        correctAnswers++
                                        // Trigger animation for correct answer
                                        coroutineScope.launch {
                                            animateCorrect = true
                                            delay(500)
                                            animateCorrect = false
                                        }
                                    }
                                    focusManager.clearFocus()
                                }
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        enabled = userAnswer.isNotEmpty() || showFeedback
                    ) {
                        Text(
                            text = if (showFeedback) {
                                if (currentQuestionIndex < questions.size - 1) "Next Question" else "Finish"
                            } else {
                                "Check Answer"
                            },
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

/**
 * Progress indicator card
 */
@Composable
private fun ProgressCard(
    current: Int,
    total: Int,
    correct: Int,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Question $current of $total",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )
                Text(
                    text = "✓ $correct correct",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            
            LinearProgressIndicator(
                progress = { current.toFloat() / total },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp)),
            )
        }
    }
}

/**
 * Question display card
 */
@Composable
private fun QuestionCard(
    question: String,
    questionNumber: Int,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "Question $questionNumber",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
            )
            Text(
                text = question,
                style = MaterialTheme.typography.displaySmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                textAlign = TextAlign.Center
            )
        }
    }
}

/**
 * Feedback card showing if answer is correct/incorrect
 */
@Composable
private fun FeedbackCard(
    isCorrect: Boolean,
    correctAnswer: Int,
    explanation: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (isCorrect) {
                MaterialTheme.colorScheme.primaryContainer
            } else {
                MaterialTheme.colorScheme.errorContainer
            }
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.Top
        ) {
            // Icon
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(
                        if (isCorrect) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.error
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (isCorrect) Icons.Default.Check else Icons.Default.Close,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            }
            
            // Content
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = if (isCorrect) "Correct! 🎉" else "Not quite...",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (isCorrect) {
                        MaterialTheme.colorScheme.onPrimaryContainer
                    } else {
                        MaterialTheme.colorScheme.onErrorContainer
                    }
                )
                
                if (!isCorrect) {
                    Text(
                        text = "The correct answer is: $correctAnswer",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                }
                
                if (explanation.isNotEmpty()) {
                    Text(
                        text = explanation,
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (isCorrect) {
                            MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                        } else {
                            MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.8f)
                        }
                    )
                }
            }
        }
    }
}

/**
 * Completion screen shown after all questions
 */
@Composable
private fun CompletionScreen(
    score: Int,
    total: Int,
    isPerfect: Boolean = false,
    onDone: () -> Unit,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    val percentage = (score.toFloat() / total * 100).toInt()
    val emoji = when {
        isPerfect -> "🎊"
        percentage >= 90 -> "🏆"
        percentage >= 70 -> "🎉"
        percentage >= 50 -> "👍"
        else -> "💪"
    }
    val message = when {
        isPerfect -> "Perfect Score!"
        percentage >= 90 -> "Outstanding!"
        percentage >= 70 -> "Great Job!"
        percentage >= 50 -> "Good Effort!"
        else -> "Keep Practicing!"
    }
    
    // Celebration animation for perfect score
    var animateEmoji by remember { mutableStateOf(false) }
    val emojiScale by animateFloatAsState(
        targetValue = if (animateEmoji) 1.3f else 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = EaseInOutCubic),
            repeatMode = RepeatMode.Reverse
        ),
        label = "emoji"
    )
    
    LaunchedEffect(isPerfect) {
        if (isPerfect) {
            animateEmoji = true
        }
    }
    
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Perfect score confetti effect (text-based)
        if (isPerfect) {
            Text(
                text = "🎊 ✨ 🎉 ⭐ 🏆 ⭐ 🎉 ✨ 🎊",
                fontSize = 20.sp,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(16.dp))
        }
        
        // Emoji with animation
        Box(
            modifier = Modifier.scale(if (isPerfect) emojiScale else 1f)
        ) {
            Text(
                text = emoji,
                fontSize = 80.sp
            )
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = message,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        
        Spacer(modifier = Modifier.height(32.dp))
        
        // Score card
        Card(
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer
            )
        ) {
            Column(
                modifier = Modifier.padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "Your Score",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Text(
                    text = "$score / $total",
                    style = MaterialTheme.typography.displayLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "$percentage%",
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                )
            }
        }
        
        Spacer(modifier = Modifier.height(48.dp))
        
        // Action buttons
        Button(
            onClick = onRetry,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
        ) {
            Text(
                text = "Practice Again",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
        }
        
        Spacer(modifier = Modifier.height(12.dp))
        
        OutlinedButton(
            onClick = onDone,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
        ) {
            Text(
                text = "Done",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

/**
 * Data class for practice questions
 */
private data class PracticeQuestion(
    val problem: String,
    val answer: Int,
    val explanation: String
)

/**
 * Generate practice questions for a specific trick
 */
private fun generateQuestionsForTrick(trickId: String, count: Int): List<PracticeQuestion> {
    return when (trickId) {
        "multiply_by_9" -> generateMultiplyBy9Questions(count)
        "multiply_by_11" -> generateMultiplyBy11Questions(count)
        "square_ending_5" -> generateSquareEnding5Questions(count)
        "multiply_by_5" -> generateMultiplyBy5Questions(count)
        "add_near_100" -> generateAddNear100Questions(count)
        "subtract_from_1000" -> generateSubtractFrom1000Questions(count)
        "divisibility_3" -> generateDivisibility3Questions(count)
        "multiply_by_4" -> generateMultiplyBy4Questions(count)
        "multiply_near_100" -> generateMultiplyNear100Questions(count)
        "square_ending_in_1" -> generateSquareEnding1Questions(count)
        "multiply_by_12" -> generateMultiplyBy12Questions(count)
        else -> generateGenericQuestions(count)
    }
}

private fun generateMultiplyBy9Questions(count: Int): List<PracticeQuestion> {
    return (1..count).map {
        val num = Random.nextInt(2, 10)
        val answer = num * 9
        PracticeQuestion(
            problem = "$num × 9 = ?",
            answer = answer,
            explanation = "Using the finger method: $num fingers down = $answer"
        )
    }
}

private fun generateMultiplyBy11Questions(count: Int): List<PracticeQuestion> {
    return (1..count).map {
        val num = Random.nextInt(10, 99)
        val answer = num * 11
        val tens = num / 10
        val ones = num % 10
        PracticeQuestion(
            problem = "$num × 11 = ?",
            answer = answer,
            explanation = "Split: $tens and $ones, middle: ${tens + ones} = $answer"
        )
    }
}

private fun generateSquareEnding5Questions(count: Int): List<PracticeQuestion> {
    return (1..count).map {
        val num = Random.nextInt(1, 10) * 10 + 5
        val answer = num * num
        val n = num / 10
        PracticeQuestion(
            problem = "$num² = ?",
            answer = answer,
            explanation = "$n × ${n + 1} = ${n * (n + 1)}, then add 25 = $answer"
        )
    }
}

private fun generateMultiplyBy5Questions(count: Int): List<PracticeQuestion> {
    return (1..count).map {
        val num = Random.nextInt(10, 100)
        val answer = num * 5
        PracticeQuestion(
            problem = "$num × 5 = ?",
            answer = answer,
            explanation = "$num ÷ 2 = ${num / 2}.${if (num % 2 == 0) "0" else "5"}, then × 10 = $answer"
        )
    }
}

private fun generateAddNear100Questions(count: Int): List<PracticeQuestion> {
    return (1..count).map {
        val num1 = Random.nextInt(90, 99)
        val num2 = Random.nextInt(90, 99)
        val answer = num1 + num2
        PracticeQuestion(
            problem = "$num1 + $num2 = ?",
            answer = answer,
            explanation = "Add to 100: ${100 + num1 - (100 - num2)} = $answer"
        )
    }
}

private fun generateSubtractFrom1000Questions(count: Int): List<PracticeQuestion> {
    return (1..count).map {
        val num = Random.nextInt(100, 999)
        val answer = 1000 - num
        PracticeQuestion(
            problem = "1000 - $num = ?",
            answer = answer,
            explanation = "Subtract each digit from 9, last from 10"
        )
    }
}

private fun generateDivisibility3Questions(count: Int): List<PracticeQuestion> {
    return (1..count).map {
        val isDivisible = Random.nextBoolean()
        val num = if (isDivisible) {
            Random.nextInt(10, 100) * 3
        } else {
            (Random.nextInt(10, 100) * 3) + Random.nextInt(1, 3)
        }
        PracticeQuestion(
            problem = "Is $num divisible by 3?",
            answer = if (num % 3 == 0) 1 else 0,
            explanation = "Sum of digits: ${num.toString().map { it.toString().toInt() }.sum()}"
        )
    }
}

private fun generateMultiplyBy4Questions(count: Int): List<PracticeQuestion> {
    return (1..count).map {
        val num = Random.nextInt(10, 50)
        val answer = num * 4
        PracticeQuestion(
            problem = "$num × 4 = ?",
            answer = answer,
            explanation = "$num × 2 = ${num * 2}, then ${num * 2} × 2 = $answer"
        )
    }
}

private fun generateMultiplyNear100Questions(count: Int): List<PracticeQuestion> {
    return (1..count).map {
        val num1 = Random.nextInt(94, 99)
        val num2 = Random.nextInt(94, 99)
        val answer = num1 * num2
        PracticeQuestion(
            problem = "$num1 × $num2 = ?",
            answer = answer,
            explanation = "Advanced Vedic method"
        )
    }
}

private fun generateSquareEnding1Questions(count: Int): List<PracticeQuestion> {
    return (1..count).map {
        val num = Random.nextInt(1, 9) * 10 + 1
        val answer = num * num
        PracticeQuestion(
            problem = "$num² = ?",
            answer = answer,
            explanation = "Use the ending in 1 trick"
        )
    }
}

private fun generateMultiplyBy12Questions(count: Int): List<PracticeQuestion> {
    return (1..count).map {
        val num = Random.nextInt(10, 50)
        val answer = num * 12
        PracticeQuestion(
            problem = "$num × 12 = ?",
            answer = answer,
            explanation = "($num × 2) + ($num × 10) = ${num * 2} + ${num * 10} = $answer"
        )
    }
}

private fun generateGenericQuestions(count: Int): List<PracticeQuestion> {
    return (1..count).map {
        val num1 = Random.nextInt(10, 50)
        val num2 = Random.nextInt(2, 10)
        val answer = num1 * num2
        PracticeQuestion(
            problem = "$num1 × $num2 = ?",
            answer = answer,
            explanation = ""
        )
    }
}

/**
 * Review Dialog showing the trick explanation during practice
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ReviewTrickDialog(
    trick: com.athreya.mathworkout.data.MathTrick,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 500.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(24.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = trick.emoji,
                            fontSize = 32.sp
                        )
                        Text(
                            text = trick.name,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, "Close")
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                HorizontalDivider()
                Spacer(modifier = Modifier.height(16.dp))
                
                // Description
                Text(
                    text = trick.shortDescription,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Steps
                Text(
                    text = "Steps:",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                trick.steps.forEach { step ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primaryContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = step.stepNumber.toString(),
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = step.title,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = step.description,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                            )
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Example
                Text(
                    text = "Example:",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp)
                    ) {
                        Text(
                            text = trick.example.problem,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "= ${trick.example.solution}",
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Close button
                Button(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Got it!")
                }
            }
        }
    }
}

/**
 * Dialog showing milestone achievements
 */
@Composable
private fun MilestoneDialog(
    milestones: List<com.athreya.mathworkout.data.TrickMilestone>,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Text(
                text = "🏆",
                fontSize = 48.sp
            )
        },
        title = {
            Text(
                text = "Achievement Unlocked!",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                milestones.forEach { milestone ->
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer
                        )
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = milestone.title,
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = milestone.description,
                                style = MaterialTheme.typography.bodyMedium,
                                textAlign = TextAlign.Center,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Awesome!", fontSize = 18.sp, fontWeight = FontWeight.Bold)
            }
        }
    )
}
