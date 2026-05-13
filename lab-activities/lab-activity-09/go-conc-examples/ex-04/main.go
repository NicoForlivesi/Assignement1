package main
// Questo esempi rappresenta una prova di performance
import (
	"fmt"
	"time"
)

type Msg struct {
	content  string
	senderId string
	time     time.Time
}

func MyAgent(ch chan Msg, id string) {
	msg := Msg{content: "Hello", senderId: id, time: time.Now()}
	ch <- msg
}
// Qui l'idea è: quando ogni agente crea un proprio canale, di isolare il tutto in una funzione e lanciare la 
// go routine da qui dentro, dal main quindi vedo solo che voglio fare lo spwan di un agente specificando il suo id
// che è una stringae questa factory dell'agente crea automaticamente il canale privato di ogni agente e lo restituisce 
// al main, quindi avremo un numero di canali uguale al numero di agenti.
func spawnMyAgent(id string) chan Msg {
	ch := make(chan Msg)
	go MyAgent(ch, id)
	return ch
}

func main() {
	fmt.Println("Booted.")

	numAgents := 1000000 // si creano tanti agenti 

	time0 := time.Now()

	/* a slice of channels */
	var channels []chan Msg

	/* spawning agents */
	for i := 0; i < numAgents; i++ {
		agentId := fmt.Sprintf("agent-%d", i)
		ch := spawnMyAgent(agentId)

		/* collecting agent channels */
		channels = append(channels, ch)
	}

	/* receiving messages */
	for i := 0; i < len(channels); i++ { // Per ogni canale creato vado a fare una recive.
		msg := <-channels[i]
		fmt.Printf("%s from %s at %s\n", msg.content, msg.senderId, msg.time.String())
	}
    
	time1 := time.Now()
	elapsed := time1.Sub(time0).Milliseconds()

	fmt.Printf("Done in: %d\n", elapsed)

}
