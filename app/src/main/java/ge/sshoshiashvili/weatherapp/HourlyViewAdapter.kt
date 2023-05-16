package ge.sshoshiashvili.weatherapp

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import ge.sshoshiashvili.weatherapp.databinding.HourLineLayoutBinding

class HourlyViewAdapter(
    var hourLines: List<HourLine>
) : RecyclerView.Adapter<HourlyViewAdapter.HourLineLayoutViewHolder>() {

    inner class HourLineLayoutViewHolder(val binding: HourLineLayoutBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HourLineLayoutViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val binding = HourLineLayoutBinding.inflate(layoutInflater, parent, false)
        return HourLineLayoutViewHolder(binding)
    }

    override fun onBindViewHolder(holder: HourLineLayoutViewHolder, position: Int) {
        holder.binding.apply {
            tvDate.text = hourLines[position].date
            Glide.with(holder.itemView.context)
                .load("http://openweathermap.org/img/w/${hourLines[position].icon}.png")
                .into(ivHourlyIcon)
            tvHourlyTemp.text = "${hourLines[position].temperature}°"
            tvHourlyDesc.text = hourLines[position].description
        }
    }

    override fun getItemCount(): Int {
        return hourLines.size
    }
}