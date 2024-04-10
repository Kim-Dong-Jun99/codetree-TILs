import java.util.*;
import java.io.*;

public class Main {
    static final BufferedReader BR = new BufferedReader(new InputStreamReader(System.in));
    static final BufferedWriter BW = new BufferedWriter(new OutputStreamWriter(System.out));
    static final int[] DX = {0, 1, 1, 1, 0, -1, -1, -1};
    static final int[] DY = {1, 1, 0, -1, -1, -1, 0, 1};

    int[] inputArray;
    int N, M, K;
    int[][] power;
    int[][] attack;
    int time;
    List<Turret> turrets;
    boolean[][] attacked;
    Turret attacker, target;

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
        N = inputArray[0];
        M = inputArray[1];
        K = inputArray[2];

        power = new int[N][M];
        attack = new int[N][M];
        for (int i = 0; i < N; i++) {
            power[i] = getInputArray();
        }
        time = 1;
    }

    void solve() {
        while (K-- > 0) {
            getTurrets();
            if (turrets.size() == 1) {
                break;
            }
            increaseAttackerPower();
            if (!laserAttack()) {
                canonAttack();
            }
            repair();
//            System.out.println("attack " + attacker.x + " " + attacker.y);
//            System.out.println("target " + target.x + " " + target.y);
//            System.out.println("==============");
//            for (int i = 0; i < N; i++) {
//                for (int j = 0; j < M; j++) {
//                    System.out.print(power[i][j] +" ");
//                }
//                System.out.println();
//            }
            time += 1;
        }
    }

    void getTurrets() {
        turrets = new ArrayList<>();
        for (int i = 0; i < N; i++) {
            for (int j = 0; j < N; j++) {
                if (power[i][j] > 0) {
                    turrets.add(new Turret(i, j, power[i][j], attack[i][j]));
                }
            }
        }
        turrets.sort(Turret::sort);
        attacker = turrets.get(0);
        target = turrets.get(turrets.size()-1);
    }

    void increaseAttackerPower() {
        power[attacker.x][attacker.y] += N + M;
        attack[attacker.x][attacker.y] = time;
        attacked = new boolean[N][M];
    }

    boolean laserAttack() {
        Position[][] before = new Position[N][M];
        List<Position> current = new ArrayList<>();
        current.add(new Position(attacker.x, attacker.y));
        while (!current.isEmpty()) {
            List<Position> temp = new ArrayList<>();
            for (Position p : current) {
                for (int d = 0; d < 8; d += 2) {
                    Position next = nextPosition(p.x, p.y, d);
                    if (power[next.x][next.y] > 0 && before[next.x][next.y] == null) {
                        temp.add(next);
                        before[next.x][next.y] = p;
                    }
                }
            }
            if (before[target.x][target.y] != null) {
                break;
            }
            current = temp;
        }

        if(before[target.x][target.y] == null) {
            return false;
        }

        attacked[attacker.x][attacker.y] = true;
        attacked[target.x][target.y] = true;

        int original = power[attacker.x][attacker.y];
        int half = original / 2;

        List<Position> track = new ArrayList<>();
        Position back = before[target.x][target.y];
        while (!(back.x == attacker.x && back.y == attacker.y)) {
            track.add(back);
            back = before[back.x][back.y];
        }

        for (Position p : track) {
            power[p.x][p.y] -= half;
            attacked[p.x][p.y] = true;
        }

        power[target.x][target.y] -= original;
        return true;
    }

    Position nextPosition(int x, int y, int d) {
        int newX = x + DX[d];
        int newY = y + DY[d];
        if (newX == -1) {
            newX = N-1;
        }
        if (newX == N) {
            newX = 0;
        }
        if (newY == -1) {
            newY = M-1;
        }
        if (newY == M) {
            newY = 0;
        }
        return new Position(newX, newY);
    }

    boolean isInner(int x, int y) {
        return 0 <= x && x < N && 0 <= y && y < M;
    }

    void canonAttack() {
        attacked[attacker.x][attacker.y] = true;
        attacked[target.x][target.y] = true;

        int original = power[attacker.x][attacker.y];
        int half = original / 2;

        for (int d = 0; d < 8; d++) {
            Position next = nextPosition(target.x, target.y, d);
            if (power[next.x][next.y] > 0) {
                attacked[next.x][next.y] = true;
                power[next.x][next.y] -= half;
            }
        }

        power[target.x][target.y] -= original;
    }

    void repair() {
        for (int i = 0; i < N; i++) {
            for (int j = 0; j < M; j++) {
                if (power[i][j] > 0 && !attacked[i][j]) {
                    power[i][j] += 1;
                }
            }
        }
    }

    void printResult() throws IOException {
        int maxPower = 0;
        for (int i = 0; i < N; i++) {
            for (int j = 0; j < M; j++) {
                maxPower = Math.max(maxPower, power[i][j]);
            }
        }
        BW.write(Integer.toString(maxPower));
        BW.flush();
        BW.close();
        BR.close();
    }

    static class Turret {
        int power;
        int lastAttack;
        int x;
        int y;

        Turret(int x, int y, int power, int lastAttack) {
            this.x = x;
            this.y = y;
            this.power = power;
            this.lastAttack = lastAttack;
        }

        int sort(Turret compare) {
            if (this.power != compare.power) {
                return Integer.compare(this.power, compare.power);
            }
            if (this.lastAttack != compare.lastAttack) {
                return Integer.compare(compare.lastAttack, this.lastAttack);
            }
            if (this.x + this.y != compare.x + compare.y) {
                return Integer.compare(compare.x + compare.y, this.x + this.y);
            }
            return Integer.compare(compare.y, this.y);
        }
    }

    static class Position {
        int x;
        int y;

        Position(int x, int y) {
            this.x = x;
            this.y = y;
        }
    }
}