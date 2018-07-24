package com.sty.horizontal.barchart;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.sty.horizontal.barchart.view.HorizontalBarChart;

import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class FirstActivity extends AppCompatActivity {
    private LinearLayout llContainer;
    private HorizontalBarChart horizontalBarChart;
    private List<HorizontalBarChart.Data> dataList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_first);

        initView();
    }

    private void initView(){
        llContainer = findViewById(R.id.ll_container);

        horizontalBarChart = new HorizontalBarChart(this);
        dataList = createData();
        horizontalBarChart.setChartData(dataList);
        horizontalBarChart.setOnSelectedChangeListener(new HorizontalBarChart.OnSelectedChangeListener() {
            @Override
            public void onSelectedChange(HorizontalBarChart barChart, View selectedView, HorizontalBarChart.Data selectedData) {
                Toast.makeText(FirstActivity.this, selectedData.getTitle() + " is checked", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onUnselectedChange(HorizontalBarChart barChart, View unselectedView, HorizontalBarChart.Data unselectedData) {
                //Toast.makeText(FirstActivity.this, unselectedData.getTitle() + " is unchecked", Toast.LENGTH_SHORT).show();
            }
        });

        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
        horizontalBarChart.setLayoutParams(lp);
        llContainer.addView(horizontalBarChart, 0);
    }

    private List<HorizontalBarChart.Data> createData(){
        List<HorizontalBarChart.Data> dataList = new LinkedList<>();
        HorizontalBarChart.Data data;
        Map<Integer, Float> map;
        for(int i = 11; i < 20; i++){
            data = horizontalBarChart.new Data();
            data.setTitle("06-" + i);
            map = new LinkedHashMap<>();
            map.put(getResources().getColor(R.color.greenFont), i * 10F);
            data.setValueMap(map);
            dataList.add(data);
        }
        return dataList;
    }
}
