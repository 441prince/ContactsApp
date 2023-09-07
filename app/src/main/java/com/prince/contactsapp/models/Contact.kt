package com.prince.contactsapp.models

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
    (tableName = "contact_table")
data class Contact(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "contactId")
    val id: Long, // This field will be auto-generated as a primary key

    @ColumnInfo(name = "contactNumber") var phoneNumber: String,
    @ColumnInfo(name = "contactName") var name: String,
    @ColumnInfo(name = "contactEmailId") var emailId: String,
    @ColumnInfo var imageUri: String,
    @ColumnInfo(name = "isFavorite") var isFavorite: Boolean,
    @ColumnInfo(name = "profileId") val profileId: Long
) {
}


//(tableName = "contact_table", indices = [Index(value = ["contactNumber"], unique = true)])