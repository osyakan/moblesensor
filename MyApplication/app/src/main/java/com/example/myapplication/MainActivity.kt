package com.example.myapplication

import android.content.Context
import android.os.Bundle
import com.google.android.material.snackbar.Snackbar
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import android.view.Menu
import android.view.MenuItem
import com.example.myapplication.databinding.ActivityMainBinding

import android.hardware.Sensor
import android.hardware.SensorManager
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.os.Build
import android.widget.TextView

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import android.graphics.Color;
import java.text.SimpleDateFormat


class MainActivity : AppCompatActivity(), SensorEventListener{

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityMainBinding
    private lateinit var sensorManager: SensorManager
    private lateinit var textInfo: TextView
    private lateinit var textView: TextView
    private lateinit var mChart: LineChart
    private val labels: Array<String> = arrayOf("accX","accY","accZ","Pressure")
    private val colors = arrayOf(Color.BLUE,Color.RED,Color.GREEN,Color.CYAN)
    private var sensors: Array<Float> = arrayOf(0f,0f,0f,0f)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // get SensorManager
        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        textInfo = findViewById(R.id.text_info)
        textView = findViewById(R.id.text_view)

        // get LineChart
        mChart = findViewById(R.id.chart)
        mChart.data = LineData()
        mChart.description.isEnabled = true
        mChart.axisRight.isEnabled = false

    }

    override fun onResume() {
        super.onResume()
        // Register Listener
        //読み込みたいセンサをここで登録する
        // スマホに搭載されていないセンサの値は読まない（エラーにはならない）
        // 参照URL: https://developer.android.com/guide/topics/sensors?hl=ja
        sensorManager.registerListener(this,
            sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),// accelerometer
            SensorManager.SENSOR_DELAY_NORMAL)
        sensorManager.registerListener(this,
            sensorManager.getDefaultSensor(Sensor.TYPE_PRESSURE), // barometric pressure
            SensorManager.SENSOR_DELAY_NORMAL)

        //initialize chart
        var linedatasets = mutableListOf<ILineDataSet>()
        for (i in 0..3){
            var linedataset: LineDataSet = LineDataSet(null,labels[i])
            linedataset.color = colors[i]
            linedataset.lineWidth = 2.0f
            linedataset.setDrawCircles(false)
            linedatasets.add(linedataset)
        }
        mChart.data = LineData(linedatasets)
    }

    override fun onPause() {
        super.onPause()
        // Free Listener
        sensorManager.unregisterListener(this)
    }

    public override fun onSensorChanged(event: SensorEvent?){
        // accelerometers' value are changed
        if (event?.sensor != null){
            if (event.sensor.type == Sensor.TYPE_ACCELEROMETER){ // 圧力センサの処理
                // get sensor values
                for (i in 0..2){sensors[i] = event.values[i]}
            }else if(event.sensor.type == Sensor.TYPE_PRESSURE){ // 気圧センサの処理
                sensors[3] == event.values[0]
            }

            // show value string
            var str : String = "センサの値\n"
            for (i in 0..3){str+="${labels[i]}: ${sensors[i]}\n"}
            textView.setText(str)

            // figure out
            var data: LineData = mChart.data
//            textView.append("${data.dataSetCount}")
            if (data!=null){ //  && data.dataSetCount>0
                for (i in 0..3){ // in each sensor value
                    var iLineDataSet: ILineDataSet = data.getDataSetByIndex(i)
                    data.addEntry(Entry(iLineDataSet.entryCount.toFloat(), sensors[i]),i)
                    data.notifyDataChanged()// it must be called when data is updated
                }

                mChart.notifyDataSetChanged()// it must be called when data is updated
                mChart.setVisibleXRangeMaximum(50F)// point width for x-axis in the window
                mChart.moveViewToX(data.entryCount.toFloat()-50F)// set auto scroll to latest value

            }

        }
    }

    public override fun onAccuracyChanged(p0: Sensor?, p1: Int) {

    }
}