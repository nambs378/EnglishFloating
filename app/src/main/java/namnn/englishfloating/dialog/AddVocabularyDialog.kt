package namnn.englishfloating.dialog

import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import android.widget.FrameLayout
import androidx.fragment.app.DialogFragment
import com.mannan.translateapi.Language
import com.mannan.translateapi.TranslateAPI
import namnn.englishfloating.R
import namnn.englishfloating.database.dao.VocabularyDAO
import namnn.englishfloating.database.entity.Vocabulary


class AddVocabularyDialog(
    private val vocabularyDAO: VocabularyDAO,
    private val addSuccess: (() -> Unit),
    private val showSnack: ((String) -> Unit),
    private val hideKeyboard: (() -> Unit)
) : DialogFragment() {
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val inflater = requireActivity().layoutInflater
        val view = inflater.inflate(R.layout.dialog_add_vocabulary, null)

        val etEnglish = view.findViewById<View>(R.id.dialog_et_english) as EditText
        val etVietnamese = view.findViewById<View>(R.id.dialog_et_vietnamese) as EditText


        (view.findViewById<View>(R.id.dialog_btn_translate) as Button).setOnClickListener {
            hideKeyboardInAndroidFragment(view)
            if (etEnglish.text.isBlank()) return@setOnClickListener
            val translateApi: TranslateAPI =
                TranslateAPI(Language.ENGLISH, Language.VIETNAMESE, etEnglish.text.toString())
            translateApi.setTranslateListener(object : TranslateAPI.TranslateListener {
                override fun onSuccess(translatedText: String?) {
                    etVietnamese.setText(translatedText.toString())
                }

                override fun onFailure(ErrorText: String?) {
                    showSnack("Translate Failure")
                }
            })
        }

        (view.findViewById<View>(R.id.dialog_btn_english_clear) as FrameLayout).setOnClickListener {
            etEnglish.text?.clear()
        }

        (view.findViewById<View>(R.id.dialog_btn_vietnamese_clear) as FrameLayout).setOnClickListener {
            etVietnamese.text?.clear()
        }


        (view.findViewById<View>(R.id.dialog_btn_add) as Button).setOnClickListener {
            var snackTxt = "";
            if (vocabularyDAO.checkEnglishExist(etEnglish.text.toString())) {
                snackTxt = "Vocabulary already exists"
                showSnack.invoke(snackTxt)
                hideKeyboardInAndroidFragment(view)
            } else {
                if (etEnglish.text.toString().isBlank() || etVietnamese.text.toString().isBlank()
                ) {
                    snackTxt = "English and Vietnamese cannot be left blank"
                    showSnack.invoke(snackTxt)
                    hideKeyboardInAndroidFragment(view)
                } else {
                    val newLang =
                        Vocabulary(null, etEnglish.text.toString(), etVietnamese.text.toString(), 0, false)
                    vocabularyDAO.insert(newLang)
                    addSuccess.invoke()
                    snackTxt = "Add new vocabulary success"
                    showSnack.invoke(snackTxt)
                    dismiss()
                }
            }

        }

        val builder = AlertDialog.Builder(context)
        builder.setView(view)
        return builder.create()
    }

    fun hideKeyboardInAndroidFragment(view: View) {
        val imm: InputMethodManager =
            view.context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(view.windowToken, 0)
    }

    override fun onStart() {
        super.onStart()
        val window: Window? = dialog!!.window
        val windowParams: WindowManager.LayoutParams = window!!.attributes
        windowParams.dimAmount = 0.0f
        window.attributes = windowParams
    }

}