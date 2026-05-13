package main
// Qui introduciamo l'uso della "select"
import (
	"fmt"
)

type Msg struct {
	x,y int
	reply chan int
}

func client_body(xv, yv int, ch chan Msg) {
	fmt.Println("hello from client")
    mychan := make(chan int)
	ch <- Msg{x: xv, y: yv, reply: mychan}
    res := <- mychan	
	fmt.Printf("res: %d \n", res)    
}


func server_body(ch1 chan Msg, ch2 chan Msg) {
	fmt.Println("hello from agent C")
	for {
		select { // Qui select: nel caso in cui facessimo le due recive senza select, col fatto che è bloccante, 
			// la prima recive si bloccherebbe e saremmo impossibilitati da ricevere la seconda, tramite la select invece
			// il server si mette in ascolto su entrambi i canali
		case msg := <- ch1:
   			fmt.Printf("[add] received %d %d \n", msg.x, msg.y)
    		msg.reply <- msg.x + msg.y
		case msg := <- ch2:
   			fmt.Printf("[mul] received %d %d \n", msg.x, msg.y)
    		msg.reply <- msg.x * msg.y
    	}
    }
}

func main() {

	c_mul := make(chan Msg)
	c_add := make(chan Msg)

	go server_body(c_add, c_mul) // Il server ascolta su due canali di tipo diverso, su un canale il server
	// fa le somme e sull'altro le moltiplicazioni
	go client_body(2, 3, c_add)
	go client_body(2, 3, c_mul)

	for {}
}
// notare che la send fatta dal client è bloccante e non essendoci il buffer viene fatta solo quando è presente 
// una recive dall'altra parte 


/* OUTPUT:
hello from agent C
hello from client
[add] received 2 3 
hello from client
[mul] received 2 3 
res: 5 
res: 6
*/
