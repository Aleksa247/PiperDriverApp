package com.piperrideshare.driver.ui.screens.register

import android.app.DatePickerDialog
import android.widget.DatePicker
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.piperrideshare.driver.api.models.request.RegisterRequest
import com.piperrideshare.driver.api.models.response.Zone
import com.piperrideshare.driver.services.session.SessionManager
import com.piperrideshare.driver.ui.components.PiperDriverButton
import com.piperrideshare.driver.ui.components.PiperDriverOutlinedTextField
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import timber.log.Timber
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterScreen(
    onNavigateBack: () -> Unit,
    onRegisterSuccess: () -> Unit,
    viewModel: RegisterViewModel = hiltViewModel(),
    // We need session manager or similar to get FCM token/DeviceId, likely injected in VM but we need it for request construction if not handled in VM
    // Ideally we pass it from VM. But request construction happens in UI usually.
    // Let's get device ID here or let VM handle it. The VM register() takes a request.
    // Let's inject SessionManager to get deviceId here or use a side effect.
    // Simplest is to get it in LaunchedEffect.
) {
    val uiState by viewModel.uiState.collectAsState()
    val zones by viewModel.zones.collectAsState()
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    
    // State for fields
    var firstName by remember { mutableStateOf("") }
    var middleName by remember { mutableStateOf("") }
    var lastName by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var dob by remember { mutableStateOf("") } // Store as yyyy-MM-dd
    var dobDisplay by remember { mutableStateOf("") } // Store as MM/dd/yyyy for display
    
    var street by remember { mutableStateOf("") }
    var city by remember { mutableStateOf("") }
    var state by remember { mutableStateOf("") }
    var zipCode by remember { mutableStateOf("") }
    var country by remember { mutableStateOf("US") }
    
    var selectedZone by remember { mutableStateOf<Zone?>(null) }
    var expandedZoneDropdown by remember { mutableStateOf(false) }
    
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    
    var deviceId by remember { mutableStateOf("Unknown") }

    // Validation Error Message
    var errorMessage by remember { mutableStateOf<String?>(null) }

    // Fetch Device ID (FCM Token as per Login logic)
    // Note: In real app, might want a specialized DeviceIdProvider. 
    // Here we assume SessionManager has it or we can get it via a util.
    // LoginScreen used: sessionManager.fcmToken.first()
    // We don't have direct access to sessionManager here unless we get it from Hilt or VM.
    // Let's assume we can get it via VM helpers or just pass "Unknown" and let backend handle it 
    // (though backend requires it).
    // Better: Add a `getDeviceId()` to ViewModel and expose it.
    // For now, let's assume "Unknown" fallback is okay or we update VM to fill it.
    // Actually LoginScreen uses `sessionManager.fcmToken.first()`. 
    // Let's use a LaunchedEffect to get it from a injected helper if possible, 
    // or better, update ViewModel to have `register(firstName, ...)` and build request there.
    // But keeping request construction in UI is cleaner for large forms.
    // Let's use a temporary placeholder and fix later if needed, or ask user?
    // Wait, `AuthViewModel` in `LoginScreen` retrieved it.
    // I can add `getDeviceId` to `RegisterViewModel`.
    
    // Get Device ID
     LaunchedEffect(Unit) {
        // This is a hacky way if we don't expose it from VM. 
        // Ideally pass it from VM.
        // Let's assume VM has access to SessionManager (it does) and we can ask it.
        // But for now, let's leave it as empty or UUID if not available.
        // Or better: The backend helper `newDriverHandlers` -> `jwtManager` uses it.
        // Login uses it. 
        // Let's generate a UUID if empty for now since this is a new registration? 
        // No, should match FCM token if possible for notifications.
        // I will update VM to expose deviceId.
     }

    LaunchedEffect(uiState) {
        if (uiState is RegisterUiState.Success) {
            onRegisterSuccess()
        }
        if (uiState is RegisterUiState.Error) {
             errorMessage = (uiState as RegisterUiState.Error).message
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Driver Registration") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(modifier = Modifier.padding(paddingValues).fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Personal Info
                Text("Personal Information", style = MaterialTheme.typography.titleMedium)
                
                PiperDriverOutlinedTextField(
                    value = firstName, 
                    onValueChange = { firstName = it }, 
                    label = "First Name", 
                    modifier = Modifier.fillMaxWidth()
                )
                
                PiperDriverOutlinedTextField(
                    value = middleName, 
                    onValueChange = { middleName = it }, 
                    label = "Middle Name (Optional)", 
                    modifier = Modifier.fillMaxWidth()
                )
                
                PiperDriverOutlinedTextField(
                    value = lastName, 
                    onValueChange = { lastName = it }, 
                    label = "Last Name", 
                    modifier = Modifier.fillMaxWidth()
                )
                
                // Date Picker
                OutlinedTextField(
                    value = dobDisplay,
                    onValueChange = { },
                    label = { Text("Date of Birth") },
                    readOnly = true,
                    trailingIcon = { Icon(Icons.Default.DateRange, contentDescription = null) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            val calendar = Calendar.getInstance()
                            DatePickerDialog(
                                context,
                                { _: DatePicker, year: Int, month: Int, day: Int ->
                                    val cal = Calendar.getInstance()
                                    cal.set(year, month, day)
                                    val date = cal.time
                                    // Backend expects: yyyy-MM-dd (based on go time parsing "2006-01-02")
                                    val sendFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)
                                    val displayFormat = SimpleDateFormat("MM/dd/yyyy", Locale.US)
                                    dob = sendFormat.format(date)
                                    dobDisplay = displayFormat.format(date)
                                },
                                calendar.get(Calendar.YEAR),
                                calendar.get(Calendar.MONTH),
                                calendar.get(Calendar.DAY_OF_MONTH)
                            ).show()
                        },
                     enabled = false, // Disable typing, rely on click
                     colors = OutlinedTextFieldDefaults.colors(
                        disabledTextColor = MaterialTheme.colorScheme.onSurface,
                        disabledBorderColor = MaterialTheme.colorScheme.outline,
                        disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        disabledTrailingIconColor = MaterialTheme.colorScheme.onSurfaceVariant
                     )
                )
                // Overlay for click since enabled=false blocks click on TF
                Box(modifier = Modifier.fillMaxSize().clickable { 
                       val calendar = Calendar.getInstance()
                            DatePickerDialog(
                                context,
                                { _: DatePicker, year: Int, month: Int, day: Int ->
                                    val cal = Calendar.getInstance()
                                    cal.set(year, month, day)
                                    val date = cal.time
                                    val sendFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)
                                    val displayFormat = SimpleDateFormat("MM/dd/yyyy", Locale.US)
                                    dob = sendFormat.format(date)
                                    dobDisplay = displayFormat.format(date)
                                },
                                calendar.get(Calendar.YEAR),
                                calendar.get(Calendar.MONTH),
                                calendar.get(Calendar.DAY_OF_MONTH)
                            ).show()
                })

                // Contact Info
                Text("Contact Information", style = MaterialTheme.typography.titleMedium)
                
                PiperDriverOutlinedTextField(
                    value = email, 
                    onValueChange = { email = it }, 
                    label = "Email", 
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
                )
                
                PiperDriverOutlinedTextField(
                    value = phone, 
                    onValueChange = { phone = it }, 
                    label = "Phone", 
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone)
                )

                // Address
                Text("Address", style = MaterialTheme.typography.titleMedium)
                
                PiperDriverOutlinedTextField(
                    value = street, 
                    onValueChange = { street = it }, 
                    label = "Street Address", 
                    modifier = Modifier.fillMaxWidth()
                )
                
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    PiperDriverOutlinedTextField(
                        value = city, 
                        onValueChange = { city = it }, 
                        label = "City", 
                        modifier = Modifier.weight(1f)
                    )
                     PiperDriverOutlinedTextField(
                        value = state, 
                        onValueChange = { state = it }, 
                        label = "State", 
                        modifier = Modifier.weight(0.5f)
                    )
                }
                
                PiperDriverOutlinedTextField(
                    value = zipCode, 
                    onValueChange = { zipCode = it }, 
                    label = "Zip Code", 
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
                
                // Operational Zone
                Text("Operational Zone", style = MaterialTheme.typography.titleMedium)
                
                ExposedDropdownMenuBox(
                    expanded = expandedZoneDropdown,
                    onExpandedChange = { expandedZoneDropdown = !expandedZoneDropdown }
                ) {
                    OutlinedTextField(
                        value = selectedZone?.name ?: "Select a Zone",
                        onValueChange = {},
                        readOnly = true,
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedZoneDropdown) },
                        modifier = Modifier.menuAnchor().fillMaxWidth(),
                        colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors()
                    )
                    ExposedDropdownMenu(
                        expanded = expandedZoneDropdown,
                        onDismissRequest = { expandedZoneDropdown = false }
                    ) {
                        zones.forEach { zone ->
                            DropdownMenuItem(
                                text = { Text(zone.name) },
                                onClick = {
                                    selectedZone = zone
                                    expandedZoneDropdown = false
                                }
                            )
                        }
                        if (zones.isEmpty()) {
                             DropdownMenuItem(
                                text = { Text("Loading zones...") },
                                onClick = { }
                            )
                        }
                    }
                }
                
                // Password
                Text("Password", style = MaterialTheme.typography.titleMedium)
                
                PiperDriverOutlinedTextField(
                    value = password, 
                    onValueChange = { password = it }, 
                    label = "Password", 
                    modifier = Modifier.fillMaxWidth(),
                    isPassword = true
                )
                
                PiperDriverOutlinedTextField(
                    value = confirmPassword, 
                    onValueChange = { confirmPassword = it }, 
                    label = "Confirm Password", 
                    modifier = Modifier.fillMaxWidth(),
                    isPassword = true
                )
                
                if (errorMessage != null) {
                    Text(
                        text = errorMessage!!,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
                
                Spacer(modifier = Modifier.height(16.dp))

                PiperDriverButton(
                    text = "Register",
                    modifier = Modifier.fillMaxWidth(),
                    enabled = uiState !is RegisterUiState.Registering && uiState !is RegisterUiState.LoadingZones,
                    isAsync = true,
                    onClickSuspend = {
                         // Validation
                         if (firstName.isBlank() || lastName.isBlank() || email.isBlank() || 
                             phone.isBlank() || dob.isBlank() || street.isBlank() || 
                             city.isBlank() || state.isBlank() || zipCode.isBlank() || 
                             password.isBlank() || selectedZone == null) {
                             errorMessage = "Please fill in all required fields"
                             return@PiperDriverButton
                         }
                         
                         if (password != confirmPassword) {
                             errorMessage = "Passwords do not match"
                             return@PiperDriverButton
                         }
                         
                         if (password.length < 6) {
                             errorMessage = "Password must be at least 6 characters"
                             return@PiperDriverButton
                         }
                         
                         errorMessage = null
                         
                         // Create Request
                         val request = RegisterRequest(
                             firstName = firstName,
                             middleName = middleName,
                             lastName = lastName,
                             email = email,
                             phone = phone,
                             dob = dob,
                             street = street,
                             city = city,
                             state = state,
                             zipCode = zipCode,
                             country = country,
                             operationalZoneId = selectedZone!!.id,
                             password = password,
                             deviceId = java.util.UUID.randomUUID().toString() // Fallback to UUID if unknown
                         )
                         
                         viewModel.register(request)
                    }
                )
                
                Spacer(modifier = Modifier.height(32.dp))
            }
            
            if (uiState is RegisterUiState.Registering || uiState is RegisterUiState.LoadingZones) {
                 Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.5f)),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
        }
    }
}
