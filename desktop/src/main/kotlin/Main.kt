import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.CheckBoxOutlineBlank
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.window.*
import data.CreateUser
import data.PublicUser
import data.Location
import data.LocationOutline
import data.Coordinate
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import requests.*


enum class MenuState {
    USERS_LIST, ABOUT_APP, ADD_USER, ADD_EVENT, EVENTS_LIST, ADD_LOCATION, LOCATIONS_LIST,
    ADD_LOCATION_OUTLINE, LOCATION_OUTLINES_LIST
}


@Composable
@Preview
fun App() {
    val menuState = remember { mutableStateOf(MenuState.ADD_EVENT) }
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
            size = DpSize(1050.dp, 800.dp)
        ),
        undecorated = false,
        resizable = true
    ) {
        (window as? java.awt.Window)?.minimumSize = java.awt.Dimension(1050, 800)
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
                AddLocationButton(menuState)
                LocationsListButton(menuState)
                DividerLine()
                AddLocationOutlineButton(menuState)
                LocationOutlinesListButton(menuState)
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
                MenuState.ADD_USER -> AddUserTab(menuState)
                MenuState.USERS_LIST -> UsersListTab()
                MenuState.ADD_EVENT -> AddEventTab()
                MenuState.EVENTS_LIST -> EventsListTab()
                MenuState.ABOUT_APP -> AboutAppTab()
                MenuState.ADD_LOCATION -> AddLocationTab(menuState)
                MenuState.LOCATIONS_LIST -> LocationsListTab()
                MenuState.ADD_LOCATION_OUTLINE -> AddLocationOutlineTab()
                MenuState.LOCATION_OUTLINES_LIST -> LocationOutlinesListTab()
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

@Composable
fun AddLocationButton(menuState: MutableState<MenuState>, modifier: Modifier = Modifier) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(50.dp)
            .background(Color.LightGray, RoundedCornerShape(4.dp))
            .clickable { menuState.value = MenuState.ADD_LOCATION },
        contentAlignment = Alignment.CenterStart
    ) {
        Row(
            modifier = Modifier.padding(start = 8.dp)
        ) {
            Icon(
                imageVector = Icons.Filled.LocationOn,
                contentDescription = "location",
                tint = Color.Black
            )
            Spacer(modifier = Modifier.width(20.dp))
            Text(
                text = "Add Location",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )
        }
    }
}

@Composable
fun AddLocationOutlineButton(menuState: MutableState<MenuState>, modifier: Modifier = Modifier) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(50.dp)
            .background(Color.LightGray, RoundedCornerShape(4.dp))
            .clickable { menuState.value = MenuState.ADD_LOCATION_OUTLINE },
        contentAlignment = Alignment.CenterStart
    ) {
        Row(
            modifier = Modifier.padding(start = 8.dp)
        ) {
            Icon(
                imageVector = Icons.Filled.LocationOn,
                contentDescription = "locationOutline",
                tint = Color.Black
            )
            Spacer(modifier = Modifier.width(20.dp))
            Text(
                text = "Add Location Outline",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )
        }
    }
}

@Composable
fun LocationOutlinesListButton(menuState: MutableState<MenuState>, modifier: Modifier = Modifier) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(50.dp)
            .background(Color.LightGray, RoundedCornerShape(4.dp))
            .clickable { menuState.value = MenuState.LOCATION_OUTLINES_LIST },
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
                text = "Location Outlines",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )
        }
    }
}

@Composable
fun LocationsListButton(menuState: MutableState<MenuState>, modifier: Modifier = Modifier) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(50.dp)
            .background(Color.LightGray, RoundedCornerShape(4.dp))
            .clickable { menuState.value = MenuState.LOCATIONS_LIST },
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
                text = "Locations",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )
        }
    }
}


