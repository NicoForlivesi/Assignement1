package main

import (
    "fmt"
    "math/rand"
)

type PlayRequest struct {
    Role  int // 0 = pari, 1 = dispari
    Reply chan int
}

func runTournament(players []chan PlayRequest) {

    var current []int
    for i := range players {
        current = append(current, i)
    }
	round := 1
    for len(current) > 1 { // Quando current contiene un solo elemento quello è l'id del vincitore

		fmt.Printf("\n=========================\n")
        fmt.Printf("🏁  ROUND %d — %d giocatori\n", round, len(current))
        fmt.Printf("=========================\n")

        rand.Shuffle(len(current), func(i, j int) { // Shuffle dell'array contenente i giocatori ancora in gioco per
		// avere matchmaking casuale 
            current[i], current[j] = current[j], current[i]
        })

        results := make(chan int)
		logs := make(chan string)
        next := []int{}

        for i := 0; i < len(current); i += 2 { // Avvio di tutti i match in parallelo con len(current)/2 go routine
            go playMatch(current[i], current[i+1], players, results, logs)
        }

        // Raccolgo log e vincitori in ordine
        for i := 0; i < len(current)/2; i++ {
            fmt.Print(<-logs)      // stampa ordinata
            next = append(next, <-results)
        }

		if len(next) > 1 { fmt.Printf("\n➡️  Avanzano: %v\n", next) }
        current = next
		round++
    }
    fmt.Printf("\n===============================\n")
    fmt.Printf("👑  CAMPIONE ASSOLUTO: Player %d\n", current[0])
    fmt.Printf("===============================\n")
}

func playMatch(p1, p2 int, players []chan PlayRequest, results chan int, logs chan string) {
	log := ""
	log += fmt.Sprintf("\n⚔️  MATCH: Player %d vs Player %d\n", p1, p2)
    // Il primo player sceglie casualmente pari/dispari
    roleP1 := rand.Intn(2)      // 0 = pari, 1 = dispari
    roleP2 := 1 - roleP1        // ruolo opposto

	log += fmt.Sprintf("   • Player %d è '%s'\n", p1, roleName(roleP1))
    log += fmt.Sprintf("   • Player %d è '%s'\n", p2, roleName(roleP2))

    reply1 := make(chan int)
    reply2 := make(chan int)

    // Invio ruolo e chiedo numero
    players[p1] <- PlayRequest{Role: roleP1, Reply: reply1}
    players[p2] <- PlayRequest{Role: roleP2, Reply: reply2}

    n1 := <-reply1
    n2 := <-reply2

	log += fmt.Sprintf("   • Player %d sceglie %d\n", p1, n1)
    log += fmt.Sprintf("   • Player %d sceglie %d\n", p2, n2)

    sum := n1 + n2
	log += fmt.Sprintf("   • Somma = %d → %s\n", sum, parity(sum))

    // Se somma pari, vince chi ha ruolo pari (0)
    // Se somma dispari, vince chi ha ruolo dispari (1)
    var winner int
	if roleP1 == sum % 2 {
    	winner = p1
	} else {
    	winner = p2
	}

    log += fmt.Sprintf("🏆  VINCE Player %d!\n", winner)
    logs <- log // Passo al canale tutti i log del determinato match per mantenerne l'ordine 
    results <- winner
}

func roleName(role int) string {
    if role == 0 {
        return "PARI"
    }
    return "DISPARI"
}

func parity(n int) string {
    if n%2 == 0 {
        return "PARI"
    }
    return "DISPARI"
}

