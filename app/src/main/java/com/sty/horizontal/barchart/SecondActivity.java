package com.sty.horizontal.barchart;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.sty.horizontal.barchart.view.HorizontalBarChart2;

import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class SecondActivity extends AppCompatActivity {
    private LinearLayout llContainer;
    private HorizontalBarChart2 horizontalBarChart2;
    private List<HorizontalBarChart2.Data> dataList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_second);

        initView();
    }

    private void initView(){
        llContainer = findViewById(R.id.ll_container);

        horizontalBarChart2 = new HorizontalBarChart2(this);
        dataList = createData();
        horizontalBarChart2.setDataList(dataList);
        horizontalBarChart2.setOnSelectedChangeListener(new HorizontalBarChart2.OnSelectedChangeListener() {
            @Override
            public void onSelectedChange(HorizontalBarChart2 barChart, View selectedView, HorizontalBarChart2.Data selectedData) {
                Toast.makeText(SecondActivity.this, selectedData.getTitle() + " is checked", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onUnselectedChange(HorizontalBarChart2 barChart, View unselectedView, HorizontalBarChart2.Data unselectedData) {
                //Toast.makeText(SecondActivity.this, unselectedData.getTitle() + " is unchecked", Toast.LENGTH_SHORT).show();
            }
        });

        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
        horizontalBarChart2.setLayoutParams(lp);
        llContainer.addView(horizontalBarChart2, 0);
    }

    private List<HorizontalBarChart2.Data> createData(){
        List<HorizontalBarChart2.Data> dataList = new LinkedList<>();
        HorizontalBarChart2.Data data;
        Map<Integer, Float> map;
        for(int i = 11; i < 30; i++){
            data = horizontalBarChart2.new Data();
            data.setTitle("06-" + i);
            map = new LinkedHashMap<>();
            map.put(getResources().getColor(R.color.greenFont), i * 10F);
            data.setValueMap(map);
            dataList.add(data);
        }
        return dataList;
    }
}
