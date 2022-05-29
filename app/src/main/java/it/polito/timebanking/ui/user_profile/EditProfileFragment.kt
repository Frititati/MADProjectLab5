package it.polito.timebanking.ui.user_profile

import android.app.Activity.RESULT_OK
import android.content.ActivityNotFoundException
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.*
import androidx.core.graphics.scale
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.findNavController
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import it.polito.timebanking.NavBarUpdater
import it.polito.timebanking.R
import it.polito.timebanking.databinding.FragmentEditProfileBinding
import it.polito.timebanking.model.profile.*
import java.io.ByteArrayOutputStream
import kotlin.concurrent.thread

@Suppress("DEPRECATION")
class EditProfileFragment : Fragment() {

    private val vm by viewModels<ProfileViewModel>()
    private val galleryId = 1
    private val cameraId = 2
    private var picModified = false
    private val binding get() = _binding!!
    private var _binding: FragmentEditProfileBinding? = null
    private var imageBitmap = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888)
    private lateinit var drawerListener: NavBarUpdater
    private val firebaseUserID = FirebaseAuth.getInstance().uid!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        requireActivity().findViewById<DrawerLayout>(R.id.drawer_layout).setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED)
        _binding = FragmentEditProfileBinding.inflate(inflater, container, false)
        drawerListener = context as NavBarUpdater
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val userProfilePath = String.format(requireActivity().getString(R.string.firebaseUserPic,firebaseUserID))
        vm.get(firebaseUserID).observe(viewLifecycleOwner) {
            binding.fullName.hint = fullNameFormatter(it.fullName)
            binding.nickName.hint = nickNameFormatter(it.nickName)
            binding.age.hint = ageFormatter(it.age.toString()) + " Years Old"
            binding.email.hint = emailFormatter(it.email)
            binding.location.hint = locationFormatter(it.location)
            binding.description.hint = descriptionFormatter(it.description)
            Firebase.storage.getReferenceFromUrl(userProfilePath).getBytes(1024 * 1024).addOnSuccessListener { pic ->
                    binding.userImage.setImageBitmap(BitmapFactory.decodeByteArray(pic, 0, pic.size))
                }
        }

        binding.userImage.setOnClickListener {
            registerForContextMenu(it)
            requireActivity().openContextMenu(it)
        }

        binding.buttonSkill.setOnClickListener {
            view.findNavController().navigate(R.id.edit_profile_to_edit_skill)
            Snackbar.make(binding.root, "Edit your skills here", 1500).show()

        }
    }

    override fun onPause() {
        super.onPause()
        thread {
            vm.update(firebaseUserID, binding.fullName.text.toString().trim().trim(), binding.nickName.text.toString().trim(), binding.age.text.toString().toLongOrNull(), binding.email.text.toString().trim(), binding.location.text.toString().trim(), binding.description.text.toString().trim())
        }
        if (binding.fullName.text.toString().isNotEmpty()) drawerListener.updateFName(binding.fullName.text.toString())
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onCreateContextMenu(
        menu: ContextMenu, v: View, menuInfo: ContextMenu.ContextMenuInfo?
    ) {
        super.onCreateContextMenu(menu, v, menuInfo)
        menu.setHeaderTitle("Choose a pic")
        menu.add(0, galleryId, 0, "Choose from gallery")
        menu.add(0, cameraId, 0, "Choose from camera")
    }

    override fun onContextItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            cameraId -> dispatchTakePictureIntent()
            galleryId -> imageChooser()
        }
        return super.onContextItemSelected(item)
    }

    private fun dispatchTakePictureIntent() {
        picModified = true
        try {
            startActivityForResult(Intent(MediaStore.ACTION_IMAGE_CAPTURE), cameraId)
        } catch (e: ActivityNotFoundException) {
            Log.w("activity", "No Activity found...")
            Snackbar.make(binding.root, "No camera activity", Snackbar.LENGTH_SHORT).show()
        }
    }

    private fun imageChooser() {
        picModified = true
        val choosePictureIntent = Intent(Intent.ACTION_GET_CONTENT)
        try {
            choosePictureIntent.type = "image/*"
            startActivityForResult(Intent.createChooser(choosePictureIntent, "Select Picture"), galleryId)
        } catch (e: ActivityNotFoundException) {
            Log.w("activity", "No Activity found...")
            Snackbar.make(binding.root, "No gallery activity found", Snackbar.LENGTH_SHORT).show()
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        val userProfilePath = String.format(requireActivity().getString(R.string.firebaseUserPic,firebaseUserID))
        if (resultCode == RESULT_OK) {
            val h = 300
            val w = 300
            if (requestCode == cameraId) {
                imageBitmap = data!!.extras!!.get("data") as Bitmap
                imageBitmap.scale(w, h)
            }
            if (requestCode == galleryId) {
                imageBitmap = MediaStore.Images.Media.getBitmap(requireActivity().contentResolver, data!!.data).scale(w, h)
            }
            binding.userImage.setImageBitmap(imageBitmap)
            val b = ByteArrayOutputStream()
            imageBitmap.compress(Bitmap.CompressFormat.JPEG, 100, b)
            Firebase.storage.getReferenceFromUrl(userProfilePath).putBytes(b.toByteArray()).addOnSuccessListener {
                    drawerListener.updateIMG(userProfilePath)
                }
        }
    }
}