package ua.pp.trushkovsky.MyKTGG.ui.home.weather

data class WeatherModel (
    var weather: Array<Weather>?,
    var main: Main?
)

data class Main (
    var temp: Double?
)

data class Weather (
    var description: String?,
    var icon: String?
)