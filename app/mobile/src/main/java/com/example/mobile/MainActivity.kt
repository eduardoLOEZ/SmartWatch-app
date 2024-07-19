package com.example.mobile

import android.graphics.Bitmap
import android.os.Bundle
import android.provider.MediaStore
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.mobile.ui.theme.MiAplicacionWearableTheme
import com.google.android.gms.tasks.Task
import com.google.android.gms.wearable.*

import java.io.ByteArrayOutputStream

class MainActivity : ComponentActivity() {

    private val takePictureLauncher = registerForActivityResult(ActivityResultContracts.TakePicturePreview()) { bitmap ->
        bitmap?.let {
            sendImageToWearable(it)
        }
    }

    private val pickImageLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let {
            val bitmap = MediaStore.Images.Media.getBitmap(this.contentResolver, it)
            sendImageToWearable(bitmap)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MiAplicacionWearableTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    MainScreen(
                        modifier = Modifier.padding(innerPadding),
                        onTakePictureClick = { takePictureLauncher.launch(null) },
                        onPickImageClick = { pickImageLauncher.launch("image/*") }
                    )
                }
            }
        }
    }

    private fun sendImageToWearable(bitmap: Bitmap) {
        val nodeClient: NodeClient = Wearable.getNodeClient(this)
        val nodesTask: Task<List<Node>> = nodeClient.connectedNodes
        nodesTask.addOnSuccessListener { nodes ->
            for (node in nodes) {
                val byteArrayOutputStream = ByteArrayOutputStream()
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream)
                val byteArray = byteArrayOutputStream.toByteArray()

                val putDataMapRequest = PutDataMapRequest.create("/image")
                putDataMapRequest.dataMap.putByteArray("image", byteArray)
                val putDataRequest = putDataMapRequest.asPutDataRequest().setUrgent()

                Wearable.getDataClient(this).putDataItem(putDataRequest)
            }
        }
    }
}

@Composable
fun MainScreen(modifier: Modifier = Modifier, onTakePictureClick: () -> Unit, onPickImageClick: () -> Unit) {
    Column(modifier = modifier.padding(16.dp)) {
        Button(onClick = onTakePictureClick, modifier = Modifier.padding(bottom = 8.dp)) {
            Text(text = "Take Picture")
        }
        Button(onClick = onPickImageClick, modifier = Modifier.padding(bottom = 8.dp)) {
            Text(text = "Pick Image")
        }
    }
}

@Preview(showBackground = true)
@Composable
fun MainScreenPreview() {
    MiAplicacionWearableTheme {
        MainScreen(onTakePictureClick = {}, onPickImageClick = {})
    }
}
