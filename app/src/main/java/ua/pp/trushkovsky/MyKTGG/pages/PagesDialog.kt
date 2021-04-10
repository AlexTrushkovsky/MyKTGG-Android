package ua.pp.trushkovsky.MyKTGG.pages

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager
import androidx.viewpager2.widget.ViewPager2
import kotlinx.android.synthetic.main.dialog_pages.*
import ua.pp.trushkovsky.MyKTGG.R


class PagesDialog : DialogFragment() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.AppTheme_FullScreenDialog)
    }

    override fun onStart() {
        super.onStart()
        val pager = view_pager ?: return
        val adapter = ViewPagerAdapter()
        pager.adapter = adapter
        indicator?.setViewPager(pager)

        pager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                setupPage()
            }
        })

        val dialog = dialog
        if (dialog != null) {
            val width = ViewGroup.LayoutParams.MATCH_PARENT
            val height = ViewGroup.LayoutParams.MATCH_PARENT
            dialog.window!!.setLayout(width, height)
            dialog.window!!.setWindowAnimations(R.style.AppTheme_Slide)
        }

        next_button?.setOnClickListener {
            when (view_pager.currentItem) {
                0,1 -> {
                    view_pager.currentItem = view_pager.currentItem+1
                }
                2 -> {
                    skip()
                }
            }
        }
        skip_button?.setOnClickListener { skip() }
    }

    fun setupPage() {
        when (view_pager.currentItem) {
            0,1 -> {
                next_button?.text = "Далі"
                skip_button?.isVisible = true
            }
            2 -> {
                next_button?.text = "Розпочати"
                skip_button?.isVisible = false
            }
        }
    }

    private fun skip() {
        dismiss()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        return inflater.inflate(R.layout.dialog_pages, container, false)
    }

    companion object {
        private const val TAG = "dialog_pages"
        fun display(fragmentManager: FragmentManager?): PagesDialog {
            val exampleDialog =
                PagesDialog()
            exampleDialog.show(
                fragmentManager!!,
                TAG
            )
            return exampleDialog
        }
    }
}