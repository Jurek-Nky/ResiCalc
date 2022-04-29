package com.example.resistorcalculator

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment.Companion.CenterHorizontally
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.resistorcalculator.ui.theme.ResistorCalculatorTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ResistorCalculatorTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colors.background
                ) {
                    MyScreenContent()
                }
            }
        }
    }
}

var band1: Int = -1
var band2: Int = -1
var band3: Int = -1
var band4: Int = -1
var band5: Int = -1
var band6: Int = -1

@Composable
fun MyScreenContent() {
    var result: Double by remember {
        mutableStateOf(0.0)
    }
    var tolerance: Double by remember {
        mutableStateOf(0.0)
    }
    var error: Boolean by remember {
        mutableStateOf(false)
    }
    var prefix: String by remember {
        mutableStateOf("")
    }

    fun calcRes() {
        error = false
        result = 0.0
        tolerance = 0.0
        prefix = ""
        fun factor(sum: Double, facBand: Int): Double {
            when (facBand) {
                0 -> {
                    return sum.toDouble()
                }
                1 -> {
                    return (sum * 10).toDouble()
                }
                2 -> {
                    return (sum * 100).toDouble()
                }
                3 -> {
                    return (sum * 1000).toDouble()
                }
                4 -> {
                    return (sum * 10000).toDouble()
                }
                5 -> {
                    return (sum * 100000).toDouble()
                }
                6 -> {
                    return (sum * 1000000).toDouble()
                }
                7 -> {
                    return (sum * 10000000).toDouble()
                }
                8 -> {
                    return (sum * 100000000).toDouble()
                }
                9 -> {
                    return (sum * 1000000000).toDouble()
                }
                10 -> {
                    return sum * 0.1
                }
                11 -> {
                    return sum * 0.01
                }
                12 -> {
                    return sum * 0.001
                }
                else -> {
                    return 0.0
                }
            }

        }

        fun tolerance(prod: Double, band: Int): Double {
            when (band) {
                1 -> {
                    return (prod * 0.01)
                }
                2 -> {
                    return (prod * 0.02)
                }
                3 -> {
                    return (prod * 0.0005)
                }
                4 -> {
                    return (prod * 0.0002)
                }
                5 -> {
                    return (prod * 0.005)
                }
                6 -> {
                    return (prod * 0.0025)
                }
                7 -> {
                    return (prod * 0.001)
                }
                8 -> {
                    return (prod * 0.0001)
                }
                10 -> {
                    return (prod * 0.05)
                }
                11 -> {
                    return (prod * 0.10)
                }
                else -> {
                    error = true
                    result = 0.0
                    return 0.0
                }
            }
        }

        val bandList: MutableList<Int> = mutableListOf()
        for (band in listOf(band1, band2, band3, band4, band5, band6)) {
            if (band > -1) {
                bandList.add(band)
            }
        }

        when (bandList.size) {
            4 -> {
                if (listOf(0, 10, 12).contains(bandList[3])
                    ||
                    listOf(10, 11, 12).contains(bandList[0])
                    ||
                    listOf(10, 11, 12).contains(bandList[1])
                ) {
                    error = true
                    return
                }
                val sum = (bandList[0].toString() + bandList[1].toString()).toDouble()
                result = factor(sum, bandList[2])
                tolerance = tolerance(result, bandList[3])
            }
            5 -> {
                if (listOf(10, 11, 12).contains(bandList[0])
                    ||
                    listOf(10, 11, 12).contains(bandList[1])
                    ||
                    listOf(10, 11, 12).contains(bandList[2])
                    ||
                    listOf(0, 10, 12).contains(bandList[4])
                ) {
                    error = true
                    return
                }
                val sum = (bandList[0].toString() + bandList[1].toString() + bandList[2]).toDouble()
                result = factor(sum, bandList[3])
                tolerance = tolerance(result, bandList[4])
            }
            6 -> {
                if (listOf(10, 11, 12).contains(bandList[0])
                    ||
                    listOf(10, 11, 12).contains(bandList[1])
                    ||
                    listOf(10, 11, 12).contains(bandList[2])
                    ||
                    listOf(0, 10, 12).contains(bandList[4])
                ) {
                    error = true
                    return
                }
                val sum = (bandList[0].toString() + bandList[1].toString() + bandList[2]).toDouble()
                result = factor(sum, bandList[3])
                tolerance = tolerance(result, bandList[4])

            }
            else -> {
                error = true
            }
        }
        if (result > 999 && result < 1000000) {
            result = "%.3f".format(result / 1000).toDouble()
            prefix = "K"
            tolerance /= 1000
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

    Column(modifier = Modifier.fillMaxSize()) {
        Row(modifier = Modifier.padding(top = 30.dp, start = 10.dp, end = 8.dp)) {
            val btnWidth = (configuration.screenWidthDp.dp - 32.dp) / 6
            DropDownColorPicker("b1", btnWidth, 1)
            DropDownColorPicker("b2", btnWidth, 2)
            DropDownColorPicker("b3", btnWidth, 3)
            DropDownColorPicker("b4", btnWidth, 4)
            DropDownColorPicker("b5", btnWidth, 5)
            DropDownColorPicker("b6", btnWidth, 6)
        }
        Divider(thickness = 2.dp, modifier = Modifier.padding(vertical = 50.dp))
        Row(modifier = Modifier.align(alignment = CenterHorizontally)) {
            Button(onClick = {
                calcRes()
                if (error) {
                    Toast.makeText(context, "error", Toast.LENGTH_SHORT).show()
                }
            }) {
                Text("click")
            }
            ResultField(modifier = Modifier.padding(horizontal = 10.dp), result, prefix)
            ToleranceField(modifier = Modifier, tolerance = tolerance, result = result, prefix)
        }
        Image(painter = painterResource(id = R.drawable.resistor_map), contentDescription = "chart with all resistor colors", modifier = Modifier.fillMaxSize() )

    }
}

@Composable
fun ResultField(modifier: Modifier, result: Double, prefix: String) {
    Text(
        text = "$result $prefix" + "Ω",
        modifier = modifier
            .border(1.dp, Color.Gray, shape = RoundedCornerShape(85))
            .background(color = Color.LightGray, shape = RoundedCornerShape(85))
            .padding(10.dp)
    )
}

@Composable
fun ToleranceField(modifier: Modifier, tolerance: Double, result: Double, prefix: String) {
    Text(
        text = "%.3f".format(result - tolerance)+ " " + prefix + "Ω" + " - " + "%.3f".format(result + tolerance) + " " + prefix + "Ω",
        modifier = modifier
            .border(1.dp, Color.Gray, shape = RoundedCornerShape(85))
            .background(color = Color.LightGray, shape = RoundedCornerShape(85))
            .padding(10.dp)
    )
}

@Composable
fun DropDownColorPicker(name: String, width: Dp, number: Int) {
    val context = LocalContext.current
    var expanded by remember { mutableStateOf(false) }
    val suggestions = mapOf(
        "Black" to Color.Black to 0,
        "Brown" to Color(red = 0x8b, green = 0x45, blue = 0x13, alpha = 0xFF) to 1,
        "Red" to Color.Red to 2,
        "Orange" to Color(red = 0xff, green = 0x8c, blue = 0x00, alpha = 0xff) to 3,
        "Yellow" to Color.Yellow to 4,
        "Green" to Color.Green to 5,
        "Blue" to Color.Blue to 6,
        "Violet" to Color(red = 0x80, green = 0x00, blue = 0x80, alpha = 0xff) to 7,
        "Grey" to Color.Gray to 8,
        "White" to Color.White to 9,
        "Gold" to Color(red = 0xff, green = 0xd7, blue = 0x00, alpha = 0xff) to 10,
        "Silver" to Color(red = 0xc0, green = 0xc0, blue = 0xc0, alpha = 0xff) to 11,
        "Pink" to Color(red = 0xff, green = 0x14, blue = 0x93, alpha = 0xff) to 12,
        "Reset" to Color.Transparent to -1
    )
    var selected by remember { mutableStateOf("") }
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
            suggestions.forEach { label ->
                DropdownMenuItem(
                    onClick = {
                        expanded = false
                        selected = label.key.first
                        btnColor = label.key.second
                        btnTextColor =
                            if (label.key.first == "Black" || label.key.first == "Blue" || label.key.first == "Violet") {
                                Color.White
                            } else {
                                Color.Black
                            }
                        when (number) {
                            1 -> {
                                band1 = label.value
                            }
                            2 -> {
                                band2 = label.value
                            }
                            3 -> {
                                band3 = label.value
                            }
                            4 -> {
                                band4 = label.value
                            }
                            5 -> {
                                band5 = label.value
                            }
                            6 -> {
                                band6 = label.value
                            }
                            else -> {
                                Toast.makeText(context, "error", Toast.LENGTH_LONG).show()
                            }
                        }
                    },
                    modifier = Modifier
                        .height(22.dp)
                        .background(color = label.key.second)
                )
                {
                    if (label.value == -1) {
                        Text(
                            text = "Clear",
                            color = Color.Black,
                            modifier = Modifier.background(Color.White)
                        )
                    } else {
                        Text(
                            text = "",
                        )
                    }
                }
            }
        }
    }
}