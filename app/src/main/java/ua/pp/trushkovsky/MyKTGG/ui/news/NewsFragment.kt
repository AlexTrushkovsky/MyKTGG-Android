package ua.pp.trushkovsky.MyKTGG.ui.news.d

import android.os.Bundle
import android.util.Log
import android.view.*
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.fragment_news.*
import kotlinx.android.synthetic.main.fragment_news.bootomSheetRecycler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import ua.pp.trushkovsky.MyKTGG.R
import ua.pp.trushkovsky.MyKTGG.helpers.showDialogWith
import ua.pp.trushkovsky.MyKTGG.ui.news.api.APIRequest
import ua.pp.trushkovsky.MyKTGG.ui.news.api.RecyclerAdapter


const val BASE_NEWS_URL = "https://ktgg.kiev.ua"

class NewsFragment: Fragment() {

    private var titleList = mutableListOf<String>()
    private var textList = mutableListOf<String>()
    private var imagesList = mutableListOf<String>()
    private var dateList = mutableListOf<String>()
    private var categoryList = mutableListOf<String>()
    private var linksList = mutableListOf<String>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        Log.e("News", "Creating view")

        makeAPIRequest()
        val root = inflater.inflate(R.layout.fragment_news, container, false)
        return root
    }

    override fun onStart() {
        super.onStart()
        swiperefresh.isRefreshing = true
        swiperefresh.setOnRefreshListener {
            titleList.clear()
            textList.clear()
            imagesList.clear()
            dateList.clear()
            categoryList.clear()
            linksList.clear()
            setUpRecyclerView()
            makeAPIRequest()
        }
    }

    private fun setUpRecyclerView() {
        bootomSheetRecycler.layoutManager = LinearLayoutManager(requireActivity().application.applicationContext)
        bootomSheetRecycler.adapter = RecyclerAdapter(
            categoryList,
            dateList,
            imagesList,
            textList,
            linksList,
            titleList
        )
    }

    private fun addToList(
        title: String,
        description: String,
        image: String,
        link: String,
        category: String,
        date: String
    ) {
        titleList.add(title)
        textList.add(description)
        imagesList.add(image)
        linksList.add(link)
        categoryList.add(category)
        dateList.add(date)
    }

    private fun makeAPIRequest() {
        val api = Retrofit.Builder()
            .baseUrl(BASE_NEWS_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(APIRequest::class.java)
        GlobalScope.launch(Dispatchers.IO) {
            try {
                val response = api.getNews()
                for (article in response.items) {
                    Log.e("News", "Result = $article")
                    addToList(
                        article.title,
                        article.introtext,
                        article.imageMedium,
                        article.link,
                        article.category.name,
                        article.created
                    )
                }
                withContext(Dispatchers.Main) {
                    setUpRecyclerView()
                    swiperefresh.isRefreshing = false
                }
            } catch (e: Exception) {
                Log.e("News", e.toString())
                activity?.runOnUiThread {
                    showDialogWith("Відсутнє з'єднання з мережею", "зверніться до техпідтримки, або cпробуйте будь ласка пізніше", context, swiperefresh)
                }
            }
        }
    }
}