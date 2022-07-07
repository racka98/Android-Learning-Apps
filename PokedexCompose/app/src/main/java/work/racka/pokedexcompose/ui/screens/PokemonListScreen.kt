package work.racka.pokedexcompose.ui.screens

import android.content.res.Configuration
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment.Companion.BottomEnd
import androidx.compose.ui.Alignment.Companion.Center
import androidx.compose.ui.Alignment.Companion.CenterHorizontally
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Alignment.Companion.TopCenter
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.NavOptionsBuilder
import androidx.navigation.compose.rememberNavController
import coil.compose.rememberImagePainter
import com.google.accompanist.insets.navigationBarsPadding
import kotlinx.coroutines.launch
import work.racka.pokedexcompose.R
import work.racka.pokedexcompose.data.models.PokemonEntry
import work.racka.pokedexcompose.ui.theme.PokedexComposeTheme
import work.racka.pokedexcompose.ui.theme.Shapes
import work.racka.pokedexcompose.ui.viewModel.PokemonListViewModel

@ExperimentalAnimationApi
@Composable
fun PokemonListScreen(
    navController: NavController,
    modifier: Modifier = Modifier,
    statusBarOffset: Modifier = Modifier,
    viewModel: PokemonListViewModel = hiltViewModel()
) {
    Surface(
        color = MaterialTheme.colors.background,
        modifier = modifier.fillMaxSize()
    ) {

        Box(modifier = statusBarOffset.fillMaxSize()) {
            // Scrolling state
            val listState = rememberLazyListState()
            val showButton by remember {
                derivedStateOf {
                    listState.firstVisibleItemIndex > 0
                }
            }

            val coroutineScope = rememberCoroutineScope()

            Column(
                modifier = Modifier.align(TopCenter)
            ) {

                Spacer(modifier = Modifier.height(20.dp))

                Image(
                    painter = painterResource(id = R.drawable.ic_international_pok_mon_logo),
                    contentDescription = "Pokemon Logo",
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(CenterHorizontally)
                )

                SearchBar(
                    hint = "Search Pokemon...",
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp)
                        .padding(16.dp),
                    onSearch = {
                        viewModel.searchPokemonList(it)
                    }
                )

                PokemonList(
                    navController = navController,
                    viewModel = viewModel,
                    listState = listState
                )
            }
            AnimatedVisibility(
                visible = showButton,
                modifier = Modifier.align(BottomEnd)
            ) {
                FloatingActionButton(
                    onClick = {
                        coroutineScope.launch {
                            listState.scrollToItem(0)
                        }
                    },
                    modifier = Modifier
                        .padding(16.dp)
                        .navigationBarsPadding()
                        .align(BottomEnd),
                    backgroundColor = MaterialTheme.colors.primary
                ) {
                    Icon(
                        imageVector = Icons.Filled.ArrowUpward,
                        contentDescription = "Scroll Up button"
                    )
                }
            }
        }
    }
}

@Composable
fun SearchBar(
    modifier: Modifier = Modifier,
    hint: String,
    onSearch: (String) -> Unit = { }
) {
    var searchText by remember {
        mutableStateOf("")
    }

    var isHintDisplayed by remember {
        mutableStateOf(hint.isNotEmpty())
    }

    Box(modifier = modifier) {
        BasicTextField(
            value = searchText,
            onValueChange = {
                searchText = it
                onSearch(it)
            },
            maxLines = 1,
            singleLine = true,
            textStyle = TextStyle(color = MaterialTheme.colors.onSurface),
            modifier = Modifier
                .fillMaxWidth()
                .align(Center)
                .shadow(elevation = 5.dp, shape = Shapes.large)
                .background(color = MaterialTheme.colors.surface, shape = Shapes.large)
                .padding(horizontal = 20.dp, vertical = 20.dp)
                .onFocusChanged {
                    isHintDisplayed = !it.isFocused
                }
        )
        if (isHintDisplayed) {
            Text(
                text = hint,
                color = Color.LightGray,
                modifier = Modifier
                    .padding(horizontal = 20.dp, vertical = 20.dp)
            )
        }
    }
}

