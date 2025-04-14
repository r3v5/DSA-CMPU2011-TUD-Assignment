// Simple weighted graph representation 
// Uses an Adjacency Linked Lists, suitable for sparse graphs

import java.io.*;
import java.util.Queue;
import java.util.LinkedList;
import java.util.Scanner;

enum C {White, Grey, Black};

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
        // Save the root (min)
        int v = a[1];

        // v is no longer in heap
        hPos[v] = 0;
    
        // Move last element to root and shrink heap
        a[1] = a[N--];
        
        // If heap is not empty
        if (!isEmpty()) {
            // update the position in heap of new root
            hPos[a[1]] = 1;
            siftDown(1);
        }

        return v;
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

    // list for representing MST
    private int[] mst;
    
    // used for traversing graph to mark vertices already visited
    private C[] colour;

    /*
     * a global timer (or counter) used during DFS to timestamp each vertex
     * It's incremented each time a vertex is discovered or finished
     */
    private int time;

    // for stroring predecessor during DFS/BFS forest (or tree) traversal, where each node points back to the node that discovered it.
    private int[] parent;

    /*
     * depth-first search also timestamps each vertex. 
     * Each vertex  has two timestamps: 
        1. the first timestamp - d records when is first discovered (and grayed), 
        2. the second timestamp - f records when the search finishes examining’s adjacency list (and blackens). 
     */

    /*
    * In DFS: d[u] stores the **discovery time** of vertex u when mark node u as GREY
    * In BFS: d[u] stores the **distance** from the source vertex s to vertex u (i.e., number of edges)
    */
    private int[] d;

    // timestamps when mark node as BLACK used in DFS
    private int[] f;
    
    
    // default constructor
    public Graph(String graphFile)  throws IOException {
        int u, v;
        int e, wgt;

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
        
        colour = new C[V+1];
        parent = new int[V+1];
        d = new int[V+1];
        f = new int[V+1];

        // create mst array for storing MST
        mst = new int[V + 1];
        
       // read the edges
        System.out.println("Reading edges from text file");
        for(e = 1; e <= E; ++e)
        {
            line = reader.readLine();
            parts = line.split(splits);
            u = Integer.parseInt(parts[0]);
            v = Integer.parseInt(parts[1]); 
            wgt = Integer.parseInt(parts[2]);
            
            System.out.println("Edge " + toChar(u) + "--(" + wgt + ")--" + toChar(v));   
            
            // insert node in front of adjacency list for vertex u
            adj[u] = new Node(v, wgt, adj[u]);

            // insert node in front of adjacency list for vertex v (since undirected)
            adj[v] = new Node(u, wgt, adj[v]);
        }	   
        
        System.out.println("\nBuilding adjacency list representation, which is very efficient for sparse graphs.");
        reader.close();
    }
   
    // convert vertex into char for pretty printing
    private char toChar(int u) {  
        return (char)(u + 64);
    }
    
    // method to display the graph representation
    public void display() {
        int v;
        Node n;
        
        for(v=1; v<=V; ++v){
            System.out.print("\nadj[" + toChar(v) + "] ->" );
            for(n = adj[v]; n != z; n = n.next) 
                System.out.print(" |" + toChar(n.vertex) + " | " + n.wgt + "| ->");    
        }
        System.out.println("");
    }

    // method to initialise Depth First Traversal of Graph (Cormem's version)
    public void DF(int s) {
        for (int u = 1; u <= V; ++u) {
            colour[u] = C.White;
            System.out.print("\nVertex " + toChar(u) + " is marked as White\n");
            parent[u] = 0;
        }

        System.out.print("\nStarting Depth First Graph Traversal Cormen's version\n");
        System.out.println("Starting with Vertex " + toChar(s) + " prompted by user");

        // start from vertex s, with 0 as the "previous" node
        time = 0;
        dfVisit(s);
    }

    // Recursive Depth First Traversal for adjacency lists and colouring (Cormem's version)
    private void dfVisit(int u) {
        ++time;
        d[u] = time;
        colour[u] = C.Grey;

        if (parent[u] != 0) {
            System.out.println("Visited vertex " + toChar(u) + " from " + toChar(parent[u]) + 
                               " | Discovery time: " + d[u] + " | Vertex " + toChar(u) + " is marked as Grey");
        } else {
            System.out.println("\nDF just visited starting vertex " + toChar(u) + 
                               " | Discovery time: " + d[u] + " | Vertex " + toChar(u) + " is marked as Grey");
        }

        // process all the vertices u connected to vertex v
        for (Node v = adj[u]; v != z; v = v.next) {
            if (colour[v.vertex] == C.White) {
                parent[v.vertex] = u;
                dfVisit(v.vertex); 
            }
        }

        colour[u] = C.Black;
        ++time;
        f[u] = time;

        System.out.println("Finished vertex " + toChar(u) + 
                       " | Finish time: " + f[u] + " | Vertex " + toChar(u) + " is marked as Black");
    }

    public void breadthFirst(int s) {
        int u;
        int currQueueSize;
        int traversalLevel = 1;

        // Mark all vertices as white at the beginning
        for (u = 1; u <= V; ++u) {
            colour[u] = C.White;
            System.out.print("\nVertex " + toChar(u) + " is marked as White\n");
            parent[u] = 0;
        }

        // Run BFS from source s
        colour[s] = C.Grey;
        d[s] = 0;
        parent[s] = 0;

        Queue<Integer> queue = new LinkedList<>();
        queue.add(s);

        System.out.println("\nStarting Breadth First Traversal Cormen's version\n");
        System.out.println("Starting with Vertex " + toChar(s) + " prompted by user");
        displayQueue(queue);

        while (!queue.isEmpty()) {
            // Get the size of the current queue to be able to count traversal levels further
            currQueueSize = queue.size();

            System.out.println("Current traversal level: " + traversalLevel);

            // Examine each vertex in the queue
            for (int i = 0; i < currQueueSize; ++i) {
                u = queue.remove();

                if (parent[u] != 0) {
                    System.out.println("Visited vertex " + toChar(u) + " from " + toChar(parent[u]) + 
                                        " | Distance from source " + toChar(s) + " to vertex " + toChar(u) + " is " + d[u] + (d[u] == 1 ? " edge" : " edges") + 
                                        " | Vertex " + toChar(u) + " was marked as Grey");
                } else {
                    System.out.println("\nBF just visited starting vertex " + toChar(u) + 
                                        " | Distance from source " + toChar(s) + " to vertex " + toChar(u) + " is " + d[u] + (d[u] == 1 ? " edge" : " edges") + 
                                        " | Vertex " + toChar(u) + " was marked as Grey");
                }
                
                // For each neighbour v of u
                for (Node v = adj[u]; v != z; v = v.next) {
                    // If v is not visited yet, mark as Grey and enqueue to the queue
                    if (colour[v.vertex] == C.White) {
                        colour[v.vertex] = C.Grey;
                        d[v.vertex] = d[u] + 1;
                        parent[v.vertex] = u;
                        queue.add(v.vertex);
                        System.out.println("Vertex " + toChar(v.vertex) + " is enqued to the queue | ");
                        displayQueue(queue);
                    }
                }

                // Mark u as Black since processed all adjacent neighbours
                colour[u] = C.Black;
                System.out.println("Vertex " + toChar(u) + " is marked as Black\n");
            }

            // Only move to the next level if more nodes were enqueued to the Queue
            if (!queue.isEmpty()) {
                ++traversalLevel;
            }
        }
        
        System.out.println("Total traversal levels in graph processed: " + traversalLevel);
    }

    // display vertices as characters in current queue
    private void displayQueue(Queue<Integer> queue) {
        System.out.print("Queue now: [ ");
        for (int v : queue) {
            System.out.print(toChar(v) + " ");
        }
        System.out.println("]");
    }

    // display traversal tree to see from what vertex we reached current vertex
    public void displayTraversalTree(int source) {
        System.out.println("\nTraversal Tree (parent array):");
        for (int u = 1; u <= V; ++u) {
            if (parent[u] != 0)
                System.out.println(toChar(u) + " ← " + toChar(parent[u]));
            else if (u == source)
                System.out.println(toChar(u) + " ← Root of tree");
            else
                System.out.println(toChar(u) + " ← - (not reachable from source)");
        }
    }
    

	public void MST_Prim(int s) {
        int v;
        int totalEdgesInMst = 0;
        int wgtSum = 0;
        int[] dist = new int[V + 1];
        int[] parent = new int[V + 1]; 
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

        System.out.println("\nStarting MST Prim’s algorithm: \n");
        System.out.println("Start from source vertex: " + toChar(s) + 
                           ", dist = " + dist[s] + 
                           ", hPos[s] = " + hPos[s] +
                           ", current total MST weight = " + wgtSum);
        
        while (!h.isEmpty()) {

            // pop the vertex with minimal weigth from the heap
            v = h.remove();

            // increase the weight sum by the priority of current vertex
            wgtSum += dist[v];

            // mark current vertex as presented in MST (set negative priority)
            dist[v] = -dist[v];
            
            // if we added non source vertex, increase the total number of edges constructed in MST
            if (v != s) {
                ++totalEdgesInMst;
            }

            System.out.println("Removed from heap: " + "vertex " + toChar(v) + 
                           ", dist = " + dist[v] + 
                           ", current total MST weight = " + wgtSum);
            
            // For each neighbor u of v
            for (u = adj[v]; u != z; u = u.next) {

                // If not presented in MST and found an edge with weight that is smaller than current vertex's weight 
                // Negative priority of vertext means already added to MST
                if (dist[u.vertex] > 0 && u.wgt < dist[u.vertex]) {
                    dist[u.vertex] = u.wgt;
                    parent[u.vertex] = v;
                
                    // Not yet in heap
                    if (hPos[u.vertex] == 0) {
                        h.insert(u.vertex);
                    } else {
                        // if in the heap, should be sifted up since the priority was updated by new minimal weight
                        System.out.println("Called siftUp() on vertex: " + toChar(u.vertex));
                        h.siftUp(hPos[u.vertex]);
                    }
                }
            }

            System.out.print("dist[]: ");
            for (int i = 1; i <= V; i++) System.out.print(toChar(i) + "=" + (dist[i] == Integer.MAX_VALUE ? "∞" : dist[i]) + "  ");
            System.out.println();

            System.out.print("hPos[]: ");
            for (int i = 1; i <= V; i++) System.out.print(toChar(i) + "=" + hPos[i] + "  ");
            System.out.println();

            System.out.print("parent[]: ");
            for (int i = 1; i <= V; i++) System.out.print(toChar(i) + "=" + toChar(parent[i]) + "  ");
            System.out.println("\n");
        }

        // Copy parent array to mst[] for use in showMST()
        for (v = 1; v <= V; v++) {
            mst[v] = parent[v];
        }
        
        System.out.print("\nThere are " + V + " vertices and " + E + " edges in the input graph\n");
        System.out.print("After running Prim’s MST Algorithm on Adjacency Lists: \n");
        System.out.print("Weight of MST = " + wgtSum + "\n");
        System.out.print("Number of vertices connected in MST = " + V + "\n");
        System.out.print("Number of edges in MST = " + totalEdgesInMst + " (should be equal to V - 1)" + "\n");
	}
    
    public void showMST() {
        System.out.print("\nEdges in Minimum Spanning Tree (parent -> child):\n");
    
        for (int v = 1; v <= V; ++v) {
            int p = mst[v];
            
            if (p != 0) { 
                // Find edge weight from p to v in adjacency list
                int weight = -1;
                for (Node n = adj[p]; n != z; n = n.next) {
                    if (n.vertex == v) {
                        weight = n.wgt;
                        break;
                    }
                }
    
                System.out.println(toChar(p) + " --(" + weight + ")--> " + toChar(v));
            }
        }
    }
    

    public void SPT_Dijkstra(int s) {
        int v, d;

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
        System.out.println("Start from source vertex: " + toChar(s) + 
                           ", dist = " + dist[s] + 
                           ", hPos[s] = " + hPos[s]);
        
        while (!h.isEmpty()) {

            // pop the vertex with minimal distance from source from the heap
            v = h.remove();
            
            System.out.println("Removed from heap: " + "vertex " + toChar(v) + 
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
                        System.out.println("Called siftUp() on vertex: " + toChar(u.vertex));
                        h.siftUp(hPos[u.vertex]);
                    }
                }
            }

            System.out.print("dist[]: ");
            for (int i = 1; i <= V; i++) System.out.print(toChar(i) + "=" + (dist[i] == Integer.MAX_VALUE ? "∞" : dist[i]) + "  ");
            System.out.println();

            System.out.print("hPos[]: ");
            for (int i = 1; i <= V; i++) System.out.print(toChar(i) + "=" + hPos[i] + "  ");
            System.out.println();

            System.out.print("parent[]: ");
            for (int i = 1; i <= V; i++) System.out.print(toChar(i) + "=" + toChar(parent[i]) + "  ");
            System.out.println("\n");
        }

        System.out.println("\nShortest Path Tree as it is built is:\n");
        System.out.printf("%-8s %-8s %-15s\n", "Vertex", "Parent", "Distance from " + toChar(s));

        for (int i = 1; i <= V; i++) {
            System.out.printf("%-8s %-8s %-15s\n", 
                            toChar(i), 
                            (parent[i] == 0 ? "-" : toChar(parent[i])), 
                            (dist[i] == Integer.MAX_VALUE ? "∞" : dist[i]));
        }
    }
}

