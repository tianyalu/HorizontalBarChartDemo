package com.sty.horizontal.barchart.view;

import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.sty.horizontal.barchart.R;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 完全用view填充的方式实现
 * Created by tian on 2018/7/19.
 */

public class HorizontalBarChart extends RelativeLayout {
    private int lineColor = Color.parseColor("#d81d5a");
    private int selectedColor = Color.parseColor("#dddddd");
    private int unSelectedColor = Color.parseColor("#00888888");

    private float lineHeight = 1;
    private ScrollView scrollView;
    private int lineLocation = 0;
    private List<Data> chartData;
    private float maxValue = 0;
    private int width = 0;
    private int height;
    private View headView;
    private View footView;

    private boolean isCompleteShow = false;
    private List<ScrollLocation> scrollLocationList = new LinkedList<>();
    private int mSelectedScrollLocation = -1;
    private OnSelectedChangeListener onSelectedChangeListener;

    private int scrollY = 0;

    public HorizontalBarChart(Context context) {
        super(context);
        init();
    }

    public HorizontalBarChart(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public HorizontalBarChart(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init(){
        //添加布局监听器，以获得view的真实高度
        getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                if(isCompleteShow){
                    //因为布局监听器可能会被多次调用，所以需要及时注销
                    getViewTreeObserver().removeOnGlobalLayoutListener(this);
                    height = getHeight();
                    lineLocation = height / 2;

                    //让上方的填充线填满上半屏
                    View footFillView = footView.findViewById(R.id.fill_top);
                    LinearLayout.LayoutParams footFillLp = (LinearLayout.LayoutParams)footFillView.getLayoutParams();
                    footFillLp.height = lineLocation;
                    LinearLayout.LayoutParams footLp = (LinearLayout.LayoutParams) footView.getLayoutParams();
                    footLp.height = lineLocation;
                    footFillView.setLayoutParams(footFillLp);
                    footView.setLayoutParams(footLp);

                    //让下方的填充线填满下半屏
                    View headFillView = headView.findViewById(R.id.fill_top);
                    LinearLayout.LayoutParams headFillLp = (LinearLayout.LayoutParams)headFillView.getLayoutParams();
                    headFillLp.height = lineLocation;
                    LinearLayout.LayoutParams headLp = (LinearLayout.LayoutParams) headView.getLayoutParams();
                    headLp.height = lineLocation;
                    headFillView.setLayoutParams(headFillLp);
                    headView.setLayoutParams(headLp);

                    onScroll(scrollY);
                    return;
                }
                width = getWidth();
                if(chartData != null){
                    show();
                }
            }
        });
    }

    private void show(){
        removeAllViews();

        //红色的中间横线
        View vLine = new View(getContext());
        LayoutParams lineParams = new LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, dip2px(lineHeight));
        lineParams.addRule(CENTER_VERTICAL);
        vLine.setLayoutParams(lineParams);
        vLine.setBackgroundColor(lineColor);

        scrollView = new ScrollView(getContext()){
            @Override
            protected void onScrollChanged(int l, int t, int oldl, int oldt) {
                super.onScrollChanged(l, t, oldl, oldt);
                onScroll(t);
            }
        };

        LayoutParams scrollParams = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
        scrollView.setLayoutParams(scrollParams);

        LinearLayout layout = new LinearLayout(getContext());
        layout.setOrientation(LinearLayout.VERTICAL);
        FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.WRAP_CONTENT);
        layout.setLayoutParams(layoutParams);

        //中间的数据部分
        if(chartData != null){
            maxValue = calculateMaxValue();
            headView = getFillView();
            layout.addView(headView);       // ------
                                            // |
            int index = 0;                  // | 
            for(Data data : chartData){
                index++;
                ScrollLocation scrollLocation = new ScrollLocation();
                View dataView = getDataView(data);
                layout.addView(dataView);

                scrollLocation.setData(data);
                scrollLocation.setIndex(index);
                scrollLocation.setSelectedView(dataView);
                scrollLocationList.add(scrollLocation);
            }
            
            footView = getFillView();
            layout.addView(footView);       // ------
        }                                   // |
                                            // |
        scrollView.addView(layout);
        
        addView(scrollView);
        addView(vLine);   //红色的中间横线
        isCompleteShow = true;
    }   

    /**
     * 滑动时执行，执行监听器，修改选中背景
     * @param y 滑动距离
     */
    private void onScroll(int y){
        scrollY = y;
        int height = 0;
        boolean isSelected = false;
        for(int i = 0; i < scrollLocationList.size(); i++ ){
            ScrollLocation scrollLocation = scrollLocationList.get(i);
//            Log.i("sty", "---totalheight: " + height + scrollLocation.getHeight() + "--y: " + y + "--scrollLocation.getHeight: " + scrollLocation.getHeight());

            //找到第一个高度大于或等于滑动距离的
            if(height + scrollLocation.getHeight() >= y && !isSelected){


                if(mSelectedScrollLocation == -1 || mSelectedScrollLocation != scrollLocation.getIndex()){
                    scrollLocation.getSelectedView().setBackgroundColor(selectedColor);

                    Log.i("sty", "-----mSelectedScrollLocation: " + mSelectedScrollLocation);  //上一个的索引
                    if(mSelectedScrollLocation != -1) {
                        ScrollLocation unscrollLoaction = scrollLocationList.get(mSelectedScrollLocation - 1);  //因为索引从1开始，所以这里-1
                        if (onSelectedChangeListener != null) {
                            onSelectedChangeListener.onUnselectedChange(this, unscrollLoaction.getSelectedView(), unscrollLoaction.getData());
                        }
                    }

                    if(onSelectedChangeListener != null){
                        onSelectedChangeListener.onSelectedChange(this, scrollLocation.getSelectedView(), scrollLocation.getData());
                    }
                    mSelectedScrollLocation = scrollLocation.getIndex();  //当前选中的索引
                    Log.i("sty", "+++++mSelectedScrollLocation: " + mSelectedScrollLocation);
                }
                isSelected = true;
            }else {
                scrollLocation.getSelectedView().setBackgroundColor(unSelectedColor);
            }
            height += scrollLocation.getHeight();
        }
    }

    /**
     * 计算出最大值
     * @return
     */
    private float calculateMaxValue(){
        float maxValue = -1;
        for(Data data : chartData){
            Collection<Float> values = data.getValueMap().values();
            for(Float value : values){
                if(maxValue <= value){
                    maxValue = value;
                }
            }
        }
        return maxValue;
    }

    /**
     * 下图左中间的部分
     * @param data
     * @return
     */
    private View getDataView(Data data){
        View view = View.inflate(getContext(), R.layout.item_horizontal_barchart, null);
        TextView tvTitle = view.findViewById(R.id.tv_title);
        tvTitle.setText(data.getTitle());
        LinearLayout dataLayout = (LinearLayout) view.findViewById(R.id.layout_data);

        Set<Integer> keySet = data.getValueMap().keySet();
        int totalWidth = this.width - dip2px(104);

        for(Integer color : keySet){
            float value = data.getValueMap().get(color);
            View viewLine = new View(getContext());
            int width = (int) (totalWidth * (value / maxValue));
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(width, dip2px(10));
            params.topMargin = dip2px(5);
            viewLine.setLayoutParams(params);
            viewLine.setBackgroundColor(color);
            dataLayout.addView(viewLine);
        }

        return view;
    }

    /**
     * ------        ------
     * |             |
     *        -->    |
     * |
     * @return
     */
    private View getFillView(){
        LinearLayout view = (LinearLayout) View.inflate(getContext(), R.layout.item_horizontal_barchart, null);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 0);
        view.setOrientation(LinearLayout.VERTICAL);
        view.findViewById(R.id.fill_top).setLayoutParams(params);
        return view;
    }

    private int dip2px(float dip){
        float scale = getContext().getResources().getDisplayMetrics().density;
        return (int) (dip * scale + 0.5f);
    }

    public void notifyDataSetChanged(){
        show();
    }

    public int getLineColor() {
        return lineColor;
    }

    /**
     * 设置中间线的颜色
     * @param lineColor
     */
    public void setLineColor(int lineColor) {
        this.lineColor = lineColor;
    }

    public int getSelectedColor() {
        return selectedColor;
    }

    /**
     * 设置选中的背景颜色
     * @param selectedColor
     */
    public void setSelectedColor(int selectedColor) {
        this.selectedColor = selectedColor;
    }

    public int getUnSelectedColor() {
        return unSelectedColor;
    }

    /**
     * 设置未选中的背景颜色
     * @param unSelectedColor
     */
    public void setUnSelectedColor(int unSelectedColor) {
        this.unSelectedColor = unSelectedColor;
    }

    public float getLineHeight() {
        return lineHeight;
    }

    /**
     * 设置中间线的高度 单位px
     * @param lineHeight
     */
    public void setLineHeight(float lineHeight) {
        this.lineHeight = lineHeight;
    }

    /**
     * 获得View的数据集合
     * @return
     */
    public List<Data> getChartData() {
        return chartData;
    }

    /**
     * 设置view的所有数据集合
     * @param chartData
     */
    public void setChartData(List<Data> chartData) {
        this.chartData = chartData;
    }

    public OnSelectedChangeListener getOnSelectedChangeListener() {
        return onSelectedChangeListener;
    }

    public void setOnSelectedChangeListener(OnSelectedChangeListener onSelectedChangeListener) {
        this.onSelectedChangeListener = onSelectedChangeListener;
    }

    /**
     * View所需要的数据
     */
    public class Data{
        //标题（时间）
        private CharSequence title;
        //数据，key是颜色代码，value是值
        private Map<Integer, Float> valueMap;
        //附带标记，拓展用
        private Object tag;

        public Data(){}

        public Data(CharSequence title){
            this.title = title;
        }

        public CharSequence getTitle() {
            return title;
        }

        public void setTitle(CharSequence title) {
            this.title = title;
        }

        public Map<Integer, Float> getValueMap() {
            return valueMap;
        }

        public void setValueMap(Map<Integer, Float> valueMap) {
            this.valueMap = valueMap;
        }

        public Object getTag() {
            return tag;
        }

        public void setTag(Object tag) {
            this.tag = tag;
        }
    }

    //保存滑动所需要的对象
    private class ScrollLocation{
        private View selectedView;
        private Data data;
        private int index = 1;

        public int getHeight(){
            return selectedView.getHeight();
        }

        public View getSelectedView() {
            return selectedView;
        }

        public void setSelectedView(View selectedView) {
            this.selectedView = selectedView;
        }

        public Data getData() {
            return data;
        }

        public void setData(Data data) {
            this.data = data;
        }

        public int getIndex() {
            return index;
        }

        public void setIndex(int index) {
            this.index = index;
        }
    }

    public interface OnSelectedChangeListener{
        /**
         * 当选中时执行
         * @param barChart
         * @param selectedView 选中的view
         * @param selectedData 选中的数据
         */
        void onSelectedChange(HorizontalBarChart barChart, View selectedView, Data selectedData);

        /**
         * 当取消选中时执行
         * @param barChart
         * @param unselectedView 取消选中的view
         * @param unselectedData 取消选中的数据
         */
        void onUnselectedChange(HorizontalBarChart barChart, View unselectedView, Data unselectedData);
    }
}
