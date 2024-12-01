package suzdalenko.speedometer.ui.home

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import suzdalenko.speedometer.databinding.FragmentHomeBinding
import suzdalenko.speedometer.util.App.Companion.speedViewModel
import suzdalenko.speedometer.util.SpeedService
import suzdalenko.speedometer.util.SpeedViewModel

class HomeFragment : Fragment() {
    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val homeViewModel = ViewModelProvider(this).get(HomeViewModel::class.java)
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val root: View = binding.root

        // Observar cambios en los datos del ViewModel
        speedViewModel.currentSpeed.observe(viewLifecycleOwner) { speed ->
            binding.tvCurrentSpeed.text = speed
        }
        speedViewModel.currentSpeed2.observe(viewLifecycleOwner) { speed ->
            binding.tvCurrentSpeed2.text = speed
        }
        speedViewModel.currentSpeed3.observe(viewLifecycleOwner) { speed ->
            binding.tvCurrentSpeed3.text = speed
        }
        speedViewModel.totalDistance.observe(viewLifecycleOwner) { distance ->
            binding.tvTotalDistance.text = "Distance: $distance m"
        }
        speedViewModel.altitude.observe(viewLifecycleOwner) { altitude ->
            binding.tvAltitude.text = "Height: $altitude m"
        }
        speedViewModel.elapsedTime.observe(viewLifecycleOwner) { time ->
            binding.tvElapsedTime.text = "Time: $time"
        }


        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}