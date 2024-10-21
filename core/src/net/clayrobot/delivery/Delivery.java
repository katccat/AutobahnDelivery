package net.clayrobot.delivery;

import net.clayrobot.delivery.levels.TestGrounds;
import net.clayrobot.delivery.levels.Level;
import net.clayrobot.delivery.levels.Hills;
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
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.PolygonSpriteBatch;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator.FreeTypeFontParameter;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.Box2DDebugRenderer;
import java.util.Random;
import net.clayrobot.delivery.entities.Entities;
import space.earlygrey.shapedrawer.ShapeDrawer;


public class Delivery extends Game {
	public PolygonSpriteBatch batch;
	private SpriteBatch fgBatch;
	public ShapeDrawer shapeDrawer;
	public Application.ApplicationType platform;
	public boolean mobilePlatform = false;
	
	public Box2DDebugRenderer debugRenderer;
	public BodyDef dynamicBodyDef = new BodyDef();
	public BodyDef staticBodyDef = new BodyDef();
	
	public boolean isPaused = false;
	private boolean justResumed = false;
	public boolean drawDebug = false;

	public int refreshRate;
	public final Random random = new Random();
	public BitmapFont InstrumentSerif;
	public BitmapFont foo;
	public InputMultiplexer multiplexer;
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
		setScreen(new Hills(this));
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
						if (screen instanceof Level) {
							Level level = (Level) screen;
							level.start(true);
						}
						return true;
					case Input.Keys.GRAVE:
						drawDebug = !drawDebug;
						return true;
					case Input.Keys.NUM_1:
						screen.dispose();
						setScreen(new Hills(game));
						return true;
					case Input.Keys.NUM_2:
						screen.dispose();
						setScreen(new TestGrounds(game));
						return true;
				}
				return false;
			}
			@Override
			public boolean touchDown(int screenX, int screenY, int pointer, int button) {
				if (platform != ApplicationType.Desktop) {
					if (screenX <= Gdx.graphics.getWidth() * 0.08) {
						if (screenY <= Gdx.graphics.getHeight() * 0.125) {
							//replaceScreen(new MainMenu(game));
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
			screen.dispose();
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
		if (justResumed) {
			justResumed = false;
		}
		else if (!isPaused) {
			deltaTime = Gdx.graphics.getDeltaTime();
		}
		
		screen.render(deltaTime);
		
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
	@Override
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
	@Override
	public com.badlogic.gdx.Screen getScreen() {
		return screen;
	}
}
