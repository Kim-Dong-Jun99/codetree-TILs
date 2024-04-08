import java.io.*;
import java.util.Arrays;
import java.util.HashMap;
import java.util.PriorityQueue;

/*
13:37
1 ~ P번까지 산타가 크리스마스 이브를 준비함,
루돌프는 산타들을 박치기하여 산타의 선물 배달을 방해하려고함,

N * N 게임판, 좌상단 1,1
M개의 턴에 걸쳐 게임이 진행된다,
    루돌프가 한번 움직인 뒤 1번 산타부터 P번 산타까지 순서대로 움직임
        기절해있거나, 격자밖인 산타는 이동 불가
        게임 판에서 두 칸의 거리는 점과 점 사이 거리 공식으로 구함
    루돌프 움직임
        가장 가까운 산타를 향해 1칸 돌진 (탈락하지 않은 산타 중 골라야함)
        가장 가까운 산타가 2명 이상이면, r 좌표가 큰 산타, r 좌표 같으면 c 좌표 큰 산타
        상하좌우, 대각선을 포함한 8방향 중 하나로 돌진 가능,
        가장 우선순위가 높은 산타를 향해 8방향 중 가장 가까워지는 방향으로 한칸 돌진
    산타 움직임
        1번부터 P번까지 순서대로
        기절, 탈락한 산타는 움직일 수 없음
        루돌프에게 거리가 가장 가까워지는 방향으로 1칸 이동
        다른 산타 or 게임판 밖으로 이동 불가
        움직일 수 있는 칸이 없음 이동X
        움직일 수 있는 칸이 있더라도, 루돌프로부터 가까워질 수 없음 이동 X
        상하좌우 중 하나로 이동, 가까워질 수 있는 방향이 여러개면 상우하좌 우선순위에 맞춰 이동
    충돌
        산타 - 루돌프 같은 칸이면 충돌
        루돌프 이동해서 충돌
            산타는 C 만큼 점수를 얻음
            산타는 루돌프가 이동해온 방향으로 C칸 이동
        산타가 이동해서 충돌
            산타는 D 만큼 점수를 얻음
            이동한 반대방향으로 D 칸 밀려남
        정확히 원하는 위치에 도달하고, 도달과정에서 충돌 X
        만약 밀려난 위치가 게임판밖이면 산타는 탈락
        밀려난 칸에 다른 산타가 있음 상호작용 발생
    상호작용
        충돌 후 착지하게 되는 칸에 다른 산타가 있다면, 그 산타는 1칸 해당방향으로 밀려남
        연쇄적으로 밀려남
    기절
        산타는 루돌프와 충돌 후 기절
        k턴에 기절하면 k+2턴 부터 이동 가능
        루돌프는 기절한 산타를 돌진 대상을 선택 가능
    게임 종료
        M 턴에 걸쳐 루돌프, 산타가 순서대로 이동
        P 명의 산타가 모두 탈락하면 게임 종료
        매턴 탈락하지 않은 산타들에게 1점 부여
*/


public class Main {
    static final BufferedReader BR = new BufferedReader(new InputStreamReader(System.in));
    static final BufferedWriter BW = new BufferedWriter(new OutputStreamWriter(System.out));
    static final int[] DX = {-1, -1, 0, 1, 1, 1, 0, -1};
    static final int[] DY = {0, 1, 1, 1, 0, -1, -1, -1};
    static final int[] OPPOSITE = {4, 5, 6, 7, 0, 1, 2, 3};

    int[] inputArray;
    int N, M, P, C, D;
    int rx, ry;
    HashMap<Integer, Santa> santas;
    int[] score;
    int turn;
    int[][] board;
    int outCount;

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
        P = inputArray[2];
        C = inputArray[3];
        D = inputArray[4];

        inputArray = getInputArray();
        rx = inputArray[0] - 1;
        ry = inputArray[1] - 1;
        board = new int[N][N];

