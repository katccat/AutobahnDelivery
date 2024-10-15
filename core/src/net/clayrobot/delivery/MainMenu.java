package net.clayrobot.delivery;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.utils.ScreenUtils;
public class MainMenu implements Screen {
	private final Delivery game;
	private OrthographicCamera camera;
	private Sprite backgroundSprite;
	public MainMenu(Delivery game) {
		this.game = game;
		camera = new OrthographicCamera();
		backgroundSprite = new Sprite(new Texture("new2.png"));
	}
	@Override
	public void show() {
		
	}
	@Override
	public void render(float delta) {
		ScreenUtils.clear(0, 0, 0, 1);
		//game.batch.begin();
		backgroundSprite.setBounds(camera.viewportWidth / 2 - camera.viewportHeight / 2, 0, camera.viewportHeight, camera.viewportHeight);
		backgroundSprite.draw(game.batch);
		//game.font.draw(game.batch, "Welcome to Autobahn Delivery Inc.", 0, 20);
		//game.batch.end();
		if (Gdx.input.isTouched()) game.setScreen(new Level(game));
		dispose();
	}
	@Override
	public void resize(int width, int height) {
		//if (game.mobilePlatform) camera.setToOrtho(false, 50, 50 * ((float) height / width));
		//else camera.setToOrtho(false, 30 * ((float) width / height), 30);
		camera.setToOrtho(false, 30 * ((float) width / height), 30);
		camera.update(false);
		game.batch.setProjectionMatrix(camera.combined);
	}
	@Override
	public void pause() {
		
	}
	@Override
	public void resume() {
		
	}
	@Override
	public void hide() {
		
	}
	@Override
	public void dispose() {
		
	}
}
