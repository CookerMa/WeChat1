package com.toggle.wechat.view;

import com.toggle.wechat.R;
import com.toggle.wechat.utils.AudioManager;
import com.toggle.wechat.utils.AudioManager.AudioStateChangeListener;
import com.toggle.wechat.utils.DensityUtil;
import com.toggle.wechat.utils.DialogManager;

import android.content.Context;
import android.os.Environment;
import android.os.Handler;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

public class AudioRecoderButton extends Button implements AudioStateChangeListener {

	private static final String TAG = "AutoRecoderButton";
	
	private State mCurState =State.STATE_NORMAL;
	
	private boolean isRecording =false;
	
	private boolean mReady =false;  //是否触发onclick
	
	private static final int DISTANCE_CANCEL_Y = 50;
	
	private DialogManager mDialogManager;
	
	private AudioManager mAudioManager;
	private float mTime;
	
	private static final int MSG_AUDIO_PREPARED = 0x110;
	private static final int MSG_VOLUME_CHAMGED = 0x111;
	private static final int MSG_DIALOG_DISMISS = 0x112;
	  enum State{
		 STATE_NORMAL,   //平常状态
		 STATE_RECODING, //录音状态
		 STATE_CANCEL  ;  //取消状态
	  }
	
	  
	  
	private Handler mHanlder = new Handler() {
		public void handleMessage(android.os.Message msg) {
			switch (msg.what) {
			case MSG_AUDIO_PREPARED:
				mDialogManager.creatDialog();
				isRecording = true;

				// 音量
				new Thread(getVolumeRunnable).start();

				break;
			case MSG_VOLUME_CHAMGED:
				mDialogManager.updateVoloumeLevel(mAudioManager
						.getVoiceLevel(7));
				break;
			case MSG_DIALOG_DISMISS:
				mDialogManager.dismissDialog();

				break;

			default:
				break;
			}
		};
	};
	  
	/**
	 * 录音完成后的回调
	 * 
	 */
	public interface AudioRecordFinishListener {
		void onFinish(float second, String filePath);
	}

	private AudioRecordFinishListener audioRecordFinishListener;

	public void setAudioRecordFinishListener(AudioRecordFinishListener listener) {
		audioRecordFinishListener = listener;
	}  
	  
	
	public AudioRecoderButton(Context context) {
		this(context,null);
	}

	public AudioRecoderButton(Context context, AttributeSet attrs) {
		super(context, attrs);
//		DensityUtil.px2dip(context, DISTANCE_CANCEL_Y);
		mDialogManager=new DialogManager(context);
		
		String dir = Environment.getExternalStorageDirectory()
				+ "/example_chat_audios";
		mAudioManager = AudioManager.getInstance(dir);
		mAudioManager.setOnAudioStateChangeListener(this);
		
		
		
		
		setOnLongClickListener(new OnLongClickListener() {
			
			@Override
			public boolean onLongClick(View v) {
				
				mReady =true;
				mAudioManager.prepareAudio();
				
				return false;
			}
		});
	}


	
	private Runnable getVolumeRunnable = new Runnable() {

		

		@Override
		public void run() {

			while (isRecording) {
				try {
					Thread.sleep(100);
					mTime += 0.1f;
					mHanlder.sendEmptyMessage(MSG_VOLUME_CHAMGED);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}

			}
		}

	};
	
	
	/**
	 * 处理按钮的点击
	 */
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		
		int x = (int) event.getX();
		int y = (int) event.getY();
		
		switch (event.getAction()) {
		case MotionEvent.ACTION_DOWN:
//			Log.i(TAG, "按下");
			isRecording =true;
			changeState(State.STATE_RECODING);
			break;
		case MotionEvent.ACTION_MOVE:
//			Log.i(TAG, "移动");
			if(isRecording ){
			if(wantToCancel(x,y)){
				
				changeState(State.STATE_CANCEL);	
				
			}else{
				
				changeState(State.STATE_RECODING);
			}
			
			}
			
			break;
		case MotionEvent.ACTION_UP:
//			Log.i(TAG, "抬起");
			
			if(!mReady){
				reset();
				return super.onTouchEvent(event);
			}
			
			if(!isRecording ||mTime<0.6f){
				mDialogManager.tooShort();
				mAudioManager.cancel();
				mHanlder.sendEmptyMessageDelayed(MSG_DIALOG_DISMISS, 1300);
			}else if(mCurState ==State.STATE_RECODING){ //正常录音状态结束
				mDialogManager.dismissDialog();
				//释放
				mAudioManager.release();
				
				//与activity交互
				
				if (audioRecordFinishListener != null) {
					audioRecordFinishListener.onFinish(mTime,
							mAudioManager.getCurrentPath());
				}
				
			}else if (mCurState == State.STATE_CANCEL) {
				mDialogManager.dismissDialog();
				mAudioManager.cancel();

			}
			
		
			reset();
			break;

		}
		
		return super.onTouchEvent(event);   
//		return true;    //自定义的话如果不重写点击事件，好像事件不能捕获
	}

	
	
	/**
	 * 
	 * 重置状态及标志位
	 * 
	 */
	
	private void reset() {
		isRecording =false;
		mReady =false;
		mTime = 0;
		changeState(State.STATE_NORMAL); 
	}

	/**
	 * 是否取消
	 * @param x
	 * @param y
	 * @return
	 */
	private boolean wantToCancel(int x, int y) {
		if(x<0 ||x>getWidth()){
			return true;
		}
		
		if(y<-DISTANCE_CANCEL_Y || y>DISTANCE_CANCEL_Y+getHeight()){
			return true;
		}
		return false;
	}

	/**
	 * 状态的改变
	 * 改变按钮的文本
	 * @param ordinal
	 */
	private void changeState(State stateRecoding) {
	
		
		
		if(mCurState != stateRecoding){  //如果点击的状态不是默认状态 
			
			mCurState=stateRecoding;
			switch (stateRecoding) {
			case STATE_NORMAL:
				setBackgroundResource(R.drawable.btn_recorder_normal);
				setText(R.string.btn_recorder_normal);

				break;
			case STATE_RECODING:
				setBackgroundResource(R.drawable.btn_recorder_normal);
				setText(R.string.btn_recorder_recording);
				if (isRecording) {
					mDialogManager.isRecording();
				}
				break;
			case STATE_CANCEL:
				setBackgroundResource(R.drawable.btn_recorder_normal);
				setText(R.string.btn_recorder_want_cancel);
				mDialogManager.wantToCancel();
				break;

			}
			
		}
	}
	
	
	
	public boolean IsCanUseSdCard() { 
	    try { 
	        return Environment.getExternalStorageState().equals( 
	                Environment.MEDIA_MOUNTED); 
	    } catch (Exception e) { 
	        e.printStackTrace(); 
	    } 
	    return false; 
	}

	@Override
	public void wellPrepared() {
		mHanlder.sendEmptyMessage(MSG_AUDIO_PREPARED);
		
	}
	
	
}
