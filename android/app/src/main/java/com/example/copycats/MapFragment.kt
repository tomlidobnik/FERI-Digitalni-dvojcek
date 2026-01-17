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


private lateinit var directionIcon: Drawable
class MapFragment : Fragment() {

    private lateinit var mapView: MapView
    private lateinit var locationOverlay: MyLocationNewOverlay

    // Cache marker icons to avoid repeated decoding
    private val markerIconCache = mutableMapOf<String, Drawable>()

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

        // Add event markers to the map
        addEventMarkers()

        checkLocationPermission()
    }

    private fun addEventMarkers() {
        Log.d("MapFragment", "Adding ${MyApplication.events.size} event markers to map")

        // Pre-load and cache marker icons once
        if (markerIconCache.isEmpty()) {
            try {
                markerIconCache["education"] = ContextCompat.getDrawable(requireContext(), R.drawable.baseline_school_24)!!
                markerIconCache["fun"] = ContextCompat.getDrawable(requireContext(), R.drawable.baseline_celebration_24)!!
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
                marker.snippet = """
                    ${event.description}
                    
                    Start: ${event.start_date}
                    End: ${event.end_date}
                    Tag: ${event.tag ?: "None"}
                """.trimIndent()

                // Use cached marker icon based on tag
                val iconKey = event.tag?.lowercase() ?: "default"
                val markerIcon = markerIconCache[iconKey] ?: markerIconCache["default"]

                markerIcon?.let {
                    marker.icon = it
                }

                // Set marker anchor to center bottom
                marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)

                // Add click listener to open event detail fragment
                marker.setOnMarkerClickListener { clickedMarker, _ ->
                    // Navigate to event detail fragment using activity's fragment manager
                    val eventDetailFragment = EventDetailFragment.newInstance(event.id)
                    requireActivity().supportFragmentManager.beginTransaction()
                        .replace(R.id.fragmentContainerView, eventDetailFragment)
                        .addToBackStack(null)
                        .commit()
                    true
                }

                mapView.overlays.add(marker)
                Log.d("MapFragment", "Added marker for event: ${event.title} at (${location.latitude}, ${location.longitude})")
            } else {
                Log.w("MapFragment", "No location found for event #${event.id}: ${event.title} (location_fk: ${event.location_fk})")
            }
        }

        mapView.invalidate()
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