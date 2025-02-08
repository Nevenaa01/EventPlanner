package com.example.eventplanner.activities;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.graphics.pdf.PdfDocument;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.eventplanner.R;
import com.example.eventplanner.adapters.PricelistAdapter;
import com.example.eventplanner.databinding.ActivityPricelistBinding;
import com.example.eventplanner.model.Product;
import com.example.eventplanner.model.Service;
import com.example.eventplanner.model.Package;
import com.example.eventplanner.model.UserPUPV;
import com.example.eventplanner.model.UserPUPZ;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class PricelistActivity extends AppCompatActivity {

    View view;
    ActivityPricelistBinding binding;
    ArrayList<Product> products;
    ArrayList<Service> services;
    ArrayList<Package> packages;
    FirebaseFirestore db;
    FirebaseAuth mAuth;
    FirebaseUser user;
    Object userFromDb;
    String pupvId;

    private static final int PERMISSION_REQUEST_CODE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityPricelistBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        products = new ArrayList<>();
        services = new ArrayList<>();
        packages = new ArrayList<>();

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();
        getUser();
    }

    private void components(){
        binding.download.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createPdf();
            }
        });
    }

    private void createPdf() {
        PdfDocument document = new PdfDocument();
        PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(1080, 1920, 1).create();
        PdfDocument.Page page = document.startPage(pageInfo);
        Canvas canvas = page.getCanvas();
        Paint paint = new Paint();
        paint.setColor(Color.BLACK);
        paint.setTextSize(42);
        paint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));

        float xTitle = 500;
        float yTitle = 100;
        canvas.drawText("Pricelist", xTitle, yTitle, paint);

        float marginLeft = 50; // Left margin
        float marginRight = 30; // Right margin
        float xStart = marginLeft;
        float yStart = 200;
        float cellWidth = (1080 - marginLeft - marginRight) / 5;
        float cellHeight = 100;
        float lineHeight = 50;

        float remainingSpace = 1920 - yStart; // Remaining space on the page

        // Draw Products section
        drawSectionHeader(canvas, paint, "Products", xStart, yStart);
        drawTableHeader(canvas, paint, xStart, yStart + lineHeight, cellWidth, cellHeight, lineHeight);
        float yItem = yStart + 2 * lineHeight;
        remainingSpace -= (products.size() + 3) * cellHeight; // Account for header and each row
        if (remainingSpace < 0) {
            document.finishPage(page);
            pageInfo = new PdfDocument.PageInfo.Builder(1080, 1920, document.getPages().size() + 1).create();
            page = document.startPage(pageInfo);
            canvas = page.getCanvas();
            yItem = yStart;
        }
        yItem = drawProductContent(canvas, paint, xStart, yItem, cellWidth, cellHeight, lineHeight);

        // Draw Services section
        remainingSpace = 1920 - yItem;
        drawSectionHeader(canvas, paint, "Services", xStart, yItem + cellHeight);
        drawTableHeader(canvas, paint, xStart, yItem + cellHeight + lineHeight, cellWidth, cellHeight, lineHeight);
        yItem += cellHeight + 2 * lineHeight;
        remainingSpace -= (services.size() + 3) * cellHeight; // Account for header and each row
        if (remainingSpace < 0) {
            document.finishPage(page);
            pageInfo = new PdfDocument.PageInfo.Builder(1080, 1920, document.getPages().size() + 1).create();
            page = document.startPage(pageInfo);
            canvas = page.getCanvas();
            yItem = yStart;
        }
        yItem = drawServiceContent(canvas, paint, xStart, yItem, cellWidth, cellHeight, lineHeight);

        // Draw Packages section
        remainingSpace = 1920 - yItem;
        drawSectionHeader(canvas, paint, "Packages", xStart, yItem + cellHeight);
        drawTableHeader(canvas, paint, xStart, yItem + cellHeight + lineHeight, cellWidth, cellHeight, lineHeight);
        yItem += cellHeight + 2 * lineHeight;
        remainingSpace -= (packages.size() + 3) * cellHeight; // Account for header and each row
        if (remainingSpace < 0) {
            document.finishPage(page);
            pageInfo = new PdfDocument.PageInfo.Builder(1080, 1920, document.getPages().size() + 1).create();
            page = document.startPage(pageInfo);
            canvas = page.getCanvas();
            yItem = yStart;
        }
        drawPackageContent(canvas, paint, xStart, yItem, cellWidth, cellHeight, lineHeight);

        document.finishPage(page);

        String directoryPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString();
        File file = new File(directoryPath, "pricelist - " + LocalDate.now() + ".pdf");

        try {
            document.writeTo(new FileOutputStream(file));
            document.close();
            Toast.makeText(this, "PDF file downloaded successfully", Toast.LENGTH_SHORT).show();
        } catch (FileNotFoundException e) {
            System.out.println("Error while writing " + e.toString());
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void drawSectionHeader(Canvas canvas, Paint paint, String sectionTitle, float xStart, float yStart) {
        paint.setTextSize(36);
        paint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
        canvas.drawText(sectionTitle, xStart, yStart, paint);
    }

    private void drawTableHeader(Canvas canvas, Paint paint, float xStart, float yStart, float cellWidth, float cellHeight, float lineHeight) {
        paint.setTextSize(36);
        paint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));

        canvas.drawText("No.", xStart, yStart + lineHeight, paint);
        canvas.drawText("Name", xStart + cellWidth, yStart + lineHeight, paint);
        canvas.drawText("Price", xStart + 2 * cellWidth, yStart + lineHeight, paint);
        canvas.drawText("Discount", xStart + 3 * cellWidth, yStart + lineHeight, paint);
        canvas.drawText("Final Price", xStart + 4 * cellWidth, yStart + lineHeight, paint);
    }

    private float drawProductContent(Canvas canvas, Paint paint, float xStart, float yStart, float cellWidth, float cellHeight, float lineHeight) {
        paint.setTextSize(32);
        paint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.NORMAL));

        int i = 1;
        float yItem = yStart;
        for (Product product : products) {
            Double price = product.getPrice();
            Double discount = product.getDiscount();
            Double priceWithDiscount = price * (1 - discount * 0.01);

            canvas.drawText(String.valueOf(i), xStart, yItem + lineHeight, paint);
            canvas.drawText(product.getName(), xStart + cellWidth, yItem + lineHeight, paint);
            canvas.drawText(price.toString(), xStart + 2 * cellWidth, yItem + lineHeight, paint);
            canvas.drawText("-" + discount.toString() + "%", xStart + 3 * cellWidth, yItem + lineHeight, paint);
            canvas.drawText(priceWithDiscount.toString(), xStart + 4 * cellWidth, yItem + lineHeight, paint);

            yItem += cellHeight;
            i++;
        }
        return yItem; // Return the final y position
    }

    private float drawServiceContent(Canvas canvas, Paint paint, float xStart, float yStart, float cellWidth, float cellHeight, float lineHeight) {
        paint.setTextSize(32);
        paint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.NORMAL));

        int i = 1;
        float yItem = yStart;
        for (Service service : services) {
            Double price = service.getFullPrice();
            Double discount = service.getDiscount();
            Double priceWithDiscount = price * (1 - discount * 0.01);

            canvas.drawText(String.valueOf(i), xStart, yItem + lineHeight, paint);
            canvas.drawText(service.getName(), xStart + cellWidth, yItem + lineHeight, paint);
            canvas.drawText(price.toString(), xStart + 2 * cellWidth, yItem + lineHeight, paint);
            canvas.drawText("-" + discount.toString() + "%", xStart + 3 * cellWidth, yItem + lineHeight, paint);
            canvas.drawText(priceWithDiscount.toString(), xStart + 4 * cellWidth, yItem + lineHeight, paint);

            yItem += cellHeight;
            i++;
        }
        return yItem; // Return the final y position
    }


    private void drawPackageContent(Canvas canvas, Paint paint, float xStart, float yStart, float cellWidth, float cellHeight, float lineHeight) {
        paint.setTextSize(32);
        paint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.NORMAL));

        int i = 1;
        float yItem = yStart;
        for (Package packageItem : packages) {
            Double price = packageItem.getPrice();
            Double discount = packageItem.getDiscount();
            Double priceWithDiscount = price * (1 - discount * 0.01);

            canvas.drawText(String.valueOf(i), xStart, yItem + lineHeight, paint);
            canvas.drawText(packageItem.getName(), xStart + cellWidth, yItem + lineHeight, paint);
            canvas.drawText(price.toString(), xStart + 2 * cellWidth, yItem + lineHeight, paint);
            canvas.drawText("-" + discount.toString() + "%", xStart + 3 * cellWidth, yItem + lineHeight, paint);
            canvas.drawText(priceWithDiscount.toString(), xStart +
                    4 * cellWidth, yItem + lineHeight, paint);

            yItem += cellHeight;
            i++;
        }
    }


        private void getUser(){
        db.collection("User")
                .document(user.getUid())
                .get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        if(user.getDisplayName().equals("PUPZ")){
                            userFromDb = new UserPUPZ(
                                    documentSnapshot.getLong("id"),
                                    documentSnapshot.getString("ownerId"),
                                    documentSnapshot.getString("firstName"),
                                    documentSnapshot.getString("lastName"),
                                    documentSnapshot.getString("email"),
                                    documentSnapshot.getString("password"),
                                    documentSnapshot.getString("phone"),
                                    documentSnapshot.getString("address"),
                                    documentSnapshot.getBoolean("valid"),
                                    documentSnapshot.getString("userType"));

                            pupvId = documentSnapshot.getString("ownerId");
                        }
                        else{
                            userFromDb = new UserPUPV(
                                    documentSnapshot.getString("FirstName"),
                                    documentSnapshot.getString("LastName"),
                                    documentSnapshot.getString("Email"),
                                    documentSnapshot.getString("Password"),
                                    documentSnapshot.getString("Phone"),
                                    documentSnapshot.getString("Address"),
                                    documentSnapshot.getBoolean("IsValid"),
                                    documentSnapshot.getString("CompanyName"),
                                    documentSnapshot.getString("CompanyDescription"),
                                    documentSnapshot.getString("CompanyAddress"),
                                    documentSnapshot.getString("CompanyEmail"),
                                    documentSnapshot.getString("CompanyPhone"),
                                    documentSnapshot.getString("WorkTime"));

                                    pupvId = documentSnapshot.getId();
                        }

                        getProducts();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {

                    }
                });
    }

    private void getProducts(){
        db.collection("Products")
                .whereEqualTo("pupvId", pupvId)
                .whereEqualTo("pending", false)
                .whereEqualTo("deleted", false)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        for(DocumentSnapshot doc: task.getResult()){
                            Product product = new Product(
                                    Long.parseLong(doc.getId()),
                                    pupvId,
                                    Long.parseLong(doc.getString("categoryId")),
                                    Long.parseLong(doc.getString("subcategoryId")),
                                    doc.getString("name"),
                                    doc.getString("description"),
                                    doc.getDouble("price"),
                                    doc.getDouble("discount"),
                                    new ArrayList<>(),
                                    new ArrayList<>(), //convertStringArrayToLong((ArrayList<String>) doc.get("eventTypeIds")),
                                    doc.getBoolean("available"),
                                    doc.getBoolean("visible"),
                                    doc.getBoolean("pending"),
                                    doc.getBoolean("deleted")
                            );

                            products.add(product);
                        }

                        PricelistAdapter<Product> productPricelistAdapter = new PricelistAdapter<>(PricelistActivity.this, R.layout.pricelist_card, products);
                        binding.productList.setAdapter(productPricelistAdapter);

                        getServices();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(PricelistActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
    }

    private ArrayList<Long> convertStringArrayToLong(ArrayList<String> list){
        ArrayList<Long> ids = new ArrayList<>();

        for(String item: list){
            ids.add(Long.parseLong(item));
        }

        return ids;
    }

    private void getServices(){
        db.collection("Services")
                .whereEqualTo("pupvId", pupvId)
                .whereEqualTo("pending", false)
                .whereEqualTo("deleted", false)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        for(DocumentSnapshot doc: task.getResult()){
                            Service service = new Service(
                                        Long.parseLong(doc.getId()),
                                        pupvId,
                                        Long.parseLong(doc.getString("categoryId")),
                                        Long.parseLong(doc.getString("subcategoryId")),
                                        doc.getString("name"),
                                        doc.getString("description"),
                                        new ArrayList<>(), //images
                                        doc.getString("specific"),
                                        ((Number) doc.get("pricePerHour")).doubleValue(),
                                        ((Number) doc.get("fullPrice")).doubleValue(),
                                        doc.get("duration") != null ? ((Number) doc.get("duration")).doubleValue() : null,
                                        doc.get("durationMin") != null ? ((Number) doc.get("durationMin")).doubleValue() : null,
                                        doc.get("durationMax") != null ? ((Number) doc.get("durationMax")).doubleValue() : null,
                                        doc.getString("location"),
                                        ((Number) doc.get("discount")).doubleValue(),
                                        (ArrayList<String>) doc.get("pupIds"),
                                        convertStringArrayToLong((ArrayList<String>) doc.get("eventTypeIds")),
                                        doc.getString("reservationDue"),
                                        doc.getString("cancelationDue"),
                                        doc.getBoolean("automaticAffirmation"),
                                        doc.getBoolean("available"),
                                        doc.getBoolean("visible"),
                                        doc.getBoolean("pending"),
                                        doc.getBoolean("deleted"));

                            services.add(service);
                        }

                        PricelistAdapter<Service> servicePricelistAdapter = new PricelistAdapter<>(PricelistActivity.this, R.layout.pricelist_card, services);
                        binding.serviceList.setAdapter(servicePricelistAdapter);

                        getPackages();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(PricelistActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void getPackages(){
        db.collection("Packages")
                .whereEqualTo("pupvId", pupvId)
                .whereEqualTo("deleted", false)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        for(DocumentSnapshot doc: task.getResult()){
                            Package packagee = new Package(
                                        Long.parseLong(doc.getId()),
                                        pupvId,
                                        doc.getString("name"),
                                        doc.getString("description"),
                                        ((Number) doc.get("discount")).doubleValue(),
                                        doc.getBoolean("available"),
                                        doc.getBoolean("visible"),
                                        Long.parseLong(doc.getString("categoryId")),
                                        convertStringArrayToLong((ArrayList<String>) doc.get("subcategoryIds")),
                                        convertStringArrayToLong((ArrayList<String>)doc.get("productIds")),
                                        convertStringArrayToLong((ArrayList<String>)doc.get("serviceIds")),
                                        convertStringArrayToLong((ArrayList<String>) doc.get("eventTypeIds")),
                                        ((Number) doc.get("price")).doubleValue(),
                                        new ArrayList<>(), //images
                                        doc.getString("reservationDue"),
                                        doc.getString("cancelationDue"),
                                        doc.getBoolean("automaticAffirmation"),
                                        doc.getBoolean("deleted"));

                            packages.add(packagee);

                            components();
                        }

                        PricelistAdapter<Package> packagePricelistAdapter = new PricelistAdapter<>(PricelistActivity.this, R.layout.pricelist_card, packages);
                        binding.packageList.setAdapter(packagePricelistAdapter);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(PricelistActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
    }
}