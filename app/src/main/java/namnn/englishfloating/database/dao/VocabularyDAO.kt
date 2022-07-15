package namnn.englishfloating.database.dao

import androidx.room.*
import namnn.englishfloating.database.entity.Vocabulary

@Dao
interface VocabularyDAO {
    @Query("SELECT * FROM language")
    fun getALL(): MutableList<Vocabulary>

    @Query("SELECT * FROM language ORDER BY wrong_count DESC")
    fun getALLOrderByWrongCount(): MutableList<Vocabulary>

    @Query("SELECT * FROM language ORDER BY important = 1 DESC")
    fun getALLOrderByImportant(): MutableList<Vocabulary>

    @Query("SELECT * FROM language WHERE important = 1")
    fun getImportant(): MutableList<Vocabulary>

    @Query("SELECT * FROM language ORDER BY RANDOM() LIMIT 1")
    fun getRandomEnglish(): Vocabulary

    @Query("SELECT * FROM language WHERE important = 1 ORDER BY RANDOM() LIMIT 1")
    fun getRandomEnglishImportant(): Vocabulary

    @Query("SELECT * FROM language WHERE id <> :id AND important = 1 ORDER BY RANDOM() LIMIT 1")
    fun getRandomEnglishImportantAvoidId(id: Int): Vocabulary

    @Query("SELECT * FROM language WHERE id <> :id ORDER BY RANDOM() LIMIT 1")
    fun getRandomEnglishAvoidId(id: Int): Vocabulary

//    @Query("SELECT vietnamese FROM language WHERE id not in (SELECT id LIKE :id FROM language) ORDER BY RANDOM() LIMIT 3")
    @Query("SELECT * FROM language WHERE id <> :id ORDER BY RANDOM() LIMIT 3")
    fun getRandomEnglishWithoutCurrentEnglish(id: Int): MutableList<Vocabulary>

    @Query("SELECT * FROM language WHERE id <> :id AND important = 1 ORDER BY RANDOM() LIMIT 3")
    fun getRandomEnglishImportantWithoutCurrentEnglish(id: Int): MutableList<Vocabulary>

    @Query("SELECT * FROM language WHERE english LIKE :english LIMIT 1")
    fun findByEnglish(english: String): Vocabulary

    @Query("SELECT CASE WHEN EXISTS (SELECT * FROM language WHERE english = :english) THEN CAST (1 AS BIT) ELSE CAST (0 AS BIT) END")
    fun checkEnglishExist(english: String): Boolean

    @Query("UPDATE language SET wrong_count = :count WHERE id = :id ")
    fun updateWrongCountById(id: Int, count: Int)

    @Query("UPDATE language SET important = :important WHERE id = :id ")
    fun updateImportantById(id: Int, important: Boolean)

    @Insert
    fun insert(vararg vocabulary: Vocabulary)

    @Delete
    fun delete(vocabulary: Vocabulary)

    @Update
    fun update(vocabulary: Vocabulary)
}