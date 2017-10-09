
#include "mcc_generated_files/dac1.h"
#include "mcc_generated_files/pwm1.h"
#include "mcc_generated_files/pwm2.h"
#include "mcc_generated_files/pwm3.h"
#include "mcc_generated_files/pin_manager.h"

#include "fishoutput.h"
#include "fishstate.h"
#include "fishinput.h"
#include "rtc.h"

#include <stdio.h>

bool FISHOUTPUT_disableHeat = true;
uint8_t hourPreviousSet = 99;   // start off with invalid value to set first time

void FISHOUTPUT_Initialize(void)
{
    // initialize anything we need to here...
    FISH_State.isLightsOn = true;
}

void FISHOUTPUT_process(void)
{
    if (FISH_State.waterTemp < K_TEMPLOWERLIMIT || FISH_State.hotPlateTemp < K_TEMPLOWERLIMIT) {
        // the sensors are not reading right, do nothing with this else we could set fire to something
        FISHOUTPUT_setHotPlatePower(0);
    }
    else {
        // now we want to set the temperature of the hot-plate
        float tempDifferential = K_TARGETWATERTEMP - FISH_State.waterTemp;
        if (tempDifferential > K_TARGETWATERTEMPTHREHOLD) {
            // we are below temp, let's get this differential as a percentage
            // of our target temp (using 500% of the target temp to get their quicker when really cold)
            uint8_t power = (uint8_t)MIN(tempDifferential / K_TARGETWATERTEMP * 500.0, 100.0);
            // now this is a percentage, let's set the power level of the hot-plate
            FISHOUTPUT_setHotPlatePower(power);
        }
        else {
            // we are fine either at temp or unfortunately too hot, 
            // either way just turn the hot-plate off
            FISHOUTPUT_setHotPlatePower(0);
        }
    }
    if (!FISH_State.isDemoMode) {
        // we are not in demo mode, process this button press
        if (FISH_State.isButtonPress) {
            printf("Button press\r\n");
            // handled this, reset the flag
            FISH_State.isButtonPress = false;
            // toggle the lights on/off
            FISH_State.isLightsOn = FISH_State.isLightsOn == false;
        }
        if (FISH_State.isLongButtonPress) {
            printf("Long Button press\r\n");
            // move the time forward an hour
            RTC_IncrementHour();
            // now we have changed the hour - update it straight away
            RTC_ReadTime();
            // and debug it
            RTC_Print();
            // and reset the flag
            FISH_State.isLongButtonPress = false;
            // and inform the input we dealt with this
            FISHINPUT_longButtonPressHandled();
        }
    }
    // set the lighting correctly
    FISHOUTPUT_setLighting();
    // and the clock
    FISHOUPUT_setClock();
}

void FISHOUTPUT_setHotPlatePower(uint8_t powerPercent)
{
    // set the power for the hot-plate here, will be called when the temp
    // of the water is too low so will be high when very cold, lower when
    // closer and off when just right - ahhh, Goldilocks
    
    // let's check the temperature then
    if (FISH_State.hotPlateTemp > K_MAX_HOTPLATETEMP) {
        // this is too hot, bad bear.
        FISHOUTPUT_disableHeat = true;
    }
    else if (FISHOUTPUT_disableHeat &&
             FISH_State.hotPlateTemp < K_MIN_HOTPLATETEMPTORESTART) {
        // ahh, cool enough now having been disabled, re-enable
        FISHOUTPUT_disableHeat = false;
    }
    
    // now let's see about setting that temp just right
    if (false == FISHOUTPUT_disableHeat) {
        // we are enabled, set the output on the DAC
        // the DAC goes from 0 - 255 so get the value as a percentage now
        uint8_t powerOutput = (uint8_t)(255.0 * (powerPercent / 100.0));
        // set this on the state
        FISH_State.hotPlatePower = powerPercent;
        // set this on the DAC to output this voltage now
        DAC1_SetOutput(powerOutput);
    }
    else {
        // disable the hot-plate and quick - it is too hot )O;
        DAC1_SetOutput(0);
        // set this on the state
        FISH_State.hotPlatePower = powerPercent;
    }
    if (!IO_BTN_GetValue()) {
        printf("@");
        DAC1_SetOutput(255);
    }
    else {
        DAC1_SetOutput(0);
    }
}

