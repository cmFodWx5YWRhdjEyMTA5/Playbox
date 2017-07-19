
#include "mcc_generated_files/adc.h"
#include "mcc_generated_files/pin_manager.h"

#include "fishinput.h"
#include "fishstate.h"

#include <stdio.h>

uint32_t button_pressed_time;
bool last_button_state;
bool button_state;

void FISHINPUT_Initialize(void)
{
    // initialize anything we need to here...
    button_pressed_time = 0;
    last_button_state = false;
    button_state = false;
}

void FISHINPUT_process(void)
{
    // for interest we like to know the position of the potentiometer on 
    // the programming board and the internal chip temperature, calling 
    // these functions will set them on the state which might be of interest 
    // to us later
    FISHINPUT_getChipTemp();
    FISHINPUT_getPotPosition();
    
    
    // here is some real processing, we want to poll the state of the button
    // all the time so a little blip is ignored, we only want real, hard presses
    
    //TODO De-bouncing isn't working because the noise is incredible while
    //testing - let's just listen for the 1 and leave it at that for now
    bool buttonReading = IO_BTN_GetValue() == 1;
    if (buttonReading == true) {
        // the button is down
        if (last_button_state == false) {
            // wasn't pressed, now it is
            button_state = buttonReading;
            button_pressed_time = FISH_State.miliseconds;
        }
    }
    else if (last_button_state) {
        // button is released from being pressed, if not a long press
        // then this was a short press
        if (FISH_State.miliseconds - button_pressed_time > K_LONGBUTTONPRESSTIME) {
            // this was held down for more than a second - this is a long-press
            FISH_State.isLongButtonPress = true;
        }
        else {//TODO - add debounce if (FISH_State.miliseconds - button_pressed_time > K_DEBOUNCEDELAY) {
            // this was enough to register a quick press
            FISH_State.isButtonPress = true;
        }
    }
    last_button_state = buttonReading;
    
    // process this button press
    if (FISH_State.isButtonPress) {
        // move the time forward an hour
        FISH_State.miliseconds += K_MSECONDSINHOUR;
        // handled this, reset the flag
        FISH_State.isButtonPress = false;
        // calc time to fix any overrun
        FISHSTATE_calcTime();
        // and debug
        printf("Moved time forward an hour to %d\r\n", (int)(FISH_State.miliseconds / (K_MSECONDSINHOUR * 1.0)));
    }
    if (FISH_State.isLongButtonPress) {
        //TODO handle the long press for something...
        printf("That was a looooong, press\r\n");
        FISH_State.isLongButtonPress = false;
    }
    
    /*bool reading = IO_BTN_GetValue() == 1;
    if (reading) {
        printf(".");
    }
    else {
        printf("*");
    }
    // de-bounce this for fun, in case there is noise in the power
    if (reading != last_button_state) {
        // this is pressed, reset the down timer
        printf("-");
        button_pressed_time = FISH_State.miliseconds;
        last_button_state = reading;
    }
    if (reading != button_state && FISH_State.miliseconds - button_pressed_time > K_DEBOUNCEDELAY) {
        // whatever the reading is at, it's been there longer than the delay
        // so this is the actual state of the button
        button_state = reading;
        printf("button state is now %d\r\n", (button_state ? 1 : 0));
    }*/
}

float FISHINPUT_getHotPlateTemp(void)
{
    // get the reading from the sensor - using the created PIN ID
#ifdef K_DEBUG_HPT
    // if we are debugging then use the POT instead of the actual sensor
    adc_result_t sensor = FISHINPUT_getPotPosition();
    printf("DEBUGGING HOT PLATE\r\n");
#else
    // use the actual sensor
    adc_result_t sensor = ADC_GetConversion(channel_HPT);
#endif

    // this is set as a 12-bit 2s-compliment number so ranges from 0 to 4095
    float temp = sensor / 4095.0 * 100.0;
    // we want a global state of everything - so set that here direct
    FISH_State.hotPlateTemp = temp;
    // and return the result
    return temp;
}

float FISHINPUT_getWaterTemp(void)
{
    // get the reading from the sensor - using the created PIN ID
#ifdef K_DEBUG_WT
    // if we are debugging then use the POT instead of the actual sensor
    adc_result_t sensor = FISHINPUT_getPotPosition();
    printf("DEBUGGING WATER\r\n");
#else
    // use he actual sensor
    adc_result_t sensor = ADC_GetConversion(channel_WT);
#endif

    // this is set as a 12-bit 2s-compliment number so ranges from 0 to 4095
    float temp = sensor / 4095.0 * 100.0;
    // we want a global state of everything - so set that here direct
    FISH_State.waterTemp = temp;
    // and return the result
    return temp;
}

uint16_t FISHINPUT_getPotPosition(void)
{
    // get the reading
    adc_result_t sensor = ADC_GetConversion(channel_POT);
    // set it in the state
    FISH_State.potPosition = sensor;
    // and return it
    return sensor;
}

float FISHINPUT_getChipTemp(void)
{
    // get the reading from the internal sensor - just because we can (O:
    adc_result_t sensor = ADC_GetConversion(channel_Temperature);
    // this is set as a 12-bit 2s-compliment number so ranges from 0 to 4095
    float temp = sensor / 4095.0 * 100.0;
    // we want a global state of everything - so set that here direct
    FISH_State.chipTemp = temp;
    // and return the result
    return temp;
}
