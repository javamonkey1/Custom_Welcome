package com.chang.view;

import com.chang.custom_welcome.R;

import android.content.Context;
import android.os.AsyncTask;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

public class Custom_SlidingSwitcherView extends RelativeLayout implements OnTouchListener{

	public static final int SNAP_VELOCITY = 200;   //让菜单滑动需要达到的速度  (突然的  速度)
	private int switcherViewWidth;    //SlidingSwitcherView  的宽度
	private int currentItemIndex = 0;   //当前显示的元素的下标
	private int itemsCount;          //菜单中包含的元素的总数
	private int[] borders;         //各个元素的偏移的边界值
	private int leftEdge = 0;      //最多可以滑动的左边缘。值由菜单中包含的元素总数决定， marginLeft到达此值时，不能再减少  (边缘,刀刃)
	private int rightEdge = 0;     //最多可以滑动的右边缘，值恒为0，marginLeft 到达此值后，不能再增加
	private float xDown;      //记录手指按下的横坐标
	private float xMove;      //记录手指移动的横坐标
	private float xUp;        //记录手指抬起的横坐标
	private LinearLayout itemsLayout;     //菜单布局
	private LinearLayout dotsLayout;       //标签布局
	private LinearLayout nextStemp;         //下一步按钮
	private View firstItem;           //菜单中的第一个元素
	private MarginLayoutParams firstItemparams;   //菜单中第一个元素的布局，用于改变LeftMargin的值，来决定当前显示哪一个元素
	private VelocityTracker mVelocityTracker;    //用于计算手指滑动的速度
	private static boolean isOntouch = false;    
	private String TAG = "Custom_SlidingSwitcherView";
	

