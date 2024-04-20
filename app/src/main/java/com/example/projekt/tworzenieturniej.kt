package com.example.projekt


import android.Manifest
import android.app.Activity
import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.ContentValues.TAG
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.google.firebase.database.ktx.database

import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale



data class Tournament(
    val name: String = "",
    val location: String = "",
    val prizePool: String = "",
    val image: String = "",
    val teams: List<String> = listOf(),
    val startDate: String = "",
    val endDate: String = "",
    val teamCount: Int = 0
)

class NotificationReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val tournamentName = intent.getStringExtra("tournamentName")

        // Utwórz kanał powiadomień dla Android 8.0 i nowszych
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Tournament Reminder Channel"
            val descriptionText = "Channel for Tournament Reminder"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel("tournament_reminder", name, importance).apply {
                description = descriptionText
            }
            val notificationManager: NotificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }

        // Utwórz powiadomienie
        val builder = NotificationCompat.Builder(context, "tournament_reminder")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("Przypomnienie o turnieju")
            .setContentText("Turniej $tournamentName zaczyna się za tydzień!")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)

        // Wyświetl powiadomienie
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_NOTIFICATION_POLICY) == PackageManager.PERMISSION_GRANTED) {
            with(NotificationManagerCompat.from(context)) {
                notify(0, builder.build())
            }
        } else {
            // Prośba o uprawnienia lub obsługa sytuacji, gdy uprawnienia nie są dostępne
        }
    }
}
fun scheduleNotification(context: Context, startDate: String, tournamentName: String) {
    if (startDate.isNotEmpty()) {
        val sdf = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
        val date = sdf.parse(startDate)
        val calendar = Calendar.getInstance().apply {
            time = date
            add(Calendar.DATE, -7)
        }

        val intent = Intent(context, NotificationReceiver::class.java).apply {
            putExtra("tournamentName", tournamentName)
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && !alarmManager.canScheduleExactAlarms()) {
            // Prośba o uprawnienie do ustawiania dokładnych alarmów
        } else {
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, calendar.timeInMillis, pendingIntent)
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && !alarmManager.canScheduleExactAlarms()) {
            // Prośba o uprawnienie do ustawiania dokładnych alarmów
        } else {
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, calendar.timeInMillis, pendingIntent)
        }
    } else {
        // Obsłuż sytuację, gdy startDate jest pusty
    }
}
@Composable
fun CreateTournamentScreen(navController: NavController) {
    val activity = LocalContext.current as Activity
    val requestCode = 0 // Możesz zmienić tę wartość na dowolną inną liczbę całkowitą
    val context = LocalContext.current
    var tournamentName by remember { mutableStateOf("") }
    var tournamentLocation by remember { mutableStateOf("") }
    var prizePool by remember { mutableStateOf("") }
    var startDate by remember { mutableStateOf("") }
    var endDate by remember { mutableStateOf("") }
    val tournamentImage = remember { mutableStateOf("iem") }
    val teams = remember {
        mutableStateListOf(
            "navi",
            "G2",
            "Faze",
            "Gamerlegion",
            "mousesport",
            "virtuspro",
            "liquid"
        )
    }
    val selectedTeams = remember { mutableStateListOf<String>() }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Pola do wprowadzania danych
        OutlinedTextField(
            value = tournamentName,
            onValueChange = { tournamentName = it },
            label = { Text("Nazwa turnieju") })
        Spacer(modifier = Modifier.height(16.dp))
        OutlinedTextField(
            value = tournamentLocation,
            onValueChange = { tournamentLocation = it },
            label = { Text("Lokalizacja turnieju") })
        Spacer(modifier = Modifier.height(16.dp))
        OutlinedTextField(
            value = prizePool,
            onValueChange = { prizePool = it },
            label = { Text("Pula nagród") })
        Spacer(modifier = Modifier.height(16.dp))
        OutlinedTextField(
            value = startDate,
            onValueChange = { startDate = it },
            label = { Text("Data rozpoczęcia") })
        Spacer(modifier = Modifier.height(16.dp))
        OutlinedTextField(
            value = endDate,
            onValueChange = { endDate = it },
            label = { Text("Data zakończenia") })
        Spacer(modifier = Modifier.height(16.dp))

        Row {
            Checkbox(
                checked = tournamentImage.value == "iem",
                onCheckedChange = { if (it) tournamentImage.value = "iem" })
            Text("IEM")
            Checkbox(
                checked = tournamentImage.value == "esl_pro_league",
                onCheckedChange = { if (it) tournamentImage.value = "esl_pro_league" })
            Text("ESL PRO LEAGUE")
            Checkbox(
                checked = tournamentImage.value == "blast",
                onCheckedChange = { if (it) tournamentImage.value = "blast" })
            Text("Blast")
        }


        teams.chunked(3).forEach { teamChunk ->
            Row {
                teamChunk.forEach { team ->
                    Checkbox(
                        checked = selectedTeams.contains(team),
                        onCheckedChange = {
                            if (it) selectedTeams.add(team) else selectedTeams.remove(team)
                        })
                    Text(team)
                }
            }
        }


        Button(onClick = {

            val database = Firebase.database
            val myRef = database.getReference("tournaments")


            val tournament = Tournament(
                tournamentName,
                tournamentLocation,
                prizePool,
                tournamentImage.value,
                selectedTeams,
                startDate,
                endDate,
                selectedTeams.size
            )


            myRef.child(tournamentName).setValue(tournament)
                .addOnSuccessListener {
                    Log.d(TAG, "Successfully added tournament")

                    navController.navigate("menu")

                    if (ContextCompat.checkSelfPermission(context, Manifest.permission.SCHEDULE_EXACT_ALARM) != PackageManager.PERMISSION_GRANTED) {
                        ActivityCompat.requestPermissions(activity, arrayOf(Manifest.permission.SCHEDULE_EXACT_ALARM), requestCode)
                    } else {

                        scheduleNotification(context, tournament.startDate, tournament.name)
                    }


                }
                .addOnFailureListener { e ->
                    Log.w(TAG, "Failed to add tournament", e)
                }

            navController.navigate("menu")
        }) {
            Text("Utwórz turniej")
        }
    }
}





