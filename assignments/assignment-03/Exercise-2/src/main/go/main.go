package main

func main() {
    N := 8
    players := make([]chan PlayRequest, N)

    for i := 0; i < N; i++ {
        players[i] = spawnPlayer(i)
    }

    runTournament(players)
}
