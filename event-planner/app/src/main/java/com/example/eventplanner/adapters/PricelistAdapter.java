package com.example.eventplanner.adapters;

import android.content.Context;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.eventplanner.R;
import com.example.eventplanner.activities.EditProductActivity;
import com.example.eventplanner.model.Product;
import com.example.eventplanner.model.Service;
import com.example.eventplanner.model.Package;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.WriteBatch;

import java.util.ArrayList;

public class PricelistAdapter<T> extends ArrayAdapter<T> {
    private ArrayList<T> items;
    private FirebaseFirestore db;
    private Context context;
    private int resource;
    private double priceDifference;
    FirebaseAuth mAuth;
    FirebaseUser user;
    public PricelistAdapter(Context context, int resource, ArrayList<T> items) {
        super(context, resource, items);
        this.context = context;
        this.resource = resource;
        this.items = items;
        this.db = FirebaseFirestore.getInstance();

        mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(resource, parent, false);
        }

        T item = items.get(position);

        TextView serialNumber = convertView.findViewById(R.id.serial_number);
        TextView name = convertView.findViewById(R.id.name);
        TextView price = convertView.findViewById(R.id.price);
        TextView discount = convertView.findViewById(R.id.discount);
        TextView priceWithDiscount = convertView.findViewById(R.id.price_with_discount);

        serialNumber.setText(String.valueOf(position + 1) + ".");

        if (item instanceof Product) {
            Product product = (Product) item;

            name.setText(product.getName());
            price.setText(product.getPrice().toString());
            discount.setText("-" + product.getDiscount().toString() + "%");
            priceWithDiscount.setText("= " + String.format("%.2f", product.getPrice() * (1 - product.getDiscount()*0.01)));
        } else if (item instanceof Service) {
            Service service = (Service) item;

            name.setText(service.getName());
            price.setText(service.getFullPrice().toString());
            discount.setText("-" + service.getDiscount().toString() + "%");
            priceWithDiscount.setText("= " + String.format("%.2f", service.getFullPrice() * (1 - service.getDiscount()*0.01)));
        } else if (item instanceof Package) {
            Package pkg = (Package) item;

            name.setText(pkg.getName());
            price.setText(pkg.getPrice().toString());
            discount.setText("-" + pkg.getDiscount().toString() + "%");
            priceWithDiscount.setText("= " + String.format("%.2f", pkg.getPrice() * (1 - pkg.getDiscount()*0.01)));
        }

        TextInputLayout editPriceLayout = convertView.findViewById(R.id.edit_price);
        TextInputEditText priceEditText = convertView.findViewById(R.id.priceEditText);

