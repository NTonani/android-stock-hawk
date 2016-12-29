package com.udacity.stockhawk.widget;

import android.content.Intent;
import android.os.IBinder;
import android.widget.RemoteViewsService;

/**
 * Created by ntonani on 12/29/16.
 */

public class StockWidgetRemoteViewService extends RemoteViewsService {
    public StockWidgetRemoteViewService() {
        super();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return super.onBind(intent);
    }

    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        return null;
    }
}