@Composable
fun PokedexEntry(
    entry: PokemonEntry,
    navController: NavController,
    modifier: Modifier = Modifier
) {
    val defaultDominantColor = MaterialTheme.colors.surface
    val dominantColor = MaterialTheme.colors.primary

    var isLoading by remember {
        mutableStateOf(true)
    }

    val coilPainter = rememberImagePainter(
        data = entry.imageUrl,
        builder = {
//            target {
//                calcDominantColor(it) { color ->
//                    dominantColor = color
//                }
//            }
            crossfade(true)
            listener(
                onStart = {
                    isLoading = true
                },
                onSuccess = { imageRequest, imageResult ->
                    isLoading = false
                    imageResult.dataSource

                }
            )
        }
    )

    Box(
        contentAlignment = Center,
        modifier = modifier
            .shadow(elevation = 5.dp, shape = Shapes.large)
            .clip(Shapes.large)
            .aspectRatio(1f)
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        dominantColor,
                        defaultDominantColor
                    )

                )
            )
            .clickable {
                navController.navigate(
                    route = "pokemonDetailsScreen/${dominantColor.toArgb()}/${entry.pokemonName}"
                )
            }
    ) {
        Column {
            Box(
                contentAlignment = Center,
                modifier = Modifier
                    .align(CenterHorizontally)
            ) {
                Image(
                    painter = coilPainter,
                    contentDescription = "Pokemon Image: ${entry.pokemonName}",
                    modifier = Modifier
                        .size(120.dp)
                )
                if (isLoading) {
                    CircularProgressIndicator(color = MaterialTheme.colors.primary)
                }
            }

            Text(
                text = entry.pokemonName,
                style = MaterialTheme.typography.subtitle1,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@ExperimentalFoundationApi
@Composable
fun PokemonGrid() {
    LazyVerticalGrid(
        cells = GridCells.Fixed(2)
    ) {

    }
}

@Composable
fun PokedexRow(
    rowIndex: Int,
    entries: List<PokemonEntry>,
    navController: NavController
) {
    Column {
        Row {
            PokedexEntry(
                entry = entries[rowIndex * 2],
                navController = navController,
                modifier = Modifier.weight(1f)
            )
            Spacer(modifier = Modifier.width(16.dp))
            if (entries.size >= rowIndex * 2 + 2) {
                PokedexEntry(
                    entry = entries[rowIndex * 2 + 1],
                    navController = navController,
                    modifier = Modifier.weight(1f)
                )
            } else {
                Spacer(modifier = Modifier.weight(1f))
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
    }
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun PokemonList(
    navController: NavController,
    viewModel: PokemonListViewModel = hiltViewModel(),
    listState: LazyListState = rememberLazyListState()
) {
    val pokemonList by remember {
        derivedStateOf {
            viewModel.pokemonList.distinct()
        }
    }
    val endReached by remember { viewModel.endReached }
    val loadError by remember { viewModel.loadError }
    val isLoading by remember { viewModel.isLoading }
    val isSearching by remember { viewModel.isSearching }

    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        state = listState
    ) {
        val itemCount = if (pokemonList.size % 2 == 0) {
            pokemonList.size / 2
        } else {
            pokemonList.size / 2 + 1
        }
        items(itemCount) {
            if (it >= itemCount - 1 && !isLoading && !isSearching) {
                viewModel.loadPokemonPaginated()
            }
            PokedexRow(
                rowIndex = it,
                entries = pokemonList,
                navController = navController
            )
        }
        item {

        }
        item {
            AnimatedVisibility(
                visible = isLoading,
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    contentAlignment = Center
                ) {
                    CircularProgressIndicator()
                }
            }
            AnimatedVisibility(
                visible = loadError.isNotEmpty(),
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    contentAlignment = Center
                ) {
                    RetrySection(error = loadError) {
                        viewModel.loadPokemonPaginated()
                    }
                }
            }
            Spacer(modifier = Modifier.navigationBarsPadding())
        }
    }
    Box(contentAlignment = Center, modifier = Modifier.fillMaxSize()) {

    }
}

@Composable
fun RetrySection(
    error: String,
    onRetry: () -> Unit
) {
    Column {
        Text(
            text = error,
            color = MaterialTheme.colors.error,
            modifier = Modifier.align(CenterHorizontally),
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(8.dp))
        Button(
            onClick = { onRetry() },
            modifier = Modifier.align(CenterHorizontally),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 8.dp),
            shape = CircleShape,
            colors = ButtonDefaults
                .buttonColors(
                    backgroundColor = MaterialTheme.colors.error,
                    contentColor = MaterialTheme.colors.onError
                )
        ) {
            Icon(
                imageVector = Icons.Filled.Refresh,
                contentDescription = "Refresh Icon",
                modifier = Modifier.align(CenterVertically)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Retry",
                style = MaterialTheme.typography.h6,
                modifier = Modifier.align(CenterVertically)
            )
        }
    }
}


@ExperimentalAnimationApi
@Preview(
    uiMode = Configuration.UI_MODE_NIGHT_YES,
    device = Devices.PIXEL_4
)
@Composable
private fun PokemonLisScreenPreview() {
    val navController = rememberNavController()
    PokedexComposeTheme {
        PokemonListScreen(navController = navController)
    }
}

@Preview(showBackground = true)
@Composable
private fun RetrySectionPreview() {

    PokedexComposeTheme {
        RetrySection(error = "There was an error with this request") {

        }
    }
}
