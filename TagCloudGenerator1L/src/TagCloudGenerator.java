import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Comparator;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Scanner;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;


public final class TagCloudGenerator {

    /**
     * basic constructor.
     */
    private TagCloudGenerator() {

    }

    /**
     * max size for font.
     */
    private static final int MAXFONT = 48;

    /**
     * min size for font.
     */
    private static final int MINFONT = 11;

    /**
     * comparator for string that does not depend on capitalization.
     */
    public static final class StringLT implements Comparator<String> {
        @Override
        public int compare(String s1, String s2) {
            String s3 = s1.toLowerCase();
            String s4 = s2.toLowerCase();
            return s3.compareTo(s4);
        }
    }

    /**
     * Creates a set from a character array.
     *
     * @param args
     *            character array to be turned into the set.
     * @return - set form of character array.
     */
    public static TreeSet<Character> createSet(Character... args) {
        TreeSet<Character> s = new TreeSet<>();
        for (int i = 0; i < args.length; i++) {
            s.add(args[i]);
        }
        return s;
    }

    /**
     * Takes line and reads it to the end of the string or the next seperator.
     *
     * @param line
     *            line to be read from
     * @param seperators
     *            list of seperators
     * @return - the next word in line
     */
    public static String nextWord(String line, TreeSet<Character> seperators) {
        String word = "";
        for (int i = 0; i < line.length() && !seperators.contains(line.charAt(i)); i++) {
            word = word.concat("" + line.charAt(i));
        }
        return word;
    }

    /**
     * adds the word to the map, if the map already has the word it will
     * increment the value.
     *
     * @param map
     *            map for word to be added.
     * @param word
     *            word to be added to the map.
     */
    public static void addToMap(TreeMap<String, Integer> map, String word) {
        Integer count = map.put(word.toLowerCase(), 1);
        if (count != null) {
            count++;
            map.put(word.toLowerCase(), count);
        }
    }

    /**
     * reads the number of words (numWords) from the file (fileName) and puts
     * them into a mapping with keys as the words and values for the number of
     * times each word is present.
     *
     * @param inFileName
     *            file to be read from.
     * @param numWords
     *            number of words to be read.
     * @return - mappings of the words and there frequencies
     */
    public static TreeMap<String, Integer> readFile(String inFileName, int numWords) {
        TreeSet<Character> seperators = createSet(' ', '\t', '\n', '\r', '`', '-', '\\',
                '/', '(', ')', ',', '.', '!', '?', '[', ']', '\'', ';', ':', '"', '_',
                '*');
        TreeMap<String, Integer> map = new TreeMap<>();

        BufferedReader inFile;
        try {
            inFile = new BufferedReader(new FileReader(inFileName));
        } catch (IOException e) {
            System.err.println("Error when trying to open read file " + inFileName);
            return null;
        }

        try {
            String line = inFile.readLine();
            while (line != null) {
                while (line.length() > 0 && seperators.contains(line.charAt(0))) {
                    line = line.substring(1);
                }
                if (line.length() > 0) {
                    String word = nextWord(line, seperators);
                    line = line.substring(word.length());
                    addToMap(map, word);
                }
                line = inFile.readLine();
            }
        } catch (IOException e) {
            System.err.println("Error when reading from read file " + inFileName);
        }

        try {
            inFile.close();
        } catch (IOException e) {
            System.err.println("Error when closing file, read but not closed");
        }

        return map;
    }

    /**
     * returns the entry with the smallest value from the set.
     *
     * @param entries
     *            Set of map.entry<String, Integer>
     * @return - the smallest entry in entries.
     */
    private static Entry<String, Integer> findSmallest(
            Set<Map.Entry<String, Integer>> entries) {

        Iterator<Map.Entry<String, Integer>> it = entries.iterator();
        Map.Entry<String, Integer> smallestEntry = it.next();
        while (it.hasNext()) {
            Map.Entry<String, Integer> currentEntry = it.next();
            int cVal = currentEntry.getValue();
            int sVal = smallestEntry.getValue();
            if (cVal < sVal) {
                smallestEntry = currentEntry;
            }
        }

        return smallestEntry;
    }

    /**
     * Trims the map down to only hold the pairs with highest number of times
     * used. the size of the map should be the int numWords.
     *
     * @param wordMap
     *            the map to be trimmed.
     * @param numWords
     *            number of key values in the map at the end.
     */
    private static void trimMap(TreeMap<String, Integer> wordMap, int numWords) {
        Set<Map.Entry<String, Integer>> entries = wordMap.entrySet();
        while (entries.size() > numWords) {
            Entry<String, Integer> smallest = findSmallest(entries);
            entries.remove(smallest);
        }
    }

