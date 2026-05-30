package cluster

import org.apache.pekko.actor.typed.*
import org.apache.pekko.actor.typed.scaladsl.*
import org.apache.pekko.cluster.sharding.typed.scaladsl.*

object KeypadActor:

  enum Command:
    case EnterPin(pin: String, zones: Set[AlarmControllerEntity.Zone] = Set.empty)

  import Command.*
  // L'unica differenza rispetto al caso concentrato è che questa volta il controller è passato come
  // EntityRef alla factory invece che come ActorRef
  def apply(controller: EntityRef[AlarmControllerEntity.Command]): Behavior[Command] =
    Behaviors.setup { context =>
      context.log.info("[KEYPAD] Avviato e pronto")
      Behaviors.receiveMessage {
        case EnterPin(pin, zones) =>
          context.log.info(s"[KEYPAD] PIN inserito: $pin => invio al controller (shardato)")
          controller ! AlarmControllerEntity.PinEntered(pin, zones)
          Behaviors.same
      }
    }
