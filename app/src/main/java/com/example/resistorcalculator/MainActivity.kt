package com.example.resistorcalculator

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment.Companion.CenterHorizontally
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import com.example.resistorcalculator.ui.theme.ResistorCalculatorTheme
import kotlinx.coroutines.flow.MutableStateFlow

class MainViewModel : ViewModel() {
    val bands = Array(6) { MutableStateFlow<BandColor?>(null) }
    val result = MutableStateFlow<Result?>(null)

    fun calculate() {
        result.value = try {
            val resistor = calculateResistor(bands.mapNotNull { it.value })
            Result.Ok(prefixedValues(resistor))
        } catch (e: CalculationException) {
            Result.Err(e.message.orEmpty())
        }
    }
}

class MainActivity : ComponentActivity() {
    private val model: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ResistorCalculatorTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colors.background
                ) {
                    MainContent(model)
                }
            }
        }
    }
}

@Composable
fun MainContent(model: MainViewModel) {
    val result by model.result.collectAsState()

    val values = when (val r = result) {
        is Result.Err -> {
            val context = LocalContext.current
            Toast.makeText(context, r.message, Toast.LENGTH_SHORT).show()
            null
        }
        is Result.Ok -> {
            r.values
        }
        null -> null
    }

    val configuration = LocalConfiguration.current
    // simple column/row design
    Column(modifier = Modifier.fillMaxSize()) {
        Row(modifier = Modifier.padding(top = 30.dp, start = 10.dp, end = 8.dp)) {
            val btnWidth = (configuration.screenWidthDp.dp - 32.dp) / 6
            DropDownColorPicker("b1", btnWidth, model.bands[0])
            DropDownColorPicker("b2", btnWidth, model.bands[1])
            DropDownColorPicker("b3", btnWidth, model.bands[2])
            DropDownColorPicker("b4", btnWidth, model.bands[3])
            DropDownColorPicker("b5", btnWidth, model.bands[4])
            DropDownColorPicker("b6", btnWidth, model.bands[5])
        }
        Divider(thickness = 2.dp, modifier = Modifier.padding(vertical = 25.dp))
        Row(
            modifier = Modifier
                .align(alignment = CenterHorizontally)
                .padding(bottom = 20.dp)
        ) {
            Button(
                onClick = {
                    model.calculate()
                }, modifier = Modifier
                    .shadow(3.dp)
                    .width(150.dp)
            ) {
                Text("Calculate")
            }
        }
        Row {
            Column(
                modifier = Modifier
                    .width(200.dp)
                    .padding(start = 10.dp)
            ) {
                Row(modifier = Modifier.padding(bottom = 5.dp)) {
                    Text(
                        text = "Resistance: ",
                        modifier = Modifier.padding(vertical = 5.dp)
                    )

                }
                Row(modifier = Modifier.padding(bottom = 5.dp)) {
                    Text(
                        text = "Tolerance: ",
                        modifier = Modifier.padding(vertical = 5.dp)
                    )

                }
                values?.tempCoefficient?.let {
                    Row(modifier = Modifier.padding(bottom = 5.dp)) {
                        Text(
                            text = "Temperature coefficient: ",
                            modifier = Modifier.padding(vertical = 5.dp)
                        )
                    }
                }
            }
            Column {
                Row(modifier = Modifier.padding(bottom = 5.dp)) {
                    ResultField(
                        modifier = Modifier,
                        values = values
                    )
                }
                Row(modifier = Modifier.padding(bottom = 5.dp)) {
                    ToleranceField(
                        modifier = Modifier,
                        values = values
                    )
                }
                values?.tempCoefficient?.let { coefficient ->
                    Row(modifier = Modifier.padding(bottom = 5.dp)) {
                        TempCoefficientField(
                            modifier = Modifier,
                            coefficient = coefficient
                        )
                    }
                }
            }
        }
        Divider(thickness = 2.dp, modifier = Modifier.padding(top = 35.dp))
        Image(
            painter = painterResource(id = R.drawable.resimap),
            contentDescription = "chart with all resistor colors",
            modifier = Modifier.fillMaxSize()
        )

    }
}

