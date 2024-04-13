import java.util.*;
import java.io.*;

public class Main {
    static final BufferedReader BR = new BufferedReader(new InputStreamReader(System.in));
    static final BufferedWriter BW = new BufferedWriter(new OutputStreamWriter(System.out));
    static final int[] DX = {0, 1, 1, 1, 0, -1, -1, -1};
    static final int[] DY = {1, 1, 0, -1, -1, -1, 0, 1};
    static final int WALL = -1;

    int[] inputArray;
    int n, m, k, c;
    int year;
    int[][] trees;
    int[][] lastSanitized;
    int answer;

    public static void main(String[] args) throws IOException {
        Main main = new Main();
        main.init();
        main.solve();
        main.printResult();
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

        trees = new int[n][n];
        lastSanitized = new int[n][n];
        for (int i = 0; i < n; i++) {
            trees[i] = getInputArray();
        }
        year = 1;
        answer = 0;
    }

    void solve() {
        while (year <= m) {
            growTree();
            spreadTree();
            try {

                sanitize();
            } catch (Exception e) {
                break;
            }
            year += 1;
        }
    }

    void growTree() {
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                if (trees[i][j] > 0) {
                    int neighbourCount = 0;
                    for (int d = 0; d < 8; d += 2) {
                        int newX = i + DX[d];
                        int newY = j + DY[d];
                        if (isInner(newX, newY) && trees[newX][newY] > 0) {
                            neighbourCount += 1;
                        }
                    }
                    trees[i][j] += neighbourCount;
                }
            }
        }
    }

    boolean isInner(int x, int y) {
        return 0 <= x && x < n && 0 <= y && y < n;
    }

    void spreadTree() {
        int[][] newTree = new int[n][n];
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                if (trees[i][j] > 0) {
                    int birthCount = 0;
                    for (int d = 0; d < 8; d += 2) {
                        int newX = i + DX[d];
                        int newY = j + DY[d];
                        if (isInner(newX, newY) && canGrowNewTree(newX, newY)) {
                            birthCount += 1;
                        }
                    }
                    for (int d = 0; d < 8; d += 2) {
                        int newX = i + DX[d];
                        int newY = j + DY[d];
                        if (isInner(newX, newY) && canGrowNewTree(newX, newY)) {
                            newTree[newX][newY] += trees[i][j] / birthCount;
                        }
                    }
                }
            }
        }
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                trees[i][j] += newTree[i][j];
            }
        }
    }

    boolean canGrowNewTree(int x, int y) {
        return trees[x][y] == 0 && lastSanitized[x][y] < year;
    }

    void sanitize() {
        Position sanitizePosition = getSanitizePosition();
        answer += sanitizePosition.count;
        trees[sanitizePosition.x][sanitizePosition.y] = 0;
        lastSanitized[sanitizePosition.x][sanitizePosition.y] = year + c;
        for (int d = 1; d < 8; d += 2) {
            int cx = sanitizePosition.x + DX[d];
            int cy = sanitizePosition.y + DY[d];
            int distance = 1;
            while (canRemove(cx, cy) && distance <= k) {
                trees[cx][cy] = 0;
                lastSanitized[cx][cy] = year + c;
                distance += 1;
                cx += DX[d];
                cy += DY[d];
            }
        }
    }

    Position getSanitizePosition() {
        PriorityQueue<Position> heap = new PriorityQueue<>(Position::sort);
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                if (trees[i][j] > 0) {
                    heap.add(new Position(i, j, getRemoveTreeCount(i, j)));
                }
            }
        }
        return heap.remove();
    }

    int getRemoveTreeCount(int x, int y) {
        int count = trees[x][y];
        for (int d = 1; d < 8; d += 2) {
            int cx = x + DX[d];
            int cy = y + DY[d];
            int distance = 1;
            while (canRemove(cx, cy) && distance <= k) {
                count += trees[cx][cy];
                distance += 1;
                cx += DX[d];
                cy += DY[d];
            }
        }
        return count;
    }

    boolean canRemove(int x, int y) {
        return isInner(x, y) && trees[x][y] != -1;
    }

    void printResult() throws IOException {
        BW.write(Integer.toString(answer));
        BW.flush();
        BW.close();
        BR.close();
    }

    static class Position {
        int x;
        int y;
        int count;

        Position(int x, int y, int count) {
            this.x = x;
            this.y = y;
            this.count = count;
        }

        int sort(Position compare) {
            if (this.count != compare.count) {
                return Integer.compare(compare.count, this.count);
            }
            if (this.x != compare.x) {
                return Integer.compare(this.x, compare.x);
            }
            return Integer.compare(this.y, compare.y);
        }
    }
}