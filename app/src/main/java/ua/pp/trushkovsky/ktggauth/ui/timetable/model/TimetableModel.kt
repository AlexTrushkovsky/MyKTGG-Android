package ua.pp.trushkovsky.ktggauth.ui.timetable.model

data class TimetableApiJSON (
    val timetable: Timetable
)

data class Timetable (
    val firstsubgroup: Subgroup,
    val secondsubgroup: Subgroup
)

data class Subgroup (
    val firstweek: Week,
    val secondweek: Week
)

data class Week (
    val mon: List<Fri>,
    val tue: List<Fri>,
    val wed: List<Fri>,
    val thu: List<Fri>,
    val fri: List<Fri>,
    val sun: List<Fri>,
    val sat: List<Fri>
)

data class Fri (
    val lessonNum: String,
    val lesson: String,
    val teacher: String,
    val room: String
)