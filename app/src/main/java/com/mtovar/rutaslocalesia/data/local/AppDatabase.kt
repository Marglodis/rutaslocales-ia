package com.mtovar.rutaslocalesia.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.mtovar.rutaslocalesia.model.entity.RutaFavorita

@Database(entities = [RutaFavorita::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun rutasDao(): RutasDao
}