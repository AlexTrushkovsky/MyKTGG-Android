package ua.pp.trushkovsky.MyKTGG.ui.timetable

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.ktx.Firebase
import com.google.gson.GsonBuilder
import com.michalsvec.singlerowcalendar.calendar.CalendarChangesObserver
import com.michalsvec.singlerowcalendar.calendar.CalendarViewManager
import com.michalsvec.singlerowcalendar.calendar.SingleRowCalendarAdapter
import com.michalsvec.singlerowcalendar.selection.CalendarSelectionManager
import com.michalsvec.singlerowcalendar.utils.DateUtils
import kotlinx.android.synthetic.main.calendar_item.view.*
import kotlinx.android.synthetic.main.fragment_timetable.*
import kotlinx.android.synthetic.main.fragment_timetable.view.*
import kotlinx.coroutines.*
import okhttp3.*
import ua.pp.trushkovsky.MyKTGG.R
import ua.pp.trushkovsky.MyKTGG.R.drawable.round_button_green
import ua.pp.trushkovsky.MyKTGG.R.drawable.round_button_red
import ua.pp.trushkovsky.MyKTGG.helpers.OnSwipeTouchListener
import ua.pp.trushkovsky.MyKTGG.helpers.showDialogWith
import ua.pp.trushkovsky.MyKTGG.ui.settings.*
import ua.pp.trushkovsky.MyKTGG.ui.timetable.api.TimetableRecyclerAdapter
import ua.pp.trushkovsky.MyKTGG.ui.timetable.model.TimetableRoot
import java.lang.Exception
import java.net.SocketException
import java.net.UnknownHostException
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*

class TimetableFragment: Fragment(), CoroutineScope by MainScope() {

    private var dateList= mutableListOf<String>()
    private var lessonTimeList= mutableListOf<String>()
    private var lessonDescriptionList= mutableListOf<String>()

    private val calendar = Calendar.getInstance()
    private var currentYear = 0

    private var pickedDate = Date()

