package com.example.cognitiontesting;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.chaquo.python.PyObject;
import com.chaquo.python.Python;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;

import java.util.ArrayList;
import java.util.List;

public class HomeFragment extends Fragment {

    private static final String TAG = "HomeFragment";
    private TextView textViewOutput;
    private LineChart lineChart;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView: Initializing HomeFragment");

        // Inflate the fragment layout
        View root = inflater.inflate(R.layout.fragment_home, container, false);

        // Initialize UI elements
        textViewOutput = root.findViewById(R.id.text_home);
        lineChart = root.findViewById(R.id.line_chart);

        // Set up the LineChart
        setupChart();

        // Set listener for the Fetch Signals button
        root.findViewById(R.id.button_fetch_signals).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "Fetch Signals button clicked.");
                fetchBrainSignals();
            }
        });

        // Initialize Chaquopy (Python environment)
        if (!Python.isStarted()) {
            Log.d(TAG, "Initializing Chaquopy.");
            Python.start(new com.chaquo.python.android.AndroidPlatform(requireContext()));
        }

        return root;
    }

    private void setupChart() {
        // Chart configuration
        Log.d(TAG, "Setting up LineChart.");
        lineChart.getDescription().setText("Brain Signal Chart");
        lineChart.getDescription().setTextSize(12f);
        lineChart.setNoDataText("Press the button to fetch signals.");
        lineChart.setNoDataTextColor(getResources().getColor(android.R.color.black));
        lineChart.setTouchEnabled(true);  // Ensure touch interaction
        lineChart.setPinchZoom(true);    // Enable pinch zoom
    }

    private void fetchBrainSignals() {
        Log.d(TAG, "Fetching brain signals from Python.");
        Python python = Python.getInstance();

        try {
            // Call the Python function to get brain signals
            PyObject pyObject = python.getModule("eeg_analyzer").callAttr("get_brain_signal");

            // Log raw Python output
            Log.d(TAG, "Raw Python output: " + pyObject.toString());

            // Convert Python list to Java list
            List<PyObject> signalList = pyObject.asList();

            // Debugging: Log the fetched signal list
            Log.d("TAG", "Fetched signal list: " + signalList.toString());

            // Log the size and contents of the signal list
            Log.d(TAG, "Signal list size: " + signalList.size());
            if (signalList.isEmpty()) {
                Log.e(TAG, "No signals received from Python function.");
                lineChart.clear();
                lineChart.setNoDataText("No signals available.");
                lineChart.invalidate();
                return;
            }

            for (PyObject signal : signalList) {
                Log.d(TAG, "Signal: " + signal.toString());
            }

            // Display the signals in the TextView for debugging
            textViewOutput.setText(signalList.toString());

            // Update the chart with the fetched signals
            updateChart(signalList);

        } catch (Exception e) {
            // Log any errors during data retrieval or conversion
            Log.e(TAG, "Error fetching signals: " + e.getMessage(), e);
            lineChart.clear();
            lineChart.setNoDataText("Error fetching data.");
            lineChart.invalidate();
        }
    }

    private void updateChart(List<PyObject> signals) {
        Log.d(TAG, "Updating chart with signals.");
        ArrayList<Entry> entries = new ArrayList<>();

        // Convert signals to chart entries
        for (int i = 0; i < signals.size(); i++) {
            try {
                float value = Float.parseFloat(signals.get(i).toString());
                entries.add(new Entry(i, value));
                Log.d(TAG, "Added entry: X = " + i + ", Y = " + value);
            } catch (NumberFormatException e) {
                Log.e(TAG, "Invalid signal value: " + signals.get(i), e);
            }
        }

        // Log chart entries for debugging
        Log.d(TAG, "Entries size: " + entries.size());
        if (entries.isEmpty()) {
            Log.e(TAG, "No valid entries to display on the chart.");
            lineChart.clear();
            lineChart.setNoDataText("No valid data to display.");
            lineChart.invalidate();
            return;
        }

        // Create a dataset and apply basic styling
        LineDataSet dataSet = new LineDataSet(entries, "EEG Signals");
        dataSet.setLineWidth(2.5f);
        dataSet.setCircleRadius(4.5f);
        dataSet.setColor(getResources().getColor(android.R.color.holo_blue_dark));
        dataSet.setCircleColor(getResources().getColor(android.R.color.holo_blue_light));

        // Create LineData and set it to the chart
        LineData lineData = new LineData(dataSet);
        lineChart.setData(lineData);
        lineChart.invalidate(); // Refresh chart
        Log.d(TAG, "Chart updated successfully.");
    }
}





