package namnn.englishfloating.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "language")
data class Vocabulary(
    @PrimaryKey(autoGenerate = true) val id: Int? = null,
    @ColumnInfo(name = "english") val english: String?,
    @ColumnInfo(name = "vietnamese") val vietnamese: String?
)