package `in`.jadu.anju.commonuis

import android.content.res.Configuration
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.AndroidEntryPoint
import `in`.jadu.anju.R
import `in`.jadu.anju.databinding.FragmentSelectLanguageBinding
import `in`.jadu.anju.kvstorage.KvStorage
import java.util.Locale
import javax.inject.Inject

@AndroidEntryPoint
class SelectLanguageFragment () : Fragment() {
    private lateinit var binding:FragmentSelectLanguageBinding
    @Inject lateinit var kvStorage:KvStorage
    private lateinit var auth: FirebaseAuth
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentSelectLanguageBinding.inflate(inflater,container,false)
        navigateViaHindi()
        navigateViaEnglish()
        changeAppLanguage()
        auth = FirebaseAuth.getInstance()
        checkUserExist()
        return binding.root
    }




    private fun navigateViaHindi(){
        binding.btnHindi.setOnClickListener {
            findNavController().navigate(R.id.action_selectLanguage_to_fragment_phone_verification)
            kvStorage.storageSetString("AppLanguage","hindi")
            changeAppLanguage()
        }
    }

    private fun navigateViaEnglish(){
        binding.btnEnglish.setOnClickListener {
            findNavController().navigate(R.id.action_selectLanguage_to_fragment_phone_verification)
            kvStorage.storageSetString("AppLanguage","english")
            changeAppLanguage()
        }
    }

    //create a function that will change the app language
    private fun changeAppLanguage(){
        val language = kvStorage.storageGetString("AppLanguage")
        if(language == "hindi"){
            //change the app language hindi
            setLocale("hi")

        }else{
            setLocale("en-us")
        }
    }

    private fun setLocale(language: String) {
        val myLocale = Locale(language)
        Locale.setDefault(myLocale)
        val configuration:Configuration = resources.configuration
        configuration.setLocale(myLocale)
        requireActivity().baseContext.resources.updateConfiguration(configuration,requireActivity().baseContext.resources.displayMetrics)
    }

    override fun onResume() {
        super.onResume()
    }

    private fun checkUserExist(){
        val currentUser = auth.currentUser
        if(currentUser != null){
            findNavController().navigate(R.id.farmerDashboard)
        }
    }
    interface backPressedListener {
        fun onBackPressed()
    }


}