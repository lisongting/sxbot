package com.droid.sxbot.mvp.scene;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.environment.DirectionalLight;
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.BoundingBox;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Array;
import com.droid.sxbot.Constant;

/**
 * Created by lisongting on 2018/3/5.
 * 用来加载模型
 */

public class Loader implements ApplicationListener {
    //透视相机
    private PerspectiveCamera camera;
    private ModelInstance instanceXbot,instanceMuseum;
    //使用ModelBatch来渲染图像
    private ModelBatch modelBatch;
    private Environment environment;
    private CameraController controller;
    private AssetManager assets;
    private Array<ModelInstance> instances = new Array<>();
    private Label label;
    private StringBuilder stringBuilder;
    private Stage stage;
    private boolean loading;
    private final String MODEL_XBOT = Constant.XBOT_MODEL;
    private final String MODEL_MUSEUM = Constant.MUSEUM_MODEL;
    private Vector3 minVector,maxVector;
    private Context context;
    private Texture btTexture;
    private Button btBack;
    //返回按钮的左上角坐标
    private final int buttonStartX = 50;
    private final int buttonStartY = 50;
    //表示机器人位置和方向角
    float x,z,theta;
    //代表博物馆的长宽
    private final float maxX = 50.0f;
    private final float maxY = 50.0f;
    private Vector3 axisY;


    public Loader(Context context) {
        this.context = context;
    }

    @Override
    public void create() {
        modelBatch = new ModelBatch();
        environment = new Environment();
        stringBuilder = new StringBuilder();
        axisY = new Vector3(0, 1, 0);
        stage = new Stage(){
            @Override
            public boolean touchDown(int screenX, int screenY, int pointer, int button) {
                int left = buttonStartX;
                int top = Gdx.graphics.getHeight() - buttonStartY;
                int right = (int) (left + btBack.getWidth());
                int bottom = (int) (top + btBack.getHeight());
                if (screenX >= left*0.9f && screenX <= right*1.1f && screenY >= top*0.9f && screenY <= bottom*1.1f) {
                    Gdx.app.exit();
                    return true;
                }
                return false;
            }
        };
        label = new Label(" ", new Label.LabelStyle(new BitmapFont(), Color.WHITE));
        stage.addActor(label);
        //构建环境光0.4,0.4,0.4
        environment.set(new ColorAttribute(ColorAttribute.AmbientLight,0.4f,0.4f,0.4f,1f));
        environment.add(new DirectionalLight().set(0.8f, 0.8f, 0.8f, -1f, -0.8f, -0.2f));

        //设置为67度视角
        camera = new PerspectiveCamera(67, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        camera.position.set(0f, 20f, -30f);
        camera.lookAt(0, 0, 0);
        camera.near = 1f;
        camera.far = 300f;
        camera.update();
        controller = new CameraController(camera);
        controller.setCameraData(new Vector3(0f,20f,-30f),new Vector3(0f, 0f, 0f));


        assets = new AssetManager();
        assets.load(MODEL_XBOT, Model.class);
        assets.load(MODEL_MUSEUM, Model.class);
        loading = true;


        btTexture = new Texture(Gdx.files.internal("back.jpg"));
        Button.ButtonStyle style = new Button.ButtonStyle();
        style.up = new TextureRegionDrawable(new TextureRegion(btTexture));
        btBack = new Button(style);
        btBack.setPosition(buttonStartX,buttonStartY);
        btBack.addListener(new ClickListener(){
            @Override
            public void clicked(InputEvent event, float x, float y){
                Toast.makeText(context, "Clicked", Toast.LENGTH_SHORT).show();
            }}
        );
        stage.addActor(btBack);

        InputMultiplexer inputMultiplexer = new InputMultiplexer();
        inputMultiplexer.addProcessor(stage);
        inputMultiplexer.addProcessor(controller);
        Gdx.input.setInputProcessor(inputMultiplexer);

//        ModelLoader loader = new G3dModelLoader(new JsonReader());
//        ModelData modelData = loader.loadModelData(Gdx.files.internal(MODEL_MUSEUM));
//        model = new Model(modelData, new TextureProvider.FileTextureProvider());
//        doneLoading();

    }


    private void doneLoading() {
        Model xbot = assets.get(MODEL_XBOT, Model.class);
        instanceXbot = new ModelInstance(xbot);

        Model museum = assets.get(MODEL_MUSEUM, Model.class);
        instanceMuseum = new ModelInstance(museum);

        loading = false;
        instances.add(instanceMuseum, instanceXbot);

        BoundingBox boundingBox = new BoundingBox();
        instanceMuseum.calculateBoundingBox(boundingBox);
        maxVector = new Vector3();
        minVector = new Vector3();
        boundingBox.getMax(maxVector);
        boundingBox.getMin(minVector);
        log( "maxVector:"+maxVector.toString());
        log( "minVector:"+minVector.toString());

    }

    @Override
    public void resize(int width, int height) {
        stage.getViewport().update(width, height, true);
    }

    @Override
    public void render() {
        if (loading && assets.update()) {
            doneLoading();
        }
        controller.update();
        Gdx.gl.glViewport(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);

        modelBatch.begin(camera);

        modelBatch.render(instances, environment);

        if (instances.size >= 2) {
            ModelInstance xbot = instances.get(1);
            float degree = (float) ((theta-Math.PI)/Math.PI) *180f;
            //同时设置位移和旋转角
            xbot.transform.set(new Vector3(x, 0, z), new Quaternion(axisY, degree));
        }

        modelBatch.end();

        stringBuilder.setLength(0);
        stringBuilder.append("FPS: ").append(Gdx.graphics.getFramesPerSecond());
        label.setText(stringBuilder.toString());
        label.setPosition(20,Gdx.graphics.getHeight()-50);
        label.setFontScale(3);

        stage.act();
        stage.draw();
    }

    //因为在手机中是以x-z平面显示博物馆底部的
    public synchronized void updateRobotPosition(float toX, float toZ,float theta) {
        float percentX = toX / maxX;
        //在xz平面更新位置
        float percentZ = toZ / maxY;

        //计算相对位置
        x = (maxVector.x - minVector.x) * (0.5f-percentX);
        z = minVector.z + (maxVector.z - minVector.z) * percentZ;
        this.theta = theta;
    }

    @Override
    public void pause() {

    }

    @Override
    public void resume() {

    }

    @Override
    public void dispose() {
        modelBatch.dispose();
        instances.clear();
        assets.dispose();
        stage.dispose();
    }

    private void log(String s) {
        Log.i("Loader", s);
    }
}