        santas = new HashMap<>();
        for (int i = 0; i < P; i++) {
            inputArray = getInputArray();
            Santa santa = new Santa(inputArray[0], inputArray[1] - 1, inputArray[2] - 1);
            santas.put(santa.index, santa);
            board[santa.x][santa.y] = santa.index;
        }
        score = new int[P + 1];
        turn = 1;
        outCount = 0;
    }

    void solve() throws IOException {
        while (turn <= M && outCount < P) {
            rudolfTurn();
            santaTurn();
            updateScore();
            turn += 1;
        }
        for (int i = 1; i <= P; i++) {
            BW.write(Integer.toString(score[i]) + " ");
        }
    }


    void updateScore() {
        for (Santa s : santas.values()) {
            if (!s.isOut) {
                score[s.index] += 1;
            }
        }
    }

    void rudolfTurn() {
        ClosestSanta closestSanta = getClosestSanta();
        moveRudolf(closestSanta);
    }

    ClosestSanta getClosestSanta() {
        PriorityQueue<ClosestSanta> heap = new PriorityQueue<>(ClosestSanta::sort);
        for (Santa s : santas.values()) {
            if (!s.isOut) {
                int d = calculateDistance(rx, ry, s.x, s.y);
                heap.add(new ClosestSanta(s.index, d, s.x, s.y));
            }

        }
        return heap.remove();
    }

    void moveRudolf(ClosestSanta closestSanta) {
        int d = getRudolfDirection(closestSanta.x, closestSanta.y);
        rx += DX[d];
        ry += DY[d];
        rudolfCrashWithSanta(d);
    }

    int getRudolfDirection(int x, int y) {
        int distance = 2500;
        int direction = -1;
        for (int d = 0; d < 8; d++) {
            int newX = rx + DX[d];
            int newY = ry + DY[d];

            int newDistance = calculateDistance(x, y, newX, newY);
            if (newDistance < distance) {
                distance = newDistance;
                direction = d;
            }
        }

        return direction;
    }

    int calculateDistance(int x1, int y1, int x2, int y2) {
        return (int) (Math.pow(x1 - x2, 2) + Math.pow(y1 - y2, 2));
    }

    void rudolfCrashWithSanta(int d) {
        if (board[rx][ry] == 0) {
            return;
        }
        int santaIndex = board[rx][ry];
        score[santaIndex] += C;
        int newX = rx + DX[d] * C;
        int newY = ry + DY[d] * C;
        moveSanta(santaIndex, rx, ry, newX, newY, d);
    }

    void moveSanta(int santaIndex, int fromX, int fromY, int toX, int toY, int d) {
        board[fromX][fromY] = 0;
        if (!isInner(toX, toY)) {
            santas.get(santaIndex).isOut = true;
            outCount += 1;
            return;
        }
        Santa santa = santas.get(santaIndex);
        santa.crashed = turn + 1;
        santa.x = toX;
        santa.y = toY;
        if (board[toX][toY] == 0) {
            board[toX][toY] = santaIndex;
        } else {
            interaction(santaIndex, board[toX][toY], d);
        }

    }

    boolean isInner(int x, int y) {
        return 0 <= x && x < N && 0 <= y && y < N;
    }

    void interaction(int santaIndex, int toMoveSantaIndex, int direction) {
        Santa toMoveSanta = santas.get(toMoveSantaIndex);
        board[toMoveSanta.x][toMoveSanta.y] = santaIndex;
        int newX = toMoveSanta.x + DX[direction];
        int newY = toMoveSanta.y + DY[direction];
        toMoveSanta.x = newX;
        toMoveSanta.y = newY;
        if (!isInner(newX, newY)) {
            santas.get(toMoveSantaIndex).isOut = true;
            outCount += 1;
        } else {
            if (board[newX][newY] == 0) {
                board[newX][newY] = toMoveSantaIndex;
            } else {
                interaction(toMoveSantaIndex, board[newX][newY], direction);
            }
        }

    }

    void santaTurn() {
        for (int i = 1; i <= P; i++ ){
            Santa santa = santas.get(i);
            if (turn > santa.crashed && !santa.isOut) {
                int direction = getSantaDirection(santa);
                if (direction != -1) {
                    int newX = santa.x + DX[direction];
                    int newY = santa.y + DY[direction];
                    board[santa.x][santa.y] = 0;
                    santa.x = newX;
                    santa.y = newY;
                    board[santa.x][santa.y] = santa.index;
                    if (rx == newX && ry == newY) {
                        santaCrashWithRudolf(santa, direction);
                    }
                }

            }
        }
    }

    void santaCrashWithRudolf(Santa santa, int d) {
        score[santa.index] += D;
        int opposite = OPPOSITE[d];
        int newX = santa.x + DX[opposite] * D;
        int newY = santa.y + DY[opposite] * D;
        moveSanta(santa.index, santa.x, santa.y, newX, newY, opposite);
    }

    int getSantaDirection(Santa santa) {
        int direction = -1;
        int currentDistance = calculateDistance(rx, ry, santa.x, santa.y);
        for (int d = 0; d < 8; d += 2) {
            int newX = santa.x + DX[d];
            int newY = santa.y + DY[d];
            int newDistance = calculateDistance(rx, ry, newX, newY);
            if (isInner(newX, newY) && board[newX][newY] == 0 && newDistance < currentDistance) {
                direction = d;
                currentDistance = newDistance;
            }
        }
        return direction;
    }


    void printResult() throws IOException {
        BW.flush();
        BW.close();
        BR.close();
    }

    static class Santa {
        int index;
        int x;
        int y;
        int crashed;
        boolean isOut;

        Santa(int index, int x, int y) {
            this.index = index;
            this.x = x;
            this.y = y;
            this.crashed = 0;
            this.isOut = false;
        }
    }

    static class ClosestSanta {
        int d;
        int x;
        int y;
        int index;

        ClosestSanta(int index, int d, int x, int y) {
            this.index = index;
            this.d = d;
            this.x = x;
            this.y = y;
        }

        int sort(ClosestSanta compare) {
            if (this.d != compare.d) {
                return Integer.compare(this.d, compare.d);
            }
            if (this.x != compare.x) {
                return Integer.compare(compare.x, this.x);
            }
            return Integer.compare(compare.y, this.y);
        }
    }
}