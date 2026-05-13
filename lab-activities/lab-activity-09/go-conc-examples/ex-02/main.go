package main

import (
	"fmt"
)
// Definiamo una struttura dati per i messaggi
type Msg struct {
	x int
	reply chan int
}

// Entrambi i client creano un canale locale questo canale locale viene messo sul campo "reply" del canale del server
func clientA_body(ch chan Msg) {
	fmt.Println("hello from client A")
    mychan := make(chan int)
	ch <- Msg{x: 1, reply: mychan} // send 
    res := <- mychan	// recive, il client si mette in attesa 
	fmt.Printf("res: %d \n", res)    
}

func clientB_body(ch chan Msg) {
	fmt.Println("hello from client B")
    mychan := make(chan int)
	ch <- Msg{x: 2, reply: mychan}
    res := <- mychan	
	fmt.Printf("res: %d \n", res)    
}

func server_body(ch chan Msg) {
	fmt.Println("hello from agent C")
	for {
		msg := <-ch
   		fmt.Printf("received %d ", msg.x)
    	msg.reply <- msg.x * 2
    }
}

func main() {

	c := make(chan Msg) // Questo è il canale del server 

	go server_body(c)
	go clientA_body(c)
	go clientB_body(c)

	for {}
}
