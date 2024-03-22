import java.util.*;
import java.io.*;

/*
사내 메신저 구조는 이진 트리로 되어있다,



0번 채팅방이 최상단 노드,
parents 배열로 각 노드의 부모 정보가 주어짐

각 채팅방은 권한을 가지고 있음, 
c번 채팅방에서 메세지를 보내면 그 채팅방의 상위 채팅방들에게 알림이 가는데, authority c 만큼 위로 올라가며 알림을 보냄

authority 값 역시 배열로 값이 주어짐

알림망 설정
처음 모든 채팅방의 알림망 설정은 켜져있다
알림망 설정 기능이 작동되면, c번 채팅방의 알림망 설정이 on이라면 off로, off라면 on으로 바꿔준다
알림망 설정이 off가 되면 자기 자신을 포함하여 아래에서 올라온 모든 알림을 더이상 위로 올려보내지 않는다

권한 세기 변경
c번 채팅방의 권한 세기를 power로 변경

부모 채팅방 교환
c1번과 c2번 채팅방의 부모를 교환한다

알림을 받을 수 있는 채팅방 수 조회
메세지를 보냈을 때, c번 채팅방까지 알림이 도달할 수 있는 채팅방 수 출력

q번에 걸쳐 명령을 순서대로 진행하며 알맞은 답을 출력

*/

public class Main {
    static final BufferedReader BR = new BufferedReader(new InputStreamReader(System.in));
    static final BufferedWriter BW = new BufferedWriter(new OutputStreamWriter(System.out));

    static final int INIT_MESSENGER = 100;
    static final int TOGGLE_NOTI = 200;
    static final int UPDATE_AUTHORITY = 300;
    static final int SWAP_PARENT = 400;
    static final int GET_NOTI = 500;

    int[] inputArray;
    int N, Q;
    int[] parent, authority;
    boolean[] noti;
    HashMap<Integer, HashSet<Integer>> childMap;
    public static void main(String[] args) throws IOException {
        Main main = new Main();
        main.init();
        main.solve();
    }

    int[] getInputArray() throws IOException {
        return Arrays.stream(BR.readLine().split(" ")).mapToInt(Integer::parseInt).toArray();
    }

    void init() throws IOException {
        inputArray = getInputArray();
        N = inputArray[0];
        Q = inputArray[1];
        parent = new int[N+1];
        authority = new int[N+1];
        noti = new boolean[N+1];
        childMap = new HashMap<>();
        for (int i = 0; i <= N; i++) {
            parent[i] = -1;
            authority[i] = -1;
            noti[i] = true;
            childMap.put(i, new HashSet<>());
        }
    }

    void solve() throws IOException {
        for (int i = 0; i < Q; i++) {
            inputArray = getInputArray();
            int cmd = inputArray[0];
            if (cmd == INIT_MESSENGER) {
                initMessenger(inputArray);
            }
            if (cmd == TOGGLE_NOTI) {
                toggleNoti(inputArray[1]);
            }
            if (cmd == UPDATE_AUTHORITY) {
                updateAuthority(inputArray[1], inputArray[2]);
            }
            if (cmd == SWAP_PARENT) {
                swapParent(inputArray[1], inputArray[2]);
            }
            if (cmd == GET_NOTI) {
                getNoti(inputArray[1]);
            }
        }
        BW.flush();
        BW.close();
        BR.close();
    }

    void initMessenger(int[] inputArray) {
        for (int i = 1; i <= N; i++) {
            int p = inputArray[i];
            parent[i] = p;
            childMap.get(p).add(i);
        }
        for (int i = N+1; i <= 2 * N; i++) {
            authority[i - N] = inputArray[i];
        }

    }

    void toggleNoti(int c) {
        noti[c] = !noti[c];
    }

    void updateAuthority(int c, int power) {
        authority[c] = power;
    }

    void swapParent(int c1, int c2) {
        int p1 = parent[c1];
        int p2 = parent[c2];
        childMap.get(p1).remove(c1);
        childMap.get(p2).remove(c2);
        parent[c1] = p2;
        parent[c2] = p1;
        childMap.get(p1).add(c2);
        childMap.get(p2).add(c1);
    }

    void getNoti(int c) throws IOException {
        List<Integer> currentNodes = new ArrayList<>();
        for (Integer child : childMap.get(c)) {
            currentNodes.add(child);
        }

        int answer = 0;
        int depth = 1;
        while (!currentNodes.isEmpty()) {
            List<Integer> temp = new ArrayList();
            for (Integer node : currentNodes) {
                if (!noti[node]) {
                    continue;
                }
                if (depth <= authority[node]) {
                    answer += 1;
                }
                for (Integer child : childMap.get(node)) {
                    temp.add(child);
                }
            }
            depth += 1;
            currentNodes = temp;
        }
        BW.write(Integer.toString(answer)+"\n");

    }
}