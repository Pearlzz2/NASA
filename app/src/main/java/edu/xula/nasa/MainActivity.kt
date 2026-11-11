package edu.xula.nasa

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import edu.xula.nasa.ui.theme.LightPurple
import edu.xula.nasa.ui.theme.NASATheme
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.get
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            var response by remember { mutableStateOf<ResponseData?>(null) }
            var serverResponse by remember { mutableStateOf("Loading...") }
            Log.d("xavier", serverResponse)
            LaunchedEffect(true) {
                withContext(Dispatchers.IO) {
                    serverResponse = try {
                        val list = getApodImage()
                        val item = list[(0..24).random()]
                        response = item
                        println(">>>>> Raw JSON: $item")
                        item.toString()
                    } catch (exception: Exception) {
                        "Error: $exception"
                    }
                }
            }

            NASATheme {
                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    containerColor = MaterialTheme.colorScheme.background
                ) { innerPadding ->
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                            .padding(16.dp)
                    ) {
                        Text(
                            text = "Peyton",
                            style = MaterialTheme.typography.titleLarge,
                            color = LightPurple,
                            modifier = Modifier.padding(bottom = 16.dp)
                        )
                        DisplayImageFromUrl(response?.hdurl)
                    }
                }
            }
        }
    }

    private val client = HttpClient(CIO) {
        install(ContentNegotiation) {
            json(Json { ignoreUnknownKeys = true })
        }
    }

    private suspend fun getApodImage(api_key: String = "DEMO_KEY"): List<ResponseData> = client.get(
        "https://science.nasa.gov/wp-json/wp/v2/apod-basic/?api_key=${api_key}"
    ) {
        url {
            // parameter("date", date) // YYYY-MM-DD
        }
    }.body()
}

@Composable
fun DisplayImageFromUrl(url: String?) {
    if (url != null) {
        Text(text = url, color = MaterialTheme.colorScheme.onBackground)
    }
}
