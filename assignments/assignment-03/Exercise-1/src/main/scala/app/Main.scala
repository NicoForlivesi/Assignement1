package app

import actors.*
import actors.KeypadActor.Command.*
import actors.SensorActor.Command.*
import actors.SirenActor
import org.apache.pekko.actor.typed.*
import org.apache.pekko.actor.typed.scaladsl.*
import scala.concurrent.duration.*

object Main:
  def apply(): Behavior[Nothing] = // serve solo per creare gli attori, non riceve messaggi infatti Behavior Nothing
    Behaviors.setup[Nothing] { context =>
      val config = AlarmControllerActor.Config(
        correctPin = "1234",
        exitDelay = 5.seconds,
        entryDelay = 5.seconds
      )
      val siren = context.spawn(SirenActor(), "siren")
      val controller = context.spawn(
        AlarmControllerActor(config, siren),
        "controller"
      )
      val keypad = context.spawn(
        KeypadActor(controller),
        "keypad"
      )
      def mkSensor(id: String, zone: AlarmControllerActor.Zone, sensorType: AlarmControllerActor.SensorType):
      ActorRef[SensorActor.Command] = // Funzioncina helper per far prima a creare i sensori
        context.spawn(
          SensorActor(controller, id, zone, sensorType),
          id
        )

      import AlarmControllerActor.Zone.*
      import AlarmControllerActor.SensorType.*

      // ZONA NOTTE
      val nightMotion = mkSensor("nightMotion", Sleeping, Motion)
      val nightWindow1 = mkSensor("nightWindow1", Sleeping, Window)
      val nightWindow2 = mkSensor("nightWindow2", Sleeping, Window)
      val nightWindow3 = mkSensor("nightWindow3", Sleeping, Window)

      // ZONA GIORNO
      val dayMotion = mkSensor("dayMotion", Living, Motion)
      val dayWindow1 = mkSensor("dayWindow1", Living, Window)
      val dayWindow2 = mkSensor("dayWindow2", Living, Window)
      val dayDoor = mkSensor("dayDoor", Living, Door)

      // PERIMETRO (tipo 3 telecamere esterne che rilevano movimento)
      val perimeter1 = mkSensor("perimeter1", Perimeter, Motion)
      val perimeter2 = mkSensor("perimeter2", Perimeter, Motion)
      val perimeter3 = mkSensor("perimeter3", Perimeter, Motion)

      // GARAGE
      val garageDoor = mkSensor("garageDoor", Garage, Door)

      val allZones = Set(Living, Sleeping, Perimeter, Garage)
      val nightMode = Set(Living, Perimeter, Garage)
      val onlyPerimeter = Set(Perimeter)

      // TEST
      def userBackHomeWithFullArmedSystem(): Unit =
      /**
      * Qui simulo il caso in cui una persona torna a casa con l'allarme inserito, triggera una telecamera nel
      * perimetro, triggera la porta d'ingresso e il sensore di movimento all' ingresso ma
      * inserisce il pin prima che ul countdown termini, quindi spegne il sistema d'allarme prima che suoni.
      * */
        context.log.info("=== SCENARIO 1: Utente torna a casa e spegne il sistema ===")
        context.scheduleOnce(1.second, keypad, EnterPin("1234", allZones))
        // Exit delay finito quindi sistema armed a 6.seconds
        context.scheduleOnce(8.seconds, perimeter1, Trigger) // --> Entry delay
        context.scheduleOnce(10.seconds, dayDoor, Trigger) // trigger di un altro sensore durante Entry delay
        // non fa cambiare stato al sistema
        context.scheduleOnce(11.seconds, dayMotion, Trigger) // stessa cosa
        context.scheduleOnce(12.seconds, keypad, EnterPin("1234")) // codice inserito prima della fine del countdown
        // il sistema viene riportato in modalità disarmed

      def intrusionRevealed(): Unit =
        /**
        * Qui simuliamo il caso in cui ci sia effettivamente un tentativo di intrusione, scenario uguale al caso
        * precedente ma qui il pin non viene inserito in tempo e l'allarme suona, dopo qualche secondo quando
        * si ipotizza l'intruso sia scappato viene spento tramite pin
         * */
        context.log.info("=== SCENARIO 2: Tentativo di intrusione, l'allarme suona ===")
        context.scheduleOnce(1.second, keypad, EnterPin("1234", allZones))
        context.scheduleOnce(8.seconds, perimeter1, Trigger)
        context.scheduleOnce(10.seconds, dayDoor, Trigger)
        context.scheduleOnce(11.seconds, dayMotion, Trigger)
        // L'allarme suona a 13.seconds e viene spento a 25.seconds
        context.scheduleOnce(25.seconds, keypad, EnterPin("1234"))

      def nightModeScenario(): Unit =
        /**
        * Qui simulo il caso di uso notturno in cui tutti i sensori sono attivi tranne quelli nella zona notte,
        * in modo da permettere all'utente di muoversi liberamente nella zona notte e per esempio dormire con le
        * finestre aperte.
        * Nella parte finale dello scenario simulo la rilevazione del sensore di movimento nella zona giorno
        * ipotizzando per esempio la mattina seguente in cui l'utente si reca in zona giorno per disarmare il sistema.
        * */
        context.log.info("=== SCENARIO 3: Uso notturno, l'utente può muoversi liberamente in zona notte ===")
        context.scheduleOnce(1.second, keypad, EnterPin("1234", nightMode)) // -> sensori zona notte spenti
        // Questi 3 trigger non hanno effetto sullo stato del sistema
        context.scheduleOnce(10.seconds, nightWindow1, Trigger)
        context.scheduleOnce(15.seconds, nightMotion, Trigger)
        context.scheduleOnce(20.seconds, nightWindow3, Trigger)

        context.scheduleOnce(24.seconds, dayMotion, Trigger) // La mattina seguente
        context.scheduleOnce(25.seconds, keypad, EnterPin("1234")) // Sistema disarmato

      def onlyPerimeterScenario(): Unit =
        /**
         * In questo scenario attivo solo i sensori sul perimetro in questo modo tutti i sensori all'interno della
         * casa non fanno scattare l'allarme, per test simulo il trigger di 1 sensore per ogni zona non attiva e
         * infine il caso in cui sia rilevata una vera intrusione (quindi da uno dei sensori di movimento del perimetro
         * il che quindi fa scattare l'allarme).
         */
        context.log.info("=== SCENARIO 4: Solo perimetro, l'utente puo muoversi e aprire finestre ===")
        context.scheduleOnce(1.second, keypad, EnterPin("1234", onlyPerimeter)) // -> sensori solo perimetro

        context.scheduleOnce(10.seconds, nightWindow1, Trigger)
        context.scheduleOnce(12.seconds, dayWindow2, Trigger)
        context.scheduleOnce(15.seconds, garageDoor, Trigger)

        context.scheduleOnce(20.seconds, perimeter2, Trigger) // movimento all'esterno
        // --> L'allarme suona
        context.scheduleOnce(30.seconds, keypad, EnterPin("1234")) // Sistema disarmato

      def someoneTryingToGuessPin(): Unit =
        /**
         Simulo il caso in cui un intruso tenti di inserire il pin per spegnere l'allarme ma non conoscendolo
         non riesce a fermare l'allarme, quindi se il pin è sbagliato, l'allarme continua a suonare.
         */
        context.log.info("=== SCENARIO 5: L'intruso sbaglia pin e l'allarme suona ===")
        context.scheduleOnce(1.second, keypad, EnterPin("1234", allZones))

        context.scheduleOnce(10.seconds, perimeter3, Trigger)
        // --> L'allarme suona
        context.scheduleOnce(16.seconds, dayDoor, Trigger)
        // --> continua a suonare
        context.scheduleOnce(20.seconds, keypad, EnterPin("0000")) // Pin sbagliato, continua a suonare...

      /**Scenari testabili (uno per volta per come ho definito le sequenze temporali)*/
//      userBackHomeWithFullArmedSystem()
//      intrusionRevealed()
      nightModeScenario()
//      onlyPerimeterScenario()
//      someoneTryingToGuessPin()

      Behaviors.empty
    }

@main def runAlarmSystem(): Unit =
  ActorSystem(Main(), "AlarmSystem")

