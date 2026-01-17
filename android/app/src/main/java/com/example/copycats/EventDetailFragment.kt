package com.example.copycats

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.example.copycats.obj.Event
import com.google.android.material.button.MaterialButton
import java.text.SimpleDateFormat
import java.util.*

class EventDetailFragment : Fragment() {

    private var eventId: Int = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            eventId = it.getInt(ARG_EVENT_ID, -1)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_event_detail, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Find the event by ID
        val event = MyApplication.events.find { it.id == eventId }

        if (event != null) {
            displayEventDetails(view, event)
        } else {
            // Event not found, go back
            requireActivity().supportFragmentManager.popBackStack()
        }

        // Back button
        view.findViewById<ImageButton>(R.id.back_button).setOnClickListener {
            requireActivity().supportFragmentManager.popBackStack()
        }

        // Camera button
        view.findViewById<MaterialButton>(R.id.take_photo_button).setOnClickListener {
            // Navigate to camera fragment with event ID
            val cameraFragment = CameraFragment.newInstance(eventId)
            requireActivity().supportFragmentManager.beginTransaction()
                .replace(R.id.fragmentContainerView, cameraFragment)
                .addToBackStack(null)
                .commit()
        }
    }

    private fun displayEventDetails(view: View, event: Event) {
        // Set event title
        view.findViewById<TextView>(R.id.event_title).text = event.title

        // Set event icon based on tag
        val eventIcon = view.findViewById<ImageView>(R.id.event_icon)
        val iconDrawable = when (event.tag?.lowercase()) {
            "education" -> R.drawable.baseline_school_24
            "fun" -> R.drawable.baseline_celebration_24
            "sports" -> R.drawable.baseline_sports_basketball_24
            else -> R.drawable.baseline_event_24
        }
        eventIcon.setImageDrawable(ContextCompat.getDrawable(requireContext(), iconDrawable))

        // Set event description
        view.findViewById<TextView>(R.id.event_description).text = event.description

        // Format and set dates
        val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
        val outputFormat = SimpleDateFormat("MMM dd, yyyy 'at' HH:mm", Locale.getDefault())

        try {
            val startDate = inputFormat.parse(event.start_date)
            val endDate = inputFormat.parse(event.end_date)

            view.findViewById<TextView>(R.id.event_start_date).text =
                if (startDate != null) outputFormat.format(startDate) else event.start_date

            view.findViewById<TextView>(R.id.event_end_date).text =
                if (endDate != null) outputFormat.format(endDate) else event.end_date
        } catch (_: Exception) {
            // If parsing fails, use the original strings
            view.findViewById<TextView>(R.id.event_start_date).text = event.start_date
            view.findViewById<TextView>(R.id.event_end_date).text = event.end_date
        }

        // Set event tag
        view.findViewById<TextView>(R.id.event_tag).text =
            event.tag?.replaceFirstChar { it.uppercase() } ?: "None"

        // Set user FK
        view.findViewById<TextView>(R.id.event_user_fk).text = event.user_fk.toString()

        // Set public status
        view.findViewById<TextView>(R.id.event_public).text =
            if (event.public) "Public" else "Private"

        // Set location info
        val location = MyApplication.locations.find { it.id == event.location_fk }
        if (location != null) {
            view.findViewById<TextView>(R.id.event_location_info).text = location.info
            view.findViewById<TextView>(R.id.event_location_coords).text =
                String.format(Locale.US, "(%.4f, %.4f)", location.latitude, location.longitude)

            // Show on map button
            view.findViewById<MaterialButton>(R.id.show_on_map_button).setOnClickListener {
                // Navigate back to main page (which contains the map)
                requireActivity().supportFragmentManager.popBackStack()
            }
        } else {
            view.findViewById<TextView>(R.id.event_location_info).text = "No location"
            view.findViewById<TextView>(R.id.event_location_coords).visibility = View.GONE
            view.findViewById<MaterialButton>(R.id.show_on_map_button).isEnabled = false
        }
    }

    companion object {
        private const val ARG_EVENT_ID = "event_id"

        @JvmStatic
        fun newInstance(eventId: Int) =
            EventDetailFragment().apply {
                arguments = Bundle().apply {
                    putInt(ARG_EVENT_ID, eventId)
                }
            }
    }
}
