package pcd.lab07.vertx;

import java.util.List;

import io.vertx.core.*;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.file.FileSystem;

/**
 * 
 * Chaining async calls by using Vert.x futures (based on promises)
 * Qui vediamo un esempio di chaning delle promise (future per vertx, ma non c'entrano niente con le future di java, infatti
 * quella di java è sincrona, qui è completamente asincrona: non è mai bloccante, il bloccarsi è completamente in
 * contrasto rispetto all'idea del framework)
 */
class TestChain extends VerticleBase {
	
	public Future<?> start() throws Exception {
		FileSystem fs = vertx.fileSystem();    		

        // .compose è il metodo con cui posso concatenare i risultati, sono tutte chiamate asincrone che però tramite
        // il metodo .compose specifichiamo che vanno fatte solo quando ho il risultato dell'altra.
        // Questo è il modo per ottenere sequenzialità di chiamate asincrone (in questo caso l'output è deterministico!).
        // Tutto rimane non bloccante, la lettura dei file in background viene comunque fatta da worker threads
        // i risultati però vengono concatenati.
		fs.readFile("hello.md")
		.compose((Buffer buf) -> { // Compose vuole la lambda dove buf è il risultato della future restituita al passo
            // precedente, in questo caso ciò che restituisce il metodo asincrono .readFile
			log("1 - hello.md: \n" + buf.toString());
			return fs.readFile("pom.xml"); // Concateno altra chiamata asincrona (serve il "return"), per restituire
            // a compose a mia volta la future che restituisce readFile.
		}).compose((Buffer buf) -> {
			log("2 - POM: \n" + buf.toString().substring(0,160));
			return fs.readDir("src");
		}).onComplete((AsyncResult<List<String>> list) -> {
			log("3 - DIR: \n" + list.result());
		});
        // Questo è il modo per ottenere concatenazione dei risultati, un errore comune da evitare è il fare nesting
        // di callback, ovvero espandere lateralmente il codice, il metodo compose serve proprio per evitare questo
        // effetto. Riporto un esempio di come NON ottenere questo comportamento (anche se funziona)
        var f = fs.readFile("hello.md");
        f.onSuccess(buf -> {
            log("1 - hello.md: \n" + buf.toString());
            var f2 = fs.readFile("pom.xml");
            f2.onSuccess(buf2 -> {
                log("2 - POM: \n" + buf2.toString().substring(0,160));
                var f3 = fs.readDir("src");
                f3.onSuccess(list -> {
                    log("3 - DIR: \n" + list.toString());
                });
            });
        });
        // Tipico esempio di callback of hell!! Usare il metodo .compose

		return super.start();
	}

	private void log(String msg) {
		System.out.println("[ " + System.currentTimeMillis() + " ][ " + Thread.currentThread() + " ] " + msg);
	}
}

public class Step3_chaining {

	public static void main(String[] args) {
		Vertx  vertx = Vertx.vertx();
		vertx.deployVerticle(new TestChain()).onSuccess((res -> {
            System.out.println("Reactive agent deployed.");
        }));
	}
}

