package namnn.englishfloating.dialog

import android.app.AlertDialog
import android.app.Dialog
import android.content.SharedPreferences
import android.os.Bundle
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.widget.Button
import android.widget.EditText
import androidx.fragment.app.DialogFragment
import namnn.englishfloating.R

class FloatingSchedulerSettingsDialog(
    private val sharePref: SharedPreferences,
    private val startScheduler: (() -> Unit),
    private val stopScheduler: (() -> Unit)
): DialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val inflater = requireActivity().layoutInflater
        val view = inflater.inflate(R.layout.dialog_floating_scheduler_setting, null)

        val time = sharePref.getLong("TIME", 30)

        val etTime = view.findViewById<View>(R.id.dialog_et_time) as EditText
        etTime.setText(time.toString())

        (view.findViewById<View>(R.id.dialog_btn_start) as Button).setOnClickListener {
            if (etTime.text.isBlank()) return@setOnClickListener

            val newTime = etTime.text.toString().toLong() ?: 30
            val editor = sharePref.edit()
            editor.putLong("TIME", newTime)
            editor.apply()
            startScheduler.invoke()
            dismiss()
        }

        (view.findViewById<View>(R.id.dialog_btn_stop) as Button).setOnClickListener {
            stopScheduler.invoke()
            dismiss()
        }

        val builder = AlertDialog.Builder(context)
        builder.setView(view)
        return builder.create()
    }

    override fun onStart() {
        super.onStart()
        val window: Window? = dialog!!.window
        val windowParams: WindowManager.LayoutParams = window!!.attributes
        windowParams.dimAmount = 0.0f
        window.attributes = windowParams
    }
}