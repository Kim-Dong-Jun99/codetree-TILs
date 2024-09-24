import java.util.*;
import java.io.*;

public class Main {
	static final BufferedReader BR = new BufferedReader(new InputStreamReader(System.in));
	static final BufferedWriter BW = new BufferedWriter(new OutputStreamWriter(System.out));

	int[] inputArray;
	int Q, N, M, start;
	Map<Integer, Tour> tourMap;
	List<Edge>[] edges;
	PriorityQueue<TourItem> tourItemHeap;
	int[] distance;


    public static void main(String[] args) throws IOException {
		Main main = new Main();
		main.init();
		main.solve();
		BW.flush();
    }

	int[] getInputArray() throws IOException {
		return Arrays.stream(BR.readLine().split(" ")).mapToInt(Integer::parseInt).toArray();
	}

	void init() throws IOException {
		Q = Integer.parseInt(BR.readLine());
		start = 0;
		tourMap = new HashMap<>();
		tourItemHeap = new PriorityQueue<>(TourItem::compare);
	}

	void solve() throws IOException {
		while (Q-- > 0) {
			inputArray = getInputArray();
			if (inputArray[0] == 100) {
				initializeGraph();
				dijkstra();
			}
			if (inputArray[0] == 200) {
				addTourItem(new Tour(inputArray[1], inputArray[2], inputArray[3]));
			}
			if (inputArray[0] == 300) {
				int id = inputArray[1];
				deleteTourItem(id);
			}
			if (inputArray[0] == 400) {
				findBestTourItem();
			}
			if (inputArray[0] == 500) {
				start = inputArray[1];
				dijkstra();
				reinitTourItemHeap();
			}
		}
	}

	void initializeGraph() {
		N = inputArray[1];
		M = inputArray[2];
		edges = new List[N];
		int[][] weights = new int[N][N];
		for (int i = 0; i < N; i++) {
			Arrays.fill(weights[i], Integer.MAX_VALUE);
			edges[i] = new ArrayList<>();
		}
		for (int i = 3; i < inputArray.length; i += 3) {
			int v, u, w;
			v = inputArray[i];
			u = inputArray[i+1];
			w = inputArray[i+2];
			if (v == u) {
				continue;
			}
			weights[v][u] = Math.min(weights[v][u], w);
			weights[u][v] = Math.min(weights[u][v], w);
		}

		for (int i = 0; i < N; i++) {
			for (int j = 0; j < N; j++) {
				if (weights[i][j] != Integer.MAX_VALUE) {
					edges[i].add(new Edge(j, weights[i][j]));
				}
			}
		}
	}

	void dijkstra() {
		PriorityQueue<Node> nodeHeap = new PriorityQueue<>(Node::compare);
		distance = new int[N];
		Arrays.fill(distance, Integer.MAX_VALUE);
		nodeHeap.add(new Node(start, 0));
		distance[start] = 0;
		while (!nodeHeap.isEmpty()) {
			Node node = nodeHeap.remove();
			if (distance[node.to] < node.weight) {
				continue;
			}
			for (Edge e : edges[node.to]) {
				if (distance[e.to] > distance[node.to] + e.weight) {
					distance[e.to] = distance[node.to] + e.weight;
					nodeHeap.add(new Node(e.to, distance[node.to] + e.weight));
				}
			}
		}
	}

	void addTourItem(Tour tour) {
		tourMap.put(tour.id, tour);
		if (distance[tour.dest] == Integer.MAX_VALUE) {
			return;
		}
		int profit = tour.revenue - distance[tour.dest];
		if (profit >= 0) {
			tourItemHeap.add(new TourItem(tour.id, profit));
		}
	}

	void deleteTourItem(int id) {
		tourMap.remove(id);
	}

	void findBestTourItem() throws IOException {
		while (!tourItemHeap.isEmpty()) {
			TourItem ti = tourItemHeap.remove();
			if (!tourMap.containsKey(ti.id)) {
				continue;
			}
			deleteTourItem(ti.id);
			BW.write(Integer.toString(ti.id));
			BW.write("\n");
			return;
		}
		BW.write("-1\n");
	}

	void reinitTourItemHeap() {
		tourItemHeap = new PriorityQueue<>(TourItem::compare);
		for (Integer id : tourMap.keySet()) {
			Tour tour = tourMap.get(id);
			if (distance[tour.dest] == Integer.MAX_VALUE) {
				continue;
			}
			int profit = tour.revenue - distance[tour.dest];
			if (profit >= 0) {
				tourItemHeap.add(new TourItem(tour.id, profit));
			}
		}
	}	

	static class Node {
		int to;
		int weight;

		Node(int to, int weight) {
			this.to = to;
			this.weight = weight;
		}

		int compare(Node compare) {
			return Integer.compare(this.weight, compare.weight);
		}
	}

	static class Edge {
		int to;
		int weight;

		Edge(int to, int weight) {
			this.to = to;
			this.weight = weight;
		}
	}

	static class Tour {
		int id;
		int revenue;
		int dest;

		Tour(int id, int revenue, int dest) {
			this.id = id;
			this.revenue = revenue;
			this.dest = dest;
		}
	}

	static class TourItem {
		int id;
		int profit;

		TourItem(int id, int profit) {
			this.id = id;
			this.profit = profit;
		}

		int compare(TourItem compare) {
			if (this.profit != compare.profit) {
				return Integer.compare(compare.profit, this.profit);
			}
			return Integer.compare(this.id, compare.id);
		}
	}
}