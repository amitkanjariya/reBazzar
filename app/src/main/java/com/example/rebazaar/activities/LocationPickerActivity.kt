package com.example.rebazaar.activities

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.location.Geocoder
import android.location.Location
import android.location.LocationManager
import android.os.Bundle
import android.view.View
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.example.rebazaar.R
import com.example.rebazaar.Utils
import com.example.rebazaar.databinding.ActivityLocationPickerBinding
import com.google.android.gms.common.api.Status
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.net.PlacesClient
import com.google.android.libraries.places.widget.AutocompleteSupportFragment
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener

class LocationPickerActivity : AppCompatActivity(), OnMapReadyCallback {
    private lateinit var binding: ActivityLocationPickerBinding
    private companion object{
        private const val DEFAULT_ZOOM = 15
    }
    private var mMap: GoogleMap? = null

    //current place picker
    private var mPlaceClient: PlacesClient? = null
    private var mFusedLocationProviderClient: FusedLocationProviderClient? = null

    //the geographical location where the device is currently located. That is, the last-known location retrieved by Fused Location Provider.
    private var mLastKnownLocation: Location? = null
    private var selectedLatitude: Double? = null
    private var selectedLongitude: Double? = null
    private var selectedAddress = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLocationPickerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.doneLl.visibility = View.GONE
        val mapFragment = supportFragmentManager.findFragmentById(R.id.mapFragment) as SupportMapFragment
        mapFragment.getMapAsync(this)

        Places.initialize(this, getString(R.string.map_api_key))

        mPlaceClient = Places.createClient(this)
        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)

        val autoCompleteSupportMapFragment = supportFragmentManager.findFragmentById(R.id.autoComplete_fragment) as AutocompleteSupportFragment

        val placeList = arrayOf(Place.Field.ID, Place.Field.NAME, Place.Field.ADDRESS, Place.Field.LAT_LNG)

        autoCompleteSupportMapFragment.setOnPlaceSelectedListener(object : PlaceSelectionListener{
            override fun onPlaceSelected(place: Place) {
                val id = place.id
                val name = place.name
                val latLng = place.latLng
                selectedLatitude = latLng?.latitude
                selectedLongitude = latLng?.longitude
                selectedAddress = place.address ?: ""

                addMarker(latLng, name, selectedAddress)
            }

            override fun onError(p0: Status) {

            }
        })
        binding.toolbarBackBtn.setOnClickListener{
            onBackPressed()
        }
        //if GPS enabled get and show user's current location
        binding.toolbarGpsBtn.setOnClickListener{
            if(isGPSEnabled()){
                requestLocationPermission.launch(Manifest.permission.ACCESS_FINE_LOCATION)
            } else{
                Utils.toast(this, "Location is not on! Turn it on to show current Location")
            }
        }

        binding.doneBtn.setOnClickListener{
            val intent = Intent()
            intent.putExtra("latitude", selectedLatitude)
            intent.putExtra("longitude", selectedLongitude)
            intent.putExtra("address", selectedAddress)
            setResult(Activity.RESULT_OK, intent)
            finish()
        }
    }

    override fun onMapReady(googleMap: GoogleMap){
        mMap = googleMap
        requestLocationPermission.launch(Manifest.permission.ACCESS_FINE_LOCATION)

        mMap!!.setOnMapClickListener { latLng ->
            selectedLatitude = latLng.latitude
            selectedLongitude = latLng.longitude

            //function call to get the address details from the latLng
            addressFromLatLng(latLng)
        }
    }

    @SuppressLint("MissingPermission")
    private val requestLocationPermission : ActivityResultLauncher<String> =
        registerForActivityResult(ActivityResultContracts.RequestPermission()){ isGranted ->
            if(isGranted){
                mMap!!.isMyLocationEnabled=true
                pickCurrentPlace()
            } else{
                Utils.toast(this, "Permission denied")
            }
        }

    private fun addressFromLatLng(latLng: LatLng){
        val geocoder = Geocoder(this)
        val addressList = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1)
        val address = addressList!![0]
        val addressLine = address.getAddressLine(0)
        val subLocality = address.subLocality
        selectedAddress = "$addressLine"
        addMarker(latLng, "$subLocality", "$addressLine")
    }

    private fun pickCurrentPlace(){
        if(mMap==null) {
            return
        }
        detectAndShowDeviceLocationMap()
    }

    //get the current location of the device, and position the map's camera
    @SuppressLint("MissingPermission")
    private fun detectAndShowDeviceLocationMap(){
        val locationResult = mFusedLocationProviderClient!!.lastLocation
        locationResult.addOnSuccessListener { location ->
            if(location != null){
                mLastKnownLocation = location
                selectedLatitude = location.latitude
                selectedLongitude = location.longitude

                val latLng = LatLng(selectedLatitude!!, selectedLongitude!!)
                mMap!!.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, DEFAULT_ZOOM.toFloat()))
                mMap!!.animateCamera(CameraUpdateFactory.zoomTo(DEFAULT_ZOOM.toFloat()))

                addressFromLatLng(latLng)
            }
        } .addOnFailureListener{ e ->
            Utils.toast(this, "fail to detect and show $e")
        }
    }

    private fun isGPSEnabled() : Boolean{
        val lm = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        var gpsEnabled = false
        var networkEnabled = false
        gpsEnabled = lm.isProviderEnabled(LocationManager.GPS_PROVIDER)
        networkEnabled = lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER)

        return !(!gpsEnabled && !networkEnabled)
    }

    private fun addMarker(latLng: LatLng, title: String, address:String){
        mMap!!.clear()
        val markerOptions = MarkerOptions()
        markerOptions.position(latLng)
        markerOptions.title("$title")
        markerOptions.snippet("$address")
        markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN))

        mMap!!.addMarker(markerOptions)
        mMap!!.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, DEFAULT_ZOOM.toFloat()))
        binding.doneLl.visibility = View.VISIBLE
        binding.selectedPlaceTv.text = address
    }
}
