package com.example.myapplication.actionbar;

import android.app.Activity;
import android.app.Dialog;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import com.example.myapplication.R;
import com.example.myapplication.adapter.StudentAdapter;
import com.example.myapplication.holder.StudentHolder;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;
import ir.hfj.library.actionbar.BaseActionBar;
import ir.hfj.library.application.AppConfig;
import ir.hfj.library.ui.NhToast;

public abstract class StudentActionBar extends BaseActionBar
{

    private final DisplayImageOptions mOptions;
    private List<StudentHolder> mStudentItems;
    private int mSelectedStudent = -1;
    private Activity mActivity;
    private CircleImageView uiStudentImage;
    private TextView uiStudentName;

    public StudentActionBar(AppCompatActivity activity, ActionBar actionBar, int layoutRes)
    {
        super(activity, actionBar, layoutRes);
        mActivity = activity;
        mOptions = AppConfig.createDisplayImageOptions();
    }

    @Override
    protected void initView(View rootView)
    {
        uiStudentImage = (CircleImageView) rootView.findViewById(R.id.samim_actionbar_student_image);
        uiStudentName = (TextView) rootView.findViewById(R.id.samim_actionbar_student_name);
    }

    @Override
    protected final int[] getItems()
    {

        int[] ids = getItemsWithOutStudent();

        int[] newId = new int[ids.length + 1];

        for (int i = 0; i < newId.length - 1; i++)
        {
            newId[i] = ids[i];
        }

        newId[newId.length - 1] = R.id.samim_actionbar_student_image;

        return newId;
    }

    protected abstract int[] getItemsWithOutStudent();

    protected abstract void onItemsClickOther(View v);

    public void setStudents(List<StudentHolder> students, long studentId)
    {
        mStudentItems = students;

        setSelectedStudent(studentId, false);
    }

    public StudentHolder getStudent(int position)
    {
        if (mStudentItems != null && mStudentItems.size() > 0 && position >= 0)
        {
            return mStudentItems.get(position);
        }
        else
        {
            return null;
        }
    }

    public StudentHolder getSelectedStudent()
    {
        if (mStudentItems != null && mStudentItems.size() > 0 && mSelectedStudent >= 0)
        {
            return mStudentItems.get(mSelectedStudent);
        }
        else
        {
            return null;
        }
    }

    @Override
    protected void onItemsClick(View v)
    {
        switch (v.getId())
        {
            case R.id.samim_actionbar_student_image:
            {

                if (mStudentItems != null && mStudentItems.size() > 0)
                {

                    final Dialog dialogSelectStudent = new Dialog(mActivity);
                    dialogSelectStudent.requestWindowFeature(Window.FEATURE_NO_TITLE);
                    dialogSelectStudent.setContentView(R.layout.dialog_list_student);
                    ListView listDialog = (ListView) dialogSelectStudent.findViewById(R.id.listDialogChild);


                    StudentAdapter listAdapterChild = new StudentAdapter(mActivity, mStudentItems);
                    listDialog.setAdapter(listAdapterChild);
                    listDialog.setOnItemClickListener(new AdapterView.OnItemClickListener()
                    {
                        @Override
                        public void onItemClick(AdapterView<?> adapterView, View view, int position, long id)
                        {

                            if (position != mSelectedStudent)
                            {
                                StudentHolder before = getSelectedStudent();

                                mSelectedStudent = position;

                                onChangeStudent(before, getSelectedStudent(), true);

                            }

                            dialogSelectStudent.dismiss();
                        }
                    });
                    dialogSelectStudent.show();
                }
                else
                {
                    NhToast.makeText(mActivity, R.string.messanger_message_error_notfound_student, NhToast.ToastIcon.ERROR, NhToast.LENGTH_SHORT).show();
                }


                break;
            }
            default:
            {
                onItemsClickOther(v);
                break;
            }
        }
    }

    private void onChangeStudent(StudentHolder before, StudentHolder selectedStudent, boolean withEvent)
    {
        if (selectedStudent != null)
        {
            ImageLoader.getInstance().displayImage(selectedStudent.imageUrl, uiStudentImage, mOptions);
            uiStudentName.setText(selectedStudent.name);
        }
        else
        {
            uiStudentImage.setImageResource(R.drawable.ic_user);
            uiStudentName.setText("...");
        }

        if (withEvent && mActivity instanceof OnSamimActionBarChangeStudent)
        {
            ((OnSamimActionBarChangeStudent) mActivity).onActionBarChangeStudent(before, selectedStudent);
        }
    }

    private void setSelectedStudent(long studentId, boolean withEvent)
    {
        if (mStudentItems != null)
        {

            StudentHolder before = null;
            StudentHolder current = null;

            for (StudentHolder holder : mStudentItems)
            {
                if (holder.id == mSelectedStudent)
                {
                    before = holder;
                }

                if (holder.id == studentId)
                {
                    current = holder;
                }
            }

            if (before != null || current != null)
            {
                onChangeStudent(before, current, withEvent);
            }
        }
    }

    //public void setSelectedStudent(long studentId)
    //{
    //    setSelectedStudent(studentId, true);
    //}
}
