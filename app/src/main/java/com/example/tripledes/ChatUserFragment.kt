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
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.example.tripledes.data.EncryptedKey
import com.example.tripledes.data.Key
import com.example.tripledes.data.KeyRoomDB
import com.example.tripledes.data.User
import com.example.tripledes.databinding.FragmentChatUserBinding
import com.google.firebase.crashlytics.buildtools.reloc.org.apache.http.client.HttpClient
import com.google.firebase.crashlytics.buildtools.reloc.org.apache.http.client.methods.HttpGet
import com.google.firebase.crashlytics.buildtools.reloc.org.apache.http.impl.client.DefaultHttpClient
import kotlinx.coroutines.*
import org.json.JSONArray
import org.json.JSONException
import java.io.IOException
import java.lang.Thread.sleep
import java.util.*
import kotlin.collections.ArrayList

/**
 * A simple [Fragment] subclass as the second destination in the navigation.
 */
class ChatUserFragment : Fragment() {

    private val viewModel: MainViewModel by viewModels()

    private var _binding: FragmentChatUserBinding? = null

    private val binding get() = _binding!!

    private val scope = CoroutineScope(SupervisorJob())

    private var serverIp: String? = ""

    private var myUserName: String = ""

    private var shouldReceiveMessage = false

    private var availableKeys: ArrayList<EncryptedKey> = ArrayList()

    private var availableMessages: ArrayList<String> = ArrayList()

    private var decryptedDESKey = ""

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val sharedPreferences = activity?.getPreferences(Context.MODE_PRIVATE)
        serverIp = sharedPreferences?.getString(getString(R.string.IP_KEY), "")
        myUserName = sharedPreferences?.getString(getString(R.string.USER_NAME), "") ?: ""
        _binding = FragmentChatUserBinding.inflate(inflater, container, false)
        runBlocking {
            getAvailableKeys()
        }
        runBlocking {
            queryForAvailableMessages()
        }

