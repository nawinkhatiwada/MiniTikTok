package com.androidbolts.minitiktok.features.feed.presentation

import androidx.annotation.DrawableRes
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.androidbolts.minitiktok.R
import com.androidbolts.minitiktok.features.feed.domain.model.Feed

// FeedActionSidebar.kt
@Composable
fun FeedActionSidebar(
    video: Feed,
    onLike: () -> Unit,
    onComment: () -> Unit,
    onShare: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .padding(end = 12.dp, bottom = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // Avatar with follow button
        Box(contentAlignment = Alignment.BottomCenter) {
            AsyncImage(
                model = video.avatarUrl,
                contentDescription = "Creator avatar",
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .border(2.dp, Color.White, CircleShape),
                contentScale = ContentScale.Crop
            )
            Box(
                modifier = Modifier
                    .offset(y = 10.dp)
                    .size(20.dp)
                    .background(Color(0xFFFF2D55), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Follow",
                    tint = Color.White,
                    modifier = Modifier.size(12.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Like
        ActionButton(
            iconId = R.drawable.ic_like,
            count = video.likeCount,
            onClick = onLike
        )

        // Comment
        ActionButton(
            iconId = R.drawable.ic_comment,
            count = video.commentCount,
            onClick = onComment
        )

        // Share
        ActionButton(
            iconId =  R.drawable.ic_share,
            count = video.shareCount,
            onClick = onShare
        )

        // Spinning sound disc
        SpinningSoundDisc(imageUrl = video.soundAvatarUrl)
    }
}

@Composable
private fun ActionButton(
    @DrawableRes iconId: Int,
    count: String,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable(
            indication = null,
            interactionSource = remember { MutableInteractionSource() }
        ) { onClick() }
    ) {
        Icon(
            painter = painterResource(iconId),
            contentDescription = null,
            tint = Color.White,
            modifier = Modifier.size(35.dp)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = count,
            color = Color.White,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
private fun SpinningSoundDisc(imageUrl: String) {
    val infiniteTransition = rememberInfiniteTransition(label = "spin")
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = LinearEasing)
        ),
        label = "disc_rotation"
    )

    Box(
        modifier = Modifier
            .size(44.dp)
            .rotate(rotation)
            .clip(CircleShape)
            .border(3.dp, Color.DarkGray, CircleShape)
    ) {
        AsyncImage(
            model = imageUrl,
            contentDescription = "Sound",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )
        // Center hole
        Box(
            modifier = Modifier
                .size(12.dp)
                .background(Color.DarkGray, CircleShape)
                .align(Alignment.Center)
        )
    }
}