// TABS
@Composable
fun AddUserTab(menuState: MutableState<MenuState>) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        var username by remember { mutableStateOf("") }
        var email by remember { mutableStateOf("") }
        var password by remember { mutableStateOf("") }
        var firstname by remember { mutableStateOf("") }
        var lastname by remember { mutableStateOf("") }
        var showValidationDialog by remember { mutableStateOf(false) }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                "Add a new User",
                fontWeight = FontWeight.Bold,
                fontSize = 30.sp,
            )
            Spacer(modifier = Modifier.height(30.dp))
            TextInputField(
                value = username,
                onValueChange = { username = it },
                label = "Username",
                icon = Icons.Filled.AccountCircle
            )
            TextInputField(
                value = firstname,
                onValueChange = { firstname = it },
                label = "First name",
                icon = Icons.Filled.Person
            )
            TextInputField(
                value = lastname,
                onValueChange = { lastname = it },
                label = "Last name",
                icon = Icons.Filled.PersonOutline
            )
            TextInputField(
                value = email,
                onValueChange = { email = it },
                label = "E-mail",
                icon = Icons.Filled.Email
            )
            PasswordInputField(
                value = password,
                onValueChange = { password = it },
            )
            Spacer(modifier = Modifier.height(8.dp))
            Button(onClick = {
                if (username.isBlank() || firstname.isBlank() || lastname.isBlank() || email.isBlank() || password.isBlank()) {
                    showValidationDialog = true
                } else {
                    val user = CreateUser(username, firstname, lastname, email, password)
                    CoroutineScope(Dispatchers.IO).launch {
                        try {
                            val response = createUser(user)
                            println("User created successfully: $response")
                        } catch (e: Exception) {
                            println("Failed to create user: ${e.message}")
                        }
                    }
                    menuState.value = MenuState.USERS_LIST
                }
            }) {
                Text("Add User")
            }
        }

        if (showValidationDialog) {
            AlertBox(
                title = "Unfilled form",
                text = "You have to complete the whole user creation form.",
                onDismiss = { showValidationDialog = false }
            )
        }
    }
}

@Composable
fun UsersListTab() {
    val users = remember { mutableStateOf<List<PublicUser>>(emptyList()) }
    val selectedUser = remember { mutableStateOf<PublicUser?>(null) }

    LaunchedEffect(Unit) {
        users.value = getAllUsers() ?: emptyList()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            "All Users",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        users.value.forEach { user ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
                    .clickable {
                        selectedUser.value = user
                        println("Clicked on ${user.firstname} ${user.lastname}")

                    },
                elevation = 4.dp,
            ) {
                Row(
                    modifier = Modifier
                        .padding(16.dp)
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Filled.Person,
                            contentDescription = "Person Icon"
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text("${user.firstname} ${user.lastname}")
                    }
                    Box(modifier = Modifier.align(Alignment.CenterVertically)) {
                        Text(user.username, color = Color.Gray)
                    }
                }
            }
        }
    }
    val userToEdit = selectedUser.value
    if (userToEdit != null) {
        EditUserModal(
            user = userToEdit,
            onDismiss = { selectedUser.value = null },
            onSave = { updatedUser ->
                // save logic
                println("Updated user: $updatedUser")
                selectedUser.value = null
            }
        )
    }

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

@Composable
fun LocationsListTab() {
    val locations = remember { mutableStateOf<List<Location>>(emptyList()) }
    val selectedLoc = remember { mutableStateOf<Location?>(null) }

    LaunchedEffect(Unit) {
        locations.value = getAllLocations() ?: emptyList()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            "All Locations",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        locations.value.forEach { location ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
                    .clickable {
                        selectedLoc.value = location
                        println("Clicked on ${location.info}")
                    },
                elevation = 4.dp,
            ) {
                Row(
                    modifier = Modifier
                        .padding(16.dp)
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = if (location.location_outline_fk != null) Icons.Outlined.CheckBoxOutlineBlank else Icons.Filled.LocationOn,
                            contentDescription = "Location Icon"
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(location.info)
                    }
                }
            }
        }
    }
    val locToEdit = selectedLoc.value
    if (locToEdit != null) {
        EditLocationModal(
            location = locToEdit,
            onDismiss = { selectedLoc.value = null },
            onSave = { updatedLocation ->
                // save logic
                println("Updated user: $updatedLocation")
                selectedLoc.value = null
            }
        )
    }

}

@Composable
fun AddLocationTab(menuState: MutableState<MenuState>) {
    var selectedOption by remember { mutableStateOf<String?>(null) }
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        if (selectedOption == null) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(32.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                ClickableBox(label = "Point", onClick = { selectedOption = "Point" })
                ClickableBox(label = "Polygon", onClick = { selectedOption = "Polygon" })
            }
        } else {
            when (selectedOption) {
                "Point" -> PointForm(onBack = { selectedOption = null }, menuState = menuState)
                "Polygon" -> PolygonForm(onBack = { selectedOption = null }, menuState = menuState)
            }
        }
    }
}

