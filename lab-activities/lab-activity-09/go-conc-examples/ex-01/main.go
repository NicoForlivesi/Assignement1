package main

import (
	"fmt"
)

func agentA_body(ch chan int) {
	fmt.Println("hello from agent A")
	ch <- 1 // "<-" è l'operatore send, rappresenta l'informazione (1) che vogliamo mandare sul canale (ch)
}

func agentB_body(ch chan int) {
	fmt.Println("hello from agent B")
	ch <- 2
}
// Ad entrambe le funzioni viene passato un canale di interi (normalmente i canali sono dinamici)
func main() {

	c := make(chan int) // quando usiamo := sottointendiamo che il tipo viene inferito automaticamente

	// Si manda in esecuzione una "go routine" con la keyword go + nome_func.
	// Una go routine equivale ad un thread logico molto leggero (simil virtual thread), qui mandiamo in eseuzione
	// due go routine che condividono lo stesso canale, non vediamo esplicitamente come funzionano sotto le gor routine,
	// ma sono strutturate per sfruttare al massimo la concorrenza sulla macchina sulla quale vengono eseguite.
	go agentA_body(c)
	go agentB_body(c)

	a := <-c // recive (tipo inferito automaticamente), è bloccante, in generale i canali non hanno buffer.
	// vale anche per la send, quindi c'è sincronizzazione fra il main e le due go routine, finche non viene fatta la prima
	// recive, non potrà essere mandata in esecuzione la seconda send.
	b := <-c

	fmt.Printf("Received %d and %d\n", a, b)
}
