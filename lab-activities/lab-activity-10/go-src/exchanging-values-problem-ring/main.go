/*
 * 
 * Exchanging values problem (module-3.1)  
 *
 * Ring solution.
 * 
 */
 package main
// Questa è la soluzione a via di mezzo fra la versione centralized e quella simmetrica 
import (
	"fmt"
	"math/rand"
)

type MinMaxMsg struct {
	min int
	max int
}

func Peer(id int, left_ch chan MinMaxMsg, right_ch chan MinMaxMsg)  {
  v := rand.Intn(100)
  min := v
  max := v
  fmt.Printf("[Peer %d] my number is %d \n", id, v)
  val := <- right_ch
  fmt.Printf("[Peer %d] Received %d %d \n", id, val.min, val.max)
  if val.min < min {
  	min = val.min
  }  
  if val.max > max {
  	max = val.max
  }  
  left_ch <- MinMaxMsg{ min: min, max: max} // Ogni peer prima fa un controllo rispetto ai propri valori e poi 
  // propaga il minimo e il massimo correnti sulla sinistra
  m := <- right_ch // Questo giro è per il minimo e massimo finali 
  fmt.Printf("[Peer %d] Max is %d and min is %d \n", id, m.max, m.min)
  left_ch <- m
}

func Coord(left_ch chan MinMaxMsg, right_ch chan MinMaxMsg)  {
  v := rand.Intn(100)
  min := v
  max := v
  fmt.Printf("[Coord] starting with min max %d %d\n", min, max)
  left_ch <- MinMaxMsg{ min: min, max: max} // Propaga sul canale di sinistra il valore iniziale di min e max
  m :=  <- right_ch // Aspetta di ricevere sul canale di destra dopo che i valori sono stati elaborati dai peers
  left_ch <- m // Ripropaga i valori finali di min e max sul canale di destra 
  fmt.Printf("[Coord] Max is %d and min is %d \n", m.max, m.min)
  <- right_ch // Bisogna fare una send fittizzia affinchè l'ultimo peer possa fare la recive
}


func main() {
	fmt.Println("Booted.")

	n_peers := 10
	channels := make([]chan MinMaxMsg, n_peers)
  	for i := range n_peers {
    	channels[i] = make(chan MinMaxMsg)
  	}

	go Coord(channels[1], channels[0])
	
	for i := 1; i < n_peers; i++ {
		go Peer(i, channels[(i+1)%n_peers], channels[i])
	}


	for {}
}