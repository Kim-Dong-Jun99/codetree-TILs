import java.util.*;
import java.io.*;

/*
17:41

P 명 산타,
N * N 크기 격자
M개의 턴
    - 루돌프가 한번 움직이고, 1번 산타부터 P번 산타까지 움직임,
    - 기절해있거나, 탈락한 산타 이동 불가
두 칸 사이 거리 = 유클리드 좌표 거리

루돌프
- 게임에서 탈락하지 않은 가장 가까운 산타를 향해 1칸 돌진,
- 가장 가까운 산타가 2명 이상이라면, r좌표가 큰 산타를 향해, 4 좌표가 동일한 경우, c 좌표가 큰 산타
상하좌우, 대각선을 포함한 8방향 중 하나로 돌진 가능,

산타
- 1번 부터 움직임,
- 기절 탈락 이동 불가
- 루돌프에게 가장 가까워지는 방향으로 1칸 이동
- 다른 산타가 있는 칸, 게임판 밖으로 이동 불가
- 움직일 수 있는 칸이 없으면 이동 x
- 상하좌우 중 1방향, 우선 순위 상우하좌,

충돌
- 산타와 루돌프 같은 칸
- 루돌프가 움직여 충돌
    - 산타는 C 만큼 점수
    - 산타는 루돌프가 이동해온 방향으로 C만큼 밀려남
- 산타가 움직여 충돌
    - 산타는 D 만큼 점수
    - 이동해온 반대 방향으로 D 만큼 밀려남
- 밀려나는 것은 포물선으로,
    - 밀려난 위치가 게임판 밖이면 탈락
    - 밀려난 칸에 다른 산타가 있음, 상호작용

상호작용
- 충돌 후 착지하게 되는 칸에 다른 산타가 있다면 산타는 해당 방향으로 1칸 밀려남,
- 밀려난 칸에 산타가 있다면 연쇄적으로 밀려남, 
- 게임판 바깥으로 밀려나면 탈락

기절
- 루돌프와 충돌 후 기절
- k번째턴이면 k+1 번째까지 기절
- K-2부터 활동 가능
- 상호작용으로 밀려나기 가능, 돌진 대상으로 선택 가능

M번의 턴에 걸쳐 루돌프, 산타가 순서대로 움직인 이후 게임 종료
P 명의 산타가 모두 게임에서 탈락하면 그 즉시 종료
매 턴 이후 탈락하지 않은 산타는 1점씩 획득


*/

public class Main {
    static final BufferedReader BR = new BufferedReader(new InputStreamReader(System.in));
    static final int[] DX = {-1, -1, 0, 1, 1, 1, 0, -1};
    static final int[] DY = {0, 1, 1, 1, 0, -1, -1, -1};
    static final int[] OPPOSITE = {4, 5, 6, 7, 0, 1, 2, 3};

    int[] inputArray;
    int N, M, P, C, D;
    int rx, ry;
    int[][] board;
    Santa[] santas;
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
        N = inputArray[0];
        M = inputArray[1];
        P = inputArray[2];
        C = inputArray[3];
        D = inputArray[4];

        inputArray = getInputArray();
        rx = inputArray[0]-1;
        ry = inputArray[1]-1;

