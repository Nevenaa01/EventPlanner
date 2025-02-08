package com.example.eventplanner.adapters;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bumptech.glide.Glide;
import com.example.eventplanner.activities.EditProductActivity;
import com.example.eventplanner.R;

import java.util.ArrayList;

import com.example.eventplanner.model.Product;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.FirebaseFirestore;

public class ProductListPupvAdapter extends ArrayAdapter<Product> {
    private ArrayList<Product> products;
    private FirebaseFirestore db;

    public ProductListPupvAdapter(Context context, ArrayList<Product> products){
        super(context, R.layout.product_card_pupv, products);
        this.products = products;
        db = FirebaseFirestore.getInstance();
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
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.product_card_pupv, parent, false);
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

        Button editButton = convertView.findViewById(R.id.product_edit);
        Button deleteButton = convertView.findViewById(R.id.product_delete);

        final View finalConvertView = convertView;
        editButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(finalConvertView.getContext(), EditProductActivity.class);
                intent.putExtra("productId", product.getId());

                finalConvertView.getContext().startActivity(intent);
            }
        });

        deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                View popUpView = inflater.inflate(R.layout.confirmation_popup, null);

                int width = ViewGroup.LayoutParams.MATCH_PARENT;
                int height = ViewGroup.LayoutParams.MATCH_PARENT;
                boolean focusable = true;
                PopupWindow popupWindow = new PopupWindow(popUpView, width, height, focusable);

                popupWindow.showAtLocation(v, Gravity.CENTER, 0, 0);

                Button confrim = popUpView.findViewById(R.id.delete_button);
                confrim.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        db.collection("Products")
                                .document(products.get(position).getId().toString())
                                .update("deleted", true)
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void unused) {
                                        products.remove(position);
                                        notifyDataSetChanged();
                                        Toast.makeText(finalConvertView.getContext(), "Product deleted", Toast.LENGTH_LONG).show();
                                        popupWindow.dismiss();
                                    }
                                })

                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Toast.makeText(finalConvertView.getContext(), e.getMessage(), Toast.LENGTH_LONG).show();
                                    }
                                });
                        }
                });

                Button cancel = popUpView.findViewById(R.id.cancel_button);

                cancel.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        popupWindow.dismiss();
                    }
                });
            }
        });


        return convertView;
    }
}
