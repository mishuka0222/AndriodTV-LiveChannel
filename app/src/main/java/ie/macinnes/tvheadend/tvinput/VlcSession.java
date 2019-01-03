/*
 * Copyright (c) 2016 Kiall Mac Innes <kiall@macinnes.ie>
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may
 * not use this file except in compliance with the License. You may obtain
 * a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */
package ie.macinnes.tvheadend.tvinput;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.Context;
import android.media.tv.TvTrackInfo;
import android.net.Uri;
import android.os.Handler;
import android.util.Log;
import android.view.Surface;

import org.videolan.libvlc.LibVLC;
import org.videolan.libvlc.Media;
import org.videolan.libvlc.MediaPlayer;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import ie.macinnes.tvheadend.Constants;
import ie.macinnes.tvheadend.account.AccountUtils;
import ie.macinnes.tvheadend.client.ClientUtils;
import ie.macinnes.tvheadend.model.Channel;

public class VlcSession extends BaseSession {
    private static final String TAG = VlcSession.class.getName();

    private LibVLC mLibVLC;
    private MediaPlayer mMediaPlayer;

    /**
     * Creates a new Session.
     *
     * @param context The context of the application
     */
    public VlcSession(Context context, Handler serviceHandler) {
        super(context, serviceHandler);
        Log.d(TAG, "Session created (" + mSessionNumber + ")");

        ArrayList<String> options = new ArrayList<>();
        options.add("--http-reconnect");
        options.add("--network-caching=2000");

        mLibVLC = new LibVLC(options);
    }

    @Override
    public boolean onSetSurface(Surface surface) {
        Log.d(TAG, "Session onSetSurfaces (" + mSessionNumber + ")");

        mSurface = surface;

        if (mMediaPlayer != null && mSurface != null) {
            mMediaPlayer.getVLCVout().setVideoSurface(surface, null);
            mMediaPlayer.getVLCVout().attachViews();
        }

        return true;
    }

    @Override
    public void onSetStreamVolume(float volume) {
        Log.d(TAG, "Session onSetStreamVolume: " + volume + " (" + mSessionNumber + ")");

        mVolume = volume;

        if (mMediaPlayer != null) {
            mMediaPlayer.setVolume((int) mVolume * 100);
        }
    }

    @Override
    public boolean onSelectTrack(int type, String trackId) {
        if (trackId == null) {
            return true;
        }

        int trackIndex = getIndexFromTrackId(trackId);
        Boolean result = false;

        Log.d(TAG, "Selecting track: " + type + "/" + trackId + "/" + trackIndex);

        if (mMediaPlayer != null) {
            if (type == TvTrackInfo.TYPE_VIDEO) {
                result = mMediaPlayer.setVideoTrack(trackIndex);
            } else if (type == TvTrackInfo.TYPE_AUDIO) {
                result = mMediaPlayer.setAudioTrack(trackIndex);
            } else {
                return false;
            }

            if (result) {
                Log.d(TAG, "Selected track: " + type + "/" + trackId + "/" + trackIndex);
                notifyTrackSelected(type, trackId);
            } else {
                Log.d(TAG, "Failed to select track: " + type + "/" + trackId + "/" + trackIndex);
            }
        }

        return result;
    }

    protected boolean playChannel(Channel channel) {
        // Stop any existing playback
        stopPlayback();

        // Gather Details on the Channel
        String channelUuid = channel.getInternalProviderData().getUuid();

        // Gather Details on the TVHeadend Instance
        AccountManager accountManager = AccountManager.get(mContext);;
        Account account = AccountUtils.getActiveAccount(mContext);

        String username = account.name;
        String password = accountManager.getPassword(account);
        String hostname = accountManager.getUserData(account, Constants.KEY_HOSTNAME);
        String httpPort = accountManager.getUserData(account, Constants.KEY_HTTP_PORT);

        // Create authentication headers and streamUri
        Map<String, String> headers = ClientUtils.createBasicAuthHeader(username, password);
        Uri videoUri = Uri.parse("http://" + username + ":" + password + "@" + hostname + ":" + httpPort + "/stream/channel/" + channelUuid + "?profile=tif");

        // Prepare the media player
        mMediaPlayer = prepareMediaPlayer(videoUri, headers);

        // Start the media playback
        Log.d(TAG, "Starting playback of channel: " + channel.toString());
        mMediaPlayer.play();

        return mMediaPlayer != null;
    }

    protected void stopPlayback() {
        Log.d(TAG, "Session stopPlayback (" + mSessionNumber + ")");

        if (mMediaPlayer != null) {
            mMediaPlayer.stop();
            mMediaPlayer.release();
            mMediaPlayer = null;
        }
    }

    private MediaPlayer prepareMediaPlayer(Uri videoUri, Map<String, String> headers) {
        // Create and prep the MediaPlayer instance
        MediaPlayer mediaPlayer = new MediaPlayer(mLibVLC);
        mediaPlayer.setEventListener(new MediaPlayerEventListener());

        try {
            Log.d(TAG, "Preparing video: " + videoUri + ".");

            Media currentMedia = new Media(mLibVLC, videoUri);
            currentMedia.setEventListener(new MediaEventListener());
            mediaPlayer.setMedia(currentMedia);

        } catch (Throwable e) {
            Log.e(TAG, "Error preparing video: " + e);

            mediaPlayer.release();

            return null;
        }

        mediaPlayer.getMedia().setHWDecoderEnabled(true, false);
        mediaPlayer.getVLCVout().setVideoSurface(mSurface, null);
        mediaPlayer.getVLCVout().attachViews();
        mediaPlayer.setVolume((int) mVolume * 100);

        return mediaPlayer;
    }

