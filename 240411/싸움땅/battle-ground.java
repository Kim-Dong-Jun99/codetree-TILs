import java.util.*;
import java.io.*;

public class Main {
    static final BufferedReader BR = new BufferedReader(new InputStreamReader(System.in));
    static final BufferedWriter BW = new BufferedWriter(new OutputStreamWriter(System.out));
    static final int[] DX = {-1, 0, 1, 0};
    static final int[] DY = {0, 1, 0, -1};
    static final int[] OPPOSITE = {2, 3, 0, 1};

    PriorityQueue<Integer>[][] guns;
    int[] inputArray;
    int n, m, k;
    Player[] players;

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

        initGuns();
        initPlayers();
    }

    void initGuns() throws IOException {
        guns = new PriorityQueue[n][n];
        for (int i = 0; i < n; i++) {
            inputArray = getInputArray();
            for (int j = 0; j < n; j++) {
                guns[i][j] = new PriorityQueue<>(Collections.reverseOrder());
                guns[i][j].add(inputArray[j]);
            }
        }
    }

    void initPlayers() throws IOException{
        players = new Player[m];
        for (int i = 0; i < m; i++) {
            inputArray = getInputArray();
            players[i] = new Player(i,inputArray[0] - 1, inputArray[1] - 1, inputArray[2], inputArray[3]);
        }
    }

    void solve() {
        while (k-- > 0) {
            movePlayer();
        }
    }

    void movePlayer() {
        for (Player p : players) {
            int newX = p.x + DX[p.d];
            int newY = p.y + DY[p.d];
            if (!isInner(newX, newY)) {
                p.d = OPPOSITE[p.d];
                newX = p.x + DX[p.d];
                newY = p.y + DY[p.d];
            }
            p.x = newX;
            p.y = newY;
            Player opponent = getPlayer(p.index, newX, newY);
            if (opponent == null) {
                changeGun(p);
            } else {
                fight(p, opponent);
            }
        }
    }

    void fight(Player p1, Player p2) {
        Player winner, loser;
        int score = Math.abs(p1.s + p1.gun - (p2.s + p2.gun));
        if (p1.s + p1.gun > p2.s + p2.gun) {
            winner = p1;
            loser = p2;
        } else if (p1.s + p1.gun < p2.s + p2.gun) {
            winner = p2;
            loser = p1;
        } else {
            if (p1.s > p2.s) {
                winner = p1;
                loser = p2;
            } else {
                winner = p2;
                loser = p1;
            }
        }
        throwAwayGun(loser);
        moveLoser(loser);
        changeGun(winner);
        winner.score += score;
    }

    void throwAwayGun(Player p) {
        guns[p.x][p.y].add(p.gun);
        p.gun = 0;
    }

    void moveLoser(Player p) {
        while (true) {
            int newX = p.x + DX[p.d];
            int newY = p.y + DY[p.d];
            if (!isInner(newX, newY) || getPlayer(p.index, newX, newY) != null) {
                p.d = getRightDirection(p.d);
                continue;
            }
            p.x = newX;
            p.y = newY;
            changeGun(p);
            break;
        }
    }

    int getRightDirection(int d) {
        return (d + 1) % 4;
    }

    void changeGun(Player p) {
        guns[p.x][p.y].add(p.gun);
        p.gun = guns[p.x][p.y].remove();
    }

    boolean isInner(int x, int y) {
        return 0 <= x && x < n && 0 <= y && y < n;
    }

    Player getPlayer(int index, int x, int y) {
        for (Player p : players) {
            if (p.index == index) {
                continue;
            }
            if (p.x == x && p.y == y) {
                return p;
            }
        }
        return null;
    }

    void printResult() throws IOException{
        for (Player player : players) {
            BW.write(Integer.toString(player.score) + " ");
        }
        BW.flush();
        BW.close();
        BR.close();
    }

    static class Player {
        int index;
        int x;
        int y;
        int d;
        int s;
        int gun;
        int score;

        Player(int index, int x, int y, int d, int s) {
            this.index = index;
            this.x = x;
            this.y = y;
            this.d = d;
            this.s = s;
            this.gun = 0;
            this.score = 0;
        }
    }
}