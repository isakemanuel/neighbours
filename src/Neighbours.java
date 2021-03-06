import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.Random;

import static java.lang.Math.round;
import static java.lang.Math.sqrt;
import static java.lang.System.*;

/*
 *  Program to simulate segregation.
 *  See : http://nifty.stanford.edu/2014/mccown-schelling-model-segregation/
 *
 * NOTE:
 * - JavaFX first calls method init() and then method start() far below.
 * - To test uncomment call to test() first in init() method!
 *
 */
// Extends Application because of JavaFX (just accept for now)
public class Neighbours extends Application {

    // Enumeration type for the Actors
    enum Actor {
        BLUE, RED, NONE   // NONE used for empty locations
    }

    // Enumeration type for the state of an Actor
    enum State {
        UNSATISFIED,
        SATISFIED,
        NA     // Not applicable (NA), used for NONEs
    }


    // Below is the *only* accepted instance variable (i.e. variables outside any method)
    // This variable may *only* be used in methods init() and updateWorld()
    Actor[][] world;              // The world is a square matrix of Actors

    // This is the method called by the timer to update the world
    // (i.e move unsatisfied) approx each 1/60 sec.
    void updateWorld() {
        // % of surrounding neighbours that are like me
        final double threshold = 0.7;
        State[][] states = getStates(world, threshold);
        getNextWorld(states, world);
        // TODO
    }

    // This method initializes the world variable with a random distribution of Actors
    // Method automatically called by JavaFX runtime (before graphics appear)
    // Don't care about "@Override" and "public" (just accept for now)
    @Override
    public void init() {
        //test();    // <---------------- Uncomment to TEST!

        // %-distribution of RED, BLUE and NONE
        double[] dist = {0.25, 0.25, 0.50};
        // Number of locations (places) in world (square)
        int nLocations = 900;
        int sideLength = (int) Math.sqrt(nLocations);


        // TODO
        world = new Actor[sideLength][sideLength];
        populateWorld(world, dist);
        shuffle(world);

        // Should be last
        fixScreenSize(nLocations);
    }


    // ------- Methods ------------------

    // TODO write the methods here, implement/test bottom up

    void populateWorld(Actor[][] realm, double[] distribution) {
        int nCells = realm.length * realm.length;
        int[] counts = new int[distribution.length];
        Actor[] types = Actor.values();
        for (int i = 0; i < distribution.length; i++) {
            counts[i] = (int) (distribution[i] * nCells);
        }
        for (int row = 0; row < realm.length; row++) {
            for (int col = 0; col < realm.length; col++) {
                realm[row][col] = Actor.NONE;
                for (int type = 0; type < types.length; type++) {
                    if (counts[type] > 0) {
                        realm[row][col] = types[type];
                        counts[type]--;
                        break;
                    }
                }
            }

        }
    }

    <T> void shuffle(T[][] matrix) {
        Random rand = new Random();
        int sideLength = matrix.length;
        int elements = sideLength * sideLength;

        int iCol, iRow, jCol, jRow, j;
        T tmp;

        for (int i = elements - 1; i > 1; i--) {
            j = rand.nextInt(i);

            iCol = i % sideLength;
            iRow = i / sideLength;
            jCol = j % sideLength;
            jRow = j / sideLength;

            tmp = matrix[iRow][iCol];
            matrix[iRow][iCol] = matrix[jRow][jCol];
            matrix[jRow][jCol] = tmp;

        }
    }

    State[][] getStates(Actor[][] world, double threshold){
        State[][] states = new State[world.length][world.length];

        for(int row = 0; row < world.length; row++){
            for(int col = 0; col < world.length; col++){
                states[row][col] = getState(world, row, col, threshold);
            }
        }

        return states;
    }

    private State getState(Actor[][] world, int row, int col, double threshold) {
        if (world[row][col] == Actor.NONE) {
            return State.NA;
        } else {

            double[] arr = getNeighbours(world, row, col);
            double neighbours = arr[0];
            double places = arr[1];

            if (neighbours / places >= threshold) {
                return State.SATISFIED;
            } else {
                return State.UNSATISFIED;
            }

        }

    }

    double[] getNeighbours(Actor[][] world, int row, int col) {

        double neighbours = - 1; // We will count our selves as a neighbour;
        double places = - 1; // We will count our selves as a place
        Actor self = world[row][col];

        for (int r = row - 1; r <= row + 1; r++) {
            if (0 <= r && r <= world.length -1) {
                for (int c = col - 1; c <= col + 1; c++) {
                    if (0 <= c && c <= world.length - 1) {
                        if (world[r][c] != Actor.NONE) {
                            places++;
                            if (world[r][c] == self) {
                                neighbours++;
                            }
                        }

                    }

                }
            }

        }
        double[] arr = {neighbours, places};

        return arr;




    }

    <T> void shuffle(T[] array) {
        Random rand = new Random();
        for (int i = array.length - 1; i > 1; i--) {
            int j = rand.nextInt(i);
            T tmp = array[i];
            array[i] = array[j];
            array[j] = tmp;

        }
    }

