/**

Buddha Quotes
Copyright (C) 2021  BanDev

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program.  If not, see <https://www.gnu.org/licenses/>.

 */

package org.bandev.buddhaquotes.widgets

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Build
import android.widget.RemoteViews
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.bandev.buddhaquotes.R
import org.bandev.buddhaquotes.architecture.Db
import org.bandev.buddhaquotes.architecture.QuoteMapper
import org.bandev.buddhaquotes.core.Images.heart
import org.bandev.buddhaquotes.core.inDarkMode
import org.bandev.buddhaquotes.items.Quote

/** The widget **/
class MainWidget : AppWidgetProvider() {

    var newQuote: String = "action.NEW_QUOTE"

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        appWidgetIds.forEach { appWidgetId ->
            updateAppWidget(context, appWidgetManager, appWidgetId)
        }
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        if (intent.action.equals(newQuote)) {
            AppWidgetManager.getInstance(context).also {  appWidgetManager ->
                ComponentName(context.packageName, this.javaClass.name).also { appWidget ->
                    appWidgetManager.getAppWidgetIds(appWidget).also { appWidgetIds ->
                        onUpdate(context, appWidgetManager, appWidgetIds)
                    }
                }
            }
        }
    }

    override fun onDeleted(context: Context?, appWidgetIds: IntArray?) {
        super.onDeleted(context, appWidgetIds)
        appWidgetIds?.forEach { appWidgetId ->
            context?.deleteWidgetPref(appWidgetId)
        }
    }
}

fun getRandom(context: Context, after: (quote: Quote) -> Unit) {
    CoroutineScope(Dispatchers.IO).launch {
        Db.getInstance(context)?.quote()?.let { QuoteMapper.convert(it.get((1..237).random())).run(after) }
    }
}

fun getPenIntent(context: Context, action: String): PendingIntent {
    val flags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    } else {
        PendingIntent.FLAG_UPDATE_CURRENT
    }
    Intent(context, MainWidget::class.java).apply { this.action = action }.also {
        return PendingIntent.getBroadcast(context, 0, it, flags)
    }
}

internal fun updateAppWidget(
    context: Context,
    appWidgetManager: AppWidgetManager,
    appWidgetId: Int,
) {
    getRandom(context) { quote ->
        context.loadWidgetPref(appWidgetId).also { widgetData ->
            RemoteViews(context.packageName, context.widgetLayout(widgetData)).apply {
                setTextViewText(R.id.widget_text, context.getString(quote.resource))
                setOnClickPendingIntent(R.id.root, getPenIntent(context, MainWidget().newQuote))
            }.let { appWidgetManager.updateAppWidget(appWidgetId, it) }
        }
    }
}

fun Context.widgetLayout(widgetData: WidgetDataItem): Int {
    return when {
        widgetData.theme == WidgetTheme.LIGHT && widgetData.translucency == WidgetTranslucency.OPQAUE -> R.layout.widget_light_opaque
        widgetData.theme == WidgetTheme.DARK && widgetData.translucency == WidgetTranslucency.TRANSPARENT -> R.layout.widget_dark_transparent
        widgetData.theme == WidgetTheme.DARK && widgetData.translucency == WidgetTranslucency.OPQAUE -> R.layout.widget_dark_opaque
        widgetData.theme == WidgetTheme.APP && widgetData.translucency == WidgetTranslucency.TRANSPARENT -> if (inDarkMode(this)) R.layout.widget_dark_transparent else R.layout.widget_light_transparent
        widgetData.theme == WidgetTheme.APP && widgetData.translucency == WidgetTranslucency.OPQAUE -> if (inDarkMode(this)) R.layout.widget_dark_opaque else R.layout.widget_light_opaque
        else -> R.layout.widget_light_transparent
    }
}
