package net.clayrobot.delivery;

import com.badlogic.gdx.*;
import com.badlogic.gdx.Application.ApplicationType;
import com.badlogic.gdx.graphics.g2d.*;
import space.earlygrey.shapedrawer.*;
import com.badlogic.gdx.physics.box2d.*;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.maps.MapObjects;
import com.badlogic.gdx.maps.objects.PolygonMapObject;
import com.badlogic.gdx.maps.objects.RectangleMapObject;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.math.Polygon;
import com.badlogic.gdx.math.Rectangle;

import net.clayrobot.delivery.entities.*;

public class Level implements Screen {
	public final int WORLD_WIDTH = 200;
	public final int WORLD_HEIGHT = 120;
	public final int GROUND_HEIGHT = 8;
	TiledMap map;
	
	public World world;
	
	private Sprite groundSprite;
	private Sprite worldSprite;
	private Sprite backgroundSprite;
	private OrthographicCamera camera;
	
	private Player player;
	private Music music;
	private final boolean ENABLE_MUSIC = false;
	private int score;
	private int winningScore;
	private final Delivery game;
	public Level(Delivery game) {
		this.game = game;
		game.platform = Gdx.app.getType();
		if (game.platform == ApplicationType.Android || game.platform == ApplicationType.iOS) game.mobilePlatform = true;
		setupBox2d();
		spawnMapObjects();
		setupVisuals();
		camera = new OrthographicCamera();
		if (ENABLE_MUSIC) {
			music = Gdx.audio.newMusic(Gdx.files.internal("helvetica.mp3"));
			music.setLooping(true);
			music.play();
		}
		setupInputProcessor();
		start(false);
	}
	@Override
	public void show() {
		
	}
	@Override
	public void hide() {
		
	}
	@Override
	public void resize(int width, int height) {
		if (game.mobilePlatform) camera.setToOrtho(false, 50, 50 * ((float) height / width));
		else camera.setToOrtho(false, 30 * ((float) width / height), 30);
		camera.setToOrtho(false, 34 * ((float) width / height), 34);
		updateCamera();
	}
	