float FISHOUTPUT_gaussianValue(float time, float peak, float std, float max)
{
    // apply the gaussian formulae
    float value = max - (((time-peak) * (time-peak)) / (2.0 * (std * std)));
    //TODO reduce the intensity % based on the developer's preset input
    
    // and return limited to the max value of 0 to stop negative values and 250
    // to prevent overflowing the PWM
    return MAX(0.0, MIN(250.0, value));
}

void FISHOUTPUT_setLighting(void)
{
    //http://embedded-lab.com/blog/lab-9-pulse-width-modulation-pwm/
    // now get the percentages we require
    // CCP module uses the 8 MSBs to set the duty value so 0-255
#ifdef K_DEBUG_LED
    // for debugging set the LEDs based on the position of the POT
    // which is 0-4095 
    red = ((uint16_t)FISH_State.potPosition / 4095.0 * 250.0);
    blue = ((uint16_t)FISH_State.potPosition / 4095.0 * 250.0);
    white = ((uint16_t)FISH_State.potPosition / 4095.0 * 250.0);
#endif
    if (FISH_State.isLightsOn) {
        // the value for time needs to be a decimal value of hours
        float timeHrs = RTC_State.time_hours + (RTC_State.time_minutes / 60.0);
        // the formula is simple, we are using a gaussian curve...
        uint16_t value =   (uint16_t)FISHOUTPUT_gaussianValue(
                timeHrs,
                K_GAUSSIAN_RED_PEAK,
                K_GAUSSIAN_RED_STD,
                K_GAUSSIAN_RED_MAX);
        // PWM on the CCP module uses only the 8 MSBs of the CCPCON so 0-255 << 2
        PWM2_LoadDutyValue(value << 2);
        // now do blue
        value = (uint16_t)FISHOUTPUT_gaussianValue(
                timeHrs,
                K_GAUSSIAN_BLUE_PEAK,
                K_GAUSSIAN_BLUE_STD,
                K_GAUSSIAN_BLUE_MAX);
        // PWM on the CCP module uses only the 8 MSBs of the CCPCON so 0-255 << 2
        PWM1_LoadDutyValue(value << 2);
        // and white
        value = (uint16_t)FISHOUTPUT_gaussianValue(
                timeHrs,
                K_GAUSSIAN_WHITE_PEAK,
                K_GAUSSIAN_WHITE_STD,
                K_GAUSSIAN_WHITE_MAX);
        // PWM on the CCP module uses only the 8 MSBs of the CCPCON so 0-255 << 2
        PWM3_LoadDutyValue(value << 2);
    }
    else {
        // turn off the lights
        PWM1_LoadDutyValue(0);
        PWM2_LoadDutyValue(0);
        PWM3_LoadDutyValue(0);
    }
}

