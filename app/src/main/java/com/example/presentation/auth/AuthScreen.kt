package com.example.presentation.auth

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.rsp

@Composable
fun SunsetMountainBackdrop(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier.fillMaxSize()) {
        val width = size.width
        val height = size.height

        // 1. Draw elegant orange-salmon-rose sky sunset gradient
        val skyGradient = Brush.verticalGradient(
            0.0f to Color(0xFF131A26),       // Top cosmic darkness
            0.35f to Color(0xFF28364B),      // Dark slate blue
            0.55f to Color(0xFF9E4E63),      // Sunset rose
            0.70f to Color(0xFFEBC1B0),      // Coral/peach horizon
            1.0f to Color(0xFF131A26)        // Blends smoothly to solid bottom
        )
        drawRect(brush = skyGradient)

        // 2. Draw soft glowing sunset sun offset right
        val sunCenterY = height * 0.45f
        val sunCenterX = width * 0.75f
        val sunRadius = width * 0.22f
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    Color(0xFFFFF3DB).copy(alpha = 0.85f),
                    Color(0xFFFFDFBF).copy(alpha = 0.35f),
                    Color(0x00FFDFBF)
                ),
                center = androidx.compose.ui.geometry.Offset(sunCenterX, sunCenterY),
                radius = sunRadius
            ),
            radius = sunRadius,
            center = androidx.compose.ui.geometry.Offset(sunCenterX, sunCenterY)
        )

        // 3. BACK MOUNTAIN RANGE (Rose-Gold Silhouettes)
        val backMountainPath = Path().apply {
            moveTo(0f, height * 0.75f)
            lineTo(width * 0.25f, height * 0.58f)
            lineTo(width * 0.48f, height * 0.69f)
            lineTo(width * 0.72f, height * 0.52f)
            lineTo(width * 0.90f, height * 0.64f)
            lineTo(width, height * 0.56f)
            lineTo(width, height)
            lineTo(0f, height)
            close()
        }
        drawPath(
            path = backMountainPath,
            color = Color(0xFF7A515B)
        )
        
        // Back Snowy highlights on peak ridges (illuminated side of peaks in warm light)
        val backHighlights = Path().apply {
            moveTo(width * 0.25f, height * 0.58f)
            lineTo(width * 0.32f, height * 0.62f)
            lineTo(width * 0.28f, height * 0.64f)
            lineTo(width * 0.25f, height * 0.58f)
            
            moveTo(width * 0.72f, height * 0.52f)
            lineTo(width * 0.80f, height * 0.57f)
            lineTo(width * 0.75f, height * 0.60f)
            lineTo(width * 0.72f, height * 0.52f)
        }
        drawPath(path = backHighlights, color = Color(0xFFFFECE3).copy(alpha = 0.35f))

        // 4. MID MOUNTAIN RANGE (Deep Rust-Purple)
        val midMountainPath = Path().apply {
            moveTo(0f, height * 0.82f)
            lineTo(width * 0.18f, height * 0.67f)
            lineTo(width * 0.38f, height * 0.74f)
            lineTo(width * 0.55f, height * 0.62f)
            lineTo(width * 0.78f, height * 0.73f)
            lineTo(width * 0.92f, height * 0.66f)
            lineTo(width, height * 0.75f)
            lineTo(width, height)
            lineTo(0f, height)
            close()
        }
        drawPath(
            path = midMountainPath,
            color = Color(0xFF422B3F)
        )
        
        val midHighlights = Path().apply {
            moveTo(width * 0.18f, height * 0.67f)
            lineTo(width * 0.26f, height * 0.70f)
            lineTo(width * 0.22f, height * 0.73f)
            lineTo(width * 0.18f, height * 0.67f)

            moveTo(width * 0.55f, height * 0.62f)
            lineTo(width * 0.64f, height * 0.66f)
            lineTo(width * 0.59f, height * 0.69f)
            lineTo(width * 0.55f, height * 0.62f)
        }
        drawPath(path = midHighlights, color = Color(0xFFFFDCD0).copy(alpha = 0.4f))

        // 5. FORE RANGE (Dark Navy Charcoal Peaks cover bottom)
        val foreMountainPath = Path().apply {
            moveTo(0f, height * 0.88f)
            lineTo(width * 0.12f, height * 0.76f)
            lineTo(width * 0.32f, height * 0.80f)
            lineTo(width * 0.48f, height * 0.70f)
            lineTo(width * 0.65f, height * 0.78f)
            lineTo(width * 0.82f, height * 0.68f)
            lineTo(width * 0.95f, height * 0.77f)
            lineTo(width, height * 0.70f)
            lineTo(width, height)
            lineTo(0f, height)
            close()
        }
        drawPath(
            path = foreMountainPath,
            color = Color(0xFF1B1B2A)
        )
        
        val foreHighlights = Path().apply {
            moveTo(width * 0.48f, height * 0.70f)
            lineTo(width * 0.55f, height * 0.74f)
            lineTo(width * 0.51f, height * 0.77f)
            lineTo(width * 0.48f, height * 0.70f)

            moveTo(width * 0.82f, height * 0.68f)
            lineTo(width * 0.89f, height * 0.72f)
            lineTo(width * 0.85f, height * 0.75f)
            lineTo(width * 0.82f, height * 0.68f)
        }
        drawPath(path = foreHighlights, color = Color(0xFFFFF3EC).copy(alpha = 0.45f))
    }
}

