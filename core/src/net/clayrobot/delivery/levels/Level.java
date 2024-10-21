package net.clayrobot.delivery.levels;

import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.utils.ScreenUtils;
import net.clayrobot.delivery.Delivery;
import net.clayrobot.delivery.GameInputProcessor;
import net.clayrobot.delivery.MyContactListener;
import net.clayrobot.delivery.entities.Entities;
import net.clayrobot.delivery.entities.Player;

public abstract class Level implements Screen {
	protected Delivery game;
	public static World world;
	protected final int WORLD_WIDTH;
	protected final int WORLD_HEIGHT;
	public static Player player;
	protected final OrthographicCamera camera;
	protected int score = 0;
	private final boolean FREE_CAM = true;
	public Level(Delivery game, int width, int height) {
		this.game = game;
		world = new World(new Vector2(0, -10), true);
		world.setContactListener(new MyContactListener(this));
		WORLD_WIDTH = width;
		WORLD_HEIGHT = height;
		setStage();
		camera = new OrthographicCamera();
		game.multiplexer.addProcessor(new GameInputProcessor(game.platform));
		start(false);
	}
	abstract void setStage();
	public void start(boolean restart) {
		if (restart) Entities.clear();
		if (game.isPaused) game.resume();
	}
	
	protected void updateCamera() {
		if (FREE_CAM) {
			camera.position.x = player.pos.x;
			camera.position.y = player.pos.y;
		}
		else {
			if (camera.viewportWidth <= WORLD_WIDTH) {
				float maxLeft = camera.viewportWidth / 2;
				float maxRight = WORLD_WIDTH - camera.viewportWidth / 2;
				camera.position.x = Math.max(maxLeft, Math.min(player.pos.x, maxRight));
			}
			else {
				camera.position.x = WORLD_WIDTH / 2;
			}
			if (camera.viewportHeight <= WORLD_HEIGHT) {
				float maxDown = camera.viewportHeight / 2;
				float maxUp = WORLD_HEIGHT - camera.viewportHeight / 2;
				camera.position.y = Math.max(maxDown, Math.min(player.pos.y, maxUp));
			}
			else {
				camera.position.y = WORLD_HEIGHT / 2;
			}
		}
		camera.update(false);
		game.batch.setProjectionMatrix(camera.combined);
	}
	public void incrementScore() {
		score++;
	}

	public void decrementScore() {
		score--;
	}
	@Override
	public void render(float deltaTime) {
		ScreenUtils.clear(1, 1, 1, 1);
		if (deltaTime > 0) {
			world.step(deltaTime, 6, 2);
			updateCamera();
		}
	}
	@Override
	public void resize(int width, int height) {
		if (game.mobilePlatform) {
			camera.setToOrtho(false, 50, 50 * ((float) height / width));
		}
		else {
			camera.setToOrtho(false, 30 * ((float) width / height), 30);
		}
		camera.setToOrtho(false, 30 * ((float) width / height), 30);
		updateCamera();
	}
	@Override
	public void dispose() {
		Entities.clear();
		world.dispose();
		player = null;
		game.multiplexer.removeProcessor(1);
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