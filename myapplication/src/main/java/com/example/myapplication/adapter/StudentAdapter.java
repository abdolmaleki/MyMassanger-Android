package com.example.myapplication.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.myapplication.R;
import com.example.myapplication.holder.StudentHolder;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.util.List;

import ir.hfj.library.application.AppConfig;

public class StudentAdapter extends BaseAdapter
{

    private final List<StudentHolder> mStudents;
    private final DisplayImageOptions imageOption;
    private Context context;

    public StudentAdapter(Context context, List<StudentHolder> students)
    {
        this.context = context;
        this.mStudents = students;
        imageOption = AppConfig.createDisplayImageOptions();
    }

    @Override
    public int getCount()
    {
        if(mStudents != null)
        {
            return mStudents.size();
        }
        else
        {
            return 0;
        }
    }

    @Override
    public Object getItem(int position)
    {
        return mStudents.get(position);
    }

    @Override
    public long getItemId(int position)
    {
        return mStudents.get(position).id;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent)
    {
        View rootView = LayoutInflater.from(context).inflate(R.layout.item_list_student, parent, false);


        TextView txtName = (TextView) rootView.findViewById(R.id.item_list_student_txv_name);
        TextView txtDetail = (TextView) rootView.findViewById(R.id.item_list_student_txv_detail);
        ImageView imgChild = (ImageView) rootView.findViewById(R.id.item_list_student_img_student);


        StudentHolder students = mStudents.get(position);
        txtName.setText(students.name);
        txtDetail.setText(students.detail);
        ImageLoader.getInstance().displayImage(students.imageUrl, imgChild, imageOption);

        return rootView;
    }


}
