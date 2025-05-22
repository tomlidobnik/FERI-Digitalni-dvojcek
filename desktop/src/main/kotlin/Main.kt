import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.List
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowPosition
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState

enum class MenuState {
    USERS_LIST, ABOUT_APP, ADD_USER, ADD_EVENT, EVENTS_LIST
}


@Composable
@Preview
fun App() {
    val menuState = remember { mutableStateOf(MenuState.USERS_LIST) }
    MaterialTheme {
        Column(modifier = Modifier.fillMaxHeight(), horizontalAlignment = Alignment.CenterHorizontally) {
            Main(menuState)
        }
    }
}

fun main() = application {
    Window(
        onCloseRequest = ::exitApplication,
        title = "Compose Database Admin",
        state = rememberWindowState(
            position = WindowPosition.Aligned(Alignment.Center),
            size = DpSize(800.dp, 800.dp)
        ),
        undecorated = false,
        resizable = true
    ) {
        App()
    }
}

@Composable
fun Main(menuState: MutableState<MenuState>, modifier: Modifier = Modifier) {
    Row(
        modifier = Modifier
            .fillMaxHeight()
            .padding(4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        // Left-side menu
        Box(
            modifier = Modifier
                .weight(2f)
                .fillMaxSize()
                .border(1.dp, Color.LightGray, RoundedCornerShape(4.dp))
                .shadow(2.dp, RoundedCornerShape(4.dp))
                .background(Color.White)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(4.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                // Buttons
                AddUserButton(menuState)
                UsersListButton(menuState)
                DividerLine()
                AddEventButton(menuState)
                EventsListButton(menuState)
                DividerLine()
                AboutAppButton(menuState)
            }
        }
        // Main Content
        Box(
            modifier = Modifier
                .weight(5f)
                .fillMaxSize()
                .border(1.dp, Color.LightGray, RoundedCornerShape(4.dp))
                .shadow(2.dp, RoundedCornerShape(4.dp))
                .background(Color.White)
        ) {
            when (menuState.value) {
                MenuState.ADD_USER -> AddUserTab()
                MenuState.USERS_LIST -> UsersListTab()
                MenuState.ADD_EVENT -> AddEventTab()
                MenuState.EVENTS_LIST -> EventsListTab()
                MenuState.ABOUT_APP -> AboutAppTab()
            }
        }
    }
}


// BUTTONS
@Composable
fun AddUserButton(menuState: MutableState<MenuState>, modifier: Modifier = Modifier) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(50.dp)
            .background(Color.LightGray, RoundedCornerShape(4.dp))
            .clickable { menuState.value = MenuState.ADD_USER },
        contentAlignment = Alignment.CenterStart
    ) {
        Row(
            modifier = Modifier.padding(start = 8.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "Add",
                tint = Color.Black
            )
            Spacer(modifier = Modifier.width(20.dp))
            Text(
                text = "Add User",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )

        }
    }
}
@Composable
fun UsersListButton(menuState: MutableState<MenuState>, modifier: Modifier = Modifier) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(50.dp)
            .background(Color.LightGray, RoundedCornerShape(4.dp))
            .clickable { menuState.value = MenuState.USERS_LIST },
        contentAlignment = Alignment.CenterStart
    ) {
        Row(
            modifier = Modifier.padding(start = 8.dp)
        ) {
            Icon(
                imageVector = Icons.Default.List,
                contentDescription = "List",
                tint = Color.Black
            )
            Spacer(modifier = Modifier.width(20.dp))
            Text(
                text = "Users",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )
        }
    }
}
@Composable
fun AddEventButton(menuState: MutableState<MenuState>, modifier: Modifier = Modifier) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(50.dp)
            .background(Color.LightGray, RoundedCornerShape(4.dp))
            .clickable { menuState.value = MenuState.ADD_EVENT },
        contentAlignment = Alignment.CenterStart
    ) {
        Row(
            modifier = Modifier.padding(start = 8.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "Add",
                tint = Color.Black
            )
            Spacer(modifier = Modifier.width(20.dp))
            Text(
                text = "Add Event",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )

        }
    }
}
@Composable
fun EventsListButton(menuState: MutableState<MenuState>, modifier: Modifier = Modifier) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(50.dp)
            .background(Color.LightGray, RoundedCornerShape(4.dp))
            .clickable { menuState.value = MenuState.EVENTS_LIST },
        contentAlignment = Alignment.CenterStart
    ) {
        Row(
            modifier = Modifier.padding(start = 8.dp)
        ) {
            Icon(
                imageVector = Icons.Default.List,
                contentDescription = "List",
                tint = Color.Black
            )
            Spacer(modifier = Modifier.width(20.dp))
            Text(
                text = "Events",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )
        }
    }
}
@Composable
fun AboutAppButton(menuState: MutableState<MenuState>, modifier: Modifier = Modifier) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(50.dp)
            .background(Color.LightGray, RoundedCornerShape(4.dp))
            .clickable { menuState.value = MenuState.ABOUT_APP },
        contentAlignment = Alignment.CenterStart
    ) {
        Row(
            modifier = Modifier.padding(start = 8.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Info,
                contentDescription = "Info",
                tint = Color.Black
            )
            Spacer(modifier = Modifier.width(20.dp))
            Text(
                text = "About",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )
        }
    }
}


// TABS
@Composable
fun AddUserTab() {
    Text("Add User Tab")
}
@Composable
fun UsersListTab() {
    Text("Users List Tab")
}
@Composable
fun AddEventTab() {
    Text("Add Event Tab")
}
@Composable
fun EventsListTab() {
    Text("Events List Tab")
}
@Composable
fun AboutAppTab() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                "About application",
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text("Application for database management")
            Text("Authors: Anej Bregant, Alen Kolmanič, Tom Li Dobnik")
        }
    }
}


// HELPER FUNCTIONS
@Composable
fun DividerLine() {
    Divider(
        color = Color.LightGray,
        thickness = 1.dp,
        modifier = Modifier.padding(vertical = 4.dp)
    )
}
