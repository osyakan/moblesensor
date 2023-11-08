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
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import java.text.SimpleDateFormat


class MainActivity : AppCompatActivity(), SensorEventListener{

    private lateinit var sensorManager: SensorManager
    private lateinit var mChart: LineChart
    private lateinit var sensorSpinner: Spinner
    private var sensorMap = mutableMapOf<String, Sensor>()
    private val colors = arrayOf(Color.BLUE,Color.RED,Color.GREEN,Color.CYAN)
    private var sensors: Array<Float> = arrayOf(0f,0f,0f,0f)
    private lateinit var selectedSensor: Sensor
    val maxEntries = 50f // max sensor keep size
    val samplingHz = 120 // サンプリング周波数
    val sensorInterval = (1000000/samplingHz).toInt()
    private val labels: Array<String> = arrayOf("X","Y","Z","W")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // get SensorManager
        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        // set default sensor type
        selectedSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        // get Spinner
        sensorSpinner = findViewById(R.id.sensorlist)
        // get LineChart
        mChart = findViewById(R.id.chart)
        mChart.data = LineData()
        mChart.description.isEnabled = true
        mChart.axisRight.isEnabled = false

        // set available sensors
        val sensorList = sensorManager.getSensorList(Sensor.TYPE_ALL)
        for (sensor in sensorList){
            sensorMap[sensor.name] = sensor
        }
        Log.d("sensors","sensor values: ${sensorMap.values}")

        // set Spinner
        val sensorNameList = ArrayList(sensorMap.keys)
        val adapter = ArrayAdapter(this,android.R.layout.simple_spinner_item,sensorNameList)
        sensorSpinner.adapter = adapter
        // set event
        sensorSpinner.onItemSelectedListener = object: AdapterView.OnItemSelectedListener{
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val selectedSensorName = sensorNameList[position]
                var NewSelectedSensor = sensorMap[selectedSensorName]

                if(NewSelectedSensor==null){ //set acc if selected sensor is none
                    NewSelectedSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
                }
                if (selectedSensor!=NewSelectedSensor && NewSelectedSensor!=null){
                    sensorManager.unregisterListener(this@MainActivity) // unregister old sensor
                    selectedSensor = NewSelectedSensor
                    // set new sensor
                    sensorManager.registerListener(this@MainActivity,selectedSensor,sensorInterval)
                }
                initializeChart()
            }

            override fun onNothingSelected(p0: AdapterView<*>?) {

            }
        }

    }

    override fun onResume() {
        super.onResume()
        // Re-register Listener
        sensorManager.registerListener(this, selectedSensor, SensorManager.SENSOR_DELAY_NORMAL)

        initializeChart()// initialize chart

    }

    //initialize chart
    private fun initializeChart(){
        var linedatasets = mutableListOf<ILineDataSet>()
        for (i in 0..3){
            var linedataset: LineDataSet = LineDataSet(null,labels[i])
            linedataset.color = colors[i]
            linedataset.lineWidth = 2.0f
            linedataset.setDrawCircles(false)
            linedatasets.add(linedataset)
        }
        mChart.data = LineData(linedatasets)
        mChart.invalidate()
    }

    override fun onPause() {
        super.onPause()
        initializeChart()
        // Free Listener
        sensorManager.unregisterListener(this)

    }

    public override fun onSensorChanged(event: SensorEvent){
        if (event.sensor!=selectedSensor)
            return
        val sensors = event.values

        // figure out
        var data: LineData = mChart.data
        var initFlag = false
        if (data!=null){
            for (i in 0 until sensors.size){ // in each sensor value
                var iLineDataSet: ILineDataSet = data.getDataSetByIndex(i)

                // keep sensor value length for prevent memory leak
//                Log.d("memory","${iLineDataSet.entryCount}:${iLineDataSet}")
                if (iLineDataSet.entryCount>=maxEntries*50){
                    initFlag = true
                }

                data.addEntry(Entry(iLineDataSet.entryCount.toFloat(), sensors[i]),i)
                data.notifyDataChanged()// it must be called when data is updated
            }

            // fit y-axis range
            val maxis = mChart.axisLeft
            maxis.axisMaximum = data.getYMax() + 1f
            maxis.axisMinimum = data.getYMin() - 1f


            mChart.notifyDataSetChanged()// it must be called when data is updated
            mChart.setVisibleXRangeMaximum(maxEntries)// point width for x-axis in the window
            mChart.moveViewToX(data.entryCount.toFloat()-maxEntries)// set auto scroll to latest value


            // prevent memory leak
            if (initFlag){
                initializeChart()
            }
        }



    }

    public override fun onAccuracyChanged(p0: Sensor?, p1: Int) {

    }
}