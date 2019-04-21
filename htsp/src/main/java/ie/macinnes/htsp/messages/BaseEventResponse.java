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

package ie.macinnes.htsp.messages;

import android.annotation.TargetApi;
import android.content.ContentValues;
import android.media.tv.TvContract;
import android.os.Build;
import android.text.TextUtils;

import ie.macinnes.htsp.HtspMessage;
import ie.macinnes.htsp.ResponseMessage;

public class BaseEventResponse extends ResponseMessage {
    protected int mEventId;
    protected int mChannelId;
    protected Long mStart;
    protected Long mStop;
    protected String mTitle;
    protected String mSubTitle;
    protected String mSummary;
    protected String mDescription;
    // Some fields skipped
    protected int mSeasonNumber;
    protected int mSeasonCount;
    protected int mEpisodeNumber;
    protected int mEpisodeCount;
    // Some fields skipped
    private String mImage;

    public int getEventId() {
        return mEventId;
    }

    public void setEventId(int eventId) {
        mEventId = eventId;
    }

    public int getChannelId() {
        return mChannelId;
    }

    public void setChannelId(int channelId) {
        mChannelId = channelId;
    }

    public Long getStart() {
        return mStart;
    }

    public void setStart(Long start) {
        mStart = start;
    }

    public Long getStop() {
        return mStop;
    }

    public void setStop(Long stop) {
        mStop = stop;
    }

    public String getTitle() {
        return mTitle;
    }

    public void setTitle(String title) {
        mTitle = title;
    }

    public String getSubTitle() {
        return mSubTitle;
    }

    public void setSubTitle(String subTitle) {
        mSubTitle = subTitle;
    }

    public String getSummary() {
        return mSummary;
    }

    public void setSummary(String summary) {
        mSummary = summary;
    }

    public String getDescription() {
        return mDescription;
    }

    public void setDescription(String description) {
        mDescription = description;
    }

    public int getSeasonNumber() {
        return mSeasonNumber;
    }

    public void setSeasonNumber(int seasonNumber) {
        mSeasonNumber = seasonNumber;
    }

    public int getSeasonCount() {
        return mSeasonCount;
    }

    public void setSeasonCount(int seasonCount) {
        mSeasonCount = seasonCount;
    }

    public int getEpisodeNumber() {
        return mEpisodeNumber;
    }

    public void setEpisodeNumber(int episodeNumber) {
        mEpisodeNumber = episodeNumber;
    }

    public int getEpisodeCount() {
        return mEpisodeCount;
    }

    public void setEpisodeCount(int episodeCount) {
        mEpisodeCount = episodeCount;
    }

    public String getImage() {
        return mImage;
    }

    public void setImage(String image) {
        mImage = image;
    }

    public void fromHtspMessage(HtspMessage htspMessage) {
        super.fromHtspMessage(htspMessage);

        setEventId(htspMessage.getInt("eventId"));
        setChannelId(htspMessage.getInt("channelId", INVALID_INT_VALUE));
        setStart(htspMessage.getLong("start", INVALID_LONG_VALUE));
        setStop(htspMessage.getLong("stop", INVALID_LONG_VALUE));
        setTitle(htspMessage.getString("title", null));
        setSubTitle(htspMessage.getString("subtitle", null));
        setSummary(htspMessage.getString("summary", null));
        setDescription(htspMessage.getString("description", null));
        // Some fields skipped
        setSeasonNumber(htspMessage.getInt("seasonNumber", INVALID_INT_VALUE));
        setSeasonCount(htspMessage.getInt("seasonCount", INVALID_INT_VALUE));
        setEpisodeNumber(htspMessage.getInt("episodeNumber", INVALID_INT_VALUE));
        setEpisodeCount(htspMessage.getInt("episodeCount", INVALID_INT_VALUE));
        // Some fields skipped
        setImage(htspMessage.getString("image", null));
    }

    public String toString() {
        return "eventId: " + getEventId();
    }

    @TargetApi(24)
    public ContentValues toContentValues(long channelId) {
        ContentValues values = new ContentValues();

        values.put(TvContract.Programs.COLUMN_CHANNEL_ID, channelId);
        values.put(TvContract.Programs.COLUMN_INTERNAL_PROVIDER_DATA, mEventId);

        if (!TextUtils.isEmpty(mTitle)) {
            values.put(TvContract.Programs.COLUMN_TITLE, mTitle);
        }

        if (!TextUtils.isEmpty(mSubTitle)) {
            values.put(TvContract.Programs.COLUMN_EPISODE_TITLE, mSubTitle);
        }

        if (!TextUtils.isEmpty(mSummary)) {
            values.put(TvContract.Programs.COLUMN_SHORT_DESCRIPTION, mSummary);
        }

        if (!TextUtils.isEmpty(mDescription)) {
            values.put(TvContract.Programs.COLUMN_LONG_DESCRIPTION, mDescription);
        }

        if (mStart != INVALID_LONG_VALUE) {
            values.put(TvContract.Programs.COLUMN_START_TIME_UTC_MILLIS, mStart * 1000);
        }

        if (mStop != INVALID_LONG_VALUE) {
            values.put(TvContract.Programs.COLUMN_END_TIME_UTC_MILLIS, mStop * 1000);
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            if (mSeasonNumber != INVALID_INT_VALUE) {
                values.put(TvContract.Programs.COLUMN_SEASON_DISPLAY_NUMBER, mSeasonNumber);
            }

            if (mEpisodeNumber != INVALID_INT_VALUE) {
                values.put(TvContract.Programs.COLUMN_EPISODE_DISPLAY_NUMBER, mEpisodeNumber);
            }
        } else {
            if (mSeasonNumber != INVALID_INT_VALUE) {
                values.put(TvContract.Programs.COLUMN_SEASON_NUMBER, mSeasonNumber);
            }

            if (mEpisodeNumber != INVALID_INT_VALUE) {
                values.put(TvContract.Programs.COLUMN_EPISODE_NUMBER, mEpisodeNumber);
            }
        }

        if (!TextUtils.isEmpty(mImage)) {
            values.put(TvContract.Programs.COLUMN_POSTER_ART_URI, mImage);
        }

        return values;
    }
}
