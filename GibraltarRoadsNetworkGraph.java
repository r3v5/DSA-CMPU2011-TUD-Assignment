/*
Gibraltar's roads network graph

Graph properties:
Vertices (V): 429
Edges (E): 578
Edge weight: Length of road segment in meters

*/

import java.io.*;
import java.util.Scanner;


/*
 * Implementation of Min Heap
 */
class Heap {
    // heap array
    private int[] a;	
    
    // hPos[h[k]] == k, tracks the positions of elements in the heap
    private int[] hPos;

    // dist[v] = priority of v, stores the distances (priority)
    private int[] dist;    

    // heap size
    private int N;      
   
    // The heap constructor gets passed from the Graph:
    //    1. maximum heap size
    //    2. reference to the dist[] array
    //    3. reference to the hPos[] array
    public Heap(int maxSize, int[] _dist, int[] _hPos) {
        N = 0;

        // Heap array with maxSize + 1 for 1-based indexing
        a = new int[maxSize + 1];

        // hPos array to track positions
        hPos = _hPos;

        // dist array for priorities
        dist = _dist; 
    }


    // check if the heap is empty
    public boolean isEmpty() {
        return N == 0;
    }


    /*
    * siftUp from position k. The node value at position k
    * may be greater than its parent at k/2
    * k is a position in the heap array a
    */
    public void siftUp(int k) {   
        // vertex on a heap after insertion (typically at the very end)
        int v = a[k];

        // priority of the current vertex
        int vDist = dist[v]; 

        // move up current vertex while not reached dummy element by index 0 and priority is less than parent's priority (since min heap)
        while (k > 1 && vDist < dist[a[k / 2]]) {

            // change current vertex with parent
            a[k] = a[k / 2]; 

            // update the position of parent on the heap
            hPos[a[k]] = k;

            // go to the next parent
            k /= 2;
        }

        // finally place the vertex into the heap array by index where we stopped
        a[k] = v;

        // update the position on the heap for current vertex we sifted up
        hPos[v] = k;
    }


    /*
     * Key of node at position k may be less than that of its children and may need to be moved down some levels
     * k is a position in the heap array a
     */
    public void siftDown(int k) {
        int j;

        // vertex on a heap we want to sift down
        int v = a[k];

        // priority of the current vertex
        int vDist = dist[v];

        // go while node at pos k has a left child node
        while (2 * k <= N) {

            // index of left child on the heap
            j = 2 * k;

            // Choose the smaller child to compare with: 
            // if right child exists and has lower priority than left child, increment j to point to right child (since min heap)
            if (j < N && dist[a[j]] > dist[a[j + 1]]) {
                ++j;
            }
            
            // Stop the loop if current vertex's priority is already less than or equal to its child's priority (since min heap)
            if (vDist <= dist[a[j]]) {
                break;
            }
            
            // change current vertex with child (could be left or right)
            a[k] = a[j];

            // update the position of child on the heap
            hPos[a[k]] = k;
            
            // go to the child and start sifting down from him
            k = j;
        }
        
        // place the vertex into the heap array by index where we stopped
        a[k] = v;

        // update the position on the heap for current vertex we sifted down
        hPos[v] = k;
    }

    // insert the vertex at the end of the heap
    public void insert(int x) {
        a[++N] = x;

        // move up the vertex from the last position on the heap
        siftUp(N);
    }

    public int remove() {   
        // Place the root (min) into dummy position by index 0
        a[0] = a[1];

        // v is no longer in heap
        hPos[a[0]] = 0;
    
        // Move last element to root and shrink heap
        a[1] = a[N--];
        
        // If heap is not empty
        if (!isEmpty()) {
            // update the position in heap of new root
            hPos[a[1]] = 1;
            siftDown(1);
        }

        return a[0];
    }
}


class Graph {
    class Node {
        public int vertex;
        public int wgt;
        public Node next;

        public Node(int vertex, int wgt, Node next) {
            this.vertex = vertex;
            this.wgt = wgt;
            this.next = next;
        }
    }
    
    // V = number of vertices
    private int V;

    // E = number of edges
    private int E;

    // adj[] is the adjacency list array of nodes. This is an array of linked lists.
    private Node[] adj;

    // sentinel node
    private Node z;    
    
    // default constructor
    public Graph(String graphFile)  throws IOException {
        int u, v;
        int e;
        int wgt;

        FileReader fr = new FileReader(graphFile);
		BufferedReader reader = new BufferedReader(fr);
	           
        String splits = " +";  // multiple whitespace as delimiter
		String line = reader.readLine();        
        String[] parts = line.split(splits);
        System.out.println("Parts[] = " + parts[0] + " " + parts[1]);
        
        V = Integer.parseInt(parts[0]);
        E = Integer.parseInt(parts[1]);
        
        // create sentinel node
        z = new Node(0, 0, null); 
        z.next = z;
        
        // create adjacency lists, initialised to sentinel node z       
        adj = new Node[V+1];        
        for(v = 1; v <= V; ++v)
            adj[v] = z;      
            
       // read the edges
        System.out.println("Reading edges from text file");
        for(e = 1; e <= E; ++e)
        {
            line = reader.readLine();
            parts = line.split(splits);
            u = Integer.parseInt(parts[0]);
            v = Integer.parseInt(parts[1]); 
            wgt = Integer.parseInt(parts[2]);
            
            System.out.println("Edge " + u + " --(" + wgt + "m)-- " + v); 
            
            // insert node in front of adjacency list for vertex u
            adj[u] = new Node(v, wgt, adj[u]);

            // insert node in front of adjacency list for vertex v (since undirected)
            adj[v] = new Node(u, wgt, adj[v]);
        }	   
        
        System.out.println("\nBuilding adjacency list representation, which is very efficient for sparse graphs.");
        reader.close();
    }
   
    
    // method to display the graph representation
    public void display() {
        int v;
        Node n;
        
        for(v=1; v<=V; ++v){
            System.out.print("\nadj[" + v + "] ->" );
            for(n = adj[v]; n != z; n = n.next) 
                System.out.print(" |" + n.vertex + " | " + n.wgt + "| ->");    
        }
        System.out.println("");
    }

