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
import com.example.tripledes.data.Key
import com.example.tripledes.data.KeyRoomDB
import com.example.tripledes.databinding.FragmentFirstBinding
import com.example.tripledes.databinding.FragmentSettingsBinding
import com.google.firebase.crashlytics.buildtools.reloc.org.apache.http.client.HttpClient
import com.google.firebase.crashlytics.buildtools.reloc.org.apache.http.client.methods.HttpGet
import com.google.firebase.crashlytics.buildtools.reloc.org.apache.http.impl.client.DefaultHttpClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL
import java.util.*

/**
 * A simple [Fragment] subclass as the default destination in the navigation.
 */
class SettingsFragment : Fragment() {

    private val viewModel: MainViewModel by viewModels()

    private var _binding: FragmentSettingsBinding? = null

    private val binding get() = _binding!!

    private val scope = CoroutineScope(SupervisorJob())

    private var serverIp: String? = ""

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val sharedPreferences = activity?.getPreferences(Context.MODE_PRIVATE)
        serverIp = sharedPreferences?.getString(getString(R.string.IP_KEY), "")
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initListeners()
    }

    private fun initListeners() {

        binding.buttonSave.setOnClickListener {
            val sharedPreferences = activity?.getPreferences(Context.MODE_PRIVATE)
            with (sharedPreferences?.edit()) {
                this?.putString(
                    getString(R.string.IP_KEY),
                    binding.inputText.editText!!.text.toString()
                )
                this?.apply()
            }.also {
                serverIp = binding.inputText.editText!!.text.toString()
            }
            val preferenceValue = sharedPreferences?.getString(getString(R.string.IP_KEY), "")
            if (preferenceValue != "") {
                Toast.makeText(context,"IP: $preferenceValue saved successfully!",Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(context,"Error while trying to save IP.",Toast.LENGTH_SHORT).show()
            }
        }
        binding.buttonShareKey.setOnClickListener {
            val name = binding.inputName.editText!!.text.toString()
            val sharedPreferences = activity?.getPreferences(Context.MODE_PRIVATE)
            with (sharedPreferences?.edit()) {
                this?.putString(
                    getString(R.string.USER_NAME),
                    name
                )
                this?.apply()
            }
            scope.launch {
                val publicKey = KeyRoomDB.getPublicKey(requireContext())
                val url = "http://$serverIp/insert_user.php?name=$name&public_key=$publicKey"
                val client: HttpClient = DefaultHttpClient()
                try {
                    val result = client.execute(HttpGet(url))
                    Log.d(this::class.java.simpleName, "Public Key: $result")

                } catch (e: IOException) {
                    Log.e(this::class.java.simpleName, "error: ${e.message}")
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}