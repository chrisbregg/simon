package org.no_ip.chrisbregg.simon;

import android.content.Context;
import android.media.AudioManager;
import android.media.SoundPool;

import java.util.ArrayList;

/**
 * Created by Chris on 2015-03-18.
 */
public class SoundPlayer implements SoundPool.OnLoadCompleteListener {
    public interface SoundPlayerLoadCompleteListener {
        public void OnAudioLoadComplete();
    }

    public static final int BLUE_TONE = 0;
    public static final int RED_TONE = 1;
    public static final int GREEN_TONE = 2;
    public static final int YELLOW_TONE = 3;

    private SoundPool mSp;
    private ArrayList<Integer> mTrackList;
    public static Context mContext;

    private AudioManager am;

    private int mSoundsLoadedCount = 0;
    private final int TOTAL_SOUND_COUNT = 4;

    private SoundPlayerLoadCompleteListener mLoadCompleteListener;

    final AudioManager.OnAudioFocusChangeListener afChangeListener = new AudioManager.OnAudioFocusChangeListener() {
        @Override
        public void onAudioFocusChange(int focusChange) {
            if (focusChange == AudioManager.AUDIOFOCUS_LOSS_TRANSIENT) {
                // do nothing since a missed sound or two isn't the end of the world
            } else if (focusChange == AudioManager.AUDIOFOCUS_GAIN) {
                // resume playing, except the sound is probably already over so who cares
            } else if (focusChange == AudioManager.AUDIOFOCUS_LOSS) {
                am.abandonAudioFocus(afChangeListener);
            }
        }
    };

    public SoundPlayer() {
        mSp = new SoundPool(1, AudioManager.STREAM_MUSIC, 0);
        mTrackList = new ArrayList<Integer>();

        mSp.setOnLoadCompleteListener(this);

        initSoundData();
    }

    @Override
    public void onLoadComplete(SoundPool soundPool, int sampleId, int status) {
        mSoundsLoadedCount++;

        if (mSoundsLoadedCount >= TOTAL_SOUND_COUNT) {
            if (mLoadCompleteListener != null) {
                mLoadCompleteListener.OnAudioLoadComplete();
            }
        }
    }

    private void initSoundData() {
        if (mContext != null) {
            mTrackList.add(mSp.load(mContext, R.raw.audiotrack1, 0));
            mTrackList.add(mSp.load(mContext, R.raw.audiotrack2, 0));
            mTrackList.add(mSp.load(mContext, R.raw.audiotrack3, 0));
            mTrackList.add(mSp.load(mContext, R.raw.audiotrack4, 0));
        }
    }

    public void playSound(int soundId) {
        if (soundId < mTrackList.size()) {
            am = (AudioManager)mContext.getSystemService(Context.AUDIO_SERVICE);
            int result = am.requestAudioFocus(afChangeListener,
                    // use the music stream
                    AudioManager.STREAM_MUSIC,
                    // request permanent focus
                    AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK);

            if (result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
                mSp.play(mTrackList.get(soundId), 1.0f, 1.0f, 0, 0, 1.0f);
            }
        }
    }

    public void setOnLoadCompleteListener(SoundPlayerLoadCompleteListener listener) {
        mLoadCompleteListener = listener;
    }
}
