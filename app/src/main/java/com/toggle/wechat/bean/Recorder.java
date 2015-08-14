package com.toggle.wechat.bean;

public class Recorder {
	public float audioLength;
	public String filePath;
	public Recorder(float second, String filePath) {
		super();
		this.audioLength = audioLength;
		this.filePath = filePath;
	}
	public float getAudioLength() {
		return audioLength;
	}
	public void setAudioLength(float audioLength) {
		this.audioLength = audioLength;
	}
	public String getFilePath() {
		return filePath;
	}
	public void setFilePath(String filePath) {
		this.filePath = filePath;
	}
	
}
