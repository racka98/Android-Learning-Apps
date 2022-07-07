package work.racka.pokedexcompose.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import work.racka.pokedexcompose.data.remote.PokeApi
import work.racka.pokedexcompose.repository.PokemonRepository
import work.racka.pokedexcompose.util.Constants.BASE_URL
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Singleton
    @Provides
    fun providesPokemonRepository(api: PokeApi) = PokemonRepository(api)

    @Singleton
    @Provides
    fun providesPokeApi(): PokeApi =
        Retrofit.Builder()
            .addConverterFactory(GsonConverterFactory.create())
            .baseUrl(BASE_URL)
            .build()
            .create(PokeApi::class.java)
}