package namnn.englishfloating.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import namnn.englishfloating.database.entity.Vocabulary
import namnn.englishfloating.R

class VocabularyAdapter(
    private var dataSet: List<Vocabulary>,
    private val onDeleteListener: (vocabulary: Vocabulary) -> Unit
) :
    RecyclerView.Adapter<VocabularyAdapter.ViewHolder>() {

    public fun setData(newData: List<Vocabulary>) {
        dataSet = newData;
    }

    /**
     * Provide a reference to the type of views that you are using
     * (custom ViewHolder).
     */
    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        var tvEnglish: TextView = view.findViewById(R.id.tv_item_english)
        var tvVietnamese: TextView = view.findViewById(R.id.tv_item_vietnamese)
        var ivDelete: ImageView = view.findViewById(R.id.iv_delete)
        var tvWrongCount: TextView = view.findViewById(R.id.tv_item_wrong_count)
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

        viewHolder.ivDelete.setOnClickListener {
            onDeleteListener(vocabulary)
        }
    }

    // Return the size of your dataset (invoked by the layout manager)
    override fun getItemCount() = dataSet.size

}
