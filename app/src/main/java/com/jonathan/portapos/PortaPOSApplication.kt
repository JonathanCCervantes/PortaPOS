package com.jonathan.portapos

import android.app.Application
import com.jonathan.portapos.data.PortaPOSRepository
import com.jonathan.portapos.data.database.PortaPOSDatabase

class PortaPOSApplication : Application() {

    val database by lazy { PortaPOSDatabase.getDatabase(this) }

    val repository by lazy {
        PortaPOSRepository(
            database.productDao(),
            database.orderDao(),
            database.settingsDao()
        )
    }
}
