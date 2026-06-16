package cluster

import org.apache.pekko.actor.typed.*
import org.apache.pekko.actor.typed.scaladsl.*
import org.apache.pekko.cluster.sharding.typed.scaladsl.*
import com.typesafe.config.ConfigFactory
import scala.concurrent.duration.*

object App {

  def main(args: Array[String]): Unit =
    // Avvio 3 nodi nella stessa JVM, ognuno con una porta diversa.
    // Questo simula un cluster distribuito, ma senza bisogno di 3 processi separati.
    val ports =
      if args.isEmpty then
        Seq(25251, 25252, 2553)
      else
        args.map(_.toInt).toSeq
    ports.foreach(startup)

  private def startup(port: Int): Unit =
    // Sovrascrivo la porta del nodo direttamente da codice.
    // Questo permette di usare un unico application.conf per tutti i nodi.
    val config =
      ConfigFactory.parseString(
          s"""
             pekko.remote.artery.canonical.port = $port
             clustering.port = $port
           """
        ).withFallback(ConfigFactory.load("application.conf"))
    // Ogni chiamata crea un ActorSystem separato, quindi un nodo del cluster.
    ActorSystem(rootBehavior(), "ClusterSystem", config)

  def rootBehavior(): Behavior[Nothing] =
    Behaviors.setup[Nothing] { context =>
      val system = context.system
      val port = system.settings.config.getInt("clustering.port") // Porta del nodo corrente sulla quale farò poi
      // matching, serve per capire il nodo che funzione svolge nel sistema

      val sharding = ClusterSharding(system) // avvio del Cluster Sharding (abilita entità distribuite)

      import AlarmControllerEntity.Zone.*
      import AlarmControllerEntity.SensorType.*
      import SensorActor.Command.*
      import KeypadActor.Command.*

      val cfg = AlarmControllerEntity.Config("1234", 5.seconds, 5.seconds)

      sharding.init( // avvio entità distribuita "AlarmControllerEntity", fatto questo per quanto riguarda il controller
        // non c'è altro da fare, da qui in poi non fa altro che aspettare messaggi da keypad e sensori
        Entity(AlarmControllerEntity.TypeKey) { entityContext =>
          AlarmControllerEntity(entityContext.entityId, cfg)
        }
      )
      // Riferimento logico al controller shardato è un EntityRef non un ActorRef, quindi usabile da qualsiasi nodo del sistema
      // il cluster sharding si occuperà poi di trovare il nodo fisico su cui vive l'entità
      val controllerRef = sharding.entityRefFor(AlarmControllerEntity.TypeKey, "controller")

      // SENSORI (ne creo uno giusto per prova, la logica è la stessa del caso centralized)
      // In confronto al caso centralized faccio giusto un test per verificare che la recovery mode
      // funzioni, essendo la logica funzionale identica al caso centrlized do qui test per assodati
      if port == 25252 then
        val dayDoor = context.spawn(SensorActor(controllerRef, "dayDoor", Living, Door), "dayDoor") // questo sensore
        // creato deve inviare gli eventi al controller distribuito (passo controllerRef)
        context.scheduleOnce(12.seconds, dayDoor, Trigger) //triggero il sensore creato a t = 12

      // KEYPAD
      if port == 2553 then
        val keypad = context.spawn(KeypadActor(controllerRef), "keypad")
        val nightMode = Set(Living, Perimeter, Garage)

        // Per uscire dalla recovery mode, visto che l'avvio è di fatto un restart il primo stato
        // è il recovery state, quindi il primo pin serve per entrare in mod disarmed
        context.scheduleOnce(1.second, keypad, EnterPin("1234"))

        // Arma davvero il sistema (In modalità notte, arriverà un trigger a t=12 dalla zona living,
        // quindi l'allarme suona)
        context.scheduleOnce(3.seconds, keypad, EnterPin("1234", nightMode))

        // a t=20 viene spento l'allarme e sitema in stato disarmed
        context.scheduleOnce(20.seconds, keypad, EnterPin("1234"))

      Behaviors.empty
    }
}
