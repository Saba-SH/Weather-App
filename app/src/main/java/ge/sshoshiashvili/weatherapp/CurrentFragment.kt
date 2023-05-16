package ge.sshoshiashvili.weatherapp

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import ge.sshoshiashvili.weatherapp.databinding.FlagsBinding
import ge.sshoshiashvili.weatherapp.databinding.FragmentCurrentBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query
import java.time.*
import java.util.*
import kotlin.math.roundToInt

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [CurrentFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class CurrentFragment : Fragment() {
    private lateinit var binding: FragmentCurrentBinding
    private lateinit var flagsBinding: FlagsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    data class WeatherData(
        val main: Main,
        val weather: List<Weather>,
        val timezone: Int
    )

    data class Main(
        val temp: Double,
        val feels_like: Double,
        val humidity: Int,
        val pressure: Int
    )

    data class Weather(
        val description: String,
        val icon: String
    )

    interface OpenWeatherMapApi {
        @GET("weather")
        suspend fun getCurrentWeather(
            @Query("q") city: String,
            @Query("appid") apiKey: String,
            @Query("units") units: String = "metric"
        ): WeatherData
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_current, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentCurrentBinding.bind(view)
        flagsBinding = FlagsBinding.bind(view.findViewById(R.id.flagsBar))
        flagsBinding.ivGeoFlag.setOnClickListener {
            val bundle = Bundle()
            bundle.putString("flag", "GEO")
            parentFragmentManager.setFragmentResult("flag_clicked_in_current", bundle)
            CoroutineScope(Dispatchers.Main).async { updateData("Tbilisi") }
        }
        flagsBinding.ivUKFlag.setOnClickListener {
            val bundle = Bundle()
            bundle.putString("flag", "UK")
            parentFragmentManager.setFragmentResult("flag_clicked_in_current", bundle)
            CoroutineScope(Dispatchers.Main).async { updateData("London") }
        }
        flagsBinding.ivJamFlag.setOnClickListener {
            val bundle = Bundle()
            bundle.putString("flag", "JAM")
            parentFragmentManager.setFragmentResult("flag_clicked_in_current", bundle)
            CoroutineScope(Dispatchers.Main).async { updateData("Kingston") }
        }

        flagsBinding.ivGeoFlag.performClick()

        parentFragmentManager.setFragmentResultListener(
            "flag_clicked_in_hourly",
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
                    city
                )
            }
        }
    }

    private suspend fun updateData(city: String) {
        val retrofit = Retrofit.Builder()
            .baseUrl("https://api.openweathermap.org/data/2.5/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val openWeatherMapApi = retrofit.create(OpenWeatherMapApi::class.java)

        val apiKey = "ENTER_API_KEY"

        try {
            val weatherData = openWeatherMapApi.getCurrentWeather(city, apiKey)

            val temperature = weatherData.main.temp.roundToInt()
            val feelsLike = weatherData.main.feels_like.roundToInt()
            val humidity = weatherData.main.humidity
            val pressure = weatherData.main.pressure
            val description = weatherData.weather[0].description
            val weatherIcon = weatherData.weather[0].icon
            val timezone = weatherData.timezone

            val utcCalendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"))
            utcCalendar.timeInMillis = utcCalendar.timeInMillis + timezone * 1000
            val currentHour = utcCalendar.get(Calendar.HOUR_OF_DAY)

            if (currentHour in 6..17) {
                binding.scrollView2.setBackgroundColor(
                    ContextCompat.getColor(
                        this.requireContext(),
                        R.color.day
                    )
                )
            } else {
                binding.scrollView2.setBackgroundColor(
                    ContextCompat.getColor(
                        this.requireContext(),
                        R.color.night
                    )
                )
            }

            binding.tvCity.text = city.uppercase()
            binding.tvTempr.text = "$temperature\u00B0"
            binding.tvTemp.text = "$temperature\u00B0"
            binding.tvFeel.text = "$feelsLike\u00B0"
            binding.tvHMDT.text = "$humidity%"
            binding.tvPress.text = pressure.toString()
            binding.tvWeathDesc.text = description.toString().uppercase()
            val iconUrl = "http://openweathermap.org/img/w/$weatherIcon.png"
            Glide.with(this)
                .load(iconUrl)
                .into(binding.ivCurrentWeather)
        } catch (e: Exception) {
            Toast.makeText(this.context, e.message, Toast.LENGTH_SHORT)
            Log.e("TAG", "Error getting weather data", e)
        }
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment CurrentFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            CurrentFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}