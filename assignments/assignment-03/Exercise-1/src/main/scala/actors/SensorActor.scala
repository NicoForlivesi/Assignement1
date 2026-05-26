package actors

import org.apache.pekko.actor.typed.*
import org.apache.pekko.actor.typed.scaladsl.*

object SensorActor:

  enum Command:
    case Trigger   // Il sensore rileva un evento

  import Command.*

  def apply(controller: ActorRef[AlarmControllerActor.Command], sensorId: String, zone: AlarmControllerActor.Zone,
             sensorType: AlarmControllerActor.SensorType): Behavior[Command] =
    Behaviors.setup { context =>
      Behaviors.receiveMessage {
        // Quando riceve un messaggio esso può essere solo il messaggio di trigger
        // logga e invia al controller che lo dovrà poi gestire il tutto in base allo stato in cui si trova.
        // Il comportamento rimane sempre lo stesso
        case Trigger =>
          context.log.info(s"[SENSOR $sensorId] Evento rilevato => invio al controller")
          controller ! AlarmControllerActor.Command.SensorTriggered(sensorId, zone, sensorType)
          Behaviors.same
      }
    }
