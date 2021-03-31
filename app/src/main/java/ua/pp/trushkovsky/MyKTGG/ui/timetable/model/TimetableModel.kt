package ua.pp.trushkovsky.MyKTGG.ui.timetable.model

data class TimetableRoot (
    val item: Array<Item>?
)

data class Item (
    val date: String?,
    val lesson_time: String?,
    val lesson_description: String?
)