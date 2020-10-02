package ua.pp.trushkovsky.ktggauth.ui.timetable.api

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import ua.pp.trushkovsky.ktggauth.R.id
import ua.pp.trushkovsky.ktggauth.R.layout

class TimetableRecyclerAdapter(
    private var dis: List<String>,
    private var cab: List<String>,
    private var para: List<String>,
    private var teacher: List<String>
): RecyclerView.Adapter<TimetableRecyclerAdapter.ViewHolder>() {

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val itemLesson: TextView = itemView.findViewById(id.item_lessonName)
        val itemCabNum: TextView = itemView.findViewById(id.item_cabNum)
        val itemTeacherView: TextView = itemView.findViewById(id.item_teacherView)
        val itemLessonStart: TextView = itemView.findViewById(id.item_lessonStartView)
        val itemLessonEnd: TextView = itemView.findViewById(id.item_lessonEndView)

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TimetableRecyclerAdapter.ViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(
            layout.timetable_item_layout,
            parent,
            false
        )
        return ViewHolder(v)
    }

    override fun onBindViewHolder(holder: TimetableRecyclerAdapter.ViewHolder, position: Int) {
        if (dis[position] != "") {
            Log.e("Timetable recycler", "${dis[position]}")
            holder.itemLesson.text = dis[position]
        } else {
            holder.itemLesson.visibility = View.INVISIBLE
        }

        if (cab[position] != "") {
            Log.e("Timetable recycler", "${cab[position]}")
            holder.itemCabNum.text = cab[position]
        } else {
            holder.itemCabNum.visibility = View.INVISIBLE
        }

        if (teacher[position] != "") {
            Log.e("Timetable recycler", "${teacher[position]}")
            holder.itemTeacherView.text = teacher[position]
        } else {
            holder.itemTeacherView.visibility = View.INVISIBLE
        }

        Log.e("Timetable recycler", "${para[position]}")
        when (para[position]){
            "1" -> {
                holder.itemLessonStart.text = "09:00"
                holder.itemLessonEnd.text = "10:20"
            }
            "2" -> {
                holder.itemLessonStart.text = "10:30"
                holder.itemLessonEnd.text = "11:50"
            }
            "3" -> {
                holder.itemLessonStart.text = "12:20"
                holder.itemLessonEnd.text = "13:40"
            }
            "4" -> {
                holder.itemLessonStart.text = "13:50"
                holder.itemLessonEnd.text = "15:10"
            }
            "5" -> {
                holder.itemLessonStart.text = "15:20"
                holder.itemLessonEnd.text = "16:40"
            }
            "6" -> {
                holder.itemLessonStart.text = "16:50"
                holder.itemLessonEnd.text = "18:10"
            }
            "7" -> {
                holder.itemLessonStart.text = "18:20"
                holder.itemLessonEnd.text = "19:40"
            }
            "8" -> {
                holder.itemLessonStart.text = "19:50"
                holder.itemLessonEnd.text = "21:10"
            }
            else -> {
                holder.itemLessonStart.text = "00:00"
                holder.itemLessonEnd.text = "00:00"
            }
        }
    }

    override fun getItemCount(): Int {
        return dis.size
    }
}