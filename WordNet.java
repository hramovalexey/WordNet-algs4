import edu.princeton.cs.algs4.Digraph;
import edu.princeton.cs.algs4.In;
import edu.princeton.cs.algs4.StdOut;

import java.util.ArrayList;
import java.util.HashMap;

public class WordNet {
    private final HashMap<String, ArrayList<Integer>> Syn;

    private final SAP sap;
    private final HashMap<Integer, String> auxSyn; // helper Map for synset look for by number


    // constructor takes the name of the two input files
    public WordNet(String synsets, String hypernyms) {
        if (synsets == null || hypernyms == null) throw new IllegalArgumentException("Null arg");
        In InHyper = new In(hypernyms);
        final String[] inHyperArr = InHyper.readAllLines();
        Digraph Hyper;
        Hyper = makeHyp(inHyperArr);
        CycleDetect cycle = new CycleDetect(Hyper);
        if (cycle.hasCycle()) throw new IllegalArgumentException("No DAG digraph");
        In InSyn = new In(synsets);
        final String[] inSynArr = InSyn.readAllLines();
        Syn = makeSyn(inSynArr);
        auxSyn = makeAuxSyn(inSynArr);

        sap = new SAP(Hyper);
    }


    // Helper for constructor
    private Digraph makeHyp(String[] inHyperArr) {
        Digraph hyperReturn = new Digraph(inHyperArr.length);
        for (int i = 0; i < hyperReturn.V(); i++) {
            String[] tempSyn = inHyperArr[i].split(",");
            int length = tempSyn.length;
            int[] tempSynInt = new int[length];
            for (int k = 0; k < length; k++) {

                tempSynInt[k] = Integer.parseInt(tempSyn[k]);
            }
            for (int j = 1; j < length; j++) {
                hyperReturn.addEdge(tempSynInt[0], tempSynInt[j]);
            }
        }
        return hyperReturn;
    }


    // putToSyn (chek if already has such key & add to value array)
    private HashMap<String, ArrayList<Integer>> putToSyn(
            HashMap<String, ArrayList<Integer>> inputSyn, String noun, int number) {
        if (inputSyn.containsKey(noun)) {
            inputSyn.get(noun).add(number);
            return inputSyn;
        }
        ArrayList<Integer> putNumber = new ArrayList<Integer>();
        putNumber.add(number);
        inputSyn.put(noun, putNumber);
        return inputSyn;
    }


    // Helper for constructor (key - noun, val - number)
    private HashMap<String, ArrayList<Integer>> makeSyn(String[] inSynArr) {
        HashMap<String, ArrayList<Integer>> Syn = new HashMap<String, ArrayList<Integer>>(
                inSynArr.length / 5);

        for (int i = 0; i < inSynArr.length; i++) {
            String[] tempSyn = inSynArr[i].split(","); // {"81", "Abramis genus_Abramis"}
            int val = Integer.parseInt(tempSyn[0]); // 81
            //  System.out.println(key);
            String keys = tempSyn[1]; // "Abramis genus_Abramis"
            String[] nouns = keys.split(" "); // {"Abramis", "genus_Abramis"}
            for (String key : nouns) {
                //  System.out.println(val);
                Syn = putToSyn(Syn, key, val);
            }
        }
        return Syn;
    }

    // makeAuxSyn
    private HashMap<Integer, String> makeAuxSyn(String[] inSynArr) {
        HashMap<Integer, String> returnSyn = new HashMap<>(inSynArr.length / 5);
        for (String oneLine : inSynArr) {
            String[] lineArr = oneLine.split(",");
            returnSyn.put(Integer.parseInt(lineArr[0]), lineArr[1]);
        }
        return returnSyn;
    }


    // detects first cycle
    private class CycleDetect {
        private boolean isCycle; // true if has at least 1 cycle
        private boolean[] marked; // visited vert
        private boolean[] onStack; // cycle detector

        public CycleDetect(Digraph G) {
            this.isCycle = false;
            this.marked = new boolean[G.V()];
            this.onStack = new boolean[G.V()];
            for (int v = 0; v < G.V(); v++) {
                if (!marked[v]) {
                    dfs(G, v);
                }
            }
        }

        private void dfs(Digraph G, int v) {
            onStack[v] = true;
            marked[v] = true;
            for (int w : G.adj(v)) {
                if (w == v) continue;
                if (onStack[w]) {
                    isCycle = true;
                }
                if (isCycle) return;
                else if (!marked[w])
                    dfs(G, w);
            }
            onStack[v] = false;
        }

        private boolean hasCycle() {
            return isCycle;
        }

    }


    // returns all WordNet nouns
    public Iterable<String> nouns() {
        return Syn.keySet();
    }

    // is the word a WordNet noun?
    public boolean isNoun(String word) {
        if (word == null) throw new IllegalArgumentException("null noun at isNoun");
        return Syn.containsKey(word);
    }

    // distance between nounA and nounB (defined below)
    public int distance(String nounA, String nounB) {
        if (!isNoun(nounA) || !isNoun(nounB))
            throw new IllegalArgumentException("Not in WordNet noun");
        if (isNoun(nounA) && isNoun(nounB)) {
            // if (sap == null) sap = new SAP(Hyper);
            ArrayList<Integer> i1 = Syn.get(nounA);
            ArrayList<Integer> i2 = Syn.get(nounB);
            return sap.length(i1, i2);
        }
        return -1;
    }

    // a synset (second field of synsets.txt) that is the common ancestor of nounA and nounB
    // in a shortest ancestral path (defined below)
    public String sap(String nounA, String nounB) {
        if (!isNoun(nounA) || !isNoun(nounB))
            throw new IllegalArgumentException("Not in WordNet noun");
        // if (sap == null) sap = new SAP(Hyper);
        ArrayList<Integer> i1 = Syn.get(nounA);
        ArrayList<Integer> i2 = Syn.get(nounB);
        int ancestor = sap.ancestor(i1, i2);

        return auxSyn.get(ancestor);

    }

    public static void main(String[] args) {
        WordNet Word = new WordNet("synsets.txt", "hypernyms.txt");
        StdOut.println(Word.distance("worm",
                                     "bird"));
        StdOut.println(Word.sap("worm",
                                "bird"));
    }
}
