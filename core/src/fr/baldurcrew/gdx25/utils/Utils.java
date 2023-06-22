package fr.baldurcrew.gdx25.utils;

import com.badlogic.gdx.math.Polygon;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.Shape;

import java.util.ArrayList;
import java.util.List;

public class Utils {

    public static boolean inside(Vector2 cp1, Vector2 cp2, Vector2 p) {
        return (cp2.x - cp1.x) * (p.y - cp1.y) > (cp2.y - cp1.y) * (p.x - cp1.x);
    }

    public static Vector2 intersection(Vector2 cp1, Vector2 cp2, Vector2 s, Vector2 e) {
        Vector2 dc = new Vector2(cp1.x - cp2.x, cp1.y - cp2.y);
        Vector2 dp = new Vector2(s.x - e.x, s.y - e.y);
        float n1 = cp1.x * cp2.y - cp1.y * cp2.x;
        float n2 = s.x * e.y - s.y * e.x;
        float n3 = 1.0f / (dc.x * dp.y - dc.y * dp.x);
        return new Vector2((n1 * dp.x - n2 * dc.x) * n3, (n1 * dp.y - n2 * dc.y) * n3);
    }

    /// Only handle polygons
    /// Original at https://www.iforce2d.net/b2dtut/
    public static ArrayList<Vector2> getIntersection(Fixture fixtureA, Fixture fixtureB) {
        if (fixtureA.getShape().getType() != Shape.Type.Polygon ||
                fixtureB.getShape().getType() != Shape.Type.Polygon)
            return null;

        PolygonShape polygonA = (PolygonShape) fixtureA.getShape();
        PolygonShape polygonB = (PolygonShape) fixtureB.getShape();

        final var intersection = new ArrayList<Vector2>();

        // fill subject polygon from fixtureA polygon
        for (int i = 0; i < polygonA.getVertexCount(); i++) {
            Vector2 vertex = new Vector2();
            polygonA.getVertex(i, vertex);
            vertex = fixtureA.getBody().getWorldPoint(vertex);
            intersection.add(new Vector2(vertex));
        }

        // fill clip polygon from fixtureB polygon
        final var clipPolygon = new ArrayList<Vector2>();
        for (int i = 0; i < polygonB.getVertexCount(); i++) {
            Vector2 vertex = new Vector2();
            polygonB.getVertex(i, vertex);
            vertex = fixtureB.getBody().getWorldPoint(vertex);
            clipPolygon.add(new Vector2(vertex));
        }

        Vector2 cp1 = clipPolygon.get(clipPolygon.size() - 1);
        for (int j = 0; j < clipPolygon.size(); j++) {
            Vector2 cp2 = clipPolygon.get(j);

            if (intersection.isEmpty())
                return null;

            final var inputList = new ArrayList<Vector2>(intersection);
            intersection.clear();

            Vector2 s = inputList.get(inputList.size() - 1);
            for (int i = 0; i < inputList.size(); i++) {
                Vector2 e = inputList.get(i);
                if (inside(cp1, cp2, e)) {
                    if (!inside(cp1, cp2, s)) {
                        intersection.add(intersection(cp1, cp2, s, e));
                    }
                    intersection.add(e);
                } else if (inside(cp1, cp2, s)) {
                    intersection.add(intersection(cp1, cp2, s, e));
                }
                s = e;
            }
            cp1 = cp2;
        }

        return intersection;
    }

    public static Polygon getPolygon(List<Vector2> vectors) {
        float[] flattened = new float[vectors.size() * 2];
        for (int i = 0; i < vectors.size(); i++) {
            flattened[2 * i] = vectors.get(i).x;
            flattened[2 * i + 1] = vectors.get(i).y;
        }
        return new Polygon(flattened);
    }
}
