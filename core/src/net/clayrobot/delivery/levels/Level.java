package net.clayrobot.delivery.levels;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.utils.ScreenUtils;
import net.clayrobot.delivery.AutobahnDelivery;
import net.clayrobot.delivery.GameInputProcessor;
import net.clayrobot.delivery.MyContactListener;
import net.clayrobot.delivery.entities.Entities;
import net.clayrobot.delivery.entities.Player;

public abstract class Level implements Screen { // this class is the template for Screens that are Levels in this game
	public final AutobahnDelivery game;
	public static World world;
	protected final int WORLD_WIDTH;
	protected final int WORLD_HEIGHT;
	protected Player player;
	protected final OrthographicCamera camera;
	protected int score = 0;
	private final boolean FREE_CAM = true; // toggles whether camera can follow player anywhere or is bound to world borders
	protected boolean timed = true; // whether there is a time limit
	protected float time;
	public Level(AutobahnDelivery game, int width, int height) {
		this.game = game;
		world = new World(new Vector2(0, -10), true);
		world.setContactListener(new MyContactListener(this)); // this is in charge of noting when objects touch and acting accordingly depending on the contact
		WORLD_WIDTH = width;
		WORLD_HEIGHT = height;
		camera = new OrthographicCamera();
		
		game.multiplexer.addProcessor(new GameInputProcessor(this)); // this processor is in charge of game controls (since a level needs that)
	}
	public void start() {
		Entities.clear();
		time = 270; // seconds
		if (game.isPaused) game.resume();
	}
	public Player getPlayer() {
		return player;
	}
	public void setPlayer(Player player) {
		this.player = player;
	}
	protected void updateCamera() { // if free cam is false this method bounds the camera to world borders
		if (FREE_CAM) {
			camera.position.x = player.position.x;
			camera.position.y = player.position.y;
		}
		else {
			if (camera.viewportWidth <= WORLD_WIDTH) {
				//float maxLeft = camera.viewportWidth / 2;
				//float maxRight = WORLD_WIDTH - camera.viewportWidth / 2;
				//camera.position.x = Math.max(maxLeft, Math.min(player.pos.x, maxRight));
				camera.position.x = player.position.x;
			}
			else {
				//camera.position.x = WORLD_WIDTH / 2;
				camera.position.x = player.position.x;
			}
			if (camera.viewportHeight <= WORLD_HEIGHT) {
				float maxDown = camera.viewportHeight / 2;
				float maxUp = WORLD_HEIGHT - camera.viewportHeight / 2;
				camera.position.y = Math.max(maxDown, player.position.y);
				//camera.position.y = Math.max(maxDown, Math.min(player.pos.y, maxUp));
			}
			else {
				camera.position.y = WORLD_HEIGHT / 2;
			}
		}
		camera.update(false);
	}
	public void incrementScore() {
		score++;
	}

	public void decrementScore() {
		score--;
	}
	@Override
	public void render(float deltaTime) {
		if (deltaTime > 0) {
			world.step(deltaTime, 6, 2);
			updateCamera();
		}
		game.batch.setProjectionMatrix(camera.combined);
	}
	@Override
	public void resize(int width, int height) {
		//deltaTime = 0;
		if (game.mobilePlatform) {
			camera.setToOrtho(false, 50, 50 * ((float) height / width));
		}
		else {
			camera.setToOrtho(false, 30 * ((float) width / height), 30);
		}
		camera.setToOrtho(false, 30 * ((float) width / height), 30);
		camera.update(false);
	}
	protected void updateTime(float deltaTime) {
		if (time <= 0) game.pause();
		time -= deltaTime;
	}
	public int getTime() {
		if (time <= 0) return 0;
		return (int) time + 1;
	}
	@Override
	public void dispose() {
		Entities.clear();
		world.dispose();
		player = null;
		game.multiplexer.removeProcessor(1); // remove game processor (only needed when level is running)
	}
	@Override
	public void pause() {	
	}
	@Override
	public void resume() {	
	}
	@Override
	public void show() {
	}
	@Override
	public void hide() {
	}
}
