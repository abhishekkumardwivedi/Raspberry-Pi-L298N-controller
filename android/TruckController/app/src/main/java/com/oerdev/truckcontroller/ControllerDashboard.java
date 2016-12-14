package com.oerdev.truckcontroller;

import android.annotation.SuppressLint;
import android.content.SharedPreferences;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
public class ControllerDashboard extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = ControllerDashboard.class.getSimpleName();

    private static final String DEFAULT_BROKER = "192.168.0.101";
    private static final String DEFAULT_PORT = "1883";
    private static String CLIENT_ID = "client_robo_001";

    private static final String TOPIC_DEVICE_MESSAGE = "from/l298n/rovar/state";

    private static final String MOVE_FORWARD = "to/l298n/rovar/forward";
    private static final String MOVE_BACKWARD = "to/l298n/rovar/backward";
    private static final String MOVE_STOP = "to/l298n/rovar/stop";
    private static final String MOVE_RIGHT = "to/l298n/rovar/right";
    private static final String MOVE_LEFT = "to/l298n/rovar/left";

    private static final String TOPIC_COMMAND = "control/robo/move";

    private static final boolean AUTO_HIDE = true;

    private static final int AUTO_HIDE_DELAY_MILLIS = 3000;

    private static final int UI_ANIMATION_DELAY = 300;

    private View mContentView;
    private View mControlsView;
    private boolean mVisible;

    private Button left;
    private Button right;
    private Button move;
    private Button reverse;
    private Button stop;
    private Button connect;
    private TextView ip;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_controller_dashboard);

        mVisible = true;
        mContentView = findViewById(R.id.fullscreen_content);


        // Set up the user interaction to manually show or hide the system UI.
        mContentView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                toggle();
            }
        });

        left = (Button) findViewById(R.id.left);
        right = (Button) findViewById(R.id.right);
        move = (Button) findViewById(R.id.move);
        reverse = (Button) findViewById(R.id.reverse);
        stop = (Button) findViewById(R.id.stop);
        connect = (Button) findViewById(R.id.connect);
        ip = (TextView) findViewById(R.id.editTextBroker);

        left.setEnabled(false);
        right.setEnabled(false);
        move.setEnabled(false);
        reverse.setEnabled(false);
        stop.setEnabled(false);

        left.setOnClickListener(this);
        right.setOnClickListener(this);
        move.setOnClickListener(this);
        reverse.setOnClickListener(this);
        stop.setOnClickListener(this);


        connect.setOnClickListener(new View.OnClickListener() {

            String broker = ip.getText().toString();

            @Override
            public void onClick(View view) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        if(MQTTHelper.connect(DEFAULT_BROKER, DEFAULT_PORT, CLIENT_ID)) {
                            Log.d(TAG, "connected!!");

                            ControllerDashboard.this.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    left.setEnabled(true);
                                    right.setEnabled(true);
                                    move.setEnabled(true);
                                    reverse.setEnabled(true);
                                    stop.setEnabled(true);
                                }
                            });
                            Thread t = new Thread(subscriber);
                            t.start();
                        } else {
                            Looper.prepare();
                            Log.d(TAG, "failed to connect " + broker + "!!");
                        }
                    }
                }).start();
            }
        });
    }

    static private Runnable subscriber  = new Runnable() {
        @Override
        public void run() {
            MQTTHelper.subscribe(TOPIC_DEVICE_MESSAGE);
        }
    };

    public void sendCommand(String cmd) {
        if(MQTTHelper.isConnected()) {
            MQTTHelper.publish(cmd, "GO");
        } else {
            Looper.prepare();
            Toast.makeText(this, "Server unreachable!\n", Toast.LENGTH_SHORT);
        }
    }
    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        // Trigger the initial hide() shortly after the activity has been
        // created, to briefly hint to the user that UI controls
        // are available.
        delayedHide(100);
    }

    /**
     * Touch listener to use for in-layout UI controls to delay hiding the
     * system UI. This is to prevent the jarring behavior of controls going away
     * while interacting with activity UI.
     */
    private final View.OnTouchListener mDelayHideTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            if (AUTO_HIDE) {
                delayedHide(AUTO_HIDE_DELAY_MILLIS);
            }
            return false;
        }
    };

    private void toggle() {
        if (mVisible) {
            hide();
        } else {
            show();
        }
    }

    private void hide() {
        // Hide UI first
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }
        mVisible = false;

        // Schedule a runnable to remove the status and navigation bar after a delay
        mHideHandler.removeCallbacks(mShowPart2Runnable);
        mHideHandler.postDelayed(mHidePart2Runnable, UI_ANIMATION_DELAY);
    }

    private final Runnable mHidePart2Runnable = new Runnable() {
        @SuppressLint("InlinedApi")
        @Override
        public void run() {
            // Delayed removal of status and navigation bar

            // Note that some of these constants are new as of API 16 (Jelly Bean)
            // and API 19 (KitKat). It is safe to use them, as they are inlined
            // at compile-time and do nothing on earlier devices.
            mContentView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE
                    | View.SYSTEM_UI_FLAG_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
        }
    };

    @SuppressLint("InlinedApi")
    private void show() {
        // Show the system bar
        mContentView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION);
        mVisible = true;

        // Schedule a runnable to display UI elements after a delay
        mHideHandler.removeCallbacks(mHidePart2Runnable);
        mHideHandler.postDelayed(mShowPart2Runnable, UI_ANIMATION_DELAY);
    }

    private final Runnable mShowPart2Runnable = new Runnable() {
        @Override
        public void run() {
            // Delayed display of UI elements
            ActionBar actionBar = getSupportActionBar();
            if (actionBar != null) {
                actionBar.show();
            }
        }
    };

    private final Handler mHideHandler = new Handler();
    private final Runnable mHideRunnable = new Runnable() {
        @Override
        public void run() {
            hide();
        }
    };

    /**
     * Schedules a call to hide() in [delay] milliseconds, canceling any
     * previously scheduled calls.
     */
    private void delayedHide(int delayMillis) {
        mHideHandler.removeCallbacks(mHideRunnable);
        mHideHandler.postDelayed(mHideRunnable, delayMillis);
    }

    @Override
    public void onClick(View view) {
        String cmd = "stop";
        switch (view.getId()) {
            case R.id.left:
                cmd = MOVE_LEFT;
                break;
            case R.id.right:
                cmd = MOVE_RIGHT;
                break;
            case R.id.move:
                cmd = MOVE_FORWARD;
                break;
            case R.id.stop:
                cmd = MOVE_STOP;
                break;
            case R.id.reverse:
                cmd = MOVE_BACKWARD;
                break;
        }
        sendCommand(cmd);
    }
}