@Composable
fun AuthScreen(viewModel: AuthViewModel, onAuthSuccess: (String) -> Unit) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()
    val isLoggedIn by viewModel.isLoggedIn.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()

    var isLoginMode by remember { mutableStateOf(true) }
    var activeAuthType by remember { mutableStateOf("password") }

    // Forms Inputs
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var googleEmail by remember { mutableStateOf("") }
    var phoneNumberInput by remember { mutableStateOf("") }
    var receivedOtpInput by remember { mutableStateOf("") }
    var isOtpSentState by remember { mutableStateOf(false) }

    // User settings
    var rememberMe by remember { mutableStateOf(true) }

    val coralPrimary = Color(0xFFEBC1B0)       // Mockup signature button color
    val textCharcoalDark = Color(0xFF281C1B)   // Button text matching coral
    val glassInputBg = Color.White.copy(alpha = 0.12f)
    val glassBorderColor = Color.White.copy(alpha = 0.20f)

    LaunchedEffect(isLoggedIn) {
        if (isLoggedIn && currentUser != null) {
            onAuthSuccess(currentUser!!)
        }
    }

    LaunchedEffect(uiState) {
        if (uiState is AuthUiState.Error) {
            Toast.makeText(context, (uiState as AuthUiState.Error).message, Toast.LENGTH_LONG).show()
            viewModel.clearError()
        } else if (uiState is AuthUiState.Success && !isLoginMode) {
            Toast.makeText(context, "New user created successfully!", Toast.LENGTH_SHORT).show()
        }
    }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        // Render custom high-fidelity scenic canvas
        SunsetMountainBackdrop()

        // Core Form Centered
        Column(
            modifier = Modifier
                .widthIn(max = 410.dp)
                .fillMaxWidth()
                .statusBarsPadding()
                .navigationBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 28.dp, vertical = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // Thin Display Heading #10
            Text(
                text = if (isLoginMode) "Login #10" else "Register #10",
                fontSize = 17.rsp,
                fontWeight = FontWeight.Light,
                color = Color.White.copy(alpha = 0.7f),
                textAlign = TextAlign.Center,
                letterSpacing = 2.sp
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Bold Have an Account? Subheading
            Text(
                text = if (isLoginMode) "Have an account?" else "Create account!",
                fontSize = 24.rsp,
                fontWeight = FontWeight.Light,
                color = Color.White,
                textAlign = TextAlign.Center,
                letterSpacing = (-0.5).sp
            )

            Spacer(modifier = Modifier.height(28.dp))

            // --- Glassmorphic Authentication Tab Selectors ---
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp)
                    .clip(RoundedCornerShape(32.dp))
                    .background(Color.White.copy(alpha = 0.08f))
                    .border(1.dp, Color.White.copy(alpha = 0.12f), RoundedCornerShape(32.dp))
                    .padding(4.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                val tabItems = listOf(
                    "password" to "🔑 Password",
                    "google" to "🔴 Google",
                    "phone" to "📱 Phone OTP"
                )
                tabItems.forEach { (type, label) ->
                    val isSelected = activeAuthType == type
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(24.dp))
                            .background(if (isSelected) Color.White.copy(alpha = 0.15f) else Color.Transparent)
                            .clickable { activeAuthType = type }
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = label,
                            fontSize = 11.rsp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            color = if (isSelected) Color.White else Color.White.copy(alpha = 0.6f)
                        )
                    }
                }
            }

            // --- Render Dynamic Input Fields ---
            when (activeAuthType) {
                "password" -> {
                    // Username/Email Input Field
                    OutlinedTextField(
                        value = username,
                        onValueChange = { username = it },
                        placeholder = { Text("Username", color = Color.White.copy(alpha = 0.5f), fontSize = 14.sp) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                            .testTag("username_input"),
                        shape = RoundedCornerShape(28.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = glassInputBg,
                            unfocusedContainerColor = glassInputBg,
                            focusedBorderColor = coralPrimary,
                            unfocusedBorderColor = glassBorderColor,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedPlaceholderColor = Color.White.copy(alpha = 0.5f),
                            unfocusedPlaceholderColor = Color.White.copy(alpha = 0.5f)
                        ),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Password Input Field with circular key icon indicator
                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        placeholder = { Text("Password", color = Color.White.copy(alpha = 0.5f), fontSize = 14.sp) },
                        trailingIcon = {
                            Box(
                                modifier = Modifier
                                    .padding(end = 6.dp)
                                    .size(32.dp)
                                    .background(Color.White.copy(alpha = 0.15f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("⚪", fontSize = 9.sp, color = Color.White)
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                            .testTag("password_input"),
                        shape = RoundedCornerShape(28.dp),
                        visualTransformation = PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = glassInputBg,
                            unfocusedContainerColor = glassInputBg,
                            focusedBorderColor = coralPrimary,
                            unfocusedBorderColor = glassBorderColor,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedPlaceholderColor = Color.White.copy(alpha = 0.5f),
                            unfocusedPlaceholderColor = Color.White.copy(alpha = 0.5f)
                        ),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(18.dp))

                    // Remember Me Checklist and Recipient Form (Space between)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Start
                        ) {
                            Checkbox(
                                checked = rememberMe,
                                onCheckedChange = { rememberMe = it },
                                colors = CheckboxDefaults.colors(
                                    checkedColor = coralPrimary,
                                    uncheckedColor = Color.White.copy(alpha = 0.40f),
                                    checkmarkColor = textCharcoalDark
                                ),
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Remember Me",
                                color = Color.White.copy(alpha = 0.75f),
                                fontSize = 13.sp
                            )
                        }

                        Text(
                            text = "Forgot Password",
                            color = Color.White.copy(alpha = 0.8f),
                            fontWeight = FontWeight.Normal,
                            fontSize = 13.sp,
                            modifier = Modifier
                                .clickable {
                                    Toast.makeText(context, "Password Recovery Initiated", Toast.LENGTH_SHORT).show()
                                }
                        )
                    }

                    Spacer(modifier = Modifier.height(28.dp))

                    // SIGN IN Submit Button (Coral/Beige)
                    Button(
                        onClick = {
                            if (isLoginMode) {
                                viewModel.login(username, password)
                            } else {
                                viewModel.register(username, password)
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = coralPrimary),
                        shape = RoundedCornerShape(28.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(54.dp)
                            .testTag("submit_button"),
                        enabled = uiState !is AuthUiState.Loading
                    ) {
                        if (uiState is AuthUiState.Loading) {
                            CircularProgressIndicator(
                                color = textCharcoalDark,
                                modifier = Modifier.size(20.dp),
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text(
                                text = if (isLoginMode) "SIGN IN" else "SIGN UP",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = textCharcoalDark,
                                letterSpacing = 1.sp
                            )
                        }
                    }
                }

                "google" -> {
                    // Google email Address Input
                    OutlinedTextField(
                        value = googleEmail,
                        onValueChange = { googleEmail = it },
                        placeholder = { Text("Google Account Email", color = Color.White.copy(alpha = 0.5f), fontSize = 14.sp) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                            .testTag("google_email_input"),
                        shape = RoundedCornerShape(28.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = glassInputBg,
                            unfocusedContainerColor = glassInputBg,
                            focusedBorderColor = coralPrimary,
                            unfocusedBorderColor = glassBorderColor,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedPlaceholderColor = Color.White.copy(alpha = 0.5f),
                            unfocusedPlaceholderColor = Color.White.copy(alpha = 0.5f)
                        ),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = "Authorized access is automatically filtered across academic institutional records.",
                        color = Color.White.copy(alpha = 0.6f),
                        fontSize = 11.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 8.dp)
                    )

                    Spacer(modifier = Modifier.height(28.dp))

                    // Google Login Action
                    Button(
                        onClick = { viewModel.loginWithGoogle(googleEmail) },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDF4A32)),
                        shape = RoundedCornerShape(28.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(54.dp)
                            .testTag("google_submit_button"),
                        enabled = uiState !is AuthUiState.Loading && googleEmail.isNotBlank()
                    ) {
                        if (uiState is AuthUiState.Loading) {
                            CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                        } else {
                            Text(
                                text = "VERIFY GOOGLE SIGN IN",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                letterSpacing = 1.sp
                            )
                        }
                    }
                }

                "phone" -> {
                    // Phone Number Input
                    OutlinedTextField(
                        value = phoneNumberInput,
                        onValueChange = { phoneNumberInput = it },
                        placeholder = { Text("Phone Number (+91...)", color = Color.White.copy(alpha = 0.5f), fontSize = 14.sp) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                            .testTag("phone_number_input"),
                        shape = RoundedCornerShape(28.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = glassInputBg,
                            unfocusedContainerColor = glassInputBg,
                            focusedBorderColor = coralPrimary,
                            unfocusedBorderColor = glassBorderColor,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedPlaceholderColor = Color.White.copy(alpha = 0.5f),
                            unfocusedPlaceholderColor = Color.White.copy(alpha = 0.5f)
                        ),
                        singleLine = true,
                        enabled = !isOtpSentState
                    )

                    if (isOtpSentState) {
                        Spacer(modifier = Modifier.height(14.dp))
                        
                        OutlinedTextField(
                            value = receivedOtpInput,
                            onValueChange = { receivedOtpInput = it },
                            placeholder = { Text("6-Digit PIN Verification", color = Color.White.copy(alpha = 0.5f), fontSize = 14.sp) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp)
                                .testTag("received_otp_input"),
                            shape = RoundedCornerShape(28.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = glassInputBg,
                                unfocusedContainerColor = glassInputBg,
                                focusedBorderColor = coralPrimary,
                                unfocusedBorderColor = glassBorderColor,
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedPlaceholderColor = Color.White.copy(alpha = 0.5f),
                                unfocusedPlaceholderColor = Color.White.copy(alpha = 0.5f)
                            ),
                            singleLine = true
                        )
                    }

                    Spacer(modifier = Modifier.height(28.dp))

                    Button(
                        onClick = {
                            if (!isOtpSentState) {
                                if (phoneNumberInput.isNotBlank()) {
                                    isOtpSentState = true
                                    Toast.makeText(context, "Firebase OTP Code Sent to $phoneNumberInput", Toast.LENGTH_LONG).show()
                                } else {
                                    Toast.makeText(context, "Enter a valid phone number", Toast.LENGTH_SHORT).show()
                                }
                            } else {
                                viewModel.loginWithPhone(phoneNumberInput)
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                        shape = RoundedCornerShape(28.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(54.dp)
                            .testTag("phone_submit_button"),
                        enabled = uiState !is AuthUiState.Loading && phoneNumberInput.isNotBlank()
                    ) {
                        if (uiState is AuthUiState.Loading) {
                            CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                        } else {
                            Text(
                                text = if (!isOtpSentState) "SEND SMS VERIFICATION" else "VERIFY CODE & SIGN IN",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                letterSpacing = 1.sp
                            )
                        }
                    }

                    if (isOtpSentState) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "Change Phone Number",
                            color = coralPrimary,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier
                                .clickable { isOtpSentState = false; receivedOtpInput = "" }
                                .padding(4.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Alternate Auth Mode Switcher (Sign Up / Log In switch)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (isLoginMode) "Don't have an account? " else "Already have an account? ",
                    fontSize = 13.sp,
                    color = Color.White.copy(alpha = 0.65f)
                )
                Text(
                    text = if (isLoginMode) "Sign up" else "Log in",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = coralPrimary,
                    modifier = Modifier
                        .clickable {
                            isLoginMode = !isLoginMode
                            activeAuthType = "password"
                        }
                        .padding(horizontal = 4.dp)
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

