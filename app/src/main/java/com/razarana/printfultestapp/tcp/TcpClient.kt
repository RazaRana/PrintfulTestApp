package com.razarana.printfultestapp.tcp

import java.io.*
import java.net.InetAddress
import java.net.Socket


class TcpClient(listener: OnMessageReceived?) {

    //server message string
    private var serverMessage: String? = null
    private var messageListener: OnMessageReceived? = null

    private var running = false


    //buffers for server writing and reading
    private var bufferOut: PrintWriter? = null


    private var bufferIn: BufferedReader? = null

    fun sendMessage(message: String?) {
        //write on buffer out to send message every time
        if (bufferOut != null && !bufferOut!!.checkError()) {
            bufferOut!!.println(message)
            bufferOut!!.flush()
        }
    }




    fun run() {
        running = true
        try {
            //server ip
            val serverAddress =
                InetAddress.getByName(SERVER_IP)

            //server port
            //creating socket and connection established
            val socket =
                Socket(serverAddress,
                    SERVER_PORT
                )


            try {

                //initializing buffers
                bufferOut = PrintWriter(
                    BufferedWriter(OutputStreamWriter(socket.getOutputStream())),
                    true
                )


                bufferIn =
                    BufferedReader(InputStreamReader(socket.getInputStream()))


                sendMessage(AUTH_MSG)

                //in this while the client listens for the messages sent by the server
                while (running) {
                    serverMessage = bufferIn!!.readLine()
                    if (serverMessage != null && messageListener != null) {
                        //call the method messageReceived from MyActivity class
                        messageListener!!.messageReceived(serverMessage)
                    }
                }

            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                //the socket must be closed. It is not possible to reconnect to this socket
                // after it is closed, which means a new socket instance has to be created.
                socket.close()
            }
        } catch (e: Exception) {

        }
    }

    //Declare the interface. The method messageReceived(String message) will must be implemented in the MyActivity
    //class at on asynckTask doInBackground
    interface OnMessageReceived {
        fun messageReceived(message: String?)
    }

    //hardcoded data for test
    companion object {
        const val SERVER_IP = "ios-test.printful.lv" //your computer IP address
        const val SERVER_PORT = 6111
        const val AUTH_MSG="AUTHORIZE <raza.rana@hotmail.com>"
    }


    init {
        messageListener = listener
    }
}