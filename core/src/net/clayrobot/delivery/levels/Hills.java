package net.clayrobot.delivery.levels;

import com.badlogic.gdx.*;
import com.badlogic.gdx.graphics.g2d.*;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.maps.MapObjects;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.utils.ScreenUtils;
import net.clayrobot.delivery.AutobahnDelivery;
import net.clayrobot.delivery.MapSpawner;

import net.clayrobot.delivery.entities.*;

public class Hills extends Level {
	private final int GROUND_HEIGHT = 8;
	private TiledMap map;
	private MapObjects houseMapObjects;
	private Sprite foregroundSprite;
	private Sprite backgroundSprite;
	private Music music;
	private final boolean ENABLE_MUSIC = true;
	private final int winningScore = 6;
	private Animation<Texture> skyAnim;
	private Sprite skySprite;
	private Animation<Texture> lakeAnim;
	private float animTime = 0;
	private OrthographicCamera parallaxCam;
	
	public Hills(AutobahnDelivery game) {
		super(game, 200, 120);
		if (ENABLE_MUSIC) {
			music = Gdx.audio.newMusic(Gdx.files.internal("helvetica.mp3"));
			music.setLooping(true);
			music.play();
		}
		int factor = 2; // skip every other frame in frames folder
		Texture[] skyTex = new Texture[76 / factor];
		for (int i = 0; i < skyTex.length; i ++) {
				skyTex[i] = new Texture("levels/hills/sky/2/sky" + (i * factor) + ".png");
				skyTex[i].setAnisotropicFilter(0);
		}
		skySprite = new Sprite(skyTex[0]);
		//skySprite.setBounds(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
		skyAnim = new Animation<>(0.2f, skyTex);
		skyAnim.setPlayMode(Animation.PlayMode.LOOP_PINGPONG);
		Texture[] fgTex = {
			new Texture("levels/hills/level1fg1.png"),
			new Texture("levels/hills/level1fg2.png")
		};
		fgTex[0].setAnisotropicFilter(0);
		fgTex[1].setAnisotropicFilter(0);
		lakeAnim = new Animation<>(0.25f, fgTex);
		lakeAnim.setPlayMode(Animation.PlayMode.LOOP);
		
		Texture bgTex = new Texture("levels/hills/level1bg.png");
		bgTex.setAnisotropicFilter(0);
		foregroundSprite = new Sprite(fgTex[0]);
		foregroundSprite.setBounds(0, 0, WORLD_WIDTH, WORLD_HEIGHT);
		backgroundSprite = new Sprite(bgTex);
		backgroundSprite.setBounds(0, 0, WORLD_WIDTH, WORLD_HEIGHT);

		map = game.mapLoader.load("levels/hills/level1.tmx"); // this file contains house locations and level geometry
		houseMapObjects = map.getLayers().get("Houses").getObjects(); // retrieve house data from house layer in tmx file
		MapObjects TerrainMapObjects = map.getLayers().get("Terrain").getObjects();
		MapSpawner mapSpawner = new MapSpawner();
		mapSpawner.TerrainSpawner(TerrainMapObjects, 0.25f, world, game.staticBodyDef);
		
		parallaxCam = new OrthographicCamera();
		start();
	}
	@Override
	public void start() {
		super.start();
		//if (game.mobilePlatform) {
			new Billboard(8, GROUND_HEIGHT + 10, 10, 10, new Texture("propaganda/GRIT.png"), new Texture("propaganda/GRIT2.png"));
		//}
		//else {
			//new Billboard(8, GROUND_HEIGHT + 10, 10, 10, new Texture("propaganda/WADSLR.png"), new Texture("propaganda/WADSLR2.png"));
			//new Billboard(8, GROUND_HEIGHT + 10, 10, 10, new Texture("propaganda/level1map.png"));
		//}
		House.spawn(houseMapObjects);
		Player.spawn.x = 25;
		Player.spawn.y = GROUND_HEIGHT + 5;
		Player.EnabledArmType = Player.ArmType.CLAW1;
		player = new Player();
		Box.spawn(15, GROUND_HEIGHT, winningScore);
	}
	@Override
	public void render(float deltaTime) {
		updateTime(deltaTime);
		ScreenUtils.clear(1, 1, 1, 1);
		game.batch.setProjectionMatrix(parallaxCam.combined);
		game.batch.begin();
		animTime += deltaTime;
		skySprite.setTexture(skyAnim.getKeyFrame(animTime));
		skySprite.draw(game.batch);
		super.render(deltaTime);
		backgroundSprite.draw(game.batch);
		//Box.drawAll();
		Entities.draw(deltaTime);
		//foregroundSprite.setTexture(anim.getKeyFrame(animTime));
		foregroundSprite.draw(game.batch);
		game.batch.end();
		if (game.drawDebug) game.debugRenderer.render(world, camera.combined);
	}
	@Override
	public void resize(int width, int height) {
		super.resize(width, height);
		parallaxCam.setToOrtho(false, camera.viewportWidth, camera.viewportHeight);
		skySprite.setBounds(0, 0, parallaxCam.viewportWidth, parallaxCam.viewportHeight);
	}
	@Override
	public void pause() {
		if (ENABLE_MUSIC) music.pause();
	}
	@Override
	public void resume() {
		if (ENABLE_MUSIC) music.play();
	}
	@Override
	public void dispose() {
		map.dispose();
		if (ENABLE_MUSIC) music.dispose();
		for (Texture texture : lakeAnim.getKeyFrames()) {
			texture.dispose();
		}
		for (Texture texture : skyAnim.getKeyFrames()) {
			texture.dispose();
		}
		backgroundSprite.getTexture().dispose();
		super.dispose();
	}
}