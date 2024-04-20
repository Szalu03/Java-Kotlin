package com.example.projekt

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Button
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp


@Composable
fun Bilety(ticketCount: Int) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.LightGray),
        contentAlignment = Alignment.Center
    ) {
        Surface(
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .padding(16.dp)
                .border(2.dp, Color.Black, RoundedCornerShape(12.dp))
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.padding(16.dp)
            ) {
                Text(text = "Bilety", style = MaterialTheme.typography.h4)
                Spacer(modifier = Modifier.height(16.dp))

                Text(text = "Zamówiłeś $ticketCount biletów.", style = MaterialTheme.typography.h5)
                Spacer(modifier = Modifier.height(16.dp))

                Row {
                    repeat(ticketCount) {
                        Icon(Icons.Filled.Receipt, contentDescription = "Bilet", modifier = Modifier.size(24.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))

                Button(onClick = { /* kupowanie*/ }) {
                    Text("Kup")
                }
            }
        }
    }
}