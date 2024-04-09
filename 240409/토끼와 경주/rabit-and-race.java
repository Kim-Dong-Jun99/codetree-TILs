import java.util.*;
import java.io.*;


public class Main {
    static final BufferedReader BR = new BufferedReader(new InputStreamReader(System.in));
    static final BufferedWriter BW = new BufferedWriter(new OutputStreamWriter(System.out));
    static final int[] DX = {0, 1, 0, -1};
    static final int[] DY = {1, 0, -1, 0};
    static final int[] OPPOSITE = {2, 3, 0, 1};
    static final int READY = 100;
    static final int RACE = 200;
    static final int CHANGE_DISTANCE = 300;
    static final int GET_BEST_RABBIT = 400;


    int Q;
    int N, M, P;
    HashMap<Integer, Rabbit> rabbitMap;
    PriorityQueue<Rabbit> rabbitPriorityHeap;

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
        Q = Integer.parseInt(BR.readLine());
    }

    void solve() throws IOException {
        while (Q-- > 0) {
            int[] inputArray = getInputArray();
            int cmd = inputArray[0];
            if (cmd == READY) {
                readyRace(inputArray);
            }
            if (cmd == RACE) {
                race(inputArray[1], inputArray[2]);
            }
            if (cmd == CHANGE_DISTANCE) {
                changeDistance(inputArray[1], inputArray[2]);
            }
            if (cmd == GET_BEST_RABBIT) {
                getBestRabbit();
            }
        }
    }

    void readyRace(int[] inputArray) {
        N = inputArray[1];
        M = inputArray[2];
        P = inputArray[3];
        rabbitMap = new HashMap<>();
        rabbitPriorityHeap = new PriorityQueue<>(Rabbit::sortWithPriority);
        for (int i = 4; i < inputArray.length; i += 2) {
            int id = inputArray[i];
            int d = inputArray[i + 1];
            Rabbit rabbit = new Rabbit(id, 1, 1, d);
            rabbitMap.put(id, rabbit);
            rabbitPriorityHeap.add(rabbit);
        }
    }

    void race(int K, int S) {
        HashSet<Integer> chosenRabbitIndex = new HashSet<>();
        while (K-- > 0) {
            Rabbit removed = rabbitPriorityHeap.remove();
//            System.out.println("removed id " + removed.id);
            removed.jumpCount += 1;
            PriorityQueue<Position> positions = new PriorityQueue<>(Position::sort);
            for (int d = 0; d < 4; d++) {
                Position p = getNextPosition(d, removed);
                positions.add(p);
            }
            Position position = positions.remove();
            for (Rabbit rabbit : rabbitMap.values()) {
                if (rabbit.id != removed.id) {
                    rabbit.score += position.x + position.y;
//                    System.out.println("rabbit id " + rabbit.id + " score " + rabbit.score);
                }
            }
            removed.x = position.x;
            removed.y = position.y;
//            System.out.println("move "+removed.id +" to "+removed.x+" "+removed.y);
            rabbitPriorityHeap.add(removed);
            chosenRabbitIndex.add(removed.id);
        }

        PriorityQueue<Rabbit> bestRabbits = new PriorityQueue<>(Rabbit::sortToFindBest);
        for (Integer id : chosenRabbitIndex) {
            bestRabbits.add(rabbitMap.get(id));
        }
        Rabbit bestRabbit = bestRabbits.remove();
        bestRabbit.score += S;

    }

    private Position getNextPosition(int d, Rabbit rabbit) {
        int currentToEdge, toOtherEdge, toCurrent, edgeX, edgeY, otherEdgeX,otherEdgeY;
        int opposite = OPPOSITE[d];
        if (d == 0) {
            currentToEdge = M - rabbit.y; // 0
            toOtherEdge = M - 1; // 2
            toCurrent = rabbit.y - 1; // 2
            edgeX = rabbit.x;
            edgeY = M;
            otherEdgeX = rabbit.x;
            otherEdgeY = 1;
        } else if (d == 1) {
            currentToEdge = N - rabbit.x; // 2
            toOtherEdge = N - 1; // 2
            toCurrent = rabbit.x - 1; // 0
            edgeX = N;
            edgeY = rabbit.y;
            otherEdgeX = 1;
            otherEdgeY = rabbit.y;
        } else if (d == 2) {
            currentToEdge = rabbit.y - 1;
            toOtherEdge = M - 1;
            toCurrent = M - rabbit.y;
            edgeX = rabbit.x;
            edgeY = 1;
            otherEdgeX = rabbit.x;
            otherEdgeY = M;
        } else {
            currentToEdge = rabbit.x - 1; // 0
            toOtherEdge = N - 1; // 2
            toCurrent = N - rabbit.x; // 2
            edgeX = 1;
            edgeY = rabbit.y;
            otherEdgeX = N;
            otherEdgeY = rabbit.y;
        }
        int cycle = currentToEdge + toOtherEdge + toCurrent;
        int mod = rabbit.d % cycle;
        if (mod <= currentToEdge) {
            return new Position(rabbit.x + DX[d] * mod, rabbit.y + DY[d] * mod);
        } else if (mod <= currentToEdge + toOtherEdge) {
            int tempDistance = mod - currentToEdge;
            return new Position(edgeX + DX[opposite] * tempDistance, edgeY + DY[opposite] * tempDistance);
        } else {
            int tempDistance = mod - currentToEdge - toOtherEdge;
            return new Position(otherEdgeX + DX[d] * tempDistance, otherEdgeY + DY[d] * tempDistance);
        }

    }

    void changeDistance(int id, int L) {
        Rabbit rabbit = rabbitMap.get(id);
        rabbit.d *= L;
    }

    void getBestRabbit() throws IOException {
        long bestScore = 0;
        for (Rabbit rabbit : rabbitMap.values()) {
            bestScore = Math.max(bestScore, rabbit.score);

        }
        BW.write(Long.toString(bestScore) + "\n");
    }

    void printResult() throws IOException {
        BW.flush();
        BW.close();
        BR.close();
    }

    static class Position {
        int x;
        int y;

        Position(int x, int y) {
            this.x = x;
            this.y = y;
        }

        int sort(Position compare) {
            if (this.x + this.y != compare.x + compare.y) {
                return Integer.compare(compare.x + compare.y, this.x + this.y);
            }
            if (this.x != compare.x) {
                return Integer.compare(compare.x, this.x);
            }
            return Integer.compare(compare.y, this.y);
        }
    }

    static class Rabbit {
        int jumpCount;
        int id;
        int x;
        int y;
        int d;
        long score;
        Rabbit(int id, int x, int y, int d) {
            this.id = id;
            this.x = x;
            this.y = y;
            this.jumpCount = 0;
            this.d = d;
            this.score = 0;
        }

        int sortWithPriority(Rabbit compare) {
            if (this.jumpCount != compare.jumpCount) {
                return Integer.compare(this.jumpCount, compare.jumpCount);
            }
            if (this.x + this.y != compare.x + compare.y) {
                return Integer.compare(this.x + this.y, compare.x + compare.y);
            }
            if (this.x != compare.x) {
                return Integer.compare(this.x, compare.x);
            }
            if (this.y != compare.y) {
                return Integer.compare(this.y, compare.y);
            }
            return Integer.compare(this.id, compare.id);
        }

        int sortToFindBest(Rabbit compare) {
            if (this.x + this.y != compare.x + compare.y) {
                return Integer.compare(compare.x + compare.y, this.x + this.y);
            }
            if (this.x != compare.x) {
                return Integer.compare(compare.x, this.x);
            }
            if (this.y != compare.y) {
                return Integer.compare(compare.y, this.y);
            }
            return Integer.compare(compare.id, this.id);
        }
    }

}