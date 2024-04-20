package com.example.projekt

import android.content.ContentValues.TAG
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text

import androidx.compose.material3.Button

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.text.KeyboardOptions

import androidx.compose.material3.OutlinedTextField
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.text.input.KeyboardType
import com.google.firebase.auth.FirebaseAuth

import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.BottomNavigation
import androidx.compose.material.BottomNavigationItem
import androidx.compose.material.ModalDrawer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDrawerState


import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color

import androidx.compose.ui.layout.ContentScale

import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.database
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.concurrent.TimeUnit
import kotlin.coroutines.CoroutineContext


class MainActivity : ComponentActivity() {
    private val auth = Firebase.auth
    val currentUser = auth.currentUser

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            Navigation()

        }
    }
    @Composable
    fun Navigation() {
        val navController = rememberNavController()
        val items = listOf(
            BottomNavigationItem(
                title = "Home",
                route = "menu",
                icon = Icons.Filled.Home,
            ),
            BottomNavigationItem(
                title = "Dodaj turniej",
                route = "createTournament",
                icon = Icons.Filled.Add,
            ),
            BottomNavigationItem(
                title = "Bilety",
                route = "bilety/0",
                icon = Icons.Filled.ShoppingCart,
            ),
        )

        NavHost(navController, startDestination = "login") {
            composable("login") { MainScreen(navController) }
            composable("menu") {
                ScaffoldWithBottomNav("Menu", navController, items) {
                    MenuScreen(navController)
                }
            }
            composable("bilety/{ticketCount}") { backStackEntry ->
                val ticketCount = backStackEntry.arguments?.getString("ticketCount")?.toIntOrNull() ?: 0
                ScaffoldWithBottomNav("Bilety", navController, items) {
                    Bilety(ticketCount)
                }
            }
            composable("createTournament") {
                ScaffoldWithBottomNav("CreateTournament", navController, items) {
                    CreateTournamentScreen(navController)
                }
            }
            composable("fullTournament/{tournamentName}") { backStackEntry ->
                val tournamentName = backStackEntry.arguments?.getString("tournamentName")
                if (tournamentName != null) {
                    ScaffoldWithBottomNav("FullTournament", navController, items) {
                        FullTournamentScreen(tournamentName, navController)
                    }
                }
            }
        }
    }

    @Composable
    fun ScaffoldWithBottomNav(screenName: String, navController: NavController, items: List<BottomNavigationItem>, content: @Composable () -> Unit) {
        Scaffold(
            bottomBar = {
                BottomNavigation(
                    backgroundColor = Color.LightGray,
                    contentColor = Color.White,
                ) {
                    items.forEachIndexed { index, item ->
                        BottomNavigationItem(
                            selected = false,
                            onClick = { navController.navigate(item.route) },
                            icon = { Icon(item.icon, contentDescription = null) },
                            label = { Text(item.title) },
                        )
                    }
                }
            }
        ) { innerPadding ->
            Box(modifier = Modifier.padding(innerPadding)) {
                content()
            }
        }
    }



    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun MainScreen(navController: NavController) {
        var email by remember { mutableStateOf("") }
        var password by remember { mutableStateOf("") }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(id = R.drawable.cs2),
                contentDescription = "Background Image",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )

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
                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = { Text("Email") },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color.Black,
                            unfocusedBorderColor = Color.Black,
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        label = { Text("Password") },
                        visualTransformation = PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color.Black,
                            unfocusedBorderColor = Color.Black,
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(onClick = { signUp(email, password) }) {
                        Text("Sign Up", fontSize = 20.sp)
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(onClick = { signIn(email, password, navController) }) {
                        Text("Sign In", fontSize = 20.sp)
                    }
                }
            }
        }
    }

    private fun signUp(email: String, password: String) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    Toast.makeText(
                        baseContext, "Sign up successful.",
                        Toast.LENGTH_SHORT
                    ).show()
                } else {
                    Toast.makeText(
                        baseContext, "Authentication failed.",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
    }

    private fun signIn(email: String, password: String, navController: NavController) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    navController.navigate("menu")
                } else {
                    Toast.makeText(
                        baseContext, "Authentication failed.",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
    }




}





