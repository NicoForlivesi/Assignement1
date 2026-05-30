package cluster

import org.apache.pekko.actor.typed.*
import org.apache.pekko.actor.typed.scaladsl.*
import org.apache.pekko.cluster.sharding.typed.scaladsl.*

object SensorActor:

  enum Command:
    case Trigger
    
  import Command.*
  
  def apply(controller: EntityRef[AlarmControllerEntity.Command], // EntityRef invece di ActorRef 
            sensorId: String, zone: AlarmControllerEntity.Zone, sensorType: AlarmControllerEntity.SensorType
           ): Behavior[Command] =
    Behaviors.setup { context =>
      context.log.info(s"[SENSOR $sensorId] inizializzato in zona $zone ($sensorType)")
      Behaviors.receiveMessage {
        case Trigger =>
          context.log.info(s"[SENSOR $sensorId] Evento rilevato => invio al controller (shardato)")
          controller ! AlarmControllerEntity.SensorTriggered(sensorId, zone, sensorType)
          Behaviors.same
      }
    }
