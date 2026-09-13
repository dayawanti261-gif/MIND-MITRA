package com.example.mind_mitra.user

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.mind_mitra.data.AuthRepository
import com.example.mind_mitra.network.GameProgressRequest
import com.example.mind_mitra.network.RetrofitClient
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.random.Random

private val DeepTeal = Color(0xFF146C68)
private val SoftMint = Color(0xFFE8F5F2)
private val DarkText = Color(0xFF183331)
private val SecondaryText = Color(0xFF61716F)

private data class MatchCard(val id: Int, val label: String, val pairId: Int)

@Composable
fun FamilyMemoryMatchingScreen(
    onBack: () -> Unit
) {
    val scope = rememberCoroutineScope()
    var cards by remember { mutableStateOf<List<MatchCard>>(emptyList()) }
    var flipped by remember { mutableStateOf(setOf<Int>()) }
    var matched by remember { mutableStateOf(setOf<Int>()) }
    var firstPick by remember { mutableStateOf<Int?>(null) }
    var mistakes by remember { mutableStateOf(0) }
    var attempts by remember { mutableStateOf(0) }
    var isSaving by remember { mutableStateOf(false) }
    var finished by remember { mutableStateOf(false) }
    var message by remember { mutableStateOf("") }
    val startTime = remember { System.currentTimeMillis() }

    LaunchedEffect(Unit) {
        val labels = listOf("Maa", "Papa", "Priya", "Raj", "Home", "Garden")
        val pairs = labels.flatMapIndexed { index, label ->
            listOf(
                MatchCard(id = index * 2, label = label, pairId = index),
                MatchCard(id = index * 2 + 1, label = label, pairId = index)
            )
        }.shuffled(Random(System.currentTimeMillis()))
        cards = pairs
    }

    fun checkWin() {
        if (matched.size == cards.size) {
            finished = true
            val elapsedSec = ((System.currentTimeMillis() - startTime) / 1000f).coerceAtLeast(1f)
            val accuracy = ((cards.size / 2 - mistakes).coerceAtLeast(0).toFloat() /
                (cards.size / 2).coerceAtLeast(1)) * 100f
            val userId = AuthRepository.getCurrentUserId()
            if (userId != null) {
                scope.launch {
                    isSaving = true
                    try {
                        RetrofitClient.apiService.saveGameProgress(
                            GameProgressRequest(
                                user_id = userId,
                                game_id = "family_memory_matching",
                                accuracy = accuracy.coerceIn(0f, 100f),
                                time_taken = elapsedSec,
                                mistakes = mistakes,
                                attempts = attempts,
                                level = 1,
                                completed = true
                            )
                        )
                        message = "Great job! Your progress was saved."
                    } catch (e: Exception) {
                        message = "Finished! Progress could not be saved."
                    } finally {
                        isSaving = false
                    }
                }
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF7FAF8))
            .padding(22.dp)
    ) {
        TextButton(onClick = onBack) {
            Text("← Back", color = DeepTeal, fontWeight = FontWeight.SemiBold)
        }

        Text(
            "Family Memory Matching",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = DarkText
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            "Tap cards to find matching pairs.",
            fontSize = 16.sp,
            color = SecondaryText
        )
        Spacer(modifier = Modifier.height(16.dp))

        if (cards.isEmpty()) {
            CircularProgressIndicator(color = DeepTeal)
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                modifier = Modifier.weight(1f),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(cards, key = { it.id }) { card ->
                    val revealed = card.id in flipped || card.id in matched
                    Card(
                        modifier = Modifier
                            .height(96.dp)
                            .clickable(enabled = !finished && card.id !in matched) {
                                if (card.id in flipped) return@clickable
                                flipped = flipped + card.id
                                val first = firstPick
                                if (first == null) {
                                    firstPick = card.id
                                } else {
                                    attempts += 1
                                    val firstCard = cards.find { it.id == first }
                                    if (firstCard?.pairId == card.pairId) {
                                        matched = matched + first + card.id
                                        firstPick = null
                                        checkWin()
                                    } else {
                                        mistakes += 1
                                        val missFirst = first
                                        val missSecond = card.id
                                        firstPick = null
                                        scope.launch {
                                            delay(700)
                                            flipped = flipped - missFirst - missSecond
                                        }
                                    }
                                }
                            },
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (revealed) SoftMint else DeepTeal
                        )
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(8.dp),
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = if (revealed) card.label else "?",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (revealed) DarkText else Color.White,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }
        }

        if (finished) {
            Spacer(modifier = Modifier.height(12.dp))
            Text(message.ifBlank { "Well done!" }, color = DeepTeal, fontSize = 16.sp)
        }
        if (isSaving) {
            Spacer(modifier = Modifier.height(8.dp))
            CircularProgressIndicator(color = DeepTeal)
        }

        Spacer(modifier = Modifier.height(12.dp))
        Button(
            onClick = onBack,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = DeepTeal)
        ) {
            Text("Done", fontSize = 17.sp)
        }
    }
}
