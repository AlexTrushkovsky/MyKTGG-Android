package ua.pp.trushkovsky.MyKTGG.ui.home

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.text.Html
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat.startActivity
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import kotlinx.android.synthetic.main.fragment_timetable.*
import ua.pp.trushkovsky.MyKTGG.R
import ua.pp.trushkovsky.MyKTGG.R.id
import ua.pp.trushkovsky.MyKTGG.R.layout
import ua.pp.trushkovsky.MyKTGG.ui.news.d.BASE_NEWS_URL
import java.text.SimpleDateFormat
import java.util.*

class RecyclerAdapter(
    private var title: List<String>,
    private var subtitle: List<String>,
    private var image: List<String>
): RecyclerView.Adapter<RecyclerAdapter.ViewHolder>() {

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val itemTitle: TextView = itemView.findViewById(id.bottom_title)
        val itemDetail: TextView = itemView.findViewById(id.bottom_text)
        val itemImage: ImageView = itemView.findViewById(id.bottom_image)

        init {
            itemView.setOnClickListener { v: View ->
                val position: Int = adapterPosition
                val intent = Intent(Intent(Intent.ACTION_VIEW))
//                Log.e("News", "${BASE_NEWS_URL + link[position]}")
//                intent.data = Uri.parse(BASE_NEWS_URL + link[position])
//                startActivity(itemView.context, intent, null)
            }
        }
    }

    private fun withoutHTML(html: String): String {
        var html = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            return  Html.fromHtml(html, Html.FROM_HTML_MODE_LEGACY).toString()
        } else {
            return Html.fromHtml(html).toString()
        }
    }

    private fun formatDate(date: String): String {
        var spf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
        val newDate: Date = spf.parse(date)
        spf = SimpleDateFormat("dd'.'MM'.'yyyy")
        return spf.format(newDate)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerAdapter.ViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(layout.main_item_layout, parent, false)
        return ViewHolder(v)
    }

    override fun onBindViewHolder(holder: RecyclerAdapter.ViewHolder, position: Int) {
        if (withoutHTML(title[position]) != "" && withoutHTML(title[position]).length >3) {
            holder.itemTitle.text = withoutHTML(title[position])
            holder.itemTitle.visibility = View.VISIBLE
        } else {
            holder.itemTitle.visibility = View.INVISIBLE
        }

        if (withoutHTML(subtitle[position]) != "" && withoutHTML(subtitle[position]).length >3) {
            holder.itemDetail.text = withoutHTML(subtitle[position])
            holder.itemDetail.visibility = View.VISIBLE
        } else {
            holder.itemDetail.visibility = View.INVISIBLE
        }

        if (image[position] != "") {
            Log.e("MainImage", "image not found")
            when {
                image[position] == "change" -> {
                    holder.itemImage.setImageResource(R.drawable.main_change)
                }
                image[position] == "alarm" -> {
                    holder.itemImage.setImageResource(R.drawable.main_alarm)
                }
                image[position] == "news" -> {
                    holder.itemImage.setImageResource(R.drawable.main_news)
                }
                else -> {
                    holder.itemImage.setImageResource(R.drawable.main_default)
                }
            }
        } else {
            holder.itemImage.setImageResource(R.drawable.main_default)
        }

    }

    override fun getItemCount(): Int {
        return title.size
    }

}