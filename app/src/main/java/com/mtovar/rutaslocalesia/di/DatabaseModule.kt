package com.mtovar.rutaslocalesia.di

import android.content.Context
import androidx.room.Room
import com.mtovar.rutaslocalesia.data.local.AppDatabase
import com.mtovar.rutaslocalesia.data.local.RutasDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "rutas_db"
        ).build()
    }

    @Provides
    fun provideRutasDao(database: AppDatabase): RutasDao {
        return database.rutasDao()
    }
}