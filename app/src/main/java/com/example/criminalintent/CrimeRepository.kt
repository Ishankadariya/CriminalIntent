
package com.example.criminalintent

import android.content.Context
import androidx.room.Room
import kotlinx.coroutines.flow.Flow
import java.util.UUID

class CrimeRepository private constructor(
    context: Context
) {
    private val database = Room.databaseBuilder(
        context.applicationContext,
        CrimeDatabase::class.java,
        "crime-database"
    ).build()

    private val crimeDao = database.crimeDao()

    fun getCrimes(): Flow<List<Crime>> {
        return crimeDao.getCrimes()
    }

    suspend fun getCrime(id: UUID): Crime? {
        return crimeDao.getCrime(id)
    }

    suspend fun addCrime(crime: Crime) {
        crimeDao.insertCrime(crime)
    }

    suspend fun updateCrime(crime: Crime) {
        crimeDao.updateCrime(crime)
    }

    suspend fun deleteCrime(crime: Crime) {
        crimeDao.deleteCrime(crime)
    }

    companion object {
        @Volatile
        private var INSTANCE: CrimeRepository? = null

        fun getInstance(context: Context): CrimeRepository {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: CrimeRepository(context).also {
                    INSTANCE = it
                }
            }
        }
    }
}
