package net.clayrobot.delivery;

import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.maps.MapObjects;
import com.badlogic.gdx.maps.objects.PolygonMapObject;
import com.badlogic.gdx.maps.objects.RectangleMapObject;
import com.badlogic.gdx.math.Polygon;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.World;

public class MapSpawner {
	private float restitution = 0.03f; // default physical values for map terrain for now
	private float friction = 0.9f;
	public MapSpawner(float terrainFriction, float terrainRestitution) { // creates MapSpawner with specified physical values
		friction = terrainFriction;
		restitution = terrainRestitution;
	}
	public MapSpawner(float terrainFriction) {
		friction = terrainFriction;
	}
	public MapSpawner() {}
	public void TerrainSpawner(MapObjects terrainMapObjects, World world, BodyDef bodyDef) {
		TerrainSpawner(terrainMapObjects, 1, world, bodyDef);
	}
	public void TerrainSpawner(MapObjects terrainMapObjects, float scale, World world, BodyDef bodyDef) {
		bodyDef.position.set(0, 0);
		Body terrainBody = world.createBody(bodyDef);
		PolygonShape terrainShape = new PolygonShape(); // this will hold the shape at the current index recorded from the tmx file
		FixtureDef fixtureDef = new FixtureDef();
		fixtureDef.shape = terrainShape; // this is a reference to an updating terrain shape that is finalized when a new fixture is created
		fixtureDef.restitution = restitution;
		fixtureDef.friction = friction;

		for (MapObject mapObject : terrainMapObjects) {
			if (mapObject instanceof PolygonMapObject) {
				Polygon polygon = ((PolygonMapObject) mapObject).getPolygon();
				polygon.setPosition(polygon.getX() * scale, polygon.getY() * scale);
				polygon.setScale(scale, scale);
				terrainShape.set(polygon.getTransformedVertices());
			}
			else if (mapObject instanceof RectangleMapObject) {
				Rectangle rectangle = ((RectangleMapObject) mapObject).getRectangle();
				rectangle.setPosition(rectangle.getX() * scale + rectangle.getWidth() * scale / 2, rectangle.getY() * scale + rectangle.getHeight() * scale / 2);
				Vector2 newPosition = new Vector2();
				rectangle.getPosition(newPosition);
				terrainShape.setAsBox(rectangle.getWidth() * scale / 2, rectangle.getHeight() * scale / 2, newPosition, 0);
			}
			terrainBody.createFixture(fixtureDef);
		}
		terrainShape.dispose();
	}
}
