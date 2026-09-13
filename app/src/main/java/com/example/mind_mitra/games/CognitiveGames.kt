package com.example.mind_mitra.games

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.mind_mitra.R
import com.example.mind_mitra.ui.components.MindCard
import com.example.mind_mitra.ui.components.MindEmptyState
import com.example.mind_mitra.ui.components.MindLoadingState
import com.example.mind_mitra.ui.components.MindPrimaryButton
import com.example.mind_mitra.ui.components.MindScreen
import com.example.mind_mitra.ui.components.MindSecondaryButton
import com.example.mind_mitra.ui.components.MindSectionHeader
import com.example.mind_mitra.ui.theme.MindAccentSoft
import com.example.mind_mitra.ui.theme.MindDarkText
import com.example.mind_mitra.ui.theme.MindDeepTeal
import com.example.mind_mitra.ui.theme.MindSecondaryText
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun CognitiveGameScreen(gameType: GameType, onBack: () -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val difficulty = remember { GameProgressManager.getDifficulty(context, gameType) }
    var started by remember { mutableStateOf(false) }
    var finished by remember { mutableStateOf(false) }
    var resultText by remember { mutableStateOf("") }
    var memories by remember { mutableStateOf<List<GameMemoryItem>>(emptyList()) }
    var loadingMemories by remember { mutableStateOf(true) }
    var newRewards by remember { mutableStateOf<List<RewardId>>(emptyList()) }

    LaunchedEffect(Unit) {
        loadingMemories = true
        memories = MemoryGameRepository.loadMemories(context)
        loadingMemories = false
    }

    fun finish(result: GameSessionResult) {
        finished = true
        resultText = context.getString(R.string.game_complete_score, result.accuracy.toInt())
        scope.launch {
            newRewards = GameProgressManager.saveSession(context, result)
        }
    }

    if (newRewards.isNotEmpty()) {
        val reward = newRewards.first()
        AlertDialog(
            onDismissRequest = { newRewards = emptyList() },
            title = { Text(stringResource(R.string.reward_earned_title)) },
            text = {
                Text(
                    stringResource(reward.titleRes) + "\n" +
                        stringResource(reward.descRes)
                )
            },
            confirmButton = {
                androidx.compose.material3.TextButton(onClick = { newRewards = emptyList() }) {
                    Text(
                        stringResource(R.string.continue_button),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = MindDeepTeal
                    )
                }
            }
        )
    }

    MindScreen(onBack = onBack, scrollable = !started || finished) {
        MindSectionHeader(
            title = stringResource(gameType.titleRes),
            subtitle = stringResource(
                R.string.difficulty_label,
                stringResource(
                    when (difficulty) {
                        GameDifficulty.EASY -> R.string.difficulty_easy
                        GameDifficulty.MEDIUM -> R.string.difficulty_medium
                        GameDifficulty.HARD -> R.string.difficulty_hard
                    }
                )
            )
        )
        Spacer(modifier = Modifier.height(16.dp))

        if (loadingMemories) {
            MindLoadingState(stringResource(R.string.loading))
        } else if (memories.isEmpty()) {
            MindEmptyState(
                title = stringResource(R.string.no_memories_for_games_title),
                body = stringResource(R.string.no_memories_for_games_body)
            )
            Spacer(modifier = Modifier.height(16.dp))
            MindSecondaryButton(text = stringResource(R.string.back_button), onClick = onBack)
        } else if (!started && !finished) {
            Text(
                stringResource(R.string.game_instructions_intro),
                fontSize = 20.sp,
                color = MindDarkText,
                lineHeight = 28.sp
            )
            Spacer(modifier = Modifier.height(20.dp))
            MindPrimaryButton(text = stringResource(R.string.start_game), onClick = { started = true })
        } else if (finished) {
            MindCard {
                Text(resultText, fontSize = 22.sp, color = MindDarkText, fontWeight = FontWeight.SemiBold)
                Spacer(modifier = Modifier.height(16.dp))
                MindPrimaryButton(
                    text = stringResource(R.string.play_again),
                    onClick = { started = false; finished = false }
                )
                Spacer(modifier = Modifier.height(8.dp))
                MindSecondaryButton(text = stringResource(R.string.back_button), onClick = onBack)
            }
        } else {
            when (gameType) {
                GameType.MEMORY_MATCH -> MemoryMatchGame(memories, difficulty, ::finish)
                GameType.RECALL -> RecallGame(memories, difficulty, ::finish)
                GameType.SEQUENCE -> SequenceGame(memories, difficulty, ::finish)
            }
        }
    }
}

