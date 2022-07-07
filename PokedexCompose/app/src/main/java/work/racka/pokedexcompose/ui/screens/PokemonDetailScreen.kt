package work.racka.pokedexcompose.ui.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement.SpaceBetween
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Alignment.Companion.BottomCenter
import androidx.compose.ui.Alignment.Companion.Center
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Alignment.Companion.TopStart
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.layoutId
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.font.FontWeight.Companion.Bold
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.rememberImagePainter
import com.google.accompanist.insets.navigationBarsPadding
import com.google.accompanist.insets.statusBarsPadding
import work.racka.pokedexcompose.R
import work.racka.pokedexcompose.data.remote.responses.Pokemon
import work.racka.pokedexcompose.data.remote.responses.Type
import work.racka.pokedexcompose.ui.theme.Shapes
import work.racka.pokedexcompose.ui.viewModel.PokemonDetailViewModel
import work.racka.pokedexcompose.util.Resource
import work.racka.pokedexcompose.util.parseStatToAbbr
import work.racka.pokedexcompose.util.parseStatToColor
import work.racka.pokedexcompose.util.parseTypeToColor
import java.util.*
import kotlin.math.round

@Composable
fun PokemonDetailScreen(
    dominantColor: Color,
    pokemonName: String,
    navController: NavController,
    modifier: Modifier = Modifier,
    statusBarOffset: Dp = 20.dp,
    viewModel: PokemonDetailViewModel = hiltViewModel()
) {
    val pokemonInfo = produceState<Resource<Pokemon>>(
        initialValue = Resource.Loading()
    ) {
        value = viewModel.getPokemonInfo(pokemonName)
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(dominantColor)
            .padding(bottom = 16.dp)
            .navigationBarsPadding()
    ) {

        PokemonDetailTopSection(
            navController = navController,
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.2f)
                .align(Alignment.TopCenter)
        )

        PokemonDetailStateWrapper(
            pokemonInfo = pokemonInfo.value,
            modifier = Modifier
                .fillMaxSize()
                .padding(
                    top = statusBarOffset + 100.dp,
                    start = 16.dp,
                    end = 16.dp,
                    bottom = 8.dp
                )
                .shadow(
                    elevation = 10.dp,
                    shape = Shapes.large,
                    clip = true
                )
                .background(MaterialTheme.colors.surface)
                .padding(16.dp)
                .align(BottomCenter),
            loadingModifier = Modifier
                .size(100.dp)
                .align(Center)
                .padding(
                    top = statusBarOffset + 100.dp,
                    start = 16.dp,
                    end = 16.dp,
                    bottom = 8.dp
                ),
            onRetry = { /*TODO*/ }
        )

        Box(
            contentAlignment = Alignment.TopCenter,
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
        ) {
            if (pokemonInfo.value is Resource.Success) {
                pokemonInfo.value.data?.sprites?.let {
                    Image(
                        painter = rememberImagePainter(
                            data = it.front_default,
                            builder = {
                                crossfade(true)
                            }
                        ),
                        contentDescription = "Image of $pokemonName",
                        modifier = Modifier
                            .size(200.dp)
                            .offset(y = statusBarOffset)
                    )
                }
            }
        }

    }
}

@Composable
fun PokemonDetailTopSection(
    navController: NavController,
    modifier: Modifier = Modifier
) {
    Box(
        contentAlignment = TopStart,
        modifier = modifier
            .background(
                brush = Brush.verticalGradient(
                    listOf(
                        Color.Black,
                        Color.Transparent
                    )
                )
            )
    ) {
        Column(Modifier.statusBarsPadding()) {
            Icon(
                imageVector = Icons.Default.ArrowBack,
                contentDescription = "Back Arrow",
                tint = Color.White,
                modifier = Modifier
                    .size(36.dp)
                    .offset(16.dp, 16.dp)
                    .clickable {
                        navController.popBackStack()
                    }
            )
        }
    }
}

@Composable
fun PokemonDetailStateWrapper(
    pokemonInfo: Resource<Pokemon>,
    modifier: Modifier = Modifier,
    onRetry: () -> Unit,
    loadingModifier: Modifier = Modifier
) {
    when (pokemonInfo) {
        is Resource.Success -> {
            PokemonDetailSection(
                pokemonInfo = pokemonInfo.data!!,
                modifier = modifier

            )
        }
        is Resource.Error -> {
            Box(
                modifier = modifier,
                contentAlignment = Center
            ) {
                RetrySection(
                    error = pokemonInfo.message!!,
                    onRetry = onRetry
                )
            }
        }
        is Resource.Loading -> {
            CircularProgressIndicator(loadingModifier)
        }
    }
}

