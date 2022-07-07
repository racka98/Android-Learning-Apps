package work.racka.runningtracker.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.android.scopes.ActivityScoped
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import timber.log.Timber
import work.racka.runningtracker.di.IoDispatcher
import work.racka.runningtracker.util.Constants
import java.io.IOException
import javax.inject.Inject

@ActivityScoped
class DataStoreRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    @IoDispatcher private val dispatcher: CoroutineDispatcher
) {

    private object PreferenceKeys {
        val weightInKg = intPreferencesKey("weight_kg")
        val name = stringPreferencesKey("user_name")
    }

    // DataStore
    private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(
        name = Constants.SETTINGS_PREFERENCES,
        scope = CoroutineScope(dispatcher)
    )

    // Write
    private suspend fun <T> Context.writePreference(
        preferenceKey: Preferences.Key<T>,
        value: T
    ) = this.dataStore.edit { preferences ->
        preferences[preferenceKey] = value
    }

    // Read
    private fun <T> Context.readPreference(
        preferenceKey: Preferences.Key<T>,
        defaultValue: T
    ): Flow<T> = this.dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                Timber.d(exception.message.toString())
            } else {
                throw exception
            }
        }.map { preferences ->
            preferences[preferenceKey] ?: defaultValue
        }

    // Write username
    suspend fun saveUserName(name: String) = context.writePreference(
        preferenceKey = PreferenceKeys.name,
        value = name
    )

    // Read username
    val readUserName: Flow<String> = context.readPreference(
        preferenceKey = PreferenceKeys.name,
        defaultValue = ""
    )

    // Write weight
    suspend fun saveWeightInKg(weight: Int) = context.writePreference(
        preferenceKey = PreferenceKeys.weightInKg,
        value = weight
    )

    // Read weight
    val readWeightInKg: Flow<Int> = context.readPreference(
        preferenceKey = PreferenceKeys.weightInKg,
        defaultValue = 0
    )

}