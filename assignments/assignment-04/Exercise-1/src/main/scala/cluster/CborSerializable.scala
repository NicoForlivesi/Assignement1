package cluster

// CborSerializable è un marker trait vuoto richiesto da Pekko per la serializzazione dei messaggi
// che viaggiano tra nodi del cluster. Tutti i messaggi e le strutture dati che devono essere
// trasmessi in rete devono estendere questo trait, che internamente viene usato da Pekko
trait CborSerializable
