package ua.pp.trushkovsky.ktggauth.ui.timetable

import android.annotation.SuppressLint
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.os.CountDownTimer
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.michalsvec.singlerowcalendar.calendar.CalendarChangesObserver
import com.michalsvec.singlerowcalendar.calendar.CalendarViewManager
import com.michalsvec.singlerowcalendar.calendar.SingleRowCalendarAdapter
import com.michalsvec.singlerowcalendar.selection.CalendarSelectionManager
import com.michalsvec.singlerowcalendar.utils.DateUtils
import kotlinx.android.synthetic.main.calendar_item.view.*
import kotlinx.android.synthetic.main.fragment_timetable.*
import kotlinx.android.synthetic.main.fragment_timetable.rv_recyclerView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import ua.pp.trushkovsky.ktggauth.R
import ua.pp.trushkovsky.ktggauth.R.drawable.*
import ua.pp.trushkovsky.ktggauth.ui.timetable.api.TimetableAPIRequest
import ua.pp.trushkovsky.ktggauth.ui.timetable.api.TimetableRecyclerAdapter
import ua.pp.trushkovsky.ktggauth.ui.timetable.model.Fri
import ua.pp.trushkovsky.ktggauth.ui.timetable.model.TimetableApiJSON
import java.util.*

const val BASE_TIMETABLE_URL = "http://217.76.201.219:5000"

@Suppress("DEPRECATION")
class TimetableFragment: Fragment(){

    lateinit var countDownTimer: CountDownTimer

    private var disList= mutableListOf<String>()
    private var cabList= mutableListOf<String>()
    private var paraList= mutableListOf<String>()
    private var teacherList= mutableListOf<String>()

    private val calendar = Calendar.getInstance()
    private var currentYear = 0

    private var pickedDate = Date()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        Log.e("Timetable", "Creating view")
        makeAPIRequest(pickedDate)
        val root = inflater.inflate(R.layout.fragment_timetable, container, false)
        return root
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onStart() {
        super.onStart()
        calendar.time = Date()
        currentYear = calendar[Calendar.YEAR]

        todayButton.setOnClickListener {
            pickCurrentDate()
        }

        timetableSwipeRefresh.setOnRefreshListener {
            makeAPIRequest(pickedDate)
            deinitModel()
        }

        val myCalendarViewManager = object : CalendarViewManager {
            override fun setCalendarViewResourceId(position: Int, date: Date, isSelected: Boolean): Int {
                val cal = Calendar.getInstance()
                cal.time = date
                if (isSelected) {
                    return  R.layout.selected_calendar_item
                } else {
                   return R.layout.calendar_item
                }
            }

            override fun bindDataToCalendarView(holder: SingleRowCalendarAdapter.CalendarViewHolder, date: Date, position: Int, isSelected: Boolean) {
                holder.itemView.tv_date_calendar_item.text = DateUtils.getDayNumber(date)
                holder.itemView.tv_day_calendar_item.text = dayOfWeekTo2LettersCapitalized(date.day)
            }
        }

        val myCalendarChangesObserver = object : CalendarChangesObserver {
            override fun whenSelectionChanged(isSelected: Boolean, position: Int, date: Date) {
                Log.e("DatePicker", "picked new date... $date")
                pickedDate = date
                makeAPIRequest(pickedDate)
                deinitModel()
                dayNumView.text = DateUtils.getDayNumber(date)
                weekDayView.text = dayOfWeekToString(date.day)
                monthYearView.text = "${monthNumToString(date.month)} ${DateUtils.getYear(date)}"

                if (date.date == Date().date) {
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

        val singleRowCalendar = datePickerView.apply {
            calendarViewManager = myCalendarViewManager
            calendarChangesObserver = myCalendarChangesObserver
            calendarSelectionManager = mySelectionManager
            initialPositionIndex = 14
            futureDaysCount = 350
            pastDaysCount = 14
            init()
        }
        pickCurrentDate()
    }

    private fun pickCurrentDate() {
        datePickerView.smoothScrollToPosition(14)
        datePickerView.select(14)
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
            12 -> return "Груд."
            1 -> return "Січ."
            2 -> return "Лют."
            3 -> return "Бер."
            4 -> return "Квіт."
            5 -> return "Трав."
            6 -> return "Черв."
            7 -> return "Лип."
            8 -> return "Серп."
            9 -> return "Вер."
            10 -> return "Жовт."
            11 -> return "Лист."
            else -> return ""
        }
    }

    private fun setUpRecyclerView() {
        rv_recyclerView.layoutManager = LinearLayoutManager(requireActivity().application.applicationContext)
        rv_recyclerView.adapter = TimetableRecyclerAdapter(disList, cabList, paraList, teacherList)
    }

    private fun addToList(
        dis: String,
        cab: String,
        teacher: String,
        para: String
    ) {
        disList.add(dis)
        cabList.add(cab)
        teacherList.add(teacher)
        paraList.add(para)
    }

    private fun deinitModel() {
        disList.clear()
        cabList.clear()
        paraList.clear()
        teacherList.clear()
    }

    private fun makeAPIRequest(date: Date) {
        val api = Retrofit.Builder()
            .baseUrl(BASE_TIMETABLE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(TimetableAPIRequest::class.java)
        GlobalScope.launch(Dispatchers.IO) {
            try {
                val response = api.TimetableApiJSON()
                for (timetable in timetableDay(response, date)) {
                    Log.e("Timetable", "Result = $timetable")
                    addToList(timetable.lesson, timetable.room, timetable.teacher, timetable.lessonNum)
                }
                withContext(Dispatchers.Main) {
                    setUpRecyclerView()
                    timetableSwipeRefresh.isRefreshing = false
//                    fadeInFRomBlack()
//                    progressBar.visibility = View.GONE
                }
            } catch (e: Exception) {
                Log.e("Timetable", e.toString())

                withContext(Dispatchers.Main) {
//                    attemptRequestAgain()
                }
            }
        }
    }

    fun timetableDay(response: TimetableApiJSON, date: Date): List<Fri> {
        when (date.day) {
            1 -> {
                Log.e("TimetableDay", "mon")
                return response.timetable.firstsubgroup.firstweek.mon
                }
            2 ->{
                Log.e("TimetableDay", "tue")
                return response.timetable.firstsubgroup.firstweek.tue
                }
            3 ->{
                Log.e("TimetableDay", "wed")
                return response.timetable.firstsubgroup.firstweek.wed
                }
            4 ->{
                Log.e("TimetableDay", "thu")
                return response.timetable.firstsubgroup.firstweek.thu
                }
            5 ->{
                Log.e("TimetableDay", "fri")
                return response.timetable.firstsubgroup.firstweek.fri
                }
            6 ->{
                Log.e("TimetableDay", "sun")
                return response.timetable.firstsubgroup.firstweek.sun
                }
            7 ->{
                Log.e("TimetableDay", "sat")
                return response.timetable.firstsubgroup.firstweek.sat
                }
            else ->{
                Log.e("TimetableDay", "sat")
                return response.timetable.firstsubgroup.firstweek.sat
                }
        }
    }


//    private fun attemptRequestAgain() {
//        countDownTimer = object: CountDownTimer(5 * 1000, 1000) {
//            override fun onTick(millisUntilFinished: Long) {
//                Log.i(
//                    "Timetable",
//                    "Could not retrieve data... Trying again in ${millisUntilFinished / 1000} seconds"
//                )
//            }
//
//            override fun onFinish() {
//                makeAPIRequest()
//                countDownTimer.cancel()
//            }
//
//        }
//        countDownTimer.start()
//    }

}