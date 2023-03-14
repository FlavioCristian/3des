package com.example.tripledes

import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.example.tripledes.data.KeyRoomDB
import com.example.tripledes.data.User
import com.example.tripledes.databinding.FragmentChatBinding
import com.example.tripledes.databinding.FragmentSecondBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONException
import java.security.PrivateKey
import java.security.PublicKey
import java.util.*
import kotlin.collections.ArrayList

/**
 * A simple [Fragment] subclass as the second destination in the navigation.
 */
class ChatFragment : Fragment() {

    private val viewModel: MainViewModel by viewModels()

    private var _binding: FragmentChatBinding? = null

    private val binding get() = _binding!!

    private lateinit var userAdapter: UserAdapter

    private val scope = CoroutineScope(SupervisorJob())

    private var serverIp: String? = ""

    private var arrayListUsers = ArrayList<User>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val sharedPreferences = activity?.getPreferences(Context.MODE_PRIVATE)
        serverIp = sharedPreferences?.getString(getString(R.string.IP_KEY), "")
        _binding = FragmentChatBinding.inflate(inflater, container, false)
        return binding.root
    }

    private fun prepareItems() {
        userAdapter.notifyDataSetChanged()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)



        val recyclerView: RecyclerView = requireView().findViewById(R.id.recyclerView)
        val list = KeyRoomDB.getUsers(requireContext())
        arrayListUsers = list
        userAdapter = UserAdapter(list, requireContext()){selectedUser ->
            Log.d(this::class.java.simpleName, "name: ${selectedUser.name}")

            Log.d(this::class.java.simpleName, "size: ${list.size}")
            list.forEach { availableUser ->
                if (selectedUser.name == availableUser.name) {
                    findNavController().navigate(R.id.action_ChatFragment_to_ChatUserFragment, Bundle().apply {
                        putString("username", selectedUser.name)
                        putString("public_key", selectedUser.publicKey)
                    })
                }
            }
        }
        val layoutManager = LinearLayoutManager(context)
        recyclerView.layoutManager = layoutManager
        recyclerView.adapter = userAdapter
        prepareItems()
        initListeners()
    }

    private fun getAvailableUsers(): ArrayList<User> {

        // url to post our data
        val url = "http://$serverIp/available_users.php"

        if(serverIp == "") {
            return ArrayList()
        }

        // creating a new variable for our request queue
        val queue = Volley.newRequestQueue(context)

        val availableUsers = ArrayList<User>()

        // on below line we are calling a string
        // request method to post the data to our API
        // in this we are calling a post method.
        val request: StringRequest = object : StringRequest(
            Method.GET, url,
            Response.Listener { response ->
                try {
                    // on below line passing our response to json object.
                    val jsonArray = JSONArray(response)

                    for (i in 0 until jsonArray.length()) {
                        val name = jsonArray.getJSONObject(i).getString("name")
                        val publicKey = jsonArray.getJSONObject(i).getString("public_key")
                        availableUsers.add(User(name, publicKey))
                    }
                    var users = ""
                    availableUsers.forEach { user ->
                        users += user.name + " "
                    }
                    scope.launch {
                        val db = KeyRoomDB.getDatabase(requireContext())
                        availableUsers.forEach{ user ->
                            db.userDao().insert(User(user.name, user.publicKey))
                        }
                    }

                } catch (e: JSONException) {
                    Log.e(this::class.java.simpleName, "error: ${e.message}")
                }
            },
            Response.ErrorListener { error -> // method to handle errors.
                Toast.makeText(context, "Fail to get course$error", Toast.LENGTH_LONG)
                    .show()
                Log.e(this::class.java.simpleName, "error: ${error.message}")
            }) {
            override fun getBodyContentType(): String {
                // as we are passing data in the form of url encoded
                // so we are passing the content type below
                return "application/x-www-form-urlencoded; charset=UTF-8"
            }
        }
        // below line is to make
        // a json object request.

        queue.add(request)
        return availableUsers
    }

    private fun initListeners() {

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
