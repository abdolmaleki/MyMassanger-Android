package com.example.myapplication.fragment;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.VideoView;

import com.example.myapplication.R;
import com.example.myapplication.entity.ChatContentType;
import com.example.myapplication.factory.FileManager;

import java.util.UUID;

import ir.hfj.library.util.ImageHelper;
import ir.hfj.library.util.VideoHelper;

public class MediaFragment extends DialogFragment implements View.OnClickListener
{


    private final static int REQUEST_IMAGE_GALLERY = 1;
    private final static int REQUEST_IMAGE_CAPTURE = 2;
    private final static int REQUEST_VIDEO_GALLERY = 3;
    private final static int REQUEST_VIDEO = 4;
    private final static int REQUEST_VOICE = 5;
    private final static int REQUEST_FILE = 6;

    private Uri fileUri;


    public static final int MEDIA_TYPE_IMAGE = 1;
    public static final int MEDIA_TYPE_VIDEO = 2;

    OnSelectedListener fragment;

    private String mImgPath;
    private VideoView videoPreview;

    public static MediaFragment newInstance()
    {
        return new MediaFragment();
    }


    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

    }


    @Override
    public void onAttach(Activity activity)
    {
        super.onAttach(activity);

    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {

        View rootView;

        rootView = inflater.inflate(R.layout.fragment_media, container, false);

        rootView.findViewById(R.id.fragment_media_image_gallery).setOnClickListener(this);
        rootView.findViewById(R.id.fragment_media_capture_image).setOnClickListener(this);
        rootView.findViewById(R.id.fragment_media_video_gallery).setOnClickListener(this);
        rootView.findViewById(R.id.fragment_media_capture_video).setOnClickListener(this);
        rootView.findViewById(R.id.fragment_media_file).setOnClickListener(this);
        rootView.findViewById(R.id.fragment_media_close).setOnClickListener(this);

        fragment = (MediaFragment.OnSelectedListener) getTargetFragment();


        return rootView;
    }


    @Override
    public void onClick(View view)
    {
        int id = view.getId();
        if (id == R.id.fragment_media_image_gallery)
        {
            Intent i = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            startActivityForResult(i, REQUEST_IMAGE_GALLERY);
        }

        else if (id == R.id.fragment_media_file)
        {
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("file/*");
            startActivityForResult(intent, REQUEST_FILE);
        }

        else if (id == R.id.fragment_media_video_gallery)
        {
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("video/*");
            startActivityForResult(intent, REQUEST_VIDEO);
        }

        /*else if (id == R.id.fragment_media_capture_image)
        {
            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

            fileUri = getOutputMediaFileUri(MEDIA_TYPE_IMAGE);

            intent.putExtra(MediaStore.EXTRA_OUTPUT, fileUri);

            startActivityForResult(intent, REQUEST_IMAGE_CAPTURE);

        }
        else if (id == R.id.fragment_media_video_gallery)
        {
            Intent i = new Intent(Intent.ACTION_PICK, MediaStore.Video.Media.EXTERNAL_CONTENT_URI);
            startActivityForResult(i, REQUEST_VIDEO_GALLERY);
        }
        else if (id == R.id.fragment_media_capture_video)
        {
            Intent intent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);

            fileUri = getOutputMediaFileUri(MEDIA_TYPE_VIDEO);

            // set video quality
            intent.putExtra(MediaStore.EXTRA_VIDEO_QUALITY, 1);

            // set the image file name
            intent.putExtra(MediaStore.EXTRA_OUTPUT, fileUri);

            // start the video capture Intent
            startActivityForResult(intent, REQUEST_VIDEO_CAPTURE);
        }
*/
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK)
        {
            if (requestCode == REQUEST_IMAGE_GALLERY)//image form gallery
            {
                Uri imgUri = data.getData();
                mImgPath = ImageHelper.getPath(getActivity(), imgUri);
                try
                {
                    String compressedImagePath = FileManager.getDirectory(ChatContentType.Image) + "/" + UUID.randomUUID().toString() + ".jpg";
                    ImageHelper.saveCompressedImage(mImgPath, compressedImagePath, 75);

                    fragment.onSelectedImage(mImgPath);
                }
                catch (Exception e)
                {

                }

            }

            else if (requestCode == REQUEST_VIDEO)
            {
                if (data != null)
                {
                    Uri fileUri = data.getData();
                    fragment.onSelectedVideo(VideoHelper.getPath(getActivity(), fileUri));
                }
            }

            else if (requestCode == REQUEST_FILE)
            {
                if (data != null)
                {
                    Uri fileUri = data.getData();
                    fragment.onSelectedFile(fileUri.getPath());
                }
            }
            /*else if (requestCode == REQUEST_IMAGE_CAPTURE)//image from camera
            {
                //compress
                fragment.onSelectedImage(uriToByte(fileUri), 0);
            }
            else if (requestCode == REQUEST_VIDEO_CAPTURE)
            {
                previewVideo();

            }*/
        }

    }

    public interface OnSelectedListener
    {

        void onSelectedImage(String path);

        void onSelectedVideo(String path);

        void onSelectedAudio(String path);

        void onSelectedFile(String path);
    }

}
