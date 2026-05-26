package actors

import org.apache.pekko.actor.typed.*
import org.apache.pekko.actor.typed.scaladsl.*

object KeypadActor:

  enum Command:
    case EnterPin(pin: String, zones: Set[AlarmControllerActor.Zone] = Set.empty)
    // Il default di zones = Set.empty mi serve quando bisogna immettere il pin per esempio per evitare l'allarme quindi
    // quando il sistema deve passare da modalità armed a disarmed, senza questo valore di default dal main bisognerebbe
    // passare manualmente un set fittizzio che in realtà non viene mai usato dal controller.
    // Il set contenente le zone da attivare mi serve solo quando il sistema riceve il pin ed è in modalità disarmed.

  import Command.*

  // Riceve un riferimento all'attore controller
  def apply(controller: ActorRef[AlarmControllerActor.Command]): Behavior[Command] =
    Behaviors.setup { context =>
      Behaviors.receiveMessage {
        // Qui il comportamento è molto semplice, quando riceve un messaggio esso può essere solo il pin
        // logga il pin inserito e invia PinEntered(pin, zones) al controller che lo dovrà poi gestire.
        // Il comportamento rimane sempre lo stesso
         case EnterPin(pin, zones) =>
           context.log.info(s"[KEYPAD] PIN inserito: $pin => invio al controller")
           controller ! AlarmControllerActor.Command.PinEntered(pin, zones)
           Behaviors.same
      }
    }