    private val client = OkHttpClient()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_timetable, container, false)
    }

    override fun onDestroy() {
        super.onDestroy()
        cancel()
    }

    override fun onStart() {
        super.onStart()

        calendar.time = Date()
        currentYear = calendar[Calendar.YEAR]
        todayButton.setOnClickListener {
            pickCurrentDate()
        }
        client.dispatcher().maxRequests = 1
        timetableSwipeRefresh.setOnRefreshListener {
            launch {
                getData()
            }
        }

        bootomSheetRecycler.setOnTouchListener(object: OnSwipeTouchListener(activity) {
            override fun onSwipeRight() {
                val positions = datePickerView.getSelectedIndexes()
                if (positions.count() >= 0) {
                    val currentPosition = positions[0]
                    datePickerView.smoothScrollToPosition(currentPosition-1)
                    datePickerView.select(currentPosition-1)
                }
            }

            override fun onSwipeLeft() {
                val positions = datePickerView.getSelectedIndexes()
                if (positions.count() >= 0) {
                    val currentPosition = positions[0]
                    datePickerView.smoothScrollToPosition(currentPosition+1)
                    datePickerView.select(currentPosition+1)
                }
            }
        })

        val myCalendarViewManager = object : CalendarViewManager {
            override fun setCalendarViewResourceId(position: Int, date: Date, isSelected: Boolean): Int {
                val cal = Calendar.getInstance()
                cal.time = date
                return if (isSelected) {
                    R.layout.selected_calendar_item
                } else {
                    R.layout.calendar_item
                }
            }

            override fun bindDataToCalendarView(holder: SingleRowCalendarAdapter.CalendarViewHolder, date: Date, position: Int, isSelected: Boolean) {
                holder.itemView.tv_date_calendar_item.text = DateUtils.getDayNumber(date)
                holder.itemView.tv_day_calendar_item.text = dayOfWeekTo2LettersCapitalized(date.day)
            }
        }

        val myCalendarChangesObserver = object : CalendarChangesObserver {
            override fun whenSelectionChanged(isSelected: Boolean, position: Int, date: Date) {
                if (date.day == pickedDate.day && date.month == pickedDate.month && date.year == pickedDate.year) return

                Log.e("DatePicker", "picked new date... $date")
                pickedDate = date
                launch {
                    getData()
                }
                dayNumView.text = DateUtils.getDayNumber(date)
                weekDayView.text = dayOfWeekToString(date.day)
                monthYearView.text = "${monthNumToString(date.month)} ${DateUtils.getYear(date)}"

                val formatter: DateFormat = SimpleDateFormat("dd/MM/yyyy")
                if (formatter.parse(formatter.format(date)) == formatter.parse(formatter.format(Date()))) {
                    todayButton.setBackgroundResource(round_button_green)
                    todayButton.setTextColor(Color.parseColor("#4DC591"))
                } else {
                    todayButton.setBackgroundResource(round_button_red)
                    todayButton.setTextColor(Color.parseColor("#C54D4D"))
                }
            }
        }

        // selection manager is responsible for managing selection
        val mySelectionManager = object : CalendarSelectionManager {
            override fun canBeItemSelected(position: Int, date: Date): Boolean {
                val cal = Calendar.getInstance()
                cal.time = date
                return true
            }
        }

        datePickerView?.apply {
            calendarViewManager = myCalendarViewManager
            calendarChangesObserver = myCalendarChangesObserver
            calendarSelectionManager = mySelectionManager
            initialPositionIndex = 60
            futureDaysCount = 60
            pastDaysCount = 60
            init()
            datePickerView.select(60)
            datePickerView.scrollToPosition(60)
        }
    }

    private suspend fun getData() {
        timetableSwipeRefresh.isRefreshing = true
        var group = getStringFromSharedPreferences("group", activity)
        if (group == "") {
            val userID = Firebase.auth.currentUser?.uid
            if (userID != null) {
                FirebaseDatabase.getInstance().reference
                    .child("users")
                    .child(userID)
                    .child("public").addValueEventListener(object : ValueEventListener {
                        override fun onCancelled(error: DatabaseError) {}
                        override fun onDataChange(snapshot: DataSnapshot) {
                            if (snapshot.value == null) { return }
                            val map = snapshot.value as Map<String, Any>
                            if (map["group"] != null) {
                                group = map["group"].toString()
                                saveStringToSharedPreferences("group", group, context)
                            }
                        }
                    })
            }
        }
        var subgroup = getIntFromSharedPreferences("subgroup", activity)
        if (subgroup == -1) {
            val userID = Firebase.auth.currentUser?.uid
            if (userID != null) {
                FirebaseDatabase.getInstance().reference
                    .child("users")
                    .child(userID)
                    .child("public").addValueEventListener(object : ValueEventListener {
                        override fun onCancelled(error: DatabaseError) {}
                        override fun onDataChange(snapshot: DataSnapshot) {
                            if (snapshot.value == null) { return }
                            val map = snapshot.value as Map<String, Any>
                            if (map["subgroup"] != null) {
                                subgroup = map["subgroup"].toString().toInt()
                                saveIntToSharedPreferences("subgroup", subgroup, context)
                            }
                        }
                    })
            }
        }
        val isStudent = getBoolFromSharedPreferences("isStudent", activity)

        val animation = AnimationUtils.loadAnimation(context, R.anim.slide_up)
        animation.repeatCount = 100
        datePickerView?.view?.startAnimation(animation)
        client.dispatcher().cancelAll()
        val answer = fetchData(pickedDate, group, isStudent)
        val e = answer.second
        val string = answer.first
        if (e == null) {
            if (string != null) {
                val gson = GsonBuilder().create() ?: return
                val timetableRoot = gson.fromJson(string, TimetableRoot::class.java)
                val item = timetableRoot.item ?: return
                deinitModel()
                for (timetable in item) {
                    if (timetable.lesson_description != null &&
                        timetable.lesson_description != "" &&
                        timetable.lesson_time != null &&
                        timetable.date != null) {
                        addToList(timetable.date, timetable.lesson_time, timetable.lesson_description, isStudent, subgroup)
                    }
                }
                noLessonImage?.isVisible = lessonDescriptionList.isEmpty()
                if (bootomSheetRecycler?.adapter != null) {
                    bootomSheetRecycler?.adapter?.notifyItemRangeChanged(0, lessonDescriptionList.size)
                } else {
                    setUpRecyclerView()
                }
                Log.e("timetable", "refreshing timetable")
                timetableSwipeRefresh?.isRefreshing = false
            }
        } else {
            activity?.runOnUiThread {
                showDialogWith("Відсутнє з'єдняння з мережею", "$e, пeперевірте з'єднання, або спробуйте будь ласка пізніше", context, timetableSwipeRefresh)
            }
            timetableSwipeRefresh?.isRefreshing = false
        }
    }

    private fun pickCurrentDate() {
        datePickerView.select(60)
        datePickerView.smoothScrollToPosition(60)
    }

    private fun dayOfWeekToString(dayOfWeek: Int): String {
        when(dayOfWeek) {
            1 -> return "Понеділок"
            2 -> return "Вівторок"
            3 -> return "Середа"
            4 -> return "Четвер"
            5 -> return "П'ятниця"
            6 -> return "Субота"
            0 -> return "Неділя"
            else -> return ""
        }
    }

    private fun dayOfWeekTo2LettersCapitalized(dayOfWeek: Int): String {
        when(dayOfWeek) {
            1 -> return "ПН"
            2 -> return "ВТ"
            3 -> return "СР"
            4 -> return "ЧТ"
            5 -> return "ПТ"
            6 -> return "СБ"
            0 -> return "НД"
            else -> return ""
        }
    }

    private fun monthNumToString(monthNum: Int): String {
        when(monthNum) {

            0 -> return "Січ."
            1 -> return "Лют."
            2 -> return "Бер."
            3 -> return "Квіт."
            4 -> return "Трав."
            5 -> return "Черв."
            6 -> return "Лип."
            7 -> return "Серп."
            8 -> return "Вер."
            9 -> return "Жовт."
            10 -> return "Лист."
            11 -> return "Груд."
            else -> return ""
        }
    }

    private fun setUpRecyclerView() {
        val subgroup = getIntFromSharedPreferences("subgroup", activity)
        val isStudent = getBoolFromSharedPreferences("isStudent", activity)
        bootomSheetRecycler?.layoutManager = LinearLayoutManager(requireActivity().application.applicationContext)
        bootomSheetRecycler?.adapter = TimetableRecyclerAdapter(isStudent, subgroup, dateList, lessonTimeList, lessonDescriptionList)
    }

    private fun addToList(
        date: String,
        lessonTime: String,
        lessonDescription: String,
        isStudent: Boolean,
        subGroup: Int
    ) {
        if (isStudent) {
            if (subGroup == 0) {
                    if (lessonDescription.contains("(підгр. 2)")) {
                        if (!lessonDescription.contains("(підгр. 1)")) {
                            return
                        }
                    }
            } else if (subGroup == 1) {
                    if (lessonDescription.contains("(підгр. 1)")) {
                        if (!lessonDescription.contains("(підгр. 2)")) {
                            return
                        }
                    }
            }
        }

        dateList.add(date)
        lessonTimeList.add(lessonTime)
        lessonDescriptionList.add(lessonDescription)
    }

    private fun deinitModel() {
        val size = lessonDescriptionList.size
        dateList.clear()
        lessonTimeList.clear()
        lessonDescriptionList.clear()
        bootomSheetRecycler?.removeAllViews()
        bootomSheetRecycler?.adapter?.notifyItemRangeRemoved(0, size)
    }

    private fun formatJson(str: String): String {
        val st = ", \"item\":"
        val srt = "}}"
        var result = str.replace(st, ",")
        result = result.replace("\"item\": {", "\"item\": [{")
        result = result.replace(srt, "}]}")
        result = result.replace("(Сем)", "")
        result = result.replace("(Л)", "")
        result = result.replace("(ПрЛ)", "")
        return result
    }

    private suspend fun fetchData(pickedDate: Date, group: String, isStudent: Boolean): Pair<String?, java.lang.Exception?> {
        return withContext(Dispatchers.Default) {
            val mode = if (isStudent) { "group" } else { "teacher" }
            val simpleDateFormat = SimpleDateFormat("dd.MM.yyyy")
            var encodedGroup = java.net.URLEncoder.encode(group, "windows-1251")
            encodedGroup = encodedGroup.replace(".", "%2E").replace("+", "%20").replace("-", "%2D")
            val stringPickedDate = simpleDateFormat.format(pickedDate)
            val url = "http://app.ktgg.kiev.ua/cgi-bin/timetable_export.cgi?req_type=rozklad&req_mode=$mode&req_format=json&begin_date=$stringPickedDate&end_date=$stringPickedDate&bs=ok&OBJ_name=$encodedGroup"
//            val url = "http://192.168.5.230/cgi-bin/timetable_export.cgi?req_type=rozklad&req_mode=$mode&req_format=json&begin_date=$stringPickedDate&end_date=$stringPickedDate&bs=ok&OBJ_name=$encodedGroup"
            Log.e("Timetable", "Trying to get data from $url")
            val request = Request.Builder().url(url).build()
            try {
                val response = client.newCall(request).execute()
                    val body = response.body() ?: return@withContext Pair(null, null)
                    return@withContext Pair(formatJson(body.string()), null)
            } catch (e: UnknownHostException) {
                Log.e("timetable", "$e")
                return@withContext Pair(null, e)
            } catch (e: Exception) {
                return@withContext Pair(null, null)
            }
        }
    }
}