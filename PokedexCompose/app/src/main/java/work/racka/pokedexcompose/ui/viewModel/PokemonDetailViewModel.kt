package work.racka.pokedexcompose.ui.viewModel

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import work.racka.pokedexcompose.data.remote.responses.Pokemon
import work.racka.pokedexcompose.repository.PokemonRepository
import work.racka.pokedexcompose.util.Resource
import javax.inject.Inject

@HiltViewModel
class PokemonDetailViewModel @Inject constructor(
    private val repository: PokemonRepository
) : ViewModel() {
    suspend fun getPokemonInfo(pokemonName: String): Resource<Pokemon> {
        return repository.getPokemon(pokemonName)
    }
}