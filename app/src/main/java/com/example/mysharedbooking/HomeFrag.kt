package com.example.mysharedbooking

import android.content.Context
import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer

import androidx.lifecycle.ViewModelProviders
import androidx.lifecycle.viewModelScope
import androidx.navigation.Navigation.findNavController
import androidx.navigation.fragment.findNavController
import com.example.mysharedbooking.databinding.FragmentHomeBinding
import com.example.mysharedbooking.models.MySharedBookingDB
import com.example.mysharedbooking.models.User
import com.example.mysharedbooking.viewmodels.MainViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"


/**
 * A simple [Fragment] subclass.
 * Activities that contain this fragment must implement the
 * [HomeFrag.OnFragmentInteractionListener] interface
 * to handle interaction events.
 * Use the [HomeFrag.newInstance] factory method to
 * create an instance of this fragment.
 */
class HomeFrag : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null
    private var listener: OnFragmentInteractionListener? = null
    lateinit var goToBookingForm: Observer<Boolean?>
    lateinit var myDatabase: MySharedBookingDB
    private lateinit var mainViewModel:MainViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val fragmentHomeBinding: FragmentHomeBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_home, container, false)
        mainViewModel = ViewModelProviders.of(this).get(MainViewModel::class.java)
        fragmentHomeBinding.viewmodel = mainViewModel

        suspend fun newUser(newName: String) = withContext(Dispatchers.IO){
            if(!newName.isBlank()) {
                myDatabase.myDao().insertUsers(User(0, "gianni", "fantoni", newName, "User"))
            }
        }

        // Create the observer which updates the UI.
        val insertNewUser = Observer<String> { newName ->
            print("PORCODIOIOIIOI")
            mainViewModel.viewModelScope.launch { newUser(newName) }
        }

        goToBookingForm = Observer { bool ->
            if(bool == true) {
                val action = HomeFragDirections.actionHomeFragToNewBookingForm()
                findNavController(activity as MainActivity, R.id.nav_host_fragment).navigate(action)
                mainViewModel.addNewBook.value = false
            }
        }

        val showAllObserver = Observer<Boolean?> { bool ->
            mainViewModel.viewModelScope.launch { printAllUsers() }
        }

        // Observe the LiveData, passing in this activity as the LifecycleOwner and the observer.
        mainViewModel.insertUser.observe(this, insertNewUser)
        mainViewModel.showAllUsers.observe(this, showAllObserver)
        mainViewModel.addNewBook.observe(this, goToBookingForm)
        return fragmentHomeBinding.root
        // Inflate the layout for this fragment
        //return inflater.inflate(R.layout.fragment_home, container, false)
    }

    private suspend fun printAllUsers() = withContext(Dispatchers.IO){
        //navController.navigate(R.id.action_mainFragment_to_usersFragment)
        val userList: List<User> = myDatabase.myDao().getAllUsers()

        for (user:User in userList){
            println(user.username+"")
        }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

    }

    // TODO: Rename method, update argument and hook method into UI event
    fun onButtonPressed(uri: Uri) {
        listener?.onFragmentInteraction(uri)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is OnFragmentInteractionListener) {
            listener = context
            myDatabase = MainActivity.getInMemoryDatabase(activity!!.applicationContext)

        } else {
           throw RuntimeException(context.toString() + " must implement OnFragmentInteractionListener")
        }
    }

    override fun onDetach() {
        super.onDetach()
        listener = null
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     *
     *
     * See the Android Training lesson [Communicating with Other Fragments]
     * (http://developer.android.com/training/basics/fragments/communicating.html)
     * for more information.
     */
    interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        fun onFragmentInteraction(uri: Uri)
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment HomeFrag.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            HomeFrag().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}
