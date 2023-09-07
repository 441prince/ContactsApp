package com.prince.contactsapp.view

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager.widget.ViewPager
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.prince.contactsapp.R
import com.prince.contactsapp.models.Contact
import com.prince.contactsapp.models.ContactDao
import com.prince.contactsapp.viewmodel.FavoriteViewModel

class FavoriteAdapter(
    private val context: Context,
    private val viewPager: ViewPager,
    private val favoriteContactList: ArrayList<Contact>,
    private val clickListener: ItemClickListener,
    private val contactDao: ContactDao,
    private val viewModel: FavoriteViewModel // Add ViewModel parameter
) :
    RecyclerView.Adapter<FavoriteAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.favorite_contact_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val contact = favoriteContactList[position]
        // sets the image to the imageview from our itemHolder class
        // Load the image using Glide
        Glide.with(holder.itemView.context)
            .load(contact.imageUri) // Assuming contact.imageUri is a String
            .apply(
                RequestOptions()
                    .placeholder(R.drawable.contactblack) // Set your default image resource here
                    .error(R.drawable.contactblack) // Set your default image resource here as well
                    .centerCrop()
            )
            .into(holder.contactImageView)

        // sets the text to the textview from our itemHolder class
        holder.contactNameTextView.text = contact.name

        // sets the text to the textview from our itemHolder class
        holder.contactNumberTextView.text = contact.phoneNumber.toString()

        // Set the favorite image based on the isFavorite property
        val favoriteImageResource = if (contact.isFavorite) {
            R.drawable.filledheart // Use your favorite image resource
        } else {
            R.drawable.emptyheart // Use your non-favorite image resource
        }
        holder.contactFavoriteImageView.setImageResource(favoriteImageResource)
    }

    override fun getItemCount(): Int {
        return favoriteContactList.size
    }

    fun setList(contacts: List<Contact>) {
        favoriteContactList.clear()
        favoriteContactList.addAll(contacts)
        notifyDataSetChanged() // Notify the adapter that the data has changed
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val contactImageView: ImageView = itemView.findViewById(R.id.contact_imageview)
        val contactNameTextView: TextView = itemView.findViewById(R.id.contact_name_textView)
        val contactNumberTextView: TextView = itemView.findViewById(R.id.contact_number_textView)
        val contactFavoriteImageView: ImageButton =
            itemView.findViewById(R.id.contact_favorite_imageview)
        // Add other views here as needed


        init {
            itemView.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    val contact = favoriteContactList[position]
                    clickListener.onContactClick(contact, context)
                }
            }
            contactFavoriteImageView.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    val contact = favoriteContactList[position]
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
                    //Toast.makeText(context, "FavoriteAdapter position $position", Toast.LENGTH_SHORT).show();

                    viewPager.adapter?.notifyDataSetChanged()
                    // No need to call notifyDataSetChanged() here
                }
            }
        }
    }
}
