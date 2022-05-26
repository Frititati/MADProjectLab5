package it.polito.timebanking

import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.AttributeSet
import android.util.Log
import android.view.Menu
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
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
    private val firestoreUser = FirebaseAuth.getInstance().currentUser
    private val defaultProfilePath = "gs://madproject-3381c.appspot.com/user.png"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.appBarMain.toolbar)

        val drawerLayout = binding.drawerLayout
        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.fragment_content_main) as NavHostFragment
        val navController = navHostFragment.navController
        appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.personalTimeslotListFragment,
                R.id.showProfileFragment,
                R.id.allSkillFragment,
                R.id.favoritesListFragment,
                R.id.consumingJobsFragment,
                R.id.producingJobFragment
            ), drawerLayout
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
        binding.navView.setupWithNavController(navController)

        binding.navView.getHeaderView(0).findViewById<Button>(R.id.buttonLogout)
            .setOnClickListener {
                Firebase.auth.signOut()
                GoogleSignIn.getClient(
                    this, GoogleSignInOptions
                        .Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                        .build()
                )
                    .signOut()
                Toast.makeText(this, "Logout successfully", Toast.LENGTH_LONG).show()
                startActivity(Intent(this, EntryPointActivity::class.java))
                finish()
            }

        FirebaseFirestore.getInstance().collection("users")
            .document(firestoreUser!!.uid).get()
            .addOnSuccessListener { res ->
                if (!res.exists()) {
                    Firebase.storage.getReferenceFromUrl(defaultProfilePath).getBytes(1024 * 1024)
                        .addOnSuccessListener {
                            Firebase.storage.getReferenceFromUrl("gs://madproject-3381c.appspot.com/user_profile_picture/${firestoreUser.uid}.png")
                                .putBytes(it)
                        }
                    FirebaseFirestore.getInstance().collection("users").document(firestoreUser.uid)
                        .set(
                            ProfileData(
                                "Empty fullname",
                                "Empty nickname",
                                getSharedPreferences(
                                    "group21.lab5.PREFERENCES",
                                    MODE_PRIVATE
                                ).getString("email", "unknown email")!!,
                                defaultAge,
                                "Empty location",
                                listOf(),
                                listOf<Any>(),
                                "Empty description",
                                listOf<Any>(),
                                startingTime
                            )
                        )
                    binding.navView.getHeaderView(0)
                        .findViewById<TextView>(R.id.userTimeOnDrawer).text =
                        getSharedPreferences(
                            "group21.lab5.PREFERENCES",
                            MODE_PRIVATE
                        ).getString("email", "unknown email")

                } else {
                    updateIMG("gs://madproject-3381c.appspot.com/user_profile_picture/${firestoreUser.uid}.png")
                    FirebaseFirestore.getInstance().collection("users").document(firestoreUser.uid)
                        .get().addOnSuccessListener {
                            updateTime(
                                String.format(
                                    resources.getString(
                                        R.string.minutes,
                                        it.get("time").toString().toLong()
                                    )
                                )
                            )
                            updateFName(it.get("fullName").toString())
                        }
                }
            }
        timeVM.get(FirebaseAuth.getInstance().currentUser!!.uid).observe(this) {
            updateTime(String.format(resources.getString(R.string.minutes), it))
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

    override fun updateTime(time: String) {
        binding.navView.getHeaderView(0).findViewById<TextView>(R.id.userTimeOnDrawer).text = time
    }

    override fun updateFName(name: String) {
        binding.navView.getHeaderView(0).findViewById<TextView>(R.id.userNameOnDrawer).text = name
    }

    override fun updateIMG(url: String) {
        Firebase.storage.getReferenceFromUrl(url)
            .getBytes(1024 * 1024).addOnSuccessListener { pic ->
                binding.navView.getHeaderView(0)
                    .findViewById<ShapeableImageView>(R.id.userImageOnDrawer)
                    .setImageBitmap(
                        BitmapFactory.decodeByteArray(
                            pic,
                            0,
                            pic.size
                        )
                    )
            }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        super.onCreateOptionsMenu(menu)
        menuInflater.inflate(R.menu.main, menu)
        /*if (this.resources.configuration.uiMode.and(Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES) {
            val item = menu.getItem(0)
            val s = SpannableString("Edit")
            s.setSpan(ForegroundColorSpan(Color.WHITE), 0, s.length, 0)
            item.title = s
        }*/
        return true
    }

    override fun onSupportNavigateUp(): Boolean {
        return findNavController(R.id.fragment_content_main)
            .navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }
}