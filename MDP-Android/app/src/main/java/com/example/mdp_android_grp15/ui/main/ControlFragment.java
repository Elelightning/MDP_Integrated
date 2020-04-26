package com.example.mdp_android_grp15.ui.main;

import android.content.Context;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;

import com.example.mdp_android_grp15.MainActivity;
import com.example.mdp_android_grp15.R;

import static android.content.Context.SENSOR_SERVICE;

public class ControlFragment extends Fragment implements SensorEventListener {
    // Init
    private static final String ARG_SECTION_NUMBER = "section_number";
    private static final String TAG = "ControlFragment";
    private PageViewModel pageViewModel;

    // Declaration Variable
    // Shared Preferences
    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;

    // Control Button
    ImageButton moveForwardImageBtn, turnRightImageBtn, moveBackImageBtn, turnLeftImageBtn, exploreResetButton, fastestResetButton;
    private static long exploreTimer, fastestTimer;
    ToggleButton exploreButton, fastestButton;
    TextView exploreTimeTextView, fastestTimeTextView, robotStatusTextView;
    Switch phoneTiltSwitch;
    static Button calibrateButton;
    private static GridMap gridMap;

    private Sensor mSensor;
    private SensorManager mSensorManager;


    // Timer
    static Handler timerHandler = new Handler();

    Runnable timerRunnableExplore = new Runnable() {
        @Override
        public void run() {
            long millisExplore = System.currentTimeMillis() - exploreTimer;
            int secondsExplore = (int) (millisExplore / 1000);
            int minutesExplore = secondsExplore / 60;
            secondsExplore = secondsExplore % 60;

            exploreTimeTextView.setText(String.format("%02d:%02d", minutesExplore, secondsExplore));

            timerHandler.postDelayed(this, 500);
        }
    };

    Runnable timerRunnableFastest = new Runnable() {
        @Override
        public void run() {
            long millisFastest = System.currentTimeMillis() - fastestTimer;
            int secondsFastest = (int) (millisFastest / 1000);
            int minutesFastest = secondsFastest / 60;
            secondsFastest = secondsFastest % 60;

            fastestTimeTextView.setText(String.format("%02d:%02d", minutesFastest, secondsFastest));

            timerHandler.postDelayed(this, 500);
        }
    };

    // Fragment Constructor
    public static ControlFragment newInstance(int index) {
        ControlFragment fragment = new ControlFragment();
        Bundle bundle = new Bundle();
        bundle.putInt(ARG_SECTION_NUMBER, index);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        pageViewModel = ViewModelProviders.of(this).get(PageViewModel.class);
        int index = 1;
        if (getArguments() != null) {
            index = getArguments().getInt(ARG_SECTION_NUMBER);
        }
        pageViewModel.setIndex(index);
    }

    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        // inflate
        View root = inflater.inflate(R.layout.activity_control, container, false);

        // get shared preferences
        sharedPreferences = getActivity().getSharedPreferences("Shared Preferences", Context.MODE_PRIVATE);


        // variable initialization
        moveForwardImageBtn = root.findViewById(R.id.forwardImageBtn);
        turnRightImageBtn = root.findViewById(R.id.rightImageBtn);
        moveBackImageBtn = root.findViewById(R.id.backImageBtn);
        turnLeftImageBtn = root.findViewById(R.id.leftImageBtn);
        exploreTimeTextView = root.findViewById(R.id.exploreTimeTextView);
        fastestTimeTextView = root.findViewById(R.id.fastestTimeTextView);
        exploreButton = root.findViewById(R.id.exploreToggleBtn);
        fastestButton = root.findViewById(R.id.fastestToggleBtn);
        exploreResetButton = root.findViewById(R.id.exploreResetImageBtn);
        fastestResetButton = root.findViewById(R.id.fastestResetImageBtn);
        phoneTiltSwitch = root.findViewById(R.id.phoneTiltSwitch);
        calibrateButton = root.findViewById(R.id.calibrateButton);

        robotStatusTextView = MainActivity.getRobotStatusTextView();
        fastestTimer = 0;
        exploreTimer = 0;

        mSensorManager = (SensorManager) getActivity().getSystemService(SENSOR_SERVICE);
        mSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

        gridMap = MainActivity.getGridMap();

