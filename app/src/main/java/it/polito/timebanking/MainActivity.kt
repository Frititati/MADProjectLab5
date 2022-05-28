package it.polito.timebanking

import android.app.AlertDialog
import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Bundle
import android.view.Menu
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.material.imageview.ShapeableImageView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import it.polito.timebanking.databinding.ActivityMainBinding
import it.polito.timebanking.model.profile.ProfileData
import it.polito.timebanking.ui.TimeViewModel


class MainActivity : AppCompatActivity(), NavBarUpdater {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityMainBinding
    private val defaultAge = 18L
    private val startingTime = 120L
    private val timeVM by viewModels<TimeViewModel>()
    private val firebaseUserID = FirebaseAuth.getInstance().currentUser!!.uid

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.appBarMain.toolbar)

        val drawerLayout = binding.drawerLayout
        val navHostFragment = supportFragmentManager.findFragmentById(R.id.fragment_content_main) as NavHostFragment
        val navController = navHostFragment.navController
        appBarConfiguration = AppBarConfiguration(setOf(R.id.personalTimeslotListFragment, R.id.showProfileFragment, R.id.allSkillFragment, R.id.favoritesListFragment, R.id.consumingJobsFragment, R.id.producingJobFragment), drawerLayout)
        setupActionBarWithNavController(navController, appBarConfiguration)
        binding.navView.setupWithNavController(navController)

        binding.navView.getHeaderView(0).findViewById<Button>(R.id.buttonLogout).setOnClickListener {
                findViewById<DrawerLayout>(R.id.drawer_layout).setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED)
                val dialog = AlertDialog.Builder(this)
                dialog.setTitle("Are you sure you want to log out?")
                dialog.setView(this.layoutInflater.inflate(R.layout.dialog_generic, findViewById(android.R.id.content), false))
                dialog.setNegativeButton("No") { _, _ ->
                    findViewById<DrawerLayout>(R.id.drawer_layout).setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED)
                }
                dialog.setPositiveButton("Yes") { _, _ ->
                    Firebase.auth.signOut()
                    GoogleSignIn.getClient(this, GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).build()).signOut()
                    Toast.makeText(this, "See you soon!", Toast.LENGTH_SHORT).show()
                    startActivity(Intent(this, EntryPointActivity::class.java))
                    finish()
                }
                dialog.create().show()
            }

        FirebaseFirestore.getInstance().collection("users").document(firebaseUserID).get().addOnSuccessListener { res ->
                if (!res.exists()) {
                    Firebase.storage.getReferenceFromUrl(String.format(resources.getString(R.string.firebaseDefaultPic))).getBytes(1024 * 1024).addOnSuccessListener {
                            Firebase.storage.getReferenceFromUrl(String.format(resources.getString(R.string.firebaseUserPic,firebaseUserID))).putBytes(it)
                        }
                    FirebaseFirestore.getInstance().collection("users").document(firebaseUserID).set(ProfileData("Empty FullName", "Empty Nickname", getSharedPreferences("group21.lab5.PREFERENCES", MODE_PRIVATE).getString("email", "unknown email")!!, defaultAge, "Empty location", listOf<String>(), listOf<String>(), "Empty description", listOf<String>(), startingTime, 0, 0))
                    binding.navView.getHeaderView(0).findViewById<TextView>(R.id.userTimeOnDrawer).text = getSharedPreferences("group21.lab5.PREFERENCES", MODE_PRIVATE).getString("email", "unknown email")

                } else {
                    updateIMG(String.format(resources.getString(R.string.firebaseUserPic,firebaseUserID)))
                    FirebaseFirestore.getInstance().collection("users").document(firebaseUserID).get().addOnSuccessListener {
                            updateTime(it.get("time").toString().toLong())
                            updateFName(it.get("fullName").toString())
                        }
                }
            }
        timeVM.get(firebaseUserID).observe(this) {
            updateTime(it)
        }

        /*
          when (this.resources?.configuration?.uiMode?.and(Configuration.UI_MODE_NIGHT_MASK)) {
              Configuration.UI_MODE_NIGHT_YES -> {
                  window.statusBarColor = ContextCompat.getColor(this, R.color.myGreenDark)
              }
              Configuration.UI_MODE_NIGHT_NO -> {
                  window.statusBarColor = ContextCompat.getColor(this, R.color.myGreenLight)
              }
          }
         */
        window.statusBarColor = ContextCompat.getColor(this, R.color.MenuColor)

    }

    override fun setTitleWithSkill(title: String?) {
        supportActionBar!!.title = title
    }

    override fun updateTime(time: Long) {
        binding.navView.getHeaderView(0).findViewById<TextView>(R.id.userTimeOnDrawer).text = timeFormatter(time)
    }

    private fun timeFormatter(time: Long): String {
        val h = if (time / 60L == 1L) "1 hour"
        else "${time / 60L} hours"
        val m = if (time % 60L == 1L) "1 minute"
        else "${time % 60L} minutes"
        return if (h == "0 hours") m
        else "$h, $m"
    }

    override fun updateFName(name: String) {
        binding.navView.getHeaderView(0).findViewById<TextView>(R.id.userNameOnDrawer).text = name
    }

    override fun updateIMG(url: String) {
        Firebase.storage.getReferenceFromUrl(url).getBytes(1024 * 1024).addOnSuccessListener { pic ->
                binding.navView.getHeaderView(0).findViewById<ShapeableImageView>(R.id.userImageOnDrawer).setImageBitmap(BitmapFactory.decodeByteArray(pic, 0, pic.size))
            }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        super.onCreateOptionsMenu(menu)
        menuInflater.inflate(R.menu.main, menu)/*if (this.resources.configuration.uiMode.and(Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES) {
            val item = menu.getItem(0)
            val s = SpannableString("Edit")
            s.setSpan(ForegroundColorSpan(Color.WHITE), 0, s.length, 0)
            item.title = s
        }*/
        return true
    }

    override fun onSupportNavigateUp(): Boolean {
        return findNavController(R.id.fragment_content_main).navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }
}