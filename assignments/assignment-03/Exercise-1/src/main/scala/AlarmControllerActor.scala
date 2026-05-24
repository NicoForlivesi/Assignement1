package main.scala

import org.apache.pekko.actor.typed.*
import org.apache.pekko.actor.typed.scaladsl.*
import scala.concurrent.duration.*

object AlarmController:
  
  enum Command:
    case PinEntered(pin: String)
    case SensorTriggered(sensorId: String, zone: Zone, sensorType: SensorType)
    case ExitDelayTimeout
    case EntryDelayTimeout
    case SetActiveZones(zones: Set[Zone])   // bonus

  import Command.*

  enum SensorType:
    case Motion
    case Door
    case Window

  enum Zone:
    case Living
    case Sleeping
    case Perimeter
    case Garage

  final case class Config(correctPin: String, exitDelay: FiniteDuration, entryDelay: FiniteDuration)

  def apply(config: Config): Behavior[Command] =
    Behaviors.withTimers { timers =>
      Behaviors.setup { context =>
        context.log.info("Sistema avviato in stato DISARMED")
        disarmed(context, timers, config, activeZones = Set.empty)
      }
    }

  private def disarmed(context: ActorContext[Command], timers: TimerScheduler[Command], config: Config,
                       activeZones: Set[Zone]): Behavior[Command] = Behaviors.receiveMessage:
    case PinEntered(pin) if pin == config.correctPin =>
      context.log.info("PIN corretto → avvio EXIT DELAY")
      timers.startSingleTimer(ExitDelayTimeout, config.exitDelay)
      exitDelay(context, timers, config, activeZones)
    case SensorTriggered(id, zone, t) =>
      context.log.info(s"[DISARMED] Evento da sensore $id ($t) in zona $zone ignorato")
      Behaviors.same
    case SetActiveZones(z) =>
      context.log.info(s"Zone attive aggiornate: $z")
      disarmed(context, timers, config, z)
    case _ =>
      Behaviors.same

  private def exitDelay(context: ActorContext[Command], timers: TimerScheduler[Command], config: Config,
                         activeZones: Set[Zone]): Behavior[Command] = Behaviors.receiveMessage:
    case ExitDelayTimeout =>
      context.log.info("EXIT DELAY terminato → sistema ARMATO")
      armed(context, timers, config, activeZones)
    case PinEntered(pin) if pin == config.correctPin =>
      context.log.info("PIN corretto durante EXIT DELAY → ritorno a DISARMED")
      disarmed(context, timers, config, activeZones)
    case SensorTriggered(id, zone, t) =>
      context.log.info(s"[EXIT DELAY] Ignoro evento da sensore $id ($t)")
      Behaviors.same
    case _ =>
      Behaviors.same

  private def armed(context: ActorContext[Command], timers: TimerScheduler[Command], config: Config,
                     activeZones: Set[Zone]): Behavior[Command] = Behaviors.receiveMessage:
    case PinEntered(pin) if pin == config.correctPin =>
      context.log.info("PIN corretto → DISARMED")
      disarmed(context, timers, config, activeZones)
    case SensorTriggered(id, zone, t) if activeZones.contains(zone) =>
      context.log.warn(s"[ARMED] Intrusione rilevata da $id → ENTRY DELAY")
      timers.startSingleTimer(EntryDelayTimeout, config.entryDelay)
      entryDelay(context, timers, config, activeZones)
    case SensorTriggered(id, zone, t) =>
      context.log.info(s"[ARMED] Sensore $id in zona inattiva $zone ignorato")
      Behaviors.same
    case _ =>
      Behaviors.same

  private def entryDelay(context: ActorContext[Command], timers: TimerScheduler[Command], config: Config,
                          activeZones: Set[Zone]): Behavior[Command] = Behaviors.receiveMessage:
    case PinEntered(pin) if pin == config.correctPin =>
      context.log.info("PIN corretto durante ENTRY DELAY → DISARMED")
      disarmed(context, timers, config, activeZones)
    case EntryDelayTimeout =>
      context.log.error("ENTRY DELAY scaduto → ALLARME")
      alarm(context, timers, config, activeZones)
    case SensorTriggered(_, _, _) =>
      context.log.info("[ENTRY DELAY] Evento ignorato")
      Behaviors.same
    case _ =>
      Behaviors.same

  private def alarm(context: ActorContext[Command], timers: TimerScheduler[Command], config: Config,
                     activeZones: Set[Zone]): Behavior[Command] = Behaviors.receiveMessage:
    case PinEntered(pin) if pin == config.correctPin =>
      context.log.info("PIN corretto → ALLARME DISATTIVATO → DISARMED")
      disarmed(context, timers, config, activeZones)
    case _ =>
      context.log.warn("[ALARM] Sistema in allarme, ignorando messaggi")
      Behaviors.same
