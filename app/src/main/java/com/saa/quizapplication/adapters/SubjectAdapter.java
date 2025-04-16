package com.saa.quizapplication.adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.saa.quizapplication.ChapterSelectionActivity;
import com.saa.quizapplication.R;
import com.saa.quizapplication.model.Subject;
//import com.saa.quizapplication.;
import java.util.List;

public class SubjectAdapter extends RecyclerView.Adapter<SubjectAdapter.SubjectViewHolder> {
    private Context context;
    private List<Subject> subjectList;
    private String department, semester;

    public SubjectAdapter(Context context, List<Subject> subjectList, String department, String semester) {
        this.context = context;
        this.subjectList = subjectList;
        this.department = department;
        this.semester = semester;
    }

    @NonNull
    @Override
    public SubjectViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_subject, parent, false);
        return new SubjectViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SubjectViewHolder holder, int position) {
        Subject subject = subjectList.get(position);
        holder.subjectName.setText(subject.getSubjectName());

        holder.itemView.setOnClickListener(v -> {
            // Open ChapterListingActivity when a subject is clicked
            Intent intent = new Intent(context, ChapterSelectionActivity.class);
            intent.putExtra("department", department);
            intent.putExtra("semester", semester);
            intent.putExtra("subject", subject.getSubjectName());
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return subjectList.size();
    }

    public static class SubjectViewHolder extends RecyclerView.ViewHolder {
        TextView subjectName;

        public SubjectViewHolder(@NonNull View itemView) {
            super(itemView);
            subjectName = itemView.findViewById(R.id.textViewSubjectName);
        }
    }
}