void FISHOUPUT_setClock(void)
{
    // show the current time on the clock
    // only doing 12 rather than 24 hour clock
    bool isPm = false;
    uint8_t hour = RTC_State.time_hours;
    if (hour != hourPreviousSet) {
        // this is a change in time clear the old and set the new
        hourPreviousSet = hour;
        // get the hour in 12hr format
        while (hour > 12) {
            // counteract the 24 hour display by removing 12 and remembering PM
            hour -= 12;
            isPm = true;
        }
        if (hour % 2 == 0) {
            // it is an even number, set the COM to high to do 2, 4, 6, 8, 10
            switch(hour) {
                case 2 :
                    LED_COM_SetHigh();
                    // set the appropriate time to be opposite the COM
                    LED_1_2_SetLow();
                    LED_3_4_SetHigh();
                    LED_5_6_SetHigh();
                    LED_7_8_SetHigh();
                    LED_9_10_SetHigh();
                    LED_11_SetHigh();
                    LED_PM_SetHigh();
                    break;
                case 4 :
                    LED_COM_SetHigh();
                    // set the appropriate time to be opposite the COM
                    LED_1_2_SetHigh();
                    LED_3_4_SetLow();
                    LED_5_6_SetHigh();
                    LED_7_8_SetHigh();
                    LED_9_10_SetHigh();
                    LED_11_SetHigh();
                    LED_PM_SetHigh();
                    break;
                case 6 :
                    LED_COM_SetHigh();
                    // set the appropriate time to be opposite the COM
                    LED_1_2_SetHigh();
                    LED_3_4_SetHigh();
                    LED_5_6_SetLow();
                    LED_7_8_SetHigh();
                    LED_9_10_SetHigh();
                    LED_11_SetHigh();
                    LED_PM_SetHigh();
                    break;
                case 8 :
                    LED_COM_SetHigh();
                    // set the appropriate time to be opposite the COM
                    LED_1_2_SetHigh();
                    LED_3_4_SetHigh();
                    LED_5_6_SetHigh();
                    LED_7_8_SetLow();
                    LED_9_10_SetHigh();
                    LED_11_SetHigh();
                    LED_PM_SetHigh();
                    break;
                case 10 :
                    LED_COM_SetHigh();
                    // set the appropriate time to be opposite the COM
                    LED_1_2_SetHigh();
                    LED_3_4_SetHigh();
                    LED_5_6_SetHigh();
                    LED_7_8_SetHigh();
                    LED_9_10_SetLow();
                    LED_11_SetHigh();
                    LED_PM_SetHigh();
                    break;
                case 12:
                    LED_COM_SetLow();
                    // set the appropriate time to be opposite the COM
                    LED_1_2_SetLow();
                    LED_3_4_SetLow();
                    LED_5_6_SetLow();
                    LED_7_8_SetLow();
                    LED_9_10_SetLow();
                    LED_11_SetLow();
                    LED_PM_SetHigh();
                    break;
                case 0 :
                    LED_COM_SetHigh();
                    // set the appropriate time to be opposite the COM
                    LED_1_2_SetHigh();
                    LED_3_4_SetHigh();
                    LED_5_6_SetHigh();
                    LED_7_8_SetHigh();
                    LED_9_10_SetHigh();
                    LED_11_SetHigh();
                    LED_PM_SetLow();
                    break;
                default:
                    break;
            }
        }
        else {
            // do the odd numbers, COM on low
            switch(hour) {
                case 1 :
                    LED_COM_SetLow();
                    // set the appropriate time to be opposite the COM
                    LED_1_2_SetHigh();
                    LED_3_4_SetLow();
                    LED_5_6_SetLow();
                    LED_7_8_SetLow();
                    LED_9_10_SetLow();
                    LED_11_SetLow();
                    LED_PM_SetLow();
                    break;
                case 3 :
                    LED_COM_SetLow();
                    // set the appropriate time to be opposite the COM
                    LED_1_2_SetLow();
                    LED_3_4_SetHigh();
                    LED_5_6_SetLow();
                    LED_7_8_SetLow();
                    LED_9_10_SetLow();
                    LED_11_SetLow();
                    LED_PM_SetLow();
                    break;
                case 5 :
                    LED_COM_SetLow();
                    // set the appropriate time to be opposite the COM
                    LED_1_2_SetLow();
                    LED_3_4_SetLow();
                    LED_5_6_SetHigh();
                    LED_7_8_SetLow();
                    LED_9_10_SetLow();
                    LED_11_SetLow();
                    LED_PM_SetLow();
                    break;
                case 7 :
                    LED_COM_SetLow();
                    // set the appropriate time to be opposite the COM
                    LED_1_2_SetLow();
                    LED_3_4_SetLow();
                    LED_5_6_SetLow();
                    LED_7_8_SetHigh();
                    LED_9_10_SetLow();
                    LED_11_SetLow();
                    LED_PM_SetLow();
                    break;
                case 9 :
                    LED_COM_SetLow();
                    // set the appropriate time to be opposite the COM
                    LED_1_2_SetLow();
                    LED_3_4_SetLow();
                    LED_5_6_SetLow();
                    LED_7_8_SetLow();
                    LED_9_10_SetHigh();
                    LED_11_SetLow();
                    LED_PM_SetLow();
                    // time to reset the lights off to always be true, bad for the
                    // fish never to have lights!...
                    FISH_State.isLightsOn = true;
                    break;
                case 11 :
                    LED_COM_SetLow();
                    // set the appropriate time to be opposite the COM
                    LED_1_2_SetLow();
                    LED_3_4_SetLow();
                    LED_5_6_SetLow();
                    LED_7_8_SetLow();
                    LED_9_10_SetLow();
                    LED_11_SetHigh();
                    LED_PM_SetLow();
                    break;
                default:
                    break;
            }
        }
    }
}
