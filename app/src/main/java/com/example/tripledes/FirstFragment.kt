package com.example.tripledes

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.example.tripledes.data.Key
import com.example.tripledes.data.KeyRoomDB
import com.example.tripledes.data.User
import com.example.tripledes.databinding.FragmentFirstBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONException
import java.util.*
import kotlin.collections.ArrayList


/**
 * A simple [Fragment] subclass as the default destination in the navigation.
 */
class FirstFragment : Fragment() {

    private val viewModel: MainViewModel by viewModels()

    private var _binding: FragmentFirstBinding? = null

    private val binding get() = _binding!!

    private val scope = CoroutineScope(SupervisorJob())

    private var serverIp: String? = ""

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val sharedPreferences = activity?.getPreferences(Context.MODE_PRIVATE)
        serverIp = sharedPreferences?.getString(getString(R.string.IP_KEY), "")
        _binding = FragmentFirstBinding.inflate(inflater, container, false)
        initListeners()
        initializeDatabase()
        getAvailableUsers()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.serverIpContent.text = serverIp
    }

    private fun initializeDatabase() {
        scope.launch {
            val db = context?.let { KeyRoomDB.getDatabase(it) }!!
            val keySize = db.keyDao().all.size

            if(keySize == 0) {
                Log.d(this::class.java.simpleName, "generating RSA pair keys")
                viewModel.generateKeyRSA()

                try {
                    val keys: ArrayList<Key> = ArrayList()
                    val publicKey = Base64.getEncoder().encodeToString(viewModel.publicKey.value?.encoded) ?: ""
                    val privateKey = Base64.getEncoder().encodeToString(viewModel.privateKey.value?.encoded) ?: ""

                    if (publicKey != "" && privateKey != ""){

                        keys.add(Key(PUBLIC_KEY, publicKey))
                        keys.add(Key(PRIVATE_KEY, privateKey))

                        keys.forEach { key ->
                            db.keyDao().insert(key)
                        }
                    }
                } catch (e: Exception) {
                    Log.e(this::class.java.simpleName, "${e.message}")
                }

            } else {
                Log.d(this::class.java.simpleName, "keys already generated")
                val keys = db.keyDao().all
                keys.forEach { key ->
                    when (key.name) {
                        PUBLIC_KEY -> {
                            viewModel._publicKey.postValue(viewModel.loadPublicKeyRSA(key.value))
                        }
                        PRIVATE_KEY -> {
                            viewModel._privateKey.postValue(viewModel.loadPrivateKeyRSA(key.value))
                        }
                    }
                }
            }
        }
    }

    private fun getAvailableUsers(): Boolean {

        // url to post our data
        val url = "http://$serverIp/available_users.php"

        if(serverIp == "") {
            return false
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
                    binding.availableUsersContent.text = users

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
        return true
    }



    private fun initListeners() {

        binding.buttonFirst.setOnClickListener {
            val privateKey: String = Base64.getEncoder().
            encodeToString(viewModel.privateKey.value!!.encoded)
            val publicKey: String = Base64.getEncoder().
            encodeToString(viewModel.publicKey.value!!.encoded)

            findNavController().navigate(
                R.id.action_FirstFragment_to_SecondFragment, Bundle().apply {
                    putString("public_key", publicKey)
                    putString("private_key", privateKey)
                }
            )
        }
        viewModel.privateKey.observe(this.viewLifecycleOwner) {privateKey ->
            binding.privateKeyContent.text = Base64.getEncoder().encodeToString(privateKey.encoded)
        }
        viewModel.publicKey.observe(this.viewLifecycleOwner) {publicKey ->
            binding.publicKeyContent.text = Base64.getEncoder().encodeToString(publicKey.encoded)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        const val PRIVATE_KEY = "private_key"
        const val PUBLIC_KEY = "public_key"
    }
}