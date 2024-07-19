package com.example.myapplication_wearable.presentation

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.example.myapplication_wearable.R
import com.example.myapplication_wearable.presentation.theme.MyApplicationWearableTheme
import java.util.concurrent.TimeUnit

class MainActivity : ComponentActivity() {
    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)

        // Solicita el permiso de notificaciones si no está concedido
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
            != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.POST_NOTIFICATIONS), 1)
        }

        setContent {
            MyApplicationWearableTheme {
                MainScreen()
            }
        }

        // Programar el WorkManager solo si se tiene el permiso de notificaciones
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
            == PackageManager.PERMISSION_GRANTED) {
            scheduleFirstNotificationWorker()
        }
    }

    private fun scheduleFirstNotificationWorker() {
        val notificationWorkRequest = OneTimeWorkRequestBuilder<NotificationWorker>()
            .setInitialDelay(1, TimeUnit.MINUTES)
            .build()

        WorkManager.getInstance(this)
            .enqueueUniqueWork(
                "NotificationWork",
                ExistingWorkPolicy.REPLACE,
                notificationWorkRequest
            )
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 1 && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            scheduleFirstNotificationWorker()
        }
    }
}

@RequiresApi(Build.VERSION_CODES.TIRAMISU)
@Composable
fun MainScreen() {
    val context = LocalContext.current

    Column(modifier = Modifier.padding(16.dp)) {
        Button(
            onClick = {
                // Acción del botón para enviar una notificación manualmente
                val randomChallenge = "Test Challenge"
                if (ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS)
                    == PackageManager.PERMISSION_GRANTED) {
                    val builder = NotificationCompat.Builder(context, "CHALLENGE_CHANNEL")
                        .setSmallIcon(R.drawable.ic_launcher)
                        .setContentTitle("Test Challenge")
                        .setContentText(randomChallenge)
                        .setPriority(NotificationCompat.PRIORITY_HIGH)

                    with(NotificationManagerCompat.from(context)) {
                        notify(System.currentTimeMillis().toInt(), builder.build())
                    }
                } else {
                    ActivityCompat.requestPermissions(
                        context as ComponentActivity,
                        arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                        1
                    )
                }
            },
            modifier = Modifier.padding(bottom = 8.dp)
        ) {
            Text(text = "Send Test Notification")
        }
    }
}