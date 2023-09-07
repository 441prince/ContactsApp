package com.prince.contactsapp.view

import android.content.Context
import com.prince.contactsapp.models.Contact
import com.prince.contactsapp.models.Profile

interface ItemClickListener {
    fun onContactClick(contact: Contact, context: Context)
    fun onProfileClick(profile: Profile, context: Context)
    fun onProfileLongClick(profile: Profile)
}
