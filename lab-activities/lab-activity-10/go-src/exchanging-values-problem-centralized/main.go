/*
 *
 * Exchanging values problem (module-3.1)
 *
 * Centralized solution.
 *
 */

 // In questo esempio abbiamo N processi che si devono coordinare in modo che tutti conoscano il minimo e il massimo corrente 
package main

import (
	"fmt"
	"math/rand"
)

type MinMaxMsg struct {
	min int
	max int
}

// In questa versione centralizzata l'idea è che i peer svolgano il minor lavoro possbile.
// Tutti i peer sfruttano il canale del coordinatore ed un proprio canale privato
func Peer(id int, coord_ch chan int, my_ch chan MinMaxMsg) {
	v := rand.Intn(100)
	fmt.Printf("[Peer %d] my number is %d \n", id, v)
	coord_ch <- v // Il numero generato viene mandato con una send sul canale del coordinatore
	m := <-my_ch // Il peer aspetta di ricevere sul proprio canale il valore
	fmt.Printf("[Peer %d] Max is %d and min is %d \n", id, m.max, m.min)
}

func Coord(n_peers int, coord_ch chan int, channels []chan MinMaxMsg) { // Qui è facile perchè conosco il numero dei peer passato
	// come argomento al coordinatore
	max := -1 // minimo e massimo inizializzato su due valori fuori banda
	min := 101
	for i := 0; i < n_peers; i++ {
		val := <-coord_ch
		fmt.Printf("[Coord] received %d \n", val)
		if val < min {
			min = val
		} else if val > max {
			max = val
		}
	}
	fmt.Printf("[Coord] Max is %d and min is %d \n", max, min)
	for i := 0; i < n_peers; i++ {
		channels[i] <- MinMaxMsg{min: min, max: max}
	}
}

func main() {
	fmt.Println("Booted.")

	n_peers := 10

	coord_ch := make(chan int)
	channels := make([]chan MinMaxMsg, n_peers) // In realtà qua viene creato un array di canali di dimensione n_peers ma non 
	// vengono ancora inizializzati i singoli elementi, è necessario fare un for per creare i singoli elementi
	for i := 0; i < n_peers; i++ {
		channels[i] = make(chan MinMaxMsg)
	}

	go Coord(n_peers, coord_ch, channels)

	for i := 0; i < n_peers; i++ {
		go Peer(i, coord_ch, channels[i])
	}

	for {
	}
}
