	package pcd.lab07.vertx;

import java.io.*;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.file.FileSystem;

/*
* In questo esempio viene usato un approccio molto low level di vertx (che ci dimenticheremo alla svelta, in questa
* tipologia d'uso infatti non garantiamo l'incapsulamento che vorremmo), comunque l'esempio fornisce un
* esempio base dell'uso del framework.
*/
public class Step1_basic {

	public static void main(String[] args) {
		
		System.out.println(new File(".").getAbsoluteFile());
		
		Vertx  vertx = Vertx.vertx(); // Con la chiamata Vertx.vertx() creiamo un componente attivo con un
        // suo event-loop che possiamo usare per fare chiamate asincrone specificando e agganciando le callback
        // come vogliamo noi

		FileSystem fs = vertx.fileSystem(); // FileSystem è una libreria messa a disposizione da Vertx

		log("doing the async call... ");
		
		Future<Buffer> fut = fs.readFile("hello.md"); // Chiamata asincrona a readFile, non c'è nessuna keyword
        // che specifica che sia asincrono perchè in Vertx tutto lo è !!!
        // NON BLOCCO L'EVENT-LOOP!!! Ma specifico cosa fare quando sarà pronto il risultato (fut.onComplete(...))
        /*
        * Il metodo asincrono "readFile" restituisce quella che nelle slide abbiamo chiamato "promise" qui si chiama
        * future ma è importante sottolineare che ai nostri occhi sono a tutti gli effetti ciò che abbiamo
        * chiamato promise. NON SONO LE FUTURE SINCRONE di java DOVE MI POSSO SOSPENDERE CON UNA "GET", sono promise.
        * Quindi questo oggetto future restituito da readFile è logico aspettarsi che sia configurabile con una
        * callback (ovvero specificare quando è pronto il risultato cosa fare)
        * */

		fut.onComplete((AsyncResult<Buffer> res) -> {	// E' la .then che abbiamo visto nelle slide
			log("hello.md content: \n" + res.result().toString());
		});
        /*
        * Oltre a .onComplete abbiamo .onSuccess e .onFailure: onComplete controlla solo che la future sia completa,
        * ovvero che la chiamata asincrona a readFile abbia finito il suo lavoro, onSuccess invece controlla che il
        * risultato sia di successo e onFailure tipicamente lo usiamo per gestire gli errori lanciando eccezioni.
        * */

		log("async call done. Waiting some time... ");

		try { // Qui blocchiamo volontariamente il thread principale per evidenziare il comportamento del thread
            // in background che si incarica di servire la chiamata asincrona
			Thread.sleep(1000);
		} catch (Exception ex) {}
		
		log("done");
	}
	
	private static void log(String msg) {
		System.out.println("[ " + System.currentTimeMillis() + " ][ " + Thread.currentThread() + " ] " + msg);
	}
}
// CallBack = È una funzione passata come argomento a un’operazione asincrona.
    // Se avessimo passato una funzione a readFile, quella sarebbe stata la callback
// Handler = È la funzione che registri per gestire un evento (in questo caso la lambda passata a .onComplete)

    /*
    * Usare Vertx in poche parole consiste nel fare chiamate asincrone che restituiscono promise e definire cosa
    * fare quando quella chiamata asincrona viene completata (o quando ci sono errori)
    * */

/*
* OUTPUT:
* [ 1777370890369 ][ Thread[#3,main,5,main] ] doing the async call...
[ 1777370890385 ][ Thread[#3,main,5,main] ] async call done. Waiting some time...
[ 1777370890425 ][ Thread[#42,vert.x-eventloop-thread-0,5,main] ] hello.md content:
hello, world! --(E' l'event-loop che esegue l'handler !!! Non il main, ma è concorrente al main)--

[ 1777370891387 ][ Thread[#3,main,5,main] ] done
*/

    // Qual'è il problema di questo esempio??
    /* Il flusso di controllo è abbastanza opaco, guardando il codice a primo impatto non è ben chiaro quali parti
    vengono eseguite dal main thread e quali dall'event-loop, vogliamo avere un approccio più "incapsulato", come fare
    questo? --> attraverso il concetto di VERTICLE (modus operandi classico da usare con Vertx)
    * */
