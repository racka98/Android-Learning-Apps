package work.racka.pokedexcompose.util

import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import androidx.compose.ui.graphics.Color
import androidx.palette.graphics.Palette

fun calcDominantColor(drawable: Drawable, onFinished: (Color) -> Unit) {
    val bitmap = (drawable as BitmapDrawable).bitmap.copy(
        Bitmap.Config.ARGB_8888,
        true
    )
    Palette.from(bitmap).generate { palette ->
        palette?.dominantSwatch?.rgb?.let { colorValue ->
            onFinished(Color(colorValue))
        }
    }
}
