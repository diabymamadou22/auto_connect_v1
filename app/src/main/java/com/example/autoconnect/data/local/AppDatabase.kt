package com.example.autoconnect.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.autoconnect.data.SampleData
import com.example.autoconnect.util.PasswordHasher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        UserEntity::class,
        ServiceProviderEntity::class,
        ReviewEntity::class,
        TutorialEntity::class,
        BookingEntity::class,
        ChatMessageEntity::class,
        OfferedServiceEntity::class
    ],
    version = 5,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun userDao(): UserDao
    abstract fun serviceProviderDao(): ServiceProviderDao
    abstract fun reviewDao(): ReviewDao
    abstract fun tutorialDao(): TutorialDao
    abstract fun bookingDao(): BookingDao
    abstract fun chatMessageDao(): ChatMessageDao
    abstract fun offeredServiceDao(): OfferedServiceDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "auto_connect.db"
                )
                .addCallback(object : RoomDatabase.Callback() {
                    override fun onCreate(db: SupportSQLiteDatabase) {
                        super.onCreate(db)
                        // Seed default users and sample service providers
                        INSTANCE?.let { database ->
                            CoroutineScope(Dispatchers.IO).launch {
                                seedInitialData(database)
                            }
                        }
                    }
                })
                .fallbackToDestructiveMigration()
                .build()
                
                INSTANCE = instance
                instance
            }
        }

        private suspend fun seedInitialData(database: AppDatabase) {
            // Seed Users
            database.userDao().insertUser(
                UserEntity(
                    id = "u1",
                    username = "admin",
                    passwordHash = PasswordHasher.hash("admin", "00223"),
                    role = "admin"
                )
            )
            database.userDao().insertUser(
                UserEntity(
                    id = "u2",
                    username = "prestataire",
                    passwordHash = PasswordHasher.hash("prestataire", "prestataire"),
                    role = "prestataire"
                )
            )
            database.userDao().insertUser(
                UserEntity(
                    id = "u3",
                    username = "client",
                    passwordHash = PasswordHasher.hash("client", "client"),
                    role = "client"
                )
            )

            // Seed Sample Service Providers
            val entities = SampleData.sampleProviders.map { ServiceProviderEntity.fromDomainModel(it) }
            database.serviceProviderDao().insertAll(entities)

            // Seed Offline Troubleshooting Tutorials
            database.tutorialDao().insertAll(SampleData.sampleTutorials)

            // Seed Sample Bookings
            database.bookingDao().insertAll(SampleData.sampleBookings)

            // Seed Sample Chat Messages
            database.chatMessageDao().insertAll(SampleData.sampleChatMessages)

            // Seed Sample Reviews
            SampleData.sampleReviews.forEach { review ->
                database.reviewDao().insertReview(review)
            }

            // Seed Sample Offered Services
            database.offeredServiceDao().insertAll(SampleData.sampleOfferedServices)
        }
    }
}
