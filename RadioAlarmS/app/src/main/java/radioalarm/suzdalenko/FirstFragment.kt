package radioalarm.suzdalenko

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TimePicker
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import radioalarm.suzdalenko.databinding.FragmentFirstBinding
import radioalarm.suzdalenko.util.App.Companion.sh

class FirstFragment : Fragment() {
    private var _binding: FragmentFirstBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentFirstBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        val timePicker: TimePicker = binding.root.findViewById(R.id.timePicker)
            timePicker.setIs24HourView(true)
            timePicker.setOnTimeChangedListener { _, hourOfDay, minute ->
                Toast.makeText(requireContext(), "Hora seleccionada: ${hourOfDay}:${minute}", Toast.LENGTH_SHORT).show()
                val hour = hourOfDay.toInt()
                val min  = minute.toInt()
                sh.edit().putInt("hour", hour).apply()
                sh.edit().putInt("min", min).apply()
            }


        binding.buttonFirst.setOnClickListener {
            findNavController().navigate(R.id.action_FirstFragment_to_SecondFragment)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}