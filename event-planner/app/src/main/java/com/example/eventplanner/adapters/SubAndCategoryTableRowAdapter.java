package com.example.eventplanner.adapters;

import static android.content.ContentValues.TAG;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.eventplanner.R;
import com.example.eventplanner.activities.ShowOneEventActivity;
import com.example.eventplanner.databinding.FragmentAddSubcategoryOnBudgetPlannerBinding;
import com.example.eventplanner.model.EventType;
import com.example.eventplanner.model.Subcategory;
import com.example.eventplanner.model.SubcategoryPlanner;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.example.eventplanner.model.SubcategoryPlanner;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

public class SubAndCategoryTableRowAdapter extends RecyclerView.Adapter<SubAndCategoryTableRowAdapter.SubAndCategoryViewHolder>{

    private static ArrayList<SubcategoryPlanner> subcategories;

    public SubAndCategoryTableRowAdapter(ArrayList<SubcategoryPlanner> subcategories){
        this.subcategories = subcategories;
    }

    @NonNull
    @Override
    public SubAndCategoryTableRowAdapter.SubAndCategoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.subcategory_row_table, parent, false);
        return new SubAndCategoryTableRowAdapter.SubAndCategoryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SubAndCategoryTableRowAdapter.SubAndCategoryViewHolder holder, int position) {
        SubcategoryPlanner subcategory = subcategories.get(position);
        holder.bind(subcategory,position);
    }

    @Override
    public int getItemCount() {
        return subcategories.size();
    }

    public static class SubAndCategoryViewHolder extends RecyclerView.ViewHolder {
        TextView nameCategory;
        TextView nameSubcategory;
        TextView price;
        TextView serialNum;

        TextView idSubcategoryPlanner;

        ImageView deleteButton;

        ImageView editButton;
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        BottomSheetDialog bottomSheetDialog;

        ShowOneEventActivity activity;

        private List<EventType> itemList=new ArrayList<>();



        public SubAndCategoryViewHolder(@NonNull View itemView) {
            super(itemView);
            activity = (ShowOneEventActivity) itemView.getContext();
            nameCategory = itemView.findViewById(R.id.category);
            nameSubcategory = itemView.findViewById(R.id.subcategory);
            price = itemView.findViewById(R.id.amount);
            serialNum = itemView.findViewById(R.id.serialNumber);

            deleteButton = itemView.findViewById(R.id.deleteSub);
            idSubcategoryPlanner = itemView.findViewById(R.id.idSubcategoryPlanner);



            deleteButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    LayoutInflater inflater = (LayoutInflater) itemView.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                    View popUpView = inflater.inflate(R.layout.confirmation_popup, null);



                    int width = ViewGroup.LayoutParams.MATCH_PARENT;
                    int height = ViewGroup.LayoutParams.MATCH_PARENT;
                    boolean focusable = true;
                    PopupWindow popupWindow = new PopupWindow(popUpView, width, height, focusable);

                    popupWindow.showAtLocation(v, Gravity.CENTER, 0, 0);

                    Button cancelButton = popUpView.findViewById(R.id.cancel_button);
                    Button deleteButton = popUpView.findViewById(R.id.delete_button);

                    cancelButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            popupWindow.dismiss();
                        }
                    });

                    deleteButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            int position = getAdapterPosition();


                            if (position != RecyclerView.NO_POSITION) {
                                // Pristupanje elementu u listi podataka na odgovarajućoj poziciji
                                SubcategoryPlanner subcategory = subcategories.get(position);

                                // Sada možete izvršiti željene akcije sa tim elementom
                                // Na primjer, možete prikazati dijalog za potvrdu brisanja ili izvršiti brisanje direktno
                                //showDeleteConfirmationDialog(subcategory);
                                deleteSubcategoryPlanner(subcategory);
                                popupWindow.dismiss();
                            }

                        }
                    });


                }
            });

            editButton = itemView.findViewById(R.id.editSub);

            editButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    Context context = itemView.getContext();

                    LayoutInflater inflater = LayoutInflater.from(context);

                    bottomSheetDialog = new BottomSheetDialog( itemView.getContext(), R.style.FullScreenBottomSheetDialog);
                    View dialogView =inflater.inflate(R.layout.fragment_add_subcategory_on_budget_planner, null);


                    // Pronalaženje komponenti unutar layouta
                    AutoCompleteTextView editCategory = dialogView.findViewById(R.id.categoryInput);
                    AutoCompleteTextView editSubcategory = dialogView.findViewById(R.id.subcategoryInput);
                    TextInputEditText editPrice = dialogView.findViewById(R.id.priceInput);
                    TextView idSubcatPlanner = dialogView.findViewById(R.id.idsubPlanner);
                    MaterialButton buttonEdit = dialogView.findViewById(R.id.saveAddBtn);

                    // Postavljanje podataka u komponente
                    editCategory.setText(nameCategory.getText());
                    editSubcategory.setText(nameSubcategory.getText());
                    editPrice.setText(price.getText());
                    idSubcatPlanner.setText(idSubcategoryPlanner.getText());

                    buttonEdit.setText("Edit");

                    buttonEdit.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {

                            if (validateCreteSubcategoryPlanner(editPrice))
                                return;

                            updateSubcategoryPlanner(new SubcategoryPlanner(Long.parseLong(String.valueOf(idSubcatPlanner.getText())), editCategory.getText().toString() , editSubcategory.getText().toString(),Float.parseFloat(String.valueOf(editPrice.getText()))));
                            bottomSheetDialog.dismiss();
                        }

                    });



                    bottomSheetDialog.setContentView(dialogView);
                    bottomSheetDialog.show();
                }
            });

        }

        public void bind(SubcategoryPlanner subcategory, int position) {
            nameCategory.setText(subcategory.getNameCategory());
            nameSubcategory.setText(subcategory.getNameSubcategory());
            price.setText(subcategory.getPrice().toString());
            serialNum.setText(String.valueOf(position + 1));
            idSubcategoryPlanner.setText((subcategory.getSerialNum().toString()));

        }

        private void updateSubcategoryPlanner(SubcategoryPlanner subcategoryPlanner) {


            Map<String, Object> updates = new HashMap<>();
            updates.put("nameCategory", subcategoryPlanner.getNameCategory());
            updates.put("nameSubcategory", subcategoryPlanner.getNameSubcategory());
            updates.put("price", subcategoryPlanner.getPrice());

            db.collection("SubcategoryPlanner").document(subcategoryPlanner.getSerialNum().toString()).update(updates)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Log.d(TAG, "Document successfully updated!");
                            activity.getSubcategoryPlanner();

                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.w(TAG, "Error updating document", e);
                        }
                    });


        }

        private void deleteSubcategoryPlanner(SubcategoryPlanner subcategoryPlanner) {

            db.collection("SubcategoryPlanner").document(subcategoryPlanner.getSerialNum().toString()).delete()
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Log.d(TAG, "Document successfully deleted!");
                            activity.getSubcategoryPlanner();

                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.w(TAG, "Error updating document", e);
                        }
                    });


        }

        private boolean validateCreteSubcategoryPlanner(TextInputEditText priceInput) {
            boolean error=false;
            if(TextUtils.isEmpty(priceInput.getText())){
                priceInput.setError("Fill number!");
                error=true;
            }
            else{
                try {
                    int price = Integer.parseInt(priceInput.getText().toString());
                    if(price <= 0){
                        priceInput.setError("Price must be >0!");
                        error =  true;
                    }
                }catch (Exception e){
                    priceInput.setError("Price must be integer!");
                    error = true;
                }
            }

            if(error) return true;

            return false;
        }


    }
}
