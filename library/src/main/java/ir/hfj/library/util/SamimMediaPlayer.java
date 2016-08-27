package ir.hfj.library.util;

import android.media.MediaPlayer;

import java.io.IOException;

public class SamimMediaPlayer
{

    private MediaPlayer mPlayer;
    private PlayerState mState;
    private String mFilePath;

    public SamimMediaPlayer()
    {
        mState = PlayerState.Init;
    }

    public void play(String filePath, MediaPlayer.OnCompletionListener onCompletionListener)
    {
        mFilePath = filePath;
        if (isPlaying())
        {
            mPlayer.stop();
        }

        if (mState == PlayerState.Stop || mState == PlayerState.Init)
        {
            mPlayer = new MediaPlayer();
            mPlayer.setOnCompletionListener(onCompletionListener);
            try
            {
                mPlayer.setDataSource(mFilePath);
                mPlayer.prepare();
                mPlayer.start();
            }

            catch (IOException e)
            {

            }
        }

        else if (mState == PlayerState.Pause)
        {
            int lenght = mPlayer.getCurrentPosition();
            mPlayer.seekTo(lenght);
            mPlayer.start();
        }
    }

    public void stop()
    {
        if (isPlaying())
        {
            mPlayer.stop();

            mPlayer.release();
            mPlayer = null;
        }

        setState(PlayerState.Stop);
    }

    public void pause()
    {
        if (isPlaying())
        {
            mPlayer.pause();
            mState = PlayerState.Pause;
        }
    }

    enum PlayerState
    {
        Init, Pause, Stop,
    }

    private void setState(PlayerState state)
    {
        mState = state;
    }

    public boolean isPlaying()
    {
        return mPlayer != null && mPlayer.isPlaying();
    }

}
