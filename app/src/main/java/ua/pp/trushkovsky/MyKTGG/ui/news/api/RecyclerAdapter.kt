package ua.pp.trushkovsky.MyKTGG.ui.news.api

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
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import ua.pp.trushkovsky.MyKTGG.R
import ua.pp.trushkovsky.MyKTGG.R.id
import ua.pp.trushkovsky.MyKTGG.R.layout
import ua.pp.trushkovsky.MyKTGG.ui.news.d.BASE_NEWS_URL
import java.text.SimpleDateFormat
import java.util.*

class RecyclerAdapter(
    private var category: List<String>,
    private var created: List<String>,
    private var imageMedium: List<String>,
    private var introtext: List<String>,
    private var link: List<String>,
    private var title: List<String>
): RecyclerView.Adapter<RecyclerAdapter.ViewHolder>() {

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val itemTitle: TextView = itemView.findViewById(id.bottom_title)
        val itemDetail: TextView = itemView.findViewById(id.bottom_text)
        val itemImage: ImageView = itemView.findViewById(id.news_image)
        val itemCreated: TextView = itemView.findViewById(id.data_news)
        val itemCategory: TextView = itemView.findViewById(id.rubric_news)

        init {
            itemView.setOnClickListener { v: View ->
                val position: Int = adapterPosition
                val intent = Intent(Intent(Intent.ACTION_VIEW))
                Log.e("News", "${BASE_NEWS_URL + link[position]}")
                intent.data = Uri.parse(BASE_NEWS_URL + link[position])
                startActivity(itemView.context, intent, null)
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
        val v = LayoutInflater.from(parent.context).inflate(
            layout.news_item_layout,
            parent,
            false
        )
        return ViewHolder(v)
    }

    override fun onBindViewHolder(holder: RecyclerAdapter.ViewHolder, position: Int) {
        if (withoutHTML(title[position]) != "" && withoutHTML(title[position]).length >3) {
            holder.itemTitle.text = withoutHTML(title[position])
            holder.itemTitle.visibility = View.VISIBLE
        } else {
            holder.itemTitle.visibility = View.INVISIBLE
        }

        if (withoutHTML(introtext[position]) != "" && withoutHTML(introtext[position]).length >3) {
            holder.itemDetail.text = withoutHTML(introtext[position])
            holder.itemDetail.visibility = View.VISIBLE
        } else {
            holder.itemDetail.text = ""
        }

        if (withoutHTML(category[position]) != "" && withoutHTML(category[position]).length >3) {
            holder.itemCategory.text = withoutHTML(category[position])
            holder.itemCategory.visibility = View.VISIBLE
        } else {
            holder.itemCategory.visibility = View.INVISIBLE
        }

        if (withoutHTML(created[position]) != "" && withoutHTML(created[position]).length >3) {
            Log.e("DataFormatter", withoutHTML(created[position]))
            Log.e("DataFormatter", formatDate(withoutHTML(created[position])))
            holder.itemCreated.text = formatDate(withoutHTML(created[position]).trim())
            holder.itemCreated.visibility = View.VISIBLE
        } else {
            holder.itemCreated.visibility = View.INVISIBLE
        }

        if (imageMedium[position] != "") {
            Log.e("NewsImage", "image not found")
            Glide.with(holder.itemImage)
                .load(BASE_NEWS_URL + imageMedium[position])
                .into(holder.itemImage)
        } else {
            holder.itemImage.setImageResource(R.drawable.new_placeholder)
        }

    }

    override fun getItemCount(): Int {
        return title.size
    }

}