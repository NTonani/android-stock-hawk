package com.udacity.stockhawk.ui;

import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.view.ViewCompat;
import android.support.v7.widget.CardView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;
import com.udacity.stockhawk.R;
import com.udacity.stockhawk.data.Contract;

import java.io.IOException;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import timber.log.Timber;
import yahoofinance.Stock;
import yahoofinance.YahooFinance;

import static com.udacity.stockhawk.data.Contract.Quote.COLUMN_ABSOLUTE_CHANGE;
import static com.udacity.stockhawk.data.Contract.Quote.COLUMN_HISTORY;
import static com.udacity.stockhawk.data.Contract.Quote.COLUMN_PERCENTAGE_CHANGE;
import static com.udacity.stockhawk.data.Contract.Quote.COLUMN_PRICE;
import static com.udacity.stockhawk.data.Contract.Quote.COLUMN_SYMBOL;

/**
 * Created by ntonani on 1/9/17.
 */

public class DetailFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>{

    static final int STOCK_LOADER = 0;

    public static final String[] QUOTE_COLUMNS = {
            Contract.Quote.TABLE_NAME + "." + Contract.Quote._ID,
            COLUMN_SYMBOL,
            COLUMN_PRICE,
            COLUMN_ABSOLUTE_CHANGE,
            COLUMN_PERCENTAGE_CHANGE,
            COLUMN_HISTORY
    };

    public static final int POSITION_ID = 0;
    public static final int POSITION_SYMBOL = 1;
    public static final int POSITION_PRICE = 2;
    public static final int POSITION_ABSOLUTE_CHANGE = 3;
    public static final int POSITION_PERCENTAGE_CHANGE = 4;
    public static final int POSITION_HISTORY = 5;

    @BindView(R.id.detail_info_frame)
    FrameLayout mFrameLayout;

    @BindView(R.id.symbol)
    TextView mSymbolView;

    @BindView(R.id.price)
    TextView mPriceView;

    @BindView(R.id.detail_card_view)
    CardView mCardView;

    @BindView(R.id.detail_line_chart)
    LineChart mLineChart;

    private String mSymbol;
    private Stock mStock;
    private Uri mStockUri;
    private DecimalFormat dollarFormat;
    private boolean isRtl;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
        Bundle arguments = getArguments();

        if(arguments != null){
            mStockUri = arguments.getParcelable(getString(R.string.stock_symbol_variable));
            mSymbol = Contract.Quote.getStockFromUri(mStockUri);
        }

        dollarFormat = (DecimalFormat) NumberFormat.getCurrencyInstance(Locale.US);
        isRtl = getResources().getConfiguration().getLayoutDirection() == ViewCompat.LAYOUT_DIRECTION_RTL;

        View rootView = inflater.inflate(R.layout.fragment_detail,container,false);
        ButterKnife.bind(this,rootView);

        //mFrameLayout.bringToFront();
        return rootView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        getLoaderManager().initLoader(STOCK_LOADER, null, this);
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        if(id!=STOCK_LOADER) return null;

        return new CursorLoader(
                getActivity(),
                mStockUri,
                QUOTE_COLUMNS,
                null,
                null,
                null);

    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if(loader.getId() != STOCK_LOADER || !data.moveToFirst()) return;

        // Set symbol
        mSymbolView.setText(data.getString(POSITION_SYMBOL));

        // Set price
        mPriceView.setText(dollarFormat.format(data.getFloat(POSITION_PRICE)));

        // Prepare line chart
        List<Entry> yValues = new ArrayList<Entry>();
        List<String> xLabels = new ArrayList<String>();
        DateFormat simpleDateFormat = new SimpleDateFormat("dd/MM/yyyy");

        String rawHistoricalData = data.getString(POSITION_HISTORY);
        if(rawHistoricalData == null){
            // TODO Empty view
            return;
        }

        String[] rawHistoricalDataPoints = rawHistoricalData.split("\n");
        for(int i = rawHistoricalDataPoints.length-1;i>=0;i--){
            String rawHistoricalDataPoint = rawHistoricalDataPoints[i];
            String[] dateAndClose = rawHistoricalDataPoint.split(",");
            Calendar curCal = new GregorianCalendar();
            curCal.setTimeInMillis(Long.parseLong(dateAndClose[0]));

            // Handle rtl
            if(isRtl){
                yValues.add(0,new Entry(i,Float.parseFloat(dateAndClose[1])));
                xLabels.add(0,simpleDateFormat.format(curCal.getTime()));
            }else {
                yValues.add(new Entry(rawHistoricalDataPoints.length-i-1,Float.parseFloat(dateAndClose[1])));
                xLabels.add(simpleDateFormat.format(curCal.getTime()));
            }
        }

        final List<String> final_xLabels = new ArrayList<String>(xLabels);

        IAxisValueFormatter formatter = new IAxisValueFormatter() {
            @Override
            public String getFormattedValue(float value, AxisBase axis) {
                return final_xLabels.get((int)value);
            }
        };

        XAxis xAxis = mLineChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setValueFormatter(formatter);
        xAxis.setDrawAxisLine(true);
        xAxis.setDrawGridLines(false);
        xAxis.setGranularityEnabled(false);

        YAxis left = mLineChart.getAxisLeft();

        if(isRtl){
            left = mLineChart.getAxisRight();
            mLineChart.getAxisLeft().setEnabled(false);
        }else  mLineChart.getAxisRight().setEnabled(false);

        left.setEnabled(true);
        left.setLabelCount(10, true);
        left.setGranularityEnabled(false);

        // Hide axis, legend, description, background grid
        mLineChart.getLegend().setEnabled(false);
        mLineChart.getDescription().setEnabled(false);
        mLineChart.setDrawGridBackground(false);

        LineDataSet dataSet = new LineDataSet(yValues,getString(R.string.detail_activity_chart_label));
        dataSet.setDrawCircles(false);

        LineData lineData = new LineData(dataSet);

        mLineChart.setData(lineData);
        mLineChart.invalidate();
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {}

    public class FetchHistoryData extends AsyncTask<String,Void,Stock> {

        @Override
        protected Stock doInBackground(String... strings) {
            String symbol = strings[0];
            if(symbol == null) return null;

            try{
                return YahooFinance.get(symbol,true);
            }catch(IOException e){
                Timber.e("Failed to get historical data of symbol: %s",symbol);
            }

            return null; // Fail state
        }

        @Override
        protected void onPostExecute(Stock stock) {
            if(stock==null){
                // TODO empty view
                return;
            }


        }
    }
}

