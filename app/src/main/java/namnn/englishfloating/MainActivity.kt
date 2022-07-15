package namnn.englishfloating

import android.app.Activity
import android.content.*
import android.net.ConnectivityManager
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
import namnn.englishfloating.broadcast.MyStartServiceReceiver
import namnn.englishfloating.database.AppDatabase
import namnn.englishfloating.database.dao.VocabularyDAO
import namnn.englishfloating.database.entity.Vocabulary
import namnn.englishfloating.dialog.AddVocabularyDialog
import namnn.englishfloating.dialog.FloatingSchedulerSettingsDialog
import namnn.englishfloating.util.Util


class MainActivity : AppCompatActivity() {
    private val CODE_DRAW_OVER_OTHER_APP_PERMISSION = 2048
    lateinit var br: BroadcastReceiver

    private lateinit var viewGroup: ViewGroup

    private lateinit var langAdapter: VocabularyAdapter
    private lateinit var vocabularies: List<Vocabulary>
    private lateinit var vocabularyDAO: VocabularyDAO

    private lateinit var addDialog: AddVocabularyDialog
    private lateinit var schedulerDialog: FloatingSchedulerSettingsDialog

    private lateinit var sharePref: SharedPreferences

    private var isSortWrongCount = false
    private var isSortStar = false

    private lateinit var sortState : Sort


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        sortState = Sort.ALL

        // Initial
        val ref = this
        br = MyStartServiceReceiver()

        sharePref = getSharedPreferences("ENGLISH_FLOATING", Context.MODE_PRIVATE)

        val db = Room.databaseBuilder(
            applicationContext,
            AppDatabase::class.java, "english_db",
        ).fallbackToDestructiveMigration().allowMainThreadQueries()
            .build()

        vocabularyDAO = db.languageDao()

        viewGroup =
            (findViewById<View>(android.R.id.content) as ViewGroup).getChildAt(0) as ViewGroup

        schedulerDialog = FloatingSchedulerSettingsDialog(sharePref, startScheduler = {
            if (vocabularies.count() < 4) {
                viewGroup.snack("Need more than 4 vocabularies to start scheduler test")
                return@FloatingSchedulerSettingsDialog
            }
            val filter = IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION).apply {
                addAction(Intent.ACTION_AIRPLANE_MODE_CHANGED)
            }
            try {
                registerReceiver(br, filter)
            } catch (e: IllegalArgumentException) {
                e.printStackTrace()
            }
            viewGroup.snack("Start job scheduler for float")
        }, stopScheduler = {
            try {
                unregisterReceiver(br)
                Util.cancelScheduleJob(this)
            } catch (e: IllegalArgumentException) {
                e.printStackTrace()
            }
            viewGroup.snack("Stop job scheduler for float")
        })

        addDialog = AddVocabularyDialog(vocabularyDAO, addSuccess = {
            vocabularies = vocabularyDAO.getALL()
            langAdapter.setData(vocabularies)
            langAdapter.notifyDataSetChanged()
        }, showSnack = {
            viewGroup.snack(it)
        }, hideKeyboard = {

        })

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

        langAdapter = VocabularyAdapter(this ,vocabularies, onDeleteListener = {
            vocabularyDAO.delete(it)

            vocabularies = vocabularyDAO.getALL()
            langAdapter.setData(vocabularies)
            langAdapter.notifyDataSetChanged()
            viewGroup.snack("Delete vocabulary success")
        }, onImportantListener = {
            vocabularyDAO.updateImportantById(it.id!!, it.important)
        })

        rv.adapter = langAdapter
        rv.layoutManager = LinearLayoutManager(ref)

        btnAdd.setOnClickListener {

            addDialog.show(supportFragmentManager, "add dialog")
        }

        btn_sort_by_star.setOnClickListener {
            isSortStar = !isSortStar
            isSortWrongCount = false

            if (isSortStar) {
                setSortState(Sort.IMPORTANT)
            } else {
                setSortState(Sort.ALL)
            }
        }

        btn_sort_by_wrong_count.setOnClickListener {
            isSortWrongCount = !isSortWrongCount
            isSortStar = false

            if (isSortWrongCount) {
                setSortState(Sort.WRONG)
            } else {
                setSortState(Sort.ALL)
            }
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

        iv_start_scheduler.setOnClickListener {
            schedulerDialog.show(supportFragmentManager, "scheduler dialog")
        }
    }

    private fun initializeView() {
        iv_start_floating.setOnClickListener {
            if (vocabularies.count() < 4) {
                viewGroup.snack("Need more than 4 vocabularies to start floating test")
                return@setOnClickListener
            }
            startService(Intent(this@MainActivity, FloatingViewService::class.java))
//            finish()
        }
    }

    private fun View.snack(message: String, duration: Int = Snackbar.LENGTH_LONG) {
        Snackbar.make(this, message, duration).show()
    }

    private fun setSortState(state: Sort) {
        when (state) {
            Sort.ALL -> {
                vocabularies = vocabularyDAO.getALL()
                iv_wrong_line.visibility = View.VISIBLE
                iv_line_star.visibility = View.VISIBLE
            }
            Sort.IMPORTANT -> {
                vocabularies = vocabularyDAO.getImportant()
                iv_line_star.visibility = View.INVISIBLE
                iv_wrong_line.visibility = View.VISIBLE
            }
            Sort.WRONG -> {
                vocabularies = vocabularyDAO.getALLOrderByWrongCount()
                iv_line_star.visibility = View.VISIBLE
                iv_wrong_line.visibility = View.INVISIBLE
            }
        }
        langAdapter.setData(vocabularies)
        langAdapter.notifyDataSetChanged()
    }
}

enum class Sort {
    ALL,
    IMPORTANT,
    WRONG
}