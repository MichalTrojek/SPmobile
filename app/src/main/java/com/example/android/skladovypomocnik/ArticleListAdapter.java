package com.example.android.skladovypomocnik;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;

public class ArticleListAdapter extends ArrayAdapter<Article> {

    private Context context;
    private int resource;

    public ArticleListAdapter(Context c, int resource, ArrayList<Article> list) {
        super(c, resource, list);
        this.context = c;
        this.resource = resource;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        String ean = getItem(position).getEan();
        int amount = getItem(position).getAmount();
        String name = getItem(position).getName();
        String price = getItem(position).getPrice();


        LayoutInflater inflater = LayoutInflater.from(context);
        convertView = inflater.inflate(resource, parent, false);


        TextView eanTextView = (TextView) convertView.findViewById(R.id.eanTextView);
        TextView amountTextView = (TextView) convertView.findViewById(R.id.amountTextView);
        TextView nameTextView = (TextView) convertView.findViewById(R.id.nameTexView);
        TextView priceTextView = (TextView) convertView.findViewById(R.id.priceTextView);

        eanTextView.setText(ean);
        amountTextView.setText(String.format("%d", amount));
        nameTextView.setText(name);
        priceTextView.setText(String.format("%s,- Kč", price.replace(".00", "")));


        return convertView;
    }


}
