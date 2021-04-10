package ua.pp.trushkovsky.MyKTGG.pages

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.dialog_page_item.view.*
import kotlinx.android.synthetic.main.dialog_pages.view.*
import ua.pp.trushkovsky.MyKTGG.R

class ViewPagerAdapter : RecyclerView.Adapter<PagerVH>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PagerVH =
        PagerVH(LayoutInflater.from(parent.context).inflate(R.layout.dialog_page_item, parent, false))

    //get the size of color array
    override fun getItemCount(): Int = 3

    //binding the screen with view
    override fun onBindViewHolder(holder: PagerVH, position: Int) = holder.itemView.run {
        if(position == 0){
            tvTitle.text = "РОЗКЛАД"
            tvAbout.text = "Актуальний розклад та заміни прямо у вас в кишені."
            ivImage.setImageResource(R.drawable.first_page)
        }
        if(position == 1) {
            tvTitle.text = "НОВИНИ"
            tvAbout.text = "Будь в курсі останніх подій твого навчального закладу."
            ivImage.setImageResource(R.drawable.second_page)

        }
        if(position == 2) {
            tvTitle.text = "МІЙ КТГГ"
            tvAbout.text = "І ще купа корисних функцій про які ти дізнаєшся зовсім скоро."
            ivImage.setImageResource(R.drawable.third_page)
        }
    }
}

class PagerVH(itemView: View) : RecyclerView.ViewHolder(itemView)