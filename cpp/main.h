#ifndef LIGHT_CLASS_H
#define LIGHT_CLASS_H

#include <string>
using namespace std;
/* GPIO Class
 * Purpose: Each object instantiated from this class will control a GPIO pin
 * The GPIO pin number must be passed to the overloaded class constructor
 */
class LightController
{
  public:
    LightController();            // create a GPIO object that controls GPIO4 (default
    LightController(int pinMode); // create a GPIO object that controls GPIOx, where x is passed to this constructor
    void setLightSwitch(int pinSwitch, bool var);
    void setLightSwitch(bool var);

  private:
};

#endif