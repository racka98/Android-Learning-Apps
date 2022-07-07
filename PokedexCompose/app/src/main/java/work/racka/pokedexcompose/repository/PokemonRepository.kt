package work.racka.pokedexcompose.repository

import dagger.hilt.android.scopes.ActivityScoped
import work.racka.pokedexcompose.data.remote.PokeApi
import work.racka.pokedexcompose.data.remote.responses.Pokemon
import work.racka.pokedexcompose.data.remote.responses.pokemons.PokemonList
import work.racka.pokedexcompose.util.Resource
import javax.inject.Inject


@ActivityScoped
class PokemonRepository @Inject constructor(
    private val api: PokeApi
) {

    // Get all the list of Pokemons
    suspend fun getPokemonList(limit: Int, offset: Int): Resource<PokemonList> {
        val response = try {
            api.getPokemonList(limit, offset)
        } catch (e: Exception) {
            return Resource.Error(message = "An error occurred: ${e.message}")
        }
        return Resource.Success(data = response)
    }

    // Get data for a Pokemon
    suspend fun getPokemon(pokemonName: String): Resource<Pokemon> {
        val response = try {
            api.getPokemonInfo(pokemonName)
        } catch (e: Exception) {
            return Resource.Error(message = "An error occurred: ${e.message}")
        }
        return Resource.Success(data = response)
    }
}