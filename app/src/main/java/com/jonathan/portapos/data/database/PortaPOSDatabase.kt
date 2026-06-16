package com.jonathan.portapos.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.jonathan.portapos.data.dao.OrderDao
import com.jonathan.portapos.data.dao.ProductDao
import com.jonathan.portapos.data.dao.SettingsDao
import com.jonathan.portapos.data.model.AppSettings
import com.jonathan.portapos.data.model.Order
import com.jonathan.portapos.data.model.OrderItem
import com.jonathan.portapos.data.model.Product

@Database(
    entities = [Product::class, Order::class, OrderItem::class, AppSettings::class],
    version = 2,
    exportSchema = false
)
abstract class PortaPOSDatabase : RoomDatabase() {

    abstract fun productDao(): ProductDao
    abstract fun orderDao(): OrderDao
    abstract fun settingsDao(): SettingsDao

    companion object {
        @Volatile
        private var INSTANCE: PortaPOSDatabase? = null

        fun getDatabase(context: Context): PortaPOSDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    PortaPOSDatabase::class.java,
                    "portapos_database"
                )
                .fallbackToDestructiveMigration() // Simple migration for development
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
