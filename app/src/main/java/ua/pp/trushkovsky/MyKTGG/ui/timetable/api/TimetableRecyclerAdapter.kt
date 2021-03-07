package ua.pp.trushkovsky.MyKTGG.ui.timetable.api

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import ua.pp.trushkovsky.MyKTGG.R.id
import ua.pp.trushkovsky.MyKTGG.R.layout
import ua.pp.trushkovsky.MyKTGG.ui.settings.getBoolFromSharedPreferences
import java.util.*
import kotlin.math.acos

class TimetableRecyclerAdapter(

    private var isStudent: Boolean,
    private var pickedDate: Date,
    private var subGroup: Int,

    private var group:MutableList<String>,
    private var date: MutableList<String>,
    private var comment: MutableList<String>,
    private var lessonName: MutableList<String>,
    private var lessonTime: MutableList<String>,
    private var lessonDescription: MutableList<String>,
    private var lessonNumber: MutableList<String>

): RecyclerView.Adapter<TimetableRecyclerAdapter.ViewHolder>() {

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val itemLesson: TextView = itemView.findViewById(id.item_lessonName)
        val itemCabNum: TextView = itemView.findViewById(id.item_cabNum)
        val itemTeacherView: TextView = itemView.findViewById(id.item_teacherView)
        val itemLessonStart: TextView = itemView.findViewById(id.item_lessonStartView)
        val itemLessonEnd: TextView = itemView.findViewById(id.item_lessonEndView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TimetableRecyclerAdapter.ViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(layout.timetable_item_layout, parent, false)
        return ViewHolder(v)
    }

    override fun onBindViewHolder(holder: TimetableRecyclerAdapter.ViewHolder, position: Int) {
        if (position <= itemCount) {

            if (lessonTime[position] != null) {
                val firstPart = lessonTime[position].substringBefore("-")
                val secondPart = lessonTime[position].substringAfter("-")
                holder.itemLessonStart.text = firstPart
                holder.itemLessonEnd.text = secondPart
            }

            var lesson = lessonDescription[position]
            if (isStudent) {
                if (isTZ(lesson)) {
                    //format cell to change
                    if (isBigChangeForStudent(lesson)) {
                        val firstSubgroupLesson = lesson.split("<br> <br>")[0]
                        print(firstSubgroupLesson)
                        val secondSubgroupLesson = lesson.split("<br> <br>")[1]
                        print(secondSubgroupLesson)
                        if (subGroup == 0) {
                            //работаем по первой сабгруппе
                        } else if (subGroup == 1) {
                            //работает по второй сабгруппе
                        }
                    } else {
                        //если замена маленькая
                    }

                    lesson = lesson.replace("Увага! Заміна!", "")
                    var newLesson = lesson.split("замість:")[0]
                    val oldLesson = lesson.split("замість:")[1]
                    if (oldLesson.split("<br>").count() == 3) {
                        newLesson = "${oldLesson.split("<br>")[0]}<br> $newLesson"
                    }
                    print("NL: $newLesson")
                    print("OLDL: $oldLesson")

                    if (isT3(newLesson)) {

                    } else if (isT2(newLesson)) {
                        val room = newLesson.split("<br>")[0]
                        newLesson = newLesson.replace("$room<br>", "").trim()
                        val teacher =
                            "${newLesson.split(".")[0]}.${newLesson.split(".")[1]}.".trim()
                        newLesson = newLesson.replace(teacher, "")
                        val subgroup = newLesson.split("<br>")[0].trim()
                        newLesson = newLesson.split("<br>")[1]
                        val lesson =
                            newLesson.replace("<br>", "").replace("<div class='link'> </div>", "")
                                .trim()
                        print("lesson: $lesson")
                        holder.itemLesson.text = lesson
                        print("teacher: $teacher")
                        holder.itemTeacherView.text = teacher
                        print("room: $room")
                        holder.itemCabNum.text = room
                    } else if (isT1(newLesson, isStudent)) {
                        val room = newLesson.split("<br>")[0]
                        newLesson = newLesson.replace("$room<br>", "").trim()
                        val teacher =
                            "${newLesson.split(".")[0]}.${newLesson.split(".")[1]}.".trim()
                        newLesson = newLesson.replace(teacher, "")
                        val lesson = newLesson.replace("<br>", "").trim()
                        print("lesson: $lesson")
                        holder.itemLesson.text = lesson
                        print("teacher: $teacher")
                        holder.itemTeacherView.text = teacher
                        print("room: $room")
                        holder.itemCabNum.text = room
                    } else {
                        holder.itemLesson.text =
                            newLesson.replace("<br>", "").replace("<div class='link'> </div>", "")
                                .trim()
//                    cell.roomImage.isHidden = true
//                    cell.lessonRoom.isHidden = true
//                    cell.teacher.isHidden = true
//                    cell.teacherImage.isHidden = true
                    }

                } else {
                    //format cell to lesson
                    if (isT3(lesson)) {
                        val firstPart = lesson.split("<br> <div class='link'> </div> <br>")[0]
                        val secondPart = lesson.split("<br> <div class='link'> </div> <br>")[1]
                        print("firstPart of t3: $firstPart")
                        print("secondPart of t3: $secondPart")
                        var mainPart = String()
                        val subGroupString = "(підгр. ${subGroup+1})"
                        if (firstPart.contains(subGroupString)) {
                            mainPart = firstPart
                        } else if (secondPart.contains("(підгр. ${subGroup+1})")) {
                            mainPart = secondPart
                        } else return
                        val room = mainPart.split("<br>")[0]
                        mainPart = mainPart.replace("$room<br>", "").trim()
                        val teacher = "${mainPart.split(".")[0]}.${lesson.split(".")[1]}.".trim()
                        mainPart = mainPart.replace(teacher, "")
                        val subgroup = mainPart.split("<br>")[0].trim()
                        mainPart = mainPart.split("<br>")[1]
                        val lesson = mainPart.replace("<br>", "").replace("<div class='link'> </div>", "").trim()
                        print("subgroup: $subgroup")
                        print("lesson: $lesson")
                        holder.itemLesson.text = lesson
                        print("teacher: $teacher")
                        holder.itemTeacherView.text = teacher
                        print("room: $room")
                        holder.itemCabNum.text = room
                    } else if (isT2(lesson)) {
                        val room = lesson.split(("<br>")[0]).toString()
                        lesson = lesson.replace("$room<br>", "").trim()
                        val teacher = "${lesson.split(".")[0]}.${lesson.split(".")[1]}.".trim()
                        lesson = lesson.replace(teacher, "")
                        val subgroup = lesson.split("<br>")[0].trim()
                        lesson = lesson.split("<br>")[1]
                        val lesson = lesson.replace("<br>","").replace("<div class='link'> </div>","").trim()
                        print("subgroup: $subgroup")
                        print("lesson: $lesson")
                        holder.itemLesson.text = lesson
                        print("teacher: $teacher")
                        holder.itemTeacherView.text = teacher
                        print("room: $room")
                        holder.itemCabNum.text = room
                    } else if (isT1(lesson, isStudent)) {
                        val room = lesson.split("<br>")[0]
                        lesson = lesson.replace("$room<br>", "").trim()
                        val teacher = "${lesson.split(".")[0]}.${lesson.split(".")[1]}.".trim()
                        lesson = lesson.replace(teacher,"")
                        val lesson = lesson.replace("<br>","").replace("<div class='link'> </div>","").trim()
                        print("lesson: $lesson")
                        holder.itemLesson.text = lesson
                        print("teacher: $teacher")
                        holder.itemTeacherView.text = teacher
                        print("room: $room")
                        holder.itemCabNum.text = room
                    } else {
                        holder.itemLesson.text = lesson.replace("<br>", "").replace("<div class='link'> </div>","").trim()
//                        cell.roomImage.isHidden = true
//                        cell.lessonRoom.isHidden = true
//                        cell.teacher.isHidden = true
//                        cell.teacherImage.isHidden = true
                    }
                }
            } else {
                //For teacher
                if (isTZ(lesson)) {
                    //formatCellToChange(cell: cell)
                    if (isTZnewCab(lesson)){
                        print(lesson)
                        lesson = lesson.replace("Увага! Заняття перенесено у іншу аудиторію","")
                        val room = lesson.split("!")[0]
                        lesson = lesson.replace("$room!", "")
                        holder.itemCabNum.text = room

                        if (isTZcancel(lesson)) {
                            print(lesson)
                            holder.itemLesson.text = "Заняття відмінено"
//                            cell.roomImage.isHidden = true
//                            cell.lessonRoom.isHidden = true
//                            cell.teacher.isHidden = true
//                            cell.teacherImage.isHidden = true
                        } else if (isTZforNewTeacher(lesson)) {
                            print(lesson)
                            lesson = lesson.replace("Увага! Цей викладач на заміні! Замість викладача ","").trim()
                            val teacher = "${lesson.split(".")[0]}.${lesson.split(".")[1]}.".trim()
                            lesson = lesson.replace(teacher,"").trim()
                            val group = lesson.split("<br>")[0]
                            lesson = lesson.replace("${group}<br>","").replace("<br>","").replace("<div class='link'> </div>","").trim()
                            holder.itemLesson.text = lesson
                            holder.itemTeacherView.text = teacher
                        } else if (isTZforOldTeacher(lesson)) {
                            print(lesson)
                            lesson = lesson.replace("Увага! Заміна!","")
                            val newLesson = lesson.split("замість:")[0]
                            val newTeacher = "${lesson.split(".")[0]}.${newLesson.split(".")[1]}.".trim()
                            holder.itemLesson.text = "Замість вас на заміні $newTeacher"
//                            cell.roomImage.isHidden = true
//                            cell.lessonRoom.isHidden = true
//                            cell.teacher.isHidden = true
//                            cell.teacherImage.isHidden = true
                        } else {
                            holder.itemLesson.text = lesson.replace("<br>","").replace("<div class='link'> </div>", "").trim()
//                            cell.roomImage.isHidden = true
//                            cell.lessonRoom.isHidden = true
//                            cell.teacher.isHidden = true
//                            cell.teacherImage.isHidden = true
                        }
                    } else {
                        if (isTZcancel(lesson)) {
                            print(lesson)
                            holder.itemLesson.text = "Заняття відмінено"
//                            cell.roomImage.isHidden = true
//                            cell.lessonRoom.isHidden = true
//                            cell.teacher.isHidden = true
//                            cell.teacherImage.isHidden = true
                        } else if (isTZforNewTeacher(lesson)) {
                            print(lesson)
                            if (lesson.split("<br>").count()-1 == 3) {
                                val room = lesson.split("<br>")[0]
                                lesson = lesson.replace("$room<br>","").trim()
                                lesson = lesson.replace("Увага! Цей викладач на заміні! Замість викладача ","").trim()
                                val teacher = "${lesson.split(".")[0]}.${lesson.split(".")[1]}.".trim()
                                lesson = lesson.replace(teacher,"").trim()
                                val group = lesson.split("<br>")[0]
                                lesson = lesson.replace("$group<br>","")
                                            .replace("<br>","")
                                            .replace("<div class='link'> </div>","").trim()
                                holder.itemLesson.text = lesson
                                holder.itemTeacherView.text = teacher
                                holder.itemCabNum.text = room
                            } else {
                                holder.itemLesson.text = lesson.replace("<br>","").replace("<div class='link'> </div>","").trim()
//                                cell.roomImage.isHidden = true
//                                cell.lessonRoom.isHidden = true
//                                cell.teacher.isHidden = true
//                                cell.teacherImage.isHidden = true
                            }
                        } else if (isTZforOldTeacher(lesson)) {
                            print(lesson)
                            lesson = lesson.replace("Увага! Заміна!","")
                            val newLesson = lesson.split("замість:")[0]
                            val newTeacher = "${lesson.split(".")[0]}.${newLesson.split(".")[1]}.".trim()
                            holder.itemLesson.text = "Замість вас на заміні $newTeacher"
//                            cell.roomImage.isHidden = true
//                            cell.lessonRoom.isHidden = true
//                            cell.teacher.isHidden = true
//                            cell.teacherImage.isHidden = true
                        } else {
                            holder.itemLesson.text = lesson.replace("<br>","").replace("<div class='link'> </div>","").trim()
//                            cell.roomImage.isHidden = true
//                            cell.lessonRoom.isHidden = true
//                            cell.teacher.isHidden = true
//                            cell.teacherImage.isHidden = true
                        }

                    }
                } else {
                    //formatCellToLesson(cell: cell)
                    if (isT2(lesson) || isT1(lesson, isStudent)) {
                        val room = lesson.split("<br>")[0]
                        lesson = lesson.replace("$room<br>","").trim()
                        val group = lesson.split("<br>")[0]
                        lesson = lesson.replace("$group<br>","")
                        lesson = lesson.split("<br>")[0]
                        val lesson = lesson.replace("<br>","").trim()
                        print("lesson: $lesson")
                        holder.itemLesson.text = lesson
                        print("group: $group")
                        holder.itemTeacherView.text = group
                        print("room: $room")
                        holder.itemCabNum.text = room
                    } else {
                        holder.itemLesson.text = lesson.replace("<br>","").trim()
//                        cell.roomImage.isHidden = true
//                        cell.lessonRoom.isHidden = true
//                        cell.teacher.isHidden = true
//                        cell.teacherImage.isHidden = true
                    }
                }
            }
        }
    }

    fun isT1(lesson: String, isStudent: Boolean): Boolean {
        return if (isStudent) {
            if (lesson.split("<br>").count()-1 == 2 && !lesson.contains("підгр.")) {
                print("t1 student")
                true
            } else {
                false
            }
        } else {
            if (lesson.split("<br>").count()-1 == 3 && !lesson.contains("підгр.")) {
                print("t1 teacher")
                true
            } else {
                false
            }
        }
    }

    fun isT2(lesson: String): Boolean {
        //identical for teacher
        if (lesson.split("<br>").count()-1 == 3 && lesson.split("підгр.").count()-1 == 1) {
            print("t2")
            return true
        } else {
            return false
        }
    }

    fun isT3(lesson: String): Boolean {
        if (lesson.split("<br>").count()-1 == 7 && lesson.split("підгр.").count()-1 == 2) {
            print("t3")
            return true
        } else {
            return false
        }
    }

    fun isTZ(lesson: String): Boolean {
        if (lesson.contains("Увага!", false)) {
            return true
        } else {
            return false
        }
    }

    fun isTZcancel(lesson: String): Boolean {
        return if (lesson.contains("Заняття відмінено!", false)) {
            print("tz cancel")
            true
        } else {
            false
        }
    }

    fun isTZnewCab(lesson: String): Boolean {
        return if (lesson.split("Заняття перенесено у іншу аудиторію").count()-1 == 1 && (lesson.split("!").count()-1) >= 2) {
            print("tz new cab")
            true
        } else {
            false
        }
    }

    fun isTZforNewTeacher(lesson: String): Boolean {
        if (lesson.split("Цей викладач на заміні!").count()-1 == 1) {
            print("tz for new teacher")
            return true
        } else {
            return false
        }
    }

    fun isTZforOldTeacher(lesson: String): Boolean {
        if (lesson.contains("Увага! Заміна!", false)) {
            print("tz for old teacher")
            return true
        } else {
            return false
        }
    }

    fun isTZforStudent(lesson: String): Boolean {
        return if (lesson.contains("Увага! Заміна!", false)) {
            print("tz for student")
            true
        } else {
            false
        }
    }

    fun isBigChangeForStudent(lesson: String): Boolean{
        return if (lesson.contains("<br> <br>", false)) {
            print("big change ofr student")
            true
        } else {
            false
        }
    }

    override fun getItemCount(): Int {
        return lessonDescription.size
    }
}