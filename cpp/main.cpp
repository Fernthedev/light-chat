#include <fstream>
#include <string>
#include <iostream>
#include <sstream>
#include "main.h"
#include <wiringPi.h>

using namespace std;

int LED = 26;

LightController::LightController()
{
    wiringPiSetup();
}

LightController::LightController(int pinMode)
{
    LED = pinMode;
    wiringPiSetup();
}

void LightController::setLightSwitch(int pinSwitch, bool var)
{
    LED = pinSwitch;
    pinMode(LED, OUTPUT);

    if (var)
    {
        digitalWrite(LED, HIGH);
    }

    if (!var)
    {
        digitalWrite(LED, LOW);
    }
}

void LightController::setLightSwitch(bool var)
{
    setLightSwitch(LED, var);
}
