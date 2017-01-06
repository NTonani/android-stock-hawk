package com.udacity.stockhawk.widget;

import android.appwidget.AppWidgetManager;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Binder;
import android.os.Bundle;
import android.widget.AdapterView;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import com.udacity.stockhawk.R;
import com.udacity.stockhawk.data.Contract;
import com.udacity.stockhawk.data.PrefUtils;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Locale;

/**
 * Created by ntonani on 12/29/16.
 */

public class StockWidgetRemoteViewsService extends RemoteViewsService {

    private static final String[] QUOTE_COLUMNS = {
            Contract.Quote.TABLE_NAME + "." +Contract.Quote._ID,
            Contract.Quote.COLUMN_SYMBOL,
            Contract.Quote.COLUMN_PRICE,
            Contract.Quote.COLUMN_ABSOLUTE_CHANGE,
            Contract.Quote.COLUMN_PERCENTAGE_CHANGE,
    };

    static final int INDEX_QUOTE_ID = 0;
    static final int INDEX_QUOTE_SYMBOL = 1;
    static final int INDEX_QUOTE_PRICE = 2;
    static final int INDEX_QUOTE_ABSOLUTE_CHANGE = 3;
    static final int INDEX_QUOTE_PERCENTAGE_CHANGE = 4;

    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        Bundle dimens = intent.getExtras();

        int width = dimens.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH);
        final int blocks = (width + 30) / 70;

        return new RemoteViewsFactory() {
            private Cursor data = null;
            private DecimalFormat dollarFormat;
            private DecimalFormat dollarFormatWithPlus;
            private DecimalFormat percentageFormat;

            @Override
            public void onCreate() {
                dollarFormat = (DecimalFormat) NumberFormat.getCurrencyInstance(Locale.US);
                dollarFormatWithPlus = (DecimalFormat) NumberFormat.getCurrencyInstance(Locale.US);
                dollarFormatWithPlus.setPositivePrefix("+$");
                percentageFormat = (DecimalFormat) NumberFormat.getPercentInstance(Locale.getDefault());
                percentageFormat.setMaximumFractionDigits(2);
                percentageFormat.setMinimumFractionDigits(2);
                percentageFormat.setPositivePrefix("+");
            }

            @Override
            public void onDataSetChanged() {
                if(data!=null) data.close();

                // Allow use of StockHawk process / permissions
                final long identityToken = Binder.clearCallingIdentity();

                Uri stockDataUri = Contract.Quote.URI;

                data = getContentResolver().query(stockDataUri,
                        QUOTE_COLUMNS,
                        null,
                        null,
                        null);

                // Restore calling identity
                Binder.restoreCallingIdentity(identityToken);
            }

            @Override
            public void onDestroy() {
                if(data!=null){
                    data.close();
                    data = null;
                }
            }

            @Override
            public int getCount() {
                return data == null ? 0 : data.getCount();
            }

            @Override
            public RemoteViews getViewAt(int position) {
                if(data == null
                        || position == AdapterView.INVALID_POSITION
                        || !data.moveToPosition(position)) return null;

                int layoutId = R.layout.tracked_stocks_widget_list_item;

                if(blocks <= 3){
                    layoutId = R.layout.tracked_stocks_widget_list_item_small;
                }

                RemoteViews views = new RemoteViews(getPackageName(),
                        layoutId);

                String symbol = data.getString(INDEX_QUOTE_SYMBOL);

                // Assign values to view objects in view hierarchy
                views.setTextViewText(R.id.symbol,symbol);
                views.setTextViewText(R.id.price,dollarFormat.format(data.getFloat(INDEX_QUOTE_PRICE)));

                // TODO determine if remoteviews object shows dimensions and use them to determine whether or not
                // the change text view should be shown

                // If larger layout
                if(blocks > 3){
                    float rawAbsoluteChange = data.getFloat(Contract.Quote.POSITION_ABSOLUTE_CHANGE);
                    float percentageChange = data.getFloat(Contract.Quote.POSITION_PERCENTAGE_CHANGE);

                    if (rawAbsoluteChange > 0) {
                        views.setInt(R.id.change,"setBackgroundResource",R.drawable.percent_change_pill_green);
                    } else {
                        views.setInt(R.id.change,"setBackgroundResource",R.drawable.percent_change_pill_red);
                    }

                    String change = dollarFormatWithPlus.format(rawAbsoluteChange);
                    String percentage = percentageFormat.format(percentageChange / 100);

                    if (PrefUtils.getDisplayMode(getApplicationContext())
                            .equals(getApplicationContext().getString(R.string.pref_display_mode_absolute_key))){
                        views.setTextViewText(R.id.change,change);
                    } else {
                        views.setTextViewText(R.id.change,percentage);
                    }
                }

                // TODO change intent for DetailActivity.class
                final Intent fillinIntent = new Intent();
                //fillinIntent.setData(Contract.Quote.makeUriForStock(symbol));
                views.setOnClickFillInIntent(R.id.tracked_stocks_widget_list_item,fillinIntent);

                return views;
            }

            @Override
            public RemoteViews getLoadingView() {
                return new RemoteViews(getPackageName(),R.layout.tracked_stocks_widget_list_item);
            }

            @Override
            public int getViewTypeCount() {
                return 1;
            }

            @Override
            public long getItemId(int position) {
                return !data.moveToPosition(position) ? position : data.getLong(INDEX_QUOTE_ID);
            }

            @Override
            public boolean hasStableIds() {
                return true;
            }
        };
    }
}
