package `in`.jadu.anju.farmer.ui.fragments

import android.app.Activity.RESULT_OK
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.provider.Settings
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.AndroidEntryPoint
import `in`.jadu.anju.R
import `in`.jadu.anju.databinding.FragmentFarmerListItemBinding
import `in`.jadu.anju.farmer.models.dtos.ListItemTypes
import `in`.jadu.anju.farmer.viewmodels.FarmerListItemViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import java.io.File

@AndroidEntryPoint
class FarmerListItemFragment : Fragment() {
    private lateinit var binding: FragmentFarmerListItemBinding
    private lateinit var itemList: List<CardView>
    private val PICK_IMAGE_REQUEST = 1
    private lateinit var auth: FirebaseAuth
    private val farmerListItemViewModel: FarmerListItemViewModel by viewModels()
    private var bundle = Bundle()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = FragmentFarmerListItemBinding.inflate(inflater, container, false)
        auth = FirebaseAuth.getInstance()
        selectProductType()
        checkPermission()
        binding.cvCustomimage.setOnClickListener {
            openGallery()
        }
        binding.PreviewAddedProduct.setOnClickListener {
            if (isFieldsEmpty()) {
                Toast.makeText(
                    requireContext(), getString(R.string.Fields_not_empty), Toast.LENGTH_SHORT
                ).show()
                binding.productName.error = "Enter Product Name"
                binding.productDescription.error = "Enter Product Description"
                binding.harvestedDate.error = "Enter Harvested Date"
                binding.expiryDate.error = "Enter Expiry Date"
                binding.productPrice.error = "Enter Product Price"
            } else {
                productInfo()
                findNavController().navigate(
                    R.id.action_farmerListItemFragment2_to_farmerPreviewItemListFragment2, bundle
                )
            }
        }

        return binding.root
    }

    private fun selectProductType() {
        binding.fragmentProductType.apply {
            itemList = listOf(
                listItem01, listItem02, listItem03, listItem04, listItem05, listItem06, listItem07
            )
        }
        var selectedCardViewIndex: Int = -1
        itemList.forEachIndexed { index, itemView ->
            itemView.setOnClickListener {
                if (selectedCardViewIndex == index) {
                    // if the card view is already selected, reset its background
                    itemView.background =
                        ContextCompat.getDrawable(requireContext(), R.drawable.resetcardviewborder)
                    itemView.cardElevation = 10f
                    selectedCardViewIndex = -1
                } else {
                    // deselect the previously selected card view
                    selectedCardViewIndex.takeIf { it != -1 }?.let {
                        itemList[it].background = ContextCompat.getDrawable(
                            requireContext(), R.drawable.resetcardviewborder
                        )
                        itemList[it].cardElevation = 10f
                    }
                    // select the current card view and set its background to the border drawable
                    itemView.background =
                        ContextCompat.getDrawable(requireContext(), R.drawable.cardviewborder)
                    itemView.cardElevation = 20f
                    selectedCardViewIndex = index
                    farmerListItemViewModel.setIndex(index)
                }
            }
        }
        Log.d("TAG", "selectProductType: $selectedCardViewIndex")
    }

    private fun updateItemSelected(itemViewPosition: Int): Int {
        return itemViewPosition
    }

    private fun isFieldsEmpty(): Boolean {
        return binding.productName.text.toString()
            .isEmpty() || binding.productDescription.text.toString()
            .isEmpty() || binding.harvestedDate.text.toString()
            .isEmpty() || binding.expiryDate.text.toString()
            .isEmpty() || binding.productPrice.text.toString().isEmpty()
    }

    private fun productInfo() {
        val productName = binding.productName.text.toString()
        val productDescription = binding.productDescription.text.toString()
        val harvestedDate = binding.harvestedDate.text.toString()
        val expiryDate = binding.expiryDate.text.toString()
        val productPrice = binding.productPrice.text.toString()
        var productType = ""
        //get user phone number

        when (farmerListItemViewModel.getIndex()) {
            0 -> {
                productType = "Vegetables"
            }

            1 -> {
                productType = "Fruits"
            }

            2 -> {
                productType = "Handloom"
            }

            3 -> {
                productType = "Manures"
            }

            4 -> {
                productType = "Dairy"
            }

            5 -> {
                productType = "Poultry"
            }

            6 -> {
                productType = "Others"
            }
        }

        bundle = bundleOf(
            "productName" to productName,
            "productDescription" to productDescription,
            "harvestedDate" to harvestedDate,
            "expiryDate" to expiryDate,
            "productPrice" to productPrice,
            "productType" to productType,
            "ImageUri" to farmerListItemViewModel.getUri().toString()
        )


//        insertData to local
        lifecycleScope.launch(Dispatchers.IO) {
            farmerListItemViewModel.insertListItemTypes(
                ListItemTypes(
                    productType,
                    productName,
                    farmerListItemViewModel.getUri().toString(),
                    productDescription,
                    harvestedDate,
                    expiryDate,
                    productPrice,
                    auth.currentUser?.phoneNumber.toString()
                )
            )
        }
    }

    private fun openGallery() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(intent, PICK_IMAGE_REQUEST)
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null) {
            val selectedImageUri = data.data
            Log.d("ImageUrix", "onActivityResult: $selectedImageUri")
            if (selectedImageUri != null) {
                farmerListItemViewModel.setUri(selectedImageUri)
            }

            binding.ivCustomimageselect.setImageURI(selectedImageUri)
            binding.tvClicktoupload.text = "Image Uploaded"
            //cvcustomimage should be disabled
            binding.cvCustomimage.isEnabled = false
        }
    }

    private fun checkPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if (!Environment.isExternalStorageManager()) {
                val intent = Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION)
                intent.addCategory("android.intent.category.DEFAULT")
                intent.data = Uri.parse(String.format("package:%s", requireActivity().packageName))
                requireActivity().startActivity(intent, Bundle.EMPTY)
            }
        }
    }


}