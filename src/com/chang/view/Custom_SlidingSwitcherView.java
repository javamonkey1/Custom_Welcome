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

	public static final int SNAP_VELOCITY = 200;   //�ò˵�������Ҫ�ﵽ���ٶ�  (ͻȻ��  �ٶ�)
	private int switcherViewWidth;    //SlidingSwitcherView  �Ŀ��
	private int currentItemIndex = 0;   //��ǰ��ʾ��Ԫ�ص��±�
	private int itemsCount;          //�˵��а�����Ԫ�ص�����
	private int[] borders;         //����Ԫ�ص�ƫ�Ƶı߽�ֵ
	private int leftEdge = 0;      //�����Ի��������Ե��ֵ�ɲ˵��а�����Ԫ������������ marginLeft�����ֵʱ�������ټ���  (��Ե,����)
	private int rightEdge = 0;     //�����Ի������ұ�Ե��ֵ��Ϊ0��marginLeft �����ֵ�󣬲���������
	private float xDown;      //��¼��ָ���µĺ�����
	private float xMove;      //��¼��ָ�ƶ��ĺ�����
	private float xUp;        //��¼��ָ̧��ĺ�����
	private LinearLayout itemsLayout;     //�˵�����
	private LinearLayout dotsLayout;       //��ǩ����
	private LinearLayout nextStemp;         //��һ����ť
	private View firstItem;           //�˵��еĵ�һ��Ԫ��
	private MarginLayoutParams firstItemparams;   //�˵��е�һ��Ԫ�صĲ��֣����ڸı�LeftMargin��ֵ����������ǰ��ʾ��һ��Ԫ��
	private VelocityTracker mVelocityTracker;    //���ڼ�����ָ�������ٶ�
	private static boolean isOntouch = false;    
	private String TAG = "Custom_SlidingSwitcherView";
	

	//��дSlidingSwitcherView�Ĺ��캯��������������XML�ļ������õ�ǰ���Զ��岼��
	public Custom_SlidingSwitcherView(Context context, AttributeSet attrs) {
		super(context, attrs);
		// TODO Auto-generated constructor stub
	}

	
	//�������ͼӦ�ø�ÿ�����ӷ���һ����С��λ��ʱ����,ÿ����ָ�����������
	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) {
		// TODO Auto-generated method stub
		super.onLayout(changed, l, t, r, b);
		if(changed){
			initializeItems();          //��ʼ���˵�Ԫ��
			initializeDots();			//��ʼ����ǩԪ��
		}
	}

	//��ʼ���˵�Ԫ�أ�Ϊÿһ��Ԫ����Ӽ����¼������Ҹı�������Ԫ�صĿ�ȣ������ǵ��ڸ�Ԫ�صĿ��
	private void initializeItems(){
		switcherViewWidth = getWidth();     //��XML�ļ��еõ��˲��ֵĿ�ȣ���Ϊfill_parent
		itemsLayout = (LinearLayout)getChildAt(0);
		itemsCount = itemsLayout.getChildCount();     //�˴�Ϊ4��Button�ؼ�
		borders = new int[itemsCount];
		for(int i = 0; i < itemsCount;i++){
			borders[i] = -i * switcherViewWidth;
			View item = itemsLayout.getChildAt(i);    //�õ���һ��Button
			MarginLayoutParams params = (MarginLayoutParams)item.getLayoutParams();
			params.width = switcherViewWidth;
			item.setLayoutParams(params);
			item.setOnTouchListener(this);
		}
		leftEdge = borders[itemsCount - 1];      //�����Ի��������Ե������width��
		firstItem = itemsLayout.getChildAt(0);
		firstItemparams = (MarginLayoutParams)firstItem.getLayoutParams();    //��һ��items�Ĳ���
	}
	
	//��ʼ����ǩԪ��      (Ҳ����ÿ��ͼƬ�ϵ��Ǹ���)
	private void initializeDots(){
		dotsLayout = (LinearLayout)getChildAt(1);
		refreshDotsLayout();
		refreshNextstempLayout();
	}
	
	//������һ���Ĳ���
	private void refreshNextstempLayout(){
		nextStemp = (LinearLayout)getChildAt(2);
		nextStemp.setBackgroundResource(R.drawable.nextstemp);
		nextStemp.setVisibility(View.INVISIBLE);
	}
	
	//����Բ�㲼��
	private void refreshDotsLayout(){
		dotsLayout.removeAllViews();    //���ô˷�����viewGroup��ɾ����������ͼ
		for(int i = 0;i < itemsCount;i++){
			LinearLayout.LayoutParams linearParams = new LinearLayout.LayoutParams(0, 10);    //��͸�
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
	
	/*  ����VelocityTracker���󣬲��������¼����뵽VelocityTracker����
	 *  @param     �Ҳ಼�ּ����ؼ��Ļ����¼�
	 * */
	private void creatVelocityTracker(MotionEvent event){
		if(mVelocityTracker == null){
			mVelocityTracker = VelocityTracker.obtain();     //���һ���ٶ�׷��������
		}
		mVelocityTracker.addMovement(event);
	}
	
	/*	��ȡ��ָ���Ҳ಼�ֵļ���View�ϵĻ����ٶȡ�
	 * @return       �����ٶȣ���ÿ�����ƶ��˶�������ֵΪ��λ��
	 * */
	private int getScrollVelocity(){
		mVelocityTracker.computeCurrentVelocity(1000);    //������Ҫ�Ķ�ȡ�ٶȵĵ�λ    1000 ���� ����ÿ��
		int velocity = (int)mVelocityTracker.getXVelocity();
		return Math.abs(velocity);
	}
	
	/*	����VelocityTracker����*/
	private void recycleVelocityTracker(){
		mVelocityTracker.recycle();
		mVelocityTracker = null;
	}
	
	/*
     * ��������һ��Ԫ�ء� 
     */  
    public void scrollToNext() {  
        new ScrollTask().execute(-60);  
    }  
  
    /*
     * ��������һ��Ԫ�ء� 
     */  
    public void scrollToPrevious() {  
        new ScrollTask().execute(60);  
    } 
	
	@Override
	public boolean onTouch(View v, MotionEvent event) {
		// TODO Auto-generated method stub
		//�����ٶȸ�����
		creatVelocityTracker(event);
		switch(event.getAction()){
		case MotionEvent.ACTION_DOWN:
			isOntouch = true;
			//��ָ����ʱ����¼���µĺ�����
			xDown = event.getRawX();
			break;
		case MotionEvent.ACTION_MOVE:
			//��ָ�ƶ�ʱ���ԱȰ��µĺ����꣬������ƶ��ľ��룬��������಼�ֵ�leftmargin ֵ���Ӷ���ʾ��������಼��
			xMove = event.getRawX();
			int distanceX = (int)(xMove - xDown) - (currentItemIndex * switcherViewWidth);    //��һԪ�صľ���
			firstItemparams.leftMargin = distanceX;
			if(beAbleToScroll()){
				firstItem.setLayoutParams(firstItemparams);     //����ָ����ʱͼƬ������ԭ��
			}
			break;
		case MotionEvent.ACTION_UP:
			// ��ָ̧��ʱ�������жϵ�ǰ���Ƶ���ͼ���Ӷ������ǹ�������಼�֣����ǹ������Ҳ಼��
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
	
	/*	�жϵ�ǰ���Ƶ���ͼ�ǲ������������һ���˵�Ԫ�ء������ָ�ƶ��ľ���������������Ϊ��ǰ��������Ҫ��������һ���˵�Ԫ�ء�
	 * @return       ��ǰ�������������һ���˵�Ԫ�ط���true�����򷵻�false
	 * */ 
	private boolean wantScrollToPrevious(){
		return xUp - xDown > 0;
	}
	
	/*	 �жϵ�ǰ���Ƶ���ͼ�ǲ������������һ���˵�Ԫ�ء������ָ�ƶ��ľ����Ǹ���������Ϊ��ǰ��������Ҫ��������һ���˵�Ԫ��
	 * 
	 * @return      ��ǰ�������������һ���˵�Ԫ�ط���true�����򷵻�false*/
	private boolean wantScrollToNext(){
		return xUp - xDown < 0;
	}
	
	/*	�ж��Ƿ�Ӧ�ù�������һ���˵�Ԫ�ء������ָ�ƶ����������Ļ��1/2��������ָ�ƶ��ٶȴ���SNAP_VELOCITY
	 *  ����ΪӦ�ù�������һ���˵�
	 *  
	 *  @return      ���Ӧ�ù�������һ���˵�Ԫ�ط���true�����򷵻�false*/
	private boolean shouldScrollPrevious(){
		return xUp - xDown > switcherViewWidth / 2 || getScrollVelocity() > SNAP_VELOCITY;
	}
	
	/*	�ж��Ƿ�Ӧ�ù�������һ���˵�Ԫ�ء������ָ�ƶ����������Ļ��1/2��������ָ�ƶ��ٶȴ���SNAP_VELOCITY
	 *  ����ΪӦ�ù�������һ���˵�
	 *  
	 *  @return       ���Ӧ�ù�������һ���˵�Ԫ�ط���true�����򷵻�false*/
	private boolean shouldScrollNext(){
		return xDown - xUp > switcherViewWidth / 2 || getScrollVelocity() > SNAP_VELOCITY;
	}
	
	
	/*  ��ǰ�Ƿ��ܹ���������������һ�������һ��Ԫ��ʱ�������ٹ�����
	 * 
	 * @return      ��ǰleftMargin��ֵ��leftEdge��rightEdge֮�䷵��true,���򷵻�false
	 * */
	private boolean beAbleToScroll(){
		return firstItemparams.leftMargin < rightEdge && firstItemparams.leftMargin > leftEdge;
	}
	
	
	/*  ���˵�����ʱ���Ƿ��д�Խborder 
	 *  @param leftMargin    ��һ��Ԫ�ص���ƫ��ֵ
	 *  @param speed        �������ٶȣ�����˵�����ҹ���������˵���������
	 *  @return             ��Խ�κ�һ��border�˷���true�����򷵻�false */
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
	
	/*  �ҵ����뵱ǰ��  leftMargin �������һ��borderֵ   
	 * @param leftMargin    ��һ��Ԫ�ص���ƫ��ֵ
	 * @return              ���뵱ǰ��leftMargin�����һ��borderֵ*/
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
	 ��һ���첽�����࿪ʼִ��ʱ��һ�����ĸ����裺
	 1��onPreExecute()  -->  ������ִ��ǰ�����û������ϵ����̡߳���һ��ͨ������������������ͨ�����û�������ʾһ��������
	 2: doInBackground(Integer... params)   -->   ����������е����д��𶼻������߳��н��У�����Ӧ��������ȥ�������к�ʱ�Ĳ���������һ����ɾ�
	 											����ͨ��return��佫�����ִ�н�����أ����   AsyncTask �ĵ��������Ͳ���ָ����Void���Ϳ��Բ�
	 											���������ִ�н����ע�⣬������������ǲ�������UI�����ģ������Ҫ����UIԪ�أ����練����ǰ�����
	 											ִ�н��ȣ����Ե��� publishProgress��Progress����������ɡ�	
	 3: onProgressUpdate(Progress...)    -->    ���ں�̨�е����� publishProgress��Progress�� ��������������ͻ�ܿ챻���ã�������Я���Ĳ���
	 											�����ں�̨�����д��ݹ����ģ�����������п��Զ�UI���в��������ò����е���ֵ�Ϳ��ԶԽ���Ԫ�ؽ���
	 											��Ӧ�ĸ��¡�
	 4: onPostExecute(Result)   -->   ����̨����ִ����ϲ�ͨ�� return �����з���ʱ����������ͺܿ�ᱻ���á����ص�������Ϊ�������ݵ��˷����У�
	 								�������÷��ص�����������һЩUI����������˵��������ִ�еĽ�����Լ��ص��������Ի����и���
	  */
	/*�첽������     <Params, Progress, Result> �ֱ��Ӧ�±������������е���������
	 * Params ����ִ��AsyncTask ʱ��Ҫ����Ĳ������������ں�̨������ʹ�á�
	 * progress : ��̨����ִ��ʱ�������Ҫ�ڽ�������ʾ��ǰ�Ľ��ȣ���ʹ������ָ���ķ�����Ϊ���ȵ�λ��
	 * Result :   ��������Ϻ������Ҫ�Խ�����з��أ���ʹ������ָ���ķ�����Ϊ����ֵ���͡�*/
	class ScrollTask extends AsyncTask<Integer, Integer, Integer>{
		
		@Override
		protected void onPreExecute() {
			// TODO Auto-generated method stub
			super.onPreExecute();
		}

		@Override
		protected Integer doInBackground(Integer... speed) {   //�ɱ��������˼���ǲ��������ж��������Ҫ������������ʱ������ô˷���
			// TODO Auto-generated method stub
			
			int leftMargin = firstItemparams.leftMargin;    //�õ���һ��ҳ��Ԫ�ص���߽��ֵ
			
			// ���ݴ�����ٶ����������� ����������Խ border ʱ������ѭ��
			while(true){
				leftMargin = leftMargin + speed[0];
				if(isCrossBorder(leftMargin, speed[0])){
					leftMargin = findClosesBorder(leftMargin);    //�õ����������borderֵ
					break;
				}
				publishProgress(leftMargin);         //����һ��������λ�Ľ�չ
				//Ϊ��Ҫ�й���Ч���Ĳ�����ÿ��ѭ��ʱʹ�߳�˯�� 10 ���룬�������۲��ܿ��ĵ������Ķ���
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
	//ָ����ǰ�߳�˯�߶�ã��Ժ���Ϊ��λ
	private void sleep(long millis){
		try {
			Thread.sleep(millis);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