    public void SPT_Dijkstra(int s) {
        int v, d;
        int totalEdgesInSpt = 0;

        // Record start time
        long startTime = System.nanoTime();

        // Measure memory before
        Runtime runtime = Runtime.getRuntime();
        runtime.gc();
        long usedMemoryBefore = runtime.totalMemory() - runtime.freeMemory();

        // Stores best known distance from s to every vertex
        int[] dist = new int[V + 1];

        // Stores the predecessor on the shortest path for each vertex
        int[] parent = new int[V + 1]; 

        // Position of vertex in the heap
        int[] hPos = new int[V + 1];
        Node u;

        // Initialize dist, parent, and hPos arrays (1-indexed arrays)
        for (v = 1; v <= V; ++v) {
            dist[v] = Integer.MAX_VALUE;
            parent[v] = 0;
            hPos[v] = 0;
        }
        
        // Distance to root s is 0
        dist[s] = 0;

        Heap h =  new Heap(V, dist, hPos);

        // Start from vertex s
        h.insert(s);

        System.out.println("\nStarting SPT Dijkstra's algorithm: \n");
        System.out.println("Start from source vertex: " + s + 
                           ", dist = " + dist[s] + 
                           ", hPos[s] = " + hPos[s]);
        
        while (!h.isEmpty()) {

            // pop the vertex with minimal distance from source from the heap
            v = h.remove();

            if (v != s) {
                ++totalEdgesInSpt;
            }
            
            System.out.println("Removed from heap: " + "vertex " + v + 
                           ", dist = " + dist[v]);
            
            // For each neighbor u of v
            for (u = adj[v]; u != z; u = u.next) {
                d = u.wgt;
                if (dist[v] + d < dist[u.vertex]) {
                    dist[u.vertex] = dist[v] + d;
                    parent[u.vertex] = v;
                
                    // Not yet in heap
                    if (hPos[u.vertex] == 0) {
                        h.insert(u.vertex);
                    } else {
                        // if in the heap, should be sifted up since the priority was updated by new minimal weight
                        System.out.println("Called siftUp() on vertex: " + u.vertex);
                        h.siftUp(hPos[u.vertex]);
                    }
                }
            }

            System.out.print("dist[]: ");
            for (int i = 1; i <= V; i++) System.out.print(i + "=" + (dist[i] == Integer.MAX_VALUE ? "∞" : dist[i]) + "  ");
            System.out.println();

            System.out.print("hPos[]: ");
            for (int i = 1; i <= V; i++) System.out.print(i + "=" + hPos[i] + "  ");
            System.out.println();

            System.out.print("parent[]: ");
            for (int i = 1; i <= V; i++) System.out.print(i + "=" + parent[i] + "  ");
            System.out.println("\n");
        }

        System.out.print("After running Dijkstra’s SPT Algorithm on Gibraltar's Roads Network: \n");
        System.out.print("Number of vertices connected in SPT = " + V + "\n");
        System.out.print("Number of edges in SPT = " + totalEdgesInSpt + " (should be equal to V - 1)" + "\n");
        System.out.println("\nShortest Path Tree as it is built is:\n");
        System.out.printf("%-8s %-8s %-20s\n", "Vertex", "Parent", "Distance from source " + s + " (m)");

        for (int i = 1; i <= V; i++) {
            System.out.printf("%-8s %-8s %-15s\n", 
                i, 
                (parent[i] == 0 ? "-" : parent[i]), 
                (dist[i] == Integer.MAX_VALUE ? "∞" : dist[i] + "m"));
        }

        // Record end time
        long endTime = System.nanoTime();

        // Measure memory after
        long usedMemoryAfter = runtime.totalMemory() - runtime.freeMemory();

        // Calculate and print results
        double timeInMillis = (endTime - startTime) / 1_000_000.0;
        long memoryUsedKB = (usedMemoryAfter - usedMemoryBefore) / 1024;

        System.out.printf("\nExecution Time: %.3f ms\n", timeInMillis);
        System.out.printf("Memory Usage: %d KB\n", memoryUsedKB);
    }
}


public class GibraltarRoadsNetworkGraph {
    public static void main(String[] args) throws IOException {
        System.out.print("Student name: Ian Miller\n");
        System.out.print("Student number: D23124620\n");
        Scanner scanner = new Scanner(System.in);

        // Prompt for file name
        System.out.print("\nEnter graph file name (eg. gibraltar.txt): ");
        String fname = scanner.nextLine();

        // Prompt for starting vertex
        System.out.print("Enter starting vertex (as a number, e.g., 1): ");
        int s = scanner.nextInt();
        
        // Load and construct graph
        Graph g = new Graph(fname);
        g.display();

        System.out.print("\nPreparing for running Dijkstra's Shortest Path Tree Algorithm on Gibraltar's Roads Network Graph \n"); 
        System.out.print("The weight between two nodes (roads) represents the distance in meters\n"); 
        g.SPT_Dijkstra(s);  
        System.out.print("\nTime complexity: O(V + E log V), Space complexity: O(V + E)\n");
    }
}
