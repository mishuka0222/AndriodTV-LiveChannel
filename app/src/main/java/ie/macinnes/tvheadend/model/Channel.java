/* Copyright 2016 Kiall Mac Innes <kiall@macinnes.ie>

Licensed under the Apache License, Version 2.0 (the "License"); you may
not use this file except in compliance with the License. You may obtain
a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
License for the specific language governing permissions and limitations
under the License.
*/
package ie.macinnes.tvheadend.model;

import android.content.ContentValues;
import android.database.Cursor;
import android.media.tv.TvContract;
import android.support.annotation.NonNull;
import android.text.TextUtils;

import ie.macinnes.tvheadend.client.TVHClient;

public class Channel implements Comparable<Channel> {
    public static final long INVALID_CHANNEL_ID = -1;

    private long mId = INVALID_CHANNEL_ID;
    private String mInputId;
    private String mType;
    private String mDisplayNumber;
    private String mDisplayName;
    private String mDescription;
    private int mOriginalNetworkId;
    private int mTransportStreamId = 0;
    private int mServiceId = 0;
    private InternalProviderData mInternalProviderData;

    public long getId() {
        return mId;
    }

    public void setId(long id) {
        mId = id;
    }

    public String getInputId() {
        return mInputId;
    }

    public void setInputId(String inputId) {
        mInputId = inputId;
    }

    public String getType() {
        return mType;
    }

    public void setType(String type) {
        mType = type;
    }

    public String getDisplayNumber() {
        return mDisplayNumber;
    }

    public void setDisplayNumber(String displayNumber) {
        mDisplayNumber = displayNumber;
    }

    public String getDisplayName() {
        return mDisplayName;
    }

    public void setDisplayName(String displayName) {
        mDisplayName = displayName;
    }

    public String getDescription() {
        return mDescription;
    }

    public void setDescription(String description) {
        mDescription = description;
    }

    public int getOriginalNetworkId() {
        return mOriginalNetworkId;
    }

    public void setOriginalNetworkId(int originalNetworkId) {
        mOriginalNetworkId = originalNetworkId;
    }

    public int getTransportStreamId() {
        return mTransportStreamId;
    }

    public void setTransportStreamId(int transportStreamId) {
        mTransportStreamId = transportStreamId;
    }

    public int getServiceId() {
        return mServiceId;
    }

    public void setServiceId(int serviceId) {
        mServiceId = serviceId;
    }

    public InternalProviderData getInternalProviderData() {
        return mInternalProviderData;
    }

    public void setInternalProviderData(InternalProviderData internalProviderData) {
        mInternalProviderData = internalProviderData;
    }

    public static Channel fromCursor(Cursor cursor) {
        Channel channel = new Channel();

        int index = cursor.getColumnIndex(TvContract.Channels._ID);
        if (index >= 0 && !cursor.isNull(index)) {
            channel.setId(cursor.getLong(index));
        }

        index = cursor.getColumnIndex(TvContract.Channels.COLUMN_INPUT_ID);
        if (index >= 0 && !cursor.isNull(index)) {
            channel.setInputId(cursor.getString(index));
        }

        index = cursor.getColumnIndex(TvContract.Channels.COLUMN_TYPE);
        if (index >= 0 && !cursor.isNull(index)) {
            channel.setType(cursor.getString(index));
        }

        index = cursor.getColumnIndex(TvContract.Channels.COLUMN_DISPLAY_NUMBER);
        if (index >= 0 && !cursor.isNull(index)) {
            channel.setDisplayNumber(cursor.getString(index));
        }

        index = cursor.getColumnIndex(TvContract.Channels.COLUMN_DISPLAY_NAME);
        if (index >= 0 && !cursor.isNull(index)) {
            channel.setDisplayName(cursor.getString(index));
        }

        index = cursor.getColumnIndex(TvContract.Channels.COLUMN_DESCRIPTION);
        if (index >= 0 && !cursor.isNull(index)) {
            channel.setDescription(cursor.getString(index));
        }

        index = cursor.getColumnIndex(TvContract.Channels.COLUMN_ORIGINAL_NETWORK_ID);
        if (index >= 0 && !cursor.isNull(index)) {
            channel.setOriginalNetworkId(cursor.getInt(index));
        }

        index = cursor.getColumnIndex(TvContract.Channels.COLUMN_TRANSPORT_STREAM_ID);
        if (index >= 0 && !cursor.isNull(index)) {
            channel.setTransportStreamId(cursor.getInt(index));
        }

        index = cursor.getColumnIndex(TvContract.Channels.COLUMN_SERVICE_ID);
        if (index >= 0 && !cursor.isNull(index)) {
            channel.setServiceId(cursor.getInt(index));
        }

        index = cursor.getColumnIndex(TvContract.Programs.COLUMN_INTERNAL_PROVIDER_DATA);
        if (index >= 0 && !cursor.isNull(index)) {
            channel.setInternalProviderData(InternalProviderData.fromString(cursor.getString(index)));
        }

        return channel;
    }

