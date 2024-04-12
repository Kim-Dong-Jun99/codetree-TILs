import java.util.*;
import java.io.*;

public class Main {
    static final BufferedReader BR = new BufferedReader(new InputStreamReader(System.in));
    static final BufferedWriter BW = new BufferedWriter(new OutputStreamWriter(System.out));

    static final int[] dx = {0, 1, 0, -1};
    static final int[] dy = {1, 0, -1, 0};
    static final int[] opposite = {2, 3, 0, 1};
    static final int[] DX = {0, 0, 1, -1};
    static final int[] DY = {1, -1, 0, 0};
    static final int[] OPPOSITE = {1, 0, 3, 2};

    int[] inputArray;
    int n, m, h, k;
    List<Runner> runners;
    Catcher catcher;
    boolean[][] hasTree;
    int answer;
    int turn;

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
        h = inputArray[2];
        k = inputArray[3];

        runners = new ArrayList<>();
        for (int i = 0; i < m; i++) {
            inputArray = getInputArray();
            runners.add(new Runner(inputArray[0] - 1, inputArray[1] - 1, (inputArray[2] - 1) * 2));
        }

        hasTree = new boolean[n][n];
        for (int i = 0; i < h; i++) {
            inputArray = getInputArray();
            hasTree[inputArray[0] - 1][inputArray[1] - 1] = true;
        }
        answer = 0;
        turn = 1;
        catcher = new Catcher(n / 2, n / 2, n);
    }

    void solve() {
        while (turn <= k) {
            moveRunners();
            catcher.move();
            catchRunners();
            turn += 1;
        }
    }

    void moveRunners() {
        for (Runner runner : runners) {
            int distance = getDistance(runner.x, runner.y, catcher.x, catcher.y);
            if (distance <= 3) {
                int newX = runner.x + DX[runner.d];
                int newY = runner.y + DY[runner.d];
                if (!isInner(newX, newY)) {
                    runner.d = OPPOSITE[runner.d];
                    newX = runner.x + DX[runner.d];
                    newY = runner.y + DY[runner.d];
                }
                if (!(newX == catcher.x && newY == catcher.y)) {
                    runner.x = newX;
                    runner.y = newY;
                }
            }
        }
    }

    void catchRunners() {
        boolean[][] caught = new boolean[n][n];
        int x = catcher.x;
        int y = catcher.y;
        while (isInner(x, y)) {
            caught[x][y] = true;
            x += dx[catcher.d];
            y += dy[catcher.d];
        }
        List<Runner> notCaught = new ArrayList<>();
        int caughtRunner = 0;
        for (Runner runner : runners) {
            if (caught[runner.x][runner.y] && !hasTree[runner.x][runner.y]) {
                caughtRunner += 1;
            } else {
                notCaught.add(runner);
            }
        }
        answer += turn * caughtRunner;
        runners = notCaught;
    }

    int getDistance(int x1, int y1, int x2, int y2) {
        return Math.abs(x1 - x2) + Math.abs(y1 - y2);
    }

    boolean isInner(int x, int y) {
        return 0 <= x && x < n && 0 <= y && y < n;
    }


    void printResult() throws IOException {
        BW.write(Integer.toString(answer));
        BW.flush();
        BW.close();
        BR.close();
    }

    static class Runner {
        int x;
        int y;
        int d;

        Runner(int x, int y, int d) {
            this.x = x;
            this.y = y;
            this.d = d;
        }
    }

    static class Catcher {
        int n;
        int x;
        int y;
        int d;
        boolean rotateCW;
        boolean[][] rotate;

        Catcher(int x, int y, int n) {
            this.n = n;
            this.x = x;
            this.y = y;
            this.d = 3;
            this.rotateCW = true;
            this.rotate = new boolean[n][n];
            initRotate();
        }

        void initRotate() {
            int centerX = n/2;
            int centerY = n / 2;
            int size = 1;
            while (size <= n / 2) {
                rotate[centerX + size][centerY + size] = true;
                rotate[centerX + size][centerY - size] = true;
                rotate[centerX - size][centerY + size] = true;
                rotate[centerX - size][centerY - size + 1] = true;
                size += 1;
            }
        }

        boolean mustFlip() {
            return (x == 0 && y == 0) || (x == n / 2 && y == n / 2);
        }

        int getCW() {
            return (d + 1) % 4;
        }

        int getCCW() {
            return (d + 3) % 4;
        }

        void move() {
            x += dx[d];
            y += dy[d];
            if (mustFlip()) {
                d = opposite[d];
                rotateCW = !rotateCW;
                return;
            }
            if (rotate[x][y]) {
                if (rotateCW) {
                    d = getCW();
                } else {
                    d = getCCW();
                }
            }
        }
    }
}