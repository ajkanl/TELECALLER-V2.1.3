package com.example.data.service

import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.PixelFormat
import android.os.Build
import android.os.IBinder
import android.view.Gravity
import android.view.WindowManager
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.setViewTreeLifecycleOwner
import androidx.lifecycle.setViewTreeViewModelStoreOwner
import androidx.savedstate.SavedStateRegistry
import androidx.savedstate.SavedStateRegistryController
import androidx.savedstate.SavedStateRegistryOwner
import androidx.savedstate.setViewTreeSavedStateRegistryOwner
import com.example.ui.theme.MyApplicationTheme
import java.text.NumberFormat
import java.util.Locale

/**
 * Android system overlay service that attaches a non-blocking Compose-rendered HUD directly on top
 * of system activities during call actions. Includes layout param tuning to accommodate non-focusable overlays.
 */
class OverlayWindowService : Service() {

    private var windowManager: WindowManager? = null
    private var overlayView: ComposeView? = null
    private var customLifecycleOwner: CustomLifecycleOwner? = null

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        removeOverlay() // Clean up any existing lifecycle/view bounds before creating a new overlay

        val debtorName = intent?.getStringExtra(EXTRA_DEBTOR_NAME) ?: "Amit Sharma"
        val overdueAmount = intent?.getDoubleExtra(EXTRA_OVERDUE_AMOUNT, 45000.0) ?: 45000.0
        val dpdStatus = intent?.getStringExtra(EXTRA_DPD_STATUS) ?: "90+ DPD"
        val note = intent?.getStringExtra(EXTRA_NOTE) ?: "Urgent settlement required. Authorized settlement discount limit of 15% apply."

        showOverlay(debtorName, overdueAmount, dpdStatus, note)
        return START_NOT_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        removeOverlay()
    }

    /**
     * Shows overlay on top of system dialer or other user interfaces.
     */
    private fun showOverlay(
        debtorName: String,
        overdueAmount: Double,
        dpdStatus: String,
        note: String
    ) {
        val wm = getSystemService(Context.WINDOW_SERVICE) as WindowManager
        windowManager = wm

        val layoutType = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        } else {
            @Suppress("DEPRECATION")
            WindowManager.LayoutParams.TYPE_PHONE
        }

        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            layoutType,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                    WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or
                    WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.CENTER_HORIZONTAL
            x = 0
            y = 150 // Vertical displacement to clear navigation/status bars
        }

        val lifecycleOwner = CustomLifecycleOwner()
        lifecycleOwner.handleLifecycleEvent(Lifecycle.Event.ON_CREATE)
        lifecycleOwner.handleLifecycleEvent(Lifecycle.Event.ON_START)
        lifecycleOwner.handleLifecycleEvent(Lifecycle.Event.ON_RESUME)
        customLifecycleOwner = lifecycleOwner

        val savedStateOwner = CustomSavedStateRegistryOwner(lifecycleOwner)
        val viewModelStoreOwner = CustomViewModelStoreOwner()

        val composeView = ComposeView(this).apply {
            // Assign lifecycle, savedstate and viewmodel owners to satisfy Compose's requirements
            setViewTreeLifecycleOwner(lifecycleOwner)
            setViewTreeViewModelStoreOwner(viewModelStoreOwner)
            setViewTreeSavedStateRegistryOwner(savedStateOwner)

            setContent {
                MyApplicationTheme {
                    OverlayCard(
                        name = debtorName,
                        amount = overdueAmount,
                        dpd = dpdStatus,
                        note = note,
                        onDismiss = {
                            removeOverlay()
                            stopSelf()
                        },
                        onDrag = { dx, dy ->
                            params.x += dx.toInt()
                            params.y += dy.toInt()
                            try {
                                windowManager?.updateViewLayout(this@apply, params)
                            } catch (e: Exception) {
                                // Gracefully ignore layout state updates if view is detached concurrently
                            }
                        }
                    )
                }
            }
        }

        overlayView = composeView
        try {
            wm.addView(composeView, params)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * Safely detaches the ComposeView overlay panel from the system's WindowManager.
     */
    fun removeOverlay() {
        try {
            overlayView?.let { view ->
                windowManager?.removeView(view)
            }
        } catch (e: Exception) {
            // Safe fallback if target element was already cleared
        } finally {
            overlayView = null
            customLifecycleOwner?.handleLifecycleEvent(Lifecycle.Event.ON_DESTROY)
            customLifecycleOwner = null
        }
    }

    companion object {
        const val EXTRA_DEBTOR_NAME = "extra_debtor_name"
        const val EXTRA_OVERDUE_AMOUNT = "extra_overdue_amount"
        const val EXTRA_DPD_STATUS = "extra_dpd_status"
        const val EXTRA_NOTE = "extra_note"
    }

    // --- Compose requirement owners ---

    private class CustomLifecycleOwner : LifecycleOwner {
        private val lifecycleRegistry = LifecycleRegistry(this)
        override val lifecycle: Lifecycle = lifecycleRegistry

        fun handleLifecycleEvent(event: Lifecycle.Event) {
            lifecycleRegistry.handleLifecycleEvent(event)
        }
    }

    private class CustomSavedStateRegistryOwner(lifecycleOwner: LifecycleOwner) : SavedStateRegistryOwner {
        private val controller = SavedStateRegistryController.create(this)
        override val lifecycle: Lifecycle = lifecycleOwner.lifecycle
        override val savedStateRegistry: SavedStateRegistry = controller.savedStateRegistry

        init {
            controller.performRestore(null)
        }
    }

    private class CustomViewModelStoreOwner : ViewModelStoreOwner {
        override val viewModelStore: ViewModelStore = ViewModelStore()
    }
}

@Composable
fun OverlayCard(
    name: String,
    amount: Double,
    dpd: String,
    note: String,
    onDismiss: () -> Unit,
    onDrag: (Float, Float) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .pointerInput(Unit) {
                detectDragGestures { change, dragAmount ->
                    change.consume()
                    onDrag(dragAmount.x, dragAmount.y)
                }
            },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.95f),
            contentColor = MaterialTheme.colorScheme.onSurfaceVariant
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth()
        ) {
            // Header: Grab/Drag hint, Panel Title, Dismiss
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Menu,
                        contentDescription = "Reposition panel handle",
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "Recovery Desk Panel",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close overlay HUD",
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Body content
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                        )
                        Text(
                            text = name,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    val currencyFormat = NumberFormat.getCurrencyInstance(Locale("en", "IN"))
                    val formattedAmount = try {
                        currencyFormat.format(amount)
                    } catch (e: Exception) {
                        "₹ %,.2f".format(locale = Locale.US, amount)
                    }

                    Text(
                        text = "Dues Outstanding: $formattedAmount",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                // Warning / Risk Category Badges
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(
                            if (dpd.contains("90") || dpd.contains("Write")) {
                                MaterialTheme.colorScheme.errorContainer
                            } else {
                                MaterialTheme.colorScheme.secondaryContainer
                            }
                        )
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = dpd,
                        color = if (dpd.contains("90") || dpd.contains("Write")) {
                            MaterialTheme.colorScheme.onErrorContainer
                        } else {
                            MaterialTheme.colorScheme.onSecondaryContainer
                        },
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Lower Note Banner
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.5f))
                    .padding(8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.Top
            ) {
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = null,
                    modifier = Modifier
                        .size(16.dp)
                        .padding(top = 2.dp),
                    tint = MaterialTheme.colorScheme.error
                )
                Text(
                    text = note,
                    style = MaterialTheme.typography.bodySmall,
                    lineHeight = 14.sp
                )
            }
        }
    }
}
