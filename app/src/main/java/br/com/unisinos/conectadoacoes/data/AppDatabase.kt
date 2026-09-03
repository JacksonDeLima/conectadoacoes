package br.com.unisinos.conectadoacoes.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * Banco de dados principal do aplicativo utilizando Room sobre SQLite nativo.
 * Version 3 com suporte a plataforma multientidades e gestão de equipe de voluntários.
 */
@Database(
    entities = [Donation::class, Ngo::class, Volunteer::class],
    version = 3,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun donationDao(): DonationDao
    abstract fun ngoDao(): NgoDao
    abstract fun volunteerDao(): VolunteerDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "conectadoacoes_database"
                )
                    .fallbackToDestructiveMigration()
                    .addCallback(object : Callback() {
                        override fun onCreate(db: SupportSQLiteDatabase) {
                            super.onCreate(db)
                            // Preenche o banco com dados iniciais de ONGs e Voluntários
                            CoroutineScope(Dispatchers.IO).launch {
                                INSTANCE?.let { database ->
                                    database.ngoDao().insertAll(Ngo.DEFAULT_NGOS)
                                    database.volunteerDao().insertAll(Volunteer.DEFAULT_VOLUNTEERS)
                                }
                            }
                        }
                    })
                    .build()
                INSTANCE = instance

                // Garantia adicional de seeding caso o banco já exista mas esteja vazio
                CoroutineScope(Dispatchers.IO).launch {
                    try {
                        if (instance.ngoDao().getNgoCount() == 0) {
                            instance.ngoDao().insertAll(Ngo.DEFAULT_NGOS)
                        }
                        if (instance.volunteerDao().getVolunteerCount() == 0) {
                            instance.volunteerDao().insertAll(Volunteer.DEFAULT_VOLUNTEERS)
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }

                instance
            }
        }
    }
}
