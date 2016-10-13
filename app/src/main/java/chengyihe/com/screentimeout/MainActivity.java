package chengyihe.com.screentimeout;

import android.content.ContentResolver;
import android.database.ContentObserver;
import android.provider.ContactsContract;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.provider.Settings;
import android.database.Cursor;
import android.util.Log;
import android.content.Intent;
import android.net.Uri;
import android.widget.RadioGroup;
import android.widget.Button;
import android.widget.Toast;
import android.os.Handler;

public class MainActivity extends AppCompatActivity {
    private final String TAG = MainActivity.class.getSimpleName();
    private TextView mScreenTimeout;
    private RadioGroup mRadioGroup;
    private Button mButton;
    private Handler mHandler;
    private ContentObserver mContentObserver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mScreenTimeout = (TextView) findViewById(R.id.screen_timeout);
        mRadioGroup = (RadioGroup) findViewById(R.id.timeout_group);
        mButton = (Button) findViewById(R.id.button);
        mHandler = new Handler();
        mButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int timeout = -1;
                int checkedId = mRadioGroup.getCheckedRadioButtonId();
                if (checkedId == R.id.never_button) {
                    timeout = 0;
                } else if (checkedId == R.id._15s_button) {
                    timeout = 15;
                } else if (checkedId == R.id._30s_button) {
                    timeout = 30;
                } else if (checkedId == R.id._01m_button) {
                    timeout = 60;
                } else if (checkedId == R.id._02m_button) {
                    timeout = 60 * 2;
                } else if (checkedId == R.id._05m_button) {
                    timeout = 60 * 5;
                } else if (checkedId == R.id._10m_button) {
                    timeout = 60 * 10;
                } else if (checkedId == R.id._30m_button) {
                    timeout = 60 * 30;
                }

                if (timeout >= 0) {
                    Settings.System.putInt(getContentResolver(), Settings.System.SCREEN_OFF_TIMEOUT, timeout * 1000);
                    Toast toast = Toast.makeText(getApplicationContext(), "Update screen timeout successfully", Toast.LENGTH_SHORT);
                    toast.show();
                }
            }
        });

        mContentObserver = new ContentObserver(mHandler) {
            @Override
            public void onChange(boolean selfChange) {
                Log.d(TAG, "onChange 01" + String.valueOf(selfChange));
                onChange(selfChange, null);
            }

            @Override
            public void onChange(boolean selfChange, Uri uri) {
                Log.d(TAG, "onChange 02" + String.valueOf(selfChange));
                updateScreenTimeout();
            }

        };

        if (Settings.System.canWrite(getApplicationContext()) == false) {
            Intent intent = new Intent(android.provider.Settings.ACTION_MANAGE_WRITE_SETTINGS);
            intent.setData(Uri.parse("package:" + getPackageName()));
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        }

/*
        Cursor cursor = getContentResolver().query(Settings.System.CONTENT_URI, null, null, null, null);
        Log.d(TAG, "getCount: " + cursor.getCount());
        Log.d(TAG, "getColumnCount: " + cursor.getColumnCount());

        while (cursor.moveToNext()) {
            if (cursor.getString(1).equals(System.SCREEN_OFF_TIMEOUT)) {
                int seconds = cursor.getInt(2) / 1000;
                mScreenTimeout.setText(String.valueOf(seconds) + " seconds");
            }
        }
        cursor.close();
        */
    }

    @Override
    protected void onPause() {
        super.onPause();
        getContentResolver().unregisterContentObserver(mContentObserver);
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateScreenTimeout();
        getContentResolver().registerContentObserver(Settings.System.CONTENT_URI, true, mContentObserver);
    }

    private void updateScreenTimeout() {
        try {
            int seconds = Settings.System.getInt(getContentResolver(), Settings.System.SCREEN_OFF_TIMEOUT) / 1000;
            int minutes = seconds / 60;
            if (minutes == 1) {
                mScreenTimeout.setText(String.valueOf(minutes) + " minute");
            } else if (minutes > 1) {
                mScreenTimeout.setText(String.valueOf(minutes) + " minutes");
            } else {
                mScreenTimeout.setText(String.valueOf(seconds) + " seconds");
            }
        } catch (Exception e) {
            Log.e(TAG, "Fail to get system setting " + Settings.System.SCREEN_OFF_TIMEOUT);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}