    void getNextWorld(State[][] states, Actor[][] current) {

        Integer[] unsatisfiedIndices = getIndices(states, State.UNSATISFIED);
        Integer[] emptyIndices = getIndices(states, State.NA);

        int size = current.length;

        shuffle(unsatisfiedIndices);
        shuffle(emptyIndices);

        int range = Math.min(unsatisfiedIndices.length, emptyIndices.length);

        for(int i = 0; i < range; i++){

            int emptyIndex = emptyIndices[i];

            int unsatisfiedIndex = unsatisfiedIndices[i];

            current[emptyIndex / size][emptyIndex % size] = current[unsatisfiedIndex / size][unsatisfiedIndex % size];

            current[unsatisfiedIndex / size][unsatisfiedIndex % size] = Actor.NONE;

        }


    }

    Integer[] getIndices(State[][] matris, State state) {

        State[] states = matrixToArray(matris);
        int size = 0;


        for (int i = 0; i < states.length; i++) {

            if (states[i] == state) {

                size++;

            }
        }

        Integer[] indices = new Integer[size];
        int counter = 0;

        for (int i = 0; i < states.length; i++) {

            if (states[i] == state) {

                indices[counter] = i;
                counter++;

            }
        }

        return indices;
    }


    <T> T[] matrixToArray(T[][] matrix) {
        Class<?> clazz = matrix[0][0].getClass();
        T[] array = (T[]) Array.newInstance(clazz, matrix.length * matrix[0].length);
        for (int row = 0; row < matrix.length; row++) {
            for (int col = 0; col < matrix[row].length; col++) {
                array[row * matrix[row].length + col] = matrix[row][col];
            }

        }
        return array;
    }

    <T> T[][] arrayToMatrix(T[] array) {
        int row, col, nColumns;
        Class<?> clazz = array[0].getClass();
        // We're assuming that this array is going to be converted to a square matrix
        nColumns = (int) Math.sqrt(array.length);
        T[][] matrix = (T[][]) Array.newInstance(clazz, nColumns, nColumns);
        for (int i = 0; i < array.length; i++) {
            col = i % nColumns;
            row = (i - col) / nColumns;
            matrix[row][col] = array[i];
        }
        return matrix;
    }


    // ------- Testing -------------------------------------

    // Here you run your tests i.e. call your logic methods
    // to see that they really work
    void test() {

        // A small hard coded world for testing
        Actor[][] testWorld = new Actor[][]{
                {Actor.RED, Actor.RED, Actor.NONE},
                {Actor.NONE, Actor.BLUE, Actor.NONE},
                {Actor.RED, Actor.NONE, Actor.BLUE}
        };

        Actor[] actarr = matrixToArray(testWorld);

        Actor[][] actarrmatris = arrayToMatrix(actarr);

        double th = 0.5;   // Simple threshold used for testing
        int size = testWorld.length;

        // TODO test methods

        exit(0);
    }

    // Helper method for testing (NOTE: reference equality)
    <T> int count(T[] arr, T toFind) {
        int count = 0;
        for (int i = 0; i < arr.length; i++) {
            if (arr[i] == toFind) {
                count++;
            }
        }
        return count;
    }

    // *****   NOTHING to do below this row, it's JavaFX stuff  ******

    double width = 400;   // Size for window
    double height = 400;
    long previousTime = nanoTime();
    final long interval = 450000000;
    double dotSize;
    final double margin = 50;

    void fixScreenSize(int nLocations) {
        // Adjust screen window depending on nLocations
        dotSize = (width - 2 * margin) / sqrt(nLocations);
        if (dotSize < 1) {
            dotSize = 2;
        }
    }

    @Override
    public void start(Stage primaryStage) throws Exception {

        // Build a scene graph
        Group root = new Group();
        Canvas canvas = new Canvas(width, height);
        root.getChildren().addAll(canvas);
        GraphicsContext gc = canvas.getGraphicsContext2D();

        // Create a timer
        AnimationTimer timer = new AnimationTimer() {
            // This method called by FX, parameter is the current time
            public void handle(long currentNanoTime) {
                long elapsedNanos = currentNanoTime - previousTime;
                if (elapsedNanos > interval) {
                    updateWorld();
                    renderWorld(gc, world);
                    previousTime = currentNanoTime;
                }
            }
        };

        Scene scene = new Scene(root);
        primaryStage.setScene(scene);
        primaryStage.setTitle("Simulation");
        primaryStage.show();

        timer.start();  // Start simulation
    }


    // Render the state of the world to the screen
    public void renderWorld(GraphicsContext g, Actor[][] world) {
        g.clearRect(0, 0, width, height);
        int size = world.length;
        for (int row = 0; row < size; row++) {
            for (int col = 0; col < size; col++) {
                double x = dotSize * col + margin;
                double y = dotSize * row + margin;

                if (world[row][col] == Actor.RED) {
                    g.setFill(Color.RED);
                } else if (world[row][col] == Actor.BLUE) {
                    g.setFill(Color.BLUE);
                } else {
                    g.setFill(Color.WHITE);
                }
                g.fillOval(x, y, dotSize, dotSize);
            }
        }
    }

    public static void main(String[] args) {
        launch(args);
    }

}
