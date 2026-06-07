package pcd.lab05.monitors.ex_latch;

import java.util.ArrayList;
import java.util.List;

public class TestLatch {

	public static void main(String[] args) {
		
		int nThreadA = 1;
		int nThreadB = 10;

		Latch latch = new LatchImpl1(nThreadB)
        Latch latch2 = new LatchImpl2(nThreadB)
		
		List<ThreadA> threadsAlist = new ArrayList<ThreadA>();
		for (int i = 0; i < nThreadA; i++) {
			threadsAlist.add(new ThreadA("ThreadA-" + i, latch));
		}

		List<ThreadB> threadsBlist = new ArrayList<ThreadB>();
		for (int i = 0; i < nThreadB; i++) {
			threadsBlist.add(new ThreadB("ThreadB-" + i, latch));
		}

		for (ThreadA t: threadsAlist) {
			t.start();
		}
		
		for (ThreadB t: threadsBlist) {
			t.start();
		}
		
	}
}
