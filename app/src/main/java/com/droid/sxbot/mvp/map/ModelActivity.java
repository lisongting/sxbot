package com.droid.sxbot.mvp.map;

import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.badlogic.gdx.backends.android.AndroidApplication;
import com.badlogic.gdx.backends.android.AndroidApplicationConfiguration;
import com.droid.sxbot.App;
import com.droid.sxbot.RosConnectionService;
import com.jilk.ros.PublishEvent;

/**
 * 使用libGdx加载三维模型文件
 * 注：需要先将模型文件(obj)转换为.g3dj文件。Dae文件不建议直接转.g3dj
 */
public class ModelActivity extends AndroidApplication {
	private Loader loader;
	private RosConnectionService.ServiceBinder proxy;
	private final String TOPIC_NAME = "";

	@Override
	protected void onCreate (Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		AndroidApplicationConfiguration config = new AndroidApplicationConfiguration();
		Toast.makeText(this, "模型加载中....", Toast.LENGTH_LONG).show();
		loader = new Loader(getContext());
		initialize(loader, config);

	}

	@Override
	protected void onResume() {
		super.onResume();
		App app = (App) getApplication();
		proxy = app.getRosServiceProxy();
		proxy.manipulateTopic(TOPIC_NAME, true);
	}

	//todo:控制机器人位置更新
	public void onEvent(PublishEvent event) {

	}


	private void log(String s) {
		Log.i("tag", s);
	}

}
