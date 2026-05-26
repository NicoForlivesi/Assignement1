package actors

import org.apache.pekko.actor.typed.*
import org.apache.pekko.actor.typed.scaladsl.*

object SirenActor:

  enum Command:
    case Start
    case Stop

  import Command.*

  def apply(): Behavior[Command] =
    Behaviors.setup { context =>
      Behaviors.receiveMessage { 
        case Start => 
          context.log.warn("SIRENA ATTIVA! L'allarme sta suonando!")
          Behaviors.same 
        case Stop =>
          context.log.info("Sirena disattivata")
          Behaviors.same
      }
    }
