package com.example.projekt

import android.content.ContentValues.TAG
import androidx.compose.material.icons.outlined.StarBorder
import android.util.Log
import android.widget.RatingBar
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import androidx.compose.foundation.lazy.items


data class BottomNavigationItem(
    val title: String,
    val route: String,
    val icon: ImageVector,
)


@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterialApi::class)
@Composable
fun MenuScreen(navController: NavController) {
    val database = Firebase.database
    val myRef = database.getReference("tournaments")

    val tournaments = remember { mutableStateListOf<Tournament>() }
    LaunchedEffect(Unit) {
        myRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                tournaments.clear()
                for (postSnapshot in dataSnapshot.children) {
                    val tournament = postSnapshot.getValue(Tournament::class.java)
                    if (tournament != null) {
                        tournaments.add(tournament)
                    }
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Log.w(TAG, "loadPost:onCancelled", databaseError.toException())
            }
        })
    }

    Scaffold { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding)) {
            LazyColumn { // Używamy LazyColumn do wyświetlania listy turniejów
                items(tournaments) { tournament ->
                    TournamentItem(tournament, navController)
                }
            }
        }
    }
}

@Composable
fun TournamentItem(tournament: Tournament, navController: NavController) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .border(2.dp, Color.Black)
            .clickable { navController.navigate("fullTournament/${tournament.name}") }, // Dodajemy akcję kliknięcia, która przekierowuje do ekranu FullTournament
        elevation = 8.dp,
        backgroundColor = Color(0xFF3D3C4A)
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) { // Wyśrodkowujemy zawartość kolumny
            val tournamentImage = when (tournament.image) {
                "blast" -> R.drawable.blast
                "iem" -> R.drawable.iem
                "esl_pro_league" -> R.drawable.eslproleague
                else -> R.drawable.iem
            }
            Image(
                painter = painterResource(id = tournamentImage),
                contentDescription = null,
                modifier = Modifier.fillMaxWidth()
            )

            Text(text = tournament.name, style = MaterialTheme.typography.h6, fontWeight = FontWeight.Bold, color = Color.White)

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = "Location: ${tournament.location}", fontWeight = FontWeight.Bold, color = Color.White)
                Spacer(Modifier.width(8.dp))
                val flagImage = getFlagForCountry(tournament.location)
                Image(
                    painter = painterResource(id = flagImage),
                    contentDescription = "Flag for ${tournament.location}",
                    modifier = Modifier.size(24.dp)
                )
            }

            Row {
                Text(text = "Date: ${tournament.startDate} - ${tournament.endDate}", fontWeight = FontWeight.Bold, color = Color.White)
                Spacer(Modifier.width(8.dp))
                Text(text = "Prize Pool: ${tournament.prizePool}", fontWeight = FontWeight.Bold, color = Color.White)
                Spacer(Modifier.width(8.dp))
                Text(text = "Teams: ${tournament.teamCount}", fontWeight = FontWeight.Bold, color = Color.White)
            }
        }
    }

    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) { // Wyśrodkowujemy przycisk
        Button(onClick = { deleteTournament(tournament.name, navController) }) {
            Text("Usuń turniej")
        }
    }
}

fun deleteTournament(tournamentName: String, navController: NavController) {
    val database = Firebase.database
    val myRef = database.getReference("tournaments")

    myRef.child(tournamentName).removeValue()
        .addOnSuccessListener {
            Log.d(TAG, "Successfully deleted tournament")
            navController.navigate("menu")
        }
        .addOnFailureListener { e ->
            Log.w(TAG, "Failed to delete tournament", e)
        }
}

fun getFlagForCountry(country: String): Int {
    return when (country.toLowerCase()) {
        "poland" -> R.drawable.pl
        "germany" -> R.drawable.de
        "denmark" -> R.drawable.dk
        "malta" -> R.drawable.mt
        "sweden" -> R.drawable.se
        else -> R.drawable.eu
    }
}