    /**
     * checks if the given string is a positive integer greater than 0 returning
     * true if so.
     *
     * @param num
     *            the string to be checked
     * @return - a boolean returning true if num is a valid positive integer
     */
    private static boolean validPositiveInteger(String num) {
        boolean valid = false;
        if (num.matches("\\d+(\\.\\d+)?")) {
            int i = Integer.parseInt(num);
            valid = i > 0;
        }
        return valid;
    }

    /**
     * prints the header to the output for the html file.
     *
     * @param out
     *            SimpleWriter to be written to
     * @param inFileName
     *            name of the file being read from
     * @param numWords
     *            number of words read from the file with the name of inFileName
     */
    private static void printHeader(PrintWriter out, String inFileName, int numWords) {
        out.println("<html>");
        out.println("<head>");
        String title = "Top " + numWords + " words in " + inFileName;
        out.println("<title>" + title + "</title>");
        out.println("<link href=\"https://cse22x1.engineering.osu.edu/2231/web-"
                + "sw2/assignments/projects/tag-cloud-generator/data/tagcloud"
                + ".css\" rel=\"stylesheet\" type=\"text/css\">");
        out.println("<link href=\"tagcloud.css\" rel=\"stylesheet\" type=\"text/css\">");
        out.println("</head>");

        out.println("<body>");
        out.println("<h1>" + title + "</h1>");
        out.println("<hr>");
        out.println("<div class=\"cdiv\">");
        out.println("<p class=\"cbox\">");
    }

    /**
     * gets the largest value in the map.
     *
     * @param map
     *            map of values to be searched.
     * @return - the biggest value in the map.
     */
    private static int biggestVal(TreeMap<String, Integer> map) {
        Set<Map.Entry<String, Integer>> entries = map.entrySet();
        Iterator<Map.Entry<String, Integer>> it = entries.iterator();
        int biggestVal = it.next().getValue();
        while (it.hasNext()) {
            int currentVal = it.next().getValue();
            if (currentVal > biggestVal) {
                biggestVal = currentVal;
            }
        }
        return biggestVal;
    }

    /**
     * prints the body for the html file.
     *
     * @param out
     *            file to be written to
     * @param wordMap
     *            map of words and there frequencies
     */
    private static void printBody(PrintWriter out, TreeMap<String, Integer> wordMap) {
        //body of the  print
        int fontShift = biggestVal(wordMap) / (MAXFONT - MINFONT);
        while (wordMap.size() > 0) {
            String key = wordMap.firstKey();
            int count = wordMap.remove(key);
            int fontSize = count / fontShift + MINFONT;
            out.println("<span style=\"cursor:default\" class=\"f" + fontSize
                    + "\" title=\"count: " + count + "\">" + key + "</span>");
        }
        out.println("</p>");

        //printFooter
        out.println("</div>");
        out.println("</body>");
        out.println("<hr>");
        out.println("</html>");
    }

    /**
     * Main method.
     *
     * @param args
     *            the command line arguments
     */
    public static void main(String[] args) {
        @SuppressWarnings("resource")
        
        Scanner in = new Scanner(System.in);
        BufferedWriter console = new BufferedWriter(new PrintWriter(System.out));
        String inFileName = "ERROR";
        String outFileName = "ERROR";
        String numWordInput = "ERROR";


        // User Prompts
        try {
            console.write("Enter file for input: ");
            console.flush();
            inFileName = in.nextLine();
            console.write("Enter file for output: ");
            console.flush();
            outFileName = in.nextLine();
            console.write("Enter number of words to be read: ");
            console.flush();
            numWordInput = in.nextLine();
        } catch (IOException e) {
            System.err.println("Error console input/output IOException.");
        }
        while (!validPositiveInteger(numWordInput)) {
            throw new IllegalArgumentException(
                    "number of words to be read must be a positive integer");
        }
        int numWords = Integer.parseInt(numWordInput);

        //bulk of program
        TreeMap<String, Integer> wordMap = readFile(inFileName, numWords);
        trimMap(wordMap, numWords);

        PrintWriter outFile;
        try {
            outFile = new PrintWriter(new BufferedWriter(new FileWriter(outFileName)));
        } catch (IOException e) {
            System.err.println("Error opening output file " + outFileName);
            return;
        }

        printHeader(outFile, inFileName, wordMap.size());
        printBody(outFile, wordMap);
        outFile.close();

        //finish message and closed I/O streams
        try {
            console.write("Completed!");
            console.flush();
        } catch (IOException e) {
            System.err.println("Error console IOException thrown for completition prompt");
        }
        in.close();
        try {
            console.close();
        } catch (IOException e) {
            System.err.println("Error closeing console");
        }
    }
}
