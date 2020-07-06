package com.razarana.printfultestapp

import android.util.Log
import java.io.*
import java.net.InetAddress
import java.net.Socket


class TcpClient(listener: OnMessageReceived?) {

    private var serverMessage: String? = null
    private var messageListener: OnMessageReceived? = null


    private var running = false


    private var bufferOut: PrintWriter? = null


    private var bufferIn: BufferedReader? = null

    fun sendMessage(message: String?) {
        if (bufferOut != null && !bufferOut!!.checkError()) {
            bufferOut!!.println(message)
            bufferOut!!.flush()
        }
    }


    fun stopClient() {


        sendMessage("")
        running = false
        if (bufferOut != null) {
            bufferOut!!.flush()
            bufferOut!!.close()
        }
        messageListener = null
        bufferIn = null
        bufferOut = null
        serverMessage = null
    }

    fun run() {
        running = true
        try {

            val serverAddress =
                InetAddress.getByName("ios-test.printful.lv")
            Log.e("TCP Client", "C: Connecting...")


            val socket =
                Socket(serverAddress, 6111)


            try {


                bufferOut = PrintWriter(
                    BufferedWriter(OutputStreamWriter(socket.getOutputStream())),
                    true
                )


                bufferIn =
                    BufferedReader(InputStreamReader(socket.getInputStream()))

                sendMessage("AUTHORIZE <raza.rana@hotmail.com>")

                //in this while the client listens for the messages sent by the server
                while (running) {
                    serverMessage = bufferIn!!.readLine()
                    if (serverMessage != null && messageListener != null) {
                        //call the method messageReceived from MyActivity class
                        messageListener!!.messageReceived(serverMessage)
                    }
                }
                Log.e(
                    "RESPONSE FROM SERVER",
                    "S: Received Message: '$serverMessage'"
                )
            } catch (e: Exception) {
                Log.e("TCP", "S: Error", e)
            } finally {
                //the socket must be closed. It is not possible to reconnect to this socket
                // after it is closed, which means a new socket instance has to be created.
                socket.close()
            }
        } catch (e: Exception) {
            Log.e("TCP", "C: Error", e)
        }
    }

    //Declare the interface. The method messageReceived(String message) will must be implemented in the MyActivity
    //class at on asynckTask doInBackground
    interface OnMessageReceived {
        fun messageReceived(message: String?)
    }

    companion object {
        const val SERVER_IP = "ios-test.printful.lv" //your computer IP address
        const val SERVER_PORT = 6111
    }


    init {
        messageListener = listener
    }
}