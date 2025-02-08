package com.example.eventplanner.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.eventplanner.R;
import com.example.eventplanner.databinding.ActivityCommentPreviewBinding;
import com.example.eventplanner.databinding.ActivityOwnerDashboardBinding;
import com.example.eventplanner.model.Comment;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.SetOptions;

import java.text.SimpleDateFormat;
import java.util.Date;

public class CommentPreviewActivity extends AppCompatActivity {

    ActivityCommentPreviewBinding binding;
    LinearLayout commentsContainer;
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    FirebaseAuth mAuth = FirebaseAuth.getInstance();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_comment_preview);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        binding = ActivityCommentPreviewBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        commentsContainer = binding.commentsContainer;

        binding.backBtn.setOnClickListener(v->{
            Intent intent = new Intent(this, HomeActivity.class);
            startActivity(intent);
        });

        retrieveCommentsByCompanyId(mAuth.getCurrentUser().getUid());
    }

    private void retrieveCommentsByCompanyId(String companyId){
        db.collection("Comment")
                .whereEqualTo("companyId", companyId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots ->{
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        displayComment(document.toObject(Comment.class), document.getId());
                    }
                });
    }

    private void displayComment(Comment comment, String commentDocumentId){
        View commentView = LayoutInflater.from(this).inflate(R.layout.comment_card, commentsContainer, false);

        TextView commentGradeTextView = commentView.findViewById(R.id.comment_grade_value);
        commentGradeTextView.setText(comment.getGrade());

        TextView commentDescriptionTextView = commentView.findViewById(R.id.comment_description_value);
        commentDescriptionTextView.setText(comment.getDescription());

        MaterialButton showReportCommentButton = commentView.findViewById(R.id.show_report_comment_btn);
        LinearLayout commentContainer = commentView.findViewById(R.id.comment_container);

        EditText reportDescriptionEditText = commentView.findViewById(R.id.report_description_value);

        MaterialButton reportCommentButton = commentView.findViewById(R.id.report_comment_btn);

        showReportCommentButton.setOnClickListener(v -> {
            if (commentContainer.getVisibility() == View.GONE) {
                commentContainer.setVisibility(View.VISIBLE);
            } else {
                commentContainer.setVisibility(View.GONE);
            }
        });

        reportCommentButton.setOnClickListener(v -> {
            String explanation = reportDescriptionEditText.getText().toString();
            if (!explanation.isEmpty()) {
                Date currentDate = new Date();
                SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");
                comment.getReport().setDescription(explanation);
                comment.getReport().setOccurenceDate(dateFormat.format(currentDate));

                db.collection("Comment")
                        .document(commentDocumentId)
                        .set(comment, SetOptions.merge())
                        .addOnSuccessListener(aVoid -> {
                            reportDescriptionEditText.setText("");
                            commentContainer.setVisibility(View.GONE);
                            Toast.makeText(this, "Comment reported successfully!", Toast.LENGTH_SHORT).show();
                        })
                        .addOnFailureListener(e -> {
                            Toast.makeText(this, "Failed to report comment: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        });
            } else {
                Toast.makeText(this, "Please enter explanation!", Toast.LENGTH_SHORT).show();
            }
        });

        commentsContainer.addView(commentView);
    }
}