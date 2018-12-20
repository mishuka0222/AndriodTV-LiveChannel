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
package ie.macinnes.tvheadend.migrate;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.util.Log;

import ie.macinnes.tvheadend.Constants;


public class MigrateUtils {
    public static final String TAG = MigrateUtils.class.getSimpleName();

    public static void doMigrate(Context context) {
        Log.d(TAG, "doMigrate()");

        // Store the current version
        int currentApplicationVersion = 0;

        try {
            currentApplicationVersion = context.getPackageManager()
                    .getPackageInfo(context.getPackageName(), 0)
                    .versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            Log.w(TAG, "Failed to lookup current application version", e);
            return;
        }

        // Store the last migrated version
        SharedPreferences sharedPreferences = context.getSharedPreferences(
                Constants.PREFERENCE_TVHEADEND, Context.MODE_PRIVATE);

        int lastInstalledApplicationVersion = sharedPreferences.getInt(
                Constants.KEY_APP_VERSION, 0);

        Log.d(TAG, "Migrate from " + lastInstalledApplicationVersion + " to " + currentApplicationVersion);

        // Run any migrations
        if (currentApplicationVersion != lastInstalledApplicationVersion) {
            if (lastInstalledApplicationVersion <= 14) {
                migrateAccountsPortName(context);
            }
        }

        // Store the current version as the last installed version
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt(Constants.KEY_APP_VERSION, currentApplicationVersion);
        editor.commit();
    }

    protected static void migrateAccountsPortName(Context context) {
        Log.d(TAG, "migrateAccountsPortData()");

        AccountManager accountManager = AccountManager.get(context);
        Account[] accounts = accountManager.getAccountsByType(Constants.ACCOUNT_TYPE);

        for (Account account : accounts) {
            String port = accountManager.getUserData(account, "PORT");

            if (port != null) {
                accountManager.setUserData(account, Constants.KEY_HTTP_PORT, port);
                accountManager.setUserData(account, "PORT", null);
            }
        }
    }
}