@Composable
fun PointForm(onBack: () -> Unit, menuState: MutableState<MenuState>) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        var info by remember { mutableStateOf("") }
        var longitude by remember { mutableStateOf("") }
        var latitude by remember { mutableStateOf("") }
        var showValidationDialog by remember { mutableStateOf(false) }
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                "Add a new Point",
                fontWeight = FontWeight.Bold,
                fontSize = 30.sp,
            )
            Spacer(modifier = Modifier.height(30.dp))
            TextInputField(
                value = info,
                onValueChange = { info = it },
                label = "Info",
                icon = Icons.Filled.Info
            )
            TextInputField(
                value = longitude,
                onValueChange = { longitude = it },
                label = "Longitude",
                icon = Icons.Filled.Public
            )
            TextInputField(
                value = latitude,
                onValueChange = { latitude = it },
                label = "Latitude",
                icon = Icons.Filled.Explore
            )
            Spacer(modifier = Modifier.height(8.dp))
            Button(onClick = {
                if (info.isBlank() || longitude.isBlank() || latitude.isBlank()) {
                    showValidationDialog = true
                } else {
                    val location = Location(
                        info = info,
                        longitude = longitude.toDoubleOrNull(),
                        latitude = latitude.toDoubleOrNull()
                    )
                    CoroutineScope(Dispatchers.IO).launch {
                        try {
                            val response = createLocation(location)
                            println("User created successfully: $response")
                        } catch (e: Exception) {
                            println("Failed to create location: ${e.message}")
                        }
                    }
                    menuState.value = MenuState.LOCATIONS_LIST
                }
            }) {
                Text("Add Point")
            }
            Button(onClick = onBack) {
                Text("Back")
            }
        }
        if (showValidationDialog) {
            AlertBox(
                title = "Unfilled form",
                text = "You have to complete the whole Point creation form",
                onDismiss = { showValidationDialog = false }
            )
        }
    }
}

@Composable
fun PolygonForm(onBack: () -> Unit, menuState: MutableState<MenuState>) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        var info by remember { mutableStateOf("") }
        var locationOutlineFK by remember { mutableStateOf("") }
        var showValidationDialog by remember { mutableStateOf(false) }
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                "Add a new Polygon",
                fontWeight = FontWeight.Bold,
                fontSize = 30.sp,
            )
            Spacer(modifier = Modifier.height(30.dp))
            TextInputField(
                value = info,
                onValueChange = { info = it },
                label = "Info",
                icon = Icons.Filled.Info
            )
            TextInputField(
                value = locationOutlineFK,
                onValueChange = { locationOutlineFK = it },
                label = "Location Outline FK Integer",
                icon = Icons.Filled.LocationOn
            )
            Spacer(modifier = Modifier.height(8.dp))
            Button(onClick = {
                if (info.isBlank() || locationOutlineFK.isBlank()) {
                    showValidationDialog = true
                } else {
                    val location = Location(info = info, location_outline_fk = locationOutlineFK.toIntOrNull())
                    CoroutineScope(Dispatchers.IO).launch {
                        try {
                            val response = createLocation(location)
                            println("Polygon created successfully: $response")
                        } catch (e: Exception) {
                            println("Failed to create location: ${e.message}")
                        }
                    }
                    menuState.value = MenuState.LOCATIONS_LIST
                }
            }) {
                Text("Add Polygon")
            }
            Button(onClick = onBack) {
                Text("Back")
            }
        }
        if (showValidationDialog) {
            AlertBox(
                title = "Unfilled form",
                text = "You have to complete the whole Polygon creation form",
                onDismiss = { showValidationDialog = false }
            )
        }
    }
}

