package work.racka.runningtracker.di

import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.content.SharedPreferences
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import work.racka.runningtracker.util.Constants.KEY_FIRST_TIME_TOGGLE
import work.racka.runningtracker.util.Constants.KEY_NAME
import work.racka.runningtracker.util.Constants.KEY_WEIGHT
import work.racka.runningtracker.util.Constants.SHARED_PREFERENCES_NAME
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object SharedPreferencesModule {

    @Provides
    @Singleton
    fun providesSharedPreferences(
        @ApplicationContext context: Context
    ): SharedPreferences = context
        .getSharedPreferences(SHARED_PREFERENCES_NAME, MODE_PRIVATE)

    @Provides
    @Singleton
    fun providesName(
        sharedPreferences: SharedPreferences
    ): String = sharedPreferences.getString(KEY_NAME, "") ?: ""

    @Provides
    @Singleton
    fun providesWeight(
        sharedPreferences: SharedPreferences
    ): Float = sharedPreferences.getFloat(KEY_WEIGHT, 1f)

    @Provides
    @Singleton
    fun providesFirstTimeToggle(
        sharedPreferences: SharedPreferences
    ): Boolean = sharedPreferences.getBoolean(KEY_FIRST_TIME_TOGGLE, true)

}