        // Button Listener
        moveForwardImageBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showLog("Clicked moveForwardImageBtn");
                if (gridMap.getAutoUpdate())
                    updateStatus("Please press 'MANUAL'");
                else if (gridMap.getCanDrawRobot() && !gridMap.getAutoUpdate()) {
                    gridMap.moveRobot("forward");
                    MainActivity.refreshLabel();
                    if (gridMap.getValidPosition())
                        updateStatus("moving forward");
                    else
                        updateStatus("Unable to move forward");
                    MainActivity.printMessage("W1|");
                }
                else
                    updateStatus("Please press 'STARTING POINT'");
                showLog("Exiting moveForwardImageBtn");
            }
        });

        turnRightImageBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showLog("Clicked turnRightImageBtn");
                if (gridMap.getAutoUpdate())
                    updateStatus("Please press 'MANUAL'");
                else if (gridMap.getCanDrawRobot() && !gridMap.getAutoUpdate()) {
                    gridMap.moveRobot("right");
                    MainActivity.refreshLabel();
                    MainActivity.printMessage("D|");
                }
                else
                    updateStatus("Please press 'STARTING POINT'");
                showLog("Exiting turnRightImageBtn");
            }
        });

        moveBackImageBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showLog("Clicked moveBackwardImageBtn");
                if (gridMap.getAutoUpdate())
                    updateStatus("Please press 'MANUAL'");
                else if (gridMap.getCanDrawRobot() && !gridMap.getAutoUpdate()) {
                    gridMap.moveRobot("back");
                    MainActivity.refreshLabel();
                    if (gridMap.getValidPosition())
                        updateStatus("moving backward");
                    else
                        updateStatus("Unable to move backward");
                    MainActivity.printMessage("S1|");
                }
                else
                    updateStatus("Please press 'STARTING POINT'");
                showLog("Exiting moveBackwardImageBtn");
            }
        });

        turnLeftImageBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showLog("Clicked turnLeftImageBtn");
                if (gridMap.getAutoUpdate())
                    updateStatus("Please press 'MANUAL'");
                else if (gridMap.getCanDrawRobot() && !gridMap.getAutoUpdate()) {
                    gridMap.moveRobot("left");
                    MainActivity.refreshLabel();
                    updateStatus("turning left");
                    MainActivity.printMessage("A|");
                }
                else
                    updateStatus("Please press 'STARTING POINT'");
                showLog("Exiting turnLeftImageBtn");
            }
        });

        exploreButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showLog("Clicked exploreToggleBtn");
                ToggleButton exploreToggleBtn = (ToggleButton) v;
                if (exploreToggleBtn.getText().equals("EXPLORE")) {
                    showToast("Exploration timer stop!");
                    robotStatusTextView.setText("Exploration Stopped");
                    timerHandler.removeCallbacks(timerRunnableExplore);
                }
                else if (exploreToggleBtn.getText().equals("STOP")) {
                    showToast("Exploration timer start!");
                    MainActivity.printMessage("ES|");
                    robotStatusTextView.setText("Exploration Started");
                    exploreTimer = System.currentTimeMillis();
                    timerHandler.postDelayed(timerRunnableExplore, 0);
                }
                else {
                    showToast("Else statement: " + exploreToggleBtn.getText());
                }
                showLog("Exiting exploreToggleBtn");
            }
        });

        fastestButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showLog("Clicked fastestToggleBtn");
                ToggleButton fastestToggleBtn = (ToggleButton) v;
                if (fastestToggleBtn.getText().equals("FASTEST")) {
                    showToast("Fastest timer stop!");
                    robotStatusTextView.setText("Fastest Path Stopped");
                    timerHandler.removeCallbacks(timerRunnableFastest);
                }
                else if (fastestToggleBtn.getText().equals("STOP")) {
                    showToast("Fastest timer start!");
                    MainActivity.printMessage("FS|");
                    robotStatusTextView.setText("Fastest Path Started");
                    fastestTimer = System.currentTimeMillis();
                    timerHandler.postDelayed(timerRunnableFastest, 0);
                }
                else
                    showToast(fastestToggleBtn.getText().toString());
                showLog("Exiting fastestToggleBtn");            }
        });

        exploreResetButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showLog("Clicked exploreResetImageBtn");
                showToast("Reseting exploration time...");
                exploreTimeTextView.setText("00:00");
                robotStatusTextView.setText("Not Available");
                if(exploreButton.isChecked())
                    exploreButton.toggle();
                timerHandler.removeCallbacks(timerRunnableExplore);
                showLog("Exiting exploreResetImageBtn");            }
        });

        fastestResetButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showLog("Clicked fastestResetImageBtn");
                showToast("Reseting fastest time...");
                fastestTimeTextView.setText("00:00");
                robotStatusTextView.setText("Not Available");
                if (fastestButton.isChecked())
                    fastestButton.toggle();
                timerHandler.removeCallbacks(timerRunnableFastest);
                showLog("Exiting fastestResetImageBtn");            }
        });

        phoneTiltSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                if (gridMap.getAutoUpdate()) {
                    updateStatus("Please press 'MANUAL'");
                    phoneTiltSwitch.setChecked(false);
                }
                else if (gridMap.getCanDrawRobot() && !gridMap.getAutoUpdate()) {
                    if(phoneTiltSwitch.isChecked()){
                        showToast("Tilt motion control: ON");
                        phoneTiltSwitch.setPressed(true);

                        mSensorManager.registerListener(ControlFragment.this, mSensor, mSensorManager.SENSOR_DELAY_NORMAL);
                        sensorHandler.post(sensorDelay);
                    }else{
                        showToast("Tilt motion control: OFF");
                        showLog("unregistering Sensor Listener");
                        try {
                            mSensorManager.unregisterListener(ControlFragment.this);
                        }catch(IllegalArgumentException e) {
                            e.printStackTrace();
                        }
                        sensorHandler.removeCallbacks(sensorDelay);
                    }
                } else {
                    updateStatus("Please press 'STARTING POINT'");
                    phoneTiltSwitch.setChecked(false);
                }
                if(phoneTiltSwitch.isChecked()){
                    compoundButton.setText("TILT ON");
                }else
                {
                    compoundButton.setText("TILT OFF");
                }
            }
        });

        calibrateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showLog("Clicked Calibrate Button");
                MainActivity.printMessage("SS|");
                MapTabFragment.manualUpdateRequest = true;
                showLog("Exiting Calibrate Button");
            }
        });

        return root;
    }

    private static void showLog(String message) {
        Log.d(TAG, message);
    }

    private void showToast(String message) {
        Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
    }

    Handler sensorHandler = new Handler();
    boolean sensorFlag= false;

    private final Runnable sensorDelay = new Runnable() {
        @Override
        public void run() {
            sensorFlag = true;
            sensorHandler.postDelayed(this,1000);
        }
    };

    @Override
    public void onSensorChanged(SensorEvent event) {
        float x = event.values[0];
        float y = event.values[1];
        float z = event.values[2];
        showLog("SensorChanged X: "+x);
        showLog("SensorChanged Y: "+y);
        showLog("SensorChanged Z: "+z);

        if(sensorFlag) {
            if (y < -2) {
                showLog("Sensor Move Forward Detected");
                gridMap.moveRobot("forward");
                MainActivity.refreshLabel();
                MainActivity.printMessage("W1|");
            } else if (y > 2) {
                showLog("Sensor Move Backward Detected");
                gridMap.moveRobot("back");
                MainActivity.refreshLabel();
                MainActivity.printMessage("S1|");
            } else if (x > 2) {
                showLog("Sensor Move Left Detected");
                gridMap.moveRobot("left");
                MainActivity.refreshLabel();
                MainActivity.printMessage("A|");
            } else if (x < -2) {
                showLog("Sensor Move Right Detected");
                gridMap.moveRobot("right");
                MainActivity.refreshLabel();
                MainActivity.printMessage("D|");
            }
        }
        sensorFlag = false;
    }
    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
        try{
            mSensorManager.unregisterListener(ControlFragment.this);
        } catch(IllegalArgumentException e){
            e.printStackTrace();
        }
    }

    private void updateStatus(String message) {
        Toast toast = Toast.makeText(getContext(), message, Toast.LENGTH_SHORT);
        toast.setGravity(Gravity.TOP,0, 0);
        toast.show();
    }

    public static Button getCalibrateButton() {
        return calibrateButton;
    }
}
