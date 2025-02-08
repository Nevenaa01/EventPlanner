package com.example.eventplanner.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.eventplanner.R;

import java.util.ArrayList;

import com.example.eventplanner.model.Product;

public class PackageProductListAdapter extends ArrayAdapter<Product> {

    private ArrayList<Product> products;

    public PackageProductListAdapter(Context context, ArrayList<Product> products){
        super(context, R.layout.product_card_package, products);
        this.products = products;
    }

    @Override
    public int getCount() {
        return this.products.size();
    }

    @Nullable
    @Override
    public Product getItem(int position) {
        return this.products.get(position);
    }

    @Override
    public long getItemId(int position) {
        return this.getItem(position).getId();
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        Product product = getItem(position);

        if(convertView == null){
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.product_card_package, parent, false);
        }

        ImageView productImage = convertView.findViewById(R.id.product_image);
        TextView productName = convertView.findViewById(R.id.product_name);
        TextView productDescription = convertView.findViewById(R.id.product_description);
        TextView productPrice = convertView.findViewById(R.id.product_price);

        if(product != null){
            productImage.setImageURI(product.getImages().get(0));
            productName.setText(product.getName());
            productDescription.setText(product.getDescription());
            productPrice.setText(product.getPrice().toString() + "$");
        }


        return convertView;
    }
}
