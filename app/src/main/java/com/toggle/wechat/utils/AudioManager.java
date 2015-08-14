package com.toggle.wechat.utils;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

import android.media.MediaRecorder;

public class AudioManager {

	private static AudioManager mAudioManager;
	
	private static String mDir;
	
	private String mCurFilePath;
	public AudioStateChangeListener audioStateChangeListener;

	private MediaRecorder mediaRecorder;

	private boolean isPrepared =false;
	
	private AudioManager(String dir){
		this.mDir =dir;
		
	}; 
	
	
	public interface AudioStateChangeListener {
		void wellPrepared();
	}



	public void setOnAudioStateChangeListener(AudioStateChangeListener listener) {
		audioStateChangeListener = listener;
	}
	
	public static AudioManager getInstance(String mDir){
		
		if(mAudioManager ==null){
			synchronized (AudioManager.class) {
				if(mAudioManager ==null){
				mAudioManager=new AudioManager(mDir);
				}
			}
			
			return mAudioManager;
		}
		
		return mAudioManager;
	}
	
	
	
	public void prepareAudio(){
		isPrepared = false;
		try {
			File dir =new File(mDir);
			if(!dir.exists()){
				dir.mkdirs();
			}
			
			String fileName = getFileName();
			File file =new File(dir, fileName);

			
			mCurFilePath =file.getAbsolutePath();
			mediaRecorder = new MediaRecorder();
			// 设置输出文件
			mediaRecorder.setOutputFile(mCurFilePath);
			// 设置音频源麦克风
			mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
			// 设置音频格式
			mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.AMR_NB);
			// 设置音频编码
			mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);

			mediaRecorder.prepare();
			mediaRecorder.start();
			isPrepared = true;
			if (audioStateChangeListener != null) {
				audioStateChangeListener.wellPrepared();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	
	
	/**
	 * 获取文件名
	 * @return
	 */
	private String getFileName() {
		return UUID.randomUUID()+".amr";
	}
	
	
	
	
	
	public int getVoiceLevel(int maxLevel) {
		if (isPrepared) {
			try {
				//mediaRecorder.getMaxAmplitude() -->1~32767
				return maxLevel * mediaRecorder.getMaxAmplitude() / 32768 + 1;
			} catch (Exception e) {
			}
		}
		return 1;
	}
	
	public void release() {
		mediaRecorder.stop();
		mediaRecorder.release();
		mediaRecorder = null;

	}
     /**
      * 释放删除文件
      */
	public void cancel() {
		release(); 
		if (mCurFilePath != null) {
			File file = new File(mCurFilePath);
			file.delete();
			mCurFilePath = null;
		}
	}

	public String getCurrentPath() {
		return mCurFilePath;
	}
	
	
}