        board = new int[N][N];
        santas = new Santa[P+1];
        turn = 1;
        for (int i = 1; i <= P; i++) {
            inputArray = getInputArray();
            santas[i] = new Santa(inputArray[0], inputArray[1]-1, inputArray[2]-1);
            markSanta(santas[i]);
        }

    }

    void solve() {
        while (turn <= M) {
            moveRudolf();
            moveSantas();
            if (updateSantaScore()) {
                break;
            }
            turn++;
        }
    }

    void moveRudolf() {
        int d = getRudolfDirection();
        rx += DX[d];
        ry += DY[d];
        if (board[rx][ry] != 0) {
            crashed(d, C);
        }

    }

    int getRudolfDirection() {
        Santa s = santas[getClosestSanta()];
        PriorityQueue<RudolfMove> heap = new PriorityQueue<>(RudolfMove::sort);
        for (int d = 0; d < 8; d++) {
            int nx = rx + DX[d];
            int ny = ry + DY[d];
            if (isInner(nx, ny)) {
                int distance = calcDis(nx, ny, s.x, s.y);
                heap.add(new RudolfMove(distance, nx, ny, d));
            }
        }
        return heap.remove().d;
    }

    int getClosestSanta() {
        PriorityQueue<SantaMove> heap = new PriorityQueue<>(SantaMove::sort);
        
        for (int i = 1; i <= P; i++) {
            Santa s = santas[i];
            if (!isInner(s.x, s.y)) {
                continue;
            }
            heap.add(new SantaMove(s.id, calcDis(rx, ry, s.x, s.y), s.x, s.y));
        }
        return heap.remove().id;
    }

    int calcDis(int x1, int y1, int x2, int y2) {
        return (x1-x2)*(x1-x2) + (y1-y2)*(y1-y2);
    }

    void crashed(int direction, int distance) {
        Santa santa = santas[board[rx][ry]];
        santa.crashed = turn + 1;
        santa.score += distance;
        unmarkSanta(santa);
        throwSanta(santa, direction, distance);
        if (!isInner(santa.x, santa.y)) {
            return;
        }
        if (board[santa.x][santa.y] == 0) {
            markSanta(santa);
        } else {
            Santa toPush = santas[board[santa.x][santa.y]];
            pushSanta(toPush, direction);
            markSanta(santa);
        }

    }

    void moveSantas() {
        for (int i = 1; i <= P; i++) {
            Santa s = santas[i];
            moveSanta(s);
        }
    }

    void moveSanta(Santa s) {
        if (!isInner(s.x, s.y)) {
            return;
        }
        if (turn <= s.crashed) {
            return;
        }
        int direction = getSantaDirection(s);
        if (direction == -1) {
            return;
        }
        unmarkSanta(s);
        s.x += DX[direction];
        s.y += DY[direction];
        markSanta(s);
        if (rx == s.x && s.y == ry) {
            crashed(OPPOSITE[direction], D);
        }

    }   


    int getSantaDirection(Santa s) {
        int direction = -1;
        int curDistance = calcDis(s.x, s.y, rx, ry);
        for (int d = 0; d < 8; d += 2) {
            int nx = s.x + DX[d];
            int ny = s.y + DY[d];
            if (!isInner(nx, ny) || board[nx][ny] != 0) {
                continue;
            }
            if (curDistance > calcDis(nx, ny, rx, ry)) {
                return d;
            }
        }
        return direction;
    }



    void throwSanta(Santa s, int direction, int distance) {
        s.x += DX[direction] * distance;
        s.y += DY[direction] * distance;
    }

    void pushSanta(Santa s, int direction) {
        unmarkSanta(s);
        s.x += DX[direction];
        s.y += DY[direction];
        if (isInner(s.x, s.y)) {
            if (board[s.x][s.y] == 0) {
                markSanta(s);
            } else {
                Santa toPush = santas[board[s.x][s.y]];
                pushSanta(toPush, direction);
                markSanta(s);
            }
        }
    }

    void unmarkSanta(Santa s) {
        board[s.x][s.y] = 0;
    }

    void markSanta(Santa s) {
        board[s.x][s.y] = s.id;
    }

    boolean updateSantaScore() {
        int out = 0;
        for (int i = 1; i <= P; i++) {
            Santa s = santas[i];
            if (isInner(s.x, s.y)) {
                s.score += 1;
            } else {
                out += 1;
            }
        }
        return out == P;
    }

    boolean isInner(int x, int y) {
        return 0 <= x && x < N && 0 <= y && y < N;
    }

    void printResult() {
        for (int i = 1; i <= P; i++) {
            System.out.print(santas[i].score+" ");
        }
    }

    static class Santa {
        int id;
        int x;
        int y;
        int crashed;
        int score;

        Santa(int id, int x, int y) {
            this.id = id;
            this.x = x;
            this.y = y;
        }
    }

    static class RudolfMove {
        int distance;
        int x;
        int y;
        int d;

        RudolfMove(int distance, int x, int y, int d) {
            this.distance = distance;
            this.x = x;
            this.y = y;
            this.d = d;
        }

        int sort(RudolfMove compare) {
            return Integer.compare(this.distance, compare.distance);
            
        }
    }

    static class SantaMove {
        int id;
        int distance;
        int x;
        int y;

        SantaMove(int id, int distance, int x, int y) {
            this.id = id;
            this.distance = distance;
            this.x = x;
            this.y = y;
        }

        int sort(SantaMove compare) {
            if (this.distance != compare.distance) {
                return Integer.compare(this.distance, compare.distance);
            }
            if (this.x != compare.x) {
                return Integer.compare(compare.x, this.x);
            }
            return Integer.compare(compare.y, this.y);
        }
    }
}