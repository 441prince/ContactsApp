package com.prince.contactsapp.models

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [Profile::class, Contact::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun ContactDao(): ContactDao
    abstract fun ProfileDao(): ProfileDao

    companion object {
        // Singleton prevents multiple instances of database opening at the
        // same time.
        @Volatile
        private var INSTANCE: AppDatabase? = null
        fun getDatabase(context: Context): AppDatabase {
            synchronized(this) {
                var instance = INSTANCE
                if (instance == null) {
                    instance = Room.databaseBuilder(
                        context.applicationContext,
                        AppDatabase::class.java,
                        "Contacts_App_Database"
                    ).build()
                }
                return instance
            }
        }
    }


    /*companion object {
        // Singleton prevents multiple instances of database opening at the
        // same time.
        @Volatile
        private var INSTANCE: ContactDatabase? = null
        fun getDatabase(context: Context,
        ): ContactDatabase {
            if (INSTANCE == null) {
                synchronized(ContactDatabase::class) {
                    INSTANCE = buildRoomDB(context)
                }
            }
            return INSTANCE!!
        }
        private fun buildRoomDB(context: Context) =
            Room.databaseBuilder(
                context.applicationContext,
                ContactDatabase::class.java,
                "Contact Database"
            ).build()

        *//*private class ContactDatabaseCallback(
            private val scope: CoroutineScope
        ) : RoomDatabase.Callback() {
            *//**/
    /**
     * Override the onCreate method to populate the database.
     *//**//*
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                // If you want to keep the data through app restarts,
                // comment out the following line.
                INSTANCE?.let { database ->
                    scope.launch(Dispatchers.IO) {
                        populateDatabase(database.ContactDao())
                    }
                }
            }
        }

        *//**/
    /**
     * Populate the database in a new coroutine.
     * If you want to start with more words, just add them.
     *//**//*
        suspend fun populateDatabase(contactDao: ContactDao) {
            // Start the app with a clean database every time.
            // Not needed if you only populate on creation.
            contactDao.deleteAll()

            var contact = Contact(1,"94435555390", "Dad", R.drawable.contactblack)
            contactDao.insertContact(contact)
            contact = Contact(2,"9486586133", "Mom", R.drawable.contacts)
            contactDao.insertContact(contact)
        }*//*

    }*/
}