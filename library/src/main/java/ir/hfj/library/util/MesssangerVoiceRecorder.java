package ir.hfj.library.util;


import android.media.MediaRecorder;

import java.io.IOException;

public class MesssangerVoiceRecorder
{

    private Thread mThread;
    private RecorderState mState;
    private MediaRecorder myAudioRecorder;
    private String outputFile;

    public MesssangerVoiceRecorder()
    {
        mState = RecorderState.Init;
    }

    public String record(String output)
    {
        if (mState == RecorderState.Init || mState == RecorderState.StopRecord)
        {
            try
            {
                outputFile = output;
                myAudioRecorder = new MediaRecorder();

                myAudioRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
                myAudioRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
                myAudioRecorder.setAudioEncoder(MediaRecorder.OutputFormat.AMR_NB);
                myAudioRecorder.setOutputFile(outputFile);

                myAudioRecorder.prepare();
                myAudioRecorder.start();
                setState(RecorderState.Recording);
            }

            catch (IllegalStateException e)
            {
            }

            catch (IOException e)
            {
            }
        }

        return "";
    }

    private void stopRecording()
    {
        if (myAudioRecorder != null)
        {
            myAudioRecorder.stop();
            setState(RecorderState.StopRecord);
        }
    }

    public void release()
    {
        if (myAudioRecorder != null)
        {
            stopRecording();
            myAudioRecorder.release();
            myAudioRecorder = null;

        }
    }

    enum RecorderState
    {
        Init, Recording, StopRecord
    }

    private void setState(RecorderState state)
    {
        mState = state;
    }

}
