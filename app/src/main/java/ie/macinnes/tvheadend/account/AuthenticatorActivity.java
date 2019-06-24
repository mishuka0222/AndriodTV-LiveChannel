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
package ie.macinnes.tvheadend.account;

import android.accounts.Account;
import android.accounts.AccountAuthenticatorActivity;
import android.accounts.AccountManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v17.leanback.app.GuidedStepFragment;
import android.support.v17.leanback.widget.GuidanceStylist;
import android.support.v17.leanback.widget.GuidedAction;
import android.support.v17.leanback.widget.GuidedActionsStylist;
import android.text.InputType;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import java.util.List;

import ie.macinnes.htsp.Connection;
import ie.macinnes.htsp.ConnectionListener;
import ie.macinnes.tvheadend.BuildConfig;
import ie.macinnes.tvheadend.Constants;
import ie.macinnes.tvheadend.R;
import ie.macinnes.tvheadend.migrate.MigrateUtils;

public class AuthenticatorActivity extends AccountAuthenticatorActivity {
    private static final String TAG = AuthenticatorActivity.class.getName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        GuidedStepFragment fragment = new ServerFragment();
        fragment.setArguments(getIntent().getExtras());
        GuidedStepFragment.addAsRoot(this, fragment, android.R.id.content);
    }

    @Override
    public void onBackPressed() {
        if (GuidedStepFragment.getCurrentGuidedStepFragment(getFragmentManager())
                instanceof CompletedFragment) {
            finish();

        } else if (GuidedStepFragment.getCurrentGuidedStepFragment(getFragmentManager())
                instanceof FailedFragment) {
            finish();

        } else {
            super.onBackPressed();
        }
    }

    public static abstract class BaseGuidedStepFragment extends GuidedStepFragment {
        protected AccountManager mAccountManager;

        @Override
        public int onProvideTheme() {
            return R.style.Theme_Wizard_Account;
        }

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            mAccountManager = AccountManager.get(getActivity());
        }
    }

    public static class ServerFragment extends BaseGuidedStepFragment {
        private static final int ACTION_ID_HOSTNAME = 1;
        private static final int ACTION_ID_HTSP_PORT = 2;
        private static final int ACTION_ID_HTTP_PORT = 3;
        private static final int ACTION_ID_HTTP_PATH = 4;
        private static final int ACTION_ID_NEXT = 5;

        @NonNull
        @Override
        public GuidanceStylist.Guidance onCreateGuidance(Bundle savedInstanceState) {
            GuidanceStylist.Guidance guidance = new GuidanceStylist.Guidance(
                    "Tvheadend server",
                    "Enter your Tvheadend server hostname or IP address", null, null);

            return guidance;
        }

        @Override
        public void onCreateActions(@NonNull List<GuidedAction> actions, Bundle savedInstanceState) {
            GuidedAction action = new GuidedAction.Builder(getActivity())
                    .id(ACTION_ID_HOSTNAME)
                    .title("Hostname/IP")
                    .descriptionEditInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_URI)
                    .descriptionInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_URI)
                    .descriptionEditable(true)
                    .build();

            actions.add(action);

            action = new GuidedAction.Builder(getActivity())
                    .id(ACTION_ID_HTSP_PORT)
                    .title("HTSP Port Number")
                    .descriptionEditInputType(InputType.TYPE_CLASS_NUMBER)
                    .descriptionInputType(InputType.TYPE_CLASS_NUMBER)
                    .descriptionEditable(true)
                    .build();

            actions.add(action);

            action = new GuidedAction.Builder(getActivity())
                    .id(ACTION_ID_HTTP_PORT)
                    .title("HTTP Port Number")
                    .descriptionEditInputType(InputType.TYPE_CLASS_NUMBER)
                    .descriptionInputType(InputType.TYPE_CLASS_NUMBER)
                    .descriptionEditable(true)
                    .build();

            actions.add(action);

            action = new GuidedAction.Builder(getActivity())
                    .id(ACTION_ID_HTTP_PATH)
                    .title("HTTP Path Prefix")
                    .descriptionEditInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_URI)
                    .descriptionInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_URI)
                    .descriptionEditable(true)
                    .build();

            actions.add(action);
        }

        @Override
        public void onCreateButtonActions(@NonNull List<GuidedAction> actions, Bundle savedInstanceState) {
            GuidedAction action = new GuidedAction.Builder(getActivity())
                    .id(ACTION_ID_NEXT)
                    .title("Next")
                    .editable(false)
                    .build();

            actions.add(action);
        }

        @Override
        public void onGuidedActionClicked(GuidedAction action) {
            if (action.getId() == ACTION_ID_NEXT) {
                Bundle args = getArguments();

                // Hostname Field
                GuidedAction hostnameAction = findActionById(ACTION_ID_HOSTNAME);
                CharSequence hostnameValue = hostnameAction.getDescription();

                if (hostnameValue == null || TextUtils.isEmpty(hostnameValue)) {
                    Toast.makeText(getActivity(), "Invalid Hostname", Toast.LENGTH_SHORT).show();
                    return;
                }

                args.putString(Constants.KEY_HOSTNAME, hostnameValue.toString());

                // HTSP Port Field
                GuidedAction htspPortAction = findActionById(ACTION_ID_HTSP_PORT);
                CharSequence htspPortValue = htspPortAction.getDescription();

                if (htspPortValue == null || TextUtils.isEmpty(htspPortValue)) {
                    Toast.makeText(getActivity(), "Invalid HTSP Port", Toast.LENGTH_SHORT).show();
                    return;
                }

                args.putString(Constants.KEY_HTSP_PORT, htspPortAction.getDescription().toString());

                // HTTP Port Field
                GuidedAction httpPortAction = findActionById(ACTION_ID_HTTP_PORT);
                CharSequence httpPortValue = httpPortAction.getDescription();

                if (httpPortValue == null || TextUtils.isEmpty(httpPortValue)) {
                    Toast.makeText(getActivity(), "Invalid HTTP Port", Toast.LENGTH_SHORT).show();
                    return;
                }

                args.putString(Constants.KEY_HTTP_PORT, httpPortAction.getDescription().toString());

                // HTTP Path Field
                GuidedAction httpPathAction = findActionById(ACTION_ID_HTTP_PATH);
                CharSequence httpPathValue = httpPathAction.getDescription();

                if (httpPathValue != null) {
                    args.putString(Constants.KEY_HTTP_PATH, httpPathValue.toString());
                } else {
                    args.putString(Constants.KEY_HTTP_PATH, "");
                }

                // Move to the next setup
                GuidedStepFragment fragment = new AccountFragment();
                fragment.setArguments(args);
                add(getFragmentManager(), fragment);
            }
        }
    }

    public static class AccountFragment extends BaseGuidedStepFragment {
        private static final int ACTION_ID_USERNAME = 1;
        private static final int ACTION_ID_PASSWORD = 2;
        private static final int ACTION_ID_ADD_ACCOUNT = 3;

        @NonNull
        @Override
        public GuidanceStylist.Guidance onCreateGuidance(Bundle savedInstanceState) {
            GuidanceStylist.Guidance guidance = new GuidanceStylist.Guidance(
                    "Tvheadend Account",
                    "Enter your Tvheadend username and password", null, null);

            return guidance;
        }

        @Override
        public void onCreateActions(@NonNull List<GuidedAction> actions, Bundle savedInstanceState) {
            GuidedAction action = new GuidedAction.Builder(getActivity())
                    .id(ACTION_ID_USERNAME)
                    .title("Username")
                    .descriptionEditInputType(InputType.TYPE_CLASS_TEXT)
                    .descriptionInputType(InputType.TYPE_CLASS_TEXT)
                    .descriptionEditable(true)
                    .build();

            actions.add(action);

            action = new GuidedAction.Builder(getActivity())
                    .id(ACTION_ID_PASSWORD)
                    .title("Password")
                    .descriptionEditInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD)
                    .descriptionInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD)
                    .descriptionEditable(true)
                    .build();

            actions.add(action);
        }

        @Override
        public void onCreateButtonActions(@NonNull List<GuidedAction> actions, Bundle savedInstanceState) {
            GuidedAction action = new GuidedAction.Builder(getActivity())
                    .id(ACTION_ID_ADD_ACCOUNT)
                    .title("Finish")
                    .editable(false)
                    .build();

            actions.add(action);
        }

        @Override
        public void onGuidedActionClicked(GuidedAction action) {
            if (action.getId() == ACTION_ID_ADD_ACCOUNT) {
                Bundle args = getArguments();

                // Username Field
                GuidedAction usernameAction = findActionById(ACTION_ID_USERNAME);
                CharSequence usernameValue = usernameAction.getDescription();

                if (usernameValue == null || TextUtils.isEmpty(usernameValue)) {
                    Toast.makeText(getActivity(), "Invalid Username", Toast.LENGTH_SHORT).show();
                    return;
                }

                args.putString(Constants.KEY_USERNAME, usernameAction.getDescription().toString());

                // Password Field
                GuidedAction passwordAction = findActionById(ACTION_ID_PASSWORD);
                CharSequence passwordValue = passwordAction.getDescription();

                if (passwordValue == null || TextUtils.isEmpty(passwordValue)) {
                    Toast.makeText(getActivity(), "Invalid Password", Toast.LENGTH_SHORT).show();
                    return;
                }

                args.putString(Constants.KEY_PASSWORD, passwordAction.getDescription().toString());

                // Move to the next step
                GuidedStepFragment fragment = new ValidateHTSPAccountFragment();
                fragment.setArguments(args);
                add(getFragmentManager(), fragment);
            }
        }
    }

    public static class ValidateHTSPAccountFragment extends BaseGuidedStepFragment {
        private static final int ACTION_ID_PROCESSING = 1;

        private Connection mConnection;
        private Thread mHtspConnectionThread;


        @Override
        public GuidedActionsStylist onCreateActionsStylist() {
            GuidedActionsStylist stylist = new GuidedActionsStylist() {
                @Override
                public int onProvideItemLayoutId() {
                    return R.layout.setup_progress;
                }

            };
            return stylist;
        }

        @Override
        public int onProvideTheme() {
            return R.style.Theme_Wizard_Account_NoSelector;
        }

        @NonNull
        @Override
        public GuidanceStylist.Guidance onCreateGuidance(Bundle savedInstanceState) {
            GuidanceStylist.Guidance guidance = new GuidanceStylist.Guidance(
                    "Tvheadend Account",
                    "Checking your HTSP account", null, null);

            return guidance;
        }

        @Override
        public void onCreateActions(@NonNull List<GuidedAction> actions, Bundle savedInstanceState) {
            GuidedAction action = new GuidedAction.Builder(getActivity())
                    .id(ACTION_ID_PROCESSING)
                    .title("Processing")
                    .infoOnly(true)
                    .build();
            actions.add(action);
        }

        @Override
        public void onStart() {
            super.onStart();

            Bundle args = getArguments();

            final String accountType = args.getString(AccountManager.KEY_ACCOUNT_TYPE);
            final String accountName = args.getString(Constants.KEY_USERNAME);
            final String accountPassword = args.getString(Constants.KEY_PASSWORD);
            final String accountHostname = args.getString(Constants.KEY_HOSTNAME);
            final String accountHtspPort = args.getString(Constants.KEY_HTSP_PORT);
            final String accountHttpPort = args.getString(Constants.KEY_HTTP_PORT);
            final String accountHttpPath = args.getString(Constants.KEY_HTTP_PATH);

            mConnection = new Connection(accountHostname, Integer.parseInt(accountHtspPort), accountName, accountPassword, "android-tvheadend (auth)", BuildConfig.VERSION_NAME);

            final ConnectionListener connectionListener = new ConnectionListener() {
                @Override
                public void onStateChange(int state, int previous) {
                    Log.d(TAG, "HTSP Connection State Changed: " + state);
                    if (state == Connection.STATE_READY) {
                        // Close the connection, it's no longer needed
                        mConnection.close();

                        // Store the account
                        final Account account = new Account(accountName, accountType);

                        Bundle userdata = new Bundle();

                        userdata.putString(Constants.KEY_HOSTNAME, accountHostname);
                        userdata.putString(Constants.KEY_HTSP_PORT, accountHtspPort);
                        userdata.putString(Constants.KEY_HTTP_PORT, accountHttpPort);
                        userdata.putString(Constants.KEY_HTTP_PATH, accountHttpPath);

                        mAccountManager.addAccountExplicitly(account, accountPassword, userdata);

                        // Store the result, with the username too
                        userdata.putString(Constants.KEY_USERNAME, accountName);

                        AuthenticatorActivity activity = (AuthenticatorActivity) getActivity();
                        activity.setAccountAuthenticatorResult(userdata);

                        // Move to the CompletedFragment
                        GuidedStepFragment fragment = new CompletedFragment();
                        fragment.setArguments(getArguments());
                        add(getFragmentManager(), fragment);
                    } else if (state == Connection.STATE_FAILED) {
                        // Close the connection, it's no longer needed
                        mConnection.close();

                        Log.w(TAG, "Failed to validate credentials");

                        Bundle args = getArguments();
                        args.putString(Constants.KEY_ERROR_MESSAGE, "Failed to validate HTSP Credentials");

                        // Move to the failed step
                        GuidedStepFragment fragment = new FailedFragment();
                        fragment.setArguments(args);
                        add(getFragmentManager(), fragment);
                    }
                }
            };

            mConnection.addConnectionListener(connectionListener);

            mHtspConnectionThread = new Thread(mConnection);
            mHtspConnectionThread.start();
        }
    }

    public static class CompletedFragment extends BaseGuidedStepFragment {
        @NonNull
        @Override
        public GuidanceStylist.Guidance onCreateGuidance(Bundle savedInstanceState) {
            GuidanceStylist.Guidance guidance = new GuidanceStylist.Guidance(
                    "Tvheadend Account",
                    "Successfully Added Account",
                    null,
                    null);

            return guidance;
        }

        @Override
        public void onCreateActions(@NonNull List<GuidedAction> actions, Bundle savedInstanceState) {
            GuidedAction action = new GuidedAction.Builder(getActivity())
                    .title("Complete")
                    .description("You're all set!")
                    .editable(false)
                    .build();

            actions.add(action);
        }

        @Override
        public void onGuidedActionClicked(GuidedAction action) {
            getActivity().finish();
        }
    }

    public static class FailedFragment extends BaseGuidedStepFragment {
        @NonNull
        @Override
        public GuidanceStylist.Guidance onCreateGuidance(Bundle savedInstanceState) {
            GuidanceStylist.Guidance guidance = new GuidanceStylist.Guidance(
                    "Tvheadend Account",
                    "Failed to add account",
                    null,
                    null);

            return guidance;
        }

        @Override
        public void onResume() {
            super.onResume();

            Bundle args = getArguments();
            String errorMessage = args.getString(Constants.KEY_ERROR_MESSAGE);
            getGuidanceStylist().getDescriptionView().setText("Failed to add account: " + errorMessage);
        }

        @Override
        public void onCreateActions(@NonNull List<GuidedAction> actions, Bundle savedInstanceState) {
            GuidedAction action = new GuidedAction.Builder(getActivity())
                    .title("Complete")
                    .editable(false)
                    .build();

            actions.add(action);
        }

        @Override
        public void onGuidedActionClicked(GuidedAction action) {
            getActivity().finish();
        }
    }
}
