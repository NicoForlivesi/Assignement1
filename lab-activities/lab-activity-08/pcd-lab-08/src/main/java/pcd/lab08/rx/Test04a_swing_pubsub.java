package pcd.lab08.rx;

import javax.swing.*;

import io.reactivex.rxjava3.schedulers.Schedulers;
import io.reactivex.rxjava3.subjects.PublishSubject;

import java.awt.event.*;
import java.util.concurrent.TimeUnit;

// Un esempio in cui viene aggianciato un flusso di eventi ad una GUI

public class Test04a_swing_pubsub {	

	static class MyFrame extends JFrame {	

		private PublishSubject<Integer> stream;
		
		public MyFrame(PublishSubject<Integer> stream){
			super("Swing + RxJava");
			this.stream = stream;
			setSize(150,60);
			setVisible(true);
			JButton button = new JButton("Press me");
			button.addActionListener((ActionEvent ev) -> {
				stream.onNext(1);
			});
			getContentPane().add(button);
			addWindowListener(new WindowAdapter(){
				public void windowClosing(WindowEvent ev){
					System.exit(-1);
				}
			});
		}
	}

	static public void main(String[] args){
		
		PublishSubject<Integer> clickStream = PublishSubject.create(); // per agganciare stream asincroni a GUI è molto comodo
		
		SwingUtilities.invokeLater(()->{
			new MyFrame(clickStream);
		});

        // Gestione caso click
		clickStream
			.observeOn(Schedulers.computation())
			.subscribe((v) -> {
				System.out.println(Thread.currentThread().getName() + "click: "+System.currentTimeMillis());
			});

        // Gestione caso multiclick in 250ms
		clickStream
			.buffer(clickStream.throttleWithTimeout(250, TimeUnit.MILLISECONDS))
                // buffer è un operatore che raggruppa gli elementi di un flusso secondo le proprietà che specifchiamo.
			.map(xs -> xs.size())
			.filter((v) -> v >= 2)
			.subscribe((v) -> {
				System.out.println(Thread.currentThread().getName() + ": Multi-click: "+v);
			});
	}

}
