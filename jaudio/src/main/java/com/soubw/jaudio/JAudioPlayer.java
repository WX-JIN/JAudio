/*
 * Copyright (C) 2015 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.soubw.jaudio;

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;

import java.nio.ShortBuffer;

public class JAudioPlayer {

    public interface OnCompletionListener {
        public void onCompletion();
    }

    private ShortBuffer mSamples;
    /**
     * 取样频率
     */
    private int sampleRate;
    /**
     * 声道个数：单声道或双声道
     */
    private int soundChanelCount;
    /**
     * 音频文件中的声道样本总数
     */
    private int soundSampleCount;
    private AudioTrack mAudioTrack;
    private short[] mBuffer;
    private int mPlaybackStart;  // Start offset, in samples.
    private Thread mPlayThread;
    private boolean mKeepPlaying;
    private OnCompletionListener mListener;

    public JAudioPlayer(ShortBuffer samples, int sampleRate, int channels, int numSamples) {
        mSamples = samples;
        this.sampleRate = sampleRate;
        soundChanelCount = channels;
        soundSampleCount = numSamples;
        mPlaybackStart = 0;

        int bufferSize = AudioTrack.getMinBufferSize(this.sampleRate, soundChanelCount == 1 ? AudioFormat.CHANNEL_OUT_MONO : AudioFormat.CHANNEL_OUT_STEREO,
                AudioFormat.ENCODING_PCM_16BIT);
        // make sure minBufferSize can contain at least 1 second of audio (16 bits sample).
        if (bufferSize < soundChanelCount * this.sampleRate * 2) {
            bufferSize = soundChanelCount * this.sampleRate * 2;
        }
        mBuffer = new short[bufferSize/2]; // bufferSize is in Bytes.
        mAudioTrack = new AudioTrack(
                AudioManager.STREAM_MUSIC,
                this.sampleRate,
                soundChanelCount == 1 ? AudioFormat.CHANNEL_OUT_MONO : AudioFormat.CHANNEL_OUT_STEREO,
                AudioFormat.ENCODING_PCM_16BIT,
                mBuffer.length * 2,
                AudioTrack.MODE_STREAM);
        // Check when player played all the given data and notify user if mListener is set.
        mAudioTrack.setNotificationMarkerPosition(soundSampleCount - 1);  // Set the marker to the end.
        mAudioTrack.setPlaybackPositionUpdateListener(
                new AudioTrack.OnPlaybackPositionUpdateListener() {
            @Override
            public void onPeriodicNotification(AudioTrack track) {}

            @Override
            public void onMarkerReached(AudioTrack track) {
                stop();
                if (mListener != null) {
                    mListener.onCompletion();
                }
            }
        });
        mPlayThread = null;
        mKeepPlaying = true;
        mListener = null;
    }

    public JAudioPlayer(JAudioFile mSoundFile) {
        this(mSoundFile.getSamples(), mSoundFile.getSampleRate(), mSoundFile.getSoundChanelCount(), mSoundFile.getSoundSampleCount());
    }

    public void setOnCompletionListener(OnCompletionListener listener) {
        mListener = listener;
    }

    public boolean isPlaying() {
        return mAudioTrack.getPlayState() == AudioTrack.PLAYSTATE_PLAYING;
    }

    public boolean isPaused() {
        return mAudioTrack.getPlayState() == AudioTrack.PLAYSTATE_PAUSED;
    }

    public void start() {
        if (isPlaying()) {
            return;
        }
        mKeepPlaying = true;
        mAudioTrack.flush();
        mAudioTrack.play();
        // Setting thread feeding the audio samples to the audio hardware.
        // (Assumes soundChanelCount = 1 or 2).
        mPlayThread = new Thread () {
            public void run() {
                int position = mPlaybackStart * soundChanelCount;
                mSamples.position(position);
                int limit = soundSampleCount * soundChanelCount;
                while (mSamples.position() < limit && mKeepPlaying) {
                    int numSamplesLeft = limit - mSamples.position();
                    if(numSamplesLeft >= mBuffer.length) {
                        mSamples.get(mBuffer);
                    } else {
                        for(int i=numSamplesLeft; i<mBuffer.length; i++) {
                            mBuffer[i] = 0;
                        }
                        mSamples.get(mBuffer, 0, numSamplesLeft);
                    }
                    // TODO(nfaralli): use the write method that takes a ByteBuffer as argument.
                    mAudioTrack.write(mBuffer, 0, mBuffer.length);
                }
            }
        };
        mPlayThread.start();
    }

    public void pause() {
        if (isPlaying()) {
            mAudioTrack.pause();
            // mAudioTrack.write() should block if it cannot write.
        }
    }

    public void stop() {
        if (isPlaying() || isPaused()) {
            mKeepPlaying = false;
            mAudioTrack.pause();  // pause() stops the playback immediately.
            mAudioTrack.stop();   // Unblock mAudioTrack.write() to avoid deadlocks.
            if (mPlayThread != null) {
                try {
                    mPlayThread.join();
                } catch (InterruptedException e) {
                }
                mPlayThread = null;
            }
            mAudioTrack.flush();  // just in case...
        }
    }

    public void release() {
        stop();
        mAudioTrack.release();
    }

    public void seekTo(int msec) {
        boolean wasPlaying = isPlaying();
        stop();
        mPlaybackStart = (int)(msec * (sampleRate / 1000.0));
        if (mPlaybackStart > soundSampleCount) {
            mPlaybackStart = soundSampleCount;  // Nothing to play...
        }
        mAudioTrack.setNotificationMarkerPosition(soundSampleCount - 1 - mPlaybackStart);
        if (wasPlaying) {
            start();
        }
    }

    public int getCurrentPosition() {
    	int curPos = 0;
    	try{
    	curPos = (int)((mPlaybackStart + mAudioTrack.getPlaybackHeadPosition()) *
                (1000.0 / sampleRate));
    	}catch(Exception e){
//    	mAudioTrack.setNotificationMarkerPosition(soundSampleCount - 1);
    	}
        return curPos;
    }
}
