package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.R
import com.example.ui.theme.ElevatedSurface
import com.example.ui.theme.PrimaryPurple
import java.io.File

@Composable
fun ExerciseImage(
    imagePath: String?,
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 12.dp,
    contentDescription: String = "Exercise image"
) {
    val context = LocalContext.current

    val imageModel: Any? = when {
        imagePath == null -> null
        imagePath == "res:ex_bench_press" -> R.drawable.ex_bench_press
        imagePath == "res:ex_squat" -> R.drawable.ex_squat
        imagePath == "res:ex_deadlift" -> R.drawable.ex_deadlift
        imagePath == "res:ex_shoulder_press" -> R.drawable.ex_shoulder_press
        imagePath.startsWith("res:") -> {
            val resName = imagePath.removePrefix("res:")
            val resId = context.resources.getIdentifier(resName, "drawable", context.packageName)
            if (resId != 0) resId else null
        }
        else -> {
            val file = File(imagePath)
            if (file.exists()) file else null
        }
    }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(cornerRadius))
            .background(ElevatedSurface),
        contentAlignment = Alignment.Center
    ) {
        if (imageModel != null) {
            AsyncImage(
                model = imageModel,
                contentDescription = contentDescription,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        } else {
            // Elegant fallback anatomical/fitness placeholder
            Icon(
                imageVector = Icons.Default.FitnessCenter,
                contentDescription = "Exercise placeholder",
                tint = PrimaryPurple.copy(alpha = 0.6f),
                modifier = Modifier.size(32.dp)
            )
        }
    }
}
