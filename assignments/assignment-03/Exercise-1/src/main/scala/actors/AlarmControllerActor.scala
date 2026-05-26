package actors

import org.apache.pekko.actor.typed.*
import org.apache.pekko.actor.typed.scaladsl.*
import scala.concurrent.duration.*

object AlarmControllerActor:
  
  enum Command:
    case PinEntered(pin: String, zones: Set[Zone])
    case SensorTriggered(sensorId: String, zone: Zone, sensorType: SensorType)
    case ExitDelayTimeout
    case EntryDelayTimeout

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

  def apply(config: Config, siren: ActorRef[SirenActor.Command]): Behavior[Command] =
    Behaviors.withTimers { timers =>
      Behaviors.setup { context =>
        disarmed(context, timers, config, activeZones = Set.empty, siren)
      }
    }

  private def disarmed(context: ActorContext[Command], timers: TimerScheduler[Command], config: Config,
                       activeZones: Set[Zone], siren: ActorRef[SirenActor.Command]): Behavior[Command] = Behaviors.receiveMessage:
    case PinEntered(pin, zones) =>
      if pin == config.correctPin && zones.nonEmpty then
        context.log.info(s"PIN corretto => attivo zone $zones → avvio EXIT DELAY")
        timers.startSingleTimer(ExitDelayTimeout, config.exitDelay)
        exitDelay(context, timers, config, zones, siren)
      else
        context.log.info("Il PIN inserito non è corretto!")
        Behaviors.same
    case SensorTriggered(id, zone, t) =>
      context.log.info(s"[DISARMED] Evento da sensore $id ($t) in zona $zone ignorato")
      Behaviors.same
    case _ =>
      Behaviors.same

  private def exitDelay(context: ActorContext[Command], timers: TimerScheduler[Command], config: Config,
                         activeZones: Set[Zone], siren: ActorRef[SirenActor.Command]): Behavior[Command] = Behaviors.receiveMessage:
    // Qui non ha senso logicamente mettere il caso "PinEntered", se l'allarme è stato acceso e si vuole
    // spegnere si aspettano i pochi secondi di extiDelay prima di reinserire il pin, il che fa passare il
    // sistema in stato "armed" che gestisce l'inserimento del pin.
    case ExitDelayTimeout =>
      context.log.info("EXIT DELAY terminato => sistema ARMATO")
      armed(context, timers, config, activeZones, siren)
    case SensorTriggered(id, zone, t) =>
      context.log.info(s"[EXIT DELAY] Ignoro evento da sensore $id ($t)")
      Behaviors.same
    case _ =>
      Behaviors.same

  private def armed(context: ActorContext[Command], timers: TimerScheduler[Command], config: Config,
                     activeZones: Set[Zone], siren: ActorRef[SirenActor.Command]): Behavior[Command] = Behaviors.receiveMessage:
    case PinEntered(pin, _) =>
      /*
    Questo è un caso particolare ma ha comunque senso metterlo, in generale il sistema per passare da
    stato "armed" a stato "disarmed" deve sempre passare prima per lo stato entryDelay visto che viene sempre
    triggerato almeno un sensore, tranne nel caso particolare in cui per esempio l'allarme era stato attivato
    solo per il perimetro, prendiamo per esempio il caso di notte in cui la mattina l'allarme viene spento dall'interno
    senza triggerare alcun sensore, metto questo case per gestire questa sottile situazione particolare.
    */
      if pin == config.correctPin then
        context.log.info("PIN corretto => DISARMED")
        disarmed(context, timers, config, activeZones, siren)
      else
        context.log.info("Il PIN inserito non è corretto!")
        Behaviors.same
    case SensorTriggered(id, zone, t)  =>
      if activeZones.contains(zone) then
        context.log.warn(s"[ARMED] Intrusione rilevata da $id => ENTRY DELAY")
        timers.startSingleTimer(EntryDelayTimeout, config.entryDelay)
        entryDelay(context, timers, config, activeZones, siren)
      else
        context.log.info(s"[ARMED] Sensore $id in zona inattiva $zone ignorato")
        Behaviors.same
    case _ =>
      Behaviors.same

  private def entryDelay(context: ActorContext[Command], timers: TimerScheduler[Command], config: Config,
                          activeZones: Set[Zone], siren: ActorRef[SirenActor.Command]): Behavior[Command] = Behaviors.receiveMessage:
    case PinEntered(pin, _) =>
      if pin == config.correctPin then
        context.log.info("PIN corretto durante ENTRY DELAY => DISARMED")
        disarmed(context, timers, config, activeZones, siren)
      else
        context.log.info("Il PIN inserito non è corretto!")
        Behaviors.same
    case EntryDelayTimeout =>
      context.log.error("ENTRY DELAY scaduto => ALLARME")
      siren ! SirenActor.Command.Start
      alarm(context, timers, config, activeZones, siren)
    case SensorTriggered(_, _, _) =>
      context.log.info("[ENTRY DELAY] Evento ignorato")
      Behaviors.same
    case _ =>
      Behaviors.same

  private def alarm(context: ActorContext[Command], timers: TimerScheduler[Command], config: Config,
                     activeZones: Set[Zone], siren: ActorRef[SirenActor.Command]): Behavior[Command] = Behaviors.receiveMessage:
    case PinEntered(pin, _) =>
      if pin == config.correctPin then
        context.log.info("PIN corretto => DISARMED")
        siren ! SirenActor.Command.Stop
        disarmed(context, timers, config, activeZones, siren)
      else
        context.log.info("Il PIN inserito non è corretto!")
        Behaviors.same
    case _ =>
      context.log.warn("[ALARM] Sistema in allarme, immettere il PIN per disattivare")
      Behaviors.same
