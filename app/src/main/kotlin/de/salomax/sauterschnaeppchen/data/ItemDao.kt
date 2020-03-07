package de.salomax.sauterschnaeppchen.data

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface ItemDao {
    @Query("SELECT * FROM items")
    fun getAll(): LiveData<Array<Item>>

    @Query("SELECT * FROM items WHERE description LIKE :searchTerm")
    fun getByDescripiton(searchTerm: String): LiveData<List<Item>>

    @Insert(onConflict =  OnConflictStrategy.REPLACE)
    fun insertItems(items: Array<Item>)
}
