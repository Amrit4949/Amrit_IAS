package com.amrit.jaago.ui

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.amrit.jaago.ui.theme.Alarm1
import com.amrit.jaago.ui.theme.BottomSheet
import com.amrit.jaago.ui.theme.Hairline
import com.amrit.jaago.ui.theme.SectionLabel
import com.amrit.jaago.ui.theme.Slate2
import com.amrit.jaago.ui.theme.TextHi
import com.amrit.jaago.ui.theme.TextLo
import com.amrit.jaago.ui.theme.TextMid
import kotlinx.coroutines.launch

/** Grid of live thumbnails: Auto, your photo, then every scene. */
@Composable
fun WallpaperSheet(
    visible: Boolean,
    current: String,
    onPick: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var importing by remember { mutableStateOf(false) }
    val photoId = remember(visible) {
        // Reuse the one photo on disk, if any, so the tile shows it.
        val dir = java.io.File(context.filesDir, "wallpapers")
        dir.listFiles()?.maxByOrNull { it.lastModified() }?.let { "photo:" + it.name }
    }
    val picker = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null) {
            importing = true
            scope.launch {
                val id = Wallpapers.importPhoto(context, uri)
                importing = false
                if (id != null) onPick(id)
            }
        }
    }

    BottomSheet(visible = visible, onDismiss = onDismiss, title = "Alarm wallpaper") {
        Text(
            "What you see when it rings. Every scene is drawn live and moves; Auto changes with the time of day.",
            fontSize = 12.5.sp,
            color = TextLo,
            lineHeight = 17.sp
        )

        SectionLabel("Choose")
        val hour = remember { Wallpapers.hourNow() }
        val tiles: List<Pair<String, String>> =
            listOf(Wallpapers.AUTO to "Auto") +
                listOf((photoId ?: "photo:") to "Your photo") +
                Wallpapers.scenes.map { it.id to it.title }

        tiles.chunked(3).forEach { row ->
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                row.forEach { (id, title) ->
                    val isPhotoTile = id.startsWith("photo:")
                    val selected = current == id || (isPhotoTile && Wallpapers.isPhoto(current))
                    WallpaperTile(
                        id = id,
                        title = title,
                        subtitle = when {
                            id == Wallpapers.AUTO -> Wallpapers.autoScene(hour).title + " now"
                            isPhotoTile -> if (importing) "Importing..." else if (photoId == null) "Pick from gallery" else "Tap to change"
                            else -> Wallpapers.resolve(id, hour).blurb
                        },
                        selected = selected,
                        modifier = Modifier.weight(1f)
                    ) {
                        if (isPhotoTile) {
                            picker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                        } else {
                            onPick(id)
                        }
                    }
                }
                repeat(3 - row.size) { Spacer(Modifier.weight(1f)) }
            }
        }
        Spacer(Modifier.height(4.dp))
    }
}

@Composable
private fun WallpaperTile(
    id: String,
    title: String,
    subtitle: String,
    selected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val shape = RoundedCornerShape(18.dp)
    val hasPhoto = id.startsWith("photo:") && id.length > "photo:".length
    Column(
        modifier = modifier
            .clip(shape)
            .background(Slate2, shape)
            .border(if (selected) 2.dp else 1.dp, if (selected) Alarm1 else Hairline, shape)
            .clickable { onClick() }
    ) {
        Box(modifier = Modifier.fillMaxWidth().aspectRatio(0.72f)) {
            if (id == "photo:") {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Brush.verticalGradient(listOf(Color(0xFF1C2140), Color(0xFF12152A)))),
                    contentAlignment = Alignment.Center
                ) {
                    Text("+", fontSize = 36.sp, fontWeight = FontWeight.Light, color = TextMid)
                }
            } else {
                WallpaperView(id = if (hasPhoto || !id.startsWith("photo:")) id else Wallpapers.AUTO, animated = false, modifier = Modifier.fillMaxSize())
            }
            if (selected) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(6.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Alarm1, RoundedCornerShape(8.dp))
                        .padding(horizontal = 7.dp, vertical = 3.dp)
                ) {
                    Text("On", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFF17060B))
                }
            }
        }
        Column(modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp)) {
            Text(title, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = TextHi, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text(subtitle, fontSize = 10.5.sp, color = TextLo, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
    }
}
