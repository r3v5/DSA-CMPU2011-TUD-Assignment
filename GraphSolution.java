// Simple weighted graph representation 
// Uses an Adjacency Linked Lists, suitable for sparse graphs

import java.io.*;
import java.util.Queue;
import java.util.Scanner;

enum C {White, Grey, Black};

class Heap
{
    private int[] a;	   // heap array
    private int[] hPos;	   // hPos[h[k]] == k
    private int[] dist;    // dist[v] = priority of v

    private int N;         // heap size
   
    // The heap constructor gets passed from the Graph:
    //    1. maximum heap size
    //    2. reference to the dist[] array
    //    3. reference to the hPos[] array
    public Heap(int maxSize, int[] _dist, int[] _hPos) 
    {
        N = 0;
        a = new int[maxSize + 1];
        dist = _dist;
        hPos = _hPos;
    }


    public boolean isEmpty() 
    {
        return N == 0;
    }


    public void siftUp( int k) 
    {
        int v = a[k];

        // code yourself
        // must use hPos[] and dist[] arrays
    }


    public void siftDown( int k) 
    {
        int v, j;
       
        v = a[k];  
        
        // code yourself 
        // must use hPos[] and dist[] arrays
    }


    public void insert( int x) 
    {
        a[++N] = x;
        siftUp( N);
    }


    public int remove() 
    {   
        int v = a[1];
        hPos[v] = 0; // v is no longer in heap
        a[N+1] = 0;  // put null node into empty spot
        
        a[1] = a[N--];
        siftDown(1);
        
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

    // adj[] is the adjacency lists array
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

    // for stroring DFS forest (or tree), where each node points back to the node that discovered it.
    private int[] parent;

    /*
     * depth-first search also timestamps each vertex. 
     * Each vertex  has two timestamps: 
        1. the first timestamp - d records when is first discovered (and grayed), 
        2. the second timestamp - f records when the search finishes examining’s adjacency list (and blackens). 
     */

    // timestamps when mark node as GREY 
    private int[] d;

    // timestamps when mark node as BLACK
    private int[] f;
    
    
    // default constructor
    public Graph(String graphFile)  throws IOException
    {
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
        
        reader.close();
    }
   
    // convert vertex into char for pretty printing
    private char toChar(int u)
    {  
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
    public void DF(int s) 
    {
        for (int v = 1; v <= V; ++v) {
            colour[v] = C.White;
            parent[v] = -1;
        }

        System.out.print("\nDepth First Graph Traversal\n");
        System.out.println("Starting with Vertex " + toChar(s));

         // start from vertex s, with 0 as the "previous" node
        dfVisit(0, s);
    }

    // Recursive Depth First Traversal for adjacency list (Cormem's version)
    private void dfVisit( int prev, int u)
    {
        ++time;
        d[u] = time;
        colour[u] = C.Grey;

        if (parent[u] != -1) {
            System.out.println("Visited vertex " + toChar(u) + " from " + toChar(prev) + 
                               " | Discovery time: " + d[u]);
        } else {
            System.out.println("\nDF just visited starting vertex " + toChar(u) + 
                               " | Discovery time: " + d[u]);
        }

        // process all the vertices u connected to vertex v
        for (Node v = adj[u]; v != z; v = v.next) {
            if (colour[v.vertex] == C.White) {
                parent[v.vertex] = u;
                dfVisit(u, v.vertex); 
            }
        }

        colour[u] = C.Black;
        ++time;
        f[u] = time;

        System.out.println("Finished vertex " + toChar(u) + 
                       " | Finish time: " + f[u]);
    }


    
	public void MST_Prim(int s)
	{
        int v, u;
        int wgt, wgt_sum = 0;
        int[]  dist, parent, hPos;
        Node t;

        //code here
        
        //dist[s] = 0;
        
        //Heap h =  new Heap(V, dist, hPos);
        //h.insert(s);
        
        //while ( ...)  
        //{
            // most of alg here
            
       // }
        System.out.print("\n\nWeight of MST = " + wgt_sum + "\n");
        
                  		
	}
    
    public void showMST()
    {
            System.out.print("\n\nMinimum Spanning tree parent array is:\n");
            for(int v = 1; v <= V; ++v)
                System.out.println(toChar(v) + " -> " + toChar(mst[v]));
            System.out.println("");
    }

    public void SPT_Dijkstra(int s)
    {

    }

}

public class GraphSolution {
    public static void main(String[] args) throws IOException
    {
        Scanner scanner = new Scanner(System.in);

        // Prompt for file name
        System.out.print("Enter graph file name: ");
        String fname = scanner.nextLine();

        // Prompt for starting vertex
        System.out.print("Enter starting vertex (as a number, e.g., 1 for A): ");
        int s = scanner.nextInt();
        
        // Load and construct graph
        Graph g = new Graph(fname);
        g.display();
            
       g.DF(s);
       //g.breadthFirst(s);
       //g.MST_Prim(s);   
       //g.SPT_Dijkstra(s);               
    }
}
