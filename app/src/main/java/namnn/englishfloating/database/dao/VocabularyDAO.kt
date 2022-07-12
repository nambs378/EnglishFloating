package namnn.englishfloating.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import namnn.englishfloating.database.entity.Vocabulary

@Dao
interface VocabularyDAO {
    @Query("SELECT * FROM language")
    fun getALL(): MutableList<Vocabulary>

    @Query("SELECT * FROM language ORDER BY RANDOM() LIMIT 1")
    fun getRandomEnglish(): Vocabulary

//    @Query("SELECT vietnamese FROM language WHERE id not in (SELECT id LIKE :id FROM language) ORDER BY RANDOM() LIMIT 3")
    @Query("SELECT vietnamese FROM language WHERE id <> :id ORDER BY RANDOM() LIMIT 3")
    fun getRandomEnglishWithoutCurrentEnglish(id: Int): MutableList<String>

    @Query("SELECT * FROM language WHERE english LIKE :english LIMIT 1")
    fun findByEnglish(english: String): Vocabulary


    @Query("SELECT CASE WHEN EXISTS (SELECT * FROM language WHERE english = :english) THEN CAST (1 AS BIT) ELSE CAST (0 AS BIT) END")
    fun checkEnglishExist(english: String): Boolean

    @Insert
    fun insert(vararg lang: Vocabulary)

    @Delete
    fun delete(lang: Vocabulary)
}