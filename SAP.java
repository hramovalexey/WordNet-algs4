import edu.princeton.cs.algs4.Digraph;
import edu.princeton.cs.algs4.In;
import edu.princeton.cs.algs4.Queue;
import edu.princeton.cs.algs4.StdOut;

import java.util.ArrayList;
import java.util.HashMap;

public class SAP {
    private final Digraph InDig; //Input Digraph
    private final HashMap<ArrayList<Integer>, ArrayList<Integer>> resultTable;
    // [query], [key, lenght]

    // constructor takes a digraph (not necessarily a DAG)
    public SAP(Digraph G) {
        if (G == null) throw new IllegalArgumentException("Digraph is null");
        InDig = new Digraph(G);
        resultTable = new HashMap<ArrayList<Integer>, ArrayList<Integer>>();
    }

    private class Bfs {
        private boolean[] marked;
        private final Queue<Integer> queue;
        private int[] edgeNum; // number of edges behind (length)
        private int key; // current key
        // private int currentKey;

        public Bfs(int startKey) {
            this.queue = new Queue<Integer>();
            this.marked = new boolean[InDig.V()];
            this.edgeNum = new int[InDig.V()];
            this.queue.enqueue(startKey);
            this.marked[startKey] = true;
            this.edgeNum[startKey] = 0;
        }

        private int nextSearch(int minLength) {
            if (queue.isEmpty()) {
                return key = -1;
            }

            key = queue.dequeue();
            if (minLength == -1 || edgeNum[key] <= minLength) {
                for (int i : InDig.adj(key)) {
                    if (!marked[i]) {
                        marked[i] = true;
                        queue.enqueue(i);
                        edgeNum[i] = edgeNum[key] + 1;
                    }
                }
            }
            return key;
        }

        private int currentEdgeNum() {
            return edgeNum[key];
        }
    }

    // Helper create [x, y] ArrayList
    private ArrayList<Integer> CreateArrayXY(int x, int y) {
        ArrayList<Integer> returnArray = new ArrayList<Integer>(2);
        returnArray.add(x);
        returnArray.add(y);
        return returnArray;
    }

    // Helper Chek vw or wv present?
    // vw = 1 wv = -1 notpresent = 0
    private int vw(int v, int w) {
        if (resultTable.containsKey(CreateArrayXY(v, w))) return 1;
        if (resultTable.containsKey(CreateArrayXY(w, v))) return -1;
        return 0;
    }

    // Helper for PathPut or lenght calc (returns lenght if keys coinside or -1)
    private int PathPut(HashMap<Integer, Integer> Path, Bfs bfs, int minLength) {
        int newKey = bfs.nextSearch(minLength);
        if (newKey == -1) return -1;
        if (!Path.containsKey(newKey)) {
            Path.put(newKey, bfs.currentEdgeNum());
            return -1;
        }
        else {
            return Path.get(newKey) + bfs.currentEdgeNum();
        }
    }

    // Helper for resultPut (put and return put length)
    private int resultTablePut(int lenght, Bfs bfs, int query1, int query2) {
        int doesContain = resultTableContainsKey(query1, query2);
        int currentTableLength = resultTableGetLength(query1, query2);
        if ((doesContain == 1 || doesContain == -1) && (currentTableLength == -1
                || currentTableLength > lenght)) {
            ArrayList<Integer> query;
            if (doesContain == -1) query = CreateArrayXY(query2, query1);
            else query = CreateArrayXY(query1, query2);
            currentTableLength = lenght;
            ArrayList<Integer> keyLenght = CreateArrayXY(bfs.key, lenght);
            resultTable.put(query, keyLenght);
        }
        return currentTableLength;
    }

    // resultTableContainsKey
    private int resultTableContainsKey(int query1, int query2) {
        if (resultTable.containsKey(CreateArrayXY(query1, query2))) return 1;
        if (resultTable.containsKey(CreateArrayXY(query2, query1))) return -1;
        return 0;
    }

    private int resultTableGetLength(int query1, int query2) {
        if (resultTableContainsKey(query1, query2) == -1)
            return resultTable.get(CreateArrayXY(query2, query1)).get(1);
        if (resultTableContainsKey(query1, query2) == 1)
            return resultTable.get(CreateArrayXY(query1, query2)).get(1);
        return -1;
    }

