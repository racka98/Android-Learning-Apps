package work.racka.pokedexcompose

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.toLowerCase
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.view.WindowCompat
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.navArgument
import androidx.navigation.compose.rememberNavController
import com.google.accompanist.insets.ProvideWindowInsets
import com.google.accompanist.insets.navigationBarsPadding
import com.google.accompanist.insets.statusBarsPadding
import dagger.hilt.android.AndroidEntryPoint
import work.racka.pokedexcompose.ui.screens.PokemonDetailScreen
import work.racka.pokedexcompose.ui.screens.PokemonListScreen
import work.racka.pokedexcompose.ui.theme.PokedexComposeTheme
import java.util.*


@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    @ExperimentalAnimationApi
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        WindowCompat.setDecorFitsSystemWindows(window, false)
        setContent {
            ProvideWindowInsets {
                PokedexComposeTheme {
                    val navController = rememberNavController()
                    NavHost(
                        navController = navController,
                        startDestination = "pokemonListScreen"
                        ) {

                        composable(route = "pokemonListScreen") {
                            PokemonListScreen(
                                navController = navController,
                                statusBarOffset = Modifier.statusBarsPadding()
                            )
                        }

                        composable(
                            route = "pokemonDetailsScreen/{dominantColor}/{pokemonName}",
                            arguments = listOf(
                                navArgument(name = "dominantColor") {
                                    type = NavType.IntType
                                },
                                navArgument(name = "pokemonName") {

                                }
                            )
                        ) { entry ->
                            val dominantColor = remember {
                                val color = entry.arguments?.getInt("dominantColor")
                                color?.let { Color(it) } ?: Color.White
                            }
                            val pokemonName = remember {
                                entry.arguments?.getString("pokemonName")
                            }
                            
                            PokemonDetailScreen(
                                dominantColor = dominantColor,
                                pokemonName = pokemonName?.lowercase(Locale.ROOT) ?: "Error!",
                                navController = navController
                            )
                        }
                    }
                }
            }
        }
    }
}
