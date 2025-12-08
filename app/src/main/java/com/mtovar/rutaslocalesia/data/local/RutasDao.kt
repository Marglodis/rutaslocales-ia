package com.mtovar.rutaslocalesia.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.mtovar.rutaslocalesia.model.entity.RutaFavorita
import kotlinx.coroutines.flow.Flow

@Dao
interface RutasDao {
    // TRAER: Solo las rutas de ESTE usuario
    @Query("SELECT * FROM rutas_favoritas WHERE userId = :userId ORDER BY fechaGuardado DESC")
    fun obtenerTodas(userId: String): Flow<List<RutaFavorita>>

    // Usamos Flow para que si la DB cambia, la UI se actualice sola en tiempo real
    @Query("SELECT * FROM rutas_favoritas ORDER BY fechaGuardado DESC")
    fun obtenerTodas(): Flow<List<RutaFavorita>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun guardarRuta(ruta: RutaFavorita)

    @Delete
    suspend fun eliminarRuta(ruta: RutaFavorita)

    // BORRAR: Solo si coincide nombre Y usuario (para no borrar las de otro)
    @Query("DELETE FROM rutas_favoritas WHERE nombre = :nombreRuta AND userId = :userId")
    suspend fun eliminarPorNombre(nombreRuta: String, userId: String)
}