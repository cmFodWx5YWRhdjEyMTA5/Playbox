
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
    // to represent +2 to +150 degrees
    float temp = (sensor / 4095.0 * 100.0) + 0.0;
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
    // use the actual sensor
    adc_result_t sensor = ADC_GetConversion(channel_WT);
#endif
    // this is set as a 12-bit 2s-compliment number so ranges from 0 to 4095
    float temp = sensor / 4095.0 * 100.0;
    // we want a global state of everything - so set that here direct
    FISH_State.waterTemp = temp;
    // and return the result
    return temp;
}

#if defined(K_DEBUG_WT) || defined(K_DEBUG_HPT)
uint16_t FISHINPUT_getPotPosition(void)
{
    // get the reading
    adc_result_t sensor = ADC_GetConversion(channel_POT);
    // set it in the state
    FISH_State.potPosition = sensor;
    // and return it
    return sensor;
}
#endif

#ifdef K_DEBUG
float FISHINPUT_getChipTemp(void)
{
    // get the reading from the internal sensor - just because we can (O:
    adc_result_t sensor = ADC_GetConversion(channel_Temperature);
    // this is set as a 12-bit 2s-compliment number so ranges from 0 to 4095
    float temp = sensor / 4095.0 * 100.0;
    // we want a global state of everything - so set that here direct
    FISH_State.chipTemp = (uint16_t)temp;
    // and return the result
    return temp;
}
#endif

float FISHINPUT_getIntensity(void)
{
    // get the developer-set intentisy value
    adc_result_t sensor = ADC_GetConversion(channel_INPT);
    // this is set as a 12-bit 2s-compliment number so ranges from 0 to 4095
    float reading = sensor / 4095.0 * 100.0;
    // we want a global state of everything - so set that here direct
    FISH_State.intensity = reading;
    // and return the result
    return reading;
}

void FISHINPUT_process(void)
{       
#ifdef K_DEBUG
    // get the chip temp for our interest
    FISHINPUT_getChipTemp();
#endif
#if defined(K_DEBUG_WT) || defined(K_DEBUG_HPT)
    // for debugging we can get the POT position too
    FISHINPUT_getPotPosition();
#endif
    // we want to get our state from sensors etc
    FISHINPUT_getHotPlateTemp();
    FISHINPUT_getWaterTemp();
    FISHINPUT_getIntensity();

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