private data class MatchCard(
    val id: Int,
    val memoryId: String,
    val label: String,
    val photoUrl: String?,
    val pairId: Int
)

@Composable
private fun MemoryMatchGame(
    memories: List<GameMemoryItem>,
    difficulty: GameDifficulty,
    onFinish: (GameSessionResult) -> Unit
) {
    val pairCount = when (difficulty) {
        GameDifficulty.EASY -> 2
        GameDifficulty.MEDIUM -> 3
        GameDifficulty.HARD -> minOf(4, memories.size)
    }
    val selectedMemories = remember(memories, pairCount) { memories.shuffled().take(pairCount) }
    var cards by remember { mutableStateOf<List<MatchCard>>(emptyList()) }
    var flipped by remember { mutableStateOf(setOf<Int>()) }
    var matched by remember { mutableStateOf(setOf<Int>()) }
    var firstPick by remember { mutableStateOf<Int?>(null) }
    var mistakes by remember { mutableIntStateOf(0) }
    var attempts by remember { mutableIntStateOf(0) }
    val start = remember { System.currentTimeMillis() }

    LaunchedEffect(selectedMemories) {
        cards = selectedMemories.flatMapIndexed { i, memory ->
            listOf(
                MatchCard(i * 2, memory.id, memory.title, memory.photoUrl, i),
                MatchCard(i * 2 + 1, memory.id, memory.title, memory.photoUrl, i)
            )
        }.shuffled()
    }

    fun checkWin() {
        if (matched.size == cards.size && cards.isNotEmpty()) {
            val acc = ((pairCount * 2 - mistakes).coerceAtLeast(0).toFloat() / (pairCount * 2)) * 100f
            onFinish(
                GameSessionResult(
                    "memory_match", acc, acc, mistakes, attempts,
                    (System.currentTimeMillis() - start) / 1000f, true, difficulty
                )
            )
        }
    }

    val columns = if (pairCount <= 2) 2 else 3
    Column(modifier = Modifier.fillMaxWidth()) {
        cards.chunked(columns).forEach { row ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                row.forEach { card ->
                    val revealed = card.id in flipped || card.id in matched
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .aspectRatio(1f)
                            .background(
                                if (revealed) MindAccentSoft else MindDeepTeal,
                                RoundedCornerShape(12.dp)
                            )
                            .clickable(enabled = card.id !in matched) {
                                if (card.id in flipped) return@clickable
                                flipped = flipped + card.id
                                if (firstPick == null) {
                                    firstPick = card.id
                                } else {
                                    attempts++
                                    val first = cards.find { it.id == firstPick }
                                    if (first?.pairId == card.pairId) {
                                        matched = matched + firstPick!! + card.id
                                    } else {
                                        mistakes++
                                    }
                                    firstPick = null
                                    flipped = emptySet()
                                    checkWin()
                                }
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        if (revealed) {
                            if (!card.photoUrl.isNullOrBlank()) {
                                AsyncImage(
                                    model = card.photoUrl,
                                    contentDescription = card.label,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .aspectRatio(1f),
                                    contentScale = ContentScale.Crop
                                )
                            } else {
                                Text(
                                    card.label,
                                    color = MindDarkText,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.padding(6.dp),
                                    maxLines = 3,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }
                if (row.size < columns) {
                    repeat(columns - row.size) {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

@Composable
private fun RecallGame(
    memories: List<GameMemoryItem>,
    difficulty: GameDifficulty,
    onFinish: (GameSessionResult) -> Unit
) {
    val target = remember(memories) { memories.random() }
    val distractors = remember(memories, target) {
        memories.filter { it.id != target.id }.shuffled()
    }
    var phase by remember { mutableStateOf("show") }
    val start = remember { System.currentTimeMillis() }

    val question = when {
        target.description != null -> stringResource(R.string.recall_question_description)
        target.category != null -> stringResource(R.string.recall_question_category)
        else -> stringResource(R.string.recall_question_title)
    }

    val correctAnswer = when {
        target.description != null -> target.description!!
        target.category != null -> target.category!!
        else -> target.title
    }

    val options = remember(target, distractors, correctAnswer) {
        val wrong = buildList {
            distractors.forEach { memory ->
                when {
                    target.description != null && memory.description != null -> add(memory.description!!)
                    target.category != null && memory.category != null -> add(memory.category!!)
                    else -> add(memory.title)
                }
            }
        }.distinct().filter { it != correctAnswer }.shuffled().take(3)
        (wrong + correctAnswer).shuffled()
    }

    LaunchedEffect(Unit) {
        delay(if (difficulty == GameDifficulty.HARD) 5000 else 3500)
        phase = "quiz"
    }

    if (phase == "show") {
        Text(stringResource(R.string.recall_remember), fontSize = 22.sp, color = MindDarkText, fontWeight = FontWeight.SemiBold)
        Spacer(modifier = Modifier.height(16.dp))
        MindCard {
            if (!target.photoUrl.isNullOrBlank()) {
                AsyncImage(
                    model = target.photoUrl,
                    contentDescription = target.title,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp),
                    contentScale = ContentScale.Crop
                )
                Spacer(modifier = Modifier.height(12.dp))
            }
            Text(target.title, fontSize = 22.sp, fontWeight = FontWeight.Bold, color = MindDarkText)
            if (!target.description.isNullOrBlank()) {
                Text(
                    target.description,
                    fontSize = 18.sp,
                    color = MindSecondaryText,
                    modifier = Modifier.padding(top = 8.dp),
                    lineHeight = 26.sp
                )
            }
            if (!target.category.isNullOrBlank()) {
                Text(
                    target.category,
                    fontSize = 16.sp,
                    color = MindDeepTeal,
                    modifier = Modifier.padding(top = 6.dp)
                )
            }
        }
    } else {
        Text(question, fontSize = 20.sp, color = MindDarkText, fontWeight = FontWeight.SemiBold)
        Spacer(modifier = Modifier.height(16.dp))
        options.forEach { option ->
            MindSecondaryButton(
                text = option,
                onClick = {
                    val correct = option == correctAnswer
                    val acc = if (correct) 100f else 0f
                    onFinish(
                        GameSessionResult(
                            "recall", acc, acc,
                            if (correct) 0 else 1, 1,
                            (System.currentTimeMillis() - start) / 1000f,
                            correct, difficulty
                        )
                    )
                },
                modifier = Modifier.padding(bottom = 10.dp)
            )
        }
    }
}

@Composable
private fun SequenceGame(
    memories: List<GameMemoryItem>,
    difficulty: GameDifficulty,
    onFinish: (GameSessionResult) -> Unit
) {
    val count = when (difficulty) {
        GameDifficulty.EASY -> minOf(3, memories.size)
        GameDifficulty.MEDIUM -> minOf(4, memories.size)
        GameDifficulty.HARD -> minOf(5, memories.size)
    }
    val sequence = remember(memories, count) {
        memories.sortedBy { it.title.lowercase() }.take(count)
    }
    var phase by remember { mutableStateOf("show") }
    var selectedOrder by remember { mutableStateOf(listOf<String>()) }
    val start = remember { System.currentTimeMillis() }

    LaunchedEffect(Unit) {
        delay(if (difficulty == GameDifficulty.HARD) 6000 else 4500)
        phase = "quiz"
    }

    if (phase == "show") {
        Text(stringResource(R.string.sequence_remember_order), fontSize = 22.sp, color = MindDarkText, fontWeight = FontWeight.SemiBold)
        Spacer(modifier = Modifier.height(12.dp))
        sequence.forEachIndexed { index, memory ->
            MindCard(modifier = Modifier.padding(bottom = 8.dp)) {
                Text(
                    "${index + 1}. ${memory.title}",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MindDarkText
                )
            }
        }
    } else {
        Text(stringResource(R.string.sequence_tap_order), fontSize = 20.sp, color = MindDarkText, fontWeight = FontWeight.SemiBold)
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            stringResource(R.string.sequence_progress, selectedOrder.size, sequence.size),
            fontSize = 18.sp,
            color = MindSecondaryText
        )
        Spacer(modifier = Modifier.height(12.dp))
        val remaining = remember(sequence, selectedOrder) {
            sequence.filter { it.id !in selectedOrder }.shuffled()
        }
        remaining.forEach { memory ->
            MindSecondaryButton(
                text = memory.title,
                onClick = {
                    val expected = sequence.getOrNull(selectedOrder.size)?.id
                    if (memory.id == expected) {
                        val updated = selectedOrder + memory.id
                        selectedOrder = updated
                        if (updated.size == sequence.size) {
                            onFinish(
                                GameSessionResult(
                                    "sequence", 100f, 100f, 0, sequence.size,
                                    (System.currentTimeMillis() - start) / 1000f, true, difficulty
                                )
                            )
                        }
                    } else {
                        val mistakes = 1
                        val acc = (selectedOrder.size.toFloat() / sequence.size) * 100f
                        onFinish(
                            GameSessionResult(
                                "sequence", acc, acc, mistakes, selectedOrder.size + 1,
                                (System.currentTimeMillis() - start) / 1000f, false, difficulty
                            )
                        )
                    }
                },
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }
    }
}
