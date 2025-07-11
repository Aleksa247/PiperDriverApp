package com.piperrideshare.driver.ui.components

import android.graphics.drawable.Drawable
import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toBitmap

@Composable
fun XmlDrawableImage(
    @DrawableRes drawableRes: Int,
    contentDescription: String?,
) {
    val context = LocalContext.current
    val drawable: Drawable? =
        remember(drawableRes) {
            ContextCompat.getDrawable(context, drawableRes)
        }

    drawable?.let {
        Image(
            bitmap = it.toBitmap().asImageBitmap(),
            contentDescription = contentDescription,
        )
    }
}
