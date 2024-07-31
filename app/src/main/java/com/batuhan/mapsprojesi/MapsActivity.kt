package com.batuhan.mapsprojesi

import android.Manifest
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.batuhan.mapsprojesi.databinding.ActivityMapsBinding
import com.google.android.material.snackbar.Snackbar
import java.util.Locale

class MapsActivity : AppCompatActivity(), OnMapReadyCallback,GoogleMap.OnMapLongClickListener {

    private lateinit var mMap: GoogleMap
    private lateinit var binding: ActivityMapsBinding
    private lateinit var locationManager : LocationManager
    private lateinit var locationListener : LocationListener
    private lateinit var permissionLauncher : ActivityResultLauncher<String>

    var takipBoolean : Boolean?=null
    private lateinit var sharedPreferences : SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMapsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        registerLauncher()

        sharedPreferences =getSharedPreferences("com.batuhan.mapsprojesi", MODE_PRIVATE)
        takipBoolean = false
    }



    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        mMap.setOnMapLongClickListener(this@MapsActivity)
        /*
        // Add a marker in Sydney and move the camera
        val btkAkademi = LatLng(39.91204717559683, 32.810538220275745)
        mMap.addMarker(MarkerOptions().position(btkAkademi).title("BTK Akademi"))
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(btkAkademi,14f))
        */

        locationManager = this.getSystemService(LOCATION_SERVICE) as LocationManager
        locationListener = object:LocationListener{
            override fun onLocationChanged(location: Location) {
                if (!takipBoolean!!){
                    mMap.clear()
                    val kullaniciKonumu = LatLng(location.latitude,location.longitude)
                    mMap.addMarker(MarkerOptions().position(kullaniciKonumu).title("Konumunuz!"))
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(kullaniciKonumu,14f))
                    sharedPreferences.edit().putBoolean("takipBoolean",true).apply()
                }


            }

        }
        if(ContextCompat.checkSelfPermission(this,Manifest.permission.ACCESS_FINE_LOCATION)!=PackageManager.PERMISSION_GRANTED){
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,Manifest.permission.ACCESS_FINE_LOCATION)){
                Snackbar.make(binding.root,"Konumunu Almak için izin gerekli",Snackbar.LENGTH_INDEFINITE).setAction(
                    "İzin Ver!"
                ){
                    //izin isteyeceğiz
                    permissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
                }.show()

            }else{
                //izin isteyeceğiz
                permissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
            }
        }else{
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,0,0f,locationListener)
            val sonBilinenKonum = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
            if (sonBilinenKonum !=null){
                val sonBilinenLatLng = LatLng(sonBilinenKonum.latitude,sonBilinenKonum.longitude)
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(sonBilinenLatLng,14f))
            }
        }


    }

    private fun registerLauncher() {
        permissionLauncher =
            registerForActivityResult(ActivityResultContracts.RequestPermission()) { result ->
                if (result) {
                    if (ContextCompat.checkSelfPermission(
                            this@MapsActivity,
                            Manifest.permission.ACCESS_FINE_LOCATION
                        ) == PackageManager.PERMISSION_GRANTED
                    ) {
                        locationManager.requestLocationUpdates(
                            LocationManager.GPS_PROVIDER,
                            0,
                            0f,
                            locationListener
                        )
                        val sonBilinenKonum =
                            locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
                        if (sonBilinenKonum != null) {
                            val sonBilinenLatLng =
                                LatLng(sonBilinenKonum.latitude, sonBilinenKonum.longitude)
                            mMap.moveCamera(
                                CameraUpdateFactory.newLatLngZoom(
                                    sonBilinenLatLng,
                                    14f
                                )
                            )
                        }
                    }
                }else{
                    Toast.makeText(this@MapsActivity,"İzine İhtiyacımız var!",Toast.LENGTH_LONG).show()
                }
            }
    }



    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onMapLongClick(p0: LatLng) {
        mMap.clear()
        //gecoder

        val gecoder = Geocoder(this,Locale.getDefault())

        var adress =""

        try {
            gecoder.getFromLocation(p0.latitude,p0.longitude,1,Geocoder.GeocodeListener{adresListesi->
                val ilkAdres = adresListesi.first()
                val ulkeAdi = ilkAdres.countryName
                val sokak = ilkAdres.thoroughfare
                val numara = ilkAdres.subThoroughfare
                adress+= sokak
                adress += numara
                println(adress)
            })
        }catch (e : Exception ){
            println(e.printStackTrace())
        }
    }

}