    // length of shortest ancestral path between v and w; -1 if no such path
    public int length(int v, int w) {
        if (v < 0 || v >= InDig.V() || w < 0 || w >= InDig.V())
            throw new IllegalArgumentException("arg not in range");
        if (vw(v, w) == 1) return resultTable.get(CreateArrayXY(v, w)).get(1);
        if (vw(v, w) == -1) return resultTable.get(CreateArrayXY(w, v)).get(1);
        int lenght = -1;
        ArrayList<Integer> query = CreateArrayXY(v, w);
        ArrayList<Integer> keyLenght = CreateArrayXY(-1, lenght);
        resultTable.put(query, keyLenght);
        Bfs vSearch = new Bfs(v);
        Bfs wSearch = new Bfs(w);

        // Path Map<Key, Number of edges behind>
        HashMap<Integer, Integer> Path = new HashMap<Integer, Integer>();
        int minLength = lenght;
        while (!(vSearch.key == -1 && wSearch.key == -1)) {
            lenght = PathPut(Path, vSearch, minLength);
            if (lenght != -1) {
                minLength = resultTablePut(lenght, vSearch, v, w);
                // break;
            }
            lenght = PathPut(Path, wSearch, minLength);
            if (lenght != -1) {
                minLength = resultTablePut(lenght, wSearch, v, w);
                // break;
            }
        }

        return resultTable.get(query).get(1);
    }

    // Helper for Get result (what 0 - ancestor, 1 - length)
    private int getResult(int v, int w, int what) {
        if (vw(v, w) == 1) return resultTable.get(CreateArrayXY(v, w)).get(what);
        if (vw(v, w) == -1) return resultTable.get(CreateArrayXY(w, v)).get(what);
        return -1;
    }

    // a common ancestor of v and w that participates in a shortest ancestral path; -1 if no such path
    public int ancestor(int v, int w) {
        if (v < 0 || v >= InDig.V() || w < 0 || w >= InDig.V())
            throw new IllegalArgumentException("arg not in range");
        if (vw(v, w) == 1) return resultTable.get(CreateArrayXY(v, w)).get(0);
        if (vw(v, w) == -1) return resultTable.get(CreateArrayXY(w, v)).get(0);
        this.length(v, w);
        return resultTable.get(CreateArrayXY(v, w)).get(0);
    }

    // Check that iterables in range
    private boolean inRange(Iterable<Integer> v) {
        boolean inR = true;
        if (v == null) return false;
        for (Integer i : v) {
            if (i == null || i < 0 || i >= InDig.V()) {
                inR = false;
                break;
            }
        }

        return inR;
    }

    // length of shortest ancestral path between any vertex in v and any vertex in w; -1 if no such path
    public int length(Iterable<Integer> v, Iterable<Integer> w) {
        if (v == null || w == null || !inRange(v) || !inRange(w))
            throw new IllegalArgumentException("length arg not in range");

        int minLength = -1;
        for (int i : v) {
            for (int j : w) {
                int l = length(i, j);
                if (minLength == -1 && l != -1) minLength = l;
                if (l != -1) minLength = Math.min(minLength, l);
            }
        }
        return minLength;
    }

    // a common ancestor that participates in shortest ancestral path; -1 if no such path
    public int ancestor(Iterable<Integer> v, Iterable<Integer> w) {
        if (v == null || w == null || !inRange(v) || !inRange(w))
            throw new IllegalArgumentException("ancestor arg not in range");
        int minLength = -1;
        int ancestor = -1;
        for (int i : v) {
            for (int j : w) {
                int l = length(i, j);
                if (minLength == -1 && l != -1) {
                    minLength = l;
                    ancestor = getResult(i, j, 0);
                }
                if (l != -1 && minLength > l) {
                    minLength = l;
                    ancestor = getResult(i, j, 0);
                }
            }
        }
        return ancestor;
    }


    public static void main(String[] args) {
        In in = new In(args[0]);
        Digraph G = new Digraph(in);
        SAP sap = new SAP(G);
        ArrayList<Integer> a = new ArrayList<Integer>();
        a.add(3);
        a.add(9);
        a.add(7);
        a.add(1);
        ArrayList<Integer> b = new ArrayList<Integer>();
        b.add(11);
        b.add(12);
        b.add(2);
        b.add(6);
        StdOut.println(sap.length(a, b));
        StdOut.println(sap.ancestor(a, b));
    }
}