@Composable
fun FullTournamentScreen(tournamentName: String, navController: NavController) {
    val database = Firebase.database
    val myRef = database.getReference("tournaments/$tournamentName")

    val tournament = remember { mutableStateOf<Tournament?>(null) }
    LaunchedEffect(Unit) {
        myRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                tournament.value = dataSnapshot.getValue(Tournament::class.java)
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Log.w(TAG, "loadPost:onCancelled", databaseError.toException())
            }
        })
    }

    Surface(color = Color.LightGray, shape = RoundedCornerShape(8.dp), elevation = 8.dp, modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.padding(16.dp)) {
            val tournamentImage = when (tournament.value?.image) {
                "blast" -> R.drawable.blast
                "iem" -> R.drawable.iem
                "esl_pro_league" -> R.drawable.eslproleague
                else -> R.drawable.iem
            }
            Image(
                painter = painterResource(id = tournamentImage),
                contentDescription = null,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))

            Text(text = tournament.value?.name ?: "", style = MaterialTheme.typography.h4)
            Spacer(modifier = Modifier.height(8.dp))

            Row {
                Text(text = "Location: ${tournament.value?.location}", style = MaterialTheme.typography.body1)
                Spacer(modifier = Modifier.width(8.dp))
                val flagImage = getFlagForCountry(tournament.value?.location ?: "")
                Image(
                    painter = painterResource(id = flagImage),
                    contentDescription = "Flag for ${tournament.value?.location}",
                    modifier = Modifier.size(24.dp)
                )
            }
            Spacer(modifier = Modifier.height(8.dp))

            Text(text = "Date: ${tournament.value?.startDate} - ${tournament.value?.endDate}", style = MaterialTheme.typography.body1)
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = "Prize Pool: ${tournament.value?.prizePool}", style = MaterialTheme.typography.body1)
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = "Teams: ${tournament.value?.teamCount}", style = MaterialTheme.typography.body1)
            Spacer(modifier = Modifier.height(16.dp))

            var ticketCount by remember { mutableStateOf(0) }
            OutlinedTextField(
                value = ticketCount.toString(),
                onValueChange = { newCount ->
                    if (newCount.isNotEmpty()) {
                        ticketCount = newCount.toInt()
                    }
                },
                label = { Text("Ilość biletów") }
            )
            Spacer(modifier = Modifier.height(8.dp))
            Button(onClick = { navController.navigate("bilety/$ticketCount") }) {
                Text("Kup bilet")
            }
            Spacer(modifier = Modifier.height(16.dp))

            var review by remember { mutableStateOf("") }
            OutlinedTextField(
                value = review,
                onValueChange = { review = it },
                label = { Text("Dodaj recenzję") }
            )
            Spacer(modifier = Modifier.height(8.dp))
            Button(onClick = { addReview(tournamentName, review) }) {
                Text("Dodaj recenzję")
            }
            Spacer(modifier = Modifier.height(16.dp))

            var rating by remember { mutableStateOf(0f) }
            MyRatingBar(value = rating, onValueChange = { rating = it })
            Spacer(modifier = Modifier.height(8.dp))
            Button(onClick = { addRating(tournamentName, rating) }) {
                Text("Oceń turniej")
            }
        }
    }
}
fun addReview(tournamentName: String, review: String) {
    val database = Firebase.database
    val myRef = database.getReference("tournaments/$tournamentName/reviews")

    myRef.push().setValue(review)
        .addOnSuccessListener {
            Log.d(TAG, "Successfully added review")
        }
        .addOnFailureListener { e ->
            Log.w(TAG, "Failed to add review", e)
        }
}

fun addRating(tournamentName: String, rating: Float) {
    val database = Firebase.database
    val myRef = database.getReference("tournaments/$tournamentName/ratings")

    myRef.push().setValue(rating)
        .addOnSuccessListener {
            Log.d(TAG, "Successfully added rating")
        }
        .addOnFailureListener { e ->
            Log.w(TAG, "Failed to add rating", e)
        }
}

@Composable
fun MyRatingBar(value: Float, onValueChange: (Float) -> Unit) {
    Row {
        for (i in 1..5) {
            Icon(
                imageVector = if (i <= value) Icons.Filled.Star else Icons.Outlined.StarBorder,
                contentDescription = null,
                tint = if (i <= value) Color.Yellow else Color.Gray,
                modifier = Modifier
                    .size(24.dp)
                    .clickable { onValueChange(i.toFloat()) }
            )
        }
    }
}