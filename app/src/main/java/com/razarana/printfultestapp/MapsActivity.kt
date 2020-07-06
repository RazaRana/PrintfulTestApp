package com.razarana.printfultestapp

import android.os.AsyncTask
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.razarana.printfultestapp.adaptors.CustomInfoWindowAdaptor
import com.razarana.printfultestapp.tcp.TcpClient.OnMessageReceived
import com.razarana.printfultestapp.animations.LatLngInterpolator
import com.razarana.printfultestapp.animations.MarkerAnimation
import com.razarana.printfultestapp.models.User
import com.razarana.printfultestapp.tcp.TcpClient


class MapsActivity : AppCompatActivity(), OnMapReadyCallback,GoogleMap.OnMarkerClickListener{

    //global map object reference
    private lateinit var mMap: GoogleMap
    //tcp client object reference
    private var tcpClient: TcpClient?=null
    //array list to keep the list of users
    private var usersList: ArrayList<User>? = ArrayList()

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

        //on click listener to popup info window on user click on marker
        mMap.setOnMarkerClickListener(this)



        //attaching custom window adaptor to the map
        mMap.setInfoWindowAdapter(
            CustomInfoWindowAdaptor(
                this
            )
        );

        //starting the async tcp communication with the server
        ConnectTask().execute("")
    }

    override fun onMarkerClick(p0: Marker?)= false

    private fun placeMarkerOnMap(location: LatLng,index:Int) {
        //animate camera to the location received from the server
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(location,15.0f))

        //placing a marker on the location received from the server
        val markerOptions = MarkerOptions()
        markerOptions.position(location);



        //adding marker to the user object for future reference
        this!!.usersList?.get(index)!!.marker=mMap.addMarker(markerOptions)

        //tagging the marker with the user class for the adaptor
        this!!.usersList?.get(index)!!.marker!!.tag=this!!.usersList?.get(index)!!


    }

    private fun updateMarkerPosition(newLocation: LatLng,index:Int) {

        //animate marker to the updated user location from the server
        MarkerAnimation.animateMarkerToICS(
            this!!.usersList!![index].marker, newLocation,
            LatLngInterpolator.Spherical()
        )
    }

    private fun setUpMap() {

        //place marker for each user received from the server
        for (x in 0 until usersList!!.size){
            var latLng=LatLng(usersList!![x].lat,usersList!![x].lng)
            placeMarkerOnMap(latLng,x)


        }


    }


    inner class ConnectTask : AsyncTask<String?, String?, TcpClient?>() {

        //async task running in background to comunicate with the server
        override fun doInBackground(vararg p0: String?): TcpClient? {

            //we create a TCPClient object

            tcpClient = TcpClient(object :
                OnMessageReceived {
                //here the messageReceived method is implemented
                override fun messageReceived(message: String?) {
                    //this method calls the onProgressUpdate
                    publishProgress(message)
                }
            })
            tcpClient!!.run()
            return null
        }

        //on progress update will receive the message from do in background function
        override fun onProgressUpdate(vararg values: String?) {
            super.onProgressUpdate(*values)


            try {
                //if received message from server contain word userlist
                if (values[0]!!.contains("USERLIST")){

                    //that means that communication is starting and connection has established
                    //we remove this key word from the message
                    var finalStr=values[0]!!.replace("USERLIST ","").trim()

                    //and split the remaining message to get the each user separated
                    val users=finalStr!!.split(";")
                    for (str in users){
                        //further split each user data to the attributes
                        if (str.contains(",")) {
                            val userDetails=str.split(",")
                            //assign user detail to the user object
                            val tempUser=
                                User(
                                    id = userDetails[0],
                                    userName = userDetails[1],
                                    image = userDetails[2],
                                    lat = userDetails[3].toDouble(),
                                    lng = userDetails[4].toDouble()
                                )

                            //add the user to the list
                            usersList!!.add(tempUser)
                        }
                    }


                    setUpMap()


                }

                //if the message contain the update keyword that means that the connection is already running with the server and we have to just update the new user location
                else if (values[0]!!.contains("UPDATE")){
                    var finalStr=values[0]!!.replace("UPDATE ","")
                    //split message on comma
                    if (finalStr!!.contains(",")) {
                        val userUpdates=finalStr!!.split(",")
                        //find user with user id
                        var currentUserIndex=usersList!!.indexOfFirst { user-> user.id == userUpdates[0] }

                        //assign new updated lat and lng to them
                        usersList!![currentUserIndex].lat=userUpdates[1].toDouble()
                        usersList!![currentUserIndex].lng=userUpdates[2].toDouble()

                        var newLocation=LatLng(usersList!![currentUserIndex].lat,usersList!![currentUserIndex].lng)

                        //animate marker to new location
                        updateMarkerPosition(newLocation,currentUserIndex )
                    }


                }
            } catch (e: Exception) {
                e.printStackTrace()
            }


        }




    }
}
