package com.example.agriculturalguide;

import android.Manifest;
import android.content.Context;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.crowdfire.cfalertdialog.CFAlertDialog;
import com.google.firebase.FirebaseApp;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.klinker.android.send_message.Message;
import com.klinker.android.send_message.Settings;
import com.klinker.android.send_message.Transaction;

import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

import de.nitri.gauge.Gauge;
import me.itangqi.waveloadingview.WaveLoadingView;

public class SensorStatusActivity extends AppCompatActivity implements SensorEventListener {


    final private int REQUEST_CODE_ASK_PERMISSIONS = 123;

    //Water Level
    private WaveLoadingView waveLoadingView; //wave
    private Button btnIncrWater, btnDecrWater; //wave
    private int cur_water;

    //Temperature
    private TextView txtTemperature;
    private SensorManager sensorManager;
    private Thermometer thermometer;
    private float temperature;
    private Timer timer;

    //Gauge_Pressure
    private Button btnIncrPressure, btnDecrPressure;
    private Gauge gauge_Pressure ;
    private float curPressure;
    private TextView txtPressure;

    //Gauge_Wind Speed

    private Gauge gaugeWindSpeed ;
    private float curWindSpeed;
    private TextView txtWindSpeed;

    //Gauge_Wind Direction

    private Gauge gaugeWindDirection ;
    private float curWindDirection;
    private TextView txtWindDirection;

    //Gauge_Humidity
    private CustomGauge gauge_Humidity;
    private TextView txtHumidity;
    private Button btnIncrHumidity, btnDecrHumidity;
    private int cur_Humidity;

    //Gauge_Rain
    private Gauge gauge_Rain;
    private Button btnIncrRain, btnDecrRain;
    private int cur_Rain;
    private TextView txtRain;



    //Gauge_Soil_pH
    private CustomGauge gauge_Soil_pH;
    private TextView txtSoilpH;
    private Button btnIncrSoil_pH, btnDecrSoil_pH;
    private int cur_Soil_pH;

    //Gauge_SoilMoisture
    private CustomGauge gauge_SoilMoisture;
    private TextView txtSoilMoisture;
    private int cur_SoilMoisture;

    //Gauge_SoilMoisture2
    private CustomGauge gauge_SoilMoistureTwo;
    private TextView txtSoilMoistureTwo;
    private int cur_SoilMoistureTwo;


    /// Spill Gate
    Switch swtSpillGate;
    int gateStatus=0;

    /// pumpOne and pumpTwo

    Button btnPumpOne,btnPumpTwo;


    DatabaseReference reference;



    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        FirebaseApp.initializeApp(this);



        // Get a reference to our temperature

        getTempFromFirebase();
        getHumFromFirebase();
        getSoilMoistFromFirebase();
        getSoilMoistTwoFromFirebase();
        getpHFromFirebase();

        /// pumpOne Two

        btnPumpOne = (Button) findViewById(R.id.bt1);
        btnPumpTwo = (Button) findViewById(R.id.bt2);
        getPumpStatusFromFirebase();


        /// Switch

        txtPressure=(TextView) findViewById(R.id.txtPressure);
        swtSpillGate = (Switch) findViewById(R.id.swtSpillGate);

