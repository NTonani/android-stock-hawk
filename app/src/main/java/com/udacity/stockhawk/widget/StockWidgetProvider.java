package com.udacity.stockhawk.widget;

import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;

import com.udacity.stockhawk.R;
import com.udacity.stockhawk.ui.MainActivity;

/**
 * Created by ntonani on 12/29/16.
 */

public class StockWidgetProvider extends AppWidgetProvider {

    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        for(int appWidgetId : appWidgetIds){
            RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.tracked_stocks_widget);

            // TODO: PendingIntent for onClick of banner (dne atm)

            remoteViews.setRemoteAdapter(R.id.tracked_stocks_widget_list,
                    new Intent(context,StockWidgetRemoteViewsService.class));

            Intent stockItemClickedIntent = new Intent(context, MainActivity.class);

            PendingIntent stockItemClickedPendingIntent = TaskStackBuilder.create(context)
                    .addNextIntentWithParentStack(stockItemClickedIntent)
                    .getPendingIntent(0,PendingIntent.FLAG_UPDATE_CURRENT);

            remoteViews.setPendingIntentTemplate(R.id.tracked_stocks_widget_list,stockItemClickedPendingIntent);

            appWidgetManager.updateAppWidget(appWidgetId, remoteViews);
        }
    }


    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);
        if(intent.getAction().equals(context.getString(R.string.action_data_updated))){
            AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
            int[] appWidgetIds = appWidgetManager.getAppWidgetIds(
                    new ComponentName(context, getClass()));
            appWidgetManager.notifyAppWidgetViewDataChanged(appWidgetIds,R.id.tracked_stocks_widget_list);
        }
    }
}
