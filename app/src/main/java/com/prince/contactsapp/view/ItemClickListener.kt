package com.prince.contactsapp.view

import com.prince.contactsapp.models.Contact
import com.prince.contactsapp.models.Profile

interface ItemClickListener {
    fun onContactClick(contact: Contact)
    fun onProfileClick(profile: Profile)
    fun onProfileLongClick(profile: Profile)
}
