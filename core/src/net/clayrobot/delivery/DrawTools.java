package net.clayrobot.delivery;


import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.Transform;
import com.badlogic.gdx.utils.Array;

public class DrawTools {

	private static Array<Fixture> fixtureList;
	private static PolygonShape shape;
	private static int vertexCount;
	private static float[] vertices;
	private static Vector2 currentVertex = new Vector2();
	private static Transform currentTransform;
	private static final Color DEFAULT_COLOR = Color.WHITE;
	private static Delivery game;

	public static void fillBody(Body body, Color color) {
		game = Delivery.getGame();
		fixtureList = body.getFixtureList();
		currentTransform = body.getTransform();
		game.shapeDrawer.setColor(color);
		for (Fixture fixture : fixtureList) {
			shape = (PolygonShape) fixture.getShape();
			vertexCount = shape.getVertexCount();
			vertices = new float[vertexCount * 2];
			for (int i = 0; i < vertexCount; i++) {
				shape.getVertex(i, currentVertex);
				currentTransform.mul(currentVertex);
				vertices[i * 2] = currentVertex.x;
				vertices[i * 2 + 1] = currentVertex.y;
			}
			game.shapeDrawer.filledPolygon(vertices);
		}
		game.shapeDrawer.setColor(DEFAULT_COLOR);
	}
	public static void fillBody(Body body) {
		DrawTools.fillBody(body, DEFAULT_COLOR);
	}/*
	public static void fillPolygon(Fixture fixture, Color color) {
		drawer.setColor(color);
		shape = (PolygonShape) fixture.getShape();
			vertexCount = shape.getVertexCount();
			vertices = new float[vertexCount * 2];
			for (int i = 0; i < vertexCount; i++) {
				shape.getVertex(i, currentVertex);
				vertices[i * 2] = currentVertex.x;
				vertices[i * 2 + 1] = currentVertex.y;
			}
			drawer.filledPolygon(vertices);
			drawer.setColor(DEFAULT_COLOR);
	}
	public static void fillPolygon(Fixture fixture) {
		fillPolygon(fixture, DEFAULT_COLOR);
	}
	public static void fillPolygon(PolygonShape shape, Color color) {
		drawer.setColor(color);
		vertexCount = shape.getVertexCount();
		vertices = new float[vertexCount * 2];
		for (int i = 0; i < vertexCount; i++) {
			shape.getVertex(i, currentVertex);
			vertices[i * 2] = currentVertex.x;
			vertices[i * 2 + 1] = currentVertex.y;
		}
		drawer.filledPolygon(vertices);
		drawer.setColor(DEFAULT_COLOR);
	}
	public static void fillPolygon(PolygonShape shape) {
		fillPolygon(shape, DEFAULT_COLOR);
	}*/
}
