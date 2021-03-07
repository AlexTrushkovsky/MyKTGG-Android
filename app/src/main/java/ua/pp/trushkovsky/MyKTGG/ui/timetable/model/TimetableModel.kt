package ua.pp.trushkovsky.MyKTGG.ui.timetable.model

data class TimetableRoot (
    val item: Array<Item>?
)

data class Item (
    val group: String?,
    val date: String?,
    val comment: String?,
    val lesson_number: String?,
    val lesson_name: String?,
    val lesson_time: String?,
    val lesson_description: String?
)