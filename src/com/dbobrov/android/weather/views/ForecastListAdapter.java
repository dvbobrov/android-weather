package com.dbobrov.android.weather.views;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import com.dbobrov.android.weather.R;
import com.dbobrov.android.weather.models.Forecast;
import com.dbobrov.android.weather.network.IconGetter;

import java.text.DateFormatSymbols;
import java.util.Date;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: blackhawk
 * Date: 04.12.12
 * Time: 18:27
 */
public class ForecastListAdapter extends ArrayAdapter<Forecast> {
    private final Activity context;
    private final List<Forecast> data;

    public ForecastListAdapter(Activity context, List<Forecast> data) {
        super(context, R.layout.forecast_row, data);
        this.context = context;
        this.data = data;
    }

    private static class ViewHolder {
        protected TextView tempMax, tempMin, day, month;
        protected ImageView icon;
    }

    private static TextView getTextView(View v, int id) {
        return (TextView) v.findViewById(id);
    }

    @Override
    public View getView(final int position, View convertView,
                        ViewGroup parent) {
        View v = null;
        if (convertView == null) {
            LayoutInflater inflator = context.getLayoutInflater();
            v = inflator.inflate(R.layout.forecast_row, null);
            final ViewHolder viewHolder = new ViewHolder();
            viewHolder.day = getTextView(v, R.id.day);
            viewHolder.month = getTextView(v, R.id.month);
            viewHolder.tempMin = getTextView(v, R.id.tempMin);
            viewHolder.tempMax = getTextView(v, R.id.tempMax);
            viewHolder.icon = (ImageView) v.findViewById(R.id.icon);
            v.setTag(viewHolder);
        } else {
            v = convertView;
        }
        ViewHolder holder = (ViewHolder) v.getTag();
        Forecast forecast = data.get(position);
        IconGetter.addElement(forecast.iconName, holder.icon);
        String[] date = forecast.date.split("-");
        holder.day.setText(date[2]);
        holder.month.setText(monthFromNumber(Integer.parseInt(date[1])).substring(0, 3));
        holder.tempMin.setText(forecast.tempMin);
        holder.tempMax.setText(forecast.tempMax);
        // TODO later. Wind data
        return v;
    }

    private static String monthFromNumber(int number) {
        if (number > 12 || number < 1) return "???";
        String[] month = new DateFormatSymbols().getMonths();
        return month[number - 1];
    }
}
