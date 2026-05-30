PCD a.y. 2025-2026 - ISI LM UNIBO - Cesena Campus

# Assignment #04

v1.0.0-20260517

The assignment is about distributed programming, il primo esercizio prevede lo scambio di messaggi
nel caso distribuito, il secondo si basa sul distributed object computing, ovvero sul concetto di
sfruttare la programmazione ad oggetti ma in un contesto distribuito.

### Exercise #1 - *Distributed Smart Home Alarm System*

- [Description](https://github.com/nicolasfara/seminar-pcd-actor-pekko-code/blob/master/assignment_4_smart_home_alarm_cluster.md) by N. Farabegoli
- L'obiettivo è quello di estendere l'esercizio 1 dell'assignment 3 (quello del sistema d'allarme) ma in 
un contesto distribuito, la logica deve essere la stessa, per quanto riguarda la logica funzionale quindi è
equivalente all'esercizio precedente.
- Quello che cambia è il modello, l'esercizio va implementato utilizzando "pekko cluster" (con almeno 3
nodi), guarda la descrizione per indicazioni precise su come strutturare il sistema.

### Exercise #2 - *Distributed TTT with Java RMI*

We want to implement a distributed system for playing Tic-Tac-Toe: 
- A player that aims at play a game can create a new game with some name, waiting for opponents
- A player can join an existing game, given its name
- L'idea è che il server possa supportare anche più partite contemporaneamente.
- L'UI può essere anche testuale, l'importante è che ci sia un ritorno (tipo se l'avversario fa una mossa il
giocatore deve avere un feedback di questa mossa)
- Qui si parla di "oggetti distribuiti" dove le chiamate ai metodi sono sincrone (un po come se fossero
delle remote procedure call, qui le chiamiamo remote method invocation)
- Ciò che non è specificato è lasciato a modellazione nostra.

The system should be designed according the distributed object computing and concurrent programming principles discussed in the course, using Java RMI as underlying RPC mechanism.
 
### **[Optional]**  Exercise #3 - *Distributed Critical Sections with a Message-Oriented Middleware* 

Implement a simple high-level middleware providing support for realising critical sections for processes running in a distributed system. 
- A process must be able to use the functionality provided by the middleware without knowing anything about the other processes involved in the critical sections.
- The middleware must be designed/implemented using a MOM (such as RabbitMQ), using message passing. 
- Immagino che il middleware fornisca delle funzionalità d'alto livello per fare tipo "enter critical section" ed "exit
critical section", che sotto saranno implementate sempre tramite scambio di messaggi.
- L'obiettivo principale è sul come progettare la comunicazione, non è necessario pensare anche ad eventuali failure in questo
esercizio, quindi concentrandosi sui requisiti funzionali più che su altro. 

This exercise is mandatory only for students aiming at 30L.


### The deliverable

The deliverable must be a zipped folder `Assignment-04`, to be submitted on the course web site, including:  
- `src` directory with sources
- `doc` directory with a short report in PDF (`report.pdf`). The report should include:
	- A brief analsysis of the problem, focusing in particular aspects that are relevant from a  concurrent point of view.
	- A brief description of the strategy adopted

