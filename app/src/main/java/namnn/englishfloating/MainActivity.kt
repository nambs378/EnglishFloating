package namnn.englishfloating

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.room.Room
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.activity_main.*
import namnn.englishfloating.Service.FloatingViewService
import namnn.englishfloating.adapter.VocabularyAdapter
import namnn.englishfloating.database.AppDatabase
import namnn.englishfloating.database.dao.VocabularyDAO
import namnn.englishfloating.database.entity.Vocabulary


class MainActivity : AppCompatActivity() {
    private val CODE_DRAW_OVER_OTHER_APP_PERMISSION = 2048

    private lateinit var viewGroup: ViewGroup

    private lateinit var langAdapter: VocabularyAdapter
    private lateinit var vocabularies: List<Vocabulary>
    private lateinit var vocabularyDAO: VocabularyDAO

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        viewGroup =
            (findViewById<View>(android.R.id.content) as ViewGroup).getChildAt(0) as ViewGroup

        // Initial
        val db = Room.databaseBuilder(
            applicationContext,
            AppDatabase::class.java, "english_db",
        ).fallbackToDestructiveMigration().allowMainThreadQueries()
            .build()

        vocabularyDAO = db.languageDao()

        val ref = this
//        lifecycleScope.launch(Dispatchers.IO) {
//            languages = db.languageDao().getALL()
//
//            ref.runOnUiThread {
//                langAdapter = LanguageAdapter(languages);
//
//                rv.adapter = langAdapter
//                rv.layoutManager = LinearLayoutManager(ref);
//            }
//        }

        vocabularies = vocabularyDAO.getALL()

        langAdapter = VocabularyAdapter(vocabularies, onDeleteListener = {
            vocabularyDAO.delete(it)

            vocabularies = vocabularyDAO.getALL()
            langAdapter.setData(vocabularies)
            langAdapter.notifyDataSetChanged()
            viewGroup.snack("Delete vocabulary success")
        })

        rv.adapter = langAdapter
        rv.layoutManager = LinearLayoutManager(ref)

        btnAdd.setOnClickListener {
            var snackTxt = "";
            if (vocabularyDAO.checkEnglishExist(et_english.text.toString())) {
                snackTxt = "Vocabulary already exists"
            } else {
                if (et_english.text.toString().isBlank() || et_vietnamese.text.toString().isBlank()
                ) {
                    snackTxt = "English and Vietnamese cannot be left blank"
                } else {
                    val newLang =
                        Vocabulary(null, et_english.text.toString(), et_vietnamese.text.toString())
                    vocabularyDAO.insert(newLang)

                    vocabularies = vocabularyDAO.getALL()
                    langAdapter.setData(vocabularies)
                    langAdapter.notifyDataSetChanged()
                    snackTxt = "Add new vocabulary success"
                }
            }
            clearEt()
            viewGroup.snack(snackTxt)
            closeKeyboard()
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(this)) {
            //If the draw over permission is not available open the settings screen
            //to grant the permission.
            val intent = Intent(
                Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                Uri.parse("package:$packageName")
            )
            intent.putExtra("requestCode", CODE_DRAW_OVER_OTHER_APP_PERMISSION)
            val previewRequest =
                registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
                    if (it.resultCode == Activity.RESULT_OK) {
                        initializeView()
                    } else { //Permission is not available
                        Toast.makeText(
                            this,
                            "Draw over other app permission not available. Closing the application",
                            Toast.LENGTH_SHORT
                        ).show()
                        finish()
                    }
                }
            previewRequest.launch(intent)
        } else {
            initializeView()
        }
    }

    private fun initializeView() {
        iv_start_floating.setOnClickListener {
            if (vocabularies.count() < 4) {
                viewGroup.snack("Need more than 4 vocabularies to open floating test")
                return@setOnClickListener
            }
            startService(Intent(this@MainActivity, FloatingViewService::class.java))
            finish()
        }
    }

    fun View.snack(message: String, duration: Int = Snackbar.LENGTH_LONG) {
        Snackbar.make(this, message, duration).show()
    }

    private fun clearEt() {
        et_english.text?.clear()
        et_vietnamese.text?.clear()
    }

    private fun closeKeyboard() {
        // this will give us the view
        // which is currently focus
        // in this layout
        val view = this.currentFocus

        // if nothing is currently
        // focus then this will protect
        // the app from crash
        if (view != null) {

            // now assign the system
            // service to InputMethodManager
            val manager: InputMethodManager = getSystemService(
                Context.INPUT_METHOD_SERVICE
            ) as InputMethodManager
            manager
                .hideSoftInputFromWindow(
                    view.windowToken, 0
                )
        }
    }
}