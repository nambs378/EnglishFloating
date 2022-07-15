package namnn.englishfloating.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.recyclerview.widget.RecyclerView
import namnn.englishfloating.database.entity.Vocabulary
import namnn.englishfloating.R

class VocabularyAdapter(
    private val context: Context,
    private var dataSet: List<Vocabulary>,
    private val onDeleteListener: (vocabulary: Vocabulary) -> Unit,
    private val onImportantListener: (vocabulary: Vocabulary) -> Unit,
) :
    RecyclerView.Adapter<VocabularyAdapter.ViewHolder>() {

    private var previousLayoutShow: View? = null

    public fun setData(newData: List<Vocabulary>) {
        dataSet = newData;
    }

    /**
     * Provide a reference to the type of views that you are using
     * (custom ViewHolder).
     */
    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        var rootView: ConstraintLayout = view.findViewById(R.id.item_root)
        var tvEnglish: TextView = view.findViewById(R.id.tv_item_english)
        var tvVietnamese: TextView = view.findViewById(R.id.tv_item_vietnamese)
        var tvWrongCount: TextView = view.findViewById(R.id.tv_item_wrong_count)
        var ivStar: ImageView = view.findViewById(R.id.iv_star)

        var layoutDelete: ConstraintLayout = view.findViewById(R.id.item_layout_delete)
        var ivDelete: ImageView = view.findViewById(R.id.iv_delete)
    }

    // Create new views (invoked by the layout manager)
    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        // Create a new view, which defines the UI of the list item
        val view = LayoutInflater.from(viewGroup.context)
            .inflate(R.layout.voca_item, viewGroup, false)

        return ViewHolder(view)
    }

    // Replace the contents of a view (invoked by the layout manager)
    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {

        // Get element from your dataset at this position and replace the
        // contents of the view with that element
        val vocabulary = dataSet[position]
        viewHolder.tvEnglish.text = vocabulary.english
        viewHolder.tvVietnamese.text = vocabulary.vietnamese
        viewHolder.tvWrongCount.text = vocabulary.wrongCount.toString()
        var important = vocabulary.important

        if (important) {
//            viewHolder.ivStar.background = ResourcesCompat.getDrawable(thi, R.drawable.ic_star_selected, null)
            viewHolder.ivStar.background =
                ContextCompat.getDrawable(context, R.drawable.ic_star_selected)
        } else {
            viewHolder.ivStar.background =
                ContextCompat.getDrawable(context, R.drawable.ic_star_unselect)
        }


        viewHolder.ivDelete.setOnClickListener {
            if (previousLayoutShow != null) {
                previousLayoutShow!!.visibility = View.GONE
                previousLayoutShow = null
            }
            onDeleteListener(vocabulary)
        }

        viewHolder.ivStar.setOnClickListener {
            important = !important
            if (important) {
                viewHolder.ivStar.background =
                    ContextCompat.getDrawable(context, R.drawable.ic_star_selected)
            } else {
                viewHolder.ivStar.background =
                    ContextCompat.getDrawable(context, R.drawable.ic_star_unselect)
            }
            vocabulary.important = important
            onImportantListener(vocabulary)
        }

        viewHolder.rootView.setOnLongClickListener {
            if (previousLayoutShow != null) {
                previousLayoutShow!!.visibility = View.GONE
            }
            previousLayoutShow = viewHolder.layoutDelete
            viewHolder.layoutDelete.visibility = View.VISIBLE
            true
        }

        viewHolder.rootView.setOnClickListener {
            if (previousLayoutShow != null) {
                previousLayoutShow!!.visibility = View.GONE
                previousLayoutShow = null
            }
            viewHolder.layoutDelete.visibility = View.GONE
        }
    }

    // Return the size of your dataset (invoked by the layout manager)
    override fun getItemCount() = dataSet.size

}
