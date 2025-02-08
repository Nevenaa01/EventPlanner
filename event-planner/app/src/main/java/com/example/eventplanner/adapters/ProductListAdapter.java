package com.example.eventplanner.adapters;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bumptech.glide.Glide;
import com.example.eventplanner.R;
import com.example.eventplanner.activities.ShowOneProductActivity;
import com.example.eventplanner.model.Product;

import java.util.ArrayList;

public class ProductListAdapter extends ArrayAdapter<Product> {
    private ArrayList<Product> products;
    Button remove;
    int resourece;
    private OnItemRemovedListener onItemRemovedListener;
    public interface OnItemRemovedListener {
        void onProductRemoved(Product removedItem);
    }
    public void setOnItemRemovedListener(OnItemRemovedListener listener) {
        this.onItemRemovedListener = listener;
    }

    public ProductListAdapter(Context context, int resourece, ArrayList<Product> products){
        super(context, resourece, products);
        this.products = products;
        this.resourece = resourece;
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
            convertView = LayoutInflater.from(getContext()).inflate(resourece, parent, false);
        }

        ImageView productImage = convertView.findViewById(R.id.product_image);
        TextView productName = convertView.findViewById(R.id.product_name);
        TextView productDescription = convertView.findViewById(R.id.product_description);
        TextView productPrice = convertView.findViewById(R.id.product_price);

        if(product != null){
            Glide.with(getContext())
                    .load(product.getImages().get(0))
                    .into(productImage);
            productName.setText(product.getName());
            productDescription.setText(product.getDescription());
            productPrice.setText(product.getPrice().toString() + "$");
        }

        RelativeLayout productCard = convertView.findViewById(R.id.prodouct_card);
        productCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                productCard.setAlpha(0.3f);
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        productCard.setAlpha(1.0f);
                    }
                }, 100);

                Intent intent = new Intent(getContext(), ShowOneProductActivity.class);
                intent.putExtra("productId", product.getId());
                intent.putExtra("pupvId", product.getPupvId());
                intent.putExtra("categoryId", product.getCategoryId());
                intent.putExtra("subcategoryId", product.getSubcategoryId());
                intent.putExtra("name", product.getName());
                intent.putExtra("description", product.getDescription());
                intent.putExtra("price", product.getPrice());
                intent.putExtra("discount", product.getDiscount());
                intent.putExtra("available", product.getAvailable());
                intent.putParcelableArrayListExtra("images",product.getImages());
                intent.putStringArrayListExtra("eventTypeIds", convertLongListToStringList(product.getEventTypeIds()));

                getContext().startActivity(intent);

            }
        });

        if(resourece == R.layout.product_card_package) {

            remove = convertView.findViewById(R.id.remove);

            remove.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Product removedItem = products.get(position);
                    products.remove(position);
                    notifyDataSetChanged();

                    notifyDataSetChanged();

                    if (onItemRemovedListener != null) {
                        onItemRemovedListener.onProductRemoved(removedItem);
                    }
                }

            });
        }


        return convertView;
    }

    public static ArrayList<String> convertLongListToStringList(ArrayList<Long> longList) {
        ArrayList<String> stringList = new ArrayList<>();
        for (Long number : longList) {
            stringList.add(String.valueOf(number));
        }
        return stringList;
    }
}
