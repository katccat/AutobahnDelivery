package net.clayrobot.delivery;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.Application.ApplicationType;
import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.PolygonSpriteBatch;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator.FreeTypeFontParameter;
import com.badlogic.gdx.maps.MapRenderer;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.Box2DDebugRenderer;
import com.badlogic.gdx.physics.box2d.World;
import java.util.Random;
import net.clayrobot.delivery.entities.Box;
import net.clayrobot.delivery.entities.Entities;
import net.clayrobot.delivery.entities.Player;
import space.earlygrey.shapedrawer.ShapeDrawer;


public class Delivery extends Game {
	private Screen screen;
	public PolygonSpriteBatch batch;
	private SpriteBatch fgBatch;
	public ShapeDrawer shapeDrawer;
	public Application.ApplicationType platform;
	public boolean mobilePlatform = false;
	
	protected Box2DDebugRenderer debugRenderer;
	public BodyDef dynamicBodyDef = new BodyDef();
	public BodyDef staticBodyDef = new BodyDef();
	
	protected boolean isPaused = false;
	private boolean justResumed = false;
	protected boolean drawDebug = false;

	public int refreshRate;
	public final Random random = new Random();
	public World activeWorld;
	public BitmapFont InstrumentSerif;
	public BitmapFont foo;
	protected InputMultiplexer multiplexer;
	private static Delivery game;
	public String displayText = "";
	private OrthographicCamera camera;
	public TmxMapLoader mapLoader;
	public OrthogonalTiledMapRenderer mapRenderer;
	
	public static Delivery getGame() {
		return game;
	}
	
	@Override
	public void create() {
		game = this;
		camera = new OrthographicCamera();
		batch = new PolygonSpriteBatch();
		fgBatch = new SpriteBatch();
		shapeDrawer = new ShapeDrawer(batch, new TextureRegion(new Texture("pixel.png")));
		mapLoader = new TmxMapLoader();
		FreeTypeFontGenerator generator = new FreeTypeFontGenerator(Gdx.files.internal("fonts/InstrumentSerif-Regular.ttf"));
		FreeTypeFontParameter parameter = new FreeTypeFontParameter();
		int size = 100;
		parameter.size = size;
		parameter.color = Color.BLACK;
		//parameter.kerning = true;
		InstrumentSerif = generator.generateFont(parameter);
		InstrumentSerif.getData().setScale(10f / size);
		generator.dispose();
		
		refreshRate = Gdx.graphics.getDisplayMode().refreshRate;
		Gdx.graphics.setContinuousRendering(false);
		
		platform = Gdx.app.getType();
		mobilePlatform = (platform == ApplicationType.Android || platform == ApplicationType.iOS);
		
		
		dynamicBodyDef.type = BodyDef.BodyType.DynamicBody;
		staticBodyDef.type = BodyDef.BodyType.StaticBody;
		debugRenderer = new Box2DDebugRenderer();
		
		setupInputProcessor();
		setScreen(new Level(this));
	}
	private void setupInputProcessor() {
		InputAdapter UiInputProcessor = new InputAdapter() {
			@Override
			public boolean keyDown(int keycode) {
				switch (keycode) {
					case Input.Keys.ESCAPE:
					case Input.Keys.P:
                        if (isPaused) resume();
						else pause();
						return true;
					case Input.Keys.R:
						//if (screen instanceof Level) {
						//	Level level = (Level) screen;
						//	level.start(true);
						//}
						screen.dispose();
						setScreen(new Level(game));
						return true;
					case 68:
						drawDebug = !drawDebug;
						Player.drawDebugMark = drawDebug;
						Box.drawDebug = drawDebug;
						return true;
				}
				return false;
			}
			@Override
			public boolean touchDown(int screenX, int screenY, int pointer, int button) {
				if (platform != ApplicationType.Desktop) {
					if (screenX <= Gdx.graphics.getWidth() * 0.08) {
						if (screenY <= Gdx.graphics.getHeight() * 0.125) {
							screen.dispose();
							setScreen(new MainMenu(game));
							return true;	
						}
					}
				}
				return false;
			}
		};
		multiplexer = new InputMultiplexer();
		multiplexer.addProcessor(UiInputProcessor);
		Gdx.input.setInputProcessor(multiplexer);
	}

	
	@Override
	public void dispose() {
		if (screen != null) {
			screen.hide();
		}
		batch.dispose();
		InstrumentSerif.dispose();
		Entities.disposeStaticResources();
		debugRenderer.dispose();
	}

	@Override
	public void pause() {
		isPaused = true;
		if (screen != null) {
			screen.pause();
		}
	}

	@Override
	public void resume() {
		isPaused = false;
		justResumed = true;
		if (screen != null) {
			screen.resume();
		}
	}

	@Override
	public void render() {
		if (screen == null) return;
		float deltaTime = 0;
		batch.begin();
		if (justResumed) {
			justResumed = false;
		}
		else if (!isPaused) {
			deltaTime = Gdx.graphics.getDeltaTime();
		}
		screen.render(deltaTime);
		batch.end();
		fgBatch.begin();
		
		InstrumentSerif.draw(fgBatch, displayText, 15, 20);
		
		fgBatch.end();
		if (!isPaused) Gdx.graphics.requestRendering();
	}

	@Override
	public void resize(int width, int height) {
		camera.setToOrtho(false, 30 * ((float) width / height), 30);
		camera.update(false);
		fgBatch.setProjectionMatrix(camera.combined);
		if (screen != null) {
			screen.resize(width, height);
		}
	}

	/**
	 * Sets the current screen. {@link Screen#hide()} is called on any old
	 * screen, and {@link Screen#show()} is called on the new screen, if any.
	 *
	 * @param screen may be {@code null}
	 */
	public void setScreen(Screen screen) {
		if (this.screen != null) {
			this.screen.hide();
		}
		this.screen = screen;
		if (this.screen != null) {
			this.screen.show();
			this.screen.resize(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
		}
	}

	/**
	 * @return the currently active {@link Screen}.
	 */
	public com.badlogic.gdx.Screen getScreen() {
		return screen;
	}
}
