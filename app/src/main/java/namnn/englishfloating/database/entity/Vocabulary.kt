package namnn.englishfloating.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "language")
data class Vocabulary(
    @PrimaryKey(autoGenerate = true) val id: Int? = null,
    @ColumnInfo(name = "english") var english: String,
    @ColumnInfo(name = "vietnamese") var vietnamese: String,
    @ColumnInfo(name = "wrong_count") var wrongCount: Int,
    @ColumnInfo(name = "important") var important: Boolean
)