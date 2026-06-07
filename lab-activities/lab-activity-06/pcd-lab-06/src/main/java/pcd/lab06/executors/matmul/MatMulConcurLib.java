package pcd.lab06.executors.matmul;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class MatMulConcurLib {

	private static MatMulConcurLib instance;
	private ExecutorService exec;

	public static MatMulConcurLib getInstance(){
		synchronized (MatMulConcurLib.class) {
			if (instance == null) {
				instance = new MatMulConcurLib();
			} 
			return instance;
		}
	}

	private MatMulConcurLib() {
	}
	
	public Mat matmul(Mat matA, Mat matB) throws MatMulException {
		Mat matC = new Mat(matA.getNRows(), matB.getNColumns());
		exec = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors() + 1);		
		try {
			for (int i = 0; i < matA.getNRows(); i++){
				for (int j = 0; j < matB.getNColumns(); j++){
					exec.execute(new ComputeElemTask(i,j,matA,matB,matC));
					
					// Alternative: using a lambda expression to specify the task
					/* 					
					exec.execute(() -> {
						double sum = 0;
						for (int k = 0; k < matA.getNColumns(); k++){
							sum += matA.get(i, k)*matB.get(k, j);
						}
						matC.set(i,j,sum);
					});
					*/
				}
			}
			exec.shutdown();// Chiudo l'executor il che significa che non verranno messi dentro altri task.
			exec.awaitTermination(Long.MAX_VALUE,TimeUnit.SECONDS); // Chiuso l'executor dico: Aspetta la terminazione
            // dei task che hai sotto.
            // è un metodo per aspettare la terminazione dei task che si usa solo in situazioni molto semplici.
			return matC;
		} catch (Exception ex){
			throw new MatMulException();
		}
	}
}
