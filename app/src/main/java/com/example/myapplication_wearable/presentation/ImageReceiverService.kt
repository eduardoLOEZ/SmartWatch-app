package com.example.myapplication_wearable.presentation

import android.app.Service
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.example.myapplication_wearable.R
import com.google.android.gms.wearable.DataClient
import com.google.android.gms.wearable.DataEvent
import com.google.android.gms.wearable.DataEventBuffer
import com.google.android.gms.wearable.DataMapItem
import com.google.android.gms.wearable.Wearable
import java.io.ByteArrayInputStream

class ImageReceiverService : Service(), DataClient.OnDataChangedListener {

    override fun onCreate() {
        super.onCreate()
        Wearable.getDataClient(this).addListener(this)
    }

    override fun onDestroy() {
        super.onDestroy()
        Wearable.getDataClient(this).removeListener(this)
    }

    override fun onDataChanged(dataEvents: DataEventBuffer) {
        for (event in dataEvents) {
            if (event.type == DataEvent.TYPE_CHANGED && event.dataItem.uri.path == "/image") {
                val dataMap = DataMapItem.fromDataItem(event.dataItem).dataMap
                val imageData = dataMap.getByteArray("image")

                val bitmap = BitmapFactory.decodeStream(ByteArrayInputStream(imageData))

                // Aquí puedes usar el bitmap para mostrarlo o hacer lo que necesites con él
                showNotification(bitmap)
            }
        }
    }

    private fun showNotification(bitmap: Bitmap) {
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS)
            == PackageManager.PERMISSION_GRANTED) {

            val notificationBuilder = NotificationCompat.Builder(this, "IMAGE_CHANNEL")
                .setContentTitle("New Image")
                .setContentText("You've received a new image.")
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setLargeIcon(bitmap)
                .setPriority(NotificationCompat.PRIORITY_HIGH)

            with(NotificationManagerCompat.from(this)) {
                notify(System.currentTimeMillis().toInt(), notificationBuilder.build())
            }
        } else {
            Log.d("ImageReceiverService", "Permission not granted")
        }
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }
}