	private void setupBox2d() {
		world = new World(new Vector2(0, -10), true);
		game.activeWorld = world;
		world.setContactListener(new MyContactListener(this));
		game.dynamicBodyDef.type = BodyType.DynamicBody;
		game.staticBodyDef.type = BodyType.StaticBody;
		game.staticBodyDef.position.set(0, 0);
		//Body groundBody = world.createBody(game.staticBodyDef);
		//PolygonShape groundBox = new PolygonShape();
		//groundBox.setAsBox(WORLD_WIDTH / 2, GROUND_HEIGHT / 2);
		//FixtureDef fixtureDef = new FixtureDef();
		//fixtureDef.shape = groundBox;
		//fixtureDef.restitution = 0.03f;
		//fixtureDef.friction = 0.9f;
		//groundBody.createFixture(fixtureDef);
		//groundBox.dispose();
	}
	private void spawnMapObjects() {
		map = game.mapLoader.load("untitled.tmx");
		MapObjects houseMapObjects = map.getLayers().get("Houses").getObjects();
		House.spawn(houseMapObjects);
		MapObjects TerrainMapObjects = map.getLayers().get("Terrain").getObjects();
		game.staticBodyDef.position.set(0, 0);
		Body terrainBody = world.createBody(game.staticBodyDef);
		PolygonShape terrainShape = new PolygonShape();
		FixtureDef fixtureDef = new FixtureDef();
		fixtureDef.shape = terrainShape;
		fixtureDef.restitution = 0.03f;
		fixtureDef.friction = 0.9f;
		
		for (MapObject mapObject : TerrainMapObjects) {
			if (mapObject instanceof PolygonMapObject) {
				Polygon polygon = ((PolygonMapObject) mapObject).getPolygon();
				polygon.setPosition(polygon.getX() / 4, polygon.getY() / 4);
				polygon.setScale(0.25f, 0.25f);
				terrainShape.set(polygon.getTransformedVertices());
			}
			terrainBody.createFixture(fixtureDef);
		}
		terrainShape.dispose();
	}
	protected void start(boolean restarting) {
		if (restarting) Entities.clear();
		if (game.mobilePlatform || restarting) {
			new Billboard(8, GROUND_HEIGHT + 10, 10, 10, new Texture("propaganda/GRIT.png"), new Texture("propaganda/GRIT2.png"));
		}
		else {
			new Billboard(8, GROUND_HEIGHT + 10, 10, 10, new Texture("propaganda/WADSLR.png"), new Texture("propaganda/WADSLR2.png"));
			//new Billboard(8, GROUND_HEIGHT + 10, 10, 10, new Texture("propaganda/level1map.png"));
		}
		
		player = new Player(25, GROUND_HEIGHT + 5, true);
		winningScore = 8;
		Box.spawn(15, GROUND_HEIGHT, winningScore);
		if (game.isPaused) game.resume();
	}
	private void setupVisuals() {
		Texture worldTex = new Texture("level1creepy.png");
		worldTex.setAnisotropicFilter(0);
		worldSprite = new Sprite(worldTex);
		worldSprite.setBounds(0, 0, WORLD_WIDTH, WORLD_HEIGHT);
		
	}
	
	
	@Override
	public void render(float deltaTime) {
		ScreenUtils.clear(1, 1, 1, 1);
		//backgroundSprite.draw(game.batch);
		if (deltaTime > 0) {
			world.step(deltaTime, 6, 2);
			updateCamera();
		}
		worldSprite.draw(game.batch);
		Entities.draw(deltaTime);
		
		if (game.drawDebug) game.debugRenderer.render(world, camera.combined);
		if (score >= winningScore) {
			game.pause();
			winningScore += 1;
		}
	}
	public void incrementScore() {
		score++;
		System.out.println(score);
	}
	public void decrementScore() {
		score--;
	}
	private void updateCamera() {
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
		camera.update(false);
		game.batch.setProjectionMatrix(camera.combined);
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
		Entities.clear();
		world.dispose();
		map.dispose();
		game.multiplexer.removeProcessor(1);
		if (ENABLE_MUSIC) music.dispose();
		//groundSprite.getTexture().dispose();
		worldSprite.getTexture().dispose();
	}
	private void setupInputProcessor() {
		InputAdapter GameInputProcessor = new InputAdapter() {
			private int propellingPointer = -1;
			private int clawingPointer = -1;
			@Override
			public boolean keyDown(int keycode) {
				switch (keycode) {
					case Input.Keys.W:
						player.setPropelling(true);
						break;
					case Input.Keys.A:
						player.setTiltLeft(true);
						break;
					case Input.Keys.D:
						player.setTiltRight(true);
						break;
					case Input.Keys.S:
						player.setClawing(true);
						break;
					case Input.Keys.L:
						player.releaseGrip();
						break;
					case Input.Keys.K:
						player.kill();
						player = new Player(25, GROUND_HEIGHT + 5, true);
						break;
				}
				return true;
			}
			@Override
			public boolean keyUp(int keycode) {
				switch (keycode) {
					case Input.Keys.W:
						player.setPropelling(false);
						break;
					case Input.Keys.A:
						player.setTiltLeft(false);
						break;
					case Input.Keys.D:
						player.setTiltRight(false);
						break;
					case Input.Keys.S:
						player.setClawing(false);
						break;
				}
				return true;
			}
			@Override
			public boolean touchDown(int screenX, int screenY, int pointer, int button) {
				if (game.platform == ApplicationType.Desktop) return true;
				if (screenX < Gdx.graphics.getWidth() / 2) {
					player.setPropelling(true);
					propellingPointer = pointer;
				}
				else if (!player.getGripping()) {
					player.setClawing(true);
					clawingPointer = pointer;
				}
				else {
					player.releaseGrip();
				}
				return true;
			}
			@Override
			public boolean touchUp(int screenX, int screenY, int pointer, int button) {
				if (game.platform == ApplicationType.Desktop) return true;
				if (pointer == propellingPointer) {
					player.setPropelling(false);
					propellingPointer = -1;
				}
				else if (pointer == clawingPointer) {
					player.setClawing(false);
					clawingPointer = -1;
				}
				return true;
			}
		};
		game.multiplexer.addProcessor(GameInputProcessor);
	}
}