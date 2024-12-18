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
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.PolygonSpriteBatch;
import com.badlogic.gdx.graphics.g2d.Sprite;
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


public class AutobahnDelivery extends Game {
	public PolygonSpriteBatch batch;
	private SpriteBatch fgBatch;
	public ShapeDrawer shapeDrawer;
	public Application.ApplicationType platform;
	public boolean mobilePlatform = false; // used to enable / disable touch controls
	
	public Box2DDebugRenderer debugRenderer;
	public BodyDef dynamicBodyDef = new BodyDef();
	public BodyDef staticBodyDef = new BodyDef();
	
	public boolean isPaused = false;
	private boolean justResumed = false; // used to set delta time to 0 after resume
	public boolean drawDebug = false;

	public int refreshRate;
	public final Random random = new Random();
	public BitmapFont APL333;
	public BitmapFont Redaction;
	public InputMultiplexer multiplexer;
	private static AutobahnDelivery game;
	public String displayText = "";
	private OrthographicCamera camera;
	public TmxMapLoader mapLoader;
	public OrthogonalTiledMapRenderer mapRenderer;
	private float stateTime = 0; // used so animations know where they are
	
	private Sprite overlaySprite;
	private Animation<Texture> overlayAnim;
	
	public static AutobahnDelivery getGame() {
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

		FreeTypeFontGenerator generator = new FreeTypeFontGenerator(Gdx.files.internal("fonts/APL333.ttf"));
		FreeTypeFontParameter parameter = new FreeTypeFontParameter();
		int resolution = 90;
		parameter.size = resolution;
		parameter.color = Color.WHITE;
		parameter.borderColor = Color.BLACK;
		parameter.borderWidth = 3;
		parameter.hinting = FreeTypeFontGenerator.Hinting.Medium; 
		APL333 = generator.generateFont(parameter);
		generator.dispose();
		
		generator = new FreeTypeFontGenerator(Gdx.files.internal("fonts/Redaction35-Regular.otf"));
		parameter.color = Color.BLACK;
		parameter.borderWidth = 0;
		parameter.hinting = FreeTypeFontGenerator.Hinting.None;
		Redaction = generator.generateFont(parameter);
		generator.dispose();
		
		refreshRate = Gdx.graphics.getDisplayMode().refreshRate;
		Gdx.graphics.setContinuousRendering(false);
		
		platform = Gdx.app.getType();
		mobilePlatform = (platform == ApplicationType.Android || platform == ApplicationType.iOS);
		
		
		dynamicBodyDef.type = BodyDef.BodyType.DynamicBody;
		staticBodyDef.type = BodyDef.BodyType.StaticBody;
		debugRenderer = new Box2DDebugRenderer();
		Texture[] overlayTex = { // this is the camera ui overlay
			new Texture("overlay1.png"),
			new Texture("overlay2.png")
		};
		overlaySprite = new Sprite(overlayTex[0]);
		overlayAnim = new Animation<>(1, overlayTex);
		overlayAnim.setPlayMode(Animation.PlayMode.LOOP);
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
						if (screen instanceof Level) { // only restarts if that is available (screen is of type level)
							Level level = (Level) screen;
							level.start();
						}
						return true;
					case Input.Keys.GRAVE:
						drawDebug = !drawDebug;
						return true;
					case Input.Keys.NUM_1:
						screen.dispose();
						setScreen(new Hills(game)); // press 1 to switch to hills level
						return true;
					case Input.Keys.NUM_2:
						screen.dispose();
						setScreen(new TestGrounds(game)); // press 2 to switch to old flat level
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
		multiplexer.addProcessor(UiInputProcessor); // a processor for character controls is added / removed as needed by levels 
		Gdx.input.setInputProcessor(multiplexer);
	}

	
	@Override
	public void dispose() { // this method clears memory. many objects in this game have a dispose method
		if (screen != null) {
			screen.hide();
			screen.dispose();
		}
		batch.dispose();
		APL333.dispose();
		for (Texture texture: overlayAnim.getKeyFrames()) {
			texture.dispose();
		}
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
			deltaTime = Gdx.graphics.getDeltaTime(); // only gives actual delta time if game is not paused and wasn't just resumed (avoids jumps)
		}
		
		screen.render(deltaTime); // renders screen object before rendering foreground elements
		fgBatch.begin();
		if (screen instanceof Level) {
			Level level = (Level) screen;
			int time = level.getTime();
			String timeString = String.format("%d:%02d", (time / 60), (time % 60));
			
			APL333.draw(fgBatch, timeString, camera.viewportWidth * (3f / 4), camera.viewportHeight * (7f / 8)); // this draws remaining time
			Redaction.draw(fgBatch, displayText, camera.viewportWidth * (3f / 4), camera.viewportHeight / 2); // this draws display text (usually box number)
		}
		//Meyrin.draw(fgBatch, "1", camera.viewportWidth / 2, camera.viewportHeight / 2);
		stateTime += deltaTime;
		overlaySprite.setTexture(overlayAnim.getKeyFrame(stateTime));
		overlaySprite.draw(fgBatch);
		fgBatch.end();
		if (!isPaused) Gdx.graphics.requestRendering();
	}

	@Override
	public void resize(int width, int height) {
		//camera.setToOrtho(false, 30 * ((float) width / height), 30);
		camera.setToOrtho(false, 480 * ((float) width / height), 480);
		overlaySprite.setBounds(0, 0, camera.viewportWidth, camera.viewportHeight);
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
