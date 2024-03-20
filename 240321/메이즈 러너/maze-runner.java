import java.io.*;
import java.util.*;

public class Main {
    static int[][] del = {{-1, 0}, {1, 0}, {0, -1}, {0, 1}};
    static int n, m, distSum;
    static int[][] map;
    static Node exit;
    static class Node {
        int r;
        int c;
        int dist;

        public Node(int r, int c) {
            this.r = r;
            this.c = c;
        }
    }

    public static void main(String[] args) throws Exception {
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        StringTokenizer st = new StringTokenizer(br.readLine());
        int k;
        n = Integer.parseInt(st.nextToken());
        m = Integer.parseInt(st.nextToken());
        k = Integer.parseInt(st.nextToken());
        map = new int[n + 1][n + 1];
        Set<Integer>[][] positions = new Set[n + 1][n + 1];
        for (int i = 1; i <= n; i++) {
            st = new StringTokenizer(br.readLine());
            for (int j = 1; j <= n; j++) {
                positions[i][j] = new HashSet<>();
                map[i][j] = Integer.parseInt(st.nextToken());
            }
        }

        Map<Integer, Node> participents = new HashMap();
        for (int i = 1; i <= m; i++) {
            st = new StringTokenizer(br.readLine());
            int r, c;
            r = Integer.parseInt(st.nextToken());
            c = Integer.parseInt(st.nextToken());
            positions[r][c].add(i);
            participents.put(i, new Node(r, c));
        }

        st = new StringTokenizer(br.readLine());
        exit = new Node(Integer.parseInt(st.nextToken()), Integer.parseInt(st.nextToken()));

        int idx = 1;
        while (k-- > 0) {
            // 1. 참가자 이동
            move(positions, participents);

            // 2. 참가자 탈출
            exitParticipents(positions, participents);

            // 3. 참가자 탈출 상태 확인
            if (participents.isEmpty()) {
                printRes();
                return;
            }

            // 4. 맵 회전
            rotate(positions, participents);
        }

        for (Node participent: participents.values()) distSum += participent.dist;
        printRes();
    }

    private static void exitParticipents(Set<Integer>[][] positions, Map<Integer, Node> participents) {
        List<Integer> finished = new ArrayList<>();
        for (int key : participents.keySet()) {
            Node participent = participents.get(key);
            if (participent.r == exit.r && participent.c == exit.c) {
                positions[participent.r][participent.c].remove(key);
                finished.add(key);
            }
        }
        for (int participent : finished) participents.remove(participent);
    }

    private static void move(Set<Integer>[][] positions, Map<Integer, Node> participents) {
        for (int key : participents.keySet()) {
            Node participent = participents.get(key);
            int minDist = Math.abs(exit.r - participent.r) + Math.abs(exit.c - participent.c);
            int moveR = -1, moveC = -1;
            for (int i = 0; i < 4; i++) {
                int nr = participent.r + del[i][0];
                int nc = participent.c + del[i][1];
                if (nr <= 0 || nr > n || nc <= 0 || nc > n || map[nr][nc] != 0) continue;
                int nDist = Math.abs(nr - exit.r) + Math.abs(nc - exit.c);
                if (minDist > nDist) {
                    moveR = nr;
                    moveC = nc;
                    minDist = nDist;
                }
            }

            if (moveR != -1 && map[moveR][moveC] == 0) {
                positions[participent.r][participent.c].remove(key);
                positions[moveR][moveC].add(key);
                participent.r = moveR;
                participent.c = moveC;
                distSum++;
            }
        }
    }

    private static void rotate(Set<Integer>[][] positions, Map<Integer, Node> participents) {
        Node startPosition = findStartPosition(participents);
        int len = startPosition.dist + 1;
        int r = startPosition.r;
        int c = startPosition.c;

        while (len >= 1) {
            if (len == 1 && map[r][c] > 0) map[r][c]--;

            for (int i = r; i < r + len - 1; i++) {
                int tmp = map[i][c];
                Node tmpExit = null;
                if (exit.r == i && exit.c == c) tmpExit = new Node(exit.r, exit.c);
                if (tmp > 0) tmp--;
                Set<Integer> tmpPosition = positions[i][c];

                int a = r + len - 1;
                int b = c + i - r;
                if (map[a][b] > 0) map[a][b]--;
                map[i][c] = map[a][b];
                positions[i][c] = positions[a][b];
                rotateExit(a, b, i, c);

                a = r + len - 1 - (i - r);
                b = c + len - 1;
                if (map[a][b] > 0) map[a][b]--;
                map[r + len - 1][c + i - r] = map[a][b];
                positions[r + len - 1][c + i - r] = positions[a][b];
                rotateExit(a, b, r + len - 1, c + i - r);

                a = r;
                b = c + len - 1 - (i - r);
                if (map[a][b] > 0) map[a][b]--;
                map[r + len - 1 - (i - r)][c + len - 1] = map[a][b];
                positions[r + len - 1 - (i - r)][c + len - 1] = positions[a][b];
                rotateExit(a, b, r + len - 1 - (i - r), c + len - 1);

                map[a][b] = tmp;
                positions[a][b] = tmpPosition;
                if (tmpExit != null) rotateExit(i, c, a, b);
            }

            len -= 2;
            r++;
            c++;
        }

        for (int i = 1; i <= n; i++) {
            for (int j = 1; j <= n; j++) {
                for (int num : positions[i][j]) {
                    participents.get(num).r = i;
                    participents.get(num).c = j;
                }
            }
        }
    }

    private static void rotateExit(int r, int c, int toR, int toC) {
        if (exit.r == r && exit.c == c) {
            exit.r = toR;
            exit.c = toC;
        }
    }

    private static Node findStartPosition(Map<Integer, Node> participents) {
        int minLen = Integer.MAX_VALUE;
        Node startPosition = new Node(n + 1, n + 1);

        for (int key : participents.keySet()) {
            Node participent = participents.get(key);
            int tmpLen = Math.max(Math.abs(participent.r - exit.r), Math.abs(participent.c - exit.c));
            if (minLen >= tmpLen) {
                Node tmpStartPosition = new Node(0, 0);
                if (participent.r <= exit.r) {
                    tmpStartPosition.r = exit.r - tmpLen;
                    if (tmpStartPosition.r < 1) {
                        tmpStartPosition.r = 1;
                    }
                }
                else {
                    tmpStartPosition.r = participent.r - tmpLen;
                    if (tmpStartPosition.r < 1) tmpStartPosition.r = 1;
                }

                if (participent.c <= exit.c) {
                    tmpStartPosition.c = exit.c - tmpLen;
                    if (tmpStartPosition.c < 1) tmpStartPosition.c = 1;
                }
                else {
                    tmpStartPosition.c = participent.c - tmpLen;
                    if (tmpStartPosition.c < 1) tmpStartPosition.c = 1;
                }

                if (minLen > tmpLen
                        || minLen == tmpLen && startPosition.r > tmpStartPosition.r
                        || minLen == tmpLen && startPosition.r == tmpStartPosition.r && startPosition.c > tmpStartPosition.c) {
                    minLen = tmpLen;
                    startPosition = tmpStartPosition;
                    startPosition.dist = minLen;
                }
            }
        }
        return startPosition;
    }

    private static void printRes() {
        System.out.println(distSum);
        System.out.println(exit.r + " " + exit.c);
    }
}