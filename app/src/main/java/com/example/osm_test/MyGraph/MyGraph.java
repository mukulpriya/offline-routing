package com.example.osm_test.MyGraph;

import org.jgrapht.graph.*;
import org.jgrapht.*;
import android.util.Log;
import org.jgrapht.alg.shortestpath.*;
import org.osmdroid.views.overlay.Polyline;
import org.osmdroid.util.GeoPoint;

import android.content.Context;
import android.util.Pair;


import java.util.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.File;
import java.io.FileWriter;

import java.time.Duration;
import java.time.Instant;



public class MyGraph {
    public static Graph<String, DefaultWeightedEdge> SampleGraph =
            new DefaultUndirectedWeightedGraph<String, DefaultWeightedEdge>(DefaultWeightedEdge.class);

    public static HashMap<String, Double[]> vertexToCordinateMap = new HashMap<String, Double[]>();

    public static DijkstraShortestPath sp;

    public static List<String> v_set = new ArrayList<String>();

    public static long graphBuildTime;


    public MyGraph() {


        Log.d("constructor", "inside the constructor");


    }

    public void buildGraph(Context ctx) {

        Instant start = Instant.now();

        int resId = ctx.getResources().getIdentifier(
                "vertex_file",
                "raw",
                ctx.getPackageName()
        );

        readRawTextFile(ctx, resId, true);

        resId = ctx.getResources().getIdentifier(
                "edge_file_level_10",
                "raw",
                ctx.getPackageName()
        );

        readRawTextFile(ctx, resId, false);

        Instant end = Instant.now();

        Duration timeElapsed = Duration.between(start, end);

        graphBuildTime = timeElapsed.toMillis();

        Log.d("Time to build the graph", Long.toString(graphBuildTime));

        sp = new DijkstraShortestPath(SampleGraph);


    }

    public Polyline getShortestPath (String source, String destination) {

        GraphPath shortest_path = sp.getPath(source, destination);

        Polyline line = convertIntoPolyline(shortest_path);
        return line;
    }

    private Polyline convertIntoPolyline (GraphPath path) {
        Polyline line = new Polyline();
        List v_list = path.getVertexList();
        List<GeoPoint> geoPoints = new ArrayList<>();


        v_list.forEach((item) -> {
            Double [] coords = vertexToCordinateMap.get(item);

            if (coords != null) {
                GeoPoint point = new GeoPoint((coords[1]), coords[0]);
                geoPoints.add(point);
            }
        });

        line.setPoints(geoPoints);

        return line;
    }


    private void readRawTextFile(Context ctx, int resId, boolean vertex)
    {
        InputStream inputStream = ctx.getResources().openRawResource(resId);

        InputStreamReader inputreader = new InputStreamReader(inputStream);
        BufferedReader buffreader = new BufferedReader(inputreader);
        String line;

        try {
            while (( line = buffreader.readLine()) != null) {
                String[] splitStr = line.split("\\s+");
               // Log.d("file output", line);

                if (vertex) {
                    v_set.add(splitStr[0]);
                    SampleGraph.addVertex(splitStr[0]);
                    vertexToCordinateMap.put(splitStr[0], new Double[] {Double.parseDouble(splitStr[1]), Double.parseDouble(splitStr[2])});
                } else {

                    SampleGraph.addEdge(splitStr[0], splitStr[1]);
                    SampleGraph.setEdgeWeight(splitStr[0], splitStr[1], Double.parseDouble(splitStr[2]));
                }
            }
        } catch (IOException e) {

            Log.e("error reading file", e.toString());
        }
    }

    public void  generateRandomPairFile(Context ctx) {

        File dir = new File(ctx.getFilesDir(), "mydir");
        if(!dir.exists()){
            dir.mkdir();
        }
        String body = "";
        int size = v_set.size();
        HashMap<Pair<String, String>, Double> pairs = new HashMap<Pair<String, String>, Double>();
        Random rand = new Random();
        Integer count = 20000;
        while (count > 0) {
            int source = rand.nextInt(size);
            int dest = rand.nextInt(size);

            if (source != dest) {
                String s = v_set.get(source);
                String d = v_set.get(dest);
                 if (pairs.get(new Pair<>(s, d)) == null) {
                     GraphPath shortest_path = sp.getPath(s, d);
                     List v_list = shortest_path.getVertexList();
                     Double weight = shortest_path.getWeight();

                     String l = s + " " + d + " " + weight.toString() + "\n";
                     Log.d("count", count.toString() );
                     body = body + l;
                     pairs.put(new Pair<>(s,d), weight);
                 }
            }
            count--;
        }

        try {
            File gpxfile = new File(dir, "randomPairs.txt");
            FileWriter writer = new FileWriter(gpxfile);
            writer.append(body);
            writer.flush();
            writer.close();
            Log.d("File Writing Done", "YES");
        } catch (Exception e){
            e.printStackTrace();
        }

    }

    public void generatePathDataFile (Context ctx) {

        File dir = new File(ctx.getFilesDir(), "mydir");
        if(!dir.exists()){
            dir.mkdir();
        }

        int resId = ctx.getResources().getIdentifier(
                "common_pairs",
                "raw",
                ctx.getPackageName()
        );
        InputStream inputStream = ctx.getResources().openRawResource(resId);

        InputStreamReader inputreader = new InputStreamReader(inputStream);
        BufferedReader buffreader = new BufferedReader(inputreader);

        String line;

        String body = "";

        try {
            while (( line = buffreader.readLine()) != null) {
                String[] splitStr = line.split("\\s+");

                Instant start = Instant.now();

                GraphPath shortest_path = sp.getPath(splitStr[0], splitStr[1]);
                Instant end = Instant.now();
                Duration timeElapsed = Duration.between(start, end);

                Double weight = shortest_path.getWeight();
                Integer vertexInPath = shortest_path.getVertexList().size();

                long durationInMillis = timeElapsed.toMillis();

                String l = splitStr[0] + ' ' + splitStr[1] + ' ' + weight.toString() + ' ' + vertexInPath.toString() + ' ' + Double.toString(durationInMillis) + '\n';
                Log.d("path entry", l);
                body = body + l;
            }
        } catch (IOException e) {

            Log.e("error reading common pairs file", e.toString());
        }

        try {
            File gpxfile = new File(dir, "level_100_data.txt");
            FileWriter writer = new FileWriter(gpxfile);
            writer.append(body);
            writer.flush();
            writer.close();
            Log.d("File Writing Done", "YES");
        } catch (Exception e){
            e.printStackTrace();
        }
    }
}
