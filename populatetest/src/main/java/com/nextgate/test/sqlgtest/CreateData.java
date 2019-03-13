/*
 * 
 */

package com.nextgate.test.sqlgtest;

import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal;
import org.apache.tinkerpop.gremlin.structure.Graph;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.umlg.sqlg.structure.SqlgGraph;

/**
 * Class to load fictitious data to try to cause issues.
 * 
 * @author kevin.schmidt
 */
public class CreateData implements Runnable {

    private final int instance;
    private final Graph g;

    public CreateData(Graph g, int instance) {
        this.instance = instance;
        this.g = g;
    }

    @Override
    public void run() {
        for (int i = 0; i < 10; i++) {
            for (int j = 0; j < 100.0 * Math.sqrt(this.instance); j++) {
                GraphTraversal<Vertex, Vertex> gt = g.traversal().V().hasLabel("Test" + this.instance).has("value", j - 1);

                Vertex priorV = null;

                if (gt.hasNext()) {
                    priorV = gt.next();
                }

                Vertex v = g.addVertex("Test" + this.instance);
                v.property("value", i);
                v.property("value" + System.currentTimeMillis() % 10, true);
//                g.tx().commit();

                if (priorV != null) {
                    v.addEdge("prior", priorV, "value" + System.currentTimeMillis() % 100, this.instance);
                }

                g.tx().commit();

                if (j % 10 == 0) {
                    System.out.println(this.instance + ": " + j);
                }
            }

            Vertex v = g.addVertex("Test" + System.currentTimeMillis());
//            g.tx().commit();
            for (int j = 0; j < 10; j++) {
                v.property("value" + j, System.currentTimeMillis());
            }
            g.tx().commit();
            System.out.println("New vertex added");
        }
    }

    public static void main(String[] args) {

        Graph sqlg = SqlgGraph.open("sqlgconfig.properties");

        for (int i = 1; i <= 10; i++) {
            CreateData cd = new CreateData(sqlg, i);

            Thread t = new Thread(cd);

            t.start();
        }
    }
}
