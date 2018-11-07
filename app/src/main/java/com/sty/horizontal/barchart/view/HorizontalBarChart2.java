package com.sty.horizontal.barchart.view;

import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.util.Log;
import android.util.SparseArray;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.sty.horizontal.barchart.R;
import com.sty.horizontal.barchart.SecondActivity;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 采用ListView+view填充的方式实现
 * Created by tian on 2018/7/23.
 */

public class HorizontalBarChart2 extends RelativeLayout {
    private int lineColor = Color.parseColor("#d81d5a");
    private int selectedColor = Color.parseColor("#dddddd");
    private int unSelectedColor = Color.parseColor("#00888888");

    private Context mContext;
    private ListView mListView;
    private List<Data> dataList;
    private ListAdapter adapter;
    private int lineHeight = 1;
    private float maxValue = -1;
    private float width; //当前视图的宽度
    private int height; //当前视图的高度

    private boolean isCompleteShow = false;
    private boolean isHeaderChanged = false;
    private View headerView;
    private View footerView;
    private int listItemHeight;

    private HorizontalBarChart2 mHorizontalBarChart2;

    private OnSelectedChangeListener onSelectedChangeListener;

    public HorizontalBarChart2(Context context) {
        super(context);
        init(context);
    }

    public HorizontalBarChart2(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public HorizontalBarChart2(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context){
        this.mContext = context;
        this.mHorizontalBarChart2 = this;
        //添加布局监听器，以获得view的真实高度
        getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                if(isCompleteShow){
                    if(!isHeaderChanged) {
                        isHeaderChanged = true;

                        //因为布局监听器可能会被多次调用，所以需要及时注销
                        getViewTreeObserver().removeOnGlobalLayoutListener(this);
                        height = getHeight();
                        int fillViewHeight = height / 2;

                        //重新设置header的高度--让上方的填充线填满上半屏
                        View fillDataView = headerView.findViewById(R.id.fill_top);
                        LinearLayout.LayoutParams paramsDataView = (LinearLayout.LayoutParams) fillDataView.getLayoutParams();
                        paramsDataView.height = fillViewHeight;
                        AbsListView.LayoutParams paramsItemView = (AbsListView.LayoutParams) headerView.getLayoutParams();
                        paramsItemView.height = fillViewHeight;
                        fillDataView.setLayoutParams(paramsDataView);
                        headerView.setLayoutParams(paramsItemView);

                        //重新设置footer的高度--//让下方的填充线填满下半屏
                        View fillDataView2 = footerView.findViewById(R.id.fill_top);
                        LinearLayout.LayoutParams paramsDataView2 = (LinearLayout.LayoutParams) fillDataView2.getLayoutParams();
                        paramsDataView2.height = fillViewHeight;
                        fillDataView2.setLayoutParams(paramsDataView2);

                        //测量普通子View的高度
                        mListView.setOnScrollListener(new MyOnScrollListener());
                        View listItem = adapter.getView(1, null, mListView);  //获取第一个孩子的高度，因为第0个为header
                        listItem.measure(0, 0);
                        listItemHeight = listItem.getMeasuredHeight();

                        //初始化第一个item
                        dataList.get(0).setSelected(true);
                        adapter.notifyDataSetChanged();
                    }
                    return;
                }
                width = getWidth();  //获得本View的宽度
                if(dataList != null && dataList.size() > 0){
                    show();
                }
            }
        });


    }

    private void show(){
        removeAllViews();

        //红色的中间横线
        View vLine = new View(getContext());
        LayoutParams lineParams = new RelativeLayout.LayoutParams(LayoutParams.MATCH_PARENT, dip2pix(lineHeight));
        lineParams.addRule(CENTER_VERTICAL);
        vLine.setLayoutParams(lineParams);
        vLine.setBackgroundColor(lineColor);

        //ListView的初始化
        mListView = new ListView(mContext);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
        mListView.setLayoutParams(params);
        mListView.setDividerHeight(0);
        headerView = getHeaderFillView();
        footerView = getFooterFillView();
        mListView.addHeaderView(headerView);
        mListView.addFooterView(footerView);

        adapter = new ListAdapter();
        mListView.setAdapter(adapter);

        addView(mListView);
        addView(vLine);

        isCompleteShow = true;
    }

    private class MyOnScrollListener implements AbsListView.OnScrollListener {
        private int prePosition = 0;
        private int curPosition = 0;
        private View firstView;
        private View preView;
        private SparseArray recordSp = new SparseArray(0);
        private int mCurrentFirstVisibleItem = 0;
        private int scrollY;
        boolean isSelected = false;

        @Override
        public void onScrollStateChanged(AbsListView view, int scrollState) {
            if(scrollState == SCROLL_STATE_IDLE){ //停止滑动
                View firstView = view.getChildAt(0);  //处理初始状态
                firstView.getTop();
                if(firstView.getTop() == 0){
                    dataList.get(0).setSelected(true);
                    if (!isSelected) {
                        isSelected = true;
                        adapter.notifyDataSetChanged();
                        if(onSelectedChangeListener != null){
                            onSelectedChangeListener.onSelectedChange(mHorizontalBarChart2, firstView, dataList.get(0));
                        }
                    }
                }

            }
        }

        @Override
        public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
            mCurrentFirstVisibleItem = firstVisibleItem;
            firstView = view.getChildAt(0); //获得第一个可见的子view（包括header）（非整个ListView所有数据的第一个）
            if(firstView != null){
                ItemRecord itemRecord = (ItemRecord) recordSp.get(mCurrentFirstVisibleItem);
                if(itemRecord == null){
                    itemRecord = new ItemRecord();
                }
                itemRecord.height = firstView.getHeight();
                itemRecord.top = firstView.getTop();   //第一个可见子View的Top距离ListView的Top的距离（通常<=0）
                recordSp.append(mCurrentFirstVisibleItem, itemRecord);
            }

//            int firstViewTop = firstView.getTop();
//            Log.i("tian", "firstVisibleItem:(" + firstVisibleItem + ") itemHeight:(" + firstView.getHeight() + ") firstViewTop:(" + firstViewTop + ") scrollTotalY:(" + getScrollY() + ")" );

            if(listItemHeight > 0) {
                scrollY = getScrollY();

                prePosition = curPosition;
                preView = firstView;
                curPosition = scrollY / listItemHeight;
                if(curPosition > dataList.size() -1){
                    curPosition = dataList.size() -1;
                }
                Log.i("tian", "totalItemCount:" + totalItemCount);
                for (int i = 0; i < dataList.size(); i++) {
                    if (i == curPosition) {
                        dataList.get(i).setSelected(true);
                    } else {
                        dataList.get(i).setSelected(false);
                    }
                }
                if (!isSelected) {
                    isSelected = true;
                    adapter.notifyDataSetChanged();
                    if(onSelectedChangeListener != null){
                        onSelectedChangeListener.onSelectedChange(mHorizontalBarChart2, firstView, dataList.get(curPosition));
                    }
                } else if (prePosition != curPosition) {
                    adapter.notifyDataSetChanged();
                    if(onSelectedChangeListener != null){
                        onSelectedChangeListener.onSelectedChange(mHorizontalBarChart2, firstView, dataList.get(curPosition));
                        onSelectedChangeListener.onUnselectedChange(mHorizontalBarChart2, preView, dataList.get(prePosition));
                    }
                }
            }

        }

        private int getScrollY(){ //计算整个ListView的滚动高度
            int height = 0;
            for(int i = 0; i < mCurrentFirstVisibleItem; i++){
                ItemRecord itemRecord = (ItemRecord) recordSp.get(i);
                height += itemRecord.height;
            }
            ItemRecord curRecord = (ItemRecord) recordSp.get(mCurrentFirstVisibleItem);
            if(curRecord == null){
                curRecord = new ItemRecord();
            }
            height -= curRecord.top;
            return  height;
        }

        class ItemRecord{
            int height = 0;
            int top = 0;
        }
    }

    public interface OnSelectedChangeListener{
        /**
         * 当选中时执行
         * @param barChart
         * @param selectedView 选中的view
         * @param selectedData 选中的数据
         */
        void onSelectedChange(HorizontalBarChart2 barChart, View selectedView, Data selectedData);

        /**
         * 当取消选中时执行
         * @param barChart
         * @param unselectedView 取消选中的view
         * @param unselectedData 取消选中的数据
         */
        void onUnselectedChange(HorizontalBarChart2 barChart, View unselectedView, Data unselectedData);
    }

    public void addData(Data data){
        if(dataList == null){
            dataList = new LinkedList<>();
        }
        dataList.add(data);
        if(adapter == null){
            adapter = new ListAdapter();
        }else{
            maxValue = calculateMaxValue();
        }
        adapter.notifyDataSetChanged();
    }

    public void removeData(Data data){
        dataList.remove(data);
        maxValue = calculateMaxValue();
        adapter.notifyDataSetChanged();
    }

    private class ListAdapter extends BaseAdapter{
        public ListAdapter(){
            maxValue = calculateMaxValue();
        }

        @Override
        public int getCount() {
            return dataList == null ? 0 : dataList.size();
        }

        @Override
        public Data getItem(int position) {
            return dataList == null ? null : dataList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View view;
            MyViewHolder holder;
            if(convertView == null){
                view = View.inflate(mContext, R.layout.item_horizontal_barchart, null);
                holder = new MyViewHolder();
                holder.llFillTop = view.findViewById(R.id.fill_top);
                holder.tvTitle = view.findViewById(R.id.tv_title);
                holder.llLayoutData = view.findViewById(R.id.layout_data);

                view.setTag(holder);
            }else{
                view = convertView;
                holder = (MyViewHolder) view.getTag();
            }

            Data data = dataList.get(position);

            holder.tvTitle.setText(data.getTitle());
            if(maxValue > 0){
                float totalWidth = width - dip2pix(104);
                Set<Integer> keySet = data.getValueMap().keySet();

                holder.llLayoutData.removeAllViews();
                for(Integer color : keySet){
                    float value = data.getValueMap().get(color);
                    View viewLine = new View(mContext);
                    int lineWidth = (int) ((value / maxValue) * totalWidth);
                    LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(lineWidth, dip2pix(10));
                    params.topMargin = dip2pix(5);
                    viewLine.setLayoutParams(params);
                    viewLine.setBackgroundColor(color);

                    holder.llLayoutData.addView(viewLine);
                }
            }
            if(data.isSelected()){
                view.setBackgroundColor(selectedColor);
            }else{
                view.setBackgroundColor(unSelectedColor);
            }

            return view;
        }
    }

    private class MyViewHolder{
        LinearLayout llFillTop;
        TextView tvTitle;
        LinearLayout llLayoutData;
    }

    /**
     * 计算出最大值
     * @return
     */
    private float calculateMaxValue(){
        float maxValue = -1;
        for(Data data : dataList){
            Collection<Float> values = data.getValueMap().values();
            for(Float value : values){
                if(value > maxValue){
                    maxValue = value;
                }
            }
        }
        return maxValue;
    }

    private View getHeaderFillView(){
        LinearLayout view = (LinearLayout) View.inflate(getContext(), R.layout.item_horizontal_barchart, null);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 0);
        view.setOrientation(LinearLayout.VERTICAL);
        view.findViewById(R.id.fill_top).setLayoutParams(params);
        return view;
    }

    private View getFooterFillView(){
        LinearLayout view = (LinearLayout) View.inflate(getContext(), R.layout.item_footer_horizontal_barchart, null);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 0);
        view.setOrientation(LinearLayout.VERTICAL);
        view.findViewById(R.id.fill_top).setLayoutParams(params);
        return view;
    }

    private int dip2pix(int dip){
        float scale = mContext.getResources().getDisplayMetrics().density;
        return (int) (dip * scale + 0.5f);
    }

    public int getLineHeight() {
        return lineHeight;
    }

    public void setLineHeight(int lineHeight) {
        this.lineHeight = lineHeight;
    }

    public List<Data> getDataList() {
        return dataList;
    }

    public void setDataList(List<Data> dataList) {
        this.dataList = dataList;
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

        private boolean isSelected = false;

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

        public boolean isSelected() {
            return isSelected;
        }

        public void setSelected(boolean selected) {
            isSelected = selected;
        }
    }

}
