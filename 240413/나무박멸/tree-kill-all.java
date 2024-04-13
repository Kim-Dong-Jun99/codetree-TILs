import java.util.*;
import java.io.*;

public class Main {
    static final BufferedReader BR = new BufferedReader(new InputStreamReader(System.in));
    static final int[] DX = {-1, 0, 1, 0, 1, 1, -1, -1};
    static final int[] DY = {0, 1, 0, -1, 1, -1, 1, -1};
    static final int WALL = -1;
    int[] inputArray;
    int[][] tree;
    int[][] poison;
    int n, m, c, k;
    int time;
    int answer;

    public static void main(String[] args) {
        Main main = new Main();
        try {
            main.init();
            main.solution();
        } catch (IOException e) {
            System.out.println("Exception during I/O");
        }
    }

    int[] getInputArray() throws IOException {
        return Arrays.stream(BR.readLine().split(" ")).mapToInt(Integer::parseInt).toArray();
    }

    void init() throws IOException {
        inputArray = getInputArray();
        n = inputArray[0];
        m = inputArray[1];
        k = inputArray[2];
        c = inputArray[3];

        tree = new int[n][n];
        poison = new int[n][n];

        time = 1;
        answer = 0;
        for (int i = 0; i < n; i++) {
            tree[i] = getInputArray();
        }

    }


    void solution() throws IOException {
        while (time <= m) {
            growAndBreed();
            try {

                poison();
            } catch (RuntimeException e) {
                break;
            }
            time += 1;
        }
        System.out.println(answer);
    }

    void growAndBreed() {
        int[][] newTree = new int[n][n];
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                if (tree[i][j] == -1) {
                    newTree[i][j] = -1;
                } else if (tree[i][j] > 0) {
                    List<Position> treeNeighbor = new ArrayList<>();
                    List<Position> empty = new ArrayList<>();

                    for (int d = 0; d < 4; d++) {
                        int newX = i + DX[d];
                        int newY = j + DY[d];
                        if (isInner(newX, newY)) {
                            if (canBreed(newX, newY)) {
                                empty.add(new Position(newX, newY));
                            }
                            if (tree[newX][newY] > 0) {
                                treeNeighbor.add(new Position(newX, newY));
                            }
                        }
                    }
                    newTree[i][j] = tree[i][j] + treeNeighbor.size();
                    for (Position emptyPosition : empty) {
                        newTree[emptyPosition.x][emptyPosition.y] += newTree[i][j] / empty.size();
                    }

                }
            }
        }

        tree = newTree;
    }

    boolean canBreed(int x, int y) {
        return poison[x][y] < time && tree[x][y] == 0;
    }

    boolean canPoison(int x, int y) {
        return tree[x][y] > 0;
    }
    boolean isInner(int x, int y) {
        return 0 <= x && x < n && 0 <= y && y < n;
    }

    Position getPoisonTarget() {
        int maxErase = 0;
        Position toReturn = null;
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                if (tree[i][j] > 0) {
                    int removedTree = calculateRemovedTree(i, j);
                    if (removedTree > maxErase) {
                        maxErase = removedTree;
                        toReturn = new Position(i, j);
                    }


                }
            }
        }
        return toReturn;
    }

    int calculateRemovedTree(int x, int y) {
        int removed = tree[x][y];
        for (int i = 4; i < 8; i++) {
            for (int d = 1; d <= k; d++) {
                int newX = x + DX[i] * d;
                int newY = y + DY[i] * d;
                if (isInner(newX, newY) && canPoison(newX, newY)) {
                    removed += tree[newX][newY];
                } else {
                    break;
                }
            }
        }
        return removed;

    }

    void poison() {
        Position position = getPoisonTarget();
        answer += tree[position.x][position.y];
        tree[position.x][position.y] = 0;
        poison[position.x][position.y] = time + c;
        for (int i = 4; i < 8; i++) {
            for (int d = 1; d <= k; d++) {
                int newX = position.x + DX[i] * d;
                int newY = position.y + DY[i] * d;
                if (isInner(newX, newY)) {
                    if (canPoison(newX, newY)) {
                        answer += tree[newX][newY];
                        tree[newX][newY] = 0;
                        poison[newX][newY] = time + c;
                    } else {
                        if (tree[newX][newY] == -1) {
                            break;
                        }
                        answer += tree[newX][newY];
                        tree[newX][newY] = 0;
                        poison[newX][newY] = time + c;
                        break;
                    }
                } else {
                    break;
                }

            }
        }


    }

    static class Position {
        int x;
        int y;

        public Position(int x, int y) {
            this.x = x;
            this.y = y;
        }
    }

}