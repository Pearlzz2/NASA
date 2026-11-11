package edu.xula.nasa

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Work
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import edu.xula.nasa.ui.theme.LightPurple
import edu.xula.nasa.ui.theme.NASATheme
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.cio.CIO
import io.ktor.client.request.get
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

data class CensusStateData(
    val name: String,
    val medianIncome: Double,
    val employmentRate: Double,
    val bachelorsRate: Double
)

class MainActivity : ComponentActivity() {

    private val fallbackCensusData = listOf(
        CensusStateData("Louisiana", 57852.0, 56.4, 26.4),
        CensusStateData("Texas", 73035.0, 62.1, 33.1),
        CensusStateData("California", 91905.0, 61.5, 36.2),
        CensusStateData("New York", 81386.0, 59.8, 38.5),
        CensusStateData("Florida", 67917.0, 57.2, 31.5),
        CensusStateData("Georgia", 71355.0, 61.2, 33.6),
        CensusStateData("Illinois", 78241.0, 61.9, 36.8),
        CensusStateData("Washington", 90325.0, 62.8, 38.4),
        CensusStateData("Colorado", 87598.0, 65.4, 42.8),
        CensusStateData("Alabama", 59609.0, 55.1, 27.4),
        CensusStateData("Mississippi", 52985.0, 53.2, 23.2),
        CensusStateData("Ohio", 66990.0, 60.3, 30.7),
        CensusStateData("Pennsylvania", 73170.0, 59.6, 33.1),
        CensusStateData("North Carolina", 66186.0, 59.9, 33.4),
        CensusStateData("Virginia", 87249.0, 63.1, 40.3)
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            var censusList by remember { mutableStateOf(fallbackCensusData) }
            var selectedState1 by remember { mutableStateOf("Louisiana") }
            var selectedState2 by remember { mutableStateOf("Texas") }

            LaunchedEffect(Unit) {
                withContext(Dispatchers.IO) {
                    try {
                        val fetched = fetchCensusData()
                        if (fetched.isNotEmpty()) {
                            censusList = fetched
                        }
                    } catch (e: Throwable) {
                        Log.e("Census", "Fetch failed, using fallback census data", e)
                    }
                }
            }

            val stateA = censusList.find { it.name.equals(selectedState1, ignoreCase = true) }
                ?: censusList.firstOrNull() ?: fallbackCensusData[0]
            val stateB = censusList.find { it.name.equals(selectedState2, ignoreCase = true) }
                ?: censusList.getOrNull(1) ?: fallbackCensusData[1]

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
                                .padding(16.dp)
                                .verticalScroll(rememberScrollState()),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Text(
                                text = "Peyton",
                                style = MaterialTheme.typography.titleLarge,
                                color = LightPurple
                            )

                            Text(
                                text = "Census.gov State Comparison",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onBackground
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                StateDropdown(
                                    label = "State 1",
                                    selectedState = selectedState1,
                                    statesList = censusList.map { it.name }.sorted(),
                                    onStateSelected = { selectedState1 = it },
                                    modifier = Modifier.weight(1f)
                                )
                                StateDropdown(
                                    label = "State 2",
                                    selectedState = selectedState2,
                                    statesList = censusList.map { it.name }.sorted(),
                                    onStateSelected = { selectedState2 = it },
                                    modifier = Modifier.weight(1f)
                                )
                            }

                            RateComparisonCard(
                                title = "Employment Rate",
                                icon = Icons.Default.Work,
                                valueA = "${stateA.employmentRate}%",
                                valueB = "${stateB.employmentRate}%",
                                isAHigher = stateA.employmentRate > stateB.employmentRate,
                                isBHigher = stateB.employmentRate > stateA.employmentRate,
                                stateAName = stateA.name,
                                stateBName = stateB.name
                            )

                            RateComparisonCard(
                                title = "Median Household Income",
                                icon = Icons.Default.AttachMoney,
                                valueA = "$${"%,.0f".format(stateA.medianIncome)}",
                                valueB = "$${"%,.0f".format(stateB.medianIncome)}",
                                isAHigher = stateA.medianIncome > stateB.medianIncome,
                                isBHigher = stateB.medianIncome > stateA.medianIncome,
                                stateAName = stateA.name,
                                stateBName = stateB.name
                            )

                            RateComparisonCard(
                                title = "Bachelor's Degree or Higher",
                                icon = Icons.Default.School,
                                valueA = "${stateA.bachelorsRate}%",
                                valueB = "${stateB.bachelorsRate}%",
                                isAHigher = stateA.bachelorsRate > stateB.bachelorsRate,
                                isBHigher = stateB.bachelorsRate > stateA.bachelorsRate,
                                stateAName = stateA.name,
                                stateBName = stateB.name
                            )
                        }
                    }
                }
            }
        }
    }

    private suspend fun fetchCensusData(): List<CensusStateData> {
        val client = HttpClient(CIO)
        return try {
            val response: List<List<String>> = client.get(
                "https://api.census.gov/data/2022/acs/acs5/profile?get=NAME,DP03_0062E,DP03_0004PE,DP02_0068PE&for=state:*"
            ).body()

            if (response.size > 1) {
                response.drop(1).mapNotNull { row ->
                    val name = row.getOrNull(0) ?: return@mapNotNull null
                    val income = row.getOrNull(1)?.toDoubleOrNull() ?: 0.0
                    val empRate = row.getOrNull(2)?.toDoubleOrNull() ?: 0.0
                    val bachRate = row.getOrNull(3)?.toDoubleOrNull() ?: 0.0
                    CensusStateData(name, income, empRate, bachRate)
                }
            } else emptyList()
        } finally {
            client.close()
        }
    }
}

@Composable
fun StateDropdown(
    label: String,
    selectedState: String,
    statesList: List<String>,
    onStateSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }

    Box(modifier = modifier) {
        OutlinedTextField(
            value = selectedState,
            onValueChange = {},
            readOnly = true,
            label = { Text(label) },
            trailingIcon = {
                Icon(
                    imageVector = Icons.Default.ArrowDropDown,
                    contentDescription = "Expand state list"
                )
            },
            modifier = Modifier
                .fillMaxWidth()
                .clickable { expanded = true }
        )
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            statesList.forEach { state ->
                DropdownMenuItem(
                    text = { Text(state) },
                    onClick = {
                        onStateSelected(state)
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
fun RateComparisonCard(
    title: String,
    icon: ImageVector,
    valueA: String,
    valueB: String,
    isAHigher: Boolean,
    isBHigher: Boolean,
    stateAName: String,
    stateBName: String
) {
    val highlightColor = Color(0xFF2E7D32)

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
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stateAName,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = valueA,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = if (isAHigher) FontWeight.Bold else FontWeight.Normal,
                        color = if (isAHigher) highlightColor else MaterialTheme.colorScheme.onSurface
                    )
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stateBName,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = valueB,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = if (isBHigher) FontWeight.Bold else FontWeight.Normal,
                        color = if (isBHigher) highlightColor else MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }
}