@Composable
fun PokemonDetailSection(
    pokemonInfo: Pokemon,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .fillMaxSize()
            .offset(y = 100.dp)
            .verticalScroll(scrollState)
    ) {
        Text(
            text = "#${pokemonInfo.id} ${
                pokemonInfo.name.replaceFirstChar {
                    if (it.isLowerCase()) it.titlecase(
                        Locale.getDefault()
                    ) else it.toString()
                }
            }",
            fontWeight = Bold,
            fontSize = 30.sp,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colors.onSurface
        )
        PokemonTypeSection(types = pokemonInfo.types)
        PokemonDetailDataSection(
            pokemonWeight = pokemonInfo.weight,
            pokemonHeight = pokemonInfo.height
        )
        PokemonBaseStats(pokemonInfo = pokemonInfo)
    }
}

@Composable
fun PokemonTypeSection(
    types: List<Type>
) {
    Row(
        verticalAlignment = CenterVertically,
        modifier = Modifier
            .padding(16.dp)
    ) {
        for (type in types) {
            Box(
                contentAlignment = Center,
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 8.dp)
                    .clip(CircleShape)
                    .background(color = parseTypeToColor(type))
                    .height(35.dp)
            ) {
                Text(
                    text = type.type.name.replaceFirstChar {
                        if (it.isLowerCase()) it.titlecase(
                            Locale.ROOT
                        ) else it.toString()
                    },
                    color = Color.White,
                    fontSize = 18.sp
                )
            }
        }
    }
}

@Composable
fun PokemonDetailDataSection(
    pokemonWeight: Int,
    pokemonHeight: Int,
    sectionHeight: Dp = 80.dp
) {
    val pokemonWeightInKg = remember {
        round(pokemonWeight * 100f) / 1000f
    }
    val pokemonHeightInMeters = remember {
        round(pokemonHeight * 100f) / 1000f
    }

    Row(modifier = Modifier.fillMaxWidth()) {
        PokemonDetailDataItem(
            dataValue = pokemonWeightInKg,
            dataUnit = "Kg",
            dataIcon = painterResource(
                id = R.drawable.ic_weight
            ),
            modifier = Modifier.weight(1f)
        )
        Spacer(
            modifier = Modifier
                .size(1.dp, sectionHeight)
                .background(Color.LightGray)
        )
        PokemonDetailDataItem(
            dataValue = pokemonHeightInMeters,
            dataUnit = "m",
            dataIcon = painterResource(
                id = R.drawable.ic_height
            ),
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
fun PokemonDetailDataItem(
    dataValue: Float,
    dataUnit: String,
    dataIcon: Painter,
    modifier: Modifier = Modifier
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = modifier
    ) {
        Icon(
            painter = dataIcon,
            contentDescription = null,
            tint = MaterialTheme.colors.onSurface
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "$dataValue $dataUnit",
            color = MaterialTheme.colors.onSurface
        )
    }
}

@Composable
fun PokemonStat(
    statName: String,
    statValue: Int,
    statMaxValue: Int,
    statColor: Color,
    height: Dp = 28.dp,
    animDuration: Int = 1000,
    animDelay: Int = 0
) {
    var animationPlayed by remember {
        mutableStateOf(false)
    }
    val currentPercent = animateFloatAsState(
        targetValue = if (animationPlayed) {
            statValue / statMaxValue.toFloat()
        } else 0f,
        animationSpec = tween(
            durationMillis = animDuration,
            delayMillis = animDelay
        )
    )

    LaunchedEffect(key1 = true) {
        animationPlayed = true
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(height)
            .clip(CircleShape)
            .background(
                if (isSystemInDarkTheme()) {
                    Color.DarkGray
                } else Color.LightGray
            )
    ) {
        Row(
            horizontalArrangement = SpaceBetween,
            verticalAlignment = CenterVertically,
            modifier = Modifier
                .fillMaxHeight()
                .fillMaxWidth(currentPercent.value)
                .clip(CircleShape)
                .background(statColor)
                .padding(horizontal = 8.dp)
        ) {
            Text(
                text = statName,
                fontWeight = Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = (currentPercent.value * statMaxValue)
                    .toInt()
                    .toString(),
                fontWeight = Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
fun PokemonBaseStats(
    pokemonInfo: Pokemon,
    animationDelayPerItem: Int = 100
) {
    val maxBaseStat = remember {
        pokemonInfo.stats.maxOf {
            it.base_stat
        }
    }
    Column(Modifier.fillMaxWidth()) {
        Text(
            text = "Base Stats:",
            fontSize = 20.sp,
            color = MaterialTheme.colors.onSurface
        )
        Spacer(modifier = Modifier.height(8.dp))

        for (item in pokemonInfo.stats.indices) {
            val stat = pokemonInfo.stats[item]
            PokemonStat(
                statName = parseStatToAbbr(stat),
                statValue = stat.base_stat,
                statMaxValue = maxBaseStat,
                statColor = parseStatToColor(stat),
                animDelay = item * animationDelayPerItem
            )
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}
