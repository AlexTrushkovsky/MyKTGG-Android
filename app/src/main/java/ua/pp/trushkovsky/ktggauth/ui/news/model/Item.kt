package ua.pp.trushkovsky.ktggauth.ui.news.model

data class Item(
    val category: Category,
    val created: String,
    val fulltext: String,
    val imageMedium: String,
    val introtext: String,
    val link: String,
    val title: String
)