    public static Channel fromClientChannel(TVHClient.Channel clientChannel, String inputId) {
        Channel channel = new Channel();

        // Set the provided inputId
        channel.setInputId(inputId);

        // Copy values from the clientChannel
        channel.setDisplayNumber(Integer.toString(clientChannel.number));
        channel.setDisplayName(clientChannel.name);

        // Set generated values
        channel.setOriginalNetworkId(clientChannel.uuid.hashCode());

        // Set hardcoded values
        channel.setType(TvContract.Channels.TYPE_OTHER);

        // Prep and set a InternalProviderData object
        InternalProviderData providerData = new InternalProviderData();

        providerData.setUuid(clientChannel.uuid);

        channel.setInternalProviderData(providerData);

        return channel;
    }

    public String toString() {
        return "<Number: " + mDisplayNumber + ", Name: " + mDisplayName + ">";
    }

    public ContentValues toContentValues() {
        ContentValues values = new ContentValues();

        if (mId != INVALID_CHANNEL_ID) {
            values.put(TvContract.Channels._ID, mId);
        }

        if (!TextUtils.isEmpty(mInputId)) {
            values.put(TvContract.Channels.COLUMN_INPUT_ID, mInputId);
        } else {
            values.putNull(TvContract.Channels.COLUMN_INPUT_ID);
        }

        if (!TextUtils.isEmpty(mType)) {
            values.put(TvContract.Channels.COLUMN_TYPE, mType);
        } else {
            values.putNull(TvContract.Channels.COLUMN_TYPE);
        }

        if (!TextUtils.isEmpty(mDisplayNumber)) {
            values.put(TvContract.Channels.COLUMN_DISPLAY_NUMBER, mDisplayNumber);
        } else {
            values.putNull(TvContract.Channels.COLUMN_DISPLAY_NUMBER);
        }

        if (!TextUtils.isEmpty(mDisplayName)) {
            values.put(TvContract.Channels.COLUMN_DISPLAY_NAME, mDisplayName);
        } else {
            values.putNull(TvContract.Channels.COLUMN_DISPLAY_NAME);
        }

        if (!TextUtils.isEmpty(mDescription)) {
            values.put(TvContract.Channels.COLUMN_DESCRIPTION, mDescription);
        } else {
            values.putNull(TvContract.Channels.COLUMN_DESCRIPTION);
        }

        values.put(TvContract.Channels.COLUMN_ORIGINAL_NETWORK_ID, mOriginalNetworkId);
        values.put(TvContract.Channels.COLUMN_TRANSPORT_STREAM_ID, mTransportStreamId);
        values.put(TvContract.Channels.COLUMN_SERVICE_ID, mServiceId);

        if (mInternalProviderData != null) {
            values.put(TvContract.Channels.COLUMN_INTERNAL_PROVIDER_DATA, mInternalProviderData.toString());
        } else {
            values.putNull(TvContract.Channels.COLUMN_INTERNAL_PROVIDER_DATA);
        }

        return values;
    }

    @Override
    public int compareTo(@NonNull Channel other) {
        return Integer.compare(Integer.parseInt(mDisplayNumber), Integer.parseInt(other.mDisplayNumber));
    }

    public static class InternalProviderData {
        // TODO: Replace with gson store
        private String mUuid;

        public static InternalProviderData fromString(String string) {
            InternalProviderData providerData = new InternalProviderData();
            String[] parts = string.split(":");

            providerData.mUuid = parts[0];

            return providerData;
        }

        public String toString() {
            return mUuid;
        }

        public String getUuid() {
            return mUuid;
        }

        public void setUuid(String uuid) {
            mUuid = uuid;
        }
    }
}
