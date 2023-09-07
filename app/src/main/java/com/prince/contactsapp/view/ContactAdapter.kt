package com.prince.contactsapp.view

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager.widget.ViewPager
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.prince.contactsapp.R
import com.prince.contactsapp.models.Contact
import com.prince.contactsapp.models.ContactDao
import com.prince.contactsapp.viewmodel.ContactViewModel

class ContactAdapter(
    private val context: Context,
    private val viewPager: ViewPager,
    private val contactsList: ArrayList<Contact>,
    private val clickListener: ItemClickListener,
    private val contactDao: ContactDao,
    private val viewModel: ContactViewModel // Add ViewModel parameter
) : RecyclerView.Adapter<ContactAdapter.ViewHolder>() {
    // create new views
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        // inflates the card_view_design view
        // that is used to hold list item
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.contact_list_item, parent, false)

        return ViewHolder(view)
    }

    // binds the list items to a view
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        val ItemsViewModel = contactsList[position]

        // sets the image to the imageview from our itemHolder class
        // Load the image using Glide
        Glide.with(holder.itemView.context)
            .load(ItemsViewModel.imageUri) // Assuming contact.imageUri is a String
            .apply(
                RequestOptions()
                    .placeholder(R.drawable.contactblack) // Set your default image resource here
                    .error(R.drawable.contactblack) // Set your default image resource here as well
                    .centerCrop() // Center-crop the image within the circular frame
            )
            .into(holder.contactImageView)

        // sets the text to the textview from our itemHolder class
        holder.contactNameTextView.text = ItemsViewModel.name

        // sets the text to the textview from our itemHolder class
        holder.contactNumberTextView.text = ItemsViewModel.phoneNumber.toString()

        // Set the favorite image based on the isFavorite property
        val favoriteImageResource = if (ItemsViewModel.isFavorite) {
            R.drawable.filledheart // Use your favorite image resource
        } else {
            R.drawable.emptyheart // Use your non-favorite image resource
        }
        holder.contactFavoriteImageView.setImageResource(favoriteImageResource)

    }

    // return the number of the items in the list
    override fun getItemCount(): Int {
        return contactsList.size
    }

    fun setList(contacts: List<Contact>) {
        contactsList.clear()
        contactsList.addAll(contacts)
        notifyDataSetChanged() // Notify the adapter that the data has changed
    }

    // Holds the views for adding it to image and text
    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val contactImageView: ImageView = itemView.findViewById(R.id.contact_imageview)
        val contactNameTextView: TextView = itemView.findViewById(R.id.contact_name_textView)
        val contactNumberTextView: TextView = itemView.findViewById(R.id.contact_number_textView)
        val contactFavoriteImageView: ImageButton =
            itemView.findViewById(R.id.contact_favorite_imageview)

        init {
            itemView.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    val contact = contactsList[position]
                    clickListener.onContactClick(contact)
                }
            }
            contactFavoriteImageView.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    val contact = contactsList[position]
                    contact.isFavorite = !contact.isFavorite // Toggle the favorite state

                    /*// Use a coroutine scope to call the suspend function
                    CoroutineScope(Dispatchers.IO).launch {
                        // Update the contact in the Room database
                        val repository = ContactRepository(contactDao) // Use your ContactRepository
                        repository.update(contact)

                        // Notify the ViewModel which will, in turn, notify the LiveData
                        viewModel.notifyRepositoryEvent(
                            ContactRepository.RepositoryEvent(
                                ContactRepository.RepositoryEvent.Action.NOTIFY_DATA_SET_CHANGED
                            )
                        )
                    }

                    notifyDataSetChanged() // Refresh the list to update the ImageButton*/

                    // Use ViewModel to update the contact and notify LiveData
                    viewModel.updateContactAndNotify(contact)

                    //Toast.makeText(context, "ContactAdapter position $position", Toast.LENGTH_SHORT).show();

                    viewPager.adapter?.notifyDataSetChanged()

                    // No need to call notifyDataSetChanged() here
                }
            }
        }
    }
}