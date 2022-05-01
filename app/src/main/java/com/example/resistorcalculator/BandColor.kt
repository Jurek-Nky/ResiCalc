package com.example.resistorcalculator

import androidx.compose.ui.graphics.Color

data class Bands(
    var band1: BandColor? = null,
    var band2: BandColor? = null,
    var band3: BandColor? = null,
    var band4: BandColor? = null,
    var band5: BandColor? = null,
    var band6: BandColor? = null,
)

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