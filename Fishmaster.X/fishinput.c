
#include "mcc_generated_files/adc.h"
#include "mcc_generated_files/pin_manager.h"
#include "mcc_generated_files/eusart.h"

#include "fishinput.h"
#include "fishstate.h"
#include "rtc.h"

#include <stdio.h>

uint32_t button_pressed_time;
bool last_button_state;
bool button_state;
bool wasLongButtonPress;
bool isPowerOn;

void FISHINPUT_Initialize(void)
{
    // initialize anything we need to here...
    button_pressed_time = 0;
    last_button_state = false;
    button_state = false;
    wasLongButtonPress = false;
    isPowerOn = true;
    
    // setup the button
    IO_BTN_SetDigitalInput();
}

void FISHINPUT_longButtonPressHandled(void)
{
    // when the long button press is handled, reset the timer
    button_pressed_time = FISH_State.milliseconds;
    wasLongButtonPress = true;
}

void FISHINPUT_process(void)
{
    // get the reading from the sensor - using the created PIN ID
    // this is set as a 12-bit 2s-compliment number so ranges from 0 to 4095
    // to represent +2 to +150 degrees
    // 1.5v is 150 degrees (0 - 1228)
    FISH_State.hotPlateTemp = ADC_GetConversion(channel_HPT)/ 1228.0 * 148.0;
    FISH_State.waterTemp = ADC_GetConversion(channel_WT) / 1228.0 * 148.0;
    //FISH_State.intensity = ADC_GetConversion(channel_INPT) / 4095.0 * 100.0;
    
    // read the state of the button to get short and long presses
    bool buttonReading = !IO_BTN_GetValue();
    if (buttonReading == true) {
        // the button is down
        if (last_button_state == false) {
            // wasn't pressed, now it is
            button_state = buttonReading;
            button_pressed_time = FISH_State.milliseconds;
        }
        // the button is down, has it been down a while?
        if (FISH_State.milliseconds - button_pressed_time > K_LONGBUTTONPRESSTIME) {
            // this was held down for more than a second - this is a long-press
            if (isPowerOn) {
                // the button was down, and continued to be down while the power
                // came on, this is a request to put this in demo mode, do that
                // instead of registering the long button press
                FISH_State.isDemoMode = true;
            }
            else {
                // this is just a long press of the button
                FISH_State.isLongButtonPress = true;
            }
        }
    }
    else {
        // button is released
        if (last_button_state) {
            // button is released from being pressed, if not a long press
            // then this was a short press
            if (false == wasLongButtonPress &&
                FISH_State.milliseconds - button_pressed_time > K_SHORTBUTTONPRESSTIME &&
                FISH_State.milliseconds - button_pressed_time < K_LONGBUTTONPRESSTIME) {
                // this was enough to register a quick press, not a long press
                FISH_State.isButtonPress = true;
            }
            // released now, reset any long button press we might have picked up
            wasLongButtonPress = false;
        }
        // isPowerOn can no longer be true, the button is up 
        isPowerOn = false;
    }
    // remember the button state
    last_button_state = buttonReading;
}