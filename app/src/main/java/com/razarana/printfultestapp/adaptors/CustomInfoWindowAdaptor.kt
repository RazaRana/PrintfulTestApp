package com.razarana.printfultestapp.adaptors

import android.app.Activity
import android.content.Context
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.google.android.gms.maps.GoogleMap.InfoWindowAdapter
import com.google.android.gms.maps.model.Marker
import com.razarana.printfultestapp.R
import com.razarana.printfultestapp.models.User
import com.squareup.picasso.Picasso


class CustomInfoWindowAdaptor(private val context: Context) : InfoWindowAdapter {
    override fun getInfoWindow(marker: Marker): View? {
        return null
    }

    override fun getInfoContents(marker: Marker): View {
        //inflate the view with the adaptor
        val view: View = (context as Activity).layoutInflater
            .inflate(R.layout.info_window_layout, null)

        //get the reference to the fields
        val name = view.findViewById<TextView>(R.id.name)
        val address = view.findViewById<TextView>(R.id.address)
        val img = view.findViewById<ImageView>(R.id.pic)


        //get the tagged user with each
        val infoWindowData: User? = marker.tag as User?


        //assign the view fields with the data
        name.text = infoWindowData!!.userName
        address.text = infoWindowData.lat.toString()+" "+infoWindowData.lng.toString()

        //loading image from internet
        Picasso.get().setIndicatorsEnabled(true)
        Picasso.get().isLoggingEnabled=true
        Picasso.get()
            .load(infoWindowData.image) //optional
            .resize(100, 100)         //optional
            .centerCrop().into(img)

        return view
    }

}