    private List<TvTrackInfo> getAllTracks() {
        String trackId;
        List<TvTrackInfo> tracks = new ArrayList<>();

        Log.d(TAG, "GAT: Finding all available tracks");

        MediaPlayer.TrackDescription[] trackDescriptions = mMediaPlayer.getVideoTracks();

        if (trackDescriptions != null) {
            Log.d(TAG, "GAT: Processing " + trackDescriptions.length + " video tracks");
            for (MediaPlayer.TrackDescription trackDescription : trackDescriptions) {
                if (trackDescription.id == -1) continue;

                Log.d(TAG, "GAT: Found video track. ID: " + trackDescription.id + " Name: " + trackDescription.name);

                trackId = getTrackId(TvTrackInfo.TYPE_VIDEO, trackDescription.id);

                TvTrackInfo.Builder builder = new TvTrackInfo.Builder(TvTrackInfo.TYPE_VIDEO, trackId);

                tracks.add(builder.build());
            }
        }

        trackDescriptions = mMediaPlayer.getAudioTracks();

        if (trackDescriptions != null) {
            Log.d(TAG, "GAT: Processing " + trackDescriptions.length + " audio tracks");
            for (MediaPlayer.TrackDescription trackDescription : trackDescriptions) {
                if (trackDescription.id == -1) continue;

                Log.d(TAG, "GAT: Found audio track. ID: " + trackDescription.id + " Name: " + trackDescription.name);

                trackId = getTrackId(TvTrackInfo.TYPE_AUDIO, trackDescription.id);

                TvTrackInfo.Builder builder = new TvTrackInfo.Builder(TvTrackInfo.TYPE_AUDIO, trackId);

                tracks.add(builder.build());
            }
        }

        trackDescriptions = mMediaPlayer.getSpuTracks();

        if (trackDescriptions != null) {
            Log.d(TAG, "GAT: Processing " + trackDescriptions.length + " text tracks");
            for (MediaPlayer.TrackDescription trackDescription : trackDescriptions) {
                if (trackDescription.id == -1) continue;

                Log.d(TAG, "GAT: Found subtitle track. ID: " + trackDescription.id + " Name: " + trackDescription.name);

                trackId = getTrackId(TvTrackInfo.TYPE_SUBTITLE, trackDescription.id);

                TvTrackInfo.Builder builder = new TvTrackInfo.Builder(TvTrackInfo.TYPE_SUBTITLE, trackId);

                tracks.add(builder.build());
            }
        }

        return tracks;
    }

    private static String getTrackId(int trackType, int trackIndex) {
        return trackType + "-" + trackIndex;
    }

    private static int getIndexFromTrackId(String trackId) {
        return Integer.parseInt(trackId.split("-")[1]);
    }

    private class MediaPlayerEventListener implements MediaPlayer.EventListener {
        @Override
        public void onEvent(MediaPlayer.Event event) {
            switch(event.type) {
                case MediaPlayer.Event.ESAdded:
                case MediaPlayer.Event.ESDeleted:
                case MediaPlayer.Event.Vout:
                    Log.d(TAG, "Received VLC MediaPlayer.Event: ESAdded/ESDeleted/Vout");
                    notifyTracksChanged(getAllTracks());
                    notifyTrackSelected(TvTrackInfo.TYPE_AUDIO,
                            getTrackId(TvTrackInfo.TYPE_AUDIO, mMediaPlayer.getAudioTrack()));
                    notifyTrackSelected(TvTrackInfo.TYPE_VIDEO,
                            getTrackId(TvTrackInfo.TYPE_VIDEO, mMediaPlayer.getVideoTrack()));
                    notifyTrackSelected(TvTrackInfo.TYPE_SUBTITLE,
                            getTrackId(TvTrackInfo.TYPE_SUBTITLE, mMediaPlayer.getSpuTrack()));
                    break;
                case MediaPlayer.Event.Playing:
                    Log.d(TAG, "Received VLC MediaPlayer.Event: Playing");
                    notifyVideoAvailable();
                    break;
                case MediaPlayer.Event.TimeChanged:
                case MediaPlayer.Event.PositionChanged:
                    // Don't log these events, VLC fires them all the time...
                    break;
                case MediaPlayer.Event.EndReached:
                case MediaPlayer.Event.Paused:
                case MediaPlayer.Event.Stopped:
                case MediaPlayer.Event.Opening:
                case MediaPlayer.Event.MediaChanged:
                default:
                    Log.d(TAG, "Received VLC MediaPlayer.Event: " + event.type);
                    break;
            }
        }
    }

    private class MediaEventListener implements Media.EventListener {
        @Override
        public void onEvent(Media.Event event) {
            switch(event.type) {
                default:
                    Log.d(TAG, "Received VLC Media.Event: " + event.type);
                    break;
            }
        }
    }
}
