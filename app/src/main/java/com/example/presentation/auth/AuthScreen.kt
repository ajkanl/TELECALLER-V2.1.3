package com.example.presentation.auth

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun AuthScreen(viewModel: AuthViewModel, onAuthSuccess: (String) -> Unit) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()
    val isLoggedIn by viewModel.isLoggedIn.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()

    var isLoginMode by remember { mutableStateOf(true) }
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    // Modern glowing dark gradient palette
    val gradientBackground = Brush.verticalGradient(
        colors = listOf(
            Color(0xFF1E2640), // Premium deep dark blue
            Color(0xFF0F1322), // Near black blue
            Color(0xFF060810)  // Solid elegant black
        )
    )

    val primaryBlue = Color(0xFF3B82F6)
    val textSlateColor = Color.White
    val textSlateMuted = Color(0xFF94A3B8)
    val borderCharcoal = Color(0xFF1E293B)
    val activeGlowBlue = Color(0xFF2563EB)

    LaunchedEffect(isLoggedIn) {
        if (isLoggedIn && currentUser != null) {
            onAuthSuccess(currentUser!!)
        }
    }

    LaunchedEffect(uiState) {
        if (uiState is AuthUiState.Error) {
            Toast.makeText(context, (uiState as AuthUiState.Error).message, Toast.LENGTH_LONG).show()
            viewModel.clearError()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(gradientBackground)
            .statusBarsPadding()
            .navigationBarsPadding(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .widthIn(max = 440.dp)
                .fillMaxWidth()
                .padding(28.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Spacer(modifier = Modifier.height(24.dp))

            // Large White Premium Title
            Text(
                text = if (isLoginMode) "Log in" else "Sign up",
                fontSize = 44.sp,
                fontWeight = FontWeight.Normal,
                color = textSlateColor,
                textAlign = TextAlign.Center,
                letterSpacing = (-1).sp
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Subtitle aligned with recovery pro/student database context but matching exact imagery text flow
            Text(
                text = if (isLoginMode) {
                    "Log in to your account and seamlessly continue managing your student database, campaigns, and progress just where you left off."
                } else {
                    "Set up your secure recovery credentials to continue maintaining compliance, calling campaigns, and secure student databases."
                },
                fontSize = 13.sp,
                color = Color(0xFF8E9CB2),
                textAlign = TextAlign.Center,
                lineHeight = 20.sp,
                modifier = Modifier.padding(horizontal = 12.dp)
            )

            Spacer(modifier = Modifier.height(36.dp))

            // Email/Username Input (fully rounded as requested)
            OutlinedTextField(
                value = username,
                onValueChange = { username = it },
                placeholder = { Text("Enter your email address", color = Color(0xFF556073), fontSize = 14.sp) },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Email,
                        contentDescription = "Mail Icon",
                        tint = Color(0xFF556073),
                        modifier = Modifier.size(18.dp)
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .testTag("username_input"),
                shape = RoundedCornerShape(28.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = Color(0xFF0B0E17),
                    unfocusedContainerColor = Color(0xFF0B0E17),
                    focusedBorderColor = activeGlowBlue,
                    unfocusedBorderColor = borderCharcoal,
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedPlaceholderColor = Color(0xFF556073),
                    unfocusedPlaceholderColor = Color(0xFF556073)
                ),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Password Input (fully rounded with lock glowing status indicator at the end)
            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                placeholder = { Text("Enter your password", color = Color(0xFF556073), fontSize = 14.sp) },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = "Lock Icon",
                        tint = Color(0xFF556073),
                        modifier = Modifier.size(18.dp)
                    )
                },
                trailingIcon = {
                    // Premium blue lock status icon matching exact image visual accent on right side
                    Box(
                        modifier = Modifier
                            .padding(end = 4.dp)
                            .size(32.dp)
                            .background(Color(0xFF1E3A8A).copy(alpha = 0.4f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("🔒", fontSize = 12.sp, color = primaryBlue)
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
                    focusedContainerColor = Color(0xFF0B0E17),
                    unfocusedContainerColor = Color(0xFF0B0E17),
                    focusedBorderColor = activeGlowBlue,
                    unfocusedBorderColor = borderCharcoal,
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedPlaceholderColor = Color(0xFF556073),
                    unfocusedPlaceholderColor = Color(0xFF556073)
                ),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(36.dp))

            // Log In premium pill-shaped button
            Button(
                onClick = {
                    if (isLoginMode) {
                        viewModel.login(username, password)
                    } else {
                        viewModel.register(username, password)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF191D29)), // Charcoal dark pill container as requested
                border = BorderStroke(1.dp, Color(0xFF2E3B4E)),
                shape = RoundedCornerShape(28.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
                    .testTag("submit_button"),
                enabled = uiState !is AuthUiState.Loading
            ) {
                if (uiState is AuthUiState.Loading) {
                    CircularProgressIndicator(
                        color = Color.White,
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp
                    )
                } else {
                    Text(
                        text = if (isLoginMode) "Log in" else "Sign up",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Normal,
                        color = Color.White
                    )
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            // Social Buttons side-by-side as in image mockup (Facebook, Google, Apple) with click feedback
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                val socialButtonColors = ButtonDefaults.outlinedButtonColors(
                    containerColor = Color(0xFF0E121E),
                    contentColor = Color.White
                )

                // Facebook Social
                OutlinedButton(
                    onClick = {
                        Toast.makeText(context, "Facebook Sign-In Demo Mode Activated", Toast.LENGTH_SHORT).show()
                    },
                    colors = socialButtonColors,
                    border = BorderStroke(1.dp, borderCharcoal),
                    shape = RoundedCornerShape(24.dp),
                    contentPadding = PaddingValues(0.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(44.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text("🔵 ", fontSize = 11.sp)
                        Text("Facebook", fontSize = 11.sp, fontWeight = FontWeight.Normal, color = Color(0xFF8E9CB2))
                    }
                }

                // Google Social
                OutlinedButton(
                    onClick = {
                        Toast.makeText(context, "Google Sign-In Demo Mode Activated", Toast.LENGTH_SHORT).show()
                    },
                    colors = socialButtonColors,
                    border = BorderStroke(1.dp, borderCharcoal),
                    shape = RoundedCornerShape(24.dp),
                    contentPadding = PaddingValues(0.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(44.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text("🔴 ", fontSize = 11.sp)
                        Text("Google", fontSize = 11.sp, fontWeight = FontWeight.Normal, color = Color(0xFF8E9CB2))
                    }
                }

                // Apple Social
                OutlinedButton(
                    onClick = {
                        Toast.makeText(context, "Apple Sign-In Demo Mode Activated", Toast.LENGTH_SHORT).show()
                    },
                    colors = socialButtonColors,
                    border = BorderStroke(1.dp, borderCharcoal),
                    shape = RoundedCornerShape(24.dp),
                    contentPadding = PaddingValues(0.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(44.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(" ", fontSize = 13.sp, color = Color.White)
                        Text("Apple", fontSize = 11.sp, fontWeight = FontWeight.Normal, color = Color(0xFF8E9CB2))
                    }
                }
            }

            Spacer(modifier = Modifier.height(36.dp))

            // Alternate Auth Mode Switcher at the bottom
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (isLoginMode) "Didn't have an account? " else "Already have an account? ",
                    fontSize = 13.sp,
                    color = Color(0xFF6B7280)
                )
                Text(
                    text = if (isLoginMode) "Sign up" else "Log in",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = primaryBlue,
                    modifier = Modifier
                        .clickable { isLoginMode = !isLoginMode }
                        .padding(horizontal = 4.dp)
                )
            }
        }
    }
}
