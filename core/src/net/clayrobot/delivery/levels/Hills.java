package net.clayrobot.delivery.levels;

import com.badlogic.gdx.*;
import com.badlogic.gdx.graphics.g2d.*;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.maps.MapObjects;
import com.badlogic.gdx.maps.tiled.TiledMap;
import net.clayrobot.delivery.Delivery;
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
	
	public Hills(Delivery game) {
		super(game, 200, 120);
		if (ENABLE_MUSIC) {
			music = Gdx.audio.newMusic(Gdx.files.internal("helvetica.mp3"));
			music.setLooping(true);
			music.play();
		}
	}
	@Override
	public void start(boolean restart) {
		super.start(restart);
		if (game.mobilePlatform || restart) {
			new Billboard(8, GROUND_HEIGHT + 10, 10, 10, new Texture("propaganda/GRIT.png"), new Texture("propaganda/GRIT2.png"));
		}
		else {
			new Billboard(8, GROUND_HEIGHT + 10, 10, 10, new Texture("propaganda/WADSLR.png"), new Texture("propaganda/WADSLR2.png"));
			//new Billboard(8, GROUND_HEIGHT + 10, 10, 10, new Texture("propaganda/level1map.png"));
		}
		House.spawn(houseMapObjects);
		Player.spawn.x = 25;
		Player.spawn.y = GROUND_HEIGHT + 5;
		player = new Player();
		Box.spawn(15, GROUND_HEIGHT, winningScore);
	}
	@Override
	protected void setStage() {
		Texture fgTex = new Texture("levels/hills/level1fg1lit.png");
		Texture bgTex = new Texture("levels/hills/level1bg.png");
		fgTex.setAnisotropicFilter(0);
		bgTex.setAnisotropicFilter(0);
		foregroundSprite = new Sprite(fgTex);
		foregroundSprite.setBounds(0, 0, WORLD_WIDTH, WORLD_HEIGHT);
		backgroundSprite = new Sprite(bgTex);
		backgroundSprite.setBounds(0, 0, WORLD_WIDTH, WORLD_HEIGHT);
		
		map = game.mapLoader.load("levels/hills/level1.tmx");
		houseMapObjects = map.getLayers().get("Houses").getObjects();
		MapObjects TerrainMapObjects = map.getLayers().get("Terrain").getObjects();
		MapSpawner mapSpawner = new MapSpawner();
		mapSpawner.TerrainSpawner(TerrainMapObjects, 0.25f, world, game.staticBodyDef);
	}
	@Override
	public void render(float deltaTime) {
		super.render(deltaTime);
		game.batch.begin();
		backgroundSprite.draw(game.batch);
		Entities.draw(deltaTime);
		foregroundSprite.draw(game.batch);
		game.batch.end();
		if (game.drawDebug) game.debugRenderer.render(world, camera.combined);
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
		foregroundSprite.getTexture().dispose();
		backgroundSprite.getTexture().dispose();
		super.dispose();
	}
}