package namnn.englishfloating.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import namnn.englishfloating.database.dao.VocabularyDAO
import namnn.englishfloating.database.entity.Vocabulary

@Database(entities = [Vocabulary::class], version = 2)
public abstract class AppDatabase : RoomDatabase() {
    public abstract fun languageDao(): VocabularyDAO

    private val DATABASE_NAME = "english_db"

    private var INSTANCE: AppDatabase? = null

    open fun getAppDatabase(context: Context): AppDatabase? {
        if (INSTANCE == null) {
            INSTANCE = Room.databaseBuilder(
                context.getApplicationContext(),
                AppDatabase::class.java, DATABASE_NAME
            ) // allow queries on the main thread.
                // Don't do this on a real app! See PersistenceBasicSample for an example.
                .allowMainThreadQueries()
                .build()
        }
        return INSTANCE
    }

    open fun destroyInstance() {
        INSTANCE = null
    }
}