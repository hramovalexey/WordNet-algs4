import edu.princeton.cs.algs4.In;
import edu.princeton.cs.algs4.StdOut;

public class Outcast {
    private final WordNet wn;

    // constructor takes a WordNet object
    public Outcast(WordNet wordnet) {
        wn = wordnet;
    }

    // given an array of WordNet nouns, return an outcast
    public String outcast(String[] nouns) {
        Node resultNode = new Node(0, null);
        for (String noun1 : nouns) {
            int distance = 0;
            for (int i = 0; i < nouns.length; i++) {
                String noun2 = nouns[i];
                distance += wn.distance(noun1, noun2);
            }
            Node tempNode = new Node(distance, noun1);
            if (resultNode.noun == null || resultNode.dist < tempNode.dist) resultNode = tempNode;

        }
        return resultNode.noun;
    }

    private class Node implements Comparable<Node> {
        private final int dist;
        private final String noun;

        public Node(int distance, String inNoun) {
            this.dist = distance;
            this.noun = inNoun;
        }

        public int compareTo(Node that) {
            return Integer.compare(this.dist, that.dist);
        }
    }

    public static void main(String[] args) {
        WordNet wordnet = new WordNet(args[0], args[1]);
        Outcast outcast = new Outcast(wordnet);
        for (int t = 2; t < args.length; t++) {
            In in = new In(args[t]);
            String[] nouns = in.readAllStrings();
            StdOut.println(args[t] + ": " + outcast.outcast(nouns));
        }
    }
}
