package com.example.copycats

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.compass.CompassOverlay
import org.osmdroid.views.overlay.compass.InternalCompassOrientationProvider
import org.osmdroid.views.overlay.gestures.RotationGestureOverlay
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay
import android.util.Log
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import org.osmdroid.views.overlay.Overlay


private lateinit var directionIcon: Drawable

data class EventMarkerData(
    val marker: Marker,
    var temperature: Float? = null,
    var soundLevel: Double? = null
)

class MapFragment : Fragment() {

    private lateinit var mapView: MapView
    private lateinit var locationOverlay: MyLocationNewOverlay

    private val markerIconCache = mutableMapOf<String, Drawable>()
    private val eventMarkers = mutableMapOf<Int, EventMarkerData>()
    private lateinit var sensorDataOverlay: SensorDataOverlay

    private val sensorDataListener: (Int, EventSensorData) -> Unit = { eventId, sensorData ->
        updateMarkerWithSensorData(eventId, sensorData)
    }

    private val locationPermissionRequest = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        when {
            permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true -> {
                enableUserLocation()
            }
            permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true -> {
                enableUserLocation()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Configuration.getInstance().load(
            requireContext(),
            requireContext().getSharedPreferences("osmdroid", 0)
        )
        Configuration.getInstance().userAgentValue = requireContext().packageName
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_map, container, false)
        mapView = view.findViewById(R.id.mapView)
        setupMap()
        return view
    }

    private fun setupMap() {
        mapView.setTileSource(TileSourceFactory.MAPNIK)
        mapView.setMultiTouchControls(true)
        mapView.controller.setZoom(15.0)


        // Reduce tile loading for better performance
        mapView.setTilesScaledToDpi(true)
        mapView.isTilesScaledToDpi = true

        // Check if specific coordinates were passed
        val startPoint = if (arguments?.containsKey(ARG_LATITUDE) == true &&
                             arguments?.containsKey(ARG_LONGITUDE) == true) {
            val lat = arguments?.getDouble(ARG_LATITUDE) ?: 46.5547
            val lon = arguments?.getDouble(ARG_LONGITUDE) ?: 15.6459
            GeoPoint(lat, lon)
        } else {
            // Use cached location if available, otherwise use default
            MyApplication.lastKnownUserLocation ?: GeoPoint(46.5547, 15.6459)
        }
        mapView.controller.setCenter(startPoint)

        val compassOverlay = CompassOverlay(
            requireContext(),
            InternalCompassOrientationProvider(requireContext()),
            mapView
        )
        compassOverlay.enableCompass()
        mapView.overlays.add(compassOverlay)

        locationOverlay =
            MyLocationNewOverlay(
                GpsMyLocationProvider(requireContext()),
                mapView
            )

        locationOverlay.setPersonAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER)

        directionIcon = ContextCompat.getDrawable(requireContext(), R.drawable.direction_icon)!!
        directionIcon.setBounds(0,0 , directionIcon.intrinsicWidth/24, directionIcon.intrinsicHeight/24)
        locationOverlay.setDirectionIcon(drawableToBitmap(directionIcon))