public class GraphSolution {
    public static void main(String[] args) throws IOException {
        System.out.print("Student name: Ian Miller\n");
        System.out.print("Student number: D23124620\n");
        Scanner scanner = new Scanner(System.in);

        // Prompt for file name
        System.out.print("\nEnter graph file name (eg. wGraph.txt): ");
        String fname = scanner.nextLine();

        // Prompt for starting vertex
        System.out.print("Enter starting vertex (as a number, e.g., 1 for A): ");
        int s = scanner.nextInt();
        
        // Load and construct graph
        Graph g = new Graph(fname);
        g.display();
       
        System.out.print("\n1) Preparing for DFS Traversal Cormen's version with colouring\n"); 
        g.DF(s);
        g.displayTraversalTree(s);
        System.out.print("\nTime complexity: O(V + E), Space complexity: O(V)\n"); 

        System.out.print("\n2) Preparing for BFS Traversal Cormen's version with colouring\n"); 
        g.breadthFirst(s);
        g.displayTraversalTree(s);
        System.out.print("\nTime complexity: O(V + E), Space complexity: O(V)\n"); 
        
        System.out.print("\n3) Preparing for running Prim’s MST Algorithm on Adjacency Lists\n"); 
        g.MST_Prim(s);   
        g.showMST();
        System.out.print("\nTime complexity: O(E log V), Space complexity: O(V + E)\n"); 

        System.out.print("\n4) Preparing for running Dijkstra's Shortest Path Tree Algorithm on Adjacency Lists\n"); 
        g.SPT_Dijkstra(s);  
        System.out.print("\nTime complexity: O(V + E log V), Space complexity: O(V + E)\n");              
    }
}
