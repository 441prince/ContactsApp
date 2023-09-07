package com.prince.contactsapp.models

import androidx.annotation.WorkerThread
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull

class ProfileRepository(private val profileDao: ProfileDao) {

    val allProfiles: Flow<List<Profile>> = profileDao.getAllProfiles()

    // Function to get a contact by phone number
    suspend fun getProfileById(profileId: Long): Profile? {
        return profileDao.getProfileById(profileId)
    }
    suspend fun insert(profile: Profile) {
        profileDao.insert(profile)
    }

    suspend fun update(profile: Profile) {
        profileDao.update(profile)
    }

    @Suppress("RedundantSuspendModifier")
    @WorkerThread
    suspend fun deleteProfileById(profileId: Long) {
        profileDao.deleteProfileById(profileId)
    }

    suspend fun checkAndInsertDefaultProfile(profile: Profile) {
        // Check if there are any profiles; if not, insert a default profile
        val profiles = profileDao.getAllProfiles().firstOrNull()
        if (profiles == null || profiles.isEmpty()) {
            profileDao.insertDefaultProfileIfNotExists(profile)
            profileDao.selectProfile(profile.id)
        }
    }

    suspend fun getSelectedProfile(): Profile {
        return profileDao.getSelectedProfile()
    }

    suspend fun deselectAllProfiles() {
        profileDao.deselectAllProfiles()
    }

    suspend fun selectProfile(profileId: Long) {
        // Deselect all profiles first
        deselectAllProfiles()

        // Select the specified profile
        profileDao.selectProfile(profileId)
    }
}
