package main

import (
    "math/rand"
    "time"
)

func spawnPlayer(id int) chan PlayRequest {
    ch := make(chan PlayRequest)
    go playerBody(id, ch)
    return ch
}

func playerBody(id int, ch chan PlayRequest) {
    r := rand.New(rand.NewSource(time.Now().UnixNano() + int64(id)))

    for {
        req := <-ch
        // Qui il giocatore sa il suo ruolo ma in realtà non è determinante per fare la scelta del numero,
        // quindi non viene neanche letto dal canale
        n := r.Intn(6) // numero casuale tra 0 e 5, generato ad ogni nuovo match ed indipendete dalla scelta fatta nel match precedente
        req.Reply <- n // Risponde sul canale creato dal coordinatore
    }
}
