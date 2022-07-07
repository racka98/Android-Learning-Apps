package work.racka.runningtracker.di

import android.content.Context
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import work.racka.runningtracker.data.database.RunDao
import work.racka.runningtracker.data.database.RunningDatabase
import work.racka.runningtracker.util.Constants.RUNNING_TABLE
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Singleton
    @Provides
    fun providesRunningDatabase(
        @ApplicationContext app: Context
    ): RunningDatabase = Room.databaseBuilder(
        app,
        RunningDatabase::class.java,
        RUNNING_TABLE
    ).build()

    @Singleton
    @Provides
    fun providesRunDao(
        database: RunningDatabase
    ): RunDao = database.runDao

}