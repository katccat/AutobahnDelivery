package net.clayrobot.delivery;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.Transform;
import com.badlogic.gdx.utils.Array;

public class DrawTools {
	private DrawTools() {}
	private static Array<Fixture> fixtureList;
	private static PolygonShape shape;
	private static int vertexCount;
	private static float[] vertices;
	private static Vector2 currentVertex = new Vector2();
	private static Transform currentTransform;
	private static final Color DEFAULT_COLOR = Color.WHITE;

	public static void fillBody(Body body, Color color) { // draws box2d bodies as solid colors
		AutobahnDelivery game = AutobahnDelivery.getGame(); // used to get game's shapeDrawer object
		fixtureList = body.getFixtureList();
		currentTransform = body.getTransform();
		game.shapeDrawer.setColor(color);
		for (Fixture fixture : fixtureList) {
			shape = (PolygonShape) fixture.getShape();
			vertexCount = shape.getVertexCount();
			vertices = new float[vertexCount * 2]; // creates float array of vertices as shapeDrawer uses
			for (int i = 0; i < vertexCount; i++) {
				shape.getVertex(i, currentVertex); // puts contents of vertex at i into currentVertex Vector2
				currentTransform.mul(currentVertex); // transforms the vertex based on where the body is and its rotation
				vertices[i * 2] = currentVertex.x;
				vertices[i * 2 + 1] = currentVertex.y;
			}
			game.shapeDrawer.filledPolygon(vertices); // finally draws the shape
		}
		game.shapeDrawer.setColor(DEFAULT_COLOR); // resets shapeDrawer's current color
	}
	public static void fillBody(Body body) {
		DrawTools.fillBody(body, DEFAULT_COLOR);
	}
}
