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
import android.widget.TextView
import org.osmdroid.views.overlay.Overlay
import org.osmdroid.bonuspack.clustering.RadiusMarkerClusterer


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
    private lateinit var markerClusterer: RadiusMarkerClusterer

    private val sensorDataListener: (Int, EventSensorData) -> Unit = { eventId, sensorData ->
        Log.d("MapFragment", "MQTT data received for event $eventId: temp=${sensorData.temperature}, sound=${sensorData.soundLevel}")
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


        mapView.setTilesScaledToDpi(true)
        mapView.isTilesScaledToDpi = true

        val startPoint = if (arguments?.containsKey(ARG_LATITUDE) == true &&
                             arguments?.containsKey(ARG_LONGITUDE) == true) {
            val lat = arguments?.getDouble(ARG_LATITUDE) ?: 46.5547
            val lon = arguments?.getDouble(ARG_LONGITUDE) ?: 15.6459
            GeoPoint(lat, lon)
        } else {
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

        // Initialize marker clusterer with custom icon
        markerClusterer = RadiusMarkerClusterer(requireContext())
        markerClusterer.setRadius(150) // Cluster radius in pixels - increased for better clustering
        mapView.overlays.add(markerClusterer)

        // Add event markers first to populate eventMarkers map
        addEventMarkers()

        // Load any existing sensor data from global storage
        loadSensorDataFromGlobalStorage()

        // Add sensor data overlay AFTER markers are added (draws on top)
        sensorDataOverlay = SensorDataOverlay(eventMarkers)
        mapView.overlays.add(sensorDataOverlay)
        Log.d("MapFragment", "SensorDataOverlay added with ${eventMarkers.size} event markers")
        Log.d("MapFragment", "Event IDs in map: ${eventMarkers.keys.joinToString()}")

        MqttDataListener.addDataListener(sensorDataListener)

        checkLocationPermission()
    }

    private fun addEventMarkers() {
        Log.d("MapFragment", "Adding ${MyApplication.events.size} event markers to map")

        if (markerIconCache.isEmpty()) {
            try {
                markerIconCache["education"] = ContextCompat.getDrawable(requireContext(), R.drawable.education)!!
                markerIconCache["fun"] = ContextCompat.getDrawable(requireContext(), R.drawable.`fun`)!!
                markerIconCache["sports"] = ContextCompat.getDrawable(requireContext(), R.drawable.sports)!!
                markerIconCache["default"] = ContextCompat.getDrawable(requireContext(), R.drawable.cat)!!
            } catch (e: Exception) {
                Log.e("MapFragment", "Error loading marker icons", e)
            }
        }

        // Group events by location
        val eventsByLocation = mutableMapOf<Int, MutableList<com.example.copycats.obj.Event>>()
        MyApplication.events.forEach { event ->
            event.location_fk?.let { locationId ->
                eventsByLocation.getOrPut(locationId) { mutableListOf() }.add(event)
            }
        }

        // Create markers for each location
        eventsByLocation.forEach { (locationId, eventsAtLocation) ->
            val location = MyApplication.locations.find { it.id == locationId }

            if (location != null) {
                val marker = Marker(mapView)
                marker.position = GeoPoint(location.latitude, location.longitude)

                // Create combined title and snippet for grouped events
                if (eventsAtLocation.size == 1) {
                    val event = eventsAtLocation[0]
                    marker.title = event.title
                    marker.snippet = """
                        ${event.description}
                        
                        Start: ${event.start_date}
                        End: ${event.end_date}
                        Tag: ${event.tag ?: "None"}
                        Attendees: ${event.num_people ?: 0}
                    """.trimIndent()
                } else {
                    // Multiple events at same location
                    marker.title = "${eventsAtLocation.size} Events at ${location.info}"
                    val snippetBuilder = StringBuilder()
                    eventsAtLocation.forEachIndexed { index, event ->
                        snippetBuilder.append("${index + 1}. ${event.title}")
                        snippetBuilder.append("\n   ${event.description}")
                        snippetBuilder.append("\n   ${event.start_date} - ${event.end_date}")
                        if (index < eventsAtLocation.size - 1) {
                            snippetBuilder.append("\n\n")
                        }
                    }
                    marker.snippet = snippetBuilder.toString()
                }
                val event = eventsAtLocation[0]

                // Calculate total people at this location
                val totalPeople = eventsAtLocation.sumOf { it.num_people ?: 0 }
                val scale = 1.0f + (totalPeople * 0.10f).coerceAtMost(1.0f)

                val baseMarkerIcon = markerIconCache[event.tag]
                baseMarkerIcon?.let { icon ->
                    val scaledIcon = createScaledDrawable(icon, scale)
                    marker.icon = scaledIcon
                }

                marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)

                marker.setOnMarkerClickListener { _, _ ->
                    if (eventsAtLocation.size == 1) {
                        // Single event - open detail directly
                        val eventDetailFragment = EventDetailFragment.newInstance(eventsAtLocation[0].id)
                        requireActivity().supportFragmentManager.beginTransaction()
                            .replace(R.id.fragmentContainerView, eventDetailFragment)
                            .addToBackStack(null)
                            .commit()
                    } else {
                        // Multiple events - show list dialog
                        showEventSelectionDialog(eventsAtLocation)
                    }
                    true
                }

                // Store marker in eventMarkers map for each event at this location
                eventsAtLocation.forEach { event ->
                    eventMarkers[event.id] = EventMarkerData(marker)
                    Log.d("MapFragment", "Stored marker for event ID ${event.id}")
                }

                // Add marker to clusterer instead of directly to map
                markerClusterer.add(marker)
                Log.d("MapFragment", "Added marker to clusterer at (${location.latitude}, ${location.longitude})")
            } else {
                Log.w("MapFragment", "Location not found for location_fk: $locationId")
            }
        }

        // Invalidate clusterer to update clusters
        markerClusterer.invalidate()
        mapView.invalidate()
        Log.d("MapFragment", "Clusterer initialized with ${eventsByLocation.size} location(s) total")
        Log.d("MapFragment", "Total markers in clusterer: ${markerClusterer.items?.size ?: 0}")
    }

    private fun showEventSelectionDialog(events: List<com.example.copycats.obj.Event>) {
        val eventTitles = events.map { it.title }.toTypedArray()

        android.app.AlertDialog.Builder(requireContext())
            .setTitle("Select Event")
            .setItems(eventTitles) { _, which ->
                val selectedEvent = events[which]
                val eventDetailFragment = EventDetailFragment.newInstance(selectedEvent.id)
                requireActivity().supportFragmentManager.beginTransaction()
                    .replace(R.id.fragmentContainerView, eventDetailFragment)
                    .addToBackStack(null)
                    .commit()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun loadSensorDataFromGlobalStorage() {
        Log.d("MapFragment", "Loading sensor data from global storage...")
        var loadedCount = 0

        MyApplication.eventSensorData.forEach { (eventId, sensorData) ->
            val markerData = eventMarkers[eventId]
            if (markerData != null) {
                markerData.temperature = sensorData.temperature
                markerData.soundLevel = sensorData.soundLevel
                loadedCount++
                Log.d("MapFragment", "Loaded sensor data for event $eventId from global storage: temp=${sensorData.temperature}, sound=${sensorData.soundLevel}")
            } else {
                Log.w("MapFragment", "Sensor data exists for event $eventId but no marker found on map")
            }
        }

        Log.d("MapFragment", "Loaded sensor data for $loadedCount events from global storage")
        if (loadedCount > 0) {
            mapView.invalidate()
        }
    }

    private fun updateMarkerWithSensorData(eventId: Int, sensorData: EventSensorData) {
        Log.d("MapFragment", "updateMarkerWithSensorData called for event $eventId")
        Log.d("MapFragment", "Current thread: ${Thread.currentThread().name}")
        Log.d("MapFragment", "EventMarkers map size: ${eventMarkers.size}")
        Log.d("MapFragment", "Looking for event $eventId in map...")

        val markerData = eventMarkers[eventId]
        if (markerData == null) {
            Log.e("MapFragment", "❌ No marker found for event $eventId in eventMarkers map")
            Log.e("MapFragment", "Available event IDs: ${eventMarkers.keys.sorted().joinToString()}")
            return
        }

        Log.d("MapFragment", "✅ Found marker for event $eventId")

        activity?.runOnUiThread {
            Log.d("MapFragment", "Updating marker data on UI thread...")
            val oldTemp = markerData.temperature
            val oldSound = markerData.soundLevel

            markerData.temperature = sensorData.temperature
            markerData.soundLevel = sensorData.soundLevel

            Log.d("MapFragment", "Marker data updated: temp $oldTemp -> ${markerData.temperature}, sound $oldSound -> ${markerData.soundLevel}")

            mapView.invalidate()
            Log.d("MapFragment", "✅ MapView invalidated - overlay should redraw now")
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

            var markersDrawn = 0

            eventMarkers.forEach { (eventId, markerData) ->
                val marker = markerData.marker
                val projection = mapView.projection
                val point = projection.toPixels(marker.position, null)

                val labels = mutableListOf<String>()

                markerData.temperature?.let { temp ->
                    labels.add("🌡️ ${String.format(java.util.Locale.US, "%.1f", temp)}°C")
                }

                markerData.soundLevel?.let { sound ->
                    labels.add("🔊 ${String.format(java.util.Locale.US, "%.0f", sound)} dB")
                }

                if (labels.isNotEmpty()) {
                    markersDrawn++
                    val lineHeight = 35f
                    val startY = point.y - 150f

                    labels.forEachIndexed { index, label ->
                        val yPos = startY - (index * lineHeight)

                        val textWidth = textPaint.measureText(label)
                        val padding = 12f

                        val left = point.x - (textWidth / 2) - padding
                        val right = point.x + (textWidth / 2) + padding
                        val top = yPos - 25f
                        val bottom = yPos + 8f

                        canvas.drawRoundRect(left, top, right, bottom, 8f, 8f, backgroundPaint)
                        canvas.drawText(label, point.x.toFloat(), yPos, textPaint)
                    }
                }
            }

            if (markersDrawn > 0) {
                Log.d("MapFragment", "SensorDataOverlay: Drawing $markersDrawn markers with sensor data")
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