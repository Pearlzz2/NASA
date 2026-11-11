package edu.xula.nasa

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import edu.xula.nasa.ui.theme.LightPurple
import edu.xula.nasa.ui.theme.NASATheme
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.get
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json

class MainActivity : ComponentActivity() {

    private val sampleData = listOf(
        ResponseData(
            date = "2024-03-15",
            title = "The Great Orion Nebula",
            credit = "NASA / ESA / Hubble",
            alt = "Orion Nebula in vivid colors",
            url = "https://apod.nasa.gov/apod/image/2403/Orion_Hubble_960.jpg",
            hdurl = "https://apod.nasa.gov/apod/image/2403/Orion_Hubble_960.jpg"
        ),
        ResponseData(
            date = "2024-03-14",
            title = "Carina Nebula Pillars",
            credit = "JWST / NASA",
            alt = "Pillars of creation in Carina",
            url = "https://apod.nasa.gov/apod/image/2403/Carina_JWST_960.jpg",
            hdurl = "https://apod.nasa.gov/apod/image/2403/Carina_JWST_960.jpg"
        ),
        ResponseData(
            date = "2024-03-13",
            title = "Andromeda Galaxy M31",
            credit = "Subaru Telescope / NAOJ",
            alt = "Spiral arms of Andromeda",
            url = "https://apod.nasa.gov/apod/image/2403/Andromeda_Subaru_960.jpg",
            hdurl = "https://apod.nasa.gov/apod/image/2403/Andromeda_Subaru_960.jpg"
        )
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            var itemsList by remember { mutableStateOf(sampleData) }
            val pagerState = rememberPagerState(pageCount = { itemsList.size })
            val coroutineScope = rememberCoroutineScope()

            LaunchedEffect(Unit) {
                withContext(Dispatchers.IO) {
                    try {
                        val fetched = getApodImage()
                        if (fetched.isNotEmpty()) {
                            itemsList = fetched
                        }
                    } catch (exception: Throwable) {
                        Log.e("NASA", "Network fetch failed, showing sample data", exception)
                    }
                }
            }

            NASATheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    Scaffold(
                        modifier = Modifier.fillMaxSize(),
                        containerColor = MaterialTheme.colorScheme.background
                    ) { innerPadding ->
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(innerPadding)
                                .padding(vertical = 16.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Text(
                                text = "Peyton",
                                style = MaterialTheme.typography.titleLarge,
                                color = LightPurple,
                                modifier = Modifier.padding(horizontal = 16.dp)
                            )

                            if (itemsList.isNotEmpty()) {
                                HorizontalPager(
                                    state = pagerState,
                                    modifier = Modifier.fillMaxWidth(),
                                    contentPadding = PaddingValues(horizontal = 16.dp),
                                    pageSpacing = 16.dp
                                ) { page ->
                                    val currentItem = itemsList[page]

                                    Card(
                                        modifier = Modifier.fillMaxWidth(),
                                        colors = CardDefaults.cardColors(
                                            containerColor = MaterialTheme.colorScheme.surface
                                        )
                                    ) {
                                        Column(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(16.dp),
                                            verticalArrangement = Arrangement.spacedBy(12.dp)
                                        ) {
                                            // Row 1: NASA Image
                                            AsyncImage(
                                                model = currentItem.hdurl.ifEmpty { currentItem.url },
                                                contentDescription = currentItem.alt.ifEmpty { currentItem.title },
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .height(220.dp),
                                                contentScale = ContentScale.Crop
                                            )

                                            // Row 2: Image's title
                                            Text(
                                                text = currentItem.title,
                                                style = MaterialTheme.typography.titleMedium,
                                                color = MaterialTheme.colorScheme.onSurface
                                            )

                                            // Row 3: Image's credit / author next to date
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween
                                            ) {
                                                Text(
                                                    text = "Author: ${currentItem.credit.ifEmpty { "NASA" }}",
                                                    style = MaterialTheme.typography.bodyMedium,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                                Text(
                                                    text = "Date: ${currentItem.date}",
                                                    style = MaterialTheme.typography.bodyMedium,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            }

                                            // Optional text: Image's index
                                            Text(
                                                text = "Image ${page + 1} of ${itemsList.size}",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.primary
                                            )

                                            // Row 4: "Random image" button
                                            Button(
                                                onClick = {
                                                    if (itemsList.size > 1) {
                                                        var newIndex: Int
                                                        do {
                                                            newIndex = itemsList.indices.random()
                                                        } while (newIndex == pagerState.currentPage)
                                                        coroutineScope.launch {
                                                            pagerState.animateScrollToPage(newIndex)
                                                        }
                                                    }
                                                },
                                                modifier = Modifier.fillMaxWidth()
                                            ) {
                                                Text("Random image")
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private suspend fun getApodImage(apiKey: String = "DEMO_KEY"): List<ResponseData> {
        val client = HttpClient(CIO) {
            install(ContentNegotiation) {
                json(Json { ignoreUnknownKeys = true })
            }
        }
        return try {
            client.get("https://science.nasa.gov/wp-json/wp/v2/apod-basic/?api_key=${apiKey}").body()
        } finally {
            client.close()
        }
    }
}
