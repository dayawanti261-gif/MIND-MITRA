package com.example.mind_mitra.user.components



import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun BottomNavigationBar(
    selectedTab: Int,
    onTabSelected: (Int) -> Unit
) {
    val tabs = listOf(
        "Home",
        "Games",
        "Memories",
        "More"
    )

    NavigationBar(
        modifier = Modifier.fillMaxWidth()
    ) {
        tabs.forEachIndexed { index, title ->

            NavigationBarItem(
                selected = selectedTab == index,
                onClick = {
                    onTabSelected(index)
                },
                icon = {
                    Text(
                        text = when (index) {
                            0 -> "⌂"
                            1 -> "▶"
                            2 -> "▣"
                            else -> "☰"
                        }
                    )
                },
                label = {
                    Text(text = title)
                }
            )
        }
    }
}