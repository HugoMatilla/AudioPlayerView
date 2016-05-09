package com.hugomatilla.audioplayerview;

/**
 * Created by hugomatilla on 10/02/16.
 */

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Typeface;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Build;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.TextView;

import java.io.IOException;

public class AudioPlayerView extends TextView {
    private static final String NULL_PARAMETER_ERROR = "`stopText`, `playText` and `loadingText`" +
            " must have some value, if `useIcons` is set to false. Set `useIcons` to true, or add strings to stopText`, " +
            "`playText` and `loadingText` in the AudioPlayerView.xml";
    private Context context;
    private MediaPlayer mediaPlayer;
    private String playText;
    private String stopText;
    private String loadingText;
    private String url;
    private boolean useIcons;
    private boolean audioReady;
    private boolean usesCustomIcons;

    //Callbacks
    public interface OnAudioPlayerViewListener {
        void onAudioPreparing();

        void onAudioReady();

        void onAudioFinished();
    }

    private OnAudioPlayerViewListener listener;

    public void setOnAudioPlayerViewListener(OnAudioPlayerViewListener listener) {
        this.listener = listener;
    }

    private void sendCallbackAudioFinished() {
        if (listener != null)
            listener.onAudioFinished();
    }

    private void sendCallbackAudioReady() {
        if (listener != null)
            listener.onAudioReady();
    }

    private void sendCallbackAudioPreparing() {
        if (listener != null)
            listener.onAudioPreparing();
    }

    //Constructors
    public AudioPlayerView(Context context) {
        super(context);
        this.context = context;
    }

    public AudioPlayerView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
        getAttributes(attrs);
    }

    public AudioPlayerView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.context = context;
        getAttributes(attrs);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public AudioPlayerView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        this.context = context;
        getAttributes(attrs);
    }

    public void getAttributes(AttributeSet attrs) {
        TypedArray a = context.getTheme().obtainStyledAttributes(attrs, R.styleable.AudioPlayerTextView, 0, 0);

        try {
            stopText = a.getString(R.styleable.AudioPlayerTextView_stopText);
            playText = a.getString(R.styleable.AudioPlayerTextView_playText);
            loadingText = a.getString(R.styleable.AudioPlayerTextView_loadingText);
            useIcons = a.getBoolean(R.styleable.AudioPlayerTextView_useIcons, true);

            if ((stopText != null && playText != null && loadingText != null) && useIcons)
                usesCustomIcons = true;
            else if ((stopText == null || playText == null || loadingText == null) && !useIcons)
                throw new UnsupportedOperationException(NULL_PARAMETER_ERROR);

        } finally {
            a.recycle();
        }
    }

    //Implementation
    public void withUrl(String url) {
        this.url = url;
        setUpMediaPlayer();
    }

    private void setUpMediaPlayer() {
        if (useIcons) {
            setUpFont();
        }
        setText(playText);
        this.setOnClickListener(onViewClickListener);
    }

    private void setUpFont() {
        if (!usesCustomIcons) {
            Typeface iconFont = Typeface.createFromAsset(context.getAssets(), "audio-player-view-font.ttf");
            setTypeface(iconFont);
            playText = getResources().getString(R.string.playIcon);
            stopText = getResources().getString(R.string.stopIcon);
            loadingText = getResources().getString(R.string.loadingIcon);
        }
    }


    private OnClickListener onViewClickListener = new OnClickListener() {

        public void onClick(View v) {
            try {
                toggleAudio();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    };

    public void toggleAudio() throws IOException {
        if (mediaPlayer != null && mediaPlayer.isPlaying())
            stop();
        else
            play();
    }

    private void play() throws IOException {
        // Todo check what happens after second time loading
        if (!audioReady) {

            mediaPlayer = new MediaPlayer();

            mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            mediaPlayer.setDataSource(url);

            prepareAsync();

            mediaPlayer.setOnPreparedListener(onPreparedListener);
            mediaPlayer.setOnCompletionListener(onCompletionListener);

        } else
            playAudio();
    }

    private void prepareAsync() {
        mediaPlayer.prepareAsync();
        setTextLoading();
        sendCallbackAudioPreparing();
    }

    private void playAudio() {
        mediaPlayer.start();
        setText(stopText);
    }


    private MediaPlayer.OnPreparedListener onPreparedListener = new MediaPlayer.OnPreparedListener() {

        @Override
        public void onPrepared(MediaPlayer mediaPlayer) {
            playAudio();
            audioReady = true;
            clearAnimation();
            sendCallbackAudioReady();
        }
    };

    private MediaPlayer.OnCompletionListener onCompletionListener = new MediaPlayer.OnCompletionListener() {
        @Override
        public void onCompletion(MediaPlayer mediaPlayer) {
            setText(playText);
            sendCallbackAudioFinished();
        }
    };


    private void setTextLoading() {
        setText(loadingText);
        if (useIcons)
            startAnimation();
    }

    private void startAnimation() {
        final Animation rotation = AnimationUtils.loadAnimation(context, R.anim.rotate_indefinitely);
        this.startAnimation(rotation);
    }

    private void stop() {
        if (mediaPlayer != null && mediaPlayer.isPlaying()) {
            mediaPlayer.pause();
            mediaPlayer.seekTo(0);
            setText(playText);
        }
    }

    public void destroy() {
        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
            audioReady = false;
        }
    }


}