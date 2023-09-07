package com.prince.contactsapp.view

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.widget.SearchView
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.viewpager.widget.ViewPager
import com.prince.contactsapp.R
import com.prince.contactsapp.ViewPagerAdapter
import com.prince.contactsapp.databinding.FragmentContactBinding
import com.prince.contactsapp.models.AppDatabase
import com.prince.contactsapp.models.Contact
import com.prince.contactsapp.models.ContactDao
import com.prince.contactsapp.models.ContactRepository
import com.prince.contactsapp.models.Profile
import com.prince.contactsapp.models.ProfileRepository
import com.prince.contactsapp.viewmodel.ContactViewModel
import com.prince.contactsapp.viewmodel.ContactViewModelFactory

class ContactFragment : Fragment(), ItemClickListener {

    companion object {
        fun newInstance() = ContactFragment()
    }

    private lateinit var viewModel: ContactViewModel
    private lateinit var binding: FragmentContactBinding
    private lateinit var adapter: ContactAdapter
    private lateinit var viewPager: ViewPager
    private lateinit var viewPagerAdapter: ViewPagerAdapter

    /*private val wordViewModel: ContactViewModel by viewModels {
        ContactViewModelFactory((getActivity().getApplicationContext() as ContactsApplication).repository)
    }*/


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(
            inflater,
            R.layout.fragment_contact, container, false
        )
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        //viewPager = requireActivity().findViewById(R.id.viewPager) // Replace with your ViewPager ID
        viewPagerAdapter = viewPager.adapter as ViewPagerAdapter

        val contactDao = AppDatabase.getDatabase(requireContext()).ContactDao()
        val repository = ContactRepository(contactDao)
        val profileDao = AppDatabase.getDatabase(requireContext()).ProfileDao()
        val profileRepository = ProfileRepository(profileDao)
        val factory = ContactViewModelFactory(repository, profileRepository)
        viewModel = ViewModelProvider(this, factory).get(ContactViewModel::class.java)
        //viewModel.getSelectedProfile()
        // TODO: Use the ViewModel
        binding.myViewModel = viewModel
        binding.lifecycleOwner = this

        // this creates a vertical layout Manager
        binding.recyclerview.layoutManager = LinearLayoutManager(context)

        /*// This will pass the ArrayList to our Adapter
        val adapter = ContactAdapter(data)*/

        initRecyclerView(contactDao)
        // Setting the Adapter with the recyclerview
        binding.recyclerview.adapter = adapter

        viewModel.getNavigateToNewActivity()?.observe(viewLifecycleOwner, Observer {
/*
            // Navigate to the new activity
            // Create an Intent to start the new activity
            val intent = Intent(activity, AddNewContactActivity::class.java)
            // Optionally, add extra data
            intent.putExtra("key", "value")
            // Start the new activity
            startActivity(intent)
*/

        })

        binding.floatingActionButton.setOnClickListener(View.OnClickListener { // Call the ViewModel method to handle the button click
            viewModel.onPlusButtonClick()
        })

        binding.searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                // Handle search query submission if needed
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                // Handle search query text change
                viewModel.setSearchQuery(newText.orEmpty()) // Pass the query to the ViewModel
                return true
            }
        })

        /*viewPager.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrolled(
                position: Int,
                positionOffset: Float,
                positionOffsetPixels: Int
            ) {
                // This method is called when the page is scrolled.
            }

            override fun onPageSelected(position: Int) {

                // This method is called when a new fragment becomes visible.
                val selectedFragment = viewPagerAdapter.getItem(position)

                if (selectedFragment is ContactFragment) {
                    // It's a ContactFragment, so you can call the updateFragment method
                    // This method is called when a new fragment becomes visible.
                    //Toast.makeText(requireContext(), "onPageSelected in contactFragment", Toast.LENGTH_SHORT).show()
                    // Take action when ContactFragment is selected
                    //viewPager.adapter?.notifyDataSetChanged()

                    //selectedFragment.recreateFragment()
                    //refreshFragment()
                }

            }

            override fun onPageScrollStateChanged(state: Int) {
                // This method is called when the state of the scroll changes.
            }
        })*/

        /*// ArrayList of class ItemsViewModel
        val data = ArrayList<Contact>()

        // This loop will create 20 Views containing
        // the image with the count of view
        for (i in 1..20) {
            data.add(Contact("8902975290", "Name $i " ,R.drawable.contactblack))
        }*/
    }

    private fun initRecyclerView(contactDao: ContactDao) {
        binding.recyclerview.layoutManager = LinearLayoutManager(requireContext())

        // ArrayList of class ItemsViewModel
        val data = ArrayList<Contact>()

        // This loop will create 20 Views containing
        // the image with the count of view
        for (i in 1..3) {
            //data.add(Contact(3 + i,"8902975290 $i", "Name $i " ,R.drawable.contactblack))
            //viewModel.insertContact(Contact("8902975290", "Name $i " ,R.drawable.contactblack))
        }

        adapter = ContactAdapter(requireContext(), viewPager, data, this, contactDao, viewModel)
        binding.recyclerview.adapter = adapter
        displayContactList()

    }

    private fun displayContactList() {
        /*viewModel.getAllContact().observe(viewLifecycleOwner, Observer {
            adapter.setList(it)
            adapter.notifyDataSetChanged()
        })*/
        /*viewModel.getAllContactOfProfile().observe(viewLifecycleOwner, Observer {
            adapter.setList(it)
            Toast.makeText(requireContext(), "getAllContactOfProfile()", Toast.LENGTH_SHORT).show()
            adapter.notifyDataSetChanged()
        })*/
        viewModel.profileContacts.observe(viewLifecycleOwner, Observer {
            adapter.setList(it)
            adapter.notifyDataSetChanged()
        })

        // Observe searchResults LiveData
        viewModel.searchResults.observe(viewLifecycleOwner, Observer { searchResults ->
            adapter.setList(searchResults)
            adapter.notifyDataSetChanged()
        })

    }

    override fun onContactClick(contact: Contact) {

        /*// Create an Intent to open the EditContactActivity
        val intent = Intent(requireContext(), ViewOrEditContactActivity::class.java)

        // Pass the contact data to the EditContactActivity
        intent.putExtra("contact_phone", contact.phoneNumber)

        // Start the EditContactActivity
        startActivity(intent)*/
    }

    override fun onProfileClick(profile: Profile, context: Context) {
    }

    override fun onProfileLongClick(profile: Profile) {
    }

    override fun onResume() {
        super.onResume()
        //Toast.makeText(requireContext(), "Im contactFragment", Toast.LENGTH_SHORT).show()
    }

    /*override fun setUserVisibleHint(isVisibleToUser: Boolean) {
        super.setUserVisibleHint(isVisibleToUser)
        if (isVisibleToUser) {
            // Fragment is becoming visible, perform update actions here
            Toast.makeText(requireContext(), "setUserVisibleHint in contactFragment", Toast.LENGTH_SHORT).show()
            recreateFragment()
        }
    }*/

    /*private fun recreateFragment() {
        val fragmentManager = parentFragmentManager // If nested, use parentFragmentManager
        val fragmentTransaction = fragmentManager.beginTransaction()
        fragmentTransaction.detach(this)
        fragmentTransaction.attach(this)
        fragmentTransaction.commit()
    }*/

    /*private fun refreshFragment() {
        // Replace the current fragment with a new instance
        val transaction = requireFragmentManager().beginTransaction()
        transaction.detach(this)
        transaction.attach(this)
        transaction.commit()
    }*/

}