        locationOverlay.setDirectionAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER)

        locationOverlay.enableMyLocation()
        locationOverlay.enableFollowLocation()

        mapView.getOverlays().add(locationOverlay)

        val rotationGestureOverlay = RotationGestureOverlay(mapView)
        rotationGestureOverlay.isEnabled = true
        mapView.overlays.add(rotationGestureOverlay)

        mapView.minZoomLevel = 4.0
        mapView.maxZoomLevel = 20.0

        // Initialize sensor data overlay
        sensorDataOverlay = SensorDataOverlay(eventMarkers)
        mapView.overlays.add(sensorDataOverlay)

        // Add event markers to the map
        addEventMarkers()

        // Register MQTT sensor data listener
        MqttDataListener.addDataListener(sensorDataListener)

        checkLocationPermission()
    }

    private fun addEventMarkers() {
        Log.d("MapFragment", "Adding ${MyApplication.events.size} event markers to map")

        // Pre-load and cache marker icons once
        if (markerIconCache.isEmpty()) {
            try {
                markerIconCache["education"] = ContextCompat.getDrawable(requireContext(), R.drawable.baseline_school_24)!!
                markerIconCache["fun"] = ContextCompat.getDrawable(requireContext(), R.drawable.baseline_sports_basketball_24)!!
                markerIconCache["sports"] = ContextCompat.getDrawable(requireContext(), R.drawable.baseline_sports_basketball_24)!!
                markerIconCache["default"] = ContextCompat.getDrawable(requireContext(), R.drawable.baseline_event_24)!!
            } catch (e: Exception) {
                Log.e("MapFragment", "Error loading marker icons", e)
            }
        }

        MyApplication.events.forEach { event ->
            // Find the location for this event
            val location = MyApplication.locations.find { it.id == event.location_fk }

            if (location != null) {
                val marker = Marker(mapView)
                marker.position = GeoPoint(location.latitude, location.longitude)
                marker.title = event.title

                // Build initial snippet (without sensor data - that's shown on the map)
                marker.snippet = """
                    ${event.description}
                    
                    Start: ${event.start_date}
                    End: ${event.end_date}
                    Tag: ${event.tag ?: "None"}
                    Attendees: ${event.num_people ?: 0}
                """.trimIndent()

                // Use cached marker icon based on tag
                val iconKey = event.tag?.lowercase() ?: "default"
                val baseMarkerIcon = markerIconCache[iconKey] ?: markerIconCache["default"]

                baseMarkerIcon?.let { icon ->
                    // Calculate scale based on number of people (1.0 to 2.0)
                    val numPeople = event.num_people ?: 0
                    val scale = 1.0f + (numPeople * 0.15f).coerceAtMost(1.5f)

                    // Create scaled icon
                    val scaledIcon = createScaledDrawable(icon, scale)
                    marker.icon = scaledIcon
                }

                // Set marker anchor to center bottom
                marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)

                // Add click listener to open event detail fragment
                marker.setOnMarkerClickListener { _, _ ->
                    // Navigate to event detail fragment using activity's fragment manager
                    val eventDetailFragment = EventDetailFragment.newInstance(event.id)
                    requireActivity().supportFragmentManager.beginTransaction()
                        .replace(R.id.fragmentContainerView, eventDetailFragment)
                        .addToBackStack(null)
                        .commit()
                    true
                }

                mapView.overlays.add(marker)

                // Store marker data with initial sensor values
                val sensorData = MqttDataListener.getSensorData(event.id)
                eventMarkers[event.id] = EventMarkerData(
                    marker = marker,
                    temperature = sensorData?.temperature,
                    soundLevel = sensorData?.soundLevel
                )

                Log.d("MapFragment", "Added marker for event: ${event.title} at (${location.latitude}, ${location.longitude})")
            } else {
                Log.w("MapFragment", "No location found for event #${event.id}: ${event.title} (location_fk: ${event.location_fk})")
            }
        }

        mapView.invalidate()
    }

    private fun updateMarkerWithSensorData(eventId: Int, sensorData: EventSensorData) {
        val markerData = eventMarkers[eventId] ?: return

        // Update marker data with new sensor readings
        activity?.runOnUiThread {
            markerData.temperature = sensorData.temperature
            markerData.soundLevel = sensorData.soundLevel
            mapView.invalidate()
            Log.d("MapFragment", "Updated marker for event $eventId with sensor data: temp=${sensorData.temperature}, sound=${sensorData.soundLevel}")
        }
    }


    private fun checkLocationPermission() {
        when {
            ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED -> {
                enableUserLocation()
            }
            else -> {
                locationPermissionRequest.launch(
                    arrayOf(
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    )
                )
            }
        }
    }

    private fun enableUserLocation() {
        locationOverlay.enableMyLocation()

        locationOverlay.runOnFirstFix {
            requireActivity().runOnUiThread {
                val location = locationOverlay.myLocation
                mapView.controller.animateTo(location)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        mapView.onResume()
    }

    override fun onPause() {
        super.onPause()
        mapView.onPause()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        markerIconCache.clear()
        eventMarkers.clear()
        MqttDataListener.removeDataListener(sensorDataListener)
        mapView.onDetach()
    }

    private fun drawableToBitmap(drawable: Drawable): Bitmap {
        val bitmap = Bitmap.createBitmap(
            drawable.intrinsicWidth/18,
            drawable.intrinsicHeight/16,
            Bitmap.Config.ARGB_8888
        )
        val canvas = Canvas(bitmap)
        drawable.setBounds(0, 0, canvas.width, canvas.height)
        drawable.draw(canvas)
        return bitmap
    }

    private fun createScaledDrawable(drawable: Drawable, scale: Float): Drawable {
        val scaledWidth = (drawable.intrinsicWidth * scale).toInt()
        val scaledHeight = (drawable.intrinsicHeight * scale).toInt()

        val bitmap = Bitmap.createBitmap(scaledWidth, scaledHeight, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        drawable.setBounds(0, 0, scaledWidth, scaledHeight)
        drawable.draw(canvas)

        return android.graphics.drawable.BitmapDrawable(resources, bitmap)
    }

    inner class SensorDataOverlay(
        private val eventMarkers: Map<Int, EventMarkerData>
    ) : Overlay() {

        private val textPaint = Paint().apply {
            color = Color.WHITE
            textSize = 28f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
            textAlign = Paint.Align.CENTER
            setShadowLayer(3f, 0f, 0f, Color.BLACK)
        }

        private val backgroundPaint = Paint().apply {
            color = Color.argb(200, 0, 0, 0)
            isAntiAlias = true
        }

        override fun draw(canvas: Canvas, mapView: MapView, shadow: Boolean) {
            if (shadow) return

            eventMarkers.forEach { (_, markerData) ->
                val marker = markerData.marker
                val projection = mapView.projection
                val point = projection.toPixels(marker.position, null)

                val labels = mutableListOf<String>()

                // Build label text
                markerData.temperature?.let { temp ->
                    labels.add("🌡️ ${String.format(java.util.Locale.US, "%.1f", temp)}°C")
                }

                markerData.soundLevel?.let { sound ->
                    labels.add("🔊 ${String.format(java.util.Locale.US, "%.0f", sound)} dB")
                }

                if (labels.isNotEmpty()) {
                    // Draw labels above the marker
                    val lineHeight = 35f
                    val startY = point.y - 150f // Position above marker

                    labels.forEachIndexed { index, label ->
                        val yPos = startY - (index * lineHeight)

                        // Measure text width for background
                        val textWidth = textPaint.measureText(label)
                        val padding = 12f

                        // Draw background rounded rectangle
                        val left = point.x - (textWidth / 2) - padding
                        val right = point.x + (textWidth / 2) + padding
                        val top = yPos - 25f
                        val bottom = yPos + 8f

                        canvas.drawRoundRect(left, top, right, bottom, 8f, 8f, backgroundPaint)

                        // Draw text
                        canvas.drawText(label, point.x.toFloat(), yPos, textPaint)
                    }
                }
            }
        }
    }

    companion object {
        private const val ARG_LATITUDE = "latitude"
        private const val ARG_LONGITUDE = "longitude"

        @JvmStatic
        fun newInstance() = MapFragment()

        @JvmStatic
        fun newInstanceWithLocation(latitude: Double, longitude: Double) =
            MapFragment().apply {
                arguments = Bundle().apply {
                    putDouble(ARG_LATITUDE, latitude)
                    putDouble(ARG_LONGITUDE, longitude)
                }
            }
    }
}