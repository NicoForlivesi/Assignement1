package cluster

import org.apache.pekko.actor.typed.*
import org.apache.pekko.actor.typed.scaladsl.*
import org.apache.pekko.cluster.sharding.typed.scaladsl.*
import scala.concurrent.duration.*


object AlarmControllerEntity {

  // entityTypeKey (necessario per Cluster Sharding), serve per identificare l'entità shardata nel cluster
  val TypeKey: EntityTypeKey[Command] = EntityTypeKey[Command]("AlarmController")

  sealed trait Command extends CborSerializable

  final case class PinEntered(pin: String, zones: Set[Zone]) extends Command
  final case class SensorTriggered(sensorId: String, zone: Zone, sensorType: SensorType) extends Command

  case object ExitDelayTimeout extends Command
  case object EntryDelayTimeout extends Command

  case object EnterRecoveryMode extends Command

  enum SensorType:
    case Motion, Door, Window

  enum Zone:
    case Living, Sleeping, Perimeter, Garage

  final case class Config(correctPin: String, exitDelay: FiniteDuration, entryDelay: FiniteDuration) extends CborSerializable

  // Ogni volta che il cluster ricrea l'entità parte in Recovery Mode a differenza del caso concentrato che
  // partiva dallo stato disarmed
  def apply(entityId: String, config: Config): Behavior[Command] =
    Behaviors.withTimers { timers =>
      Behaviors.setup { context =>
        context.log.info(s"[CONTROLLER] Avviato come entità shardata con ID = $entityId")
        recoveryMode(context, timers, config)
      }
    }

  def recoveryMode(context: ActorContext[Command], timers: TimerScheduler[Command], config: Config): Behavior[Command] =
    Behaviors.receiveMessage {
      case PinEntered(pin, _) =>
        if pin == config.correctPin then
          context.log.info("[RECOVERY] PIN corretto => DISARMED")
          disarmed(context, timers, config, Set.empty)
        else
          context.log.info("[RECOVERY] PIN errato!")
          Behaviors.same
      case _ =>
        Behaviors.same
    }

  // Da qui in poi la logica funzionale è uguale alla versione centralizzata
  def disarmed(context: ActorContext[Command], timers: TimerScheduler[Command], config: Config, activeZones: Set[Zone]): Behavior[Command] =
    Behaviors.receiveMessage {
      case PinEntered(pin, zones) =>
        if pin == config.correctPin && zones.nonEmpty then
          context.log.info(s"PIN corretto => attivo zone $zones => EXIT DELAY")
          timers.startSingleTimer(ExitDelayTimeout, config.exitDelay)
          exitDelay(context, timers, config, zones)
        else
          context.log.warn("Il PIN inserito non è corretto!")
          Behaviors.same
      case SensorTriggered(id, zone, t) =>
        context.log.info(s"[DISARMED] Evento da sensore $id ($t) in zona $zone ignorato")
        Behaviors.same
      case _ => Behaviors.same
    }

  def exitDelay(context: ActorContext[Command], timers: TimerScheduler[Command], config: Config, activeZones: Set[Zone]): Behavior[Command] =
    Behaviors.receiveMessage {
      case ExitDelayTimeout =>
        context.log.info("[EXIT DELAY] Terminato => ARMED")
        armed(context, timers, config, activeZones)
      case SensorTriggered(id, zone, t) =>
        context.log.info(s"[EXIT DELAY] Ignoro evento da sensore $id ($t)")
        Behaviors.same
      case _ => Behaviors.same
    }

  def armed(context: ActorContext[Command], timers: TimerScheduler[Command], config: Config, activeZones: Set[Zone]): Behavior[Command] =
    Behaviors.receiveMessage {
      case PinEntered(pin, _) =>
        if pin == config.correctPin then
          context.log.info("[ARMED] PIN corretto => DISARMED")
          disarmed(context, timers, config, activeZones)
        else
          context.log.warn("[ARMED] PIN errato")
          Behaviors.same
      case SensorTriggered(id, zone, t) =>
        if activeZones.contains(zone) then
          context.log.warn(s"[ARMED] Intrusione da $id => ENTRY DELAY")
          timers.startSingleTimer(EntryDelayTimeout, config.entryDelay)
          entryDelay(context, timers, config, activeZones)
        else
          context.log.info(s"[ARMED] Sensore $id in zona inattiva $zone ignorato")
          Behaviors.same
      case _ => Behaviors.same
    }

  def entryDelay(context: ActorContext[Command], timers: TimerScheduler[Command], config: Config, activeZones: Set[Zone]): Behavior[Command] =
    Behaviors.receiveMessage {
      case PinEntered(pin, _) =>
        if pin == config.correctPin then
          context.log.info("[ENTRY DELAY] PIN corretto => DISARMED")
          disarmed(context, timers, config, activeZones)
        else
          context.log.warn("[ENTRY DELAY] PIN errato")
          Behaviors.same
      case EntryDelayTimeout =>
        context.log.error("ENTRY DELAY Scaduto => ALLARME")
        context.log.error("SIRENA ATTIVA !!!")
        alarm(context, timers, config, activeZones)
      case SensorTriggered(_, _, _) =>
        context.log.info("[ENTRY DELAY] Evento ignorato")
        Behaviors.same
      case _ => Behaviors.same
    }

  def alarm(context: ActorContext[Command], timers: TimerScheduler[Command], config: Config, activeZones: Set[Zone]): Behavior[Command] =
    Behaviors.receiveMessage {
      case PinEntered(pin, _) =>
        if pin == config.correctPin then
          context.log.info("[ALARM] PIN corretto => DISARMED")
          context.log.info("Sirena disattivata")
          disarmed(context, timers, config, activeZones)
        else
          context.log.warn("[ALARM] PIN errato")
          Behaviors.same
      case _ =>
        context.log.warn("[ALARM] Sistema in allarme, immettere il PIN per disattivare")
        Behaviors.same
    }
}
