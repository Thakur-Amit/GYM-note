package com.example.ui.screens.body

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.BodyCompositionSnapshot
import com.example.data.model.BodyMeasurementEntry
import com.example.data.model.BodyWeightEntry
import com.example.data.model.UserProfile
import com.example.data.repository.GymRepository
import com.example.ui.components.GymCard
import com.example.ui.components.GymGradientButton
import com.example.ui.theme.AccentError
import com.example.ui.theme.BackgroundDark
import com.example.ui.theme.DividerDark
import com.example.ui.theme.ElevatedSurface
import com.example.ui.theme.MutedText
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonEmerald
import com.example.ui.theme.PrimaryCard
import com.example.ui.theme.PrimaryPurple
import com.example.ui.theme.PrimaryText
import com.example.ui.theme.SecondaryText
import com.example.util.BodyMath
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ComprehensiveBodyDetailsScreen(
    repository: GymRepository,
    onNavigateBack: () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    val profile by repository.userProfile.collectAsStateWithLifecycle(initialValue = null)
    val latestWeightEntry by repository.latestBodyWeight.collectAsStateWithLifecycle(initialValue = null)
    val latestMeasurement by repository.latestBodyMeasurement.collectAsStateWithLifecycle(initialValue = null)

    // Preferred Units
    var weightUnit by remember { mutableStateOf("kg") }
    var lengthUnit by remember { mutableStateOf("cm") }

    // Profile Demographics
    var selectedSex by remember(profile) { mutableStateOf(profile?.gender ?: "male") }
    var heightInput by remember(profile) { mutableStateOf(profile?.heightCm?.let { if (it > 0) it.toString() else "" } ?: "") }
    var dobInput by remember(profile) { mutableStateOf(profile?.dateOfBirth?.let { if (it > 0) SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date(it)) else "" } ?: "") }

    // Body Weight
    var weightInput by remember(latestWeightEntry) { mutableStateOf(latestWeightEntry?.weight?.let { if (it > 0) it.toString() else "" } ?: "") }
    var weightNote by remember { mutableStateOf("") }

    // Core Circumferences (Waist, Neck, Hip)
    var waistInput by remember(latestMeasurement) { mutableStateOf(latestMeasurement?.waistCm?.toString() ?: "") }
    var neckInput by remember(latestMeasurement) { mutableStateOf(latestMeasurement?.neckCm?.toString() ?: "") }
    var hipInput by remember(latestMeasurement) { mutableStateOf(latestMeasurement?.hipsCm?.toString() ?: "") }

    // Additional Progress Circumferences
    var chestInput by remember(latestMeasurement) { mutableStateOf(latestMeasurement?.chestCm?.toString() ?: "") }
    var shouldersInput by remember(latestMeasurement) { mutableStateOf(latestMeasurement?.shouldersCm?.toString() ?: "") }
    var leftArmInput by remember(latestMeasurement) { mutableStateOf(latestMeasurement?.leftArmCm?.toString() ?: "") }
    var rightArmInput by remember(latestMeasurement) { mutableStateOf(latestMeasurement?.rightArmCm?.toString() ?: "") }
    var leftForearmInput by remember(latestMeasurement) { mutableStateOf(latestMeasurement?.leftForearmCm?.toString() ?: "") }
    var rightForearmInput by remember(latestMeasurement) { mutableStateOf(latestMeasurement?.rightForearmCm?.toString() ?: "") }
    var leftThighInput by remember(latestMeasurement) { mutableStateOf(latestMeasurement?.leftThighCm?.toString() ?: "") }
    var rightThighInput by remember(latestMeasurement) { mutableStateOf(latestMeasurement?.rightThighCm?.toString() ?: "") }
    var leftCalfInput by remember(latestMeasurement) { mutableStateOf(latestMeasurement?.leftCalfCm?.toString() ?: "") }
    var rightCalfInput by remember(latestMeasurement) { mutableStateOf(latestMeasurement?.rightCalfCm?.toString() ?: "") }

    // Calculated Age helper
    val calculatedAge: Int? = remember(dobInput) {
        try {
            if (dobInput.isNotBlank()) {
                val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                val d = sdf.parse(dobInput.trim())
                if (d != null) {
                    val birthCal = java.util.Calendar.getInstance().apply { time = d }
                    val nowCal = java.util.Calendar.getInstance()
                    var age = nowCal.get(java.util.Calendar.YEAR) - birthCal.get(java.util.Calendar.YEAR)
                    if (nowCal.get(java.util.Calendar.DAY_OF_YEAR) < birthCal.get(java.util.Calendar.DAY_OF_YEAR)) {
                        age--
                    }
                    if (age in 5..120) age else null
                } else null
            } else null
        } catch (e: Exception) {
            null
        }
    }

    // Live Metrics Calculations
    val weightKg = weightInput.toDoubleOrNull()?.let {
        if (weightUnit == "lbs") it * 0.45359237 else it
    }
    val heightCmVal = heightInput.toDoubleOrNull()?.let {
        if (lengthUnit == "in") it * 2.54 else it
    }
    val waistCmVal = waistInput.toDoubleOrNull()?.let {
        if (lengthUnit == "in") it * 2.54 else it
    }
    val neckCmVal = neckInput.toDoubleOrNull()?.let {
        if (lengthUnit == "in") it * 2.54 else it
    }
    val hipCmVal = hipInput.toDoubleOrNull()?.let {
        if (lengthUnit == "in") it * 2.54 else it
    }

    val liveBmi = remember(weightKg, heightCmVal) {
        if (weightKg != null && heightCmVal != null) {
            BodyMath.calculateBmi(weightKg, heightCmVal)
        } else null
    }

    val liveBmiCategory = remember(liveBmi, calculatedAge) {
        if (liveBmi != null) {
            BodyMath.getBmiCategory(liveBmi, calculatedAge)
        } else null
    }

    val liveBodyFat = remember(heightCmVal, waistCmVal, neckCmVal, hipCmVal, selectedSex) {
        if (heightCmVal != null && waistCmVal != null && neckCmVal != null) {
            BodyMath.calculateNavyBodyFatPercentage(
                heightCm = heightCmVal,
                waistCm = waistCmVal,
                neckCm = neckCmVal,
                hipCm = hipCmVal,
                sex = selectedSex
            )
        } else null
    }

    val liveFatFreeMass = remember(weightKg, liveBodyFat) {
        if (weightKg != null && liveBodyFat != null) {
            BodyMath.calculateFatFreeMass(weightKg, liveBodyFat)
        } else null
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Body Details & Composition",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = PrimaryText
                        )
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = PrimaryText
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = BackgroundDark)
            )
        },
        containerColor = BackgroundDark
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(BackgroundDark)
                .testTag("comprehensive_body_details_screen"),
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 1. Real-time Calculated Results Card
            item {
                GymCard(
                    backgroundColor = PrimaryCard,
                    borderColor = PrimaryPurple,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Text(
                            text = "Live Body Calculations",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = PrimaryPurple,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.5.sp
                            )
                        )
                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            // Estimated Body Fat %
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Est. Body Fat", style = MaterialTheme.typography.labelSmall.copy(color = SecondaryText))
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = if (liveBodyFat != null) "$liveBodyFat%" else "--",
                                    style = MaterialTheme.typography.headlineMedium.copy(
                                        fontWeight = FontWeight.Black,
                                        color = NeonCyan
                                    )
                                )
                                Text("U.S. Navy Method", style = MaterialTheme.typography.bodySmall.copy(color = MutedText, fontSize = 10.sp))
                            }

                            // Estimated Fat-Free Mass
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Est. Fat-Free Mass", style = MaterialTheme.typography.labelSmall.copy(color = SecondaryText))
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = if (liveFatFreeMass != null) "${liveFatFreeMass.toInt()} kg" else "--",
                                    style = MaterialTheme.typography.headlineMedium.copy(
                                        fontWeight = FontWeight.Black,
                                        color = NeonEmerald
                                    )
                                )
                                Text("Lean Body Mass", style = MaterialTheme.typography.bodySmall.copy(color = MutedText, fontSize = 10.sp))
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // BMI & Category
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(ElevatedSurface)
                                .padding(horizontal = 14.dp, vertical = 10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text("Body Mass Index (BMI)", style = MaterialTheme.typography.labelSmall.copy(color = SecondaryText))
                                Text(
                                    text = if (liveBmi != null) "$liveBmi" else "--",
                                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold, color = PrimaryText)
                                )
                            }
                            if (liveBmiCategory != null) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(PrimaryPurple.copy(alpha = 0.2f))
                                        .padding(horizontal = 10.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        text = liveBmiCategory,
                                        style = MaterialTheme.typography.labelSmall.copy(color = PrimaryPurple, fontWeight = FontWeight.Bold)
                                    )
                                }
                            } else if (calculatedAge != null && calculatedAge < 20) {
                                Text(
                                    text = "Under 20 (Pediatric)",
                                    style = MaterialTheme.typography.labelSmall.copy(color = MutedText)
                                )
                            }
                        }
                    }
                }
            }

            // 2. Profile Demographics Section
            item {
                GymCard(backgroundColor = PrimaryCard) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "1. Basic Profile",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = PrimaryText)
                        )
                        Spacer(modifier = Modifier.height(12.dp))

                        // Sex Selector (Male / Female)
                        Text("Biological Sex (Required for Navy Equation)", style = MaterialTheme.typography.labelSmall.copy(color = SecondaryText))
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            listOf("Male", "Female").forEach { sexOption ->
                                val isSelected = selectedSex.equals(sexOption, ignoreCase = true)
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(42.dp)
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(if (isSelected) PrimaryPurple else ElevatedSurface)
                                        .clickable { selectedSex = sexOption.lowercase() },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = sexOption,
                                        style = MaterialTheme.typography.labelMedium.copy(
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                            color = if (isSelected) PrimaryText else SecondaryText
                                        )
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Date of Birth
                        OutlinedTextField(
                            value = dobInput,
                            onValueChange = { dobInput = it },
                            label = { Text("Date of Birth (YYYY-MM-DD)") },
                            placeholder = { Text("e.g. 1998-05-15") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = ElevatedSurface,
                                unfocusedContainerColor = ElevatedSurface,
                                focusedBorderColor = PrimaryPurple,
                                unfocusedBorderColor = DividerDark,
                                focusedTextColor = PrimaryText,
                                unfocusedTextColor = PrimaryText
                            )
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        // Height with cm / in toggle
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            OutlinedTextField(
                                value = heightInput,
                                onValueChange = { heightInput = it },
                                label = { Text("Height ($lengthUnit)") },
                                placeholder = { Text("e.g. 178") },
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                modifier = Modifier.weight(1f),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedContainerColor = ElevatedSurface,
                                    unfocusedContainerColor = ElevatedSurface,
                                    focusedBorderColor = PrimaryPurple,
                                    unfocusedBorderColor = DividerDark,
                                    focusedTextColor = PrimaryText,
                                    unfocusedTextColor = PrimaryText
                                )
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            UnitToggle(
                                options = listOf("cm", "in"),
                                selected = lengthUnit,
                                onSelect = { lengthUnit = it }
                            )
                        }
                    }
                }
            }

            // 3. Body Weight Section
            item {
                GymCard(backgroundColor = PrimaryCard) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "2. Body Weight",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = PrimaryText)
                        )
                        Spacer(modifier = Modifier.height(12.dp))

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            OutlinedTextField(
                                value = weightInput,
                                onValueChange = { weightInput = it },
                                label = { Text("Weight ($weightUnit)") },
                                placeholder = { Text("e.g. 74.5") },
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                modifier = Modifier.weight(1f),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedContainerColor = ElevatedSurface,
                                    unfocusedContainerColor = ElevatedSurface,
                                    focusedBorderColor = PrimaryPurple,
                                    unfocusedBorderColor = DividerDark,
                                    focusedTextColor = PrimaryText,
                                    unfocusedTextColor = PrimaryText
                                )
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            UnitToggle(
                                options = listOf("kg", "lbs"),
                                selected = weightUnit,
                                onSelect = { weightUnit = it }
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        OutlinedTextField(
                            value = weightNote,
                            onValueChange = { weightNote = it },
                            label = { Text("Optional Note") },
                            placeholder = { Text("e.g. Morning fasted measurement") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = ElevatedSurface,
                                unfocusedContainerColor = ElevatedSurface,
                                focusedBorderColor = PrimaryPurple,
                                unfocusedBorderColor = DividerDark,
                                focusedTextColor = PrimaryText,
                                unfocusedTextColor = PrimaryText
                            )
                        )
                    }
                }
            }

            // 4. Core Circumferences (Waist, Neck, Hip)
            item {
                GymCard(backgroundColor = PrimaryCard) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "3. Core Circumferences (U.S. Navy Formula)",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = PrimaryText)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Measure waist horizontally at navel, neck at narrowest point, and hips at widest point.",
                            style = MaterialTheme.typography.bodySmall.copy(color = SecondaryText, fontSize = 11.sp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))

                        MeasurementField("Waist Circumference ($lengthUnit)", waistInput) { waistInput = it }
                        Spacer(modifier = Modifier.height(10.dp))
                        MeasurementField("Neck Circumference ($lengthUnit)", neckInput) { neckInput = it }

                        if (selectedSex.equals("female", ignoreCase = true)) {
                            Spacer(modifier = Modifier.height(10.dp))
                            MeasurementField("Hips Circumference ($lengthUnit) *Required for female", hipInput) { hipInput = it }
                        }
                    }
                }
            }

            // 5. Additional Progress Circumferences
            item {
                GymCard(backgroundColor = PrimaryCard) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "4. Additional Progress Circumferences (Optional)",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = PrimaryText)
                        )
                        Spacer(modifier = Modifier.height(12.dp))

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            Box(modifier = Modifier.weight(1f)) { MeasurementField("Chest", chestInput) { chestInput = it } }
                            Box(modifier = Modifier.weight(1f)) { MeasurementField("Shoulders", shouldersInput) { shouldersInput = it } }
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            Box(modifier = Modifier.weight(1f)) { MeasurementField("Left Arm", leftArmInput) { leftArmInput = it } }
                            Box(modifier = Modifier.weight(1f)) { MeasurementField("Right Arm", rightArmInput) { rightArmInput = it } }
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            Box(modifier = Modifier.weight(1f)) { MeasurementField("Left Forearm", leftForearmInput) { leftForearmInput = it } }
                            Box(modifier = Modifier.weight(1f)) { MeasurementField("Right Forearm", rightForearmInput) { rightForearmInput = it } }
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            Box(modifier = Modifier.weight(1f)) { MeasurementField("Left Thigh", leftThighInput) { leftThighInput = it } }
                            Box(modifier = Modifier.weight(1f)) { MeasurementField("Right Thigh", rightThighInput) { rightThighInput = it } }
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            Box(modifier = Modifier.weight(1f)) { MeasurementField("Left Calf", leftCalfInput) { leftCalfInput = it } }
                            Box(modifier = Modifier.weight(1f)) { MeasurementField("Right Calf", rightCalfInput) { rightCalfInput = it } }
                        }
                    }
                }
            }

            // 6. Save & Update Button
            item {
                GymGradientButton(
                    text = "Save Body Records",
                    icon = Icons.Default.Check,
                    onClick = {
                        coroutineScope.launch {
                            val now = System.currentTimeMillis()

                            // 1. Update UserProfile if demographics provided
                            profile?.let { p: UserProfile ->
                                var updatedProfile = p.copy(
                                    gender = selectedSex,
                                    preferredWeightUnit = weightUnit
                                )
                                if (heightCmVal != null && heightCmVal > 0.0) {
                                    updatedProfile = updatedProfile.copy(heightCm = heightCmVal)
                                }
                                if (dobInput.isNotBlank()) {
                                    try {
                                        val d = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(dobInput.trim())
                                        if (d != null) {
                                            updatedProfile = updatedProfile.copy(dateOfBirth = d.time)
                                        }
                                    } catch (e: Exception) { }
                                }
                                repository.updateProfile(updatedProfile)
                            }

                            // 2. Insert BodyWeightEntry if weight entered
                            if (weightKg != null && weightKg > 0.0) {
                                repository.addBodyWeightEntry(
                                    weight = weightKg,
                                    unit = "kg",
                                    bodyFatPercentage = liveBodyFat,
                                    muscleMass = liveFatFreeMass,
                                    date = now,
                                    notes = weightNote.ifBlank { null }
                                )
                            }

                            // 3. Insert BodyMeasurementEntry if any measurement entered
                            val hasAnyMeasurement = waistCmVal != null || neckCmVal != null || hipCmVal != null ||
                                    chestInput.isNotBlank() || shouldersInput.isNotBlank() ||
                                    leftArmInput.isNotBlank() || rightArmInput.isNotBlank() ||
                                    leftThighInput.isNotBlank() || rightThighInput.isNotBlank()

                            if (hasAnyMeasurement) {
                                repository.addBodyMeasurementEntry(
                                    BodyMeasurementEntry(
                                        measuredAt = now,
                                        waistCm = waistCmVal,
                                        neckCm = neckCmVal,
                                        hipsCm = hipCmVal,
                                        chestCm = chestInput.toDoubleOrNull()?.let { if (lengthUnit == "in") it * 2.54 else it },
                                        shouldersCm = shouldersInput.toDoubleOrNull()?.let { if (lengthUnit == "in") it * 2.54 else it },
                                        leftArmCm = leftArmInput.toDoubleOrNull()?.let { if (lengthUnit == "in") it * 2.54 else it },
                                        rightArmCm = rightArmInput.toDoubleOrNull()?.let { if (lengthUnit == "in") it * 2.54 else it },
                                        leftForearmCm = leftForearmInput.toDoubleOrNull()?.let { if (lengthUnit == "in") it * 2.54 else it },
                                        rightForearmCm = rightForearmInput.toDoubleOrNull()?.let { if (lengthUnit == "in") it * 2.54 else it },
                                        leftThighCm = leftThighInput.toDoubleOrNull()?.let { if (lengthUnit == "in") it * 2.54 else it },
                                        rightThighCm = rightThighInput.toDoubleOrNull()?.let { if (lengthUnit == "in") it * 2.54 else it },
                                        leftCalfCm = leftCalfInput.toDoubleOrNull()?.let { if (lengthUnit == "in") it * 2.54 else it },
                                        rightCalfCm = rightCalfInput.toDoubleOrNull()?.let { if (lengthUnit == "in") it * 2.54 else it },
                                        unit = "cm",
                                        notes = "Entered via comprehensive body details form",
                                        createdAt = now,
                                        updatedAt = now
                                    )
                                )
                            }

                            // 4. Insert BodyCompositionSnapshot if calculations available
                            if (weightKg != null && heightCmVal != null && liveBmi != null) {
                                repository.addCompositionSnapshot(
                                    BodyCompositionSnapshot(
                                        measuredAt = now,
                                        weightKgSnapshot = weightKg,
                                        heightCmSnapshot = heightCmVal,
                                        formulaInputSex = selectedSex,
                                        waistCmSnapshot = waistCmVal,
                                        neckCmSnapshot = neckCmVal,
                                        hipsCmSnapshot = hipCmVal,
                                        bmiValue = liveBmi,
                                        bodyFatPercentEstimate = liveBodyFat,
                                        fatFreeMassKgEstimate = liveFatFreeMass,
                                        formulaName = "U.S. Navy Circumference",
                                        formulaVersion = "1.0",
                                        notes = null,
                                        createdAt = now
                                    )
                                )
                            }

                            onNavigateBack()
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("save_body_records_btn")
                )
            }
        }
    }
}

@Composable
private fun MeasurementField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label, fontSize = 12.sp) },
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
        modifier = Modifier.fillMaxWidth(),
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = ElevatedSurface,
            unfocusedContainerColor = ElevatedSurface,
            focusedBorderColor = PrimaryPurple,
            unfocusedBorderColor = DividerDark,
            focusedTextColor = PrimaryText,
            unfocusedTextColor = PrimaryText
        )
    )
}

@Composable
private fun UnitToggle(
    options: List<String>,
    selected: String,
    onSelect: (String) -> Unit
) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(PrimaryCard)
            .padding(2.dp)
    ) {
        options.forEach { opt ->
            val isSel = opt == selected
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(if (isSel) ElevatedSurface else Color.Transparent)
                    .clickable { onSelect(opt) }
                    .padding(horizontal = 10.dp, vertical = 6.dp)
            ) {
                Text(
                    text = opt,
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal,
                        color = if (isSel) PrimaryText else MutedText
                    )
                )
            }
        }
    }
}