@Composable
fun AddLocationOutlineTab() {
    var info by remember { mutableStateOf("") }
    var longitude by remember { mutableStateOf("") }
    var latitude by remember { mutableStateOf("") }
    val points = remember { mutableStateListOf<Coordinate>() }
    var showValidationDialog by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                "Add Location Outline", fontWeight = FontWeight.Bold,
                fontSize = 30.sp
            )
            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(value = info, onValueChange = { info = it }, label = { Text("Info") })
            OutlinedTextField(
                value = longitude,
                onValueChange = { longitude = it },
                label = { Text("Longitude") },
            )
            Spacer(Modifier.width(8.dp))
            OutlinedTextField(
                value = latitude,
                onValueChange = { latitude = it },
                label = { Text("Latitude") },

            )

            Spacer(modifier = Modifier.height(8.dp))
            Button(onClick = {
                val lon = longitude.toDoubleOrNull()
                val lat = latitude.toDoubleOrNull()
                if (lon != null && lat != null) {
                    points.add(Coordinate(lon, lat))
                    longitude = ""
                    latitude = ""
                } else {
                    showValidationDialog = true
                }
            }) {
                Text("Add Point")
            }

            Spacer(Modifier.height(16.dp))

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(points) { point: Coordinate ->
                    Box(
                        modifier = Modifier
                            .background(color = Color.LightGray, shape = RoundedCornerShape(8.dp))
                            .border(1.dp, Color.Gray, RoundedCornerShape(8.dp))
                            .padding(16.dp)
                    ) {
                        Text(
                            text = "(${point.longitude}, ${point.latitude})",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color.Black
                        )
                    }
                }
            }


            Spacer(Modifier.height(16.dp))

            Button(
                onClick = {
                    CoroutineScope(Dispatchers.IO).launch {
                        createLocationOutline(info, points.toList())
                    }
                },
                enabled = info.isNotBlank() && points.size >= 3
            ) {
                Text("Add Location Outline")
            }
        }
        if (showValidationDialog) {
            AlertBox(
                title = "Invalid Coordinates",
                text = "Please enter valid longitude and latitude values.",
                onDismiss = { showValidationDialog = false }
            )
        }
    }

}


@Composable
fun LocationOutlinesListTab() {
    val locOutlines = remember { mutableStateOf<List<LocationOutline>>(emptyList()) }
    val selectedLoc = remember { mutableStateOf<LocationOutline?>(null) }

    LaunchedEffect(Unit) {
        locOutlines.value = getAllLocationOutlines() ?: emptyList()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            "All Location Outlines",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        locOutlines.value.forEach { outline ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
                    .clickable {
                        selectedLoc.value = outline
                        println("Clicked on ${outline.id}")
                    },
                elevation = 4.dp,
            ) {
                Row(
                    modifier = Modifier
                        .padding(16.dp)
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Outlined.CheckBoxOutlineBlank,
                            contentDescription = "Outline Icon"
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text("Outline ID: ${outline.id}")
                    }
                }
            }
        }
    }

    val outlineToShow = selectedLoc.value
    if (outlineToShow != null) {
        OutlinePointsModal(
            outline = outlineToShow,
            onDismiss = { selectedLoc.value = null }
        )
    }
}

// HELPER FUNCTIONS
@Composable
fun ClickableBox(label: String, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(250.dp)
            .background(Color.LightGray, RoundedCornerShape(12.dp))
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Black
        )
    }
}

@Composable
fun DividerLine() {
    Divider(
        color = Color.LightGray,
        thickness = 1.dp,
        modifier = Modifier.padding(vertical = 4.dp)
    )
}

@Composable
fun TextInputField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    icon: ImageVector,
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        singleLine = true,
        leadingIcon = {
            Icon(
                imageVector = icon,
                contentDescription = "$label Icon"
            )
        },
        modifier = Modifier
            .width(300.dp)
            .height(70.dp)
    )
}

@Composable
fun PasswordInputField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var passwordVisibility by remember { mutableStateOf(false) }
    val icon = if (passwordVisibility) {
        Icons.Filled.Visibility
    } else {
        Icons.Filled.VisibilityOff
    }
    OutlinedTextField(
        value = value,
        label = { Text("Password") },
        onValueChange = onValueChange,
        singleLine = true,
        leadingIcon = {
            Icon(
                imageVector = Icons.Filled.Lock,
                contentDescription = "Lock Icon"
            )
        },
        trailingIcon = {
            IconButton(onClick = { passwordVisibility = !passwordVisibility }) {
                Icon(
                    imageVector = icon,
                    contentDescription = if (passwordVisibility) "Visibility Icon" else "VisibilityOff Icon"
                )
            }
        },
        visualTransformation = if (passwordVisibility) VisualTransformation.None else PasswordVisualTransformation(),
        modifier = Modifier
            .width(300.dp)
            .height(70.dp)
    )
}

@Composable
fun AlertBox(
    title: String,
    text: String,
    onDismiss: (() -> Unit)? = null,
) {
    AlertDialog(
        onDismissRequest = { onDismiss?.invoke() },
        title = { Text(title) },
        text = { Text(text) },
        confirmButton = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                contentAlignment = Alignment.BottomEnd
            ) {
                TextButton(
                    onClick = { onDismiss?.invoke() },
                    modifier = Modifier
                        .background(Color(0xFF6200EE), RoundedCornerShape(4.dp))
                ) {
                    Text("Ok", color = Color.White)
                }
            }

        }
    )
}

