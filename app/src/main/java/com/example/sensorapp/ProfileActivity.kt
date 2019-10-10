package com.example.sensorapp

import android.content.Context
import android.os.Build
import android.os.Bundle
import android.transition.Slide
import android.transition.TransitionManager
import android.util.Log
import android.view.*
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.room.Dao
import androidx.room.Room
import kotlinx.android.synthetic.main.activity_profile.*
import kotlinx.android.synthetic.main.name_change_popup.*
import kotlinx.android.synthetic.main.rv_history_row.*
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread
import java.time.LocalDateTime

class ProfileActivity : AppCompatActivity() {

    private lateinit var name: String
    private lateinit var history: MutableList<History>

    private lateinit var linearLayoutManager: LinearLayoutManager
    private lateinit var adapter: HistoryRecyclerAdapter


    override fun onCreate(savedInstanceState: Bundle?) {
        if (App.isNightModeEnabled()) {
            setTheme(R.style.DarkTheme)
        } else setTheme(R.style.AppTheme)

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)
        loadProfileData()

        val actionbar = supportActionBar
        actionbar!!.title = getString(R.string.profile_title)
        actionbar.setDisplayHomeAsUpEnabled(true)
        actionbar.setDisplayUseLogoEnabled(true)
        actionbar.setIcon(R.drawable.brightness_4)

        linearLayoutManager = LinearLayoutManager(this)
        recyclerView.layoutManager = linearLayoutManager
        recyclerView.addItemDecoration(DividerItemDecoration(applicationContext, DividerItemDecoration.VERTICAL))
    }

    private fun loadProfileData() {
        val db = Room.databaseBuilder(
            applicationContext,
            AppDatabase::class.java, "user.db"
        ).build()

        doAsync {
            name = db.dao().getUsername()
            history = db.dao().getAllHistory()

            textView_username.text = name
            adapter = HistoryRecyclerAdapter(history)
            recyclerView.adapter = adapter
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return super.onSupportNavigateUp()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.profile_settings, menu)
        return true
    }

    // Handles action bar item clicks
    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        val id = item.getItemId()

        if (id == R.id.action_menu1) {
            val inflater: LayoutInflater = getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            val view = inflater.inflate(R.layout.name_change_popup,null)
            val popupWindow = PopupWindow(
                view,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                true
            )

            // Set an elevation for the popup window
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                popupWindow.elevation = 10.0F
            }

            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                // Create a new slide animation for popup window enter transition
                val slideIn = Slide()
                slideIn.slideEdge = Gravity.TOP
                popupWindow.enterTransition = slideIn

                // Slide animation for popup window exit transition
                val slideOut = Slide()
                slideOut.slideEdge = Gravity.RIGHT
                popupWindow.exitTransition = slideOut
            }

            val buttonPopupCancel = view.findViewById<Button>(R.id.btn_nameChange_popup_cancel)
            val buttonPopupConfirm = view.findViewById<Button>(R.id.btn_nameChange_popup_confirm)
            var username = view.findViewById<EditText>(R.id.editText_newUsername)

            // Closes popup
            buttonPopupCancel.setOnClickListener{
                popupWindow.dismiss()
            }

            // Sets new username and makes sure it's not blank
            buttonPopupConfirm.setOnClickListener{
               var newUsername = username.text.toString()
                newUsername = newUsername.trim()

                if(!newUsername.isBlank()) {
                    val db = Room.databaseBuilder(
                        this,
                        AppDatabase::class.java, "user.db"
                    ).build()
                    doAsync {
                        db.dao().updateUsername((newUsername))
                        uiThread {
                            textView_username.text = newUsername
                        }
                    }
                    popupWindow.dismiss()
                } else {
                    Toast.makeText(
                        applicationContext,
                        getString(R.string.profile_emptyNameToast),
                        Toast.LENGTH_LONG
                    ).show()
                }
            }

            TransitionManager.beginDelayedTransition(root_layout)
            popupWindow.showAtLocation(root_layout, Gravity.CENTER, 0, 0)
        }
        return super.onOptionsItemSelected(item)
    }
}