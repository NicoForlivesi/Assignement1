package main

func main() {
    N := 8
    players := make([]chan PlayRequest, N) // Come abbiamo visto a lezione è necessario il for dopo
    // che crea realmente i singoli elementi (delegato ciò a "spawnPlayer")

    for i := 0; i < N; i++ {
        players[i] = spawnPlayer(i)
    }

    runTournament(players) // Chiamata senza keyword "go" il che vuol dire che viene eseguita in modo sincrono
    // se si chiamasse con "go" davanti, il main terminerebbe prima della fine del torneo
}
