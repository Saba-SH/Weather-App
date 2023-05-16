package ge.sshoshiashvili.weatherapp

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import ge.sshoshiashvili.weatherapp.databinding.FlagsBinding
import ge.sshoshiashvili.weatherapp.databinding.FragmentHourlyBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.roundToInt

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [HourlyFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class HourlyFragment : Fragment() {

    private lateinit var binding: FragmentHourlyBinding
    private lateinit var flagsBinding: FlagsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    data class ForecastResponse(
        val city: City,
        val list: List<ForecastItem>
    )

    data class City(
        val name: String,
        val timezone: Int
    )

    data class ForecastItem(
        val dt: Long,
        val main: Main,
        val weather: List<Weather>
    )

    data class Main(
        val temp: Double,
    )

    data class Weather(
        val icon: String,
        val description: String
    )

    interface OpenWeatherMapApi {
        @GET("forecast")
        suspend fun getForecastResponse(
            @Query("lat") latitude: Double,
            @Query("lon") longitude: Double,
            @Query("appid") apiKey: String,
            @Query("units") units: String,
        ): ForecastResponse
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_hourly, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentHourlyBinding.bind(view)
        flagsBinding = FlagsBinding.bind(view.findViewById(R.id.flagsBar))

        flagsBinding.ivGeoFlag.setOnClickListener {
            val bundle = Bundle()
            bundle.putString("flag", "GEO")
            parentFragmentManager.setFragmentResult("flag_clicked_in_hourly", bundle)
            CoroutineScope(Dispatchers.Main).async {
                updateData(
                    "Tbilisi",
                    getLatitude("Tbilisi"),
                    getLongitude("Tbilisi"),
                    getTimezone("Tbilisi")
                )
            }
        }
        flagsBinding.ivUKFlag.setOnClickListener {
            val bundle = Bundle()
            bundle.putString("flag", "UK")
            parentFragmentManager.setFragmentResult("flag_clicked_in_hourly", bundle)
            CoroutineScope(Dispatchers.Main).async {
                updateData(
                    "London",
                    getLatitude("London"),
                    getLongitude("London"),
                    getTimezone("London")
                )
            }
        }
        flagsBinding.ivJamFlag.setOnClickListener {
            val bundle = Bundle()
            bundle.putString("flag", "JAM")
            parentFragmentManager.setFragmentResult("flag_clicked_in_hourly", bundle)
            CoroutineScope(Dispatchers.Main).async {
                updateData(
                    "Kingston",
                    getLatitude("Kingston"),
                    getLongitude("Kingston"),
                    getTimezone("Kingston")
                )
            }
        }

//        CoroutineScope(Dispatchers.Main).async {
//            updateData(
//                "Tbilisi",
//                getLatitude("Tbilisi"),
//                getLongitude("Tbilisi"),
//                getTimezone("Tbilisi")
//            )
//        }

        parentFragmentManager.setFragmentResultListener(
            "flag_clicked_in_current",
            viewLifecycleOwner
        ) { _, result ->
            val flag = result.getString("flag")
            val city = mapOf(
                "GEO" to "Tbilisi",
                "UK" to "London",
                "JAM" to "Kingston",
            )[flag]!!
            CoroutineScope(Dispatchers.Main).async {
                updateData(
                    city,
                    getLatitude(city),
                    getLongitude(city),
                    getTimezone(city)
                )
            }
        }
    }

    private suspend fun updateData(
        city: String,
        latitude: Double,
        longitude: Double,
        timezone: Int
    ) {
        val retrofit = Retrofit.Builder()
            .baseUrl("https://api.openweathermap.org/data/2.5/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val openWeatherMapApi = retrofit.create(OpenWeatherMapApi::class.java)

        val apiKey = "ENTER_API_KEY"

        try {
            val response =
                openWeatherMapApi.getForecastResponse(latitude, longitude, apiKey, "metric")
            val hourlyList = response.list

            binding.tvCity.text = city.uppercase()

            val utcCalendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"))
            utcCalendar.timeInMillis = utcCalendar.timeInMillis + timezone * 1000
            val currentHour = utcCalendar.get(Calendar.HOUR_OF_DAY)

            if (currentHour in 6..17) {
                binding.scrollView.setBackgroundColor(
                    ContextCompat.getColor(
                        this.requireContext(),
                        R.color.day
                    )
                )
            } else {
                binding.scrollView.setBackgroundColor(
                    ContextCompat.getColor(
                        this.requireContext(),
                        R.color.night
                    )
                )
            }

            val inflater = LayoutInflater.from(requireContext())
            val weatherList = binding.weatherList

            var hourLines = mutableListOf<HourLine>()

            hourlyList.forEach { forecastItem ->
                val dt = forecastItem.dt
                val dtMillis = dt.toLong() * 1000
                val weather = forecastItem.weather[0]
                val main = forecastItem.main

                val date = SimpleDateFormat(
                    "hh a dd MMM",
                    Locale.getDefault()
                ).format(Date(dtMillis - TimeZone.getDefault().rawOffset + timezone * 1000))
                    .toString()
                val temp = main.temp.roundToInt()
                val desc = weather.description
                val icon = weather.icon

                hourLines.add(HourLine(date, icon, temp, desc))
            }
            val adapter = HourlyViewAdapter(hourLines)
            weatherList.adapter = adapter
            weatherList.layoutManager = LinearLayoutManager(this.context)
        } catch (e: Exception) {
            Toast.makeText(this.context, e.message, Toast.LENGTH_SHORT)
            Log.e("TAG", "Error getting weather data", e)
        }
    }

    private val latMap = mapOf(
        "Tbilisi" to 41.69,
        "London" to 51.50,
        "Kingston" to 18.01,
    )

    private val lonMap = mapOf(
        "Tbilisi" to 44.80,
        "London" to -0.12,
        "Kingston" to -76.81,
    )

    private val timMap = mapOf(
        "Tbilisi" to 14400,
        "London" to 3600,
        "Kingston" to -18000,
    )

    private fun getLatitude(city: String): Double {
        return latMap[city]!!
    }

    private fun getLongitude(city: String): Double {
        return lonMap[city]!!
    }

    private fun getTimezone(city: String): Int {
        return timMap[city]!!
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment HourlyFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            HourlyFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}