// Component for displaying the result of the resistance multiplication
// if error is true the field gets filled with ---- and turns red
@Composable
fun ResultField(modifier: Modifier, values: PrefixedValues?) {
    if (values == null) {
        Text(
            text = "---- " + "Ω",
            modifier = modifier
                .shadow(3.dp)
                .background(
                    color = MaterialTheme.colors.error,
                    shape = MaterialTheme.shapes.medium
                )
                .padding(5.dp),
            color = MaterialTheme.colors.onError
        )
    } else {
        val (prefix, resistance) = values
        Text(
            text = "$resistance ${prefix}Ω",
            modifier = modifier
                .shadow(3.dp)
                .background(MaterialTheme.colors.secondary, shape = MaterialTheme.shapes.medium)
                .padding(5.dp),
            color = MaterialTheme.colors.onSecondary
        )
    }
}

// Component for displaying the result of the resistance +- tolerance
// if error is true the field gets filled with ---- and turns red
@Composable
fun ToleranceField(
    modifier: Modifier,
    values: PrefixedValues?,
) {
    if (values == null) {
        Text(
            text = "---- Ω",
            modifier = modifier
                .shadow(3.dp)
                .background(
                    color = MaterialTheme.colors.error,
                    shape = MaterialTheme.shapes.medium
                )
                .padding(5.dp),
            color = MaterialTheme.colors.onError
        )
    } else {
        val (prefix, resistance, tolerance) = values
        val min = "%.3f".format(resistance - tolerance)
        val max = "%.3f".format(resistance + tolerance)
        Text(
            text = "$min ${prefix}Ω - $max ${prefix}Ω",
            modifier = modifier
                .shadow(3.dp)
                .background(MaterialTheme.colors.secondary, shape = MaterialTheme.shapes.medium)
                .padding(5.dp),
            color = MaterialTheme.colors.onSecondary
        )
    }
}

// Component for displaying the temperature coefficient
@Composable
fun TempCoefficientField(modifier: Modifier, coefficient: Int) {
    Text(
        text = "$coefficient  ppm/◦C",
        modifier = modifier
            .shadow(3.dp)
            .background(MaterialTheme.colors.secondary, shape = MaterialTheme.shapes.medium)
            .padding(5.dp),
        color = MaterialTheme.colors.onSecondary
    )
}

// Component to pick Colors in this application each one of these represents one band of the resistor
@Composable
fun DropDownColorPicker(name: String, width: Dp, bandState: MutableStateFlow<BandColor?>) {
    val band by bandState.collectAsState()
    val btnColor = band?.background ?: MaterialTheme.colors.surface
    val btnTextColor = band?.text ?: MaterialTheme.colors.onSurface

    var expanded by remember { mutableStateOf(false) }

    Box(modifier = Modifier.padding(end = 2.dp)) {
        Button(
            onClick = { expanded = !expanded },
            colors = ButtonDefaults.buttonColors(
                backgroundColor = btnColor,
            ),
            modifier = Modifier.width(width)

        ) {
            Text(text = name, color = btnTextColor)
            Icon(
                imageVector = Icons.Filled.ArrowDropDown,
                contentDescription = null,
            )
        }
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
        ) {
            BandColor.values().forEach { bandColor ->
                DropdownMenuItem(
                    onClick = {
                        expanded = false
                        bandState.value = bandColor
                    },
                    modifier = Modifier
                        .height(22.dp)
                        .background(color = bandColor.background)
                ) {}
            }
            DropdownMenuItem(
                onClick = {
                    expanded = false
                    bandState.value = null
                },
                modifier = Modifier.height(22.dp)
            )
            {
                Text(text = "Clear")
            }
        }
    }
}