package com.androidbolts.minitiktok.features.feed.presentation

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.androidbolts.minitiktok.R
import com.androidbolts.minitiktok.features.feed.domain.model.Feed

// FeedBottomInfo.kt
@Composable
fun FeedBottomInfo(
    video: Feed,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .padding(start = 16.dp, end = 80.dp, bottom = 16.dp)
    ) {
        // Username + verified
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "@${video.author.displayName}",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )
            if (video.isVerified) {
                Spacer(modifier = Modifier.width(4.dp))
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = "Verified",
                    tint = Color(0xFF20D5EC),
                    modifier = Modifier.size(16.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Caption
        Text(
            text = video.caption,
            color = Color.White,
            fontSize = 14.sp,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Sound row
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                painter = painterResource(R.drawable.ic_music_note),
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(14.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = video.soundName,
                color = Color.White,
                fontSize = 13.sp
            )
        }
    }
}