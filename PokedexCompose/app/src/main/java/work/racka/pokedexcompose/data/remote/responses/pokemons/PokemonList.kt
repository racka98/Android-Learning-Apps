package work.racka.pokedexcompose.data.remote.responses.pokemons

data class PokemonList(
    val count: Int,
    val next: String,
    val previous: Any,
    val results: List<Result>
)