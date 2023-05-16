package ge.sshoshiashvili.weatherapp

data class HourLine(
    var date: String,
    var icon: String,
    var temperature: Int,
    var description: String
)