        return binding.root
    }

    private fun sendKeyToUser() {

        val destination = requireArguments().getString("username")!!
        val publicKeyString = requireArguments().getString("public_key")!!
        val publicKey = viewModel.loadPublicKeyRSA(publicKeyString)

        val secretKey = viewModel.generateKeyDES()

        decryptedDESKey = viewModel.fromSecretKeyToString(secretKey)

        val encryptedKeyDES = viewModel.encryptMessageRSA(decryptedDESKey, Base64.getEncoder().encodeToString(publicKey.encoded))

        sendKeyDES(destination, encryptedKeyDES)
    }

    private fun queryForAvailableMessages() {
        // url to post our data
        val url = "http://$serverIp/available_messages.php?destination=$myUserName"

        // creating a new variable for our request queue
        val queue = Volley.newRequestQueue(context)

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
                        val encryptedMessage = jsonArray.getJSONObject(i).getString("encrypted_message")
                        availableMessages.add(encryptedMessage)
                    }

                } catch (e: JSONException) {
                    Log.e(this::class.java.simpleName, "error response listener: ${e.message}")
                }
            },
            Response.ErrorListener { error -> // method to handle errors.
                Toast.makeText(context, "Fail to get course$error", Toast.LENGTH_LONG)
                    .show()
                Log.e(this::class.java.simpleName, "error errorlistener: ${error.message}")
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
    }

    private fun sendKeyDES(destination: String, encryptedKey: String) {
        val url = "http://$serverIp/insert_key.php?name=$destination&encrypted_key=$encryptedKey"
        val client: HttpClient = DefaultHttpClient()
        try {
            scope.launch {
                val result = client.execute(HttpGet(url))
                Log.d(this::class.java.simpleName, "result: $result")
            }

        } catch (e: IOException) {
            Log.e(this::class.java.simpleName, "error: ${e.message}")
        }
    }

    private suspend fun getAvailableKeys() {

        // url to post our data
        val url = "http://$serverIp/available_keys.php"

        if(serverIp == "") {
            return
        }

        // creating a new variable for our request queue
        val queue = Volley.newRequestQueue(context)

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
                        val destination = jsonArray.getJSONObject(i).getString("destination")
                        val encryptedDesKey = jsonArray.getJSONObject(i).getString("encrypted_key")
                        this.availableKeys.add(
                            EncryptedKey(destination, encryptedDesKey)
                        )
                        availableKeys.add(EncryptedKey(destination, encryptedDesKey))
                    }
                    val db = KeyRoomDB.getDatabase(requireContext())
                    scope.launch {
                        availableKeys.forEach{ key ->
                            db.encryptedKeyDao().insert(EncryptedKey(key.destination, key.value))
                        }
                    }

                    var encryptedKey = EncryptedKey("","")

                    availableKeys.forEach { key ->
                        if (key.destination == myUserName) {
                            shouldReceiveMessage = true
                            encryptedKey = EncryptedKey(key.destination, key.value)
                        }
                    }

                    if (shouldReceiveMessage) {
                        Log.d("available keys", "encryptedKey.value: ${encryptedKey.value}")
                        Log.d("available keys", "private key: ${KeyRoomDB.getPrivateKey(requireContext())}")
                        decryptedDESKey = viewModel.decryptMessageRSA(encryptedKey.value, KeyRoomDB.getPrivateKey(requireContext()))
                        Log.d("available keys", "decryptedDESKey: $decryptedDESKey")
                        runBlocking {
                            KeyRoomDB.getDatabase(requireContext()).keyDao().insert((Key("des_key", decryptedDESKey)))
                        }
                    }

                } catch (e: JSONException) {
                    Log.e(this::class.java.simpleName, "error available keys: ${e.message}")
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

        queue.add(request)
    }

    private fun sendEncryptedMessage(destination: String, encryptedMessage: String) {
        val url = "http://$serverIp/insert_message.php?destination=$destination&encrypted_message=$encryptedMessage"
        val client: HttpClient = DefaultHttpClient()
        try {
            scope.launch {
                val result = client.execute(HttpGet(url))
                Log.d(this::class.java.simpleName, "result: $result")
            }

        } catch (e: IOException) {
            Log.e(this::class.java.simpleName, "error: ${e.message}")
        }
    }

    fun receiveKeyFromUser(userName: String) {
        val url = "http://$serverIp/available_keys.php"
        val client: HttpClient = DefaultHttpClient()
        try {
            scope.launch {
                val result = client.execute(HttpGet(url))
                Log.d(this::class.java.simpleName, "result: $result")


            }

        } catch (e: IOException) {
            Log.e(this::class.java.simpleName, "error: ${e.message}")
        }
    }

    private fun initListeners() {
        binding.buttonSendMessage.setOnClickListener {
            Log.d(this.javaClass.simpleName, "sending button pressed")
            var encryptedKey = EncryptedKey("","")
            availableKeys.forEach { key ->
                if (key.destination == myUserName) {
                    encryptedKey = EncryptedKey(key.destination, key.value)
                }
            }

            val destination = requireArguments().getString("username")!!
            Log.d(javaClass.simpleName, "destination: $destination")
            Log.d(javaClass.simpleName, "decryptedDESKey: $decryptedDESKey")
            val encryptedMessageByteArray = viewModel.encryptDES(binding.inputText.editText!!.text.toString(), viewModel.fromStringToSecretKey(decryptedDESKey))
            val encryptedMessage = Base64.getEncoder().encodeToString(encryptedMessageByteArray)
            sendEncryptedMessage(destination, encryptedMessage)
        }

        binding.buttonRefresh.setOnClickListener {
            var message = ""
            if (availableMessages.size > 0) {
                val secretKey = viewModel.fromStringToSecretKey(decryptedDESKey)
                Log.d("read message","trying to read message, des key: $decryptedDESKey")
                Log.d("read message","encrypted message: ${availableMessages[0]}")
                message = viewModel.decryptDES(availableMessages[0], secretKey)
            }
            binding.messageUserContent.text = message
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initListeners()
    }

    override fun onStart() {
        super.onStart()
        if (availableMessages.size > 0) {
            Log.d(javaClass.simpleName, "shouldReceiveMessage")
        } else {
            Log.d(javaClass.simpleName, "sending key to destination")
            sendKeyToUser()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
