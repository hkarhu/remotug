# RemoTug

A game of 'tug-of-war' that can be played over great distances.

This version based on the previous remote-tug-of-war by <???>. 

Developed for Tiedeseura. Premiere at Joen Yö 2014.

---

# Compilation notes

## Hardware

###Schematics and wiring

The HX711 will connect to the Arduino

GND - GND
VCC - VCC
DT - A1
SCK - A0

Sensor will connect to the HX711

RED - E+
BLACK - E-
GREEN - A+
WHITE - A-

<img src="">

### Get and assemble

[Arduino Nano casing](http://www.thingiverse.com/thing:434245)

You will also need:

* 2x 3mm 20mm M-Screw
* 2x 3mm Nut
* Soldering tools and some wire
* Screwdriver

Print the case and assemble as depicted in image below:

<img src="">

## Firmware (Arduino project)

#### Download and install
[HX711 ADC Library](https://github.com/bogde/HX711.git)


## Game itself

Bindings:
*LWJGL
*RXTX
*Netty

#### Download and install

[Ä Library](https://github.com/irah-000/ae)


```
git clone https://github.com/irah-000/ae
cd ae
mvn install
```

### Build and run

```
<TODO>
```

---

<a rel="license" href="http://creativecommons.org/licenses/by-nc-sa/4.0/"><img alt="Creative Commons License" style="border-width:0" src="http://i.creativecommons.org/l/by-nc-sa/4.0/88x31.png" /></a><br /><span xmlns:dct="http://purl.org/dc/terms/" property="dct:title">RemoTug</span> is licensed under a <a rel="license" href="http://creativecommons.org/licenses/by-nc-sa/4.0/">Creative Commons Attribution-NonCommercial-ShareAlike 4.0 International License</a>.
