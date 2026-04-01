package com.androidbolts.minitiktok.features.feed.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.androidbolts.minitiktok.core.common.enums.FeedType

@Composable
fun FeedTopBar(
    selected: FeedType,
    onSelect: (FeedType) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(top = 52.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        FeedTypeTab(
            label = "Following",
            isSelected = selected == FeedType.FOLLOWING,
            onClick = { onSelect(FeedType.FOLLOWING) }
        )

        Spacer(modifier = Modifier.width(4.dp))

        Box(
            modifier = Modifier
                .size(4.dp)
                .background(Color.White.copy(alpha = 0.6f), CircleShape)
        )

        Spacer(modifier = Modifier.width(4.dp))

        FeedTypeTab(
            label = "For You",
            isSelected = selected == FeedType.FOR_YOU,
            onClick = { onSelect(FeedType.FOR_YOU) }
        )
    }
}

@Composable
private fun FeedTypeTab(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clickable(
                indication = null,
                interactionSource = remember { MutableInteractionSource() }
            ) { onClick() }
    ) {
        Text(
            text = label,
            color = if (isSelected) Color.White else Color.White.copy(alpha = 0.6f),
            fontSize = if (isSelected) 17.sp else 15.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
        )
        if (isSelected) {
            Spacer(modifier = Modifier.height(2.dp))
            Box(
                modifier = Modifier
                    .width(20.dp)
                    .height(2.dp)
                    .background(Color.White, RoundedCornerShape(1.dp))
            )
        }
    }
}