package ge.sshoshiashvili.weatherapp

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.android.material.tabs.TabLayoutMediator
import ge.sshoshiashvili.weatherapp.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    enum class DisplayType {
        CURRENT, HOURLY
    }

    private var tabViews = arrayOf(R.layout.current_icon, R.layout.hourly_icon)

    private lateinit var binding: ActivityMainBinding

    private var displayType = DisplayType.CURRENT

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.tabLayout.setBackgroundColor(
            ContextCompat.getColor(
                this.applicationContext,
                R.color.dark_blue
            )
        )

        val pager = binding.viewPager2
        val tabLayout = binding.tabLayout

        pager.adapter = WeatherViewAdapter(supportFragmentManager, lifecycle)

        TabLayoutMediator(tabLayout, pager) { tab, position ->
            tab.customView = layoutInflater.inflate(tabViews[position], null)
        }.attach()

    }
}