package com.example.newsapp.ui.compose.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import coil.compose.AsyncImagePainter
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import com.example.newsapp.R
import com.example.newsapp.core.helpers.shimmerEffect

@Composable
fun ArticleImage(
    imageUrl: String?,
    title: String,
    modifier: Modifier = Modifier
) {
    val painter = rememberAsyncImagePainter(
        model = ImageRequest.Builder(LocalContext.current)
            .data(imageUrl)
            .crossfade(500) // Smooth 500ms fade-in when the image finally loads!
            .build()
    )

    // Check the state to decide if we apply the shimmer or just a solid background
    val isLoading = painter.state is AsyncImagePainter.State.Loading
    val isError = painter.state is AsyncImagePainter.State.Error

    Box(
        modifier = modifier
            // If loading, sweep the shimmer. If error/success, stay solid gray.
            .then(
                if (isLoading) Modifier.shimmerEffect()
                else Modifier.background(MaterialTheme.colorScheme.surfaceVariant)
            ),
        contentAlignment = Alignment.Center
    ) {
        if (isError) {
            // ERROR STATE: Draw ONLY your custom PNG
            Image(
                painter = painterResource(id = R.drawable.no_image_placeholder),
                contentDescription = stringResource(R.string.cd_no_image_available),
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        } else {
            // SUCCESS & LOADING STATE: Draw the Coil Image
            // (Coil needs to be in the UI tree while loading to trigger the network request)
            Image(
                painter = painter,
                contentDescription = title,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}