        if(!(item instanceof Package)) {
            price.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    priceDifference = Double.parseDouble(price.getText().toString());
                    editPriceLayout.getEditText().setText(price.getText());
                    editPriceLayout.setVisibility(View.VISIBLE);
                    price.setVisibility(View.GONE);
                }
            });
        }


        if(user.getDisplayName().equals("PUPV")){
            priceEditText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
                @Override
                public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                    if (actionId == EditorInfo.IME_ACTION_DONE ||
                            (event != null && event.getKeyCode() == KeyEvent.KEYCODE_ENTER && event.getAction() == KeyEvent.ACTION_DOWN)) {
                        String updatedPriceText = editPriceLayout.getEditText().getText().toString();
                        double updatedPrice = Double.parseDouble(updatedPriceText);

                        priceDifference -= updatedPrice;

                        String discountString = discount.getText().toString().split("-")[1].split("%")[0];
                        double discountNumber = Double.parseDouble(discountString);

                        price.setText(updatedPriceText);
                        price.setVisibility(View.VISIBLE);
                        editPriceLayout.setVisibility(View.GONE);

                        priceWithDiscount.setText(String.format("%.2f", updatedPrice *
                                (1 - discountNumber * 0.01)));

                        T item = items.get(position);
                        if (item instanceof Product) {
                            ((Product) item).setPrice(updatedPrice);
                        } else if (item instanceof Service) {
                            ((Service) item).setFullPrice(updatedPrice);
                        } else if (item instanceof Package) {
                            ((Package) item).setPrice(updatedPrice);
                        }

                        notifyDataSetChanged();
                        updateItem(item);
                        return true;
                    }
                    return false;
                }
            });

            TextInputLayout editDiscountLayout = convertView.findViewById(R.id.edit_discount);
            TextInputEditText discountEditText = convertView.findViewById(R.id.discountEditText);

            discount.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    editDiscountLayout.getEditText().setText(discount.getText());
                    editDiscountLayout.setVisibility(View.VISIBLE);
                    discount.setVisibility(View.GONE);
                }
            });

            discountEditText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
                @Override
                public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                    if (actionId == EditorInfo.IME_ACTION_DONE ||
                            (event != null && event.getKeyCode() == KeyEvent.KEYCODE_ENTER && event.getAction() == KeyEvent.ACTION_DOWN)) {
                        String updatedDiscountText = editDiscountLayout.getEditText().getText().toString();
                        double updatedDiscount = Double.parseDouble(updatedDiscountText.replace("-", "").replace("%", ""));

                        discount.setText("-" + updatedDiscountText + "%");
                        discount.setVisibility(View.VISIBLE);
                        editDiscountLayout.setVisibility(View.GONE);

                        priceWithDiscount.setText(String.format("%.2f", Double.parseDouble(price.getText().toString()) *
                                (1 - updatedDiscount * 0.01)));

                        if (item instanceof Product) {
                            ((Product) item).setDiscount(updatedDiscount);
                            priceWithDiscount.setText("= " + String.format("%.2f", ((Product) item).getPrice() * (1 - updatedDiscount * 0.01)));
                        } else if (item instanceof Service) {
                            ((Service) item).setDiscount(updatedDiscount);
                            priceWithDiscount.setText("= " + String.format("%.2f", ((Service) item).getFullPrice() * (1 - updatedDiscount * 0.01)));
                        } else if (item instanceof Package) {
                            ((Package) item).setDiscount(updatedDiscount);
                            priceWithDiscount.setText("= " + String.format("%.2f", ((Package) item).getPrice() * (1 - updatedDiscount * 0.01)));
                        }

                        updateItem(item);
                        notifyDataSetChanged();
                        return true;
                    }
                    return false;
                }
            });
        }

        return convertView;
    }

    private void updateItem(Object item){
        if (item instanceof Product) {
            db.collection("Products")
                    .document(((Product) item).getId().toString())
                    .update("price", ((Product) item).getPrice(),
                            "discount", ((Product) item).getDiscount())
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void unused) {
                            updateInAllPackages(item);
                            Toast.makeText(context, "Product updated", Toast.LENGTH_LONG).show();
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(context, e.getMessage(), Toast.LENGTH_LONG).show();
                        }
                    });
        } else if (item instanceof Service) {
            db.collection("Services")
                    .document(((Service) item).getId().toString())
                    .update("fullPrice", ((Service) item).getFullPrice(),
                            "discount", ((Service) item).getDiscount())
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void unused) {
                            updateInAllPackages(item);
                            Toast.makeText(context, "Service updated", Toast.LENGTH_LONG).show();
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(context, e.getMessage(), Toast.LENGTH_LONG).show();
                        }
                    });
        } else if (item instanceof Package) {
            db.collection("Packages")
                    .document(((Package) item).getId().toString())
                    .update("price", ((Package) item).getPrice())
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void unused) {
                            Toast.makeText(context, "Package updated", Toast.LENGTH_LONG).show();
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(context, e.getMessage(), Toast.LENGTH_LONG).show();
                        }
                    });
        }
    }

    private void updateInAllPackages(Object item){
        Long itemId = (item instanceof Product) ? ((Product) item).getId() : ((Service) item).getId();
        String itemField = (item instanceof Product) ? "productIds" : "serviceIds";

        db.collection("Packages")
                .whereArrayContains(itemField, itemId)
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        WriteBatch batch = db.batch();

                        for (DocumentSnapshot document : queryDocumentSnapshots.getDocuments()) {
                            DocumentReference packageRef = document.getReference();
                            double currentPrice = document.getDouble("price");
                            double newPrice = currentPrice - priceDifference;

                            batch.update(packageRef, "price", newPrice);
                        }

                        batch.commit()
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        Toast.makeText(context, "Package prices updated", Toast.LENGTH_LONG).show();
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Toast.makeText(context, e.getMessage(), Toast.LENGTH_LONG).show();
                                    }
                                });
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(context, e.getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
    }
}
