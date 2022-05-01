package com.example.resistorcalculator

import androidx.compose.ui.graphics.Color
import kotlin.math.pow

sealed class Result {
    class Ok(val values: PrefixedValues): Result()
    class Err(val message: String): Result()
}

enum class BandColor(val background: Color, val text: Color) {
    Black(Color.Black, Color.White),
    Brown(Color(red = 0x8b, green = 0x45, blue = 0x13, alpha = 0xFF), Color.Black),
    Red(Color.Red, Color.Black),
    Orange(Color(red = 0xff, green = 0x8c, blue = 0x00, alpha = 0xff), Color.Black),
    Yellow(Color.Yellow, Color.Black),
    Green(Color.Green, Color.Black),
    Blue(Color.Blue, Color.White),
    Violet(Color(red = 0x80, green = 0x00, blue = 0x80, alpha = 0xff), Color.White),
    Grey(Color.Gray, Color.Black),
    White(Color.White, Color.Black),
    Silver(Color(red = 0xc0, green = 0xc0, blue = 0xc0, alpha = 0xff), Color.Black),
    Gold(Color(red = 0xff, green = 0xd7, blue = 0x00, alpha = 0xff), Color.Black),
}

data class Resistor(
    val resistance: Double,
    val tolerance: Double,
    val tempCoefficient: Int?,
)

data class PrefixedValues(
    val prefix: String,
    val resistance: Double,
    val tolerance: Double,
    val tempCoefficient: Int?,
)

open class CalculationException(message: String): RuntimeException(message)
class MissingBandColorsException(message: String) : CalculationException(message)
class InvalidToleranceColorException(message: String) : CalculationException(message)
class InvalidTempCoefficientColorException(message: String) : CalculationException(message)
class InvalidResistanceColorException(message: String) : CalculationException(message)

// returns the result for the factor-band
fun factor(sum: Double, facBand: BandColor): Double {
    return when (facBand) {
        BandColor.Silver -> sum * 0.1
        BandColor.Gold -> sum * 0.01
        else -> {
            val factor = 10.0.pow(facBand.ordinal)
            sum * factor
        }
    }
}

// returns the maximal tolerance for a given resistance
fun tolerance(prod: Double, band: BandColor): Double {
    return when (band) {
        BandColor.Brown -> prod * 0.01
        BandColor.Red -> prod * 0.02
        BandColor.Green -> prod * 0.005
        BandColor.Blue -> prod * 0.0025
        BandColor.Violet -> prod * 0.001
        BandColor.Grey -> prod * 0.0005
        BandColor.Silver -> prod * 0.1
        BandColor.Gold -> prod * 0.05
        else -> throw InvalidToleranceColorException(
            "There is no tolerance associated to the color $band"
        )
    }
}

// returns the temperature coefficient for a given band
fun tempCoefficient(band: BandColor): Int {
    return when (band) {
        BandColor.Brown -> 100
        BandColor.Red -> 50
        BandColor.Orange -> 15
        BandColor.Yellow -> 25
        BandColor.Blue -> 10
        BandColor.Violet -> 5
        BandColor.White -> 1
        else -> throw InvalidTempCoefficientColorException(
            "There is no temperature coefficient associated to the color $band"
        )
    }
}

fun baseResistance(bands: List<BandColor>): Double {
    if (bands.first().ordinal !in 1..9) {
        throw InvalidResistanceColorException(
            "Band 1 can't consist of the colors black, silver or gold"
        )
    }
    val others = bands.slice(1 until bands.size)
    for ((i, b) in others.withIndex()) {
        if (b.ordinal !in 0..9) {
            throw InvalidResistanceColorException(
                "Band $i can't consist of the colors silver or gold"
            )
        }
    }

    return bands.reversed().withIndex().sumOf { (i, b) ->
        b.ordinal * 10.0.pow(i)
    }
}

fun calculateResistor(bands: List<BandColor>): Resistor {
    return when (bands.size) {
        4 -> {
            val sum = baseResistance(bands.slice(0..1))
            val resistance = factor(sum, bands[2])
            val tolerance = tolerance(resistance, bands[3])
            Resistor(resistance, tolerance, null)
        }
        5 -> {
            val sum = baseResistance(bands.slice(0..2))
            val resistance = factor(sum, bands[3])
            val tolerance = tolerance(resistance, bands[4])
            Resistor(resistance, tolerance, null)
        }
        6 -> {
            val sum = baseResistance(bands.slice(0..2))
            val resistance = factor(sum, bands[3])
            val tolerance = tolerance(resistance, bands[4])
            val tempCoefficient = tempCoefficient(bands[5])
            Resistor(resistance, tolerance, tempCoefficient)
        }
        else -> throw MissingBandColorsException(
            "Not enough bands to calculate a result"
        )
    }
}

fun prefixedValues(resistor: Resistor): PrefixedValues {
    val (resistance, tolerance, tempCoefficient) = resistor
    return if (resistance > 999 && resistance < 1000000) {
        PrefixedValues(
            prefix = "K",
            resistance / 1000.0,
            tolerance / 1000.0,
            tempCoefficient,
        )
    } else if (resistance > 1000000 && resistance < 1000000000) {
        PrefixedValues(
            prefix = "M",
            resistance / 1000000.0,
            tolerance / 1000000.0,
            tempCoefficient,
        )
    } else if (resistance > 1000000000 && resistance < 1000000000000) {
        PrefixedValues(
            prefix = "G",
            resistance / 1000000000,
            tolerance / 1000000000,
            tempCoefficient,
        )
    } else {
        PrefixedValues(
            prefix = "",
            resistance,
            tolerance,
            tempCoefficient,
        )
    }
}