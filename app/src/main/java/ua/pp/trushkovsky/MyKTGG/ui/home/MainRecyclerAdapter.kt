package ua.pp.trushkovsky.MyKTGG.ui.home

import android.content.Context
import android.os.Build
import android.text.Html
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.ernestoyaquello.dragdropswiperecyclerview.DragDropSwipeAdapter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import ua.pp.trushkovsky.MyKTGG.R
import ua.pp.trushkovsky.MyKTGG.R.id
import ua.pp.trushkovsky.MyKTGG.ui.home.model.Pushes
import java.lang.reflect.Type

class MainRecyclerAdapter(dataSet: List<Pushes> = emptyList()): DragDropSwipeAdapter<Pushes, MainRecyclerAdapter.ViewHolder>(dataSet) {

    inner class ViewHolder(itemView: View) : DragDropSwipeAdapter.ViewHolder(itemView) {
        val itemTitle: TextView = itemView.findViewById(id.bottom_title)
        val itemDetail: TextView = itemView.findViewById(id.bottom_text)
        val itemImage: ImageView = itemView.findViewById(id.bottom_image)

//        init {
//            itemView.setOnClickListener { v: View ->
//                val position: Int = adapterPosition
//                val intent = Intent(Intent(Intent.ACTION_VIEW))
//                Log.e("News", "${BASE_NEWS_URL + link[position]}")
//                intent.data = Uri.parse(BASE_NEWS_URL + link[position])
//                startActivity(itemView.context, intent, null)
//            }
//        }
    }

    private fun withoutHTML(html: String): String {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            Html.fromHtml(html, Html.FROM_HTML_MODE_LEGACY).toString()
        } else {
            Html.fromHtml(html).toString()
        }
    }

    override fun onBindViewHolder(item: Pushes, viewHolder: ViewHolder, position: Int) {
        if (withoutHTML(item.title) != "" && withoutHTML(item.title).length >3) {
            viewHolder.itemTitle.text = withoutHTML(item.title)
            viewHolder.itemTitle.visibility = View.VISIBLE
        } else {
            viewHolder.itemTitle.visibility = View.INVISIBLE
        }

        if (withoutHTML(item.subtitle) != "" && withoutHTML(item.subtitle).length >3) {
            viewHolder.itemDetail.text = withoutHTML(item.subtitle)
            viewHolder.itemDetail.visibility = View.VISIBLE
        } else {
            viewHolder.itemDetail.visibility = View.INVISIBLE
        }

        if (item.image != "") {
            Log.e("MainImage", "image not found")
            when {
                item.image == "change" -> {
                    viewHolder.itemImage.setImageResource(R.drawable.main_change)
                }
                item.image == "alarm" -> {
                    viewHolder.itemImage.setImageResource(R.drawable.main_alarm)
                }
                item.image == "news" -> {
                    viewHolder.itemImage.setImageResource(R.drawable.main_news)
                }
                else -> {
                    viewHolder.itemImage.setImageResource(R.drawable.main_default)
                }
            }
        } else {
            viewHolder.itemImage.setImageResource(R.drawable.main_default)
        }

    }

    override fun getViewHolder(itemView: View): ViewHolder {
        return ViewHolder(itemView)
    }

    override fun getViewToTouchToStartDraggingItem(
        item: Pushes,
        viewHolder: ViewHolder,
        position: Int
    ): View? {
        return viewHolder.itemImage
    }
}