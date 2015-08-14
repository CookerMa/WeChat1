package com.toggle.wechat;

import java.util.ArrayList;
import java.util.List;

import com.toggle.wechat.adapter.VoiceListAdapter;
import com.toggle.wechat.bean.Recorder;
import com.toggle.wechat.utils.MediaManager;
import com.toggle.wechat.view.AudioRecoderButton;
import com.toggle.wechat.view.AudioRecoderButton.AudioRecordFinishListener;

import android.media.MediaPlayer;
import android.os.Bundle;
import android.app.Activity;
import android.graphics.drawable.AnimationDrawable;
import android.view.Menu;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

public class MainActivity extends Activity implements OnItemClickListener, AudioRecordFinishListener {

	private ListView voiceList;
	private AudioRecoderButton btnRecord;
	private VoiceListAdapter mAdapter;

	private List<Recorder> mDatas = new ArrayList<Recorder>();
	private AnimationDrawable animation;
	private View voiceAnim;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		voiceList = (ListView) findViewById(R.id.voiceList);
		
		btnRecord = (AudioRecoderButton) findViewById(R.id.btnRecord);
		btnRecord.setAudioRecordFinishListener(this);

		mAdapter = new VoiceListAdapter(this, mDatas);
		voiceList.setAdapter(mAdapter);
		voiceList.setOnItemClickListener(this);
	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		MediaManager.release();
	}

	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
		MediaManager.pause();
	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		MediaManager.resume();
	}

	@Override
	public void onFinish(float second, String filePath) {
		Recorder recorder = new Recorder(second, filePath);
		mDatas.add(recorder);
		mAdapter.notifyDataSetChanged();
		voiceList.setSelection(mDatas.size() - 1);
	}

	@Override
	public void onItemClick(AdapterView<?> arg0, View view, int position, long arg3) {
		if (animation != null) {
			voiceAnim.setBackgroundResource(R.drawable.icon_voice_ripple);
			voiceAnim = null;
		}
		voiceAnim = view.findViewById(R.id.voiceAnim);
		voiceAnim.setBackgroundResource(R.drawable.anim_play_audio);
		animation = (AnimationDrawable) voiceAnim.getBackground();
		animation.start();
		// 播放音频
		MediaManager.playSound(mDatas.get(position).filePath,
				new MediaPlayer.OnCompletionListener() {
					@Override
					public void onCompletion(MediaPlayer mp) {
						voiceAnim
								.setBackgroundResource(R.drawable.icon_voice_ripple);
					}
				});

	}

}
