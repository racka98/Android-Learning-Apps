package work.racka.pokedexcompose.data.remote.responses

data class Ability(
    val ability: AbilityX,
    val is_hidden: Boolean,
    val slot: Int
)