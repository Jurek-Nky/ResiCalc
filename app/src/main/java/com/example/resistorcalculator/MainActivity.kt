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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import com.example.resistorcalculator.BandColor.*
import com.example.resistorcalculator.ui.theme.ResistorCalculatorTheme
import kotlin.math.pow

class MainViewModel : ViewModel() {
    val bands = Bands()
}

class MainActivity : ComponentActivity() {
    val model: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ResistorCalculatorTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colors.background
                ) {
                    MainContent(model.bands)
                }
            }
        }
    }
}

@Composable
fun MainContent(bands: Bands) {
    var result: Double by remember {
        mutableStateOf(0.0)
    }
    var tolerance: Double by remember {
        mutableStateOf(0.0)
    }
    var error: Boolean by remember {
        mutableStateOf(false)
    }
    var errorMessage: String by remember {
        mutableStateOf("")
    }
    var prefix: String by remember {
        mutableStateOf("")
    }
    var tempCoefi: Int by remember {
        mutableStateOf(0)
    }

    fun calculateResult() {
        error = false
        errorMessage = ""
        result = 0.0
        tolerance = 0.0
        prefix = ""
        tempCoefi = 0

        // returns the result for the factor-band
        fun factor(sum: Double, facBand: BandColor): Double {
            return when (facBand) {
                Silver -> sum * 0.1
                Gold -> sum * 0.01
                else -> {
                    val factor = 10.0.pow(facBand.ordinal)
                    sum * factor
                }
            }
        }

        // returns the maximal tolerance for a given resistance
        fun tolerance(prod: Double, band: BandColor): Double? {
            return when (band) {
                Brown -> prod * 0.01
                Red -> prod * 0.02
                Green -> prod * 0.005
                Blue -> prod * 0.0025
                Violet -> prod * 0.001
                Grey -> prod * 0.0005
                Silver -> prod * 0.1
                Gold -> prod * 0.05
                else -> {
                    errorMessage = "There is no tolerance associated to this color!"
                    error = true
                    null
                }
            }
        }

        // returns the temperature coefficient for a given band
        fun tempCoefe(band: BandColor): Int? {
            return when (band) {
                Brown -> 100
                Red -> 50
                Orange -> 15
                Yellow -> 25
                Blue -> 10
                Violet -> 5
                White -> 1
                else -> {
                    errorMessage = "There is no temperature coefficient associated to this Color"
                    error = true
                    null
                }
            }
        }

        // declaring a list of bands so that the exact position of a given band is not important, only the order of all bands.
        // for example: [brown,brown,black,green] == [brown, brown, empty, black, green]
        val bandList = listOfNotNull(
            bands.band1,
            bands.band2,
            bands.band3,
            bands.band4,
            bands.band5,
            bands.band6,
        )

        when (bandList.size) {
            4 -> {
                val (b0, b1, b2, b3) = bandList
                if (b0.ordinal !in 1..9) {
                    errorMessage = "Band 1 can't consist of the colors black, silver or gold"
                    error = true
                    return
                }
                if (b1.ordinal !in 0..9) {
                    errorMessage = "Band 2 can't consist of the colors silver or gold"
                    error = true
                    return
                }

                val sum = (b2.ordinal * 10 + b1.ordinal).toDouble()
                result = factor(sum, b2)
                tolerance = tolerance(result, b3) ?: 0.0
            }
            5 -> {
                val (b0, b1, b2, b3, b4) = bandList
                if (b0.ordinal !in 1..9) {
                    errorMessage = "Band 1 can't consist of the colors black, silver or gold"
                    error = true
                    return
                }
                if (b1.ordinal !in 0..9) {
                    errorMessage = "Band 2 can't consist of the colors silver or gold"
                    error = true
                    return
                }
                if (b2.ordinal !in 0..9) {
                    errorMessage = "Band 3 can't consist of the colors silver or gold"
                    error = true
                    return
                }

                val sum = (b0.ordinal * 100 + b1.ordinal * 10 + b2.ordinal).toDouble()
                result = factor(sum, b3)
                tolerance = tolerance(result, b4) ?: 0.0
            }
            6 -> {
                val (b0, b1, b2, b3, b4) = bandList
                if (b0.ordinal !in 1..9) {
                    errorMessage = "Band 1 can't consist of the colors black, silver or gold"
                    error = true
                    return
                }
                if (b1.ordinal !in 0..9) {
                    errorMessage = "Band 2 can't consist of the colors silver or gold"
                    error = true
                    return
                }
                if (b2.ordinal !in 0..9) {
                    errorMessage = "Band 3 can't consist of the colors silver or gold"
                    error = true
                    return
                }

                val sum = (b0.ordinal * 100 + b1.ordinal * 10 + b0.ordinal).toDouble()
                result = factor(sum, b3)
                tolerance = tolerance(result, b4) ?: 0.0
                tempCoefi = tempCoefe(bandList[5]) ?: 0
            }
            else -> {
                errorMessage = "Not enough bands to calculate a result"
                error = true
            }
        }

        // if either a wrong color is set at a specific band or there are not enough colors to
        // calculate a result, all results are set to 0 and the function returns
        if (error) {
            result = 0.0
            tolerance = 0.0
            tempCoefi = 0
            return
        }
        if (result > 999 && result < 1000000) {
            result = "%.3f".format(result / 1000).toDouble()
            prefix = "K"
            tolerance /= 1000.0
        } else if (result < 1000000000 && result > 1000000) {
            result = "%.3f".format(result / 1000000).toDouble()
            prefix = "M"
            tolerance /= 1000000
        } else if (result < 1000000000000 && result > 1000000000) {
            result = "%.3f".format(result / 1000000000).toDouble()
            prefix = "G"
            tolerance /= 1000000000
        }
    }

    val context = LocalContext.current
    val configuration = LocalConfiguration.current

    // simple column/row design
    Column(modifier = Modifier.fillMaxSize()) {
        Row(modifier = Modifier.padding(top = 30.dp, start = 10.dp, end = 8.dp)) {
            val btnWidth = (configuration.screenWidthDp.dp - 32.dp) / 6
            DropDownColorPicker("b1", btnWidth, bands::band1::set)
            DropDownColorPicker("b2", btnWidth, bands::band2::set)
            DropDownColorPicker("b3", btnWidth, bands::band3::set)
            DropDownColorPicker("b4", btnWidth, bands::band4::set)
            DropDownColorPicker("b5", btnWidth, bands::band5::set)
            DropDownColorPicker("b6", btnWidth, bands::band6::set)
        }
        Divider(thickness = 2.dp, modifier = Modifier.padding(vertical = 25.dp))
        Row(
            modifier = Modifier
                .align(alignment = CenterHorizontally)
                .padding(bottom = 20.dp)
        ) {
            Button(
                onClick = {
                    calculateResult()
                    if (error) {
                        Toast.makeText(context, errorMessage, Toast.LENGTH_SHORT).show()
                    }
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
                if (tempCoefi != 0) {
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
                        result = result,
                        prefix = prefix,
                        error = error
                    )
                }
                Row(modifier = Modifier.padding(bottom = 5.dp)) {
                    ToleranceField(
                        modifier = Modifier,
                        tolerance = tolerance,
                        result = result,
                        prefix = prefix,
                        error = error
                    )
                }
                if (tempCoefi != 0) {
                    Row(modifier = Modifier.padding(bottom = 5.dp)) {
                        TempCoefField(modifier = Modifier, coefficient = tempCoefi)
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

@Composable
// Component for displaying the result of the resistance multiplication
// if error is true the field gets filled with ---- and turns red
fun ResultField(modifier: Modifier, result: Double, prefix: String, error: Boolean) {
    if (error) {
        Text(
            text = "---- " + "Ω",
            modifier = modifier
                .shadow(3.dp)
                .background(color = MaterialTheme.colors.error, shape = MaterialTheme.shapes.medium)
                .padding(5.dp),
            color = MaterialTheme.colors.onError
        )
    } else {
        Text(
            text = "$result $prefix" + "Ω",
            modifier = modifier
                .shadow(3.dp)
                .background(MaterialTheme.colors.secondary, shape = MaterialTheme.shapes.medium)
                .padding(5.dp),
            color = MaterialTheme.colors.onSecondary
        )
    }
}

@Composable
// Component for displaying the result of the resistance +- tolerance
// if error is true the field gets filled with ---- and turns red
fun ToleranceField(
    modifier: Modifier,
    tolerance: Double,
    result: Double,
    prefix: String,
    error: Boolean
) {
    if (error) {
        Text(
            text = "---- Ω",
            modifier = modifier
                .shadow(3.dp)
                .background(color = MaterialTheme.colors.error, shape = MaterialTheme.shapes.medium)
                .padding(5.dp),
            color = MaterialTheme.colors.onError
        )
    } else {
        Text(
            text = "%.3f".format(result - tolerance) + " " + prefix + "Ω" + " - " + "%.3f".format(
                result + tolerance
            ) + " " + prefix + "Ω",
            modifier = modifier
                .shadow(3.dp)
                .background(MaterialTheme.colors.secondary, shape = MaterialTheme.shapes.medium)
                .padding(5.dp),
            color = MaterialTheme.colors.onSecondary
        )
    }
}

@Composable
// Component for displaying the temperature coefficient
fun TempCoefField(modifier: Modifier, coefficient: Int) {
    Text(
        text = "$coefficient  ppm/◦C",
        modifier = modifier
            .shadow(3.dp)
            .background(MaterialTheme.colors.secondary, shape = MaterialTheme.shapes.medium)
            .padding(5.dp),
        color = MaterialTheme.colors.onSecondary
    )
}

@Composable
// Component to pick Colors in this application each one of these represents one band of the resistor
fun DropDownColorPicker(name: String, width: Dp, callback: (BandColor?) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    var btnColor by remember { mutableStateOf(Color.White) }
    var btnTextColor by remember { mutableStateOf(Color.Black) }

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
                        btnColor = bandColor.background
                        btnTextColor = bandColor.text
                        callback(bandColor)
                    },
                    modifier = Modifier
                        .height(22.dp)
                        .background(color = bandColor.background)
                )
                {
                    Text(text = "")
                }
            }
            DropdownMenuItem(
                onClick = {
                    expanded = false
                    btnColor = Color.Transparent
                    btnTextColor = Color.Black
                    callback(null)
                },
                modifier = Modifier
                    .height(22.dp)
                    .background(color = Color.Transparent)
            )
            {
                Text(
                    text = "Clear",
                    color = Color.Black,
                    modifier = Modifier.background(Color.White)
                )
            }
        }
    }
}