@Composable
fun EditUserModal(user: PublicUser, onDismiss: () -> Unit, onSave: (CreateUser) -> Unit) {
    var editedUsername by remember { mutableStateOf(user.username) }
    var editedFirstname by remember { mutableStateOf(user.firstname) }
    var editedLastname by remember { mutableStateOf(user.lastname) }
    var editedEmail by remember { mutableStateOf(user.email) }
    var editedPassword by remember { mutableStateOf("") }

    Dialog(onDismissRequest = onDismiss) {
        Surface(shape = RoundedCornerShape(8.dp)) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Edit User")

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = editedUsername,
                    onValueChange = { editedUsername = it },
                    label = { Text("Username") }
                )

                OutlinedTextField(
                    value = editedFirstname,
                    onValueChange = { editedFirstname = it },
                    label = { Text("First Name") }
                )

                OutlinedTextField(
                    value = editedLastname,
                    onValueChange = { editedLastname = it },
                    label = { Text("Last Name") }
                )

                OutlinedTextField(
                    value = editedEmail,
                    onValueChange = { editedEmail = it },
                    label = { Text("Email") }
                )

                OutlinedTextField(
                    value = editedPassword,
                    onValueChange = { editedPassword = it },
                    label = { Text("New Password") },
                    visualTransformation = PasswordVisualTransformation(),
                )

                Spacer(modifier = Modifier.height(16.dp))

                Row(horizontalArrangement = Arrangement.End, modifier = Modifier.fillMaxWidth()) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel")
                    }
                    Button(onClick = {
                        if (editedPassword.isNotBlank()) {
                            onSave(
                                CreateUser(
                                    username = editedUsername,
                                    firstname = editedFirstname,
                                    lastname = editedLastname,
                                    email = editedEmail,
                                    password = editedPassword
                                )
                            )
                            onDismiss()
                        }
                    }) {
                        Text("Save")
                    }
                }
            }
        }
    }
}

@Composable
fun EditLocationModal(location: Location, onDismiss: () -> Unit, onSave: (Location) -> Unit) {
    var editedInfo by remember { mutableStateOf(location.info) }
    var editedLongitude by remember { mutableStateOf(location.longitude.toString()) }
    var editedLatitude by remember { mutableStateOf(location.latitude.toString()) }
    var editedLocOutline by remember { mutableStateOf(location.location_outline_fk.toString()) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(shape = RoundedCornerShape(8.dp)) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Edit Location")

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = editedInfo,
                    onValueChange = { editedInfo = it },
                    label = { Text("Info") }
                )

                OutlinedTextField(
                    value = editedLongitude,
                    onValueChange = { editedLongitude = it },
                    label = { Text("Longitude") }
                )

                OutlinedTextField(
                    value = editedLatitude,
                    onValueChange = { editedLatitude = it },
                    label = { Text("Latitude") }
                )

                OutlinedTextField(
                    value = editedLocOutline,
                    onValueChange = { editedLocOutline = it },
                    label = { Text("Location Outline FK Int") }
                )

                Spacer(modifier = Modifier.height(16.dp))

                Row(horizontalArrangement = Arrangement.End, modifier = Modifier.fillMaxWidth()) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel")
                    }
                    Button(onClick = {
                        if (editedInfo.isNotBlank() || editedLongitude.isNotBlank() || editedLatitude.isNotBlank() || editedLocOutline.isNotBlank()) {
                            onSave(
                                Location(
                                    id = location.id,
                                    info = editedInfo,
                                    longitude = editedLongitude.toDoubleOrNull(),
                                    latitude = editedLatitude.toDoubleOrNull(),
                                    location_outline_fk = editedLocOutline.toIntOrNull()
                                )
                            )
                            onDismiss()
                        }
                    }) {
                        Text("Save")
                    }
                }
            }
        }
    }
}

@Composable
fun OutlinePointsModal(outline: LocationOutline, onDismiss: () -> Unit) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(shape = RoundedCornerShape(8.dp)) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Location Outline Points", fontWeight = FontWeight.Bold, fontSize = 20.sp)
                Spacer(modifier = Modifier.height(8.dp))
                outline.points.forEach { point ->
                    Text("• (${point.longitude}, ${point.latitude})", fontSize = 16.sp)
                }
                Spacer(modifier = Modifier.height(16.dp))
                Button(onClick = onDismiss) {
                    Text("Close")
                }
            }
        }
    }
}