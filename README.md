# HEMA tournament buzzer
This repo contains an Android app that plays a sound with a setable delay, when you press a button in the app or press a bluetooth remote shutter.
It is meant for HEMA tournament judges to stop the action after a hit and with the setable delay, you can use a fixed duration for the afterblow.

## Example Setup
If you have a typical tournament setup with one main judge and one assistant judge, you give each of them a bluetooth remote shutter.
If one of them sees a hit, they press the remote shutter and after the set delay (e.g. 0.5 seconds) a tone is played over the bluetooth speaker.

In case the phone itself is not loud enough, you can connect it to a speaker for increased volume.

Note: The phone screen needs to be on for the whole duration of the tournament.

## Advantages
- By not needing to shout "STOP" or "HALT" and instead relying on an app, the judges voices get less stressed. If you are in a loud fencing hall, just increase the volume of the speaker.
- Providing each judge with a way to stop the exchange strengthens the perspective and role of the assistant judge
- Judging gets more consistent by having a fixed afterblow duration
- As the afterblow is handled by the delay and the tone itself has an additional duration, every hit that occurs after the tone has ended is definately a late afterblow and can be carded right away, without discussions like "I couldn't stop my blade as it was already moving!"
- You can use a consistent volume for the tone that can be louder than a human would be able to shout for hours on end, which helps hearing restricted fencers.
