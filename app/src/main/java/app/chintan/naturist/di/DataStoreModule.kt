package app.chintan.naturist.di

import android.app.Application
import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class PrefDataStoreModule {

    @Provides
    @Singleton
    fun getDataStore(app: Application) = getDataStoreInstance(app)

}

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "naturistDB")

fun getDataStoreInstance(androidApplication: Application): DataStore<Preferences> =
    androidApplication.dataStore

