package com.razarana.printfultestapp

import android.os.AsyncTask
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.razarana.printfultestapp.TcpClient.OnMessageReceived


class MapsActivity : AppCompatActivity(), OnMapReadyCallback,GoogleMap.OnMarkerClickListener{

    private lateinit var mMap: GoogleMap
    private var tcpClient: TcpClient?=null
    private var arrayList: ArrayList<User>? = ArrayList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
                .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }


    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        mMap.setOnMarkerClickListener(this)


        mMap.setInfoWindowAdapter(CustomInfoWindowAdaptor(this));

        ConnectTask().execute("")
    }

    override fun onMarkerClick(p0: Marker?)= false

    private fun placeMarkerOnMap(location: LatLng,index:Int) {





        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(location,15.0f))

        val markerOptions = MarkerOptions()
        markerOptions.position(location);



        this!!.arrayList?.get(index)!!.marker=mMap.addMarker(MarkerOptions().position(location))
        this!!.arrayList?.get(index)!!.marker!!.tag=this!!.arrayList?.get(index)!!


    }

    private fun updateMarkerPosition(newLocation: LatLng,index:Int) {

        MarkerAnimation.animateMarkerToICS(
            this!!.arrayList!![index].marker, newLocation,
            LatLngInterpolator.Spherical()
        )
    }

    private fun setUpMap() {

        for (x in 0 until arrayList!!.size){
            var latLng=LatLng(arrayList!![x].lat,arrayList!![x].lng)
            placeMarkerOnMap(latLng,x)


        }


    }


    inner class ConnectTask : AsyncTask<String?, String?, TcpClient?>() {
        override fun doInBackground(vararg p0: String?): TcpClient? {

            //we create a TCPClient object and

            tcpClient = TcpClient(object : OnMessageReceived {
                //here the messageReceived method is implemented
                override fun messageReceived(message: String?) {
                    //this method calls the onProgressUpdate

                    publishProgress(message)
                }
            })
            tcpClient!!.run()
            return null
        }

        override fun onProgressUpdate(vararg values: String?) {
            super.onProgressUpdate(*values)


            try {
                if (values[0]!!.contains("USERLIST")){

                    var finalStr=values[0]!!.replace("USERLIST ","").trim()
                    val users=finalStr!!.split(";")
                    for (str in users){
                        if (str.contains(",")) {
                            val userDetails=str.split(",")
                            val tempUser=User(
                                id = userDetails[0],
                                userName = userDetails[1],
                                image = userDetails[2],
                                lat = userDetails[3].toDouble(),
                                lng = userDetails[4].toDouble())
                            arrayList!!.add(tempUser)
                        }
                    }


                    setUpMap()


                }

                else if (values[0]!!.contains("UPDATE")){
                    var finalStr=values[0]!!.replace("UPDATE ","")
                    if (finalStr!!.contains(",")) {
                        Log.e("test", "${values[0]}");
                        val userUpdates=finalStr!!.split(",")
                        var currentUserIndex=arrayList!!.indexOfFirst { user-> user.id == userUpdates[0] }
                        arrayList!![currentUserIndex].lat=userUpdates[1].toDouble()
                        arrayList!![currentUserIndex].lng=userUpdates[2].toDouble()
                        var newLocation=LatLng(arrayList!![currentUserIndex].lat,arrayList!![currentUserIndex].lng)
                        updateMarkerPosition(newLocation,currentUserIndex )
                    }


                }
            } catch (e: Exception) {
                e.printStackTrace()
            }


        }




    }
}