	//重写SlidingSwitcherView的构造函数，用于允许在XML文件中引用当前的自定义布局
	public Custom_SlidingSwitcherView(Context context, AttributeSet attrs) {
		super(context, attrs);
		// TODO Auto-generated constructor stub
	}

	
	//当这个视图应该给每个孩子分配一个大小和位置时调用,每次手指滑动都会调用
	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) {
		// TODO Auto-generated method stub
		super.onLayout(changed, l, t, r, b);
		if(changed){
			initializeItems();          //初始化菜单元素
			initializeDots();			//初始化标签元素
		}
	}

	//初始化菜单元素，为每一个元素添加监听事件，并且改变所有子元素的宽度，让他们等于父元素的宽度
	private void initializeItems(){
		switcherViewWidth = getWidth();     //从XML文件中得到此布局的宽度，即为fill_parent
		itemsLayout = (LinearLayout)getChildAt(0);
		itemsCount = itemsLayout.getChildCount();     //此处为4个Button控件
		borders = new int[itemsCount];
		for(int i = 0; i < itemsCount;i++){
			borders[i] = -i * switcherViewWidth;
			View item = itemsLayout.getChildAt(i);    //得到第一个Button
			MarginLayoutParams params = (MarginLayoutParams)item.getLayoutParams();
			params.width = switcherViewWidth;
			item.setLayoutParams(params);
			item.setOnTouchListener(this);
		}
		leftEdge = borders[itemsCount - 1];      //最多可以滑动的左边缘（三个width）
		firstItem = itemsLayout.getChildAt(0);
		firstItemparams = (MarginLayoutParams)firstItem.getLayoutParams();    //第一个items的参数
	}
	
	//初始化标签元素      (也就是每张图片上的那个点)
	private void initializeDots(){
		dotsLayout = (LinearLayout)getChildAt(1);
		refreshDotsLayout();
		refreshNextstempLayout();
	}
	
	//更新下一步的布局
	private void refreshNextstempLayout(){
		nextStemp = (LinearLayout)getChildAt(2);
		nextStemp.setBackgroundResource(R.drawable.nextstemp);
		nextStemp.setVisibility(View.INVISIBLE);
	}
	
	//更新圆点布局
	private void refreshDotsLayout(){
		dotsLayout.removeAllViews();    //调用此方法从viewGroup中删除所有子视图
		for(int i = 0;i < itemsCount;i++){
			LinearLayout.LayoutParams linearParams = new LinearLayout.LayoutParams(0, 10);    //宽和高
			linearParams.weight = 1;
			RelativeLayout relativeLayout = new RelativeLayout(this.getContext());
			ImageView image = new ImageView(this.getContext());
			if(i == currentItemIndex){
				image.setBackgroundResource(R.drawable.dot_selected);
			}else{
				image.setBackgroundResource(R.drawable.dot_unselected);
			}
			RelativeLayout.LayoutParams relativeParams = new RelativeLayout.LayoutParams((int)5d, (int)5d);
			relativeParams.addRule(RelativeLayout.CENTER_HORIZONTAL);
			relativeLayout.addView(image, relativeParams);
			dotsLayout.addView(relativeLayout, linearParams);
		}
	}
	
	/*  创建VelocityTracker对象，并将触摸事件加入到VelocityTracker当中
	 *  @param     右侧布局监听控件的滑动事件
	 * */
	private void creatVelocityTracker(MotionEvent event){
		if(mVelocityTracker == null){
			mVelocityTracker = VelocityTracker.obtain();     //获得一个速度追踪器对象
		}
		mVelocityTracker.addMovement(event);
	}
	
	/*	获取手指在右侧布局的监听View上的滑动速度。
	 * @return       滑动速度，以每秒钟移动了多少像素值为单位。
	 * */
	private int getScrollVelocity(){
		mVelocityTracker.computeCurrentVelocity(1000);    //设置想要的读取速度的单位    1000 就是 像素每秒
		int velocity = (int)mVelocityTracker.getXVelocity();
		return Math.abs(velocity);
	}
	
	/*	回收VelocityTracker对象。*/
	private void recycleVelocityTracker(){
		mVelocityTracker.recycle();
		mVelocityTracker = null;
	}
	
	/*
     * 滚动到下一个元素。 
     */  
    public void scrollToNext() {  
        new ScrollTask().execute(-60);  
    }  
  
    /*
     * 滚动到上一个元素。 
     */  
    public void scrollToPrevious() {  
        new ScrollTask().execute(60);  
    } 
	
	@Override
	public boolean onTouch(View v, MotionEvent event) {
		// TODO Auto-generated method stub
		//创建速度跟踪器
		creatVelocityTracker(event);
		switch(event.getAction()){
		case MotionEvent.ACTION_DOWN:
			isOntouch = true;
			//手指按下时，记录按下的横坐标
			xDown = event.getRawX();
			break;
		case MotionEvent.ACTION_MOVE:
			//手指移动时，对比按下的横坐标，计算出移动的距离，来调整左侧布局的leftmargin 值，从而显示或隐藏左侧布局
			xMove = event.getRawX();
			int distanceX = (int)(xMove - xDown) - (currentItemIndex * switcherViewWidth);    //第一元素的距离
			firstItemparams.leftMargin = distanceX;
			if(beAbleToScroll()){
				firstItem.setLayoutParams(firstItemparams);     //当手指滑动时图片滑动的原因
			}
			break;
		case MotionEvent.ACTION_UP:
			// 手指抬起时，进行判断当前手势的意图，从而决定是滚动到左侧布局，还是滚动到右侧布局
			isOntouch = false;
			xUp = event.getRawX();
			if(beAbleToScroll()){
				if(wantScrollToPrevious()){
					if(shouldScrollPrevious()){
						currentItemIndex--;
						scrollToPrevious();
						refreshDotsLayout();
						if(currentItemIndex == 3)
							nextStemp.setVisibility(View.VISIBLE);
						else
							nextStemp.setVisibility(View.INVISIBLE);
					}else{
						scrollToNext();
					}
				}else if(wantScrollToNext()){
					if(shouldScrollNext()){
						currentItemIndex++;
						scrollToNext();
						refreshDotsLayout();
						if(currentItemIndex == 3)
							nextStemp.setVisibility(View.VISIBLE);
						else
							nextStemp.setVisibility(View.INVISIBLE);
					}else{
						scrollToPrevious();
					}
				}
			}
			recycleVelocityTracker();
			break;
		}
		return false;
	}
	
	/*	判断当前手势的意图是不是想滚动到上一个菜单元素。如果手指移动的距离是正数，则认为当前手势是想要滚动到上一个菜单元素。
	 * @return       当前手势想滚动到上一个菜单元素返回true，否则返回false
	 * */ 
	private boolean wantScrollToPrevious(){
		return xUp - xDown > 0;
	}
	
	/*	 判断当前手势的意图是不是想滚动到下一个菜单元素。如果手指移动的距离是负数，则认为当前手势是想要滚动到下一个菜单元素
	 * 
	 * @return      当前手势想滚动到下一个菜单元素返回true，否则返回false*/
	private boolean wantScrollToNext(){
		return xUp - xDown < 0;
	}
	
	/*	判断是否应该滚动到上一个菜单元素。如果手指移动距离大于屏幕的1/2，或者手指移动速度大于SNAP_VELOCITY
	 *  就认为应该滚动到上一个菜单
	 *  
	 *  @return      如果应该滚动到上一个菜单元素返回true，否则返回false*/
	private boolean shouldScrollPrevious(){
		return xUp - xDown > switcherViewWidth / 2 || getScrollVelocity() > SNAP_VELOCITY;
	}
	
	/*	判断是否应该滚动到下一个菜单元素。如果手指移动距离大于屏幕的1/2，或者手指移动速度大于SNAP_VELOCITY
	 *  就认为应该滚动到下一个菜单
	 *  
	 *  @return       如果应该滚动到下一个菜单元素返回true，否则返回false*/
	private boolean shouldScrollNext(){
		return xDown - xUp > switcherViewWidth / 2 || getScrollVelocity() > SNAP_VELOCITY;
	}
	
	
	/*  当前是否能够滚动，滚动到第一个或最后一个元素时将不能再滚动。
	 * 
	 * @return      当前leftMargin的值在leftEdge和rightEdge之间返回true,否则返回false
	 * */
	private boolean beAbleToScroll(){
		return firstItemparams.leftMargin < rightEdge && firstItemparams.leftMargin > leftEdge;
	}
	
	
	/*  检测菜单滚动时，是否有穿越border 
	 *  @param leftMargin    第一个元素的左偏移值
	 *  @param speed        滚动的速度，整数说明向右滚动，负数说明向左滚动
	 *  @return             穿越任何一个border了返回true，否则返回false */
	private boolean isCrossBorder(int leftMargin,int speed){
		for(int border : borders){
			if(speed > 0){
				if(leftMargin >= border && leftMargin - speed < border){
					return true;
				}
			}else{
				if(leftMargin <= border && leftMargin - speed > border){
					return true;
				}
			}
		}
		return false;
	}
	
	/*  找到距离当前的  leftMargin 的最近的一个border值   
	 * @param leftMargin    第一个元素的左偏移值
	 * @return              距离当前的leftMargin最近的一个border值*/
	private int findClosesBorder(int leftMargin) {
		int absLeftMargin = Math.abs(leftMargin);
		int closestBorder = borders[0];
		int closestMargin = Math.abs(Math.abs(closestBorder) - absLeftMargin);
		for (int border : borders) {
			int margin = Math.abs(Math.abs(border) - absLeftMargin);
			if (margin < closestMargin) {
				closestBorder = border;
				closestMargin = margin;
			}
		}
		return closestBorder;
	}
	
	/*
	 当一个异步工作类开始执行时，一般有四个步骤：
	 1：onPreExecute()  -->  在任务执行前，在用户界面上调用线程。这一步通常用于设置任务，例如通过在用户界面显示一个进度条
	 2: doInBackground(Integer... params)   -->   这个方法是中的所有打吗都会在子线程中进行，我们应该在这里去处理所有耗时的操作。任务一旦完成就
	 											可以通过return语句将任务的执行结果返回，如果   AsyncTask 的第三个泛型参数指定是Void，就可以不
	 											返回任务的执行结果，注意，在这个方法中是不可以有UI操作的，如果需要更新UI元素，比如反馈当前任务的
	 											执行进度，可以调用 publishProgress（Progress）方法来完成。	
	 3: onProgressUpdate(Progress...)    -->    当在后台中调用了 publishProgress（Progress） 方法后，这个方法就会很快被调用，方法中携带的参数
	 											就是在后台任务中传递过来的，在这个方法中可以对UI进行操作，利用参数中的数值就可以对界面元素进行
	 											相应的更新。
	 4: onPostExecute(Result)   -->   当后台任务执行完毕并通过 return 语句进行返回时，这个方法就很快会被调用。返回的数据作为参数传递到此方法中，
	 								可以利用返回的数据来进行一些UI操作，比如说提醒任务执行的结果，以及关掉进度条对话框中个。
	  */
	/*异步工作类     <Params, Progress, Result> 分别对应下边是三个方法中的三个参数
	 * Params ：在执行AsyncTask 时需要传入的参数，可用于在后台任务中使用。
	 * progress : 后台任务执行时，如果需要在界面上显示当前的进度，则使用这里指定的泛型作为进度单位。
	 * Result :   当任务完毕后，如果需要对结果进行返回，则使用这里指定的泛型作为返回值类型。*/
	class ScrollTask extends AsyncTask<Integer, Integer, Integer>{
		
		@Override
		protected void onPreExecute() {
			// TODO Auto-generated method stub
			super.onPreExecute();
		}

		@Override
		protected Integer doInBackground(Integer... speed) {   //可变参数，意思就是参数可以有多个，当需要传入多个参数的时候可以用此方法
			// TODO Auto-generated method stub
			
			int leftMargin = firstItemparams.leftMargin;    //得到第一个页面元素的左边界的值
			
			// 根据传入的速度来滚动界面 ，当滚动穿越 border 时，跳出循环
			while(true){
				leftMargin = leftMargin + speed[0];
				if(isCrossBorder(leftMargin, speed[0])){
					leftMargin = findClosesBorder(leftMargin);    //得到距离最近的border值
					break;
				}
				publishProgress(leftMargin);         //发布一个或多个单位的进展
				//为了要有滚动效果的产生，每次循环时使线程睡眠 10 毫秒，这样肉眼才能看的到滚动的动画
				sleep(40);
			}
			return leftMargin;
		}
		
		@Override
		protected void onProgressUpdate(Integer... leftMargin) {
			// TODO Auto-generated method stub
			firstItemparams.leftMargin = leftMargin[0];
			firstItem.setLayoutParams(firstItemparams);
		}
		
		@Override
		protected void onPostExecute(Integer leftMargin) {
			// TODO Auto-generated method stub
			firstItemparams.leftMargin = leftMargin;
			firstItem.setLayoutParams(firstItemparams);
		}
	
	}
	//指定当前线程睡眠多久，以毫秒为单位
	private void sleep(long millis){
		try {
			Thread.sleep(millis);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