        swtSpillGate.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                setSpillON();
            }
        });


        //Water Level
        waveLoadingView =(WaveLoadingView) findViewById(R.id.waveLoadingView);
        waveLoadingView.setProgressValue(0);
        getWaterLevelFromFirebase();




        //Temperature
        txtTemperature =(TextView) findViewById(R.id.txtTemperature);
        thermometer = (Thermometer) findViewById(R.id.thermometer);
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);


        //Gauge-Pressure


        gauge_Pressure =(Gauge) findViewById(R.id.gaugePressure);
        getPressFromFirebase();

        /// Gauge-wind speed

        gaugeWindSpeed = (Gauge) findViewById(R.id.gauge_wind_speed);
        txtWindSpeed = (TextView) findViewById(R.id.txt_wind_speed);
        getWindSpeedFromFirebase();
        /// Gauge-wind direction

        gaugeWindDirection = (Gauge) findViewById(R.id.gauge_wind_direction);
        txtWindSpeed = (TextView) findViewById(R.id.txt_wind_speed);
        getWindDirectionFromFirebase();



        //Gauge_Humidity
        gauge_Humidity =(CustomGauge) findViewById(R.id.gaugeHumdity);
        txtHumidity =(TextView) findViewById(R.id.txtHumidity);

        gauge_Humidity.setEndValue(100);


        //Gauge-Rain
        txtRain=(TextView) findViewById(R.id.txtRain);
        gauge_Rain =(Gauge) findViewById(R.id.gaugeRain);

        //cur_Rain = 50;
        getRainFromFirebase();




        //Gauge_WaterFlow

        gauge_Humidity.setEndValue(100);


        //Gauge_Soil_pH
        gauge_Soil_pH =(CustomGauge) findViewById(R.id.gaugeSoil_pH);
        txtSoilpH =(TextView) findViewById(R.id.txtSoilpH);
        gauge_Soil_pH.setEndValue(10);
        txtSoilpH.setText(gauge_Soil_pH.getValue()+"");




        //Gauge_SoilMoisture
        gauge_SoilMoisture =(CustomGauge) findViewById(R.id.gaugeSoilMoisture);
        txtSoilMoisture =(TextView) findViewById(R.id.txtSoilMoisture);
        gauge_SoilMoisture.setEndValue(300);
        cur_SoilMoisture = 50;
        gauge_SoilMoisture.setValue(cur_SoilMoisture);
        txtSoilMoisture.setText(gauge_Soil_pH.getValue()+"");



        //Gauge_SoilMoisture2
        gauge_SoilMoistureTwo =(CustomGauge) findViewById(R.id.gauge_Soil_Moisture_Two);
        txtSoilMoistureTwo =(TextView) findViewById(R.id.txt_Soil_Moisture_Two);
        gauge_SoilMoistureTwo.setEndValue(300);
        cur_SoilMoistureTwo = 50;
        gauge_SoilMoistureTwo.setValue(cur_SoilMoistureTwo);
        txtSoilMoistureTwo.setText(gauge_SoilMoistureTwo.getValue()+"");
    }

    private void getRainFromFirebase() {
        reference= FirebaseDatabase.getInstance().getReference().child("rainLevel");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String rain = dataSnapshot.getValue().toString();

                Float flt = Float.parseFloat(rain);
                cur_Rain = Math.round(flt);

                if(cur_Rain==25) cur_Rain=10;
                if(cur_Rain==50) cur_Rain=20;
                if(cur_Rain==75) cur_Rain=30;
                if(cur_Rain==100) cur_Rain=40;

                gauge_Rain.setValue(cur_Rain);
                txtRain.setText(cur_Rain+" mm");
                //Toast.makeText(SensorStatusActivity.this,""+curPressure,Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void getPumpStatusFromFirebase() {
        reference= FirebaseDatabase.getInstance().getReference().child("pumponestat");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String s1 = dataSnapshot.getValue().toString();
                int a = Integer.parseInt(s1);

                if(a==1){
                    btnPumpOne.setBackgroundColor(Color.GREEN);
                }else btnPumpOne.setBackgroundColor(Color.BLACK);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        reference= FirebaseDatabase.getInstance().getReference().child("pumptwostat");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String s1 = dataSnapshot.getValue().toString();
                int a = Integer.parseInt(s1);

                if(a==1){
                    btnPumpTwo.setBackgroundColor(Color.GREEN);
                }else btnPumpTwo.setBackgroundColor(Color.BLACK);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    private void getWindDirectionFromFirebase() {
            /*will write this part later*/
    }

    private void getWindSpeedFromFirebase() {
        reference= FirebaseDatabase.getInstance().getReference().child("windspeed");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String speed = dataSnapshot.getValue().toString();
                curWindSpeed = Float.parseFloat(speed);

                gaugeWindSpeed.setValue(curWindSpeed);
                txtWindSpeed.setText(String.valueOf(curWindSpeed)+" km/h");
                //Toast.makeText(SensorStatusActivity.this,""+curPressure,Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void getSoilMoistTwoFromFirebase() {
        reference= FirebaseDatabase.getInstance().getReference().child("SoilMoisttwo");

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String moist = dataSnapshot.getValue().toString();
                cur_SoilMoistureTwo =Integer.parseInt(moist);
                if(cur_SoilMoistureTwo==0) {
                    gauge_SoilMoistureTwo.setValue(33*2);
                    txtSoilMoistureTwo.setText("33.65%");
                }

                else   {
                    gauge_SoilMoistureTwo.setValue(99*2);
                    txtSoilMoistureTwo.setText("95.97%");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }


    private void setSpillON() {
        reference= FirebaseDatabase.getInstance().getReference().child("SpillGate");
        DatabaseReference mDatabase;
        mDatabase = FirebaseDatabase.getInstance().getReference();
        if(swtSpillGate.isChecked()) mDatabase.child("SpillGate").setValue(1);
        else if(!swtSpillGate.isChecked()) mDatabase.child("SpillGate").setValue(0);
    }



    private void getWaterLevelFromFirebase() {

        reference= FirebaseDatabase.getInstance().getReference().child("WaterLevel");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String waterLvl = dataSnapshot.getValue().toString();
                cur_water = Integer.parseInt(waterLvl);
                update_Water(cur_water);

                //txtPressure.setText(String.valueOf(curPressure)+" pa");
                //Toast.makeText(SensorStatusActivity.this,""+curPressure,Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void getPressFromFirebase() {

        reference= FirebaseDatabase.getInstance().getReference().child("Presasure");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String press = dataSnapshot.getValue().toString();
                curPressure = Float.parseFloat(press);

                gauge_Pressure.setValue(curPressure);
                txtPressure.setText(String.valueOf(curPressure)+" pa");
                //Toast.makeText(SensorStatusActivity.this,""+curPressure,Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void getHumFromFirebase() {
        reference= FirebaseDatabase.getInstance().getReference().child("Humidity");

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String hum = dataSnapshot.getValue().toString();
                cur_Humidity = Integer.parseInt(hum);
                gauge_Humidity.setValue(cur_Humidity);
                txtHumidity.setText(gauge_Humidity.getValue()+" %");
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void getTempFromFirebase() {

        reference= FirebaseDatabase.getInstance().getReference().child("Temperature");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String temp = dataSnapshot.getValue().toString();
                temperature = Float.parseFloat(temp);

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void getSoilMoistFromFirebase() {
        reference= FirebaseDatabase.getInstance().getReference().child("SoilMoist");

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String moist = dataSnapshot.getValue().toString();
                cur_SoilMoisture =Integer.parseInt(moist);
                if(cur_SoilMoisture==0) {
                    gauge_SoilMoisture.setValue(33*2);
                    txtSoilMoisture.setText("33.59 %");
                }

                else  {
                    gauge_SoilMoisture.setValue(98*3);
                    txtSoilMoisture.setText("94.99 %");
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void getpHFromFirebase() {
        reference= FirebaseDatabase.getInstance().getReference().child("pH");

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String pH = dataSnapshot.getValue().toString();
                float parseFloat = Float.parseFloat(pH);
                cur_Soil_pH = Math.round(parseFloat);
                gauge_Soil_pH.setValue(cur_Soil_pH);
                txtSoilpH.setText(gauge_Soil_pH.getValue()+"");
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    //Temperature
    @Override
    protected void onResume() {
        super.onResume();
        //loadAmbientTemperature();
        simulateAmbientTemperature();
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterAll();
    }
    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        if (sensorEvent.values.length > 0) {
            //temperature = sensorEvent.values[0];
            thermometer.setCurrentTemp(temperature);
            //getSupportActionBar().setTitle(getString(R.string.app_name) + " : " + temperature);
            txtTemperature.setText(temperature+ " \u2109");
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }


    //Temperature
    private void simulateAmbientTemperature() {
        timer = new Timer();

        timer.scheduleAtFixedRate(new TimerTask() {

            @Override
            public void run() {
                //temperature = Utils.randInt(-10, 40);

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        thermometer.setCurrentTemp(temperature);
                        //getSupportActionBar().setTitle(getString(R.string.app_name) + " : " + temperature);
                        txtTemperature.setText(temperature+"°C");
                    }
                });
            }
        }, 0, 3500);
    }

    private void loadAmbientTemperature() {
        Sensor sensor = sensorManager.getDefaultSensor(Sensor.TYPE_AMBIENT_TEMPERATURE);

        if (sensor != null) {
            sensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_FASTEST);
        } else {
            Toast.makeText(this, "No Ambient Temperature Sensor !", Toast.LENGTH_LONG).show();
        }
    }

    private void unregisterAll() {
        //sensorManager.unregisterListener(this);
        timer.cancel();
    }

    //Wave
    private void update_Water(int cur_water) {

        //Toast.makeText(SensorStatusActivity.this, progress+"", Toast.LENGTH_SHORT).show();
        waveLoadingView.setProgressValue(cur_water);
        if(cur_water < 50)
        {
            waveLoadingView.setBottomTitle("");
            waveLoadingView.setCenterTitle(String.format("%d%%", cur_water));
            waveLoadingView.setTopTitle("");
            waveLoadingView.setWaterLevelRatio(cur_water/10);
        }
        else if(cur_water < 80)
        {
            waveLoadingView.setBottomTitle("");
            waveLoadingView.setCenterTitle(String.format("%d%%", cur_water));
            waveLoadingView.setTopTitle("");
            waveLoadingView.setWaterLevelRatio(cur_water/10);
            //waveLoadingView.setBottomTitleColor(Color.RED);
        }
        else
        {
            waveLoadingView.setBottomTitle("ALERT!!");
            waveLoadingView.setBottomTitleColor(Color.RED);
            waveLoadingView.setBottomTitleStrokeColor(Color.BLACK);
            waveLoadingView.setCenterTitle(String.format("%d%%", cur_water));
            waveLoadingView.setTopTitle("");
            waveLoadingView.setWaterLevelRatio(cur_water/10);

            /*CFAlertDialog.Builder builder = new CFAlertDialog.Builder(SensorStatusActivity.this)
                    .setDialogStyle(CFAlertDialog.CFAlertStyle.NOTIFICATION)
                    .setTitle("Water Level Is Raising !! ")
                    .setMessage("Looks like water level is increasing continuously. Please take precautions As Soon As Possible ")
                    .addButton("Notified", -1, -1, CFAlertDialog.CFAlertActionStyle.POSITIVE, CFAlertDialog.CFAlertActionAlignment.END, (dialog, which) -> {
                        Toast.makeText(SensorStatusActivity.this,"Thank You",Toast.LENGTH_SHORT).show();
                        dialog.dismiss();
                    });

// Show the alert
            builder.show();

            ActivityCompat.requestPermissions(SensorStatusActivity.this,new String[]{Manifest.permission.SEND_SMS},1);
            try {
                SmsManager smsManager = SmsManager.getDefault();
                smsManager.sendTextMessage("01931131690", null, "Water Level is Raising, Please Take Necessary Steps", null, null);
                Toast.makeText(SensorStatusActivity.this, "Check Water Level!",
                        Toast.LENGTH_LONG).show();
            } catch (Exception e) {
                Toast.makeText(SensorStatusActivity.this,
                        "SMS faild, please try again later!",
                        Toast.LENGTH_LONG).show();
                e.printStackTrace();
            }*/
        }
    }

    /*private int getWaterLv() {
        Random rand = new Random();
        int p = rand.nextInt(100)+1;
        return p;
    }*/

    //Humidity
    @Override
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
    }



}
