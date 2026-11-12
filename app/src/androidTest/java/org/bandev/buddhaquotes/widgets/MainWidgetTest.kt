package org.bandev.buddhaquotes.widgets

import android.app.PendingIntent
import android.content.Intent
import android.os.Build
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class MainWidgetTest {
    @Test
    fun widgetRefreshPendingIntentCanBeCreatedAndReused() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        // A separate action avoids replacing a real widget's pending intent during the test.
        val action = "org.bandev.buddhaquotes.test.WIDGET_REFRESH"
        val pendingIntent = getPenIntent(context, action)
        try {
            assertEquals(pendingIntent, getPenIntent(context, action))
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                assertTrue(pendingIntent.isImmutable)
            }
            val lookupFlags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
            } else {
                PendingIntent.FLAG_NO_CREATE
            }
            val expectedIntent = Intent(context, MainWidget::class.java).setAction(action)
            assertEquals(
                pendingIntent,
                PendingIntent.getBroadcast(context, 0, expectedIntent, lookupFlags)
            )
        } finally {
            pendingIntent.cancel()
        }
    }
}
