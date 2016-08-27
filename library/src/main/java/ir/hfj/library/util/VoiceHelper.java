package ir.hfj.library.util;

import android.media.MediaPlayer;
import android.media.MediaRecorder;

import java.io.IOException;

public class VoiceHelper
{

    private static MediaPlayer PLAYER;
    private static MediaRecorder RECORDER;


    private static boolean prepearForPlay(String path)
    {
        if (PLAYER != null && PLAYER.isPlaying())
        {
            stopVoice();
        }
        try
        {
            PLAYER = new MediaPlayer();
            PLAYER.setDataSource(path);
            PLAYER.prepare();

            return true;
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        return false;
    }

    public static boolean playVoice(String path, MediaPlayer.OnCompletionListener listener)
    {

        if (prepearForPlay(path))
        {
            PLAYER.start();
            PLAYER.setOnCompletionListener(listener);
            return true;
        }
        else
        {
            return false;
        }
    }

    public static void stopVoice()
    {
        PLAYER.stop();
    }


    private static boolean prepearForRecord(String path)
    {

        try
        {
            RECORDER = new MediaRecorder();
            RECORDER.setAudioSource(MediaRecorder.AudioSource.MIC);
            RECORDER.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
            RECORDER.setAudioEncoder(MediaRecorder.OutputFormat.AMR_NB);
            RECORDER.setOutputFile(path);
            RECORDER.prepare();
            return true;
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }

        return false;

    }

    public static boolean recordVoice(String path)
    {

        if (prepearForRecord(path))
        {
            RECORDER.start();
            return true;
        }
        else
        {
            return false;
        }
    }

    public static void stopRecording()
    {
        RECORDER.stop();
        RECORDER.release();

    }


}
