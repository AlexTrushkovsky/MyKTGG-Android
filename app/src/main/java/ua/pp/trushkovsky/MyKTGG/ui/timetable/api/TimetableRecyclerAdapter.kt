package ua.pp.trushkovsky.MyKTGG.ui.timetable.api

import android.content.res.ColorStateList
import android.graphics.Color
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.ColorRes
import androidx.cardview.widget.CardView
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.fragment_timetable.view.*
import ua.pp.trushkovsky.MyKTGG.R
import ua.pp.trushkovsky.MyKTGG.R.id
import ua.pp.trushkovsky.MyKTGG.R.layout
import java.util.*
import kotlin.math.ceil

class TimetableRecyclerAdapter(

    private var isStudent: Boolean,
    private var subGroup: Int,

    var date: MutableList<String>,
    var lessonTime: MutableList<String>,
    var lessonDescription: MutableList<String>

): RecyclerView.Adapter<TimetableRecyclerAdapter.ViewHolder>() {

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val itemLesson: TextView = itemView.findViewById(id.item_lessonName)
        val itemCabNum: TextView = itemView.findViewById(id.item_cabNum)
        val cabImage: ImageView = itemView.findViewById(id.item_mapImageView)
        val teacherImage: ImageView = itemView.findViewById(id.item_teacherImageView)
        val itemTeacherView: TextView = itemView.findViewById(id.item_teacherView)
        val itemLessonStart: TextView = itemView.findViewById(id.item_lessonStartView)
        val itemLessonEnd: TextView = itemView.findViewById(id.item_lessonEndView)
        val lessonView: CardView = itemView.findViewById(id.lesson_view)
        val timeView: CardView = itemView.findViewById(id.time_view)
    }

    override fun getItemCount(): Int {
        return lessonDescription.size
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TimetableRecyclerAdapter.ViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(layout.timetable_item_layout, parent, false)
        return ViewHolder(v)
    }

    fun formatCellToChange(holder: TimetableRecyclerAdapter.ViewHolder) {
        holder.lessonView.setCardBackgroundColor(Color.parseColor("#C54D4D"))
}

    fun formatCellToLesson(holder: TimetableRecyclerAdapter.ViewHolder) {
        holder.lessonView.setCardBackgroundColor(Color.parseColor("#4DC591"))
    }

    override fun onBindViewHolder(holder: TimetableRecyclerAdapter.ViewHolder, position: Int) {
        if (position <= itemCount) {
            configureTime(holder, position)
            configureLesson(holder, position)
        }
    }

    fun configureTime(holder: TimetableRecyclerAdapter.ViewHolder, position: Int) {
        if (lessonTime[position] != null) {
            val firstPart = lessonTime[position].substringBefore("-")
            val secondPart = lessonTime[position].substringAfter("-")
            holder.itemLessonStart.text = firstPart
            holder.itemLessonEnd.text = secondPart
        }
    }

    fun configureLesson(holder: TimetableRecyclerAdapter.ViewHolder, position: Int) {
        formatCellToLesson(holder)
        var lesson = lessonDescription[position]
        if (isStudent) {
            configAsStudent(lesson, holder)
        } else {
            configAsTeacher(lesson, holder)
        }
    }

    fun configAsStudent(lesson: String, holder: TimetableRecyclerAdapter.ViewHolder) {
        isStudentBig(lesson, holder)
    }

    fun configAsTeacher(lesson: String, holder: TimetableRecyclerAdapter.ViewHolder) {
        isTeacherRoomChange(lesson, holder)
    }

    fun isStudentBig(lesson: String, holder: TimetableRecyclerAdapter.ViewHolder) {
        //getting subgroup if bigchange and giving new lessonDesk
        if (lesson.contains("(підгр. 1)") && lesson.contains("(підгр. 2)") && lesson.contains("<br> <div class='link'> </div> <br>")) {
            val firstPart = lesson.split("<br> <div class='link'> </div> <br>")[0]
            val lastPart = lesson.split("<br> <div class='link'> </div> <br>")[1]
            var lessonForSubgroup: String? = null
            if (firstPart.contains("(підгр. ${subGroup+1}")) {
                lessonForSubgroup = "$firstPart<br>"
            } else {
                lessonForSubgroup = lastPart
            }
            if (lessonForSubgroup != null) {
                val lesson = lessonForSubgroup.replace("<div class='link'> </div>", "").replace("<div class='link'></div>", "").trim()
                Log.i("timetable", "${holder.itemLessonStart.text} - got subgroup lesson")
                isStudentRoomChange(lesson, holder)
                return
            }
        }
        val formattedLesson = lesson.replace("<div class='link'> </div>", "").trim()
        isStudentRoomChange(formattedLesson, holder)
    }

    var room: String? = null
    fun isStudentRoomChange(lesson: String, holder: TimetableRecyclerAdapter.ViewHolder) {
        //if yes, remember new room
        if (lesson.contains("Увага! Заняття перенесено у іншу аудиторію")) {
            val lessonStr = lesson.replace("Увага! Заняття перенесено у іншу аудиторію", "")
            val room = lessonStr.split("!")[0].trim()
            this.room = room
            formatCellToChange(holder)
            Log.i("timetable","${holder.itemLessonStart.text} - room change found")
            isStudentChangeWithLessonNameWithCab(lessonStr.replace("$room!", "").trim(), holder)
            return
        }
        isStudentChangeWithLessonNameWithCab(lesson, holder)
    }

    fun isStudentChangeWithLessonNameWithCab(lesson: String, holder: TimetableRecyclerAdapter.ViewHolder) {
        //if yes result, write and return
        if (lesson.contains("Увага! Заміна!") && lesson.contains("замість:") && lesson.split("<br>").count() == 3) {
            var lessonStr = lesson.replace("Увага! Заміна!", "").trim()
            val teacher = "${lessonStr.split(".")[0]}.${lessonStr.split(".")[1]}."
            lessonStr = lessonStr.replace(teacher, "").trim()
            val lessonName = lessonStr.split("замість:")[0].trim()
            if (lessonName != "") {
                lessonStr = lessonStr.replace(lessonName, "").trim()
                lessonStr = lessonStr.replace("замість:", "").trim()
                var room = lessonStr.split("<br>")[0]
                formatCellToChange(holder)
                holder.itemLesson.text = lessonName
                holder.itemTeacherView.text = teacher
                if (this.room != null) {
                    room = this.room!!
                    this.room = null
                }
                holder.itemCabNum.text = room
                Log.i("timetable", "${holder.itemLessonStart.text}) - change with lesson name with cab found")
                return
            }
        }
        isStudentChangeWithLessonNameWoCab(lesson, holder)
    }

    fun isStudentChangeWithLessonNameWoCab(lesson: String, holder: TimetableRecyclerAdapter.ViewHolder) {
        //if yes result, write and return
        if (lesson.contains("Увага! Заміна!") && lesson.contains("замість:") && lesson.split("<br>").count() == 2) {
            var lessonStr = lesson.replace("Увага! Заміна!", "").trim()
            val teacher = "${lessonStr.split(".")[0]}.${lessonStr.split(".")[1]}."
            lessonStr = lessonStr.replace(teacher, "").trim()
            val lessonName = lessonStr.split("замість:")[0].trim()
            if (lessonName != "") {
                formatCellToChange(holder)
                holder.itemLesson.text = lessonName
                holder.itemTeacherView.text = teacher
                if (this.room != null) {
                    holder.itemLesson.text = this.room!!
                    this.room = null
                } else {
                    // check if cell getting vissible on the next time
                    holder.cabImage.isVisible = false
                    holder.itemCabNum.isVisible = false
                }
                Log.i("tiemtable", "${holder.itemLessonStart.text} - change with lesson name and without cab found")
                return
            }
        }
        isStudentChangeWithoutLessonNameWithCab(lesson, holder)
    }

    fun isStudentChangeWithoutLessonNameWithCab(lesson: String, holder: TimetableRecyclerAdapter.ViewHolder) {
        //if yes result, write and return
        if (lesson.contains("Увага! Заміна!") && lesson.contains("замість:") && lesson.split("<br>").count() == 3) {
            var lessonStr = lesson.replace("Увага! Заміна!", "").trim()
            val teacher = "${lessonStr.split(".")[0]}.${lessonStr.split(".")[1]}."
            lessonStr = lessonStr.replace(teacher, "").trim()
            val lessonName = lessonStr.split("замість:")[0].trim()
            if (lessonName == "") {
                formatCellToChange(holder)
                lessonStr = lessonStr.replace("замість:", "")
                val oldTeacher = "${lessonStr.split(".")[0]}.${lessonStr.split(".")[1]}."
                lessonStr = lessonStr.replace(oldTeacher, "").trim()
                val room = lessonStr.split("<br>")[0].trim()
                val lesson = lessonStr.split("<br>")[1].replace("<br>", "").trim()
                holder.itemLesson.text = lesson
                holder.itemTeacherView.text = teacher
                holder.itemCabNum.text = room
                if (this.room != null) {
                    holder.itemCabNum.text = room
                    this.room = null
                }
                Log.i("timetable", "${holder.itemLessonStart.text} - change wo lesson name with cab found")
                return
            }
        }
        isStudentChangeWithoutLessonNameWoCab(lesson, holder)
    }

    fun isStudentChangeWithoutLessonNameWoCab(lesson: String, holder: TimetableRecyclerAdapter.ViewHolder) {
        //if yes result, write and return
        if (lesson.contains("Увага! Заміна!") && lesson.contains("замість:") && lesson.split("<br>").count() == 2) {
            var lessonStr = lesson.replace( "Увага! Заміна!", "").trim()
            val teacher = "${lessonStr.split(".")[0]}.${lessonStr.split(".")[1]}."
            lessonStr = lessonStr.replace(teacher, "").trim()
            val lessonName = lessonStr.split("замість:")[0].trim()
            if (lessonName == "") {
                formatCellToChange(holder)
                lessonStr = lessonStr.replace("замість:", "")
                val oldTeacher = "${lessonStr.split(".")[0]}.${lessonStr.split(".")[1]}."
                lessonStr = lessonStr.replace(oldTeacher, "").trim()
                val room = lessonStr.split("<br>")[0].trim()
                val lesson = lessonStr.split("<br>")[1].replace("<br>", "").trim()
                holder.itemLesson.text = lesson
                holder.itemTeacherView.text = teacher
                holder.itemCabNum.text = room
                if (this.room != null) {
                    holder.itemCabNum.text = this.room!!
                    this.room = null
                } else {
                    holder.itemCabNum.isVisible = false
                    holder.cabImage.isVisible = false
                }
                Log.i("timetable","${holder.itemLessonStart.text} - change wo lesson name wo cab found")
                return
            }
        }
        isStudentChangeCanceled(lesson, holder)
    }

    fun isStudentChangeCanceled(lesson: String, holder: TimetableRecyclerAdapter.ViewHolder) {
        //if yes result, write and return
        if (lesson.contains("Увага! Заняття відмінено!")) {
            formatCellToChange(holder)
            holder.itemLesson.text = "Заняття відмінено"
            holder.itemCabNum.isVisible = false
            holder.itemTeacherView.isVisible = false
            holder.cabImage.isVisible = false
            holder.teacherImage.isVisible = false
            Log.i("timetable", "${holder.itemLessonStart.text} - change canceled found")
            return
        }
        isStudentSubgroupLessonWithCab(lesson, holder)
    }

    fun isStudentSubgroupLessonWithCab(lesson: String, holder: TimetableRecyclerAdapter.ViewHolder) {
        //if yes result, write and return
        if (lesson.contains("(підгр.") && lesson.split("<br>").count() == 4) {
            var room = lesson.split("<br>")[0]
            var lessonStr = lesson.replace("$room<br>", "").trim()
            var teacher = "${lessonStr.split(".")[0]}.${lessonStr.split(".")[1]}."
            lessonStr = lessonStr.replace(teacher, "")
            teacher = teacher.replace("(підгр. 1)", "").replace("(підгр. 2)", "").trim()
            val lessonName = lessonStr.split("<br>")[1].trim().replace( "<br>", "")
            if (this.room != null) {
                room = this.room!!
                this.room = null
            }
            holder.itemLesson.text = lessonName
            holder.itemCabNum.text = room
            holder.itemTeacherView.text = teacher
            Log.i("timetable", "${holder.itemLessonStart.text} - subgroup lesson with cab found")
            return
        }
        isStudentSubgroupLessonWoCab(lesson, holder)
    }

    fun isStudentSubgroupLessonWoCab(lesson: String, holder: TimetableRecyclerAdapter.ViewHolder) {
        //if yes result, write and return
        if (lesson.contains("(підгр.") && lesson.split("<br>").count() == 3) {
            var teacher = "${lesson.split(".")[0]}.${lesson.split(".")[1]}."
            val lessonStr = lesson.replace(teacher, "")
            teacher = teacher.replace("(підгр. 1)", "").replace("(підгр. 2)", "").trim()
            val lessonName = lessonStr.split("<br>")[1].trim().replace("<br>", "")
            if (this.room != null) {
                holder.itemLesson.text = this.room!!
                this.room = null
            } else {
                holder.itemCabNum.isVisible = false
                holder.cabImage.isVisible = false
            }
            holder.itemLesson.text = lessonName
            holder.itemTeacherView.text = teacher
            Log.i("timetable","${holder.itemLessonStart.text} - subgroup lesson without cab found")
            return
        }
        isStudentDefaultLessonWithCab(lesson, holder)
    }

    fun isStudentDefaultLessonWithCab(lesson: String, holder: TimetableRecyclerAdapter.ViewHolder) {
        //if yes result, write and return
        if (lesson.split("<br>").count() == 3) {
            var room = lesson.split("<br>")[0]
            var lessonStr = lesson.replace("$room<br>", "").trim()
            val teacher = "${lessonStr.split( ".")[0]}.${lessonStr.split(".")[1]}."
            lessonStr = lessonStr.replace(teacher, "")
            val lessonName = lessonStr.replace("<br>", "").trim()
            if (this.room != null) {
                room = this.room!!
                this.room = null
            }
            holder.itemLesson.text = lessonName
            holder.itemTeacherView.text = teacher
            holder.itemCabNum.text = room
            Log.i("timetable","${holder.itemLessonStart.text} - default lesson with cab found")
            return
        }
        isStudentDefaultLessonWoCab(lesson, holder)
    }

    fun isStudentDefaultLessonWoCab(lesson: String, holder: TimetableRecyclerAdapter.ViewHolder) {
        //if yes result, write and return
        if (lesson.split("<br>").count() == 2) {
            val teacher = "${lesson.split(".")[0]}.${lesson.split(".")[1]}."
            val lessonStr = lesson.replace(teacher, "")
            val lessonName = lessonStr.replace("<br>", "").trim()
            var room: String? = null
            if (this.room != null) {
                room = this.room!!
                this.room = null
            }
            holder.itemLesson.text = lessonName
            if (room != null) {
                holder.itemCabNum.text = room
            } else {
                holder.cabImage.isVisible = false
                holder.itemCabNum.isVisible = false
            }
            holder.itemTeacherView.text = teacher
            Log.i("timetable", "${holder.itemLessonStart.text} - default lesson wo cab found")
            return
        }
        putEverythingInline(lesson, holder)
    }

    fun putEverythingInline(lesson: String, holder: TimetableRecyclerAdapter.ViewHolder) {
        //put inline deleting occurances of tags
        Log.i("timetable", "${holder.itemLessonStart.text} - occurances not found")
        val lessonName = lesson.replace("<br>", "").trim()
        holder.itemLesson.text = lessonName
        holder.itemCabNum.isVisible = false
        holder.itemTeacherView.isVisible = false
        holder.cabImage.isVisible = false
        holder.teacherImage.isVisible = false
    }

    //MARK: Teacher declaration

    fun isTeacherRoomChange(lesson: String, holder: TimetableRecyclerAdapter.ViewHolder) {
        //if yes, remember new room
        if (lesson.contains("Увага! Заняття перенесено у іншу аудиторію")) {
            val lessonStr = lesson.replace("Увага! Заняття перенесено у іншу аудиторію", "")
            val room = lessonStr.split("!")[0].trim()
            this.room = room
            formatCellToChange(holder)
            Log.i("timetable","${holder.itemLessonStart.text} - teacher room change found")
            isTeacherChanged(lessonStr.replace("$room!", "").trim(), holder)
            return
        }
        isTeacherChanged(lesson, holder)
    }

    fun isTeacherChanged(lesson: String, holder: TimetableRecyclerAdapter.ViewHolder) {
        //if yes result, write and return
        if (lesson.contains("Увага! Заміна! Заняття проведе інший викладач:") && lesson.split("<br>").count() == 3) {
            var lessonStr = lesson.replace("Увага! Заміна! Заняття проведе інший викладач:", "")
            val teacher = "${lessonStr.split(".")[0]}.${lessonStr.split(".")[1]}."
            lessonStr = lessonStr.replace(teacher, "").trim()
            val group = lessonStr.split("<br>")[0]
            lessonStr = lessonStr.replace("$group<br>", "").trim()
            val lessonName = lessonStr.split("<br>")[0]
            var room: String? = null
            if (this.room != null) {
                room = this.room!!
                this.room = null
            }
            holder.itemLesson.text = lessonName
            holder.itemTeacherView.text = group
            if (room != null) {
                holder.itemCabNum.text = room
            } else {
                holder.cabImage.isVisible = false
                holder.itemCabNum.isVisible = false
            }
            Log.i("timetable","${holder.itemLessonStart.text} - teacher changed found")
            return
        }
        isTeacherChanges(lesson, holder)
    }

    fun isTeacherChanges(lesson: String, holder: TimetableRecyclerAdapter.ViewHolder) {
        //if yes result, write and return
        if (lesson.contains("Увага! Цей викладач на заміні! Замість викладача") && lesson.split("<br>").count() == 3) {
            val oldTeacher = "${lesson.split(".")[0]}.${lesson.split(".")[1]}."
            var lessonStr = lesson.replace(oldTeacher, "").trim()
            val group = lessonStr.split("<br>")[0].trim()
            lessonStr = lessonStr.replace("$group<br>", "")
            val lessonName = lessonStr.split("<br>")[0].trim()
            var room: String? = null
            if (this.room != null) {
                room = this.room!!
                this.room = null
            }
            holder.itemLesson.text = lessonName
            holder.itemTeacherView.text = group
            if (room != null) {
                holder.itemCabNum.text = room
            } else {
                holder.itemCabNum.isVisible = false
                holder.cabImage.isVisible = false
            }
            Log.i("timetable","${holder.itemLessonStart.text} - teacher changes found")
            return
        }
        isTeacherDefaultLessonWithCab(lesson, holder)
    }

    fun isTeacherDefaultLessonWithCab(lesson: String, holder: TimetableRecyclerAdapter.ViewHolder) {
        //if yes result, write and return
        if (lesson.split("<br>").count() == 4) {
            var room = lesson.split("<br>")[0].trim()
            var lessonStr = lesson.replace("$room<br>", "")
            val group = lessonStr.split("<br>")[0].trim()
            lessonStr = lessonStr.replace("$group <br>", "").replace("$group<br>", "")
            val lessonName = lessonStr.split("<br>")[0].trim()
            if (this.room != null) {
                room = this.room!!
                this.room = null
            }
            holder.itemLesson.text = lessonName
            holder.itemCabNum.text = room
            holder.itemTeacherView.text = group
            Log.i("timetable","${holder.itemLessonStart.text} - teacher lesson with cab found")
            return
        }
        isTeacherDefaultLessonWoCab(lesson, holder)
    }

    fun isTeacherDefaultLessonWoCab(lesson: String, holder: TimetableRecyclerAdapter.ViewHolder) {
        //if yes result, write and return
        if (lesson.split("<br>").count() == 3) {
            val group = lesson.split("<br>")[0].trim()
            val lessonStr = lesson.replace("$group <br>", "").replace("$group<br>", "")
            val lessonName = lessonStr.split("<br>")[0].trim()
            var room: String? = null
            if (this.room != null) {
                room = this.room!!
                this.room = null
            }
            holder.itemLesson.text = lessonName
            holder.itemTeacherView.text = group
            if (room != null) {
                holder.itemCabNum.text = room
            } else {
                holder.cabImage.isVisible = false
                holder.itemCabNum.isVisible = false
            }
            Log.i("timetable","${holder.itemLessonStart.text} - teacher lesson wo cab found")
            return
        }
        putEverythingInline(lesson, holder)
    }

}