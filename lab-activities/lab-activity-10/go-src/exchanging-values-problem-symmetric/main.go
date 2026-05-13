/*
 * 
 * Exchanging values problem (module-3.1)  
 *
 * Symmetric solution.
 * 
 */
package main
// A differenza della versione centralized qui l'idea è che tutti mandano il valore agli altri, non c'è nessun
// coordinatore, sono i peer che si occupano di comunicare fra loro
import (
	"fmt"
	"math/rand"
)

// Ogni peer fa una send su tutti i canali tranne il proprio
func Peer(id int, channels []chan int)  {
  v := rand.Intn(100)
  fmt.Printf("[Peer %d] my number is %d \n", id, v)

  for i := 0; i < len(channels); i++ {
    if i != id {
      fmt.Printf("[Peer %d] sending to channel %d \n", id, i)
      channels[i] <- v
    }    
  }

  my_peers := len(channels) - 1
  min := v
  max := v
  for i := 0; i < my_peers; i++ {
    val := <- channels[id]
    fmt.Printf("[Peer %d] Received %d\n", id, val)
    if val < min {
    	min = val
    }  
    if val > max {
    	 max = val
    }  
  }
  fmt.Printf("[Peer %d] Max is %d and min is %d \n", id, max, min)
}


func main() {
	fmt.Println("Booted.")

	n_peers := 10
	channels := make([]chan int, n_peers) // Importante nell'assignement (se si usano array di canali) creare per ogni 
  // elemento esplicitamente un canale.
  for i := 0 ; i < n_peers; i++ {
    channels[i] = make(chan int, n_peers) // Qui n_peers è il buffer se non si usasse buffer qui andrebbe in deadlock.
    // Infatti senza buffer la send è bloccante e tutti i peers si metterebbero a fare la send senza che nessuno arrivi a fare 
    // la recive visto che nel codice del peer facciamo per tutti prima "channels[i] <- v". 
    // Non basta mettere come buffer 1, ogni peer deve avere la possibilità di ricevere messaggi da tutti gli altri senza bloccare
    // chi sta cercando di mandare un messaggi, quindi il buffer deve essere almeno n_peers - 1
  }

	for i := 0; i < n_peers; i++ {
		go Peer(i, channels)
	}


	for {} // Loop infinito che serve per non far terminare il main, in Go se il main termina vengono killate tutte le 
  // Go routine rimaste in esecuzione, in realtà la cosa più elegante sarebbe creare un canale "done" in cui il main rimane in 
  // attesa del messaggio che gli permetta di concludere, tipo un "finish"
}
