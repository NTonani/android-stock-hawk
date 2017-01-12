package com.udacity.stockhawk.widget;

import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.RemoteViews;

import com.udacity.stockhawk.R;
import com.udacity.stockhawk.data.PrefUtils;
import com.udacity.stockhawk.ui.DetailActivity;
import com.udacity.stockhawk.ui.MainActivity;

/**
 * Created by ntonani on 12/29/16.
 */

public class StockWidgetProvider extends AppWidgetProvider {

    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        for(int appWidgetId : appWidgetIds){
            RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.tracked_stocks_widget);

            Intent mainIntent = new Intent(context,MainActivity.class);
            PendingIntent pendingIntent = PendingIntent.getActivity(context,0,mainIntent,0);
            remoteViews.setOnClickPendingIntent(R.id.tracked_stocks_widget_action_bar,pendingIntent);


            Bundle options = appWidgetManager.getAppWidgetOptions(appWidgetId);

            Intent intent = new Intent(context,StockWidgetRemoteViewsService.class);
            intent.setAction("actionstring" + System.currentTimeMillis()); // Invalidates previously cached Intent
            intent.putExtras(options);

            remoteViews.setRemoteAdapter(R.id.tracked_stocks_widget_list,
                    intent);

            Intent stockItemClickedIntent = new Intent(context, DetailActivity.class);

            PendingIntent stockItemClickedPendingIntent = TaskStackBuilder.create(context)
                    .addNextIntentWithParentStack(stockItemClickedIntent)
                    .getPendingIntent(0,PendingIntent.FLAG_UPDATE_CURRENT);

            remoteViews.setPendingIntentTemplate(R.id.tracked_stocks_widget_list,stockItemClickedPendingIntent);
            remoteViews.setEmptyView(R.id.tracked_stocks_widget_list,R.id.tracked_stocks_widget_empty);

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
        }else if(intent.getAction().equals(context.getString(R.string.action_units_updated))){
            PrefUtils.toggleDisplayMode(context);

        }
    }

    @Override
    public void onAppWidgetOptionsChanged(Context context, AppWidgetManager appWidgetManager, int appWidgetId, Bundle newOptions){
        onUpdate(context,appWidgetManager,new int[]{appWidgetId});
    }
}
