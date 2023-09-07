package com.prince.contactsapp.view

import android.content.Intent
import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.viewpager.widget.ViewPager
import com.prince.contactsapp.R
import com.prince.contactsapp.ViewPagerAdapter
import com.prince.contactsapp.databinding.FragmentProfileBinding
import com.prince.contactsapp.models.AppDatabase
import com.prince.contactsapp.models.Contact
import com.prince.contactsapp.models.ContactRepository
import com.prince.contactsapp.models.Profile
import com.prince.contactsapp.models.ProfileDao
import com.prince.contactsapp.models.ProfileRepository
import com.prince.contactsapp.viewmodel.ProfileViewModel
import com.prince.contactsapp.viewmodel.ProfileViewModelFactory
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ProfileFragment : Fragment(), ItemClickListener {

    companion object {
        fun newInstance() = ProfileFragment()
    }

    //@Inject
    //lateinit var viewModel: ProfileViewModel
    //private val viewModel: ProfileViewModel by viewModels() //worked

    private lateinit var viewModel: ProfileViewModel
    private lateinit var binding: FragmentProfileBinding
    private lateinit var adapter: ProfileAdapter
    private lateinit var viewPager: ViewPager
    private lateinit var viewPagerAdapter: ViewPagerAdapter
    private var profileCount = 0


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(
            inflater,
            R.layout.fragment_profile, container, false
        )
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        //viewPager = requireActivity().findViewById(R.id.viewPager) // Replace with your ViewPager ID
        viewPagerAdapter = viewPager.adapter as ViewPagerAdapter

        val profileDao = AppDatabase.getDatabase(requireContext()).ProfileDao()
        val profileRepository = ProfileRepository(profileDao)
        val contactDao = AppDatabase.getDatabase(requireContext()).ContactDao()
        val contactRepository = ContactRepository(contactDao)
        val factory = ProfileViewModelFactory(profileRepository, contactRepository)
        viewModel = ViewModelProvider(this, factory).get(ProfileViewModel::class.java)

        binding.myViewModel = viewModel
        binding.lifecycleOwner = this

        // this creates a vertical layout Manager
        binding.profileRecyclerView.layoutManager = LinearLayoutManager(context)
        initRecyclerView(profileDao)

        // Define your default profile
        val defaultProfile = Profile(id = 1, name = "Default Profile", isDefault = true, imageUri = null)
        viewModel.addDefaultProfile(defaultProfile)

        displayContactList()

        viewModel.getNavigateToNewActivity()?.observe(viewLifecycleOwner, Observer {
/*            // Navigate to the new activity
            // Create an Intent to start the new activity
            val intent = Intent(activity, AddViewEditProfileActivity::class.java)
            // Start the new activity
            startActivity(intent)*/
        })

        viewModel.selectedProfile.observe(viewLifecycleOwner, Observer { selectedProfile ->
            // Handle the selected profile change here
            if (selectedProfile != null) {
                // A profile is selected, you can update your UI or perform actions accordingly
                Toast.makeText(requireContext(), "Selected profile: ${selectedProfile.name}", Toast.LENGTH_SHORT).show()
            } else {
                // No profile is selected, handle this case as needed
                Toast.makeText(requireContext(), "No profile is selected", Toast.LENGTH_SHORT).show()
            }
        })

        binding.floatingActionButton.setOnClickListener(View.OnClickListener { // Call the ViewModel method to handle the button click
            if (profileCount < 3) {
                // Call the ViewModel method to handle the button click
                viewModel.onPlusButtonClick()
            } else {
                // Show a toast message when there are already three profiles
                Toast.makeText(requireContext(), "Maximum of three profiles allowed.", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun initRecyclerView(profileDao: ProfileDao) {
        binding.profileRecyclerView.layoutManager = LinearLayoutManager(context)
        adapter = ProfileAdapter(requireContext(), viewPager, ArrayList(), this, profileDao, viewModel)
        binding.profileRecyclerView.adapter = adapter
    }

    private fun displayContactList() {
        viewModel.getAllProfiles().observe(viewLifecycleOwner, Observer { profiles ->
            // Update the profile count when the profile list changes
            profileCount = profiles.size
            // Disable the "Create Profile" button if there are already three profiles
            updateCreateProfileButtonState()
            adapter.setList(profiles)
            adapter.notifyDataSetChanged()
        })
    }

    private fun updateCreateProfileButtonState() {
        if (profileCount >= 3) {
            // Disable the button if there are already three profiles
            //binding.floatingActionButton.isEnabled = false
        } else {
            // Enable the button if there are fewer than three profiles
            binding.floatingActionButton.isEnabled = true

        }
    }


    override fun onContactClick(contact: Contact) {

    }

    override fun onProfileClick(profile: Profile) {

        /*// Create an Intent to open the EditContactActivity
        val intent = Intent(requireContext(), AddViewEditProfileActivity::class.java)
        // Pass the contact data to the EditContactActivity

        intent.putExtra("profile_id", profile.id.toString())
        //Toast.makeText(requireContext(), "${profile.id}", Toast.LENGTH_SHORT).show()
        // Start the EditContactActivity
        startActivity(intent)*/
    }

    override fun onProfileLongClick(profile: Profile) {
        // Implement actions for long-pressing a profile here
        // For example, show a toast message or perform some other action

        // Debug: Check if the ViewModel is initialized
        if (viewModel == null) {
            Toast.makeText(requireContext(), "ViewModel is null", Toast.LENGTH_SHORT).show()
            return
        }

        // Debug: Check if the profile is not null
        if (profile == null) {
            Toast.makeText(requireContext(), "Selected profile is null", Toast.LENGTH_SHORT).show()
            return
        }
        // Select the clicked profile
        viewModel.selectProfile(profile.id, viewPager)
        adapter.notifyDataSetChanged()

        // Implement other actions as needed

        //Toast.makeText(requireContext(), "Long-pressed profile: ${profile.name}", Toast.LENGTH_SHORT).show()
        //viewPager.adapter?.notifyDataSetChanged()

    }

    override fun onResume() {
        super.onResume()
        //Toast.makeText(requireContext(), "Im ProfileFragment", Toast.LENGTH_SHORT).show()
    }
}