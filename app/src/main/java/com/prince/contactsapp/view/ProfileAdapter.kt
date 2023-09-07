package com.prince.contactsapp.view

import android.content.Context
import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager.widget.ViewPager
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.prince.contactsapp.R
import com.prince.contactsapp.models.Profile
import com.prince.contactsapp.models.ProfileDao
import com.prince.contactsapp.viewmodel.ProfileViewModel

class ProfileAdapter(
    val context: Context,
    private val viewPager: ViewPager,
    private val profileList: ArrayList<Profile>,
    private val clickListener: ItemClickListener,
    private val profileDao: ProfileDao,
    private val viewModel: ProfileViewModel
) : RecyclerView.Adapter<ProfileAdapter.ViewHolder>() {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.profile_list_item, parent, false)
        return ViewHolder(view)
    }

    @RequiresApi(Build.VERSION_CODES.P)
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val profile = profileList[position]
        // sets the image to the imageview from our itemHolder class
        // Load the image using Glide
       /* Glide.with(holder.itemView.context)
            .load(profile.imageUri)
            .centerCrop()
            .into(holder.profileImageView)*/

        Glide.with(holder.itemView.context)
            .load(profile.imageUri)
            .apply(
                RequestOptions()
                    .placeholder(R.drawable.contactblack) // Set your default image resource here
                    .error(R.drawable.contactblack) // Set your default image resource here as well
                    .centerCrop()
            )
            .into(holder.profileImageView)

        // sets the text to the textview from our itemHolder class
        holder.profileNameTextView.text = profile.name

        // Highlight the selected profile
        if (profile.isSelected) {
            //holder.itemView.setBackgroundResource(R.color.green) // Add a background resource for highlighting
            //holder.profileImageView.outlineAmbientShadowColor = Color.parseColor("#81D4FA")
            holder.itemView.setBackgroundResource(R.color.highlight_White) // Add a background resource for highlighting
        } else {
            holder.itemView.setBackgroundResource(0) // Clear background
        }
    }

    override fun getItemCount(): Int {
        return profileList.size
    }

    fun setList(profiles: List<Profile>) {
        profileList.clear()
        profileList.addAll(profiles)
        notifyDataSetChanged() // Notify the adapter that the data has changed
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val profileImageView: ImageView = itemView.findViewById(R.id.profile_imageview)
        val profileNameTextView: TextView = itemView.findViewById(R.id.profile_name_textView)
        // Add other views here as needed

        init {
            itemView.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    val profile = profileList[position]
                    clickListener.onProfileClick(profile, context)
                }
            }

            itemView.setOnLongClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    val profile = profileList[position]
                    clickListener.onProfileLongClick(profile)
                    notifyDataSetChanged()
                    //viewPager.adapter?.notifyDataSetChanged()
                }
                true
            }
        }
    }
}