package com.mtovar.rutaslocalesia.model.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.mtovar.rutaslocalesia.model.Ruta

@Entity(tableName = "rutas_favoritas")
data class RutaFavorita(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val nombre: String,
    val descripcion: String,
    val latitud: Double,
    val longitud: Double,
    val fechaGuardado: Long = System.currentTimeMillis()
)
// Una función de extensión para convertir fácilmente de tu modelo API a tu modelo DB
fun Ruta.toEntity() = RutaFavorita(
    nombre = this.nombre,
    descripcion = this.descripcion,
    latitud = this.latitud,
    longitud = this.longitud
)