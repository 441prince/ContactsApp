package com.prince.contactsapp.models

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface ProfileDao {
    @Query("SELECT * FROM profile_table ORDER BY profileName ASC")
    fun getAllProfiles(): Flow<List<Profile>>

    // Check if there are any profiles; if not, insert a default profile
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertDefaultProfileIfNotExists(profile: Profile)

    @Query("SELECT * FROM profile_table WHERE isSelected = 1")
    fun getSelectedProfile(): Profile

    @Query("UPDATE profile_table SET isSelected = 0")
    suspend fun deselectAllProfiles()

    @Query("UPDATE profile_table SET isSelected = 1 WHERE profileId = :profileId")
    suspend fun selectProfile(profileId: Long)

    @Query("SELECT * FROM profile_table WHERE profileId = :profileId")
    suspend fun getProfileById(profileId: Long): Profile?

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(profile: Profile)

    //(onConflict = OnConflictStrategy.IGNORE)
    /**
     * It does not throw any sort of exception.
     * It tries to process everything that the statement should affect.
     */

    @Update(onConflict = OnConflictStrategy.IGNORE)
    suspend fun update(profile: Profile)

    @Query("DELETE FROM profile_table WHERE profileId = :profileId")
    suspend fun deleteProfileById(profileId: Long)
}
