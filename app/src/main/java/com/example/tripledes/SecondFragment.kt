package com.example.tripledes

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.tripledes.databinding.FragmentSecondBinding
import java.security.PrivateKey
import java.security.PublicKey
import java.util.*

/**
 * A simple [Fragment] subclass as the second destination in the navigation.
 */
class SecondFragment : Fragment() {

    private val viewModel: MainViewModel by viewModels()

    private var _binding: FragmentSecondBinding? = null

    private lateinit var publicKey: PublicKey
    private lateinit var privateKey: PrivateKey

    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentSecondBinding.inflate(inflater, container, false)
        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        publicKey = viewModel.loadPublicKeyRSA(requireArguments().getString("public_key")!!)
        privateKey = viewModel.loadPrivateKeyRSA(requireArguments().getString("private_key")!!)

        initListeners()
    }

    private fun initListeners() {
        binding.buttonSecond.setOnClickListener {
            findNavController().navigate(R.id.action_SecondFragment_to_FirstFragment)
        }
        binding.buttonChat.setOnClickListener {
            findNavController().navigate(R.id.action_SecondFragment_to_ChatFragment)
        }
        binding.encryptButton.setOnClickListener {
            val publicKeyString: String = Base64.getEncoder().
            encodeToString(publicKey.encoded)
            binding.encryptedText.text =
                viewModel.encryptMessageRSA(
                    binding.inputText.editText?.text.toString(),
                    publicKeyString
                )
        }
        binding.decryptButton.setOnClickListener {
            val privateKeyString: String = Base64.getEncoder().
            encodeToString(privateKey.encoded)
            binding.encryptedText.text =
                viewModel.decryptMessageRSA(
                    binding.encryptedText.text.toString(),
                    privateKeyString
                )
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
