package com.example.floatingflavors.app.feature.admin.presentation.home


import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.floatingflavors.app.core.navigation.Screen
import androidx.compose.material3.ExperimentalMaterial3Api

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminHomeScreen(
    navController: NavController,
    onLogout: () -> Unit = {}
) {
    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        TopAppBar(title = { Text("Admin Home") })
        Spacer(modifier = Modifier.height(16.dp))

        Card(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text("Welcome Admin", modifier = Modifier.padding(bottom = 8.dp))
                Button(onClick = { navController.navigate(Screen.AdminAddFood.route) }, modifier = Modifier.fillMaxWidth()) {
                    Text("Add Food")
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text("Orders / Dashboard / Quick Links (placeholders)")
                Spacer(modifier = Modifier.height(8.dp))
                Button(onClick = { navController.navigate(Screen.UserMenu.route) }, modifier = Modifier.fillMaxWidth()) {
                    Text("View Menu (User view)")
                }
            }
        }

        Spacer(modifier = Modifier.weight(1f))
        Button(onClick = onLogout, modifier = Modifier.fillMaxWidth()) {
            Text("Logout")
        }
    }
}
