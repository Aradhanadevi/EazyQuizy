package com.saa.quizapplication.adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.saa.quizapplication.chapterDetailActivity;
import com.saa.quizapplication.R;
import com.saa.quizapplication.model.Chapter;
import java.util.List;

public class ChapterAdapter extends RecyclerView.Adapter<ChapterAdapter.ChapterViewHolder> {
    private Context context;
    private List<Chapter> chapterList;
    private String department, semester, subject;

    public ChapterAdapter(Context context, List<Chapter> chapterList, String department, String semester, String subject) {
        this.context = context;
        this.chapterList = chapterList;
        this.department = department;
        this.semester = semester;
        this.subject = subject;
    }

    @NonNull
    @Override
    public ChapterViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_chapter, parent, false);
        return new ChapterViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ChapterViewHolder holder, int position) {
        Chapter chapter = chapterList.get(position);
        holder.chapterName.setText(chapter.getName());

        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, chapterDetailActivity.class);
            intent.putExtra("department", department);
            intent.putExtra("semester", semester);
            intent.putExtra("subject", subject);
            intent.putExtra("chapter", chapter.getName());
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return chapterList.size();
    }

    static class ChapterViewHolder extends RecyclerView.ViewHolder {
        TextView chapterName;

        public ChapterViewHolder(@NonNull View itemView) {
            super(itemView);
            chapterName = itemView.findViewById(R.id.chapterName);
        }
    }
}
