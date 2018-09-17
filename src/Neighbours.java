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

    final long interval = 16666666 * 2;
    // Below is the *only* accepted instance variable (i.e. variables outside any method)
    // This variable may *only* be used in methods init() and updateWorld()
    Actor[][] mWorld;              // The mWorld is a square matrix of Actors
    Random rand = new Random();

    // This method initializes the mWorld variable with a random distribution of Actors
    // Method automatically called by JavaFX runtime (before graphics appear)
    // Don't care about "@Override" and "public" (just accept for now)
    double width = 900;   // Size for window


    // ------- Methods ------------------
    double height = 900;
    long previousTime = nanoTime();

    // This is the method called by the timer to update the mWorld
    // (i.e move unsatisfied) approx each 1/60 sec.
    void updateWorld() {
        // % of surrounding neighbours that are like me
        final double threshold = 0.7;
        // TODO
        State[][] states = getStates(mWorld, threshold);
        getNextWorld(states, mWorld);
    }

    @Override
    public void init() {
        //test();    // <---------------- Uncomment to TEST!

        // %-distribution of RED, BLUE and NONE
        double[] dist = {0.25, 0.25, 0.1};
        // Number of locations (places) in mWorld (square)
        int nLocations = 90000 / 16;
        int size = (int) Math.sqrt(nLocations);

        // TODO

        mWorld = createWorld(size);
        populateWorld(mWorld, dist, nLocations, size);
        shuffleMatrix(mWorld);

        // Should be last
        fixScreenSize(nLocations);
    }

    void getNextWorld(State[][] states, Actor[][] current) {

        int size = current.length;

        Integer[] emptyCells = getIndicesForState(states, State.NA);
        shuffleArray(emptyCells);
        Integer[] unsatisfiedCells = getIndicesForState(states, State.UNSATISFIED);
        shuffleArray(unsatisfiedCells);
        int range = Math.min(emptyCells.length, unsatisfiedCells.length);

        for (int i = 0; i < range; i++) {
            int emptyIndex = emptyCells[i];
            int unsatisfiedIndex = unsatisfiedCells[i];
            current[emptyIndex / size][emptyIndex % size] = current[unsatisfiedIndex / size][unsatisfiedIndex % size];
            current[unsatisfiedIndex / size][unsatisfiedIndex % size] = Actor.NONE;
        }

    }

    Integer[] getIndicesForState(State[][] states, State state) {
        State[] statesAsArray = squareMatrixToArray(State.class, states);
        int size = 0;

        for (State stateInArr : statesAsArray) {
            if (stateInArr == state) size++;
        }

        Integer[] indices = new Integer[size];
        int index = 0;

        for (int i = 0; i < statesAsArray.length; i++) {
            if (statesAsArray[i] == state) {
                indices[index] = i;
                index++;
            }
        }

        return indices;
    }

    State[][] getStates(Actor[][] world, double threshold) {
        State[][] states = new State[world.length][world.length];

        for (int row = 0; row < world.length; row++) {
            for (int col = 0; col < world.length; col++) {
                Actor actor = world[row][col];
                State state = getState(actor, world, row, col, threshold);
                states[row][col] = state;
            }
        }
        return states;
    }

    // Helper methods

    State getState(Actor actor, Actor[][] world, int row, int col, double threshold) {
        State state = State.NA;
        if (actor != Actor.NONE) {
            Actor[] neighbours = getNeighbours(world, row, col);
            int same = 0;
            int different = 0;
            for (Actor neighbour : neighbours) {
                if (neighbour == actor) {
                    same++;
                } else if (neighbour != Actor.NONE) {
                    different++;
                }
            }
            double percentage = (((double) same) / ((double) (same + different)));
            if (percentage >= threshold) state = State.SATISFIED;
            else state = State.UNSATISFIED;
        }
        return state;
    }

    Actor[] getNeighbours(Actor[][] world, int row, int col) {
        int max = (world.length - 1);

        Actor[] neighbours = new Actor[]{Actor.NONE, Actor.NONE, Actor.NONE, Actor.NONE, Actor.NONE, Actor.NONE, Actor.NONE, Actor.NONE};
        if (row > 0) {
            neighbours[1] = world[row - 1][col];
            if (col > 0) {
                neighbours[0] = world[row - 1][col - 1];
            }
            if (col < max) {
                neighbours[2] = world[row - 1][col + 1];
            }
        }
        if (col > 0) {
            neighbours[3] = world[row][col - 1];
        }
        if (col < max) {
            neighbours[4] = world[row][col + 1];
        }
        if (row < max) {
            neighbours[6] = world[row + 1][col];
            if (col > 0) {
                neighbours[5] = world[row + 1][col - 1];
            }
            if (col < max) {
                neighbours[7] = world[row + 1][col + 1];
            }
        }
        return neighbours;
    }

    Actor[][] createWorld(int size) {
        return new Actor[size][size];
    }

    void populateWorld(Actor[][] world, double[] dist, int locations, int size) {
        double distTotal = 0.0;
        for (double d : dist) {
            distTotal += d;
        }
        int nRed = (int) ((dist[0] / distTotal) * locations);
        int nBlue = (int) ((dist[1] / distTotal) * locations);

        for (int row = 0; row < world.length; row++) {
            for (int col = 0; col < world[row].length; col++) {
                if (nRed > 0) {
                    world[row][col] = Actor.RED;
                    nRed--;
                } else if (nBlue > 0) {
                    world[row][col] = Actor.BLUE;
                    nBlue--;
                } else {
                    world[row][col] = Actor.NONE;
                }
            }
        }
    }

    <T> void shuffleArray(T[] arr) {
        for (int i = arr.length - 1; i > 0; i--) {
            int r = rand.nextInt(i);
            T tmp = arr[i];
            arr[i] = arr[r];
            arr[r] = tmp;
        }
    }

    // ------- Testing -------------------------------------

    <T> void shuffleMatrix(T[][] matrix) {
        int len = matrix.length;
        for (int i = (len * len) - 1; i > 0; i--) {
            int r = rand.nextInt(i);

            int iRow = i / len;
            int iCol = i % len;
            int rRow = r / len;
            int rCol = r % len;

            T tmp = matrix[iRow][iCol];
            matrix[iRow][iCol] = matrix[rRow][rCol];
            matrix[rRow][rCol] = tmp;
        }
    }

    // *****   NOTHING to do below this row, it's JavaFX stuff  ******

    // Helper method for testing (NOTE: reference equality)
    <T> int count(T[] arr, T toFind) {
        int count = 0;
        for (T element : arr) {
            if (element == toFind) {
                count++;
            }
        }
        return count;
    }

    <T> T[] squareMatrixToArray(Class<T> cls, T[][] matrix) {
        int len = matrix.length;

        T[] arr = (T[]) Array.newInstance(cls, len * len);


        for (int row = 0; row < len; row++) {
            for (int col = 0; col < len; col++) {
                arr[row * len + col] = matrix[row][col];
            }
        }
        return arr;
    }

    <T> T[][] arrayToSquareMatrix(Class<T> cls, T[] array) {
        int len = (int) Math.sqrt(array.length);
        T[][] matrix = (T[][]) Array.newInstance(cls, len, len);
        //new Actor[len][len];

        for (int i = 0; i < array.length; i++) {
            int row = i / len;
            int col = i % len;
            matrix[row][col] = array[i];
        }

        return matrix;

    }

    // Here you run your tests i.e. call your logic methods
    // to see that they really work
    void test() {
        // A small hard coded mWorld for testing
        Actor[][] testWorld = new Actor[][]{
                {Actor.RED, Actor.RED, Actor.NONE},
                {Actor.NONE, Actor.BLUE, Actor.NONE},
                {Actor.RED, Actor.NONE, Actor.BLUE}
        };
        double th = 0.5;   // Simple threshold used for testing
        int size = testWorld.length;

        // TODO test methods
        State[][] states = getStates(testWorld, th);

        //Integer[] empty = getEmptyIndices(states);
        //out.println(empty.length == 4);


        out.println(Arrays.deepEquals(arrayToSquareMatrix(Actor.class, squareMatrixToArray(Actor.class, testWorld)), testWorld));
        exit(0);
    }
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
    public void start(Stage primaryStage) {

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
                    renderWorld(gc, mWorld);
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


    // Render the state of the